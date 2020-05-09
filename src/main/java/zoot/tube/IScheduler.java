package zoot.tube;

import java.util.Date;

public interface IScheduler {

    /**
     * Schedules all Videos in a Playlist to go private when the
     * startTime is reached. Then reverts the changed Videos back
     * to their previous privacy when the expireTime is reached.
     *
     * @param userId     the ID to use with {@link zoot.tube.googleapi.RefreshTokenSaver#loadRefreshToken(String)}.
     * @param playlistId the ID of the playlist to use with {@link zoot.tube.googleapi.YouTubeAPI#getPlaylistByID(String)}.
     * @param startTime  when the task should start.
     * @param expireTime when the task should revert Videos back.
     * @return true if the task was successfully scheduled, otherwise false.
     */
    boolean scheduleVideosInPlaylistToGoPrivate(String userId, String playlistId, Date startTime, Date expireTime);

}
