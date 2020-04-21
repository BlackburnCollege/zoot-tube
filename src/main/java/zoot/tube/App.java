package zoot.tube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import zoot.tube.googleapi.GoogleAuthJava;
import zoot.tube.googleapi.RefreshTokenSaver;
import zoot.tube.googleapi.SimpleYouTubeAPI;
import zoot.tube.googleapi.YouTubeAPI;
import zoot.tube.websocketserver.Server;

public class App {

    /**
     * Ignore this method, it's so the JUnit tests pass. :)
     *
     * @return Hello world.
     */
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) {
        new App();
    }

    private GoogleAuthJava authenticator;
    private YouTubeAPI youtubeAPI;
    private Server server;
    private Gson gson = new GsonBuilder().create();

    public App() {
        // no touchy!!!
        String user = "user";
        Credential credential = this.getCredential(user);
        youtubeAPI = new SimpleYouTubeAPI(credential);
        // ============================================

        // no touchy!!!
        this.server = new Server(8080);
        this.addMessageHandlers();
        this.server.start();
        this.openWebPage();
        this.waitToClose();
        // ============================================
    }

    private void addMessageHandlers() {
        this.server.addMessageHandler((message) -> {
            ApiRequest request = this.gson.fromJson(message, ApiRequest.class);
            if (request.getHeader().equals("getMyPlaylists")) {
                String myPlaylistsAsJSON = youtubeAPI.getMyPlaylists();
                String response = this.wrapIntoJsonObjectDataRaw("playlists", myPlaylistsAsJSON);
                System.out.println("Sending playlists");
                this.server.sendMessage(response);
            }
        });
    }

    private String wrapIntoJsonObject(String header, String data) {
        ApiRequest response = new ApiRequest(header, data);
        return gson.toJson(response);
    }

    private String wrapIntoJsonObjectDataRaw(String header, String data) {
        return String.format("{\"header\":\"%s\", \"data\": %s}", header, data);
    }

    private void openWebPage() {
        File home = new File("src\\main\\resources\\website\\ZootTube.html");
        String url = home.getAbsolutePath();
        System.out.println("\nOpen this file in your web browser if it doesn't open automatically:\n" + url + "\n\n");
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(home.toURI());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Credential getCredential(String user) {
        Credential credential;
        String refreshToken = RefreshTokenSaver.loadRefreshToken(user);
        this.authenticator = new GoogleAuthJava(
                "src/main/resources/client_secret.json",
                Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl")
        );
        if (refreshToken.length() > 0) {
            credential = authenticator.authorizeUsingRefreshToken(refreshToken);
        } else {
            credential = authenticator.authorizeAndGetNewCredential(null);
            RefreshTokenSaver.saveRefreshToken(user, credential.getRefreshToken());
        }
        return credential;
    }

    private void waitToClose() {
        Scanner input = new Scanner(System.in);
        System.out.println("Press enter to close server.");
        input.nextLine();

        this.server.shutdown();
    }
}
