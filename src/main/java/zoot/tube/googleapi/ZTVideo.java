package zoot.tube.googleapi;

public class ZTVideo {
    public final String name;
    public final String id;

    public ZTVideo(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String toString() {
        return String.format("Video | id: %s | Title: %s", this.id, this.name);
    }
}
