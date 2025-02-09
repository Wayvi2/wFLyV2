package com.wayvi.wfly.wflyV2.bentobox;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.bentobox.listeners.flags.FlyFlagListener;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import world.bentobox.bentobox.api.flags.Flag;

public class FlagsManager {

    private static WFlyV2 plugin;

    public FlagsManager(WFlyV2 plugin) {
        this.plugin = plugin;
        registerFlags();
    }

    public void registerFlags() {
        Flag flyFlag = new Flag.Builder("WFLY_FLY", Material.FEATHER).listener(new FlyFlagListener(plugin)).mode(Flag.Mode.BASIC).build();
    }
}
