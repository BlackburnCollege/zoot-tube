package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Handles the authentication of a user to give this app access
 * to their YouTube account and store a long-term refresh-token.
 * <p>
 * Every call to the YouTube API SHOULD ask for a fresh YouTube object
 * using the {@link YouTubeAPIAuthorizer#getService()} method. This
 * is due to the stored HttpTransport object potentially becoming
 * stale over time, which may cause Exceptions to be thrown.
 * <p>
 * Source: https://developers.google.com/youtube/v3/docs
 */
public class YouTubeAPIAuthorizer {

    private static final String CLIENT_SECRETS_URL = "src/main/resources/client_secret.json";
    private static final String CLIENT_ID = "991685953336-aa5vls4tv34fk6sa6u0t6m0po065kec4.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "FllR3ZmJmlU44JnMK7STXBKj";
    private static final String APPLICATION_NAME = "Zoot Tube";
    private static final Collection<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/youtube.force-ssl"
    );
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final String user;
    private Credential credential = null;

    public YouTubeAPIAuthorizer(String user) {
        this.user = user;
    }

    /**
     * Creates an authorized Credential object and stores the refresh-token.
     *
     * @return an authorized Credential object.
     */
    private Credential authorize(final NetHttpTransport httpTransport) {
        // If the credential is null, try loading a saved refresh token for the user and generate a new credential.
        if (this.credential == null) {
            this.credential = this.attemptCreateCredentialUsingRefreshToken(this.user, httpTransport);
        }
        // If there is still no credential token, get a new credential and save the refresh token.
        if (this.credential == null) {
            this.credential = this.createNewCredential(this.user, httpTransport);
        }

        return this.credential;
    }

    private Credential attemptCreateCredentialUsingRefreshToken(String user, HttpTransport httpTransport) {
        Credential credential = null;
        // Load the refresh-token for the user.
        String refreshToken = RefreshTokenSaver.loadRefreshToken(user);
        // If the refresh-token exists, create a new credential.
        if (refreshToken.length() > 0) {
            credential = new Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                    .build();
            credential.setRefreshToken(refreshToken);
            try {
                credential.refreshToken();
            } catch (IOException e) {
                // Refreshing the token failed.
                credential = null;
            }
        }
        return credential;
    }

    private Credential createNewCredential(String user, HttpTransport httpTransport) {
        Credential credential;
        InputStream is;
        try {
            is = new FileInputStream(CLIENT_SECRETS_URL);
        } catch (FileNotFoundException e) {
            System.err.println("Client Secrets FileNotFound.");
            return null; // Creating a Credential failed.
        }

        GoogleClientSecrets clientSecrets;
        try {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(is));
        } catch (IOException e) {
            System.err.println("Failed to load Client Secrets using GoogleClientSecrets.load(JsonFactory, Reader).");
            return null; // Creating a Credential failed.
        }

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        )
                .setAccessType("offline")
                .build();

        try {
            credential = new AuthorizationCodeInstalledApp(
                    flow,
                    new LocalServerReceiver()
            ).authorize(user);
        } catch (IOException e) {
            System.err.println("Failed to authorize user.");
            return null; // Creating a Credential failed.
        }

        RefreshTokenSaver.saveRefreshToken(user, credential.getRefreshToken());
        return credential;
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service.
     */
    public YouTube getService() {
        NetHttpTransport httpTransport;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            System.err.println("Failed to create NetHttpTransport for service.");
            e.printStackTrace();
            return null;
        }
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    }

}
