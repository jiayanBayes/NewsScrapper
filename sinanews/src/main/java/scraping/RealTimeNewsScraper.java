package sinanews.scraping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.Properties;

import java.io.InputStream;

public class RealTimeNewsScraper {

    private static volatile boolean keepRunning = true; // Flag to control the loop

    public static void main(String[] args) {

        // Create a Properties object
        Properties config = new Properties();

        // Load the properties file from the classpath
        try (InputStream input = RealTimeNewsScraper.class.getResourceAsStream("/config_sina_news.properties")) {
            if (input == null) {
                System.err.println("config.properties not found in resources folder.");
                return;
            }
            config.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Retrieve each property (with optional defaults)
        String driverPath = config.getProperty("driverPath");
        String targetUrl = config.getProperty("targetUrl", "https://finance.sina.com.cn/7x24/"); 
        String dynamicUrl = config.getProperty("dynamicUrl", "zhibo.sina.com.cn/api/zhibo/feed");
        int pollInterval = Integer.parseInt(config.getProperty("pollInterval", "5"));

        // Set up your Selenium driver using the read properties
        System.setProperty("webdriver.chrome.driver", driverPath);

        // Continue with the rest of your logic...
        System.out.println("Driver path: " + driverPath);
        System.out.println("Target URL: " + targetUrl);
        System.out.println("Dynamic URL: " + dynamicUrl);
        System.out.println("Poll Interval (sec): " + pollInterval);

        // Configure ChromeOptions with performance logging
        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        // Optional: run headless if you don't need the browser window
        // options.addArguments("--headless");

        WebDriver driver = new ChromeDriver(options);

        // Track processed URLs so we don't parse them multiple times
        Set<String> processedUrls = new HashSet<>();

        // 3) Separate thread to listen for 'q' to quit
        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("Press 'q' and hit Enter to terminate the program.");
                while (keepRunning) {
                    if (scanner.nextLine().trim().equalsIgnoreCase("q")) {
                        keepRunning = false;
                    }
                }
            }
        }).start();

        try {
            // 4) Navigate to the main page
            driver.get(targetUrl);
            System.out.println("Monitoring real-time updates...");

            // 5) Main loop: poll performance logs every 5 seconds
            while (keepRunning) {
                LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);
                Set<String> currentBatchUrls = new HashSet<>();

                // 5a) Extract feed URLs from logs
                for (LogEntry entry : logs) {
                    String message = entry.getMessage();
                    if (message.contains(dynamicUrl)) {
                        int urlStartIndex = message.indexOf("https://");
                        int urlEndIndex = message.indexOf("\"", urlStartIndex);
                        if (urlStartIndex != -1 && urlEndIndex != -1) {
                            String url = message.substring(urlStartIndex, urlEndIndex);
                            currentBatchUrls.add(url);
                        }
                    }
                }

                // 5b) Scrape only new feed URLs
                for (String url : currentBatchUrls) {
                    if (!processedUrls.contains(url)) {
                        scrapeNewsFromUrl(url, dynamicUrl);
                        processedUrls.add(url);
                    }
                }

                // Sleep 5 seconds, then check again
                TimeUnit.SECONDS.sleep(pollInterval);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
            System.out.println("Program terminated.");
        }
    }

    /**
     * Fetches the JSONP feed from Sina and parses "feed -> list".
     */
    private static void scrapeNewsFromUrl(String url, String dynamicUrl) {
        try {
            // Ensure it's really the feed endpoint
            if (!url.contains(dynamicUrl)) {
                System.out.println("Skipping non-feed URL: " + url);
                return;
            }

            System.out.println("Fetching data from: " + url);

            // 1) Download the raw JSONP
            Document doc = Jsoup.connect(url)
                                .ignoreContentType(true)
                                .get();
            String rawJsonp = doc.body().text();

            // 2) Extract pure JSON from JSONP wrapper
            String pureJson = extractPureJson(rawJsonp);

            // 3) Parse with Jackson
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(pureJson);

            // 4) Navigate to "feed -> list"
            JsonNode listNode = root.path("result")
                                    .path("data")
                                    .path("feed")
                                    .path("list");

            if (listNode.isArray()) {
                for (JsonNode item : listNode) {
                    // Example fields from your snippet: "rich_text", "create_time"
                    String richText = item.path("rich_text").asText(null);
                    String createTime = item.path("create_time").asText(null);

                    System.out.println("Rich text: " + (richText != null ? richText : "N/A"));
                    System.out.println("Create time: " + (createTime != null ? createTime : "N/A"));
                    System.out.println("---------------------------------");
                }
            } else {
                System.out.println("No valid 'list' array found under feed->list.");
            }

        } catch (Exception e) {
            System.err.println("Failed to fetch or parse data from: " + url);
            e.printStackTrace();
        }
    }

    /**
     * Strips the JSONP callback, e.g. try{jQuery1112({ "result":... })}catch(e){} -> { "result":... }
     */
    private static String extractPureJson(String rawJsonp) {
        int start = rawJsonp.indexOf('(');
        int end = rawJsonp.lastIndexOf(')');
        if (start != -1 && end != -1 && end > start) {
            return rawJsonp.substring(start + 1, end).trim();
        }
        return rawJsonp; // fallback if it's unexpectedly already pure JSON
    }
}
