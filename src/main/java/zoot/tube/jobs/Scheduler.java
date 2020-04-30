/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zoot.tube.jobs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import zoot.tube.googleapi.*;
import static java.time.ZonedDateTime.now;
import java.util.Collection;
import java.util.Date;


/**
 *
 * @author student
 */
public class Scheduler {
    Date desiredDate;
    long delay;
    String user;
    String clientSecretsUrl;
    Collection<String> scopes;
    PrivacyStatus newPrivacy;
    private String taskName;
    private String taskPath;
    
    public Scheduler(Date date, String user, String clientSecretsUrl, Collection<String> scopes, PrivacyStatus newPrivacy, String nameOfTask){
        taskName = nameOfTask;
        desiredDate = date;
        this.user = user;
        this.newPrivacy = newPrivacy;
        Date now = new Date();
        delay = desiredDate.getTime() - now.getTime();
    }
    
    public long getDelay(){
        return this.delay;
    }
    
    public void execute(){
        GoogleAuthJava googleAuthJava = new GoogleAuthJava(clientSecretsUrl, scopes);   
        SimpleYouTubeAPI simpleYouTubeAPI = new SimpleYouTubeAPI(googleAuthJava.authorizeUsingRefreshToken(RefreshTokenSaver.loadRefreshToken(user)));
        String playlist = simpleYouTubeAPI.getMyPlaylists();
        simpleYouTubeAPI.updatePlaylistVisibility(playlist, newPrivacy);
    }
    
    public void storeTask(String taskAsJson) {

        String userDir = System.getProperty("user.dir");
        String fileDir = userDir + "src/main/resources/jobs/schedule"
                + this.taskName + ".json";
        File taskFile = new File(fileDir);
        try {
            taskFile.createNewFile();
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file.");
        }
        this.taskPath = taskFile.toPath().toString();
        try{
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.taskPath, true));
        writer.append(taskAsJson);
        writer.close();
        } catch (IOException e){
            System.out.println("An error occured while writing to the file.");
        }
    }
    
}
