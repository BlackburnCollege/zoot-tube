/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zoot.tube.jobs;

import com.google.gson.Gson;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * this schedules the task to be ran at a certain time
 * 
 */
public class SchedulerExecutor {
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    Scheduler scheduler;
    private final String taskAsString;
/**
 * creates a way to execute tasks
 * @param scheduler - holds the information to run the task 
 */
    public SchedulerExecutor(Scheduler scheduler) 
    {
        this.scheduler = scheduler;
        Gson gson = new Gson();
        this.taskAsString = gson.toJson(this.scheduler);
        this.scheduler.storeTask(taskAsString);
    }
/**
 * starts the execution of the task with the correct delay
 */
    public void startExecution()
    {
        Runnable taskWrapper = new Runnable(){

            @Override
            public void run() 
            {
                scheduler.execute();
                startExecution();
            }

        };

        executorService.schedule(taskWrapper, scheduler.getDelay(), TimeUnit.SECONDS);
    }
    /**
     * puts all of the playlist and videos back to what they were after one day
     */
        public void revertBack()
    {
        Runnable taskWrapper = new Runnable(){

            @Override
            public void run() 
            {
                scheduler.reExecute();
                revertBack();
            }

        };

        executorService.schedule(taskWrapper, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }
/**
 * stops the task
 */
    public void stop()
    {
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            Logger.getLogger(SchedulerExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
