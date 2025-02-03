package scraping;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerApp {
    public static void main(String[] args) {
        try {
            // Define the Job
            JobDetail job = JobBuilder.newJob(ScraperJob.class)
                    .withIdentity("scraperJob", "group1")
                    .build();

            // Define a Trigger that runs every 30 minutes
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("scraperTrigger", "group1")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMinutes(30) // Execute every 30 minutes
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
