package sinanews.scraping;

import java.time.LocalDateTime;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import java.time.LocalDateTime;

public class ScraperJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
         System.out.println("Scraper job executed at: " + LocalDateTime.now());
        // Call the main scraping logic here
        try {
            NewsScraper.main(null);
        } catch (Exception e) {
            System.err.println("Failed to execute scheduled scraping task:");
            e.printStackTrace();
        }
    }
}
