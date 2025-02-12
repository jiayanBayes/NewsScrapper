package sinanews.datacleaning;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataCleaner {

    private static final Logger logger = LoggerFactory.getLogger(DataCleaner.class);
    
    // Configure ObjectMapper to ignore unknown properties
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Cleans and saves data from the source DB to the target DB.
     * Additionally, deletes documents from the source DB whose top-level "url"
     * field matches the specified urlToDelete.
     *
     * @param sourceDb    the source CouchDbConnector
     * @param targetDb    the target CouchDbConnector
     * @param urlToDelete the URL string; documents with a matching "url" will be deleted
     */
    public static void cleanAndSave(CouchDbConnector sourceDb, CouchDbConnector targetDb, String urlToDelete) {
        logger.info("Cleaning and saving data...");

        // 1. Build a query for all documents and include full documents.
        ViewQuery query = new ViewQuery().allDocs().includeDocs(true);

        // 2. Fetch raw documents from the source DB.
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> docList = sourceDb.queryView(query, (Class<Map<String, Object>>)(Class<?>) Map.class);

        // 2.1. Delete documents with a top-level "url" matching urlToDelete and remove them from docList.
        if (urlToDelete != null && !urlToDelete.isEmpty()) {
            Iterator<Map<String, Object>> iter = docList.iterator();
            while (iter.hasNext()) {
                Map<String, Object> doc = iter.next();
                Object urlObj = doc.get("url");
                if (urlObj != null && urlObj.equals(urlToDelete)) {
                    try {
                        sourceDb.delete(doc);
                        logger.info("Deleted document with url: {}", urlToDelete);
                        iter.remove();
                    } catch (Exception e) {
                        logger.error("Error deleting document with url {}: {}", urlToDelete, e.getMessage(), e);
                    }
                }
            }
        }

        // 3. Convert raw "data" fields to domain objects using Jackson.
        List<DocumentData> documentDataList = new ArrayList<>();
        for (Map<String, Object> doc : docList) {
            if (doc != null && doc.containsKey("data")) {
                Object dataObj = doc.get("data");
                if (dataObj instanceof Map) {
                    try {
                        DocumentData data = objectMapper.convertValue(dataObj, DocumentData.class);
                        documentDataList.add(data);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Failed to convert document 'data' field to DocumentData: {}", e.getMessage());
                    }
                } else {
                    logger.warn("Expected 'data' field to be a Map but found: {}",
                                (dataObj != null ? dataObj.getClass().getName() : "null"));
                }
            }
        }

        // 4. Deduplicate feed items by "docurl" using the domain objects.
        Map<String, FeedItem> deduplicatedDataMap = new LinkedHashMap<>();
        for (DocumentData documentData : documentDataList) {
            if (documentData == null) continue;
            
            Feed feed = documentData.getFeed();
            if (feed == null) {
                logger.warn("DocumentData is missing a feed");
                continue;
            }
            List<FeedItem> feedList = feed.getList();
            if (feedList == null) {
                logger.warn("Feed is missing a list");
                continue;
            }
            for (FeedItem item : feedList) {
                if (item != null && item.getDocurl() != null) {
                    deduplicatedDataMap.put(item.getDocurl(), item);
                }
            }
        }

        // 5. Reconstruct the final "data" structure using mutable maps.
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("totalPage", 1);
        pageInfo.put("pageSize", deduplicatedDataMap.size());
        pageInfo.put("totalNum", deduplicatedDataMap.size());

        Map<String, Object> feedMap = new HashMap<>();
        feedMap.put("list", new ArrayList<>(deduplicatedDataMap.values()));
        feedMap.put("html", "");
        feedMap.put("page_info", pageInfo);

        Map<String, Object> finalData = new HashMap<>();
        finalData.put("feed", feedMap);

        // 6. Generate a UUID and a timestamp.
        String uniqueId = UUID.randomUUID().toString();
        String cleanedDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);

        // 7. Build the cleaned document.
        Map<String, Object> cleanedDoc = new HashMap<>();
        cleanedDoc.put("_id", uniqueId);
        cleanedDoc.put("cleaned_date", cleanedDate);
        cleanedDoc.put("data", finalData);

        // 8. Save the cleaned document into the target DB.
        try {
            targetDb.create(cleanedDoc);
            logger.info("Cleaned data saved successfully with ID: {}", uniqueId);
        } catch (Exception e) {
            logger.error("Error saving cleaned data: {}", e.getMessage(), e);
        }
    }

}
