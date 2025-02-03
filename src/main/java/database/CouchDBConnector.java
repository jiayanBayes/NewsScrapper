package database;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

public class CouchDBConnector {

    /**
     * Connects to CouchDB and returns a CouchDbConnector instance.
     */
    public static CouchDbConnector connect(String url, String databaseName, String username, String password) {
        try {
            HttpClient httpClient = new StdHttpClient.Builder()
                    .url(url)
                    .username(username)
                    .password(password)
                    .build();

            CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
            CouchDbConnector db = new StdCouchDbConnector(databaseName, dbInstance);
            System.out.println("Connected to CouchDB: " + databaseName);
            return db;
        } catch (Exception e) {
            System.err.println("Error connecting to CouchDB: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
