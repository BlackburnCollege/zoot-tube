package zoot.tube;

public class HtmlTaskRequest {

    private long start;
    private long expire;
    private String ids;

    public HtmlTaskRequest(long start, long expire, String ids) {
        this.start = start;
        this.expire = expire;
        this.ids = ids;
    }

    public long getStart() {
        return start;
    }

    public long getExpire() {
        return expire;
    }

    public String getIds() {
        return ids;
    }
}
