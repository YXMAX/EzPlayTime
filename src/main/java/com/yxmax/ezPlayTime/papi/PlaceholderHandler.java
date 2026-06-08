package com.yxmax.ezPlayTime.papi;

import com.yxmax.ezPlayTime.util.HandleUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.yxmax.ezPlayTime.EzPlayTime.timeManager;

public class PlaceholderHandler extends me.clip.placeholderapi.expansion.PlaceholderExpansion{
    @Override
    public @NotNull String getIdentifier() {
        return "ezplaytime";
    }

    @Override
    public @NotNull String getAuthor() {
        return "YXMAX";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String args) {
        if(args.equals("today_value")){
            return String.valueOf(timeManager.getOfflineCounter(player.getUniqueId()).getToday());
        }
        if(args.equals("today_display")){
            return HandleUtil.parseTime(timeManager.getOfflineCounter(player.getUniqueId()).getToday());
        }
        if(args.equals("week_value")){
            return String.valueOf(timeManager.getOfflineCounter(player.getUniqueId()).getWeek());
        }
        if(args.equals("week_display")){
            return HandleUtil.parseTime(timeManager.getOfflineCounter(player.getUniqueId()).getWeek());
        }
        if(args.equals("month_value")){
            return String.valueOf(timeManager.getOfflineCounter(player.getUniqueId()).getMonth());
        }
        if(args.equals("month_display")){
            return HandleUtil.parseTime(timeManager.getOfflineCounter(player.getUniqueId()).getMonth());
        }
        if(args.equals("total_value")){
            return String.valueOf(timeManager.getOfflineCounter(player.getUniqueId()).getTotal());
        }
        if(args.equals("total_display")){
            return HandleUtil.parseTime(timeManager.getOfflineCounter(player.getUniqueId()).getTotal());
        }
        if(args.equals("last_online")){
            String lastOnline = timeManager.getOfflineCounter(player.getUniqueId()).getLast_online();
            if(lastOnline != null){
                return lastOnline;
            }
            return "无记录";
        }
        return null;
    }
}
