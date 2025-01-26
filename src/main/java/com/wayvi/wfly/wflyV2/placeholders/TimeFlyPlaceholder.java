package com.wayvi.wfly.wflyV2.placeholders;

import com.wayvi.wfly.wflyV2.WFlyV2;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public class TimeFlyPlaceholder extends PlaceholderExpansion {

    private final WFlyV2 plugin;

    public TimeFlyPlaceholder(WFlyV2 plugin) {
        this.plugin = plugin;
}

    @Override
    public @NotNull String getIdentifier() {
        return "wfly";
    }

    @Override
    public @NotNull String getAuthor() {
        return "wPlugin";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            Player player = offlinePlayer.getPlayer();

            if (params.equals("fly_remaining")) {
                try {
                    return String.valueOf(plugin.getTimeFlyManager().getTimeRemaining(player));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        return null;
    }

}
