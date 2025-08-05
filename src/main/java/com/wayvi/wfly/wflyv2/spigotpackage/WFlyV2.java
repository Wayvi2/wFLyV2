package com.wayvi.wfly.wflyv2.spigotpackage;

import com.wayvi.wfly.wflyv2.spigotpackage.api.FlyManager;
import com.wayvi.wfly.wflyv2.spigotpackage.api.TimeFlyManager;
import com.wayvi.wfly.wflyv2.spigotpackage.api.WflyApi;
import com.wayvi.wfly.wflyv2.spigotpackage.api.bungeecordhook.ProxyCommandManager;
import com.wayvi.wfly.wflyv2.spigotpackage.commands.all.addAllTimeCommand;
import com.wayvi.wfly.wflyv2.spigotpackage.commands.all.RemoveAllTimeFlyCommand;
import com.wayvi.wfly.wflyv2.spigotpackage.commands.all.addAllTimeCommand;
import com.wayvi.wfly.wflyv2.spigotpackage.commands.other.MigrateTempFlyCommand;
import com.wayvi.wfly.wflyv2.spigotpackage.handlers.CustomMessageHandler;
import com.wayvi.wfly.wflyv2.spigotpackage.pluginhook.cluescroll.FlyQuest;
import com.wayvi.wfly.wflyv2.spigotpackage.commands.*;

import com.wayvi.wfly.wflyv2.spigotpackage.listeners.FlyListener;
import com.wayvi.wfly.wflyv2.spigotpackage.listeners.PvPListener;
import com.wayvi.wfly.wflyv2.spigotpackage.managers.WConditionManager;
import com.wayvi.wfly.wflyv2.spigotpackage.managers.fly.WFlyManager;
import com.wayvi.wfly.wflyv2.spigotpackage.managers.PlaceholerapiManager;
import com.wayvi.wfly.wflyv2.spigotpackage.managers.fly.WTimeFlyManager;
import com.wayvi.wfly.wflyv2.spigotpackage.services.DatabaseService;
import com.wayvi.wfly.wflyv2.spigotpackage.tempfly.StorageAdapter;
import com.wayvi.wfly.wflyv2.spigotpackage.util.*;
import fr.maxlego08.sarah.RequestHelper;

import fr.traqueur.commands.spigot.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.concurrent.CompletionException;

public final class WFlyV2 extends JavaPlugin  {

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

        // INIT ConditionsManager
        WConditionManager conditionManager = new WConditionManager(configUtil);
        conditionManager.checkCanFly();
        WflyApi.inject(conditionManager);

        //INIT PvPListener
        PvPListener pvpListener = new PvPListener(configUtil);

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
        TimeFlyManager timeFlyManager = new WTimeFlyManager(requestHelper, configUtil);
        WflyApi.inject(timeFlyManager);
        try {
            timeFlyManager.decrementTimeRemaining();
            timeFlyManager.saveFlyTimes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ProxyCommandManager proxyCommandManager = new ProxyCommandManager(this);

        // COMMANDS
        commandManager = new CommandManager<>(this);
        commandManager.setDebug(false);
        commandManager.registerCommand(new ReloadCommand(this, configUtil, pvpListener, conditionManager));
        commandManager.registerCommand(new FlyCommand(this, configUtil, pvpListener));
        commandManager.registerCommand(new FlySpeedCommand(this, flyManager));
        commandManager.registerCommand(new AddTimeCommand(this, configUtil));
        commandManager.registerCommand(new ResetTimeCommand(this, configUtil));
        commandManager.registerCommand(new RemoveTimeCommand(this, configUtil));
        commandManager.setMessageHandler(new CustomMessageHandler(configUtil));
        commandManager.registerCommand(new FlyHelpCommand(this));
        commandManager.registerCommand(new FlyPlayerCommands(this, configUtil, pvpListener));
        commandManager.registerCommand(new addAllTimeCommand(this, configUtil, proxyCommandManager));
        commandManager.registerCommand(new RemoveAllTimeFlyCommand(this, configUtil));
        if (getServer().getPluginManager().isPluginEnabled("TempFly")) {
            StorageAdapter storageAdapter = new StorageAdapter(this, requestHelper);
            commandManager.registerCommand(new MigrateTempFlyCommand(this, storageAdapter));
        }
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

        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);

        getLogger().info("Plugin disabled");
    }


    public TimeFormatTranslatorUtil getTimeFormatTranslatorUtil() {
        return this.timeFormatTranslatorUtil;
    }


    public void executeMyMethod(int time) {
        WflyApi.get().getTimeFlyManager().addFlytimeForAllPlayers(time);
    }

}


