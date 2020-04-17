package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Handles the authentication of users through Google.
 */
public class GoogleAuthJava {

    private final String clientSecretsUrl;
    private final Collection<String> scopes;
    private final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Creates this using the client secrets JSON file location and scopes specified.
     * <p>
     * The passed scopes will be copied into a new {@link Collection}
     * to prevent changes after this is created.
     *
     * @param clientSecretsUrl the file location of the client secrets JSON file.
     * @param scopes           the scopes the client is requesting.
     */
    public GoogleAuthJava(String clientSecretsUrl, Collection<String> scopes) {
        this.clientSecretsUrl = clientSecretsUrl;
        this.scopes = new ArrayList<>(scopes);
    }

    /**
     * Creates a fresh {@link Credential} for a user.
     * <p>
     * If there is an error while retrieving the Credential, an
     * empty Credential will be returned.
     * <p>
     * This call originates the auth request from this, and does
     * not use a webserver. So the client secrets MUST NOT require
     * a redirect Url. (The OAuth uses the "other" type).
     *
     * @param userId userId or {@code null} if not using a persisted credential store.
     * @return a Credential.
     */
    private Credential authorizeAndGetNewCredential(String userId) {
        Credential credential = this.createEmptyCredential();
        GoogleAuthorizationCodeFlow flow = this.getAuthorizationCodeFlow();

        try {
            credential = new AuthorizationCodeInstalledApp(
                    flow,
                    new LocalServerReceiver()
            ).authorize(userId);
        } catch (IOException e) {
            System.err.println("GoogleAuthJava: Failed to authenticate user: " + userId);
            e.printStackTrace();
        }

        return credential;
    }

    /**
     * Creates a {@link Credential} using a one-time UserCode.
     * <p>
     * Note: Must be using a WebServer to use this method.
     *
     * @param code        the user's code.
     * @param redirectUri an authorized redirectUri known by the Google OAuth Client.
     * @return a Credential for the user the code belongs to.
     */
    private Credential authorizeUsingUserCode(String code, String redirectUri) {
        Credential credential = this.createEmptyCredential();
        GoogleClientSecrets clientSecrets = this.getClientSecrets();

        GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    this.getTrustedTransport(),
                    JSON_FACTORY,
                    "https://oauth2.googleapis.com/token",
                    clientSecrets.getDetails().getClientId(),
                    clientSecrets.getDetails().getClientSecret(),
                    code,
                    redirectUri
            ).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return credential;
        }

        credential.setAccessToken(tokenResponse.getAccessToken());
        credential.setRefreshToken(tokenResponse.getRefreshToken());

        return credential;
    }

    /**
     * Creates a {@link Credential} using a refresh-token.
     * <p>
     * If the authorization fails using the given refresh-token,
     * an empty Credential will be returned.
     *
     * @param refreshToken the refresh-token.
     * @return a Credential.
     */
    private Credential authorizeUsingRefreshToken(String refreshToken) {
        Credential credential = this.createEmptyCredential();
        credential.setRefreshToken(refreshToken);
        try {
            credential.refreshToken();
        } catch (IOException e) {
            System.err.println("GoogleAuthJava: Failed to authorize using refresh-token.");
        }
        return credential;
    }

    /**
     * Creates an empty Credential.
     *
     * @return an empty Credential.
     */
    private Credential createEmptyCredential() {
        GoogleClientSecrets clientSecrets = this.getClientSecrets();
        return new Builder()
                .setTransport(this.getTrustedTransport())
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(
                        clientSecrets.getDetails().getClientId(),
                        clientSecrets.getDetails().getClientSecret()
                )
                .build();
    }

    /**
     * Creates a {@link GoogleAuthorizationCodeFlow} for this client.
     *
     * @return a GoogleAuthorizationCodeFlow for this client.
     */
    private GoogleAuthorizationCodeFlow getAuthorizationCodeFlow() {
        NetHttpTransport httpTransport = this.getTrustedTransport();
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                this.getClientSecrets(),
                scopes
        ).build();
    }

    /**
     * Loads the client credentials stored in a file.
     * <p>
     * If there is an error while loading the credentials, a blank
     * credential will be returned.
     *
     * @return the client credentials.
     */
    private GoogleClientSecrets getClientSecrets() {
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        try {
            clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new FileReader(clientSecretsUrl));
        } catch (FileNotFoundException e) {
            System.err.println("GoogleAuthJava: Client Secrets File Not Found.");
        } catch (IOException e) {
            System.err.println("GoogleAuthJava: Error Reading Client Secrets.");
            e.printStackTrace();
        }
        return clientSecrets;
    }

    /**
     * Gets a {@link GoogleNetHttpTransport} trusted transport.
     *
     * @return a GoogleNetHttpTransport trusted transport.
     */
    private NetHttpTransport getTrustedTransport() {
        NetHttpTransport httpTransport = null;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            System.err.println("GoogleAuthJava: GeneralSecurityException while creating Trusted Transport.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("GoogleAuthJava: IOException while creating Trusted Transport.");
            e.printStackTrace();
        }
        return httpTransport;
    }

}
