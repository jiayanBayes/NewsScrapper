package datacleaning;

import database.CouchDbConnectorUtil;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import scraping.ConfigLoader;
import database.NewsDoc;

import java.util.List;
import java.util.Properties;

public class DataCleaner {

    public static void main(String[] args) {
        // 1. Load the config file
        Properties config = ConfigLoader.loadConfig("config_sina_news.properties");
        if (config == null) {
            System.err.println("Failed to load config file. Exiting...");
            return;
        }

        // 2. Get CouchDB properties from the config file
        String couchdbUrl = config.getProperty("couchdb.url");
        String sourceDbName = config.getProperty("couchdb.database_raw");     // Source database
        String targetDbName = config.getProperty("couchdb.database_cleaned"); // Target database
        String urlToRemove = config.getProperty("urlToRemove");               // URL to exclude

        if (urlToRemove == null || urlToRemove.isEmpty()) {
            System.err.println("No 'urlToRemove' found in config. Nothing to clean.");
            return;
        }

        // 3. Retrieve credentials from environment variables
        String username = System.getenv("COUCHDB_USERNAME");
        String password = System.getenv("COUCHDB_PASSWORD");

        // 4. Connect to the source and target databases
        CouchDbConnector sourceDb = CouchDbConnectorUtil.connectToCouchDB(couchdbUrl, sourceDbName, username, password);
        if (sourceDb == null) {
            System.err.println("Could not connect to source database: " + sourceDbName);
            return;
        }

        CouchDbConnector targetDb = CouchDbConnectorUtil.connectToCouchDB(couchdbUrl, targetDbName, username, password);
        if (targetDb == null) {
            System.err.println("Could not connect to or create target database: " + targetDbName);
            return;
        }

        // 5. Clean and transfer documents
        cleanAndSave(sourceDb, targetDb, urlToRemove);
    }

    private static void cleanAndSave(CouchDbConnector sourceDb, CouchDbConnector targetDb, String urlToRemove) {
        System.out.println("Cleaning data from source DB (" + sourceDb.getDatabaseName() + ") and appending to target DB (" + targetDb.getDatabaseName() + ")...");

        // Query all documents from the source database
        ViewQuery query = new ViewQuery().allDocs().includeDocs(true);
        List<NewsDoc> docs = sourceDb.queryView(query, NewsDoc.class);

        int skippedCount = 0;
        int savedCount = 0;

        for (NewsDoc doc : docs) {
            if (doc != null && urlToRemove.equals(doc.getUrl())) {
                // Skip documents with the target URL
                skippedCount++;
                continue;
            }

            // Apply additional cleaning logic if needed
            NewsDoc cleanedDoc = cleanDocument(doc);

            // Save the cleaned document to the target database
            try {
                targetDb.create(cleanedDoc); // Save as a new document
                savedCount++;
            } catch (Exception e) {
                System.err.println("Failed to save document with ID: " + doc.get_id());
                e.printStackTrace();
            }
        }

        System.out.println("Cleaning complete: " + savedCount + " documents saved, " + skippedCount + " documents skipped.");
    }

    private static NewsDoc cleanDocument(NewsDoc doc) {
        // Example of additional cleaning logic: Remove sensitive fields, trim data, etc.
        doc.setData(doc.getData().trim()); // Trim whitespace from the "data" field
        // Add more cleaning logic as needed
        return doc;
    }
}
