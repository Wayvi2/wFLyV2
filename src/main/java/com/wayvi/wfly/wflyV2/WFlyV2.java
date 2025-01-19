package com.wayvi.wfly.wflyV2;

import com.wayvi.wfly.wflyV2.commands.FlyCommand;
import com.wayvi.wfly.wflyV2.commands.ReloadCommand;
import com.wayvi.wfly.wflyV2.managers.FlyManager;
import com.wayvi.wfly.wflyV2.services.DatabaseService;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.maxlego08.sarah.DatabaseConnection;
import fr.maxlego08.sarah.SqliteConnection;
import fr.maxlego08.sarah.RequestHelper;
import fr.traqueur.commands.api.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;




public final class WFlyV2 extends JavaPlugin {

    @Override
    public void onEnable() {

        //CREATE INSTANCE TO ACCESS DATABASE
        DatabaseConfiguration configuration = DatabaseConfiguration.sqlite(true);
        DatabaseConnection connection = new SqliteConnection(configuration, getDataFolder());

        //INIT DATABASE
        DatabaseService databaseService = new DatabaseService(this);
        databaseService.initializeDatabase();

        //INIT RequestHelper
        RequestHelper requestHelper = new RequestHelper(connection, this.getLogger()::info);

        //INIT FlyManager
        FlyManager flyManager = new FlyManager(this, databaseService, requestHelper);

        // CONFIGS
        ConfigUtil configUtil = new ConfigUtil(this);
        configUtil.createCustomConfig();

        //INIT miniMessageSupport
        MiniMessageSupportUtil miniMessageSupportUtil = new MiniMessageSupportUtil();

        // COMMANDS
        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommand(new ReloadCommand(this, configUtil, miniMessageSupportUtil));
        commandManager.registerCommand(new FlyCommand(this, flyManager, miniMessageSupportUtil, configUtil));

        getLogger().info("Plugin enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled");
    }
}
