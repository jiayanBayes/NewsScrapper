package sinanews.datacleaning;

import common.database.CouchDbConnectorUtil;
import common.scraping.ConfigLoader;
import org.ektorp.CouchDbConnector;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import java.time.LocalDateTime;
import java.util.Properties;

public class DataCleanerJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("DataCleaner job executed at: " + LocalDateTime.now());

        // 1. Load configuration
        Properties config = ConfigLoader.loadConfig("config_sina_news.properties");
        if (config == null) {
            System.err.println("Failed to load config file. Exiting...");
            return;
        }

        // 2. Get database configuration
        String couchdbUrl   = config.getProperty("couchdb.url");
        String sourceDbName = config.getProperty("couchdb.database_raw");
        String targetDbName = config.getProperty("couchdb.database_cleaned");
        String urlToRemove = config.getProperty("urlToRemove");

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

        // 5. Call the actual cleaning method
        DataCleaner.cleanAndSave(sourceDb, targetDb, urlToRemove);
    }
}
