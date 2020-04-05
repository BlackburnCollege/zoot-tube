package zoot.tube.googleapi;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import java.io.IOException;
import java.util.List;

public class YouTubeAPI {

    /**
     * Every call to the YouTube service must get a fresh instance of
     * the service using this authorizer. Access tokens are only valid
     * for about 1 hour.
     */
    private final YouTubeAPIAuthorizer authorizer;

    public YouTubeAPI(String user) {
        this.authorizer = new YouTubeAPIAuthorizer(user);
    }

    public SearchListResponse search(String keywords) throws IOException {
        YouTube youtubeService = this.authorizer.getService();
        YouTube.Search.List request = youtubeService.search().list("snippet");
        return request.setQ(keywords).execute();
    }

    public VideoListResponse getVideoSnippetByID(String id) throws IOException {
        YouTube youtubeService = this.authorizer.getService();
        YouTube.Videos.List request = youtubeService.videos().list("snippet");
        return request.setId(id).execute();
    }

    public PlaylistListResponse getMyPlaylists() throws IOException {
        YouTube youtubeService = this.authorizer.getService();
        YouTube.Playlists.List request = youtubeService.playlists().list("snippet");
        return request.setMaxResults((long) 25).setMine(true).execute();
    }

    public String[] getMyPlaylistIDs() throws IOException {
        YouTube youtubeService = this.authorizer.getService();
        YouTube.Playlists.List request = youtubeService.playlists().list("id");
        PlaylistListResponse response = request.setMine(true).execute();
        List<Playlist> playlists = response.getItems();
        String[] ids = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) {
            ids[i] = playlists.get(i).getId();
        }
        return ids;
    }

    public String getVideoTitleFromSnippet(VideoListResponse video) {
        String title = video.toString();
        int startIndex = title.indexOf("\"title\":") + 9;
        int endIndex = title.indexOf("\"", startIndex + 1);
        return title.substring(startIndex, endIndex);
    }
}
