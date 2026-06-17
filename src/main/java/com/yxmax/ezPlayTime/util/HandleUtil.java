package com.yxmax.ezPlayTime.util;

import com.yxmax.ezPlayTime.papi.PlaceholderHandler;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.yxmax.ezPlayTime.EzPlayTime.*;
import static org.bukkit.Bukkit.getServer;

public class HandleUtil {

    public static boolean isUp118 = false;

    public static String minute_prefix;

    public static String hour_prefix;

    public static String no_record_online;

    public void detectServerVersion() {
        String v = Bukkit.getBukkitVersion().split("-")[0];
        String[] split = v.split("\\.");
        if(!split[0].equals("1")){
            isUp118 = true;
            return;
        }
        switch (v.split("\\.")[1]) {
            case "18":
            case "19":
            case "20":
            case "21":
                isUp118 = true;
                return;
            default:
                break;
        }
    }

    public boolean initPlaceholderAPI(){
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHandler().register();
            return true;
        }
        if(isChineseLanguage){
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[EzPlayTime] 未检测到 PlaceholderAPI 前置插件! 插件将关闭..");
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[EzPlayTime] Detecting PlaceholderAPI dependency failed! plugin will be disabled!");
        }
        return false;
    }

    public void loadPluginConfig(boolean boot) {
        if(boot) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            recreateConfig();
        }
        plugin.reloadConfig();
        minute_prefix = plugin.getConfig().getString("time_display.minutes");
        hour_prefix = plugin.getConfig().getString("time_display.hours");
        no_record_online = plugin.getConfig().getString("last_online.no_record");
    }

    private void recreateConfig(){
        configFile.getParentFile().mkdirs();
        Locale server_locale = Locale.getDefault();
        String language = server_locale.toLanguageTag();
        if(language.equals("zh-CN")){
            isChineseLanguage = true;
        }
        try {
            FileUtils.copyInputStreamToFile(plugin.getResource("config.yml"), new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Date parseToDate(String date){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            return df.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurrentDate(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return df.format(new Date());
    }

    public long getNextMinuteRemaining(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return (calendar.getTimeInMillis() - System.currentTimeMillis());
    }

    public long getNextHalfMinuteRemaining(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 30);
        calendar.set(Calendar.MILLISECOND, 0);
        return (calendar.getTimeInMillis() - System.currentTimeMillis());
    }

    public long getNextDay(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static String parseTime(long minutes){
        long hours = minutes / 60;
        long minute = minutes % 60;
        return hours + hour_prefix + " " + minute + minute_prefix;
    }

    public void hotLoad(){
        if(Bukkit.getOnlinePlayers().isEmpty()){
            return;
        }
        this.sendConsole("&e热加载服务端内玩家数据..","&eHot loading player's data...");
        for(Player player : Bukkit.getOnlinePlayers()){
            jdbcUtil.playerJoin(player.getUniqueId());
        }
    }

    public void sendConsole(String cn,String us){
        if(isChineseLanguage){
            getServer().getConsoleSender().sendMessage(color("&f[EzPlayTime] " + cn));
        } else {
            getServer().getConsoleSender().sendMessage(color("&f[EzPlayTime] " + us));
        }
    }

    public static String color(String msg){
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
