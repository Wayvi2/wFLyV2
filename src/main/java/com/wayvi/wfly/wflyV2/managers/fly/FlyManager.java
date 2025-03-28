package com.wayvi.wfly.wflyV2.managers.fly;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class manages the fly mechanics for players in the server, including enabling/disabling flight,
 * adjusting fly speed, and interacting with the database to store fly status and data.
 */
public class FlyManager {

    static int threads = Runtime.getRuntime().availableProcessors();
    public static ExecutorService service = Executors.newFixedThreadPool(threads);

    private final WFlyV2 plugin;
    private final RequestHelper requestHelper;
    private BukkitTask flyTask;
    private final ConfigUtil configUtil;

    /**
     * Constructs a FlyManager instance.
     *
     * @param plugin The main plugin instance.
     * @param requestHelper The request helper to interact with the database.
     * @param configUtil The configuration utility.
     */
    public FlyManager(WFlyV2 plugin, RequestHelper requestHelper, ConfigUtil configUtil) {
        this.requestHelper = requestHelper;
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    /**
     * Manages the fly state of a player. Enables or disables flight based on the given boolean.
     *
     * @param player The UUID of the player.
     * @param fly True to enable flight, false to disable.
     * @throws SQLException If there is an error accessing the database.
     */
    public void manageFly(UUID player, boolean fly) throws SQLException {
        Player player1 = Bukkit.getPlayer(player);

        if (flyTask != null) {
            flyTask.cancel();
            flyTask = null;
        }

        if (player1 == null) {
            return;
        }

        if (fly) {
            player1.setAllowFlight(true);
            player1.setFlying(true);
            upsertFlyStatus(player1, true);
            plugin.getTimeFlyManager().updateFlyStatus(player1.getUniqueId(), true);
        } else {
            player1.setFlying(false);
            player1.setAllowFlight(false);
            plugin.getTimeFlyManager().updateFlyStatus(player1.getUniqueId(), false);

            upsertFlyStatus(player1, false);
        }
    }

    /**
     * Manages the fly speed of a player by adjusting their speed based on permissions.
     *
     * @param player The player whose fly speed will be adjusted.
     * @param speed The desired fly speed, where 1.0 is the normal speed.
     */
    public void manageFlySpeed(Player player, double speed) {
        speed = speed / 10.0;

        if (speed > 1.0) {
            ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.fly-speed-too-high").replace("%speed%", String.valueOf(speed * 10)));
            return;
        }

        for (int i = (int) (speed * 10); i >= 1; i--) {
            if (player.hasPermission("wfly.fly.speed." + i)) {
                player.setFlySpeed((float) i / 10.0f);
                ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.fly-speed").replace("%speed%", String.valueOf(i)));
                return;
            }
        }

        ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.fly-speed-no-permission").replace("%speed%", String.valueOf(speed * 10)));
    }

    // ACCESS DATABASE METHODS

    /**
     * Retrieves the fly data for a specific player from the database.
     *
     * @param player The UUID of the player.
     * @return The fly data of the player.
     * @throws SQLException If there is an error accessing the database.
     */
    public AccessPlayerDTO getPlayerFlyData(UUID player) throws SQLException {
        List<AccessPlayerDTO> fly = this.requestHelper.select("fly", AccessPlayerDTO.class,
                table -> table.where("uniqueId", player));

        if (fly.isEmpty()) {
            return new AccessPlayerDTO(player, false, 0);
        } else {
            return fly.get(0);
        }
    }

    /**
     * Inserts or updates the fly status of a player in the database.
     *
     * @param player The player whose fly status will be updated.
     * @param isFlying The current flying status of the player.
     */
    public void upsertFlyStatus(Player player, boolean isFlying) {
        service.execute(() -> {
            List<AccessPlayerDTO> existingRecords = this.requestHelper.select("fly", AccessPlayerDTO.class,
                    table -> table.where("uniqueId", player.getUniqueId()));

            if (existingRecords.isEmpty()) {
                this.requestHelper.insert("fly", table -> {
                    table.uuid("uniqueId", player.getUniqueId()).primary();
                    table.bool("isinFly", isFlying);
                    table.bigInt("FlyTimeRemaining", plugin.getTimeFlyManager().getTimeRemaining(player));
                });
            } else {
                this.requestHelper.update("fly", table -> {
                    table.where("uniqueId", player.getUniqueId());
                    table.bool("isinFly", isFlying);
                    table.bigInt("FlyTimeRemaining", plugin.getTimeFlyManager().getTimeRemaining(player));
                });
            }
        });
    }

    /**
     * Creates a new player record in the database with the default fly status.
     *
     * @param player The UUID of the player.
     * @throws SQLException If there is an error accessing the database.
     */
    public void createNewPlayer(UUID player) throws SQLException {
        this.requestHelper.insert("fly", table -> {
            table.uuid("uniqueId", player).primary();
            table.bool("isinFly", false);
            table.bigInt("FlyTimeRemaining", 0);
        });
    }
}
