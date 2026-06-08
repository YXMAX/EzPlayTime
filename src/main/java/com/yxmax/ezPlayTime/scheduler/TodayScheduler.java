package com.yxmax.ezPlayTime.scheduler;

import java.time.Duration;

import static com.yxmax.ezPlayTime.EzPlayTime.*;

public class TodayScheduler {

    public void runCounter(){
        scheduler.scheduling().asyncScheduler().runAtFixedRate(task -> {
            if(isClosed){
                task.cancel();
                return;
            }
            timeManager.counterAdd();
        }, Duration.ofMillis(util.getNextMinuteRemaining()),Duration.ofMinutes(1));
    }

    public void runUpdater(){
        scheduler.scheduling().asyncScheduler().runAtFixedRate(task -> {
            if(isClosed){
                task.cancel();
                return;
            }
            timeManager.updateToMySQL();
        }, Duration.ofMillis(util.getNextHalfMinuteRemaining()),Duration.ofMinutes(10));
    }

    public void runNextDayRefresh(){
        scheduler.scheduling().asyncScheduler().runDelayed(task -> {
            if(isClosed){
                task.cancel();
                return;
            }
            if(timeManager.isRunning()){
                this.tryToRefresh();
                return;
            }
            timeManager.newDayCheck();
        },Duration.ofMillis(util.getNextDay() - System.currentTimeMillis() + 200));
    }

    private void tryToRefresh(){
        scheduler.scheduling().asyncScheduler().runDelayed(task -> {
            if(timeManager.isRunning()){
                this.tryToRefresh();
                return;
            }
            timeManager.newDayCheck();
        },Duration.ofMillis(100));
    }
}
