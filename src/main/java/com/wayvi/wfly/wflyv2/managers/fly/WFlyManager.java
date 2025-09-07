package com.wayvi.wfly.wflyv2.managers.fly;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.FlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Fly manager implementation responsible for handling flight permissions,
 * speed, and database storage for players.
 */
public class WFlyManager implements FlyManager {


    private final WFlyV2 plugin;


    /**
     * Constructs a new WFlyManager instance.
     */
    public WFlyManager(WFlyV2 plugin) {
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
        Bukkit.broadcastMessage(String.valueOf(WflyApi.get().getTimeFlyManager().getIsFlying(uuid)));
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

}
