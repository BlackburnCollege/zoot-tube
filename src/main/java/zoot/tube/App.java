/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package zoot.tube;

import java.util.List;
import zoot.tube.googleapi.Playlist;
import zoot.tube.googleapi.SimpleYouTubeAPI;
import zoot.tube.googleapi.Video;

public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) throws Exception {
        SimpleYouTubeAPI youtubeAPI = new SimpleYouTubeAPI("junior-zoot");
        List<Playlist> playlists = youtubeAPI.getMyPlaylists();
        System.out.println(playlists);

        for (Playlist playlist : playlists) {
            List<Video> videos = youtubeAPI.getVideosFromPlaylist(playlist.id);
            System.out.println(videos);
        }
    }
}
