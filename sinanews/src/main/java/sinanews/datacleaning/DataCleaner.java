package sinanews.datacleaning;

import common.database.CouchDbConnectorUtil;
import common.scraping.ConfigLoader;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataCleaner {

    public static void main(String[] args) {
        // 1. Load the config file
        Properties config = ConfigLoader.loadConfig("config_sina_news.properties");
        if (config == null) {
            System.err.println("Failed to load config file. Exiting...");
            return;
        }

        // 2. Get database configuration
        String couchdbUrl   = config.getProperty("couchdb.url");
        String sourceDbName = config.getProperty("couchdb.database_raw");
        String targetDbName = config.getProperty("couchdb.database_cleaned");

        // 3. Retrieve CouchDB credentials from environment variables
        String username = System.getenv("COUCHDB_USERNAME");
        String password = System.getenv("COUCHDB_PASSWORD");

        // 4. Connect to source & target DBs
        CouchDbConnector sourceDb = CouchDbConnectorUtil.connectToCouchDB(
            couchdbUrl, sourceDbName, username, password
        );
        CouchDbConnector targetDb = CouchDbConnectorUtil.connectToCouchDB(
            couchdbUrl, targetDbName, username, password
        );

        if (sourceDb == null || targetDb == null) {
            System.err.println("Could not connect to one or both databases. Exiting...");
            return;
        }

        // 5. Clean and save
        cleanAndSave(sourceDb, targetDb);
    }

    private static void cleanAndSave(CouchDbConnector sourceDb, CouchDbConnector targetDb) {
        System.out.println("Cleaning and saving data...");
    
        // 1. Build a query for all docs
        ViewQuery query = new ViewQuery()
                .allDocs()
                .includeDocs(true);
    
        // 2. Fetch as a raw untyped list to avoid the inference error
        List<?> rawList = sourceDb.queryView(query, Map.class);
    
        // 3. Cast each element to Map<String,Object>
        List<Map<String, Object>> docList = new ArrayList<>();
        for (Object o : rawList) {
            @SuppressWarnings("unchecked") // We know they're Map<String,Object> from CouchDB
            Map<String, Object> doc = (Map<String, Object>) o;
            docList.add(doc);
        }
    
        // 4. Collect all "data" fields into a single list
        List<Map<String, Object>> stackedData = new ArrayList<>();
        for (Map<String, Object> doc : docList) {
            if (doc != null && doc.containsKey("data")) {
                Object dataObj = doc.get("data");
                if (dataObj instanceof Map) {
                    stackedData.add((Map<String, Object>) dataObj);
                }
            }
        }
    
        // 5. Deduplicate feed items by "docurl"
        Map<String, Map<String, Object>> deduplicatedDataMap = new LinkedHashMap<>();
        for (Map<String, Object> data : stackedData) {
            if (data == null) continue;
    
            // "feed" is presumably a nested map
            Map<String, Object> feed = (Map<String, Object>) data.get("feed");
            if (feed == null) continue;
    
            // feed["list"] is presumably a List<Map<String,Object>>
            Object feedListObj = feed.get("list");
            if (!(feedListObj instanceof List)) continue;
    
            List<Map<String, Object>> feedList = (List<Map<String, Object>>) feedListObj;
            for (Map<String, Object> item : feedList) {
                String docUrl = (String) item.get("docurl");
                if (docUrl != null) {
                    deduplicatedDataMap.put(docUrl, item);
                }
            }
        }
    
        // 6. Reconstruct a final "data" structure (this is just an example format)
        Map<String, Object> finalData = new HashMap<>();
        finalData.put("zhibo", new ArrayList<>());
        finalData.put("focus", new ArrayList<>());
        finalData.put("top", Map.of(
                "list", new ArrayList<>(),
                "html", "",
                "top_ids", "",
                "survey_id", new ArrayList<>()
        ));
        finalData.put("feed", Map.of(
                "list", new ArrayList<>(deduplicatedDataMap.values()),
                "html", "",
                "page_info", Map.of(
                        "totalPage", 1,
                        "pageSize", deduplicatedDataMap.size(),
                        "prePage", 0,
                        "nextPage", 0,
                        "firstPage", 1,
                        "lastPage", 1,
                        "totalNum", deduplicatedDataMap.size(),
                        "pName", "page",
                        "page", 1
                )
        ));
    
        // 7. Generate a UUID and timestamp
        String uniqueId = UUID.randomUUID().toString();
        String cleanedDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
    
        // 8. Build the cleaned doc
        Map<String, Object> cleanedDoc = new HashMap<>();
        cleanedDoc.put("_id", uniqueId);
        cleanedDoc.put("cleaned_date", cleanedDate);
        cleanedDoc.put("data", finalData);
    
        // 9. Save into target DB
        try {
            targetDb.create(cleanedDoc);
            System.out.println("Cleaned data saved successfully with ID: " + uniqueId);
        } catch (Exception e) {
            System.err.println("Error saving cleaned data: " + e.getMessage());
        }
    }
}
