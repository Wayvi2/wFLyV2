package com.wayvi.wfly.wflyv2.managers.fly;

import com.wayvi.wfly.wflyv2.ActionBar;
import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.FlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fly manager implementation responsible for handling flight permissions,
 * speed, and database storage for players.
 */
public class WFlyManager implements FlyManager {


    private final WFlyV2 plugin;


    private final Map<UUID, BukkitTask> playerTasks = new HashMap<>();

    private boolean actionBarEnabled;
    private String actionBarMessage;


    /**
     * Constructs a new WFlyManager instance.
     */
    public WFlyManager(WFlyV2 plugin) {

        this.plugin = plugin;
        loadActionBarConfiguration();
        FlyActionBar();
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


        final boolean couldFly = player.getAllowFlight();
        final boolean isFlying = player.isFlying();

        player.setAllowFlight(fly);

        if (fly) {
            if (!couldFly || isFlying) {
                player.setFlying(true);
            }
        } else {
            player.setFlying(false);
        }

        WflyApi.get().getTimeFlyManager().updateFlyStatus(uuid, fly);
    }

    /**
     * Sets the fly speed of the player, validating permissions and limits.
     *
     * @param player the player whose fly speed is to be set
     * @param speed the desired fly speed (0–10)
     */
    @Override
    public void manageFlySpeed(final Player player, double speed) {

        int maxAllowedSpeed = 0;
        for (int i = 1; i <= 10; i++) {
            if (player.hasPermission("wfly.fly.speed." + i)) maxAllowedSpeed = i;
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


    @Override
    public void loadActionBarConfiguration() {
        this.actionBarEnabled = plugin.getConfigFile().get(ConfigEnum.SHOW_FLYTIME_ACTIONBAR_ENABLED);
        this.actionBarMessage = plugin.getConfigFile().get(ConfigEnum.SHOW_FLYTIME_ACTIONBAR_MESSAGE);
    }


    @Override
    public void FlyActionBar() {
        if (!actionBarEnabled){
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!WflyApi.get().getTimeFlyManager().getIsFlying(player.getUniqueId())) {
                continue;
            }
            String msg = PlaceholderAPI.setPlaceholders(player, actionBarMessage);
            ActionBar.sendActionBar(player, msg);
        }
    }
}


