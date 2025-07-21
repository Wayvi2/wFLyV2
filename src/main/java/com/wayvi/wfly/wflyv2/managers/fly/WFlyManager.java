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
 * Fly manager implementation responsible for handling flight permissions,
 * speed, and database storage for players.
 */
public class WFlyManager implements FlyManager {

    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREADS);

    private final RequestHelper requestHelper;
    private final ConfigUtil configUtil;

    /**
     * Constructs a new WFlyManager instance.
     *
     * @param requestHelper the database access helper
     * @param configUtil the configuration utility for message and setting retrieval
     */
    public WFlyManager(final RequestHelper requestHelper, final ConfigUtil configUtil) {
        this.requestHelper = requestHelper;
        this.configUtil = configUtil;
    }

    /**
     * Enables or disables flight for a specific player.
     *
     * @param uuid the UUID of the player
     * @param fly whether the player should be allowed to fly
     */
    @Override
    public void manageFly(final UUID uuid, final boolean fly) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        player.setAllowFlight(fly);
        player.setFlying(fly);

        WflyApi.get().getTimeFlyManager().updateFlyStatus(uuid, fly);
        upsertFlyStatus(player, fly);
    }

    /**
     * Sets the fly speed of the player, validating permissions and limits.
     *
     * @param player the player whose fly speed is to be set
     * @param speed the desired fly speed (0–10)
     */
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

    /**
     * Sends a formatted message to the player regarding their fly speed.
     *
     * @param player the player to message
     * @param key the config key for the message
     * @param speed the speed value to inject into the message
     */
    private void sendFlySpeedMessage(final Player player, final String key, final double speed) {
        final String message = configUtil.getCustomMessage().getString(key)
                .replace("%speed%", String.valueOf(speed));
        ColorSupportUtil.sendColorFormat(player, message);
    }

    /**
     * Retrieves flight-related data for a specific player from the database.
     *
     * @param uuid the UUID of the player
     * @return an {@link AccessPlayerDTO} containing player flight data
     * @throws SQLException if the database query fails
     */
    @Override
    public AccessPlayerDTO getPlayerFlyData(final UUID uuid) throws SQLException {
        final List<AccessPlayerDTO> records = requestHelper.select("fly", AccessPlayerDTO.class,
                table -> table.where("uniqueId", uuid));

        return records.isEmpty() ? new AccessPlayerDTO(uuid, false, 0) : records.get(0);
    }

    /**
     * Inserts or updates the player's current fly state and remaining fly time in the database.
     *
     * @param player the player whose data is being updated
     * @param isFlying whether the player is currently flying
     */
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

    /**
     * Creates a new database entry for a player when they are first seen.
     *
     * @param uuid the UUID of the new player
     */
    @Override
    public void createNewPlayer(final UUID uuid) {
        requestHelper.insert("fly", table -> {
            table.uuid("uniqueId", uuid).primary();
            table.bool("isinFly", false);
            table.bigInt("FlyTimeRemaining", 0);
        });
    }
}
