package zoot.tube.schedule;

public class VideoIdHolder {

    private String id;
    private String privacyStatus;

    public VideoIdHolder(String id, String privacyStatus) {
        this.id = id;
        this.privacyStatus = privacyStatus;
    }

    public String getId() {
        return id;
    }

    public String getPrivacyStatus() {
        return privacyStatus;
    }
}
