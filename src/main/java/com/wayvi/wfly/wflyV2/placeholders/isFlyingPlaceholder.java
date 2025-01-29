package com.wayvi.wfly.wflyV2.placeholders;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.UUID;

public class isFlyingPlaceholder extends PlaceholderExpansion {

    private WFlyV2 plugin;

    public isFlyingPlaceholder(WFlyV2 plugin) {
        this.plugin = plugin;
    }

        @Override
        public @NotNull String getIdentifier () {
            return "wfly";
        }

        @Override
        public @NotNull String getAuthor () {
            return "wPlugin";
        }

        @Override
        public @NotNull String getVersion () {
            return "1.0.0";
        }

        @Override
        public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params){
            if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                UUID player = offlinePlayer.getUniqueId();

                if (params.equals("fly_activate")) {
                    try {
                        AccessPlayerDTO isFlying = plugin.getFlyManager().getPlayerFlyData(player);
                        return String.valueOf(isFlying.isinFly());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return null;
        }
    }

