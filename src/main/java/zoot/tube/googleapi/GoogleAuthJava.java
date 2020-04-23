package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Handles the authentication of users through Google.
 */
public class GoogleAuthJava {

    private final Collection<String> scopes;
    private final JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private final GoogleClientSecrets clientSecrets;

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
        this.scopes = new ArrayList<>(scopes);
        this.clientSecrets = GoogleUtil.getClientSecrets(clientSecretsUrl);
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
    public Credential authorizeAndGetNewCredential(String userId) {
        // Create an empty Credential.
        Credential credential = this.createEmptyCredential();
        // Get an authorization code flow.
        GoogleAuthorizationCodeFlow flow = this.getAuthorizationCodeFlow();

        // Attempt to have user authorize this app.
        // Attempts to open a Google authorization page in the default web browser.
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
    public Credential authorizeUsingUserCode(String code, String redirectUri) {
        Credential credential = this.createEmptyCredential();

        GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    GoogleUtil.getTrustedTransport(),
                    this.jsonFactory,
                    "https://oauth2.googleapis.com/token",
                    this.clientSecrets.getDetails().getClientId(),
                    this.clientSecrets.getDetails().getClientSecret(),
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
    public Credential authorizeUsingRefreshToken(String refreshToken) {
        // Create an empty Credential.
        Credential credential = this.createEmptyCredential();

        // Set the refresh token.
        credential.setRefreshToken(refreshToken);

        // Attempt to get a full Credential from the refresh token.
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
        return new Builder()
                .setTransport(GoogleUtil.getTrustedTransport())
                .setJsonFactory(this.jsonFactory)
                .setClientSecrets(
                        this.clientSecrets.getDetails().getClientId(),
                        this.clientSecrets.getDetails().getClientSecret()
                )
                .build();
    }

    /**
     * Creates a {@link GoogleAuthorizationCodeFlow} for this client.
     *
     * @return a GoogleAuthorizationCodeFlow for this client.
     */
    private GoogleAuthorizationCodeFlow getAuthorizationCodeFlow() {
        return new GoogleAuthorizationCodeFlow.Builder(
                GoogleUtil.getTrustedTransport(),
                this.jsonFactory,
                this.clientSecrets,
                this.scopes
        ).build();
    }

}
