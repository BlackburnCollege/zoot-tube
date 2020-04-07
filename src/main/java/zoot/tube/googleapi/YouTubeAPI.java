package zoot.tube.googleapi;

import java.util.List;

public interface YouTubeAPI {

    /**
     * Gets a list of the current user's playlists and returns their
     * IDs and Titles.
     *
     * @return the IDs and Titles of the current user's playlists.
     */
    List<ZTPlaylist> getMyPlaylists();

    /**
     * Gets a list of the videos in a playlist, returning their IDs
     * and Titles.
     *
     * @param playlistId the playlist ID.
     * @return the IDs and Titles of the videos in the playlist.
     */
    List<ZTVideo> getVideosFromPlaylist(String playlistId);

    /**
     * Sets the privacy status of a playlist.
     *
     * @param id
     * @param title
     * @param privacyStatus
     * @return
     */
    boolean setPlaylistVisibility(String id, String title, String description, SimpleYouTubeAPI.PrivacyStatus privacyStatus);
}
