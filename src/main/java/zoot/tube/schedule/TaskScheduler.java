package zoot.tube.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.Video;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
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

    private void startup() {
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
        System.out.println(delay / 60000);
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
            runnable = new Runnable() {
                Credential hold = credential;
                Task theTask = task;
                List<Video> videos;

                @Override
                public void run() {
                    System.out.println("Started Task Thing!!!!");
                    long now = System.currentTimeMillis();
                    if (now < theTask.getExpire().getTime()) {
                        System.out.println("Setting Videos to be Private");
                        videos = makeVideosInPlaylistPrivate(theTask, hold);

                        // Schedule this task to run again after it expires.
                        long delay = theTask.getExpire().getTime() - now;
                        scheduler.schedule(createRunnableFromTask(theTask), delay, TimeUnit.MILLISECONDS);
                    } else {
                        System.out.println("Setting Videos back.");
                        revertVideosInPlaylist(theTask, hold);
                        System.out.println("Deleting task");
                        TaskIO.deleteTask(theTask);
                    }
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
    private List<Video> makeVideosInPlaylistPrivate(Task task, Credential credential) {
        SimpleYouTubeAPI youTubeAPI = new SimpleYouTubeAPI(credential);
        Playlist playlist = youTubeAPI.getPlaylistByID(task.getRelevantID());
        List<PlaylistItem> playlistItems = youTubeAPI.getPlaylistItemsFromPlaylist(playlist);
        List<Video> videos = new ArrayList<>();
        for (PlaylistItem playlistItem : playlistItems) {
            Video video = youTubeAPI.getVideoByID(playlistItem.getContentDetails().getVideoId());
            video.getSnippet().setThumbnails(null);

            videos.add(video);
        }

//        Video[] videosAsArray = videos.toArray(new Video[0]);
//        VideoArrayWrapper wrapper = new VideoArrayWrapper(videosAsArray);
//        TaskIO.saveContentUnderTask(task, gson.toJson(wrapper));
//        VideoWrapper[] videoWrappers = new VideoWrapper[videos.size()];
//        for (int i = 0; i < videos.size(); i++) {
//            Video video = videos.get(i);
//            videoWrappers[i] = new VideoWrapper(video.getId(), video.getSnippet().getTitle(), video.getSnippet().getCategoryId(), video.getStatus().getPrivacyStatus());
//        }
//        TaskIO.saveContentUnderTask(task, gson.toJson(videos));
        try {
            TaskIO.saveContentUnderTask(task, new ObjectMapper().writeValueAsString(videos));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("Making Videos Private...");
        for (Video video : videos) {
            System.out.println("Setting: " + video.getId());
            String previousStatus = video.getStatus().getPrivacyStatus();
            youTubeAPI.updateVideoVisibility(video, PrivacyStatus.PRIVATE);

            video.getStatus().setPrivacyStatus(previousStatus);
        }

        return videos;
    }

    /**
     * Reverts all Videos in a Playlist to their stored version.
     *
     * @param task the Task to use.
     * @param credential the Credential to use.
     */
    private void revertVideosInPlaylist(Task task, Credential credential) {
        System.out.println("Step 1");
        SimpleYouTubeAPI youTubeAPI = new SimpleYouTubeAPI(credential);
        System.out.println("Step 2");
        String contentAsString = TaskIO.loadContentUnderTask(task);
        System.out.println("Step 3");
//        VideoArrayWrapper wrapper = gson.fromJson(contentAsString, VideoArrayWrapper.class);
//        Video[] videos = wrapper.getVideos();
//        Video[] videos = gson.fromJson(contentAsString, Video[].class);
        Video[] videos = new Video[0];
        try {
            videos = new ObjectMapper().readValue(contentAsString, Video[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        VideoWrapper[] wrappers = gson.fromJson(contentAsString, VideoWrapper[].class);
//        List<Video> videos = new ArrayList<>();
//        for (VideoWrapper wrapper : wrappers) {
//            Video video = new Video();
//            video.setId(wrapper.getId());
//            video.setSnippet(new VideoSnippet().setTitle(wrapper.getTitle()).setCategoryId(wrapper.getCategoryId()));
//            video.setStatus(new VideoStatus().setPrivacyStatus(wrapper.getPrivacyStatus()));
//
//            videos.add(video);
//        }

        System.out.println("Reverting Videos...");
        for (Video video : videos) {
            System.out.println("Reverting: " + video.getId());
            youTubeAPI.updateVideoVisibility(video, video.getStatus().getPrivacyStatus());
        }

        TaskIO.deleteContentUnderTask(task);
    }


}
