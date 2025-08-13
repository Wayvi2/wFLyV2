package com.wayvi.wfly.wflyv2;

import com.wayvi.wfly.wflyv2.api.FlyManager;
import com.wayvi.wfly.wflyv2.api.TimeFlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.commands.all.RemoveAllTimeFlyCommand;
import com.wayvi.wfly.wflyv2.commands.all.addAllTimeFlyCommand;
import com.wayvi.wfly.wflyv2.commands.converter.ToggleTypeConverter;
import com.wayvi.wfly.wflyv2.commands.other.MigrateTempFlyCommand;
import com.wayvi.wfly.wflyv2.constants.ToggleType;
import com.wayvi.wfly.wflyv2.handlers.CustomMessageHandler;
import com.wayvi.wfly.wflyv2.managers.WExchangeManager;
import com.wayvi.wfly.wflyv2.pluginhook.cluescroll.FlyQuest;
import com.wayvi.wfly.wflyv2.commands.*;

import com.wayvi.wfly.wflyv2.listeners.FlyListener;
import com.wayvi.wfly.wflyv2.listeners.PvPListener;
import com.wayvi.wfly.wflyv2.managers.WConditionManager;
import com.wayvi.wfly.wflyv2.managers.fly.WFlyManager;
import com.wayvi.wfly.wflyv2.managers.PlaceholerapiManager;
import com.wayvi.wfly.wflyv2.managers.fly.WTimeFlyManager;
import com.wayvi.wfly.wflyv2.services.DatabaseService;
import com.wayvi.wfly.wflyv2.tempfly.StorageAdapter;
import com.wayvi.wfly.wflyv2.util.*;
import fr.maxlego08.sarah.RequestHelper;

import fr.traqueur.commands.spigot.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

public final class WFlyV2 extends JavaPlugin {

    private TimeFormatTranslatorUtil timeFormatTranslatorUtil;
    private boolean isStartup = false;
    private CommandManager<WFlyV2> commandManager;

    @Override
    public void onEnable() {

        WflyApi.inject(this);

        // INIT METRICS FOR BSTATS
        Metrics metrics = new Metrics(this, 24609);

        // CONFIGS
        ConfigUtil configUtil = new ConfigUtil(this);
        configUtil.createCustomConfig();

        // INIT DATABASE
        DatabaseService databaseService = new DatabaseService(this, configUtil);
        databaseService.initializeDatabase();



        // INIT miniMessageSupport
        ColorSupportUtil miniMessageSupportUtil = new ColorSupportUtil();

        PlaceholerapiManager placeholerapiManager = new PlaceholerapiManager(this, configUtil);
        placeholerapiManager.checkPlaceholderAPI();
        placeholerapiManager.initialize();

        // INIT RequestHelper
        RequestHelper requestHelper = new RequestHelper(databaseService.getConnection(), this.getLogger()::info);

        this.timeFormatTranslatorUtil = new TimeFormatTranslatorUtil(configUtil);

        // INIT ExchangeManager
        WExchangeManager exchangeManager = new WExchangeManager(this, configUtil);
        WflyApi.inject(exchangeManager);

        // INIT ConditionsManager
        WConditionManager conditionManager = new WConditionManager(configUtil);
        conditionManager.checkCanFly();
        WflyApi.inject(conditionManager);

        //INIT PvPListener
        PvPListener pvpListener = new PvPListener( configUtil);

        // INIT FlyManager
        FlyManager flyManager = new WFlyManager(requestHelper, configUtil);
        WflyApi.inject(flyManager);

        // INIT FlyQuest

        if (getServer().getPluginManager().isPluginEnabled("ClueScrolls")) {
            FlyQuest flyQuest = new FlyQuest(this);
            flyQuest.initializeFlyClue();
        } else {
            getLogger().info("ClueScrolls is not enabled: Skipping ClueScrolls integration");
        }

        // INIT TimeFlyManager
        TimeFlyManager  timeFlyManager = new WTimeFlyManager(requestHelper, configUtil);
        WflyApi.inject(timeFlyManager);
        try {
            timeFlyManager.decrementTimeRemaining();
            timeFlyManager.saveFlyTimes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        FlyCommand flyCommand = new FlyCommand(this, configUtil, pvpListener);

        // COMMANDS



        commandManager = new CommandManager<>(this);
        commandManager.setDebug(false);

        commandManager.registerConverter(ToggleType.class, new ToggleTypeConverter());

        commandManager.registerCommand(new ReloadCommand(this, configUtil, pvpListener, conditionManager));
        commandManager.registerCommand(flyCommand);
        commandManager.registerCommand(new FlySpeedCommand(this, flyManager));
        commandManager.registerCommand(new AddTimeCommand(this, configUtil));
        commandManager.registerCommand(new ResetTimeCommand(this, configUtil));
        commandManager.registerCommand(new RemoveTimeCommand(this, configUtil));
        commandManager.setMessageHandler(new CustomMessageHandler(configUtil));
        commandManager.registerCommand(new FlyHelpCommand(this, configUtil));
        commandManager.registerCommand(new FlyPlayerCommands(this,configUtil, pvpListener));
        commandManager.registerCommand(new addAllTimeFlyCommand(this,configUtil));
        commandManager.registerCommand(new RemoveAllTimeFlyCommand(this,configUtil));
        if (getServer().getPluginManager().isPluginEnabled("TempFly")) {
            StorageAdapter storageAdapter = new StorageAdapter(this, requestHelper);
            commandManager.registerCommand(new MigrateTempFlyCommand(this, storageAdapter));
        }
        commandManager.registerCommand(new GetPlayerFlyTimeCommand(this, configUtil, placeholerapiManager.getPlaceholder()));
        commandManager.registerCommand(new ExchangeCommand(this, configUtil));
        commandManager.registerCommand(new FlyHelpPlayerCommand(this, configUtil));
        commandManager.registerCommand(new ToggleFlyPlayerCommand(this,configUtil, flyCommand));

        // LISTENER
        getServer().getPluginManager().registerEvents(new FlyListener(this, flyManager, configUtil), this);
        getServer().getPluginManager().registerEvents(new PvPListener(configUtil), this);

        new VersionCheckerUtil(this, 118465).checkAndNotify();

        getLogger().info("Plugin enabled");
        Bukkit.getScheduler().runTaskLater(this, () -> isStartup = true, 40L);
    }

    @Override
    public void onDisable() {
        try {
            WflyApi.get().getTimeFlyManager().saveFlyTimeOnDisable().join();
            getLogger().info("Fly time saved");
        } catch (CompletionException e) {
            getLogger().severe("" + e.getCause());
        }
        getLogger().info("Plugin disabled");
    }

    public boolean isStartup() {
        return this.isStartup;
    }
    public void setStartup(boolean startup) {
        this.isStartup = startup;
    }

    public TimeFormatTranslatorUtil getTimeFormatTranslatorUtil() {
        return this.timeFormatTranslatorUtil;
    }
}
