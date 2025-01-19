import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NewsScraper {

    // Method to scrape news from all categories
    public List<News> scrapeAllCategories(String baseUrl) {
        List<News> allNews = new ArrayList<>();

        try {
            // Fetch the HTML content of the base URL
            Document doc = Jsoup.connect(baseUrl).get();

            // Select all category links (e.g., tabs for A股, 宏观, 公司, etc.)
            Elements categoryLinks = doc.select("div.nav a"); // Adjust selector to match category navigation links

            // Iterate through each category link
            for (Element categoryLink : categoryLinks) {
                // Get the category URL and category name
                String categoryUrl = categoryLink.absUrl("href"); // Get absolute URL for the category
                String categoryName = categoryLink.text(); // Get the category name (e.g., "A股", "宏观")

                System.out.println("Scraping category: " + categoryName);

                // Scrape news for the current category
                List<News> categoryNews = scrapeNews(categoryUrl, categoryName);
                allNews.addAll(categoryNews); // Add the category's news to the overall list
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allNews;
    }

    // Method to scrape news for a specific category
    public List<News> scrapeNews(String categoryUrl, String categoryName) {
        List<News> newsList = new ArrayList<>();

        try {
            // Fetch the HTML content of the category page
            Document doc = Jsoup.connect(categoryUrl).get();

            // Select news items based on their HTML structure
            Elements newsItems = doc.select(".item-container"); // Adjust the selector to match news items

            for (Element item : newsItems) {
                // Extract time
                String timeText = item.select(".time-class").text(); // Adjust class or tag selector
                Timestamp newsTime = Timestamp.valueOf("2025-01-12 " + timeText);

                // Extract content
                String content = item.select(".content-class").text(); // Adjust class or tag selector

                // Add the news item to the list
                newsList.add(new News(newsTime, categoryName, content));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newsList;
    }

    public static void main(String[] args) {
        // Define the base URL of the Sina Finance news page
        String baseUrl = "https://finance.sina.com.cn/7x24/";

        // Create an instance of the scraper
        NewsScraper scraper = new NewsScraper();

        // Scrape news from all categories starting from the base URL
        List<News> allNews = scraper.scrapeAllCategories(baseUrl);

        // Print the scraped news
        for (News news : allNews) {
            System.out.println(news);
        }
    }
}

// News class to represent a news item
class News {
    private Timestamp newsTime;
    private String category;
    private String content;

    public News(Timestamp newsTime, String category, String content) {
        this.newsTime = newsTime;
        this.category = category;
        this.content = content;
    }

    @Override
    public String toString() {
        return "News{" +
                "newsTime=" + newsTime +
                ", category='" + category + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
