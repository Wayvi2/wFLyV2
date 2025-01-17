package com.wayvi.wfly.wflyV2;

import org.bukkit.plugin.java.JavaPlugin;
import util.ConfigUtil;

public final class WFlyV2 extends JavaPlugin {

    @Override
    public void onEnable() {

        // CONFIG
        ConfigUtil config = new ConfigUtil(this,"config.yml");
        config.getConfig().set("version", config.getVersion());
        config.save();





        getLogger().info("Plugin enabled");


    }

    @Override
    public void onDisable() {

        getLogger().info("Plugin disabled");


    }
}
