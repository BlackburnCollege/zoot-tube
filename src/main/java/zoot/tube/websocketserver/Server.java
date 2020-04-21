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

public class Server extends WebSocketServer {

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

    public Server(int port) {
        super(new InetSocketAddress(port));
    }

    public void addMessageHandler(Consumer<String> messageHandler) {
        this.messageHandlers.add(messageHandler);
    }

    public void removeMessageHandler(Consumer<String> messageHandler) {
        this.messageHandlers.remove(messageHandler);
    }

    public void clearMessageHandlers() {
        this.messageHandlers.clear();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (this.conn != null) {
            System.out.println("New connection made. Dropping old one.");
            this.conn.close();
        }
        System.out.println(String.format("Connection opened to: %s", conn.getRemoteSocketAddress()));
        this.conn = conn;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (this.conn == conn) {
            this.conn = null;
        }
        System.out.println(String.format("Connection closed to: %s", conn.getRemoteSocketAddress()));
        conn.close();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        this.messageHandlers.forEach((messageHandler) -> messageHandler.accept(message));
        System.out.println(String.format("[DEBUG] Server: Message from: %s | %s", conn.getRemoteSocketAddress(), message));
    }

    public boolean sendMessage(String message) {
        if (this.conn == null) {
            System.out.println("There is no connection to send to.");
            return false;
        }
        this.conn.send(message);
        return true;
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("There was an error.");
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Server started!" + this.getAddress().toString());
    }

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
