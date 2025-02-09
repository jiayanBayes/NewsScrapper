package sinanews.database;

public class NewsDoc {
    private String _id;
    private String _rev;
    private String url;
    private String data;
    private long timestamp;
    
    // Getters and setters for each field...
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String get_rev() { return _rev; }
    public void set_rev(String _rev) { this._rev = _rev; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
