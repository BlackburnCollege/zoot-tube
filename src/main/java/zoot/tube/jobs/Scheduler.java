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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * allows you to schedule a task to be ran
 * @author student
 */
public class Scheduler {
    Date desiredDate;
    long delay;
    String user;
    String clientSecretsUrl;
    Collection<String> scopes;
    PrivacyStatus newPrivacy;
    Playlist playlist;
    GoogleAuthJava googleAuthJava;  
    SimpleYouTubeAPI simpleYouTubeAPI;
    private String taskName;
    private String taskPath;
    /**
     * creates a way to store information for the task
     * @param date
     * @param user
     * @param clientSecretsUrl
     * @param scopes
     * @param newPrivacy
     * @param nameOfTask
     * @param playlist 
     */
    public Scheduler(Date date, String user, String clientSecretsUrl, Collection<String> scopes, PrivacyStatus newPrivacy, String nameOfTask, Playlist playlist){
        taskName = nameOfTask;
        googleAuthJava = new GoogleAuthJava(clientSecretsUrl, scopes);
        simpleYouTubeAPI = new SimpleYouTubeAPI(googleAuthJava.authorizeUsingRefreshToken(RefreshTokenSaver.loadRefreshToken(user)));
        desiredDate = date;
        this.playlist = playlist;
        this.user = user;
        this.newPrivacy = newPrivacy;
        Date now = new Date();
        delay = desiredDate.getTime() - now.getTime();
    }
    /**
     * gives the delay for the task to be ran
     * @return - delay in seconds
     */
    public long getDelay(){
        return this.delay;
    }
    /**
     * executes the task
     */
    public void execute(){
        GoogleAuthJava googleAuthJava = new GoogleAuthJava(clientSecretsUrl, scopes);   
        SimpleYouTubeAPI simpleYouTubeAPI = new SimpleYouTubeAPI(googleAuthJava.authorizeUsingRefreshToken(RefreshTokenSaver.loadRefreshToken(user)));
        this.storeVideoPrivacy();
        simpleYouTubeAPI.updatePlaylistVisibility(playlist, newPrivacy);
    }
    /**
     * sets videos and playlist back to previous privacy setting
     */
    public void reExecute(){
        List<PlaylistItem> playlistItem = new ArrayList<>();
        playlistItem = simpleYouTubeAPI.getPlaylistItemsFromPlaylist(playlist);
        Video video;
        PrivacyStatus videoPrivacy;
        for(int i = 0; i > playlistItem.size(); i++){
            video = simpleYouTubeAPI.getVideoByID(playlistItem.get(i).getId());
            videoPrivacy = video.getPrivacyStatus();
        }
    }
    /**
     * stores the current videos privacy setting
     */
    public void storeVideoPrivacy(){
        List<PlaylistItem> playlistItem = new ArrayList<>();
        playlistItem = simpleYouTubeAPI.getPlaylistItemsFromPlaylist(playlist);
        Video video;
        PrivacyStatus videoPrivacy;
        for(int i = 0; i > playlistItem.size(); i++){
            video = simpleYouTubeAPI.getVideoByID(playlistItem.get(i).getId());
            videoPrivacy = video.getPrivacyStatus();
        }
    }
    /**
     * stores the task to be ran
     * @param taskAsJson 
     */
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
