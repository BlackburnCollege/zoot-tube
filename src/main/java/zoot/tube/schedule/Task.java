package zoot.tube.schedule;

import java.util.Date;

public class Task {

    private String user;
    private String taskType;
    private Date start;
    private Date expire;
    private String relevantID;

    public Task(String user, String taskType, Date start, Date expire, String relevantID) {
        this.user = user;
        this. taskType = taskType;
        this.start = start;
        this.expire = expire;
        this.relevantID = relevantID;
    }

    public String getUser() {
        return user;
    }

    public String getTaskType() {
        return taskType;
    }

    public Date getStart() {
        return start;
    }

    public Date getExpire() {
        return expire;
    }

    public String getRelevantID() {
        return relevantID;
    }
}
