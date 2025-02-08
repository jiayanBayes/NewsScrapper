package datacleaning;

import database.CouchDbConnectorUtil;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.support.AllDocsResponse;
import scraping.ConfigLoader; // or wherever your ConfigLoader is
import java.util.Properties;

public class DataCleaner {

    public static void main(String[] args) {
        // 1. Load the config file (adapt the path/name if needed)
        Properties config = ConfigLoader.loadConfig("config_sina_news.properties");
        if (config == null) {
            System.err.println("Failed to load config file. Exiting...");
            return;
        }

        // 2. Get the relevant properties
        String couchdbUrl    = config.getProperty("couchdb.url");      // e.g. http://127.0.0.1:5984
        String databaseName  = config.getProperty("couchdb.database"); // e.g. sina_news_database
        String targetUrl     = config.getProperty("targetUrl");        // e.g. https://finance.sina.com.cn/7x24/

        if (targetUrl == null || targetUrl.isEmpty()) {
            System.err.println("No 'targetUrl' found in config. Nothing to delete.");
            return;
        }

        // 3. Retrieve credentials from environment variables
        String username = System.getenv("COUCHDB_USERNAME"); // e.g. "admin"
        String password = System.getenv("COUCHDB_PASSWORD"); // e.g. "Aa123456yanjia!!"

        // 4. Connect to CouchDB using your CouchDbConnectorUtil
        CouchDbConnector db = CouchDbConnectorUtil.connectToCouchDB(
            couchdbUrl, databaseName, username, password
        );

        if (db == null) {
            System.err.println("Could not connect to CouchDB. Exiting...");
            return;
        }

        // 5. Delete documents where url == targetUrl
        deleteDocsWithUrl(db, targetUrl);
    }

    private static void deleteDocsWithUrl(CouchDbConnector db, String urlToRemove) {
        System.out.println("Deleting documents with url = " + urlToRemove);

        // Ektorp "all_docs" query with includeDocs so we get each doc's fields
        ViewQuery query = new ViewQuery().allDocs().includeDocs(true);

        // If you have a NewsDoc POJO with _id, _rev, url, etc.:
        AllDocsResponse<NewsDoc> allDocsResponse = db.queryForAllDocs(query, NewsDoc.class);

        int deleteCount = 0;
        for (AllDocsResponse.Row<NewsDoc> row : allDocsResponse.getRows()) {
            NewsDoc doc = row.getDoc();
            if (doc != null && urlToRemove.equals(doc.getUrl())) {
                db.delete(doc); // requires doc._id and doc._rev
                deleteCount++;
            }
        }
        System.out.println("Deleted " + deleteCount + " documents.");
    }
}
