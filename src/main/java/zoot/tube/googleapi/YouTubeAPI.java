package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.Video;
import java.util.List;

/**
 * The required functionality for use in the app when interacting with a YouTubeAPI.
 */
public interface YouTubeAPI {

    /**
     * Gets a list of the current user's playlists and returns their
     * IDs and Titles.
     * <p>
     * See https://developers.google.com/youtube/v3/docs/playlists
     * for the resource representation.
     *
     * @return the user's Playlists.
     */
    List<Playlist> getMyPlaylists();

    /**
     * Sets the privacy status of a playlist.
     * <p>
     * See https://developers.google.com/youtube/v3/docs/playlists
     * for the resource representation.
     *
     * @param playlist      the playlist object to update on YouTube.
     * @param privacyStatus the new privacy status.
     * @return the resulting playlist.
     */
    Playlist updatePlaylistVisibility(Playlist playlist, PrivacyStatus privacyStatus);

    /**
     * Gets a single playlist by its ID.
     * <p>
     * See https://developers.google.com/youtube/v3/docs/playlists
     * for the resource representation.
     *
     * @param id the ID of the playlist.
     * @return the Playlist resulting from the API call.
     */
    Playlist getPlaylistByID(String id);

    /**
     * Gets all the PlaylistItems (videos) from a playlist.
     * <p>
     * See https://developers.google.com/youtube/v3/docs/playlistItems
     * for the resource representation.
     *
     * @param playlist the playlist.
     * @return the PlaylistItems.
     */
    List<PlaylistItem> getPlaylistItemsFromPlaylist(Playlist playlist);

    /**
     * Updates the privacy status of a PlaylistItem.
     * <p>
     * See https://developers.google.com/youtube/v3/docs/playlistItems
     * for the resource representation.
     *
     * @param playlistItem  the PlaylistItem to update on YouTube.
     * @param privacyStatus the new privacy status.
     * @return the PlaylistItem resulting from the API call.
     */
    PlaylistItem updatePlaylistItemVisibility(PlaylistItem playlistItem, String privacyStatus);

    /**
     * Gets a Video by its ID.
     * <p>
     * See https://developers.google.com/youtube/v3/docs/videos
     * for the resource representation.
     *
     * @param id the ID of the Video.
     * @return the Video.
     */
    Video getVideoByID(String id);

    /**
     * Updates the privacy status of a Video
     * <p>
     * See https://developers.google.com/youtube/v3/docs/videos
     * for the resource representation.
     *
     * @param video         the Video yo update on YouTube.
     * @param privacyStatus the new privacy status.
     * @return the Video resulting from the API call.
     */
    Video updateVideoVisibility(Video video, String privacyStatus);

    /**
     * Sets the Credential to use for all API requests.
     *
     * @param credential the Credential to use.
     */
    void setCredential(Credential credential);
    
    Credential getCredential();
}
