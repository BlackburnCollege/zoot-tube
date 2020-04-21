package zoot.tube;

/**
 * The JSON format to Java equivalent for the messages sent between the client and server.
 */
public class ApiRequest {

    /**
     * The label of the message
     */
    private String header;

    /**
     * The data included in the message.
     */
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
