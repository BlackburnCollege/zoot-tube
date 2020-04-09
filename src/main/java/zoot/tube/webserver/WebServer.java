package zoot.tube.webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;

public class WebServer {

    public static final int PORT = 8080;
    private static final String PAGES_PATH = "src/main/resources/pages/";
    private static final String SCRIPTS_PATH = "src/main/resources/scripts/";

    private final HttpServer server;

    public WebServer() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        this.initDefaultPaths();

        System.out.println("Starting local server on port: " + PORT);
        server.start();
    }

    private void initDefaultPaths() {
        this.addExchange("/", exchange -> {
            File index = new File(String.format("%s%s", PAGES_PATH, "index.html"));
            this.sendFile(exchange, index, ContentType.HTML);
        });

        this.addExchange("/scripts/", exchange -> {
            String scriptRequested = exchange.getRequestURI().toASCIIString();
            scriptRequested = scriptRequested.substring("/scripts/".length());

            File script = new File(String.format("%s%s", SCRIPTS_PATH, scriptRequested));
            this.sendFile(exchange, script, ContentType.JAVASCRIPT);
        });
    }

    private void sendFile(HttpExchange exchange, File file, String type) throws IOException {
        FileInputStream fis = new FileInputStream(file);

        byte[] response = fis.readAllBytes();
        exchange.getResponseHeaders().add("Content-Type", type);
        exchange.sendResponseHeaders(200, response.length);

        OutputStream out = exchange.getResponseBody();
        out.write(response);
        out.flush();
        out.close();
    }

    public void addExchange(String path, HttpHandler handler) {
        this.server.createContext(path, handler);
    }
}
