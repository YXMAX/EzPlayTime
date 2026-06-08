package com.yxmax.ezPlayTime;

import com.google.common.base.Charsets;
import com.yxmax.ezPlayTime.jdbc.JDBCUtil;
import com.yxmax.ezPlayTime.listener.PlayerListener;
import com.yxmax.ezPlayTime.manager.TimeManager;
import com.yxmax.ezPlayTime.papi.PlaceholderHandler;
import com.yxmax.ezPlayTime.scheduler.ResetOfflineScheduler;
import com.yxmax.ezPlayTime.scheduler.TodayScheduler;
import com.yxmax.ezPlayTime.util.HandleUtil;
import com.yxmax.ezPlayTime.yaml.CommentYamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import space.arim.morepaperlib.MorePaperLib;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.yxmax.ezPlayTime.util.HandleUtil.isUp118;

public final class EzPlayTime extends JavaPlugin {

    public static EzPlayTime plugin;

    public static MorePaperLib scheduler;

    public static HandleUtil util = new HandleUtil();

    public static TimeManager timeManager = new TimeManager();

    public static JDBCUtil jdbcUtil = new JDBCUtil();

    public static TodayScheduler todayScheduler = new TodayScheduler();

    public static boolean isClosed;

    public static File configFile;

    public static FileConfiguration config;

    @Override
    public void onEnable() {
        plugin = this;
        util.detectServerVersion();
        util.loadPluginConfig(true);

        if(!util.initPlaceholderAPI()){
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if(!jdbcUtil.connectToDatabase()){
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        plugin.getServer().getPluginManager().registerEvents((Listener)new PlayerListener(), (Plugin)plugin);
        scheduler = new MorePaperLib(this);
        todayScheduler.runCounter();
        todayScheduler.runUpdater();
        todayScheduler.runNextDayRefresh();
        ResetOfflineScheduler.start();
        util.hotLoad();
    }

    @Override
    public void onDisable() {
        plugin = null;
        isClosed = true;
        timeManager.unload();
    }

    @Override
    public void reloadConfig() {
        config = new YamlConfiguration();
        if(isUp118){
            config = YamlConfiguration.loadConfiguration(configFile);
            InputStream defConfigStream = this.getResource("config.yml");
            if (defConfigStream != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
            }
        } else {
            config = CommentYamlConfiguration.loadConfiguration(configFile);
            InputStream defConfigStream = this.getResource("config.yml");
            if(defConfigStream != null) {
                config.setDefaults(CommentYamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
            }
        }
    }

    @Override
    public FileConfiguration getConfig(){
        if(config == null){
            this.reloadConfig();
        }
        return config;
    }
}
