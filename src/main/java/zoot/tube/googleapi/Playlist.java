package zoot.tube.googleapi;

public class Playlist {

    public final String name;
    public final String id;

    public Playlist(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String toString() {
        return String.format("Playlist | Title: %s | id: %s", this.name, this.id);
    }
}
