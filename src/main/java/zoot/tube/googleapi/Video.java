package zoot.tube.googleapi;

public class Video {
    public final String name;
    public final String id;

    public Video(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String toString() {
        return String.format("Video | Title: %s | id: %s", this.name, this.id);
    }
}
