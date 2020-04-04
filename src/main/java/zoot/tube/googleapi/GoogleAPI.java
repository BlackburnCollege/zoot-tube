package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("ALL")
public class GoogleAPI {

    private static final String CLIENT_SECRETS = "client_secret.json";
    private static final String APPLICATION_NAME = "Zoot Tube";
    private static final Collection<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/youtube.readonly");
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final YouTube youtubeService;

    public GoogleAPI() throws GeneralSecurityException, IOException {
        this.youtubeService = this.getService();
    }

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    private Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        File secrets = new File("src/main/resources/client_secret.json");
        InputStream in = new FileInputStream(secrets);
//        InputStream in = GoogleAPI.class.getResourceAsStream(CLIENT_SECRETS);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    private YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    }

    public SearchListResponse search(String keywords) throws IOException {
        YouTube.Search.List request = this.youtubeService.search().list("snippet");
        return request.setQ(keywords).execute();
    }

    public String getVideoTitleByID(String id) throws IOException {
        YouTube.Videos.List request = this.youtubeService.videos().list("snippet");
        VideoListResponse response = request.setId(id).execute();
        String title = response.toString();
        int startIndex = title.indexOf("\"title\":") + 9;
        int endIndex = title.indexOf("\"", startIndex + 1);
        return title.substring(startIndex, endIndex);
    }

}
