package com.yxmax.ezPlayTime.papi;

import com.yxmax.ezPlayTime.util.HandleUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.yxmax.ezPlayTime.EzPlayTime.timeManager;
import static com.yxmax.ezPlayTime.util.HandleUtil.no_record_online;

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
        switch (args) {
            case "today_value":
                return String.valueOf(timeManager.getOfflineCounter(player.getUniqueId()).getToday());
            case "today_display":
                return HandleUtil.parseTime(timeManager.getOfflineCounter(player.getUniqueId()).getToday());
            case "week_value":
                return String.valueOf(timeManager.getOfflineCounter(player.getUniqueId()).getWeek());
            case "week_display":
                return HandleUtil.parseTime(timeManager.getOfflineCounter(player.getUniqueId()).getWeek());
            case "month_value":
                return String.valueOf(timeManager.getOfflineCounter(player.getUniqueId()).getMonth());
            case "month_display":
                return HandleUtil.parseTime(timeManager.getOfflineCounter(player.getUniqueId()).getMonth());
            case "total_value":
                return String.valueOf(timeManager.getOfflineCounter(player.getUniqueId()).getTotal());
            case "total_display":
                return HandleUtil.parseTime(timeManager.getOfflineCounter(player.getUniqueId()).getTotal());
            case "last_online":
                String lastOnline = timeManager.getOfflineCounter(player.getUniqueId()).getLast_online();
                if (lastOnline != null) {
                    return lastOnline;
                }
                return no_record_online;
        }
        return null;
    }
}
