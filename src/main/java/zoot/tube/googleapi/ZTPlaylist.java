package zoot.tube.googleapi;

public class ZTPlaylist {

    public final String title;
    public final String description;
    public final String id;

    public ZTPlaylist(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String toString() {
        return String.format("Playlist | id: %s | Title: %s | Description: %s", this.id, this.title, this.description);
    }
}
