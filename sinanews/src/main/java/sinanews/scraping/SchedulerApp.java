package sinanews.scraping;

import common.scraping.ConfigLoader;
import common.utils.SchedulerService;
import org.quartz.SimpleTrigger;
import org.quartz.JobDataMap;

import java.util.Properties;

public class SchedulerApp {
    public static void main(String[] args) {
        // Load configuration
        Properties config = ConfigLoader.loadConfig("config_sina_news.properties");
        if (config == null) {
            System.err.println("Failed to load configuration. Exiting...");
            return;
        }

        try {
            // Get scheduler interval from configuration (default = 30 minutes)
            int intervalMinutes = Integer.parseInt(config.getProperty("scheduler.interval.minutes", "30"));
            System.out.println("Scheduler interval set to: " + intervalMinutes + " minutes.");

            // Convert the configured interval to seconds
            int intervalSeconds = intervalMinutes * 60;

            // Schedule the job using the common SchedulerService
            // We use the provided convenience method: scheduleSimpleJob
            // SimpleTrigger.REPEAT_INDEFINITELY means the job repeats forever
            SchedulerService.scheduleSimpleJob(
                    ScraperJob.class,
                    "scraperJob",
                    "sina_news_scraper",
                    intervalSeconds,
                    SimpleTrigger.REPEAT_INDEFINITELY,
                    null
            );

            System.out.println("Scraper scheduler started. Press Ctrl+C to exit.");
        } catch (Exception e) {
            System.err.println("Failed to start scheduler:");
            e.printStackTrace();
        }
    }
}
