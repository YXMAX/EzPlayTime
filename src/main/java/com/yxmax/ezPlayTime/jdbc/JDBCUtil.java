package com.yxmax.ezPlayTime.jdbc;

import com.yxmax.ezPlayTime.object.TimeCounter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.yxmax.ezPlayTime.EzPlayTime.*;

public class JDBCUtil {

    public static HikariDataSource ds;

    public boolean connectToDatabase() {
        boolean mysql = plugin.getConfig().getBoolean("databases.mysql");
        if(mysql){
            ds = this.getMySQLDataSource();
            if(ds == null){
                return false;
            }
            util.sendConsole("&a连接至 MySQL 数据库成功");
        } else {
            ds = this.getSQLiteDataSource();
            if(ds == null){
                return false;
            }
            util.sendConsole("&a连接至 SQLite 数据库成功");
        }
        this.createTable();
        return true;
    }

    private static Connection batchConnection = null;

    private static PreparedStatement batchPST;

    public HikariDataSource getSQLiteDataSource(){
        HikariConfig config = new HikariConfig();
        String url = System.getProperty("user.dir");
        config.setJdbcUrl("jdbc:sqlite:"+url+"/plugins/EzPlayTime/Database.db");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(50);
        config.setMinimumIdle(25);
        config.setMaxLifetime(1800000);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(5000);
        config.setConnectionTestQuery("SELECT 1");
        config.setLeakDetectionThreshold(2000);
        return new HikariDataSource(config);
    }

    public HikariDataSource getMySQLDataSource() {
        String host = plugin.getConfig().getString("databases.host");
        String port = plugin.getConfig().getString("databases.port");
        String user = plugin.getConfig().getString("databases.user");
        String password = plugin.getConfig().getString("databases.password");
        String database = plugin.getConfig().getString("databases.database");
        int maxPoolSize = plugin.getConfig().getInt("databases.max_pool_size");
        int minIdle = plugin.getConfig().getInt("databases.min_idle");
        boolean public_key = plugin.getConfig().getBoolean("databases.allow_public_key_retrieval");
        boolean ssl = plugin.getConfig().getBoolean("databases.use_ssl");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?allowPublicKeyRetrieval=" + public_key + "&rewriteBatchedStatements=true&useSSL=" + ssl;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setIdleTimeout(30000);
        config.setConnectionTestQuery("SELECT 1");
        HikariDataSource ds = new HikariDataSource(config);
        try{
            Connection connection = ds.getConnection();
            connection.close();
        } catch (SQLException e){
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[EzPlayTime] 连接至 MySQL 数据库失败, 请检查数据库是否启用!");
            e.printStackTrace();
            return null;
        }
        return ds;
    }

    public void createTable(){
        try {
            Connection con = ds.getConnection();
            String sql = "CREATE TABLE IF NOT EXISTS ezplaytime_data"
                    + "("
                    + "uuid VARCHAR(36) UNIQUE,"
                    + "daily INTEGER,"
                    + "week INTEGER,"
                    + "month INTEGER,"
                    + "total BIGINT,"
                    + "last_online TEXT"
                    + ");";
            Statement stat = null;
            stat = con.createStatement();
            stat.executeUpdate(sql);
            stat.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void playerJoin(UUID uuid){
        scheduler.scheduling().asyncScheduler().runDelayed(task -> {
            TimeCounter timeCounter = this.get(uuid.toString());
            timeCounter.check(uuid.toString());
            timeManager.putPlayer(uuid, timeCounter);
        }, Duration.ofMillis(1250));
    }

    public void playerLeave(UUID uuid){
        scheduler.scheduling().asyncScheduler().run(task -> {
            TimeCounter counter = timeManager.getCounter(uuid);
            if(counter == null){
                counter = new TimeCounter();
            }
            counter.setLast_online(util.getCurrentDate());
            this.updateAsync(uuid.toString(),counter);
            timeManager.removeCounter(uuid);
        });
    }

    private void insertAsync(String uuid){
        CompletableFuture.runAsync(() -> {
            try {
                String sql = "insert into ezplaytime_data (uuid, daily, week, month, total, last_online) values(?,?,?,?,?,?)";
                Connection con = ds.getConnection();
                PreparedStatement pst = null;
                pst = con.prepareStatement(sql);
                pst.setString(1, uuid);
                pst.setLong(2, 0);
                pst.setLong(3, 0);
                pst.setLong(4, 0);
                pst.setLong(5, 0);
                pst.setString(6, null);
                pst.executeUpdate();
                pst.close();
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public TimeCounter get(String uuid){
        try {
            Connection con = ds.getConnection();
            String sql = "select daily,week,month,total,last_online from ezplaytime_data where uuid = ?";
            PreparedStatement pst = null;
            pst = con.prepareStatement(sql);
            pst.setString(1, uuid);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                long daily = rs.getLong("daily");
                long week = rs.getLong("week");
                long month = rs.getLong("month");
                long total = rs.getLong("total");
                String last_online = rs.getString("last_online");
                rs.close();
                pst.close();
                con.close();
                return new TimeCounter(daily, week, month, total, last_online);
            }
            this.insertAsync(uuid);
            rs.close();
            pst.close();
            con.close();
            return new TimeCounter();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateAsync(String uuid, TimeCounter counter){
        CompletableFuture.runAsync(() -> {
            try {
                Connection con = ds.getConnection();
                String sql = "update ezplaytime_data set daily=?,week=?,month=?,total=?,last_online=? where uuid=?";
                PreparedStatement pst = null;
                pst = con.prepareStatement(sql);
                pst.setLong(1, counter.getToday());
                pst.setLong(2,counter.getWeek());
                pst.setLong(3,counter.getMonth());
                pst.setLong(4,counter.getTotal());
                pst.setString(5,counter.getLast_online());
                pst.setString(6, uuid);
                pst.executeUpdate();
                pst.close();
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void addBatch(String uuid, TimeCounter counter){
        try {
            batchPST.setLong(1,counter.getToday());
            batchPST.setLong(2,counter.getWeek());
            batchPST.setLong(3,counter.getMonth());
            batchPST.setLong(4,counter.getTotal());
            batchPST.setString(5,counter.getLast_online());
            batchPST.setString(6,uuid);
            batchPST.addBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateCountdown(){
        scheduler.scheduling().asyncScheduler().runDelayed(task -> {
            try {
                batchPST.executeBatch();
                batchConnection.commit();
                batchPST.close();
                batchPST = null;
                batchConnection.close();
                batchConnection = null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        },Duration.ofSeconds(3));
    }

    public void batchUpdate(String uuid, TimeCounter timeCounter){
        try {
            if(batchConnection == null){
                batchConnection = ds.getConnection();
                batchConnection.setAutoCommit(false);
                batchPST = batchConnection.prepareStatement("update ezplaytime_data set daily=?,week=?,month=?,total=?,last_online=? where uuid=?");
                this.addBatch(uuid,timeCounter);
                updateCountdown();
            } else {
                if(batchConnection == null){
                    this.batchUpdate(uuid,timeCounter);
                    return;
                }
                this.addBatch(uuid,timeCounter);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateManager(HashMap<UUID,TimeCounter> map){
        try {
            Connection con = ds.getConnection();
            String sql1 = "update ezplaytime_data set daily=?,week=?,month=?,total=?,last_online=? where uuid=?";
            PreparedStatement pst = null;
            pst = con.prepareStatement(sql1);
            con.setAutoCommit(false);
            for(Map.Entry<UUID,TimeCounter> entry : map.entrySet()){
                TimeCounter counter = entry.getValue();
                pst.setLong(1, counter.getToday());
                pst.setLong(2, counter.getWeek());
                pst.setLong(3, counter.getMonth());
                pst.setLong(4, counter.getTotal());
                pst.setString(5, counter.getLast_online());
                pst.setString(2, entry.getKey().toString());
                pst.addBatch();
            }
            pst.executeBatch();
            con.commit();
            pst.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
