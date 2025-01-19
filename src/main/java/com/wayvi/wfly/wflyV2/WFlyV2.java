package com.wayvi.wfly.wflyV2;

import com.wayvi.wfly.wflyV2.commands.ReloadCommand;
import com.wayvi.wfly.wflyV2.listeners.ChatListener;
import fr.traqueur.commands.api.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;

public final class WFlyV2 extends JavaPlugin {

    @Override
    public void onEnable() {

        //CONFIGS
        ConfigUtil configUtil = new ConfigUtil(this);
        configUtil.createCustomConfig();

        //COMMANDS
        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommand(new ReloadCommand(this, configUtil));


        //EVENTS
        this.getServer().getPluginManager().registerEvents(new ChatListener(), this);

        getLogger().info("Plugin enabled");
    }


    @Override
    public void onDisable() {

        getLogger().info("Plugin disabled");


    }
}
