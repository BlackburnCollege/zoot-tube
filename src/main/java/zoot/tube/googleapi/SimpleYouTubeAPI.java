package zoot.tube.googleapi;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleYouTubeAPI implements YouTubeAPI{

    public enum PrivacyStatus {
        PRIVATE, PUBLIC, UNLISTED
    }

    /**
     * Every call to the YouTube service must get a fresh instance of
     * the service using this authorizer. The underlying HttpTransport
     * Object may become stale over time, resulting in Exceptions.
     */
    private final YouTubeAPIAuthorizer authorizer;

    public SimpleYouTubeAPI(String user) {
        this.authorizer = new YouTubeAPIAuthorizer(user);
    }

    @Override
    public List<ZTPlaylist> getMyPlaylists() {
        YouTube youtubeService = this.authorizer.getService();
        YouTube.Playlists.List request;
        PlaylistListResponse response;
        try {
            request = youtubeService.playlists()
                    .list("snippet,contentDetails")
                    .setMine(true)
            ;
            response = request.execute();
        } catch (IOException e) {
            System.err.println("Failed to retrieve user's playlists.");
            return null;
        }

        List<ZTPlaylist> playlists = new ArrayList<>();
        for (com.google.api.services.youtube.model.Playlist playlist : response.getItems()) {
            playlists.add(new ZTPlaylist(
                    playlist.getId(),
                    playlist.getSnippet().getTitle(),
                    playlist.getSnippet().getDescription())
            );
        }

        return playlists;
    }

    @Override
    public List<ZTVideo> getVideosFromPlaylist(String id) {
        YouTube youtubeService = this.authorizer.getService();
        YouTube.PlaylistItems.List request;
        PlaylistItemListResponse response;
        try {
            request = youtubeService.playlistItems()
                    .list("snippet,contentDetails")
                    .setMaxResults(50L)
                    .setPlaylistId(id)
            ;
            response = request.execute();
        } catch (IOException e) {
            System.err.println(String.format("Failed to retrieve videos in playlist with ID: %s", id));
            return null;
        }

        List<ZTVideo> videos = new ArrayList<>();
        for (PlaylistItem video : response.getItems()) {
            videos.add(new ZTVideo(video.getSnippet().getTitle(), video.getId()));
        }

        return videos;
    }

    @Override
    public boolean setPlaylistVisibility(String id, String title, String description, PrivacyStatus privacyStatus) {
        YouTube youtubeService = this.authorizer.getService();

        com.google.api.services.youtube.model.Playlist playlist = new com.google.api.services.youtube.model.Playlist();
        playlist.setId(id);

        // THIS WILL OVERWRITE THE DESCRIPTION TO NOTHING
        PlaylistSnippet snippet = new PlaylistSnippet();
        snippet.setTitle(title);
        snippet.setDescription(description);
        playlist.setSnippet(snippet);

        String statusMessage = privacyStatus.toString().toLowerCase();
        PlaylistStatus status = new PlaylistStatus().setPrivacyStatus(statusMessage);
        playlist.setStatus(status);

        YouTube.Playlists.Update request;
        com.google.api.services.youtube.model.Playlist response;
        try {
            request = youtubeService.playlists().update("snippet,status", playlist);
            response = request.execute();
        } catch (IOException e) {
            System.err.println(String.format("Failed to update playlist privacy for playlist id: %s", id));
            return false;
        }

        return response.getStatus().getPrivacyStatus().equals(statusMessage);
    }

}
