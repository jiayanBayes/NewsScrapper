package database;

import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;

import java.util.*;
import java.util.stream.Collectors;

public class CouchDBHandler {

    private final CouchDbConnector db;

    public CouchDBHandler(CouchDbConnector db) {
        this.db = db;
    }

    /**
     * Fetches all news from CouchDB and removes duplicates.
     */
    public List<Map<String, Object>> fetchAndCleanNews() {
        try {
            // Fetch all documents from CouchDB
            ViewQuery query = new ViewQuery().allDocs().includeDocs(true);
            ViewResult result = db.queryView(query);

            // Extract documents into a list
            List<Map<String, Object>> allNews = result.getRows().stream()
                    .map(row -> row.getDocumentAs(Map.class))
                    .collect(Collectors.toList());

            System.out.println("Fetched " + allNews.size() + " news articles from CouchDB.");

            // Remove duplicates based on unique URLs
            Set<String> uniqueUrls = new HashSet<>();
            List<Map<String, Object>> cleanedNews = new ArrayList<>();

            for (Map<String, Object> news : allNews) {
                String url = (String) news.get("url"); // Assumes "url" is the unique identifier
                if (url != null && uniqueUrls.add(url)) {
                    cleanedNews.add(news);
                }
            }

            System.out.println("Cleaned news size after removing duplicates: " + cleanedNews.size());
            return cleanedNews;

        } catch (Exception e) {
            System.err.println("Error fetching and cleaning news: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Saves cleaned news back to CouchDB.
     */
    public void saveCleanedNews(List<Map<String, Object>> cleanedNews) {
        try {
            System.out.println("Saving cleaned news to CouchDB...");

            // Clear the database before saving new data
            db.deleteDatabase();
            db.createDatabaseIfNotExists();

            for (Map<String, Object> news : cleanedNews) {
                db.create(news);
            }

            System.out.println("Cleaned news saved successfully to CouchDB.");
        } catch (Exception e) {
            System.err.println("Error saving cleaned news to CouchDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
