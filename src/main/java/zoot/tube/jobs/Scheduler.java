/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zoot.tube.jobs;

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
    public Scheduler(Date date, String user, String clientSecretsUrl, Collection<String> scopes, PrivacyStatus newPrivacy){
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
    
}
