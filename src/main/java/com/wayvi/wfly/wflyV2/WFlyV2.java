package com.wayvi.wfly.wflyV2;

import com.wayvi.wfly.wflyV2.commands.FlyCommand;
import com.wayvi.wfly.wflyV2.commands.ReloadCommand;
import com.wayvi.wfly.wflyV2.managers.FlyManager;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.traqueur.commands.api.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;

public final class WFlyV2 extends JavaPlugin {

    @Override
    public void onEnable() {

        MiniMessageSupportUtil miniMessageSupportUtil = new MiniMessageSupportUtil();

        FlyManager flyManager = new FlyManager();


        //CONFIGS
        ConfigUtil configUtil = new ConfigUtil(this);
        configUtil.createCustomConfig();

        //COMMANDS
        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommand(new ReloadCommand(this, configUtil, miniMessageSupportUtil));
        commandManager.registerCommand(new FlyCommand(this, flyManager));


        //EVENTS


        getLogger().info("Plugin enabled");
    }


    @Override
    public void onDisable() {

        getLogger().info("Plugin disabled");


    }
}
