/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sockets;

/**
 *
 * @author Paul
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * A simple WebSocketServer implementation. Taken from:
 * https://github.com/TooTallNate/Java-WebSocket/blob/master/src/main/example/ChatServer.java
 */
public class SimpleSocket extends WebSocketServer {

    private WebSocket conn;

    private ArrayList<Consumer<String>> messageReceivedHandler = new ArrayList<>();

    /**
     *
     * @param port the port to listen for a new connection
     * @throws UnknownHostException
     */
    public SimpleSocket(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        start();
    }

    public SimpleSocket(int port, Consumer<String> messageReceivedHandler) throws UnknownHostException {
        this(port);
        this.messageReceivedHandler.add(messageReceivedHandler);
    }

    /**
     * overwrite the message handler to what is given Note: this will remove all
     * previously set message handlers.
     *
     * @param messageReceivedHandler
     */
    public void setMessageReceivedHandler(Consumer<String> messageReceivedHandler) {
        this.messageReceivedHandler.clear();
        this.messageReceivedHandler.add(messageReceivedHandler);
    }

    /**
     * Forgot to add some code you also want to run when a message is received?
     * Add your additional code to run here. They will execute in order added.
     *
     * @param additionalHandler
     */
    public void addMessageHandler(Consumer<String> additionalHandler) {
        this.messageReceivedHandler.add(additionalHandler);
    }

    /**
     * attempts to remove the specified handler.
     *
     * @param additionalHandler
     * @return false if not found; true if found+removed.
     */
    public boolean removeMessageHandler(Consumer<String> additionalHandler) {
        return this.messageReceivedHandler.remove(additionalHandler);
    }

    /**
     * Sends the message on established connection. If JSON is desired, make
     * sure the message is already in JSON form.
     *
     * @param message
     * @return false if sending message fails (for example if no connection has
     * been established) true on send success.
     */
    public boolean sendMessage(String message) {
        if (conn == null) {

            System.out.println("No current connection! Cannot send message");
//            Logger.getLogger(SimpleSocket.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        conn.send(message);
        return true;
    }

    private SimpleSocket(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (this.conn != null) {
            System.out.println("New connection established while previous connection existed. Closing old connection and replacing");
            conn.close();
        }
        this.conn = conn;
        conn.send("{\"data\":\"Hello world!\"}");
//        conn.send("Welcome to the server!"); //This method sends a message to the new client
//        broadcast("new connection: " + handshake.getResourceDescriptor()); //This method sends a message to all clients connected
        System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " has connected!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
//        broadcast(conn + " has disconnected!");
        if (conn == this.conn) {
            this.conn = null;
        }
        try {
            conn.close();
        } catch (Exception e) {
        }
        System.out.println(conn + " has disconnected!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
//        broadcast(message);
        System.out.println(conn + ": " + message);
        messageReceivedHandler.forEach((var x) -> x.accept(message));

    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
//        broadcast(message.array());
        System.out.println(conn + ": " + message);
    }

    /**
     * Call me when you're done; I'll clean up.
     */
    public void shutdown() {
        if (conn != null) {
            conn.close();
        }
        try {
            stop(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(SimpleSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public static void main(String[] args) throws InterruptedException, IOException {
//        int port = 55443;
//
//        SimpleSocket s = new SimpleSocket(port, (String str) -> {
//            System.out.println("I got an effing message: " + str);
//        });
////        s.start();
//        System.out.println("ChatServer started on port: " + s.getPort());
//
//        s.addMessageHandler((String message) -> {
//            System.out.println("do stuff with message: " + message);
//        });
//        Thread.sleep(5000);
//        System.out.println("Sending message");
//        s.sendMessage("{\"key\": \"string value\", \"key2\" : true, \"key3\" : 3.159265}");
//    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

}
