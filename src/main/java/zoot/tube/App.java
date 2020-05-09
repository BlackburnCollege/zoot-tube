package zoot.tube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.model.Playlist;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import zoot.tube.googleapi.*;
import zoot.tube.websocketserver.Server;

/**
 * Handles the creation of a web socket server and a YouTube client, and the
 * communications between.
 */
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

    private static final String CLIENT_SECRETS_URL = "src/main/resources/client_secret.json";
    private static final Collection<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl", "https://www.googleapis.com/auth/userinfo.email");
    private GoogleAuthJava authenticator;
    private YouTubeAPI youtubeAPI;
    private Server server;
    private Gson gson = new GsonBuilder().create();

    /**
     * Starts the app.
     */
    public App() {
        // This will need to be moved to login functionality.
        // Get the user's Credential.
        this.authenticator = new GoogleAuthJava(
                "src/main/resources/client_secret.json",
                Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl", "https://www.googleapis.com/auth/userinfo.email")
        );
        youtubeAPI = new SimpleYouTubeAPI();
        // youtubeAPI.setCredential(credential); // setting a Credential.
        // =================


        // Create the web socket server.
        this.server = new Server(8080);
        // Add message handlers to the server.
        this.addMessageHandlers();
        // Start the server.
        this.server.start();

        // Try to open the home page
        this.openWebPage();

        // Shutdown when the user presses Enter a few times in the console.
        this.waitToClose();
    }

    /**
     * Adds message handlers to the server.
     */
    private void addMessageHandlers() {
        // Add a greeting handler.
        this.server.addMessageHandler(Server.createDefaultGreeting(this.server, "Hello "));

        // Add a handler for requesting the user's playlists.
        this.server.addMessageHandler((message) -> {
            // Parse the message.
            ApiRequest request = this.gson.fromJson(message, ApiRequest.class);

            // Make sure the message is asking for the user's playlists.
            if (request.getHeader().equals("getMyPlaylists")) {
                // Get the playlists.
                List<Playlist> myPlaylists = youtubeAPI.getMyPlaylists();
                // Package up the playlists into a response.
                String response = this.wrapIntoJsonObjectDataRaw("playlists", gson.toJson(myPlaylists));
                System.out.println("Sending playlists");
                this.server.sendMessage(response);
            }

            if (request.getHeader().equals("signIn")) {

                String user = "user";
//                if(RefreshTokenSaver.loadRefreshToken(user).length() > 0){
//                    authenticator.authorizeUsingRefreshToken(RefreshTokenSaver.loadRefreshToken(user));
//                }else {
                // Credential credential = this.getCredential(user);
                Credential credential = authenticator.authorizeAndGetNewCredential(null);
                youtubeAPI.setCredential(credential);

                if (credential != null) {

                    // Create the YouTubeAPI
                    String usersEmail = GoogleUtil.getUserInfo(credential).getEmail();
                    System.out.println(usersEmail);
                    RefreshTokenSaver.saveRefreshToken(usersEmail, credential.getRefreshToken());

                    String jSONEmail = this.wrapIntoJsonObject("successfulSignIn", usersEmail);
                    //String response = this.wrapIntoJsonObjectDataRaw("email", jSONEmail);
                    this.server.sendMessage(jSONEmail);
                }

            }

            if (request.getHeader().equals("signOut")) {
                youtubeAPI.setCredential(null);
                String signOut = this.wrapIntoJsonObject("successfulSignOut", null);
                this.server.sendMessage(signOut);
            }

            if (request.getHeader().equals("isSignedIn")) {
                String usersEmail = GoogleUtil.getUserInfo(this.getYouTubeAPICredential()).getEmail();
                if (this.getYouTubeAPICredential() != null) {
                    String credentialStatus
                            = this.wrapIntoJsonObject("signedIn", usersEmail);
                    this.server.sendMessage(credentialStatus);
                } else {
                    String credentialStatus
                            = this.wrapIntoJsonObject("signedOut", null);
                    this.server.sendMessage(credentialStatus);
                }
            }

        });
    }

    /**
     * Use this to wrap the "data" into a String.
     *
     * @param header the header label
     * @param data the data to include.
     * @return a JSON formatted string with the header and data filled in.
     */
    private String wrapIntoJsonObject(String header, String data) {
        ApiRequest response = new ApiRequest(header, data);
        return gson.toJson(response);
    }

    /**
     * Use this when the "data" portion is already in a JSON format.
     *
     * @param header the header label
     * @param data the data to include.
     * @return a JSON formatted string with the header and data filled in.
     */
    private String wrapIntoJsonObjectDataRaw(String header, String data) {
        return String.format("{\"header\":\"%s\", \"data\": %s}", header, data);
    }

    /**
     * Attempts to open the home page in the default web browser.
     */
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

    /**
     * Gets a {@link Credential} for this to use.
     *
     * @param user the name to store the Credential under.
     * @return a Credential for this app.
     */
    private Credential getCredential(String user) {
        Credential credential;
        String refreshToken = RefreshTokenSaver.loadRefreshToken(user);
        this.authenticator = new GoogleAuthJava(
                "src/main/resources/client_secret.json",
                Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl", "https://www.googleapis.com/auth/userinfo.email")
        );
        if (refreshToken.length() > 0) {
            credential = authenticator.authorizeUsingRefreshToken(refreshToken);
        } else {
            credential = authenticator.authorizeAndGetNewCredential(null);
            RefreshTokenSaver.saveRefreshToken(user, credential.getRefreshToken());
        }
        return credential;
    }

    /**
     * When the user presses the Enter key a few times this app will shutdown.
     */
    private void waitToClose() {
        Scanner input = new Scanner(System.in);
        System.out.println("Press enter to close server.");
        input.nextLine();

        this.server.shutdown();
    }

    public Credential getYouTubeAPICredential() {
        Credential tempCredential = this.youtubeAPI.getCredential();
        if (tempCredential != null) {
            return tempCredential;
        }
        return null;
    }
}
