package com.yxmax.ezPlayTime.manager;

import com.yxmax.ezPlayTime.object.TimeCounter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yxmax.ezPlayTime.EzPlayTime.jdbcUtil;
import static com.yxmax.ezPlayTime.EzPlayTime.util;

public class TimeManager {

    private boolean isRunning = false;

    private HashMap<UUID, TimeCounter> timeCounters = new HashMap<>();

    public HashMap<UUID, TimeCounter> offlineTemp = new HashMap<>();

    public void putPlayer(UUID uuid,TimeCounter timeCounter) {
        timeCounters.put(uuid, timeCounter);
    }

    public TimeCounter getCounter(UUID uuid) {
        return timeCounters.get(uuid);
    }

    public void removeCounter(UUID uuid) {
        timeCounters.remove(uuid);
    }

    public TimeCounter getOfflineCounter(UUID uuid) {
        if(timeCounters.containsKey(uuid)) {
            return timeCounters.get(uuid);
        }
        if(offlineTemp.containsKey(uuid)) {
            return offlineTemp.get(uuid);
        }
        TimeCounter timeCounter = jdbcUtil.get(uuid.toString());
        offlineTemp.put(uuid, timeCounter);
        return timeCounter;
    }

    public void counterAdd(){
        this.isRunning = true;
        for (TimeCounter timeCounter : timeCounters.values()) {
            if(timeCounter == null) continue;
            timeCounter.add();
        }
        this.isRunning = false;
    }

    public void updateToMySQL(){
        jdbcUtil.updateManager(this.timeCounters);
    }

    public void unload(){
        util.sendConsole("&e卸载前保存在线玩家数据..","Saving online player's data...");
        jdbcUtil.updateManager(this.timeCounters);
    }

    public void newDayCheck(){
        for(Map.Entry<UUID, TimeCounter> entry : timeCounters.entrySet()){
            UUID uuid = entry.getKey();
            TimeCounter timeCounter = entry.getValue();
            timeCounter.newDayCheck(uuid.toString());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
