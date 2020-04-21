package zoot.tube.googleapi;

public interface YouTubeAPI {

    /**
     * Gets a list of the current user's playlists and returns their
     * IDs and Titles.
     *
     * @return the playlists as a json.
     */
    String getMyPlaylists();

    /**
     * Sets the privacy status of a playlist.
     *
     * @param playlist      the playlist json.
     * @param privacyStatus the privacy status.
     * @return the resulting playlist.
     */
    String updatePlaylistVisibility(String playlist, PrivacyStatus privacyStatus);
}
