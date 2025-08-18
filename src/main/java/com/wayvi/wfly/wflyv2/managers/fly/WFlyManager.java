package com.wayvi.wfly.wflyv2.managers.fly;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.FlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
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


    private WFlyV2 plugin;
    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREADS);

    private final RequestHelper requestHelper;

    /**
     * Constructs a new WFlyManager instance.
     *
     * @param requestHelper the database access helper
     */
    public WFlyManager(WFlyV2 plugin,final RequestHelper requestHelper) {
        this.requestHelper = requestHelper;
        this.plugin = plugin;
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
    /**
     * Sets the fly speed of the player, validating permissions and limits.
     *
     * @param player the player whose fly speed is to be set
     * @param speed the desired fly speed (1–10)
     */
    @Override
    public void manageFlySpeed(final Player player, double speed) {

        int maxAllowedSpeed = 0;
        for (int i = 1; i <= 10; i++) {
            if (player.hasPermission("wfly.fly.speed." + i)) {
                maxAllowedSpeed = i;
            }
        }

        if (maxAllowedSpeed == 0) {
            String message = plugin.getMessageFile().get(MessageEnum.FLY_SPEED_NO_PERMISSION);
            ColorSupportUtil.sendColorFormat(player, message.replace("%speed%", String.valueOf((int) speed)));
            return;
        }

        if (speed < 1) speed = 1;
        if (speed > maxAllowedSpeed) {
            String message = plugin.getMessageFile().get(MessageEnum.FLY_SPEED_TOO_HIGH);
            ColorSupportUtil.sendColorFormat(player, message.replace("%speed%", String.valueOf(maxAllowedSpeed)));
            return;
        }

        int requestedSpeed = (int) speed;
        player.setFlySpeed(requestedSpeed / 10.0f);

        String message = plugin.getMessageFile().get(MessageEnum.FLY_SPEED);
        ColorSupportUtil.sendColorFormat(player, message.replace("%speed%", String.valueOf(requestedSpeed)));
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
