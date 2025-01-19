package com.wayvi.wfly.wflyV2;

import com.wayvi.wfly.wflyV2.commands.ReloadCommand;
import fr.traqueur.commands.api.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;

public final class WFlyV2 extends JavaPlugin {

    @Override
    public void onEnable() {

        ConfigUtil configUtil = new ConfigUtil(this);
        configUtil.createCustomConfig();

        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommand(new ReloadCommand(this, configUtil));


        getLogger().info("Plugin enabled");
    }












    @Override
    public void onDisable() {

        getLogger().info("Plugin disabled");


    }
}
