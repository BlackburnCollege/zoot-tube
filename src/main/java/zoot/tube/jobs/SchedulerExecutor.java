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
 *
 * @author 
 */
public class SchedulerExecutor {
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    Scheduler scheduler;
    private final String taskAsString;

    public SchedulerExecutor(Scheduler scheduler) 
    {
        this.scheduler = scheduler;
        Gson gson = new Gson();
        this.taskAsString = gson.toJson(this.scheduler);
        this.scheduler.storeTask(taskAsString);
    }

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
