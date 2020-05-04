package zoot.tube.schedule;

import com.google.api.services.youtube.model.Video;

public class VideoArrayWrapper {

    private Video[] videos;

    public VideoArrayWrapper(Video[] videos) {
        this.videos = videos;
    }

    public Video[] getVideos() {
        return videos;
    }
}
