package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;

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

//    private static final String CLIENT_SECRETS_URL = "src/main/resources/client_secret.json";
//    private static final String CLIENT_ID = "991685953336-aa5vls4tv34fk6sa6u0t6m0po065kec4.apps.googleusercontent.com";
//    private static final String CLIENT_SECRET = "FllR3ZmJmlU44JnMK7STXBKj";
//    private static final Collection<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl");
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CLIENT_SECRETSV2_URL = "src/main/resources/client_secretsv2.json";
    private static final String APPLICATION_NAME = "Zoot Tube";

    private Credential credential;

    public YouTubeAPIAuthorizer(String userHash) throws NoRefreshTokenFound {
        try {
            this.credential = this.attemptCreateCredentialUsingRefreshToken(userHash, GoogleNetHttpTransport.newTrustedTransport());
            if (this.credential == null) {
                throw new NoRefreshTokenFound();
            }
        } catch (GeneralSecurityException | IOException e) {
            System.err.println("Failed to create transport.");
            throw new NoRefreshTokenFound();
        }

    }

    private Credential attemptCreateCredentialUsingRefreshToken(String user, HttpTransport httpTransport) {
        Credential credential = null;
        // Load the refresh-token for the user.
        String refreshToken = RefreshTokenSaver.loadRefreshToken(user);
        GoogleClientSecrets clientSecrets;
        try {
            clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new FileReader(CLIENT_SECRETSV2_URL));
        } catch (IOException e) {
            System.err.println("Failed to load API credentials.");
            return null;
        }
        // If the refresh-token exists, create a new credential.
        if (refreshToken.length() > 0) {
            credential = new Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setClientSecrets(clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret())
                    .build();
            credential.setRefreshToken(refreshToken);
            try {
                credential.refreshToken();
            } catch (IOException e) {
                System.err.println("Failed to refresh token.");
                credential = null;
            }
        }
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
        return new YouTube.Builder(httpTransport, JSON_FACTORY, this.credential).setApplicationName(APPLICATION_NAME).build();
    }

    public static Credential getCredentialFromCode(String code) {
        GoogleClientSecrets clientSecrets;
        try {
            clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new FileReader(CLIENT_SECRETSV2_URL));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    clientSecrets.getDetails().getClientId(),
                    clientSecrets.getDetails().getClientSecret(),
                    code,
                    "http://localhost:8080"
            ).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Credential credential;
        credential = new Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret())
                .build();
        credential.setAccessToken(tokenResponse.getAccessToken());
        credential.setRefreshToken(tokenResponse.getRefreshToken());

        try {
            RefreshTokenSaver.saveRefreshToken(tokenResponse.parseIdToken().getPayload().getEmail(), credential.getRefreshToken());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return credential;
    }

}
