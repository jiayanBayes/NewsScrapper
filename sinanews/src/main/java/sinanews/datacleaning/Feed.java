package sinanews.datacleaning;

import java.util.List;

public class Feed {
    private List<FeedItem> list;
    private String html;
    private PageInfo page_info;

    public List<FeedItem> getList() {
        return list;
    }

    public void setList(List<FeedItem> list) {
        this.list = list;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public PageInfo getPage_info() {
        return page_info;
    }

    public void setPage_info(PageInfo page_info) {
        this.page_info = page_info;
    }
}
