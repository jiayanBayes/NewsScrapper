package scraping;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import org.openqa.selenium.logging.LoggingPreferences;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class NewsScraper {

    public static void main(String[] args) {
        Properties config = loadConfig("config_sina_news.properties");
        if (config == null) {
            System.err.println("Failed to load configuration. Exiting...");
            return;
        }

        // Load configuration values
        String driverPath = config.getProperty("driverPath");
        String targetUrl = config.getProperty("targetUrl");
        int pollInterval = Integer.parseInt(config.getProperty("pollInterval")) * 1000; // Convert to milliseconds
        String dynamicUrlPattern = config.getProperty("dynamicUrl");

        // Step 1: Set up WebDriver
        System.setProperty("webdriver.chrome.driver", driverPath);

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        WebDriver driver = new ChromeDriver(options);

        Set<String> dynamicUrls = new HashSet<>();

        try {
            // Open the main page
            driver.get(targetUrl);

            // Wait for the page to fully load
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

            // Step 2: Scrape news from URLs while WebDriver is still active
            for (String url : dynamicUrls) {
                scrapeNewsFromUrl(url);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Quit WebDriver after scraping is complete
            driver.quit();
        }
    }

    /**
     * Scrapes news data from a given URL.
     */
    private static void scrapeNewsFromUrl(String url) {
        try {
            System.out.println("Fetching data from: " + url);

            // Fetch the content of the URL
            Document doc = Jsoup.connect(url)
                    .ignoreContentType(true) // Handle JSON responses
                    .get();

            // Parse the JSON response (e.g., using Jsoup or a JSON library like Jackson/Gson)
            String json = doc.body().text();
            System.out.println("Response JSON: " + json);

            // Use a JSON library to parse and extract news items
            // Example: Extract specific fields like title, timestamp, content, etc.

        } catch (Exception e) {
            System.err.println("Failed to fetch data from: " + url);
            e.printStackTrace();
        }
    }

    /**
     * Loads configuration from a file in the resources directory.
     */
    private static Properties loadConfig(String configFilePath) {
        Properties properties = new Properties();
        try (InputStream input = NewsScraper.class.getClassLoader().getResourceAsStream(configFilePath)) {
            if (input == null) {
                System.err.println("Configuration file not found: " + configFilePath);
                return null;
            }
            properties.load(input);
            return properties;
        } catch (Exception e) {
            System.err.println("Error loading configuration file: " + configFilePath);
            e.printStackTrace();
            return null;
        }
    }
}
