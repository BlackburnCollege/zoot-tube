package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;

/**
 * Handles YouTube API calls.
 */
public class SimpleYouTubeAPI implements YouTubeAPI {

    private static final String APPLICATION_NAME = "Zoot Tube";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String EMPTY_JSON = "{}";

    private Credential credential;
    private Gson gson = new GsonBuilder().create();

    /**
     * Creates a YouTube client handler with the given credential.
     *
     * @param credential the credential to user.
     */
    public SimpleYouTubeAPI(Credential credential) {
        this.credential = credential;
    }

    public SimpleYouTubeAPI() {

    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public Credential getCredential(){
        return credential;
    }
    /**
     * Creates an active YouTube client to use.
     *
     * @return a YouTube client.
     */
    private YouTube getService() {
        return new YouTube.Builder(
                GoogleUtil.getTrustedTransport(),
                JSON_FACTORY,
                credential
        ).setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Gets all the playlists by the user.
     * <p>
     * The user is defined as the account the {@link Credential} in the
     * constructor belongs to.
     * <p>
     * See https://developers.google.com/youtube/v3/docs/playlists#resource
     * for the format.
     *
     * @return a JSON formatted String of the user's playlists.
     */
    public String getMyPlaylists() {
        YouTube youtube = this.getService();

        YouTube.Playlists.List request;
        PlaylistListResponse response;
        try {
            request = youtube.playlists()
                    .list("snippet,contentDetails")
                    .setMine(true)
            ;
            response = request.execute();
        } catch (IOException e) {
            System.err.println("Failed to retrieve user's playlists.");
            return SimpleYouTubeAPI.EMPTY_JSON;
        }

        return gson.toJson(response.getItems());
    }

    /**
     * Sets the privacy of a playlist.
     * <p>
     * See https://developers.google.com/youtube/v3/docs/playlists#resource
     * for the format.
     *
     * @param playlist      a JSON format of a YouTube playlist.
     * @param privacyStatus the desired privacy status
     * @return the resulting playlist JSON after the API call.
     */
    public String updatePlaylistVisibility(String playlist, PrivacyStatus privacyStatus) {
        YouTube youtube = this.getService();

        Playlist modelPlaylist = gson.fromJson(playlist, Playlist.class);
        String status = privacyStatus.toString().toLowerCase();
        modelPlaylist.setStatus(new PlaylistStatus().setPrivacyStatus(status));

        Playlist response;
        try {
            YouTube.Playlists.Update request = youtube.playlists().update("snippet,status", modelPlaylist);
            response = request.execute();
        } catch (IOException e) {
            System.err.println(String.format("Failed to update playlist privacy for playlist id: %s", modelPlaylist.getId()));
            return playlist;
        }

        return gson.toJson(response);
    }

}
