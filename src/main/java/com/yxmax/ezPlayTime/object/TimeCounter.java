package com.yxmax.ezPlayTime.object;

import java.util.Calendar;

import static com.yxmax.ezPlayTime.EzPlayTime.jdbcUtil;
import static com.yxmax.ezPlayTime.EzPlayTime.util;

public class TimeCounter{

    private long today;

    private long week;

    private long month;

    private long total;

    private String last_online;

    public TimeCounter() {
        this.today = 0;
        this.week = 0;
        this.month = 0;
        this.total = 0;
        this.last_online = null;
    }

    public TimeCounter(long today, long week, long month, long total, String last_online) {
        this.today = today;
        this.week = week;
        this.month = month;
        this.total = total;
        this.last_online = last_online;
    }

    public void check(String uuid){
        if(last_online == null){
            return;
        }
        boolean need_update = false;
        Calendar current = Calendar.getInstance();
        Calendar last = Calendar.getInstance();
        last.setTime(util.parseToDate(last_online));
        int current_week = current.get(Calendar.WEEK_OF_YEAR);
        int current_month = current.get(Calendar.MONTH);
        int current_day = current.get(Calendar.DAY_OF_YEAR);
        int last_week = last.get(Calendar.WEEK_OF_YEAR);
        int last_month = last.get(Calendar.MONTH);
        int last_day = last.get(Calendar.DAY_OF_YEAR);
        if(last_day != current_day){
            need_update = true;
            this.today = 0;
        }
        if(last_week != current_week){
            need_update = true;
            this.week = 0;
        }
        if(last_month != current_month){
            need_update = true;
            this.month = 0;
        }
        if(need_update){
            jdbcUtil.updateAsync(uuid,this);
        }
    }

    public void newDayCheck(String uuid){
        Calendar current = Calendar.getInstance();
        Calendar before = Calendar.getInstance();
        before.add(Calendar.SECOND, -30);
        int current_week = current.get(Calendar.WEEK_OF_YEAR);
        int current_month = current.get(Calendar.MONTH);
        int before_week = before.get(Calendar.WEEK_OF_YEAR);
        int before_month = before.get(Calendar.MONTH);
        if(before_week != current_week){
            this.week = 0;
        }
        if(before_month != current_month){
            this.month = 0;
        }
        jdbcUtil.batchUpdate(uuid,this);
    }

    public long getToday() {
        return today;
    }

    public long getWeek() {
        return week;
    }

    public long getMonth() {
        return month;
    }

    public long getTotal() {
        return total;
    }

    public String getLast_online() {
        return last_online;
    }

    public void setToday(long today) {
        this.today = today;
    }

    public void setWeek(long week) {
        this.week = week;
    }

    public void setMonth(long month) {
        this.month = month;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setLast_online(String last_online) {
        this.last_online = last_online;
    }

    public void add(){
        today = today + 1;
        week = week + 1;
        month = month + 1;
        total = total + 1;
    }

    @Override
    public String toString() {
        return "TimeCounter{" +
                "today=" + today +
                ", week=" + week +
                ", month=" + month +
                ", total=" + total +
                ", last_online='" + last_online + '\'' +
                '}';
    }
}
