import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL); // Enable performance logging
        options.setCapability("goog:loggingPrefs", logPrefs);

        WebDriver driver = new ChromeDriver(options);

        try {
            // Open the main page
            driver.get("https://finance.sina.com.cn/7x24/");

            // Wait for the page to fully load (dynamic content loaded)
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(
                (ExpectedCondition<Boolean>) wd -> {
                    // Example: Wait for the title to contain a specific word (modify as needed)
                    return wd.getTitle().contains("实时财经新闻直播");
                }
            );

            // Recursive approach to handle multiple network calls
            Set<String> allDynamicUrls = new HashSet<>();
            boolean hasMoreData = true;

            while (hasMoreData) {
                // Extract dynamic URLs by analyzing network logs
                LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);
                List<String> batchUrls = extractDynamicUrlsFromLogs(logs);

                // Add new URLs to the set
                int beforeSize = allDynamicUrls.size();
                allDynamicUrls.addAll(batchUrls);

                // Check if new URLs were added
                if (allDynamicUrls.size() == beforeSize) {
                    hasMoreData = false; // No new data, exit loop
                } else {
                    System.out.println("Waiting for more data to load...");
                    Thread.sleep(5000); // Wait a bit before checking logs again
                }
            }

            // Print all extracted URLs
            System.out.println("Extracted URLs:");
            for (String url : allDynamicUrls) {
                System.out.println(url);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    /**
     * Extracts URLs from network logs containing "feed?callback".
     */
    private static List<String> extractDynamicUrlsFromLogs(LogEntries logs) {
        List<String> dynamicUrls = new ArrayList<>();

        for (LogEntry entry : logs) {
            String message = entry.getMessage();

            // Look for URLs containing "feed?callback"
            if (message.contains("feed?callback")) {
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
