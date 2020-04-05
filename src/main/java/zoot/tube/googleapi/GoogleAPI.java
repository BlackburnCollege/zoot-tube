package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Source: https://developers.google.com/youtube/v3/docs
 */
public class GoogleAPI {

    private static final String USER = "user";
    private static final String CLIENT_ID = "991685953336-aa5vls4tv34fk6sa6u0t6m0po065kec4.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "FllR3ZmJmlU44JnMK7STXBKj";
    private static final String APPLICATION_NAME = "Zoot Tube";
    private static final Collection<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/youtube.force-ssl"
    );
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private YouTube youtubeService;
    private Credential credential = null;

    private void updateService() throws GeneralSecurityException, IOException {
        this.youtubeService = this.getService();
    }

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException the authorization failed.
     */
    private Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // If the credential is null, try loading a saved refresh token for the user
        // and generate a new credential.
        if (this.credential == null) {
            String refreshToken = CredentialSaver.getRefreshToken(USER);
            if (refreshToken.length() > 0) {
                TokenResponse response = new GoogleRefreshTokenRequest(
                        httpTransport,
                        JSON_FACTORY,
                        refreshToken,
                        CLIENT_ID,
                        CLIENT_SECRET
                )
                        .setScopes(SCOPES)
                        .execute();

                credential = new ZootTubeCredentialBuilder()
                        .setTransport(httpTransport)
                        .setJsonFactory(JSON_FACTORY)
                        .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                        .build();
                credential.setRefreshToken(refreshToken);
                credential.setAccessToken(response.getAccessToken());
                credential.refreshToken();
            }
        }
        // If there is still no refresh token, get a new credential and save the refresh token.
        if (this.credential == null) {
            File secrets = new File("src/main/resources/client_secret.json");
            InputStream in = new FileInputStream(secrets);
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setAccessType("offline").build();
            this.credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(USER);
            CredentialSaver.saveRefreshToken(USER, this.credential.getRefreshToken());
        }

        return this.credential;
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

    public SearchListResponse search(String keywords) throws IOException, GeneralSecurityException {
        this.updateService();
        YouTube.Search.List request = this.youtubeService.search().list("snippet");
        return request.setQ(keywords).execute();
    }

    public VideoListResponse getVideoSnippetByID(String id) throws IOException, GeneralSecurityException {
        this.updateService();
        YouTube.Videos.List request = this.youtubeService.videos().list("snippet");
        return request.setId(id).execute();
    }

    public PlaylistListResponse getMyPlaylists() throws IOException, GeneralSecurityException {
        this.updateService();
        YouTube.Playlists.List request = youtubeService.playlists().list("snippet");
        return request.setMaxResults((long) 25).setMine(true).execute();
    }

    public String[] getMyPlaylistIDs() throws IOException, GeneralSecurityException {
        this.updateService();
        YouTube.Playlists.List request = this.youtubeService.playlists().list("id");
        PlaylistListResponse response = request.setMine(true).execute();
        List<Playlist> playlists = response.getItems();
        String[] ids = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) {
            ids[i] = playlists.get(i).getId();
        }
        return ids;
    }

    public String getVideoTitleFromSnippet(VideoListResponse video) throws IOException, GeneralSecurityException {
        this.updateService();
        String title = video.toString();
        int startIndex = title.indexOf("\"title\":") + 9;
        int endIndex = title.indexOf("\"", startIndex + 1);
        return title.substring(startIndex, endIndex);
    }

}
