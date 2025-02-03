package scraping;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ScraperJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Call the main scraping logic here
        try {
            NewsScraper.main(null);
        } catch (Exception e) {
            System.err.println("Failed to execute scheduled scraping task:");
            e.printStackTrace();
        }
    }
}
