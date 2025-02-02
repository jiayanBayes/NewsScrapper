package database;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents a news item with a timestamp, category, and content.
 */
public class News {
    private Timestamp newsTime;
    private String category;
    private String content;

    public News(Timestamp newsTime, String category, String content) {
        if (newsTime == null) throw new IllegalArgumentException("newsTime cannot be null");
        if (category == null || category.isEmpty()) throw new IllegalArgumentException("category cannot be null or empty");
        if (content == null || content.isEmpty()) throw new IllegalArgumentException("content cannot be null or empty");

        this.newsTime = newsTime;
        this.category = category;
        this.content = content;
    }

    public Timestamp getNewsTime() {
        return newsTime;
    }

    public void setNewsTime(Timestamp newsTime) {
        this.newsTime = newsTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        News news = (News) o;
        return Objects.equals(newsTime, news.newsTime) &&
               Objects.equals(category, news.category) &&
               Objects.equals(content, news.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newsTime, category, content);
    }
}
