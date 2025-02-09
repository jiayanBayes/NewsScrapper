package sinanews.scraping;

import common.scraping.ConfigLoader;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SchedulerApp {
    public static void main(String[] args) {
        Properties config = ConfigLoader.loadConfig("config_sina_news.properties");
        if (config == null) {
            System.err.println("Failed to load configuration. Exiting...");
            return;
        }

        try {
            // Get scheduler interval from configuration
            int intervalMinutes = Integer.parseInt(config.getProperty("scheduler.interval.minutes", "30"));
            System.out.println("Scheduler interval set to: " + intervalMinutes + " minutes.");

            // Define the Job
            JobDetail job = JobBuilder.newJob(ScraperJob.class)
                    .withIdentity("scraperJob", "group1")
                    .build();

            // Define a Trigger with the interval from the configuration file
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("scraperTrigger", "group1")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMinutes(intervalMinutes)
                            .repeatForever())
                    .build();

            // Start the Scheduler
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);

            System.out.println("Scraper scheduler started. Press Ctrl+C to exit.");
        } catch (SchedulerException e) {
            System.err.println("Failed to start scheduler:");
            e.printStackTrace();
        }
    }
}
