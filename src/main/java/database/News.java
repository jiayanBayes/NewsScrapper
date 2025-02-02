import java.sql.Timestamp;

public class News {
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
