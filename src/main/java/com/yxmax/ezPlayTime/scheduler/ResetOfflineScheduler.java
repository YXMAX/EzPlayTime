package com.yxmax.ezPlayTime.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.yxmax.ezPlayTime.EzPlayTime.timeManager;

public class ResetOfflineScheduler implements Runnable {

    public static void start(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(
                new ResetOfflineScheduler(),
                0,
                10,
                TimeUnit.MINUTES);
    }


    @Override
    public void run() {
        timeManager.offlineTemp.clear();
    }
}
