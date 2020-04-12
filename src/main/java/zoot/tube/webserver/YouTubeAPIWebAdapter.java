package zoot.tube.webserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.List;
import zoot.tube.googleapi.YouTubeAPI;
import zoot.tube.googleapi.ZTPlaylist;

public class YouTubeAPIWebAdapter {


    private YouTubeAPI youtubeAPI;

    public YouTubeAPIWebAdapter(YouTubeAPI youtubeAPI) {
        this.youtubeAPI = youtubeAPI;
    }

    public JsonObject getMyPlaylists() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<ZTPlaylist> playlists = youtubeAPI.getMyPlaylists();
        ZTPlaylist[] playlistsArray = playlists.toArray(new ZTPlaylist[0]);
        String playlistsJson = gson.toJson(playlistsArray);
        JsonObject object = new JsonObject();
        object.addProperty("data", playlistsJson);
        System.out.println(object.toString());
        return object;
    }

    public String getMyPlaylistsAsJsonString() {
        List<ZTPlaylist> playlists = this.youtubeAPI.getMyPlaylists();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"data\":[");
        for (int i = 0; i < playlists.size() - 1; i++) {
            sb.append(playlists.get(i).toJsonString()).append(",");
        }
        if (playlists.size() > 0) {
            sb.append(playlists.get(playlists.size() - 1).toJsonString());
        }
        sb.append("]}");

        return sb.toString();
    }
}
