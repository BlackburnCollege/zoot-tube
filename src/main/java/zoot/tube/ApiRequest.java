package zoot.tube;

public class ApiRequest {

    private String header;
    private String data;

    public ApiRequest(String header, String data) {
        this.header = header;
        this.data = data;
    }

    public String getHeader() {
        return this.header;
    }

    public String getData() {
        return this.data;
    }
}
