/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zoot.tube.jobs;

import static java.time.ZonedDateTime.now;
import java.util.Date;


/**
 *
 * @author student
 */
public class Scheduler {
    Date desiredDate;
    long delay;
    public Scheduler(Date date){
        desiredDate = date;
        Date now = new Date();
        delay = desiredDate.getTime() - now.getTime();
    }
    
    public long getDelay(){
        return this.delay;
    }
    
    public void execute(){
        
    }
    
}
