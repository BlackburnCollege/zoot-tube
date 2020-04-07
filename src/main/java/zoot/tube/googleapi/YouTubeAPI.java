package zoot.tube.googleapi;

import java.util.List;

public interface YouTubeAPI {

    List<Playlist> getMyPlaylists();

    List<Video> getVideosFromPlaylist(String playlistId);
}
