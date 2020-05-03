package zoot.tube.schedule;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.Video;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import zoot.tube.googleapi.GoogleAuthJava;
import zoot.tube.googleapi.PrivacyStatus;
import zoot.tube.googleapi.RefreshTokenSaver;
import zoot.tube.googleapi.SimpleYouTubeAPI;

public class TaskScheduler {

    private ScheduledExecutorService scheduler;
    private GoogleAuthJava authenticator;
    private Gson gson = new GsonBuilder().create();

    public TaskScheduler(String clientSecretsURL, Collection<String> scopes) {
        this.authenticator = new GoogleAuthJava(clientSecretsURL, scopes);
        this.startup();
    }

    public void startup() {
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
        this.loadSavedTasks();
    }

    /**
     * Adds a scheduled Task to make all the Videos in a Playlist private.
     *
     * @param user a user with a stored refresh token.
     * @param start when the Task should run by.
     * @param expire when the Task should revert any changes.
     * @param playlistID the ID of the playlist.
     */
    public void scheduleMakeVideosInPlaylistPrivate(String user, Date start, Date expire, String playlistID) {
        Task task = new Task(user, TaskType.MAKE_VIDEOS_IN_PLAYLIST_PRIVATE, start, expire, playlistID);
        TaskIO.saveTask(task);
        loadTaskIntoScheduler(task);
    }

    /**
     * Loads all saved Tasks into the Scheduler.
     */
    private void loadSavedTasks() {
        List<Task> savedTasks = TaskIO.loadSavedTasks();
        for (Task task : savedTasks) {
            loadTaskIntoScheduler(task);
        }
    }

    /**
     * Loads a Task into the Scheduler.
     *
     * @param task the Task to load into the Scheduler.
     */
    private void loadTaskIntoScheduler(Task task) {
        long delay = task.getStart().getTime() - System.currentTimeMillis();
        Runnable runnable = createRunnableFromTask(task);
        scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates the appropriate Runnable for a Task.
     *
     * @param task the Task to use.
     * @return a Runnable for the Task.
     */
    private Runnable createRunnableFromTask(Task task) {
        Runnable runnable = null;

        String refreshToken = RefreshTokenSaver.loadRefreshToken(task.getUser());
        Credential credential = authenticator.authorizeUsingRefreshToken(refreshToken);

        if (task.getTaskType().equals(TaskType.MAKE_VIDEOS_IN_PLAYLIST_PRIVATE)) {
            runnable = () -> {
                long now = System.currentTimeMillis();
                if (now < task.getExpire().getTime()) {
                    makeVideosInPlaylistPrivate(task, credential);

                    // Schedule this task to run again after it expires.
                    long delay = task.getExpire().getTime() - now;
                    scheduler.schedule(createRunnableFromTask(task), delay, TimeUnit.MILLISECONDS);
                } else {
                    revertVideosInPlaylist(task, credential);
                    TaskIO.deleteTask(task);
                }
            };
        }
        return runnable;
    }

    /**
     * Makes all Videos in a Playlist private.
     *
     * Stores the state of the Videos before changes.
     *
     * @param task the Task to use.
     * @param credential the Credential to use.
     */
    private void makeVideosInPlaylistPrivate(Task task, Credential credential) {
        SimpleYouTubeAPI youTubeAPI = new SimpleYouTubeAPI(credential);
        Playlist playlist = youTubeAPI.getPlaylistByID(task.getRelevantID());
        List<PlaylistItem> playlistItems = youTubeAPI.getPlaylistItemsFromPlaylist(playlist);
        List<Video> videos = new ArrayList<>();
        for (PlaylistItem playlistItem : playlistItems) {
            Video video = youTubeAPI.getVideoByID(playlistItem.getId());
            videos.add(video);
        }

        Video[] videosAsArray = videos.toArray(new Video[0]);
        TaskIO.saveContentUnderTask(task, gson.toJson(videosAsArray));

        for (Video video : videos) {
            youTubeAPI.updateVideoVisibility(video, PrivacyStatus.PRIVATE);
        }
    }

    /**
     * Reverts all Videos in a Playlist to their stored version.
     *
     * @param task the Task to use.
     * @param credential the Credential to use.
     */
    private void revertVideosInPlaylist(Task task, Credential credential) {
        SimpleYouTubeAPI youTubeAPI = new SimpleYouTubeAPI(credential);
        String contentAsString = TaskIO.loadContentUnderTask(task);
        Video[] videos = gson.fromJson(contentAsString, Video[].class);

        for (Video video : videos) {
            youTubeAPI.updateVideoVisibility(video, video.getStatus().getPrivacyStatus());
        }

        TaskIO.deleteContentUnderTask(task);
    }


}
