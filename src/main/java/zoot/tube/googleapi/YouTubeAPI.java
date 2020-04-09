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
     * @param playlist the playlist object to update.
     * @param privacyStatus the privacy status.
     * @return true if the change succeeded, otherwise false.
     */
    boolean setPlaylistVisibility(ZTPlaylist playlist, PrivacyStatus privacyStatus);

    /**
     * Sets the privacy status of a playlist.
     *
     * @param id the ID of the video.
     * @param title the title of the video.
     * @param description the description of the video.
     * @param privacyStatus the privacy status.
     * @return true if the change succeeded, otherwise false.
     */
    boolean setPlaylistVisibility(String id, String title, String description, PrivacyStatus privacyStatus);
}
