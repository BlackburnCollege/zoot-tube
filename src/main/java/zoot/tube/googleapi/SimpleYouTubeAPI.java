package zoot.tube.googleapi;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleYouTubeAPI implements YouTubeAPI{

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
    public List<Playlist> getMyPlaylists() {
        YouTube youtubeService = this.authorizer.getService();
        YouTube.Playlists.List request;
        PlaylistListResponse response;
        try {
            request = youtubeService.playlists().list("snippet,contentDetails");
            response = request.setMine(true).execute();
        } catch (IOException e) {
            System.err.println("Failed to retrieve user's playlists.");
            return null;
        }

        List<Playlist> playlists = new ArrayList<>();
        for (com.google.api.services.youtube.model.Playlist playlist : response.getItems()) {
            playlists.add(new Playlist(playlist.getSnippet().getTitle(), playlist.getId()));
        }

        return playlists;
    }

    @Override
    public List<Video> getVideosFromPlaylist(String id) {
        YouTube youtubeService = this.authorizer.getService();
        YouTube.PlaylistItems.List request;
        PlaylistItemListResponse response;
        try {
            request = youtubeService.playlistItems()
                    .list("snippet,contentDetails")
                    .setMaxResults(50L)
                    .setPlaylistId(id);
            response = request.execute();
        } catch (IOException e) {
            System.err.println(String.format("Failed to retrieve videos in playlist with ID: %s", id));
            return null;
        }

        List<Video> videos = new ArrayList<>();
        for (PlaylistItem video : response.getItems()) {
            videos.add(new Video(video.getSnippet().getTitle(), video.getId()));
        }

        return videos;
    }

}
