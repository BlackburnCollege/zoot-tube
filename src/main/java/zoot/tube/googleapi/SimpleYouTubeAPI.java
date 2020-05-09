package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles YouTube API calls.
 */
public class SimpleYouTubeAPI implements YouTubeAPI {

    private static final String APPLICATION_NAME = "Zoot Tube";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private Credential credential;

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
     * {@inheritDoc}
     */
    @Override
    public List<Playlist> getMyPlaylists() {
        YouTube youtube = this.getService();

        YouTube.Playlists.List request;
        PlaylistListResponse response;
        try {
            request = youtube.playlists()
                    .list("snippet,contentDetails,status")
                    .setMine(true)
            ;
            response = request.execute();
        } catch (IOException e) {
            System.err.println("Failed to retrieve user's playlists.");
            return new ArrayList<>();
        }

        return response.getItems();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Playlist updatePlaylistVisibility(Playlist playlist, PrivacyStatus privacyStatus) {
        YouTube youtube = this.getService();

        String status = privacyStatus.toString().toLowerCase();
        playlist.setStatus(new PlaylistStatus().setPrivacyStatus(status));

        Playlist response;
        try {
            YouTube.Playlists.Update request = youtube.playlists().update("snippet,status", playlist);
            response = request.execute();
        } catch (IOException e) {
            System.err.println(String.format("Failed to update playlist privacy for playlist id: %s", playlist.getId()));
            return playlist;
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Playlist getPlaylistByID(String id) {
        YouTube youtube = this.getService();

        YouTube.Playlists.List request;
        PlaylistListResponse response;
        try {
            request = youtube.playlists()
                    .list("snippet,contentDetails,status")
                    .setId(id)
            ;
            response = request.execute();
        } catch (IOException e) {
            System.err.println("Failed to retrieve user's playlists.");
            e.printStackTrace();
            return new Playlist();
        }

        // There SHOULD only be a single playlist with a given ID.
        return response.getItems().size() > 0 ? response.getItems().get(0) : new Playlist();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlaylistItem> getPlaylistItemsFromPlaylist(Playlist playlist) {
        YouTube youTube = this.getService();

        YouTube.PlaylistItems.List request;
        PlaylistItemListResponse response;
        try {
            request = youTube.playlistItems()
                    .list("snippet,contentDetails,status")
//                    .setMaxResults(99L)
                    .setPlaylistId(playlist.getId())
            ;
            response = request.execute();
        } catch (IOException e) {
            System.err.println("Failed to retrieve playlistItems.");
            e.printStackTrace();
            return new ArrayList<>();
        }

        return response.getItems();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlaylistItem updatePlaylistItemVisibility(PlaylistItem playlistItem, String privacyStatus) {
        YouTube youtube = this.getService();

        String status = privacyStatus.toLowerCase();
        playlistItem.setStatus(new PlaylistItemStatus().setPrivacyStatus(status));

        PlaylistItem response;
        try {
            YouTube.PlaylistItems.Update request = youtube.playlistItems().update("snippet,status", playlistItem);
            response = request.execute();
        } catch (IOException e) {
            System.err.println(String.format("Failed to update playlist privacy for playlist id: %s", playlistItem.getId()));
            return playlistItem;
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Video getVideoByID(String id) {
        YouTube youtube = this.getService();

        YouTube.Videos.List request;
        VideoListResponse response;
        try {
            request = youtube.videos()
                    .list("snippet,contentDetails,status")
                    .setId(id)
            ;
            response = request.execute();
        } catch (IOException e) {
            System.err.println("Failed to retrieve video.");
            return new Video();
        }

        // There SHOULD only be a single playlist with a given ID.
        Video video = response.getItems().size() > 0 ? response.getItems().get(0) : new Video();
        System.out.println(video);
        return video;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Video updateVideoVisibility(Video video, String privacyStatus) {
        YouTube youtube = this.getService();

        video.setStatus(new VideoStatus().setPrivacyStatus(privacyStatus));

        Video response;
        try {
            YouTube.Videos.Update request = youtube.videos().update("snippet,contentDetails,status", video);
            response = request.execute();
        } catch (IOException e) {
            System.err.println(String.format("Failed to update video privacy for video id: %s", video.getId()));
            e.printStackTrace();
            return video;
        }

        return response;
    }

}
