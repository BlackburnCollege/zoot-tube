package zoot.tube.webserver;

import com.google.api.client.auth.oauth2.Credential;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import zoot.tube.googleapi.RefreshTokenSaver;
import zoot.tube.googleapi.OldSimpleYouTubeAPI;
import zoot.tube.googleapi.YouTubeAPIAuthorizer;

public class WebServer {

    private static final int PORT = 8080;
    private static final String PAGES_PATH = "src/main/resources/pages/";
    private static final String IMAGES_PATH = "src/main/resources/images/";
    private static final String SCRIPTS_PATH = "src/main/resources/scripts/";

    private HashMap<String, YouTubeAPIWebAdapter> webAdapters = new HashMap<>();

    private final HttpServer server;

    public WebServer() throws IOException {
//        this.youtube = new YouTubeAPIWebAdapter(youtube);
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        this.initPaths();

        System.out.println("Starting local server on port: " + PORT);
        System.out.println("http://localhost:8080/");
        server.start();
    }

    private void initPaths() {
        // Home page and invalid requests.
        this.addExchange("/", exchange -> {
            if (exchange.getRequestMethod().equals("GET")) {
                // Need this to properly show/hide login button.
                this.handleLoginCheck(exchange);
                // Home page.
                if (exchange.getRequestURI().toASCIIString().equals("/")) {
                    File index = new File(String.format("%s%s", PAGES_PATH, "index.html"));
                    this.sendFile(exchange, index, ContentType.HTML);
                }
                // 404 Not Found.
                else {
                    File notFound = new File(String.format("%s%s", PAGES_PATH, "404notfound.html"));
                    this.sendFile(exchange, notFound, ContentType.HTML);
                }
            } else {
                System.out.println("Unrecognized Non-GET request: " + exchange.getRequestURI().toASCIIString());
            }
        });

        this.addExchange("/favicon.ico", exchange -> {
            if (exchange.getRequestMethod().equals("GET")) {
                File icon = new File(String.format("%s%s", IMAGES_PATH, "icon.png"));
                this.sendFile(exchange, icon, ContentType.PNG);
            }
        });

        this.addExchange("/scripts/", exchange -> {
            if (exchange.getRequestMethod().equals("GET")) {
                String scriptRequested = exchange.getRequestURI().toASCIIString();
                scriptRequested = scriptRequested.substring("/scripts/".length());

                File script = new File(String.format("%s%s", SCRIPTS_PATH, scriptRequested));
                this.sendFile(exchange, script, ContentType.JAVASCRIPT);
            } else {
                System.out.println("Duck off!");
                System.out.println(exchange.getRequestURI().toASCIIString());
                exchange.getResponseBody().close();
            }
        });

        this.addAPIHandlers();
    }

    private void addAPIHandlers() {
        this.addExchange("/api/getmyplaylists", exchange -> {
            if (exchange.getRequestMethod().equals("POST")) {
                YouTubeAPIWebAdapter youtube = this.handleLoginCheck(exchange);
                if (youtube == null) {
                    this.sent401(exchange);
                } else {
                    this.sendJson(exchange, youtube.getMyPlaylistsAsJsonString());
                }
            }
        });

        this.addExchange("/api/issignedin", exchange -> {
            if (exchange.getRequestMethod().equals("GET")) {
                YouTubeAPIWebAdapter youtube = this.handleLoginCheck(exchange);
                String signedIn = youtube == null ? "false" : "true";
                this.sendJson(exchange, String.format("{\"signedin\":\"%s\"}", signedIn));
            }
        });

        this.addExchange("/api/storeAuthCode", exchange -> {
            if (exchange.getRequestMethod().equals("POST")) {
                InputStream is = exchange.getRequestBody();
                BufferedInputStream bis = new BufferedInputStream(is);
                String code = new String(bis.readAllBytes());
                Credential credential = YouTubeAPIAuthorizer.getCredentialFromCode(code);

                String hash = this.hashAndStoreRefreshToken(credential);

                // Send Cookie
                exchange.getResponseHeaders().add(
                        "Set-Cookie",
                        String.format("login=%s; Expires=%s; Domain=%s; Path=%s",
                                hash, "Tue, 18 Dec 2029 04:30:28 GMT", "localhost", "/"));
                // Send Headers.
                exchange.getResponseHeaders().add("Content-Type", ContentType.PLAIN);
                exchange.sendResponseHeaders(200, 0);

                // Close Request.
                exchange.getResponseBody().flush();
                exchange.getResponseBody().close();
            }
        });
    }

    private String hashAndStoreRefreshToken(Credential credential) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Failed to create Hash algorithm.");
            e.printStackTrace();
            return "";
        }
        byte[] encodedHash = digest.digest(credential.getRefreshToken().getBytes(StandardCharsets.UTF_8));
        String hash = Base64.getEncoder().encodeToString(encodedHash);

        hash = cleanHash(hash);

        RefreshTokenSaver.saveRefreshToken(hash, credential.getRefreshToken());
        return hash;
    }

    public static String cleanHash(String hash) {
        hash = hash.replace("/", "");
        return hash;
    }

    private HashMap<String, String> parseCookies(List<String> cookies) {
        HashMap<String, String> map = new HashMap<>();
        for (String cookieString : cookies) {
            String[] rawCookies = cookieString.split(";");
            for (int i = 0; i < rawCookies.length; i++) {
                rawCookies[i] = rawCookies[i].strip();
                int equals = rawCookies[i].indexOf("=");
                map.put(
                        rawCookies[i].substring(0, equals),
                        rawCookies[i].substring(equals + 1)
                );
            }
        }
        return map;
    }

    private void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] response = json.getBytes();
        exchange.getResponseHeaders().add("Content-Type", ContentType.JSON);
        exchange.sendResponseHeaders(200, response.length);

        OutputStream out = exchange.getResponseBody();
        out.write(response);
        out.flush();
        out.close();
    }

    private void sendFile(HttpExchange exchange, File file, String type) throws IOException {
        this.sendFile(exchange, file, 200, type);
    }

    private void sent401(HttpExchange exchange) throws IOException {
        File file = new File(String.format("%s%s", PAGES_PATH, "401notauthorized.html"));
        this.sendFile(exchange, file, 401, ContentType.HTML);
    }

    private void sendFile(HttpExchange exchange, File file, int code, String type) throws IOException {
        FileInputStream fis = new FileInputStream(file);

        byte[] response = fis.readAllBytes();
        exchange.getResponseHeaders().add("Content-Type", type);
        exchange.sendResponseHeaders(code, response.length);

        OutputStream out = exchange.getResponseBody();
        out.write(response);
        out.flush();
        out.close();
    }

    private void addExchange(String path, HttpHandler handler) {
        this.server.createContext(path, handler);
    }

    private YouTubeAPIWebAdapter handleLoginCheck(HttpExchange exchange) {
        String loginHash = this.getLoginHash(exchange);
        if (loginHash.length() > 0) {
            if (!this.webAdapters.containsKey(loginHash)) {
                try {
                    return this.webAdapters.put(loginHash, new YouTubeAPIWebAdapter(new OldSimpleYouTubeAPI(loginHash)));
                } catch (Exception e) {
                    System.out.println("Might be Invalid login");
                    e.printStackTrace();
                    return null;
                }
            }
            return this.webAdapters.get(loginHash);
        }
        return null;
    }

    private String getLoginHash(HttpExchange exchange) {
        List<String> rawCookies = exchange.getRequestHeaders().get("Cookie");
        HashMap<String, String> cookies = this.parseCookies(rawCookies);
        if (cookies.containsKey("login")) {
            String hash = cookies.get("login");
            return WebServer.cleanHash(hash);
        }
        return "";
    }

}
