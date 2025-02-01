import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private static final String DB_DIRECTORY = "database";
    private static final String DB_FILE = "news.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIRECTORY + File.separator + DB_FILE;

    public DatabaseHandler() {
        ensureDatabaseDirectory();
        initDatabase();
    }

    // Ensure the database directory exists
    private void ensureDatabaseDirectory() {
        File databaseDir = new File(DB_DIRECTORY);
        if (!databaseDir.exists()) {
            if (databaseDir.mkdirs()) {
                System.out.println("Database directory created: " + DB_DIRECTORY);
            } else {
                System.err.println("Failed to create database directory: " + DB_DIRECTORY);
            }
        }
    }

    // Initialize the SQLite database and ensure the table structure exists
    private void initDatabase() {
        // Use try-with-resources to ensure the connection is automatically closed
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            // Define the SQL statement to create the 'news' table if it doesn't exist
            String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS news (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,   -- Unique identifier for each news entry
                        news_time TIMESTAMP NOT NULL,           -- Timestamp of the news (required field)
                        category TEXT,                          -- Category of the news (optional)
                        content TEXT                            -- Main content of the news (optional)
                    );
                    """;

            // Use another try-with-resources block to create and execute the SQL statement
            try (Statement stmt = conn.createStatement()) {
                // Execute the SQL command to create the table
                stmt.execute(createTableSQL);

                // Print a success message if the table is created or already exists
                System.out.println("Database initialized successfully.");
            }

        } catch (SQLException e) {
            // Print the stack trace if any SQL exception occurs (e.g., connection failure, syntax error)
            e.printStackTrace();
        }
    }

    // Method to save a news entry into the database
    public void saveNews(Timestamp news_time, String category, String content) {
        // SQL statement for inserting a new row into the 'news' table
        // The 'INSERT OR IGNORE' clause ensures that duplicate entries (based on a unique constraint) are ignored
        String insertSQL = "INSERT OR IGNORE INTO news (news_time, category, content) VALUES (?, ?, ?)";

        // Use try-with-resources to ensure the database connection and prepared statement are closed automatically
        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            // Set the value for the 'news_time' column (1st placeholder '?')
            pstmt.setTimestamp(1, news_time);

            // Set the value for the 'category' column (2nd placeholder '?')
            pstmt.setString(2, category);

            // Set the value for the 'content' column (3rd placeholder '?')
            pstmt.setString(3, content);

            // Execute the SQL INSERT query
            pstmt.executeUpdate();

            // Print a success message if the operation is successful
            System.out.println("News saved successfully.");
        } catch (SQLException e) {
            // Print the stack trace to help debug SQL-related exceptions
            e.printStackTrace();
        }
    }

    // Method to fetch all news records from the database
    public List<News> fetchNews() {
        // Create a list to store the retrieved news entries
        List<News> newsList = new ArrayList<>();

        // SQL query to select all records from the 'news' table
        String querySQL = "SELECT news_time, category, content FROM news";

        // Use try-with-resources to ensure the database connection, statement, and result set are closed automatically
        try (Connection conn = DriverManager.getConnection(DB_URL); // Establish connection to the database
            Statement stmt = conn.createStatement(); // Create a statement for executing the query
            ResultSet rs = stmt.executeQuery(querySQL)) { // Execute the SQL query and get the result set

            // Iterate through the result set to process each row
            while (rs.next()) {
                // Retrieve the 'news_time' column as a Timestamp
                Timestamp news_time = rs.getTimestamp("news_time");

                // Retrieve the 'category' column as a String
                String category = rs.getString("category");

                // Retrieve the 'content' column as a String
                String content = rs.getString("content");

                // Create a new News object and add it to the list
                newsList.add(new News(news_time, category, content));
            }
        } catch (SQLException e) {
            // Print the stack trace if any SQL-related error occurs
            e.printStackTrace();
        }

        // Return the list of news entries
        return newsList;
    }

    // Fetch news from the database within a specified time window
    public List<News> fetchNewsByTimeWindow(Timestamp startTime, Timestamp endTime) {
        List<News> newsList = new ArrayList<>();
        String querySQL = "SELECT news_time, category, content FROM news WHERE news_time BETWEEN ? AND ?";

        // Use try-with-resources to ensure resources are closed
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(querySQL)) {

            // Set the parameters for the prepared statement
            pstmt.setTimestamp(1, startTime); // Start of the time window
            pstmt.setTimestamp(2, endTime);  // End of the time window

            // Execute the query and process the result set
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp news_time = rs.getTimestamp("news_time");
                    String category = rs.getString("category");
                    String content = rs.getString("content");
                    newsList.add(new News(news_time, category, content));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newsList;
    }
}
