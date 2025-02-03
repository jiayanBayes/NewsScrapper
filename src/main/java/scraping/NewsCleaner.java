package scraping;

import database.CouchDBConnector;
import database.CouchDBHandler;
import org.ektorp.CouchDbConnector;

import java.util.List;
import java.util.Map;

public class NewsCleaner {

    public static void main(String[] args) {
        // CouchDB Configuration
        String couchdbUrl = "http://127.0.0.1:5984";
        String databaseName = "sina_news_database";

        // Fetch CouchDB credentials from environment variables
        String couchdbUsername = System.getenv("COUCHDB_USERNAME");
        String couchdbPassword = System.getenv("COUCHDB_PASSWORD");
        if (couchdbUsername == null || couchdbPassword == null) {
            System.err.println("CouchDB credentials are missing. Set COUCHDB_USERNAME and COUCHDB_PASSWORD.");
            return;
        }

        // Connect to CouchDB
        CouchDbConnector db = CouchDBConnector.connect(couchdbUrl, databaseName, couchdbUsername, couchdbPassword);
        if (db == null) {
            System.err.println("Failed to connect to CouchDB.");
            return;
        }

        // Initialize the handler
        CouchDBHandler handler = new CouchDBHandler(db);

        // Fetch and clean news
        List<Map<String, Object>> cleanedNews = handler.fetchAndCleanNews();

        // Save cleaned news back to CouchDB
        handler.saveCleanedNews(cleanedNews);
    }
}
