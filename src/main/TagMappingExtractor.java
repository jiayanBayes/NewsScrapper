import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.openqa.selenium.logging.LoggingPreferences;

public class TagMappingExtractor {

    public static void main(String[] args) {
        // Path to your ChromeDriver executable
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\jiay\\JavaCourse\\NewsScrapper\\tools\\chromedriver-win64\\chromedriver.exe");

        // Configure ChromeOptions to enable performance logging
        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        WebDriver driver = new ChromeDriver(options);

        try {
            // Open the main page
            driver.get("https://finance.sina.com.cn/7x24/");

            // Keep track of all captured URLs
            Set<String> dynamicUrls = new HashSet<>();
            long startTime = System.currentTimeMillis();
            long maxWaitTime = 60 * 1000; // 1 minute
            boolean hasNewData = true;

            while (hasNewData) {
                // Extract URLs from performance logs
                LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);
                int initialSize = dynamicUrls.size();
                dynamicUrls.addAll(extractDynamicUrlsFromLogs(logs));

                // Check if new URLs were added
                hasNewData = (dynamicUrls.size() > initialSize);

                // Stop if no new data or max wait time exceeded
                if (!hasNewData || (System.currentTimeMillis() - startTime) > maxWaitTime) {
                    break;
                }

                // Wait briefly before checking again
                Thread.sleep(60000); // Poll every 2 seconds
            }

            // Print all dynamically generated URLs
            System.out.println("Dynamically Generated URLs:");
            for (String url : dynamicUrls) {
                System.out.println(url);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    /**
     * Extracts only dynamically generated URLs from network logs.
     */
    private static Set<String> extractDynamicUrlsFromLogs(LogEntries logs) {
        Set<String> dynamicUrls = new HashSet<>();

        for (LogEntry entry : logs) {
            String message = entry.getMessage();

            // Look for URLs containing specific patterns like "feed?callback"
            if (message.contains("zhibo.sina.com.cn/api/zhibo/feed")) {
                int urlStartIndex = message.indexOf("https://");
                int urlEndIndex = message.indexOf("\"", urlStartIndex);
                if (urlStartIndex != -1 && urlEndIndex != -1) {
                    String url = message.substring(urlStartIndex, urlEndIndex);
                    dynamicUrls.add(url);
                }
            }
        }

        return dynamicUrls;
    }
}
