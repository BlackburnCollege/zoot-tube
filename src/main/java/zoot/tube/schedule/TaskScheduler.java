package zoot.tube.schedule;

import com.google.api.client.auth.oauth2.Credential;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import zoot.tube.googleapi.GoogleAuthJava;
import zoot.tube.googleapi.RefreshTokenSaver;
import zoot.tube.googleapi.SimpleYouTubeAPI;
import zoot.tube.googleapi.YouTubeAPI;

public class TaskScheduler {

    private ScheduledExecutorService scheduler;
    private GoogleAuthJava authenticator;

    public TaskScheduler(String clientSecretsURL, Collection<String> scopes) {
        this.authenticator = new GoogleAuthJava(clientSecretsURL, scopes);
    }

    public void startup() {
        scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
    }



    public void makeVideosInPlaylistPrivate(String user, Date start, Date expire, String playlistID) {
        Task task = new Task(user, TaskType.MAKE_VIDEOS_IN_PLAYLIST_PRIVATE, start, expire, playlistID);
    }

    private void loadSavedTasks() {
        List<Task> savedTasks = TaskIO.loadSavedTasks();
        for (Task task : savedTasks) {

        }
    }

    private void loadTask(Task task) {
        long delay = task.getStart().getTime() - System.currentTimeMillis();
        Runnable runnable = createRunnableFromTask(task);
        scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    private Runnable createRunnableFromTask(Task task) {
        return () -> {
            String refreshToken = RefreshTokenSaver.loadRefreshToken(task.getUser());
            Credential credential = authenticator.authorizeUsingRefreshToken(refreshToken);

            if (task.getTaskType().equals(TaskType.MAKE_VIDEOS_IN_PLAYLIST_PRIVATE)) {
                makeVideosInPlaylistPrivate(task.getRelevantID(), credential);
            }
        };
    }

    private void makeVideosInPlaylistPrivate(String playlistID, Credential credential) {
        SimpleYouTubeAPI youTubeAPI = new SimpleYouTubeAPI(credential);

    }


}
