package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Shared Google utilities for this package.
 */
public class GoogleUtil {

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Gets a {@link GoogleNetHttpTransport} trusted transport.
     *
     * @return a GoogleNetHttpTransport trusted transport.
     */
    public static NetHttpTransport getTrustedTransport() {
        NetHttpTransport httpTransport = null;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            System.err.println("GoogleUtil: GeneralSecurityException while creating Trusted Transport.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("GoogleUtil: IOException while creating Trusted Transport.");
            e.printStackTrace();
        }
        return httpTransport;
    }

    /**
     * Loads the client credentials stored in a file.
     * <p>
     * If there is an error while loading the credentials, a blank
     * credential will be returned.
     *
     * @return the client credentials.
     */
    public static GoogleClientSecrets getClientSecrets(String clientSecretsUrl) {
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        try {
            clientSecrets = GoogleClientSecrets.load(
                    JSON_FACTORY,
                    new FileReader(clientSecretsUrl));
        } catch (FileNotFoundException e) {
            System.err.println("GoogleUtil: Client Secrets File Not Found URL: " + clientSecretsUrl);
        } catch (IOException e) {
            System.err.println("GoogleUtil: Error Reading Client Secrets.");
            e.printStackTrace();
        }
        return clientSecrets;
    }

    public static Userinfoplus getUserInfo(Credential credential) {
        Oauth2 oauth2 = new Oauth2.Builder(GoogleUtil.getTrustedTransport(), JSON_FACTORY, credential).setApplicationName("Oauth2").build();
        Userinfoplus userinfoplus = null;
        try {
            userinfoplus = oauth2.userinfo().get().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userinfoplus;
    }
}
