package zoot.tube.websocketwebserver;

import java.net.InetSocketAddress;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class Server extends WebSocketServer {

    public Server(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println(String.format("Connection opened to: %s", conn.getRemoteSocketAddress()));

        conn.send("{\"data\":\"<h1>Hello world!</h1>\"}");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println(String.format("Connection closed to: %s", conn.getRemoteSocketAddress()));

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println(String.format("Message from: %s | %s", conn.getRemoteSocketAddress(), message));
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
        System.out.println(String.format("Sever started at: http://localhost:%d",  this.getPort()));
    }
}
