package com.wayvi.wfly.wflyv2.managers.fly;

import com.wayvi.wfly.wflyv2.api.FlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fly manager that handles player fly state, speed, and database interaction.
 */
public class WFlyManager implements FlyManager {

    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREADS);

    private final RequestHelper requestHelper;
    private final ConfigUtil configUtil;

    public WFlyManager(final RequestHelper requestHelper, final ConfigUtil configUtil) {
        this.requestHelper = requestHelper;
        this.configUtil = configUtil;
    }

    @Override
    public void manageFly(final UUID uuid, final boolean fly) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        player.setAllowFlight(fly);
        player.setFlying(fly);

        WflyApi.get().getTimeFlyManager().updateFlyStatus(uuid, fly);
        upsertFlyStatus(player, fly);
    }

    @Override
    public void manageFlySpeed(final Player player, double speed) {
        final double normalizedSpeed = speed / 10.0;

        if (normalizedSpeed > 1.0) {
            sendFlySpeedMessage(player, "message.fly-speed-too-high", speed);
            return;
        }

        for (int i = (int) (normalizedSpeed * 10); i >= 1; i--) {
            if (player.hasPermission("wfly.fly.speed." + i)) {
                player.setFlySpeed(i / 10.0f);
                sendFlySpeedMessage(player, "message.fly-speed", i);
                return;
            }
        }

        sendFlySpeedMessage(player, "message.fly-speed-no-permission", speed);
    }

    private void sendFlySpeedMessage(final Player player, final String key, final double speed) {
        final String message = configUtil.getCustomMessage().getString(key)
                .replace("%speed%", String.valueOf(speed));
        ColorSupportUtil.sendColorFormat(player, message);
    }

    @Override
    public AccessPlayerDTO getPlayerFlyData(final UUID uuid) throws SQLException {
        final List<AccessPlayerDTO> records = requestHelper.select("fly", AccessPlayerDTO.class,
                table -> table.where("uniqueId", uuid));

        return records.isEmpty() ? new AccessPlayerDTO(uuid, false, 0) : records.get(0);
    }

    @Override
    public void upsertFlyStatus(final Player player, final boolean isFlying) {
        EXECUTOR.execute(() -> {
            final UUID uuid = player.getUniqueId();
            final List<AccessPlayerDTO> records = requestHelper.select("fly", AccessPlayerDTO.class,
                    table -> table.where("uniqueId", uuid));

            if (records.isEmpty()) {
                requestHelper.insert("fly", table -> {
                    table.uuid("uniqueId", uuid).primary();
                    table.bool("isinFly", isFlying);
                    table.bigInt("FlyTimeRemaining", WflyApi.get().getTimeFlyManager().getTimeRemaining(player));
                });
            } else {
                requestHelper.update("fly", table -> {
                    table.where("uniqueId", uuid);
                    table.bool("isinFly", isFlying);
                    table.bigInt("FlyTimeRemaining", WflyApi.get().getTimeFlyManager().getTimeRemaining(player));
                });
            }
        });
    }

    @Override
    public void createNewPlayer(final UUID uuid) {
        requestHelper.insert("fly", table -> {
            table.uuid("uniqueId", uuid).primary();
            table.bool("isinFly", false);
            table.bigInt("FlyTimeRemaining", 0);
        });
    }
}
