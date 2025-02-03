package database;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CouchDBHandler implements AutoCloseable {

    private final String dbName;
    private final HttpClient httpClient;
    private final CouchDbInstance dbInstance;
    private final CouchDbConnector db;

    /**
     * Constructor for CouchDBHandler.
     *
     * @param dbUrl    the URL of the CouchDB server (e.g., "http://localhost:5984")
     * @param dbName   the name of the database to connect to
     * @param username CouchDB username (if authentication is enabled)
     * @param password CouchDB password (if authentication is enabled)
     */
    public CouchDBHandler(String dbUrl, String dbName, String username, String password) {
        this.dbName = dbName;

        // Build the HttpClient
        this.httpClient = new StdHttpClient.Builder()
                .url(dbUrl)
                .username(username)
                .password(password)
                .build();

        // Create the DB instance and connector
        this.dbInstance = new StdCouchDbInstance(httpClient);
        this.db = new StdCouchDbConnector(dbName, dbInstance);

        // Create database if it does not exist
        db.createDatabaseIfNotExists();
    }

    /**
     * Fetches all documents in the CouchDB database as a List of Maps.
     *
     * @return a list of documents (each document is a Map<String, Object>),
     *         or an empty list if an error occurs.
     */
    public List<Map<String, Object>> fetchAllDocuments() {
        try {
            // Query all documents with their full contents
            ViewQuery query = new ViewQuery()
                    .allDocs()
                    .includeDocs(true);

            ViewResult result = db.queryView(query);

            // Extract the full documents instead of the 'value' field
            return result.getRows()
                         .stream()
                         .map(row -> row.getDocAs(Map.class))
                         .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Deletes the database from CouchDB. Use with caution.
     */
    public void deleteDatabase() {
        try {
            dbInstance.deleteDatabase(this.dbName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the underlying HttpClient. (Called automatically if used in a try-with-resources block.)
     */
    @Override
    public void close() {
        if (httpClient != null) {
            httpClient.shutdown();
        }
    }
}
