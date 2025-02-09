package com.wayvi.wfly.wflyV2.bentobox.listeners.flags;

import com.wayvi.wfly.wflyV2.WFlyV2;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import world.bentobox.bentobox.api.flags.FlagListener;

public class FlyFlagListener extends FlagListener {

    WFlyV2 plugin;

    public FlyFlagListener(WFlyV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFly(PlayerCommandPreprocessEvent event) {

        if (event.getMessage().equalsIgnoreCase("/fly") || event.getMessage().equalsIgnoreCase("/wfly fly")) {
            event.setCancelled(true);
        }
    }
}
