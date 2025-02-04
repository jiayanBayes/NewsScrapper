package scraping;
import database.CouchDbConnectorUtil;
import utils.HumanSimulator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import org.openqa.selenium.logging.LoggingPreferences;

public class NewsScraper {

    public static void main(String[] args) {
        Properties config = ConfigLoader.loadConfig("config_sina_news.properties");
        if (config == null) {
            System.err.println("Failed to load configuration. Exiting...");
            return;
        }

        // Load configuration values
        String driverPath = config.getProperty("driverPath");
        String targetUrl = config.getProperty("targetUrl");
        int pollInterval = Integer.parseInt(config.getProperty("pollInterval")) * 1000; // Convert to milliseconds
        String dynamicUrlPattern = config.getProperty("dynamicUrl");
        String couchdbUrl = config.getProperty("couchdb.url");
        String databaseName = config.getProperty("couchdb.database");

        // Fetch CouchDB credentials from environment variables
        String couchdbUsername = System.getenv("COUCHDB_USERNAME");
        String couchdbPassword = System.getenv("COUCHDB_PASSWORD");
        if (couchdbUsername == null || couchdbUsername.isEmpty() || couchdbPassword == null || couchdbPassword.isEmpty()) {
            System.err.println("CouchDB credentials are missing. Set COUCHDB_USERNAME and COUCHDB_PASSWORD. Exiting...");
            return;
        }

        // Connect to CouchDB
        CouchDbConnector db = CouchDbConnectorUtil.connectToCouchDB(couchdbUrl, databaseName, couchdbUsername, couchdbPassword);
        if (db == null) {
            System.err.println("Failed to connect to CouchDB. Exiting...");
            return;
        }

        System.setProperty("webdriver.chrome.driver", driverPath);

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        WebDriver driver = new ChromeDriver(options);
        HumanSimulator simulator = new HumanSimulator(driver); // Instantiate the HumanSimulator
        Set<String> dynamicUrls = new HashSet<>();

        try {
            driver.get(targetUrl);
            // Simulate human behavior while waiting for dynamic content to load
            simulator.simulateScroll();       // Simulate scrolling behavior
            simulator.simulateMouseMovement(); // Simulate mouse movement
            simulator.simulateKeyPress();     // Simulate key presses (e.g., PAGE_DOWN)
            simulator.simulateDelay();        // Add a random delay for more human-like interaction
            
            Thread.sleep(pollInterval);

            // Extract URLs from network logs
            LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);
            for (LogEntry entry : logs) {
                String message = entry.getMessage();
                if (message.contains(dynamicUrlPattern)) {
                    int urlStartIndex = message.indexOf("https://");
                    int urlEndIndex = message.indexOf("\"", urlStartIndex);
                    if (urlStartIndex != -1 && urlEndIndex != -1) {
                        String url = message.substring(urlStartIndex, urlEndIndex);
                        dynamicUrls.add(url);
                    }
                }
            }

            // Scrape news and save to CouchDB
            for (String url : dynamicUrls) {
                scrapeAndSaveNews(url, db);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    /**
     * Scrapes news data from the given URL and saves it to CouchDB.
     */
    private static void scrapeAndSaveNews(String url, CouchDbConnector db) {
        try {
            System.out.println("Fetching data from: " + url);

            // Fetch the content of the URL with Jsoup
            Document doc = Jsoup.connect(url)
                    .ignoreContentType(true) // Handle JSON responses
                    .get();

            // Parse the JSON response as a UTF-8 encoded string
            String json = new String(doc.body().text().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

            // Create a map to store the scraped news
            Map<String, Object> newsDoc = new HashMap<>();
            newsDoc.put("url", url);
            newsDoc.put("data", json);
            // Store a human-readable timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            newsDoc.put("timestamp", sdf.format(new Date(System.currentTimeMillis())));
            
            // Save the news to CouchDB
            db.create(newsDoc);
            System.out.println("Saved news to CouchDB: " + url);

        } catch (Exception e) {
            System.err.println("Failed to fetch and save data from: " + url);
            e.printStackTrace();
        }
    }
}