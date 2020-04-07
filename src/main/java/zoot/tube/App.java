/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package zoot.tube;

import java.util.List;
import zoot.tube.googleapi.ZTPlaylist;
import zoot.tube.googleapi.SimpleYouTubeAPI;

public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) throws Exception {
        SimpleYouTubeAPI youtubeAPI = new SimpleYouTubeAPI("junior-zoot");
        List<ZTPlaylist> playlists = youtubeAPI.getMyPlaylists();
        System.out.println(playlists);

        for (ZTPlaylist playlist : playlists) {
//            youtubeAPI.setPlaylistVisibility(
//                    playlist.id,
//                    playlist.title,
//                    playlist.description,
//                    SimpleYouTubeAPI.PrivacyStatus.UNLISTED
//            );
            System.out.println(youtubeAPI.getVideosFromPlaylist(playlist.id));
        }
    }
}
