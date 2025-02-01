import java.sql.Timestamp;
import java.util.List;

public class DatabaseHandlerTest {
    public static void main(String[] args) {
        // Step 1: Initialize the DatabaseHandler
        DatabaseHandler dbHandler = new DatabaseHandler();

        // Step 2: Insert a news entry into the database
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String category = "Technology";
        String content = "New advancements in AI are transforming industries.";
        dbHandler.saveNews(currentTime, category, content);

        // Step 3: Fetch all news entries from the database and print them
        List<News> allNews = dbHandler.fetchNews();
        System.out.println("All News Records:");
        for (News news : allNews) {
            System.out.println(news);
        }

        // Step 4: Fetch news within a specific time window
        Timestamp startTime = new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // 24 hours ago
        Timestamp endTime = new Timestamp(System.currentTimeMillis());
        List<News> newsInTimeWindow = dbHandler.fetchNewsByTimeWindow(startTime, endTime);
        System.out.println("\nNews in the Last 24 Hours:");
        for (News news : newsInTimeWindow) {
            System.out.println(news);
        }
    }
}
