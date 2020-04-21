package zoot.tube.websocketserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import zoot.tube.ApiRequest;

/**
 * A web socket server that maintains only a single connection at a time.
 *
 * Only the newest connection is maintained, the previous connection
 * will get closed.
 */
public class Server extends WebSocketServer {

    /**
     * Creates a greeting handler.
     *
     * @param server   the server to send the message through.
     * @param greeting the message to send as the greeting.
     * @return the message handler.
     */
    public static Consumer<String> createDefaultGreeting(Server server, String greeting) {
        return message -> {
            Gson gson = new GsonBuilder().create();
            ApiRequest request = gson.fromJson(message, ApiRequest.class);

            // Make sure the message is asking for the greeting.
            if (request.getHeader().equals("getGreeting")) {
                // Send back the greeting.
                server.sendMessage(String.format("{\"header\":\"greeting\",\"data\":\"%s\"}", greeting));
            }
        };
    }

    private WebSocket conn = null;
    private List<Consumer<String>> messageHandlers = new ArrayList<>();

    /**
     * Creates a new web socket with the given port.
     *
     * @param port the port to open the web socket on.
     */
    public Server(int port) {
        super(new InetSocketAddress(port));
    }

    /**
     * Adds a message handler.
     * <p>
     * Note: Whenever this server receives a message, all message
     * handlers are notified. So they should each check if the message
     * is for them.
     *
     * @param messageHandler the message handler to add.
     */
    public void addMessageHandler(Consumer<String> messageHandler) {
        this.messageHandlers.add(messageHandler);
    }

    /**
     * Removes the given message handler.
     *
     * @param messageHandler the message handler to remove.
     */
    public void removeMessageHandler(Consumer<String> messageHandler) {
        this.messageHandlers.remove(messageHandler);
    }

    /**
     * Removes all message handlers.
     */
    public void clearMessageHandlers() {
        this.messageHandlers.clear();
    }

    /**
     * Runs whenever a web socket connection is made to this server.
     * <p>
     * Note: we only allow a single connection at a time. So if a new
     * connection is made, the old connection gets closed.
     *
     * @param conn      the new connection.
     * @param handshake no clue what this is.
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (this.conn != null) {
            System.out.println("New connection made. Dropping old one.");
            this.conn.close();
        }
        System.out.println(String.format("Connection opened to: %s", conn.getRemoteSocketAddress()));
        this.conn = conn;
    }

    /**
     * Runs whenever a web socket connection gets closed to this server.
     *
     * @param conn   the connection that is closing.
     * @param code   the closing code?
     * @param reason the reason?
     * @param remote no clue.
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (this.conn == conn) {
            this.conn = null;
        }
        System.out.println(String.format("Connection closed to: %s", conn.getRemoteSocketAddress()));
        conn.close();
    }

    /**
     * Runs whenever this server receives a message through the web socket.
     *
     * @param conn    the connection that sent the message.
     * @param message the message.
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        this.messageHandlers.forEach((messageHandler) -> messageHandler.accept(message));
        System.out.println(String.format("[DEBUG] Server: Message from: %s | %s", conn.getRemoteSocketAddress(), message));
    }

    /**
     * Sends a message to through the web socket.
     * <p>
     * Note: This server only allows a single connection at a time,
     * so the message will be sent only to the current connection.
     *
     * @param message the message to send.
     * @return true if the message was sent, otherwise false.
     */
    public boolean sendMessage(String message) {
        if (this.conn == null) {
            System.out.println("There is no connection to send to.");
            return false;
        }
        this.conn.send(message);
        return true;
    }

    /**
     * Runs whenever this server encounters an error.
     *
     * @param conn the connection that caused the error.
     * @param ex   the exception that was thrown.
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("There was an error.");
        ex.printStackTrace();
    }

    /**
     * Runs whenever {@link Server#start()} is called.
     */
    @Override
    public void onStart() {
        System.out.println("Server started!" + this.getAddress().toString());
    }

    /**
     * Safely shuts down this server.
     */
    public void shutdown() {
        if (this.conn != null) {
            this.conn.close();
        }

        try {
            this.stop(500);
        } catch (InterruptedException e) {
            System.out.println("Failed to stop server.");
            e.printStackTrace();
        }
    }
}
