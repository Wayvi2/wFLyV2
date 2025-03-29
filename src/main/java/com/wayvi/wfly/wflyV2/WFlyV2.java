package com.wayvi.wfly.wflyV2;

import com.wayvi.wfly.wflyV2.api.WflyApi;
import com.wayvi.wfly.wflyV2.handlers.CustomMessageHandler;
import com.wayvi.wfly.wflyV2.pluginhook.cluescroll.FlyQuest;
import com.wayvi.wfly.wflyV2.commands.*;

import com.wayvi.wfly.wflyV2.listeners.FlyListener;
import com.wayvi.wfly.wflyV2.listeners.PvPListener;
import com.wayvi.wfly.wflyV2.managers.ConditionManager;
import com.wayvi.wfly.wflyV2.managers.fly.FlyManager;
import com.wayvi.wfly.wflyV2.managers.PlaceholerapiManager;
import com.wayvi.wfly.wflyV2.managers.fly.TimeFlyManager;
import com.wayvi.wfly.wflyV2.services.DatabaseService;
import com.wayvi.wfly.wflyV2.util.*;
import fr.maxlego08.sarah.RequestHelper;
import fr.traqueur.commands.api.CommandManager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class WFlyV2 extends JavaPlugin {

    private FlyManager flyManager;

    private TimeFlyManager timeFlyManager;

    private TimeFormatTranslatorUtil timeFormatTranslatorUtil;

    private ConditionManager conditionManager;

    private boolean isStartup = false;


    @Override
    public void onEnable() {


        // INIT METRICS FOR BSTATS
        Metrics metrics = new Metrics(this, 24609);

        // INIT DATABASE
        DatabaseService databaseService = new DatabaseService(this);
        databaseService.initializeDatabase();

        // CONFIGS
        ConfigUtil configUtil = new ConfigUtil(this);
        configUtil.createCustomConfig();

        // INIT miniMessageSupport
        ColorSupportUtil miniMessageSupportUtil = new ColorSupportUtil();

        PlaceholerapiManager placeholerapiManager = new PlaceholerapiManager(this, configUtil);
        placeholerapiManager.checkPlaceholderAPI();
        placeholerapiManager.initialize();

        // INIT RequestHelper
        RequestHelper requestHelper = new RequestHelper(databaseService.getConnection(), this.getLogger()::info);

        this.timeFormatTranslatorUtil = new TimeFormatTranslatorUtil(configUtil);

        // INIT ConditionsManager
        ConditionManager conditionManager = new ConditionManager(this, configUtil);
        conditionManager.checkCanFly();


        //INIT PvPListener
        PvPListener pvpListener = new PvPListener(this, configUtil);

        // INIT FlyManager
        this.flyManager = new FlyManager(this, requestHelper, configUtil);

        // INIT FlyQuest

        if (getServer().getPluginManager().isPluginEnabled("ClueScrolls")) {
            FlyQuest flyQuest = new FlyQuest(this);
            flyQuest.initializeFlyClue();
        } else {
            getLogger().info("ClueScrolls is not enabled: Skipping ClueScrolls integration");
        }




        // INIT TimeFlyManager
        this.timeFlyManager = new TimeFlyManager(this, requestHelper, configUtil);
        try {
            timeFlyManager.decrementTimeRemaining();
            timeFlyManager.saveFlyTimes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // COMMANDS
        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommand(new ReloadCommand(this, configUtil, pvpListener, conditionManager));
        commandManager.registerCommand(new FlyCommand(this, configUtil, conditionManager, pvpListener));
        commandManager.registerCommand(new FlySpeedCommand(this, this.flyManager));
        commandManager.registerCommand(new AddTimeCommand(this, configUtil));
        commandManager.registerCommand(new ResetTimeCommand(this, configUtil));
        commandManager.registerCommand(new RemoveTimeCommand(this, configUtil));
        commandManager.setMessageHandler(new CustomMessageHandler(configUtil));
        commandManager.registerCommand(new FlyHelpCommand(this));

        // LISTENER
        getServer().getPluginManager().registerEvents(new FlyListener(this, flyManager, requestHelper, conditionManager, configUtil), this);
        getServer().getPluginManager().registerEvents(new PvPListener(this, configUtil), this);

        new VersionCheckerUtil(this, 118465).getLatestVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                this.getLogger().info("Plugin is up to date");
            } else {
                this.getLogger().info("Plugin has an update");
            }
        });
        getLogger().info("Plugin enabled");
        Bukkit.getScheduler().runTaskLater(this, () -> isStartup = true, 40L);

        WflyApi.inject(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled");
        timeFlyManager.SaveFlyTimeOnDisable();
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

    public ConditionManager getConditionManager() {
        return this.conditionManager;
    }

    public boolean isStartup() {
        return this.isStartup;
    }

}
