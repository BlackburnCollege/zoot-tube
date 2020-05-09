package zoot.tube;

public class ApiRequestTimes {

    private String header;
    private HtmlTaskRequest data;

    public ApiRequestTimes(String header, HtmlTaskRequest data) {
        this.header = header;
        this.data = data;
    }

    public String getHeader() {
        return header;
    }

    public HtmlTaskRequest getData() {
        return data;
    }
}
