package zoot.tube.schedule;

public class VideoWrapper {

    private String id;
    private String title;
    private String categoryId;
    private String privacyStatus;

    public VideoWrapper(String id, String title, String categoryId, String privacyStatus) {
        this.id = id;
        this.title = title;
        this.categoryId = categoryId;
        this.privacyStatus = privacyStatus;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getPrivacyStatus() {
        return privacyStatus;
    }
}
