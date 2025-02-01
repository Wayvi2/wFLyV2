package com.wayvi.wfly.wflyV2;

import com.wayvi.wfly.wflyV2.commands.*;
import com.wayvi.wfly.wflyV2.listeners.PlayerJoinListener;
import com.wayvi.wfly.wflyV2.listeners.PlayerLeaveListener;
import com.wayvi.wfly.wflyV2.managers.ConditionManager;
import com.wayvi.wfly.wflyV2.managers.fly.FlyManager;
import com.wayvi.wfly.wflyV2.managers.PlaceholerapiManager;
import com.wayvi.wfly.wflyV2.managers.fly.TimeFlyManager;
import com.wayvi.wfly.wflyV2.services.DatabaseService;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import com.wayvi.wfly.wflyV2.util.TimeFormatTranslatorUtil;
import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.maxlego08.sarah.DatabaseConnection;
import fr.maxlego08.sarah.SqliteConnection;
import fr.maxlego08.sarah.RequestHelper;
import fr.traqueur.commands.api.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;

import java.sql.SQLException;

public final class WFlyV2 extends JavaPlugin {

    private FlyManager flyManager;

    private TimeFlyManager timeFlyManager;

    private TimeFormatTranslatorUtil timeFormatTranslatorUtil;

    @Override
    public void onEnable() {


        //INIT DATABASE
        DatabaseService databaseService = new DatabaseService(this);
        databaseService.initializeDatabase();

        // CONFIGS
        ConfigUtil configUtil = new ConfigUtil(this);
        configUtil.createCustomConfig();

        //INIT miniMessageSupport
        MiniMessageSupportUtil miniMessageSupportUtil = new MiniMessageSupportUtil();

        PlaceholerapiManager placeholerapiManager = new PlaceholerapiManager(this, configUtil);
        placeholerapiManager.checkPlaceholderAPI();
        placeholerapiManager.initialize();


        //INIT RequestHelper
        RequestHelper requestHelper = new RequestHelper(databaseService.getConnection(), this.getLogger()::info);

        this.timeFormatTranslatorUtil = new TimeFormatTranslatorUtil(configUtil);

        ConditionManager conditionWorldManager = new ConditionManager(this, configUtil, requestHelper);
        conditionWorldManager.checkCanFly();


        //INIT FlyManager
        this.flyManager = new FlyManager(this, requestHelper, configUtil);

        //INIT TimeFlyManager
        this.timeFlyManager = new TimeFlyManager(this, requestHelper, configUtil);
        try {
            timeFlyManager.decrementTimeRemaining();
            timeFlyManager.saveFlyTimes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        // COMMANDS
        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommand(new ReloadCommand(this, configUtil));
        commandManager.registerCommand(new FlyCommand(this, configUtil, conditionWorldManager));
        commandManager.registerCommand(new FlySpeedCommand(this, this.flyManager));
        commandManager.registerCommand(new AddTimeCommand(this, configUtil));
        commandManager.registerCommand(new ResetTimeCommand(this, configUtil));
        commandManager.registerCommand(new RemoveTimeCommand(this, configUtil));

        //LISTENER
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this.flyManager), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(this), this);


        getLogger().info("Plugin enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled");
    }

    public TimeFlyManager getTimeFlyManager() {
        return timeFlyManager;
    }

    public FlyManager getFlyManager() {
        return flyManager;
    }

    public TimeFormatTranslatorUtil getTimeFormatTranslatorUtil() {
        return timeFormatTranslatorUtil;
    }
}
