package com.wayvi.wfly.wflyv2;

import com.wayvi.wconfigapi.wconfigapi.ConfigAPI;
import com.wayvi.wfly.wflyv2.api.FlyManager;
import com.wayvi.wfly.wflyv2.api.TimeFlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.api.storage.FlyTimeStorage;
import com.wayvi.wfly.wflyv2.commands.all.RemoveAllTimeFlyCommand;
import com.wayvi.wfly.wflyv2.commands.all.addAllTimeFlyCommand;
import com.wayvi.wfly.wflyv2.commands.converter.TimeUnitsConverter;
import com.wayvi.wfly.wflyv2.commands.converter.ToggleTypeConverter;
import com.wayvi.wfly.wflyv2.commands.items.GiveFlyTokenCommand;
import com.wayvi.wfly.wflyv2.commands.items.GiveFlyTokenToOtherCommand;
import com.wayvi.wfly.wflyv2.commands.other.MigrateTempFlyCommand;
import com.wayvi.wfly.wflyv2.constants.commands.TimeUnits;
import com.wayvi.wfly.wflyv2.constants.commands.ToggleType;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.constants.configs.ItemsEnum;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.handlers.CustomMessageHandler;
import com.wayvi.wfly.wflyv2.listeners.FlyTokenListener;
import com.wayvi.wfly.wflyv2.managers.*;
import com.wayvi.wfly.wflyv2.messaging.BungeeMessenger;
import com.wayvi.wfly.wflyv2.pluginhook.cluescroll.FlyQuest;
import com.wayvi.wfly.wflyv2.commands.*;

import com.wayvi.wfly.wflyv2.listeners.FlyListener;
import com.wayvi.wfly.wflyv2.listeners.PvPListener;
import com.wayvi.wfly.wflyv2.managers.fly.WFlyManager;
import com.wayvi.wfly.wflyv2.managers.fly.WTimeFlyManager;
import com.wayvi.wfly.wflyv2.services.DatabaseService;
import com.wayvi.wfly.wflyv2.storage.FlyTimeStorageFactory;
import com.wayvi.wfly.wflyv2.tempfly.StorageAdapter;
import com.wayvi.wfly.wflyv2.util.*;
import fr.maxlego08.sarah.RequestHelper;

import fr.traqueur.commands.spigot.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public final class WFlyV2 extends JavaPlugin {

    private TimeFormatTranslatorUtil timeFormatTranslatorUtil;
    private boolean isStartup = false;
    private CommandManager<WFlyV2> commandManager;

    private ConfigAPI<ConfigEnum> configFile;
    private ConfigAPI<MessageEnum> messageFile;
    private ConfigAPI<ItemsEnum> itemsFile;

    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREADS);

    private FlyTimeStorage storage;
    private JedisPool jedisPool;
    private ExecutorService executor;

    DatabaseService databaseService;

    private UUID serverId;

    @Override
    public void onEnable() {




        WflyApi.inject(this);

        // INIT METRICS FOR BSTATS
        Metrics metrics = new Metrics(this, 24609);






        // CONFIGS
        configFile = new ConfigAPI<>(this, ConfigEnum.class, "config.yml");
        messageFile = new ConfigAPI<>(this, MessageEnum.class, "message.yml");
        itemsFile = new ConfigAPI<>(this, ItemsEnum.class , "items.yml");
        getConfigFile().set(ConfigEnum.VERSION, this.getDescription().getVersion());




        // INIT DATABASE
        databaseService = new DatabaseService(this);
        databaseService.initializeDatabase();

        // INIT RequestHelper
        RequestHelper requestHelper = new RequestHelper(databaseService.getConnection(), this.getLogger()::info);


        boolean mysqlEnabled = getConfigFile().get(ConfigEnum.MYSQL_ENABLED);
        boolean redisEnabled = getConfigFile().get(ConfigEnum.REDIS_ENABLED);


        this.storage = FlyTimeStorageFactory.create(this, requestHelper, EXECUTOR, mysqlEnabled, redisEnabled);


        PlaceholerapiManager placeholerapiManager = new PlaceholerapiManager(this);
        placeholerapiManager.checkPlaceholderAPI();
        placeholerapiManager.initialize();








        this.timeFormatTranslatorUtil = new TimeFormatTranslatorUtil(this);

        // INIT ExchangeManager
        WExchangeManager exchangeManager = new WExchangeManager(this);
        WflyApi.inject(exchangeManager);

        WConditionManager conditionManager = new WConditionManager(this);
        WflyApi.inject(conditionManager);

        WflyApi.get().getConditionManager().checkCanFly();

        //INIT PvPListener
        PvPListener pvpListener = new PvPListener(this);

        WItemsManager wItemsManager = new WItemsManager(this);




        // INIT FlyManager
        FlyManager flyManager = new WFlyManager(this);
        WflyApi.inject(flyManager);

        // INIT FlyQuest

        if (getServer().getPluginManager().isPluginEnabled("ClueScrolls")) {
            FlyQuest flyQuest = new FlyQuest(this);
            flyQuest.initializeFlyClue();
        } else {
            getLogger().info("ClueScrolls is not enabled: Skipping ClueScrolls integration");
        }

        this.serverId = UUID.randomUUID();


        // INIT TimeFlyManager
        TimeFlyManager timeFlyManager = new WTimeFlyManager(this);
        WflyApi.inject(timeFlyManager);
        try {
            timeFlyManager.decrementTimeRemaining();
            timeFlyManager.saveFlyTimes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        FlyCommand flyCommand = new FlyCommand(this, pvpListener);



        // COMMANDS


        commandManager = new CommandManager<>(this);
        commandManager.setDebug(false);

        commandManager.registerConverter(ToggleType.class, new ToggleTypeConverter());
        commandManager.registerConverter(TimeUnits.class, new TimeUnitsConverter());

        commandManager.registerCommand(new ReloadCommand(this, pvpListener, conditionManager));
        commandManager.registerCommand(flyCommand);
        commandManager.registerCommand(new FlySpeedCommand(this, flyManager));
        commandManager.registerCommand(new AddTimeCommand(this));
        commandManager.registerCommand(new ResetTimeCommand(this));
        commandManager.registerCommand(new RemoveTimeCommand(this));
        commandManager.setMessageHandler(new CustomMessageHandler(this));
        commandManager.registerCommand(new FlyHelpCommand(this));
        commandManager.registerCommand(new FlyPlayerCommands(this, pvpListener));
        commandManager.registerCommand(new addAllTimeFlyCommand(this));
        commandManager.registerCommand(new RemoveAllTimeFlyCommand(this));
        if (getServer().getPluginManager().isPluginEnabled("TempFly")) {
            StorageAdapter storageAdapter = new StorageAdapter(this, requestHelper);
            commandManager.registerCommand(new MigrateTempFlyCommand(this, storageAdapter));
        }
        commandManager.registerCommand(new GetPlayerFlyTimeCommand(this, placeholerapiManager.getPlaceholder()));
        commandManager.registerCommand(new ExchangeCommand(this));
        commandManager.registerCommand(new FlyHelpPlayerCommand(this));
        commandManager.registerCommand(new ToggleFlyPlayerCommand(this, flyCommand));
        commandManager.registerCommand(new AboutCommand(this));
        commandManager.registerCommand(new GiveFlyTokenCommand(this, wItemsManager));
        commandManager.registerCommand(new GiveFlyTokenToOtherCommand(this, wItemsManager));


        // LISTENER
        getServer().getPluginManager().registerEvents(new FlyListener(this, flyManager), this);
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);
        getServer().getPluginManager().registerEvents(new FlyTokenListener(this), this);

        new VersionCheckerUtil(this, 118465).checkAndNotify();

        getLogger().info("Plugin enabled");
        Bukkit.getScheduler().runTaskLater(this, () -> isStartup = true, 40L);


        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeMessenger(this));

    }

    @Override
    public void onDisable() {
        try {
            WflyApi.get().getTimeFlyManager().saveFlyTimeOnDisable().join();

            FlyTimeStorageFactory.saveDisableWithRedisAndMysql(this.getStorage());

            getLogger().info("Fly time saved");
        } catch (CompletionException e) {
            getLogger().severe("" + e.getCause());
        }



        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");




        getLogger().info("Plugin disabled");
    }


    public TimeFormatTranslatorUtil getTimeFormatTranslatorUtil() {
        return this.timeFormatTranslatorUtil;
    }


    //getter config
    public ConfigAPI<ConfigEnum> getConfigFile() {
        return configFile;
    }

    public ConfigAPI<MessageEnum> getMessageFile() {
        return messageFile;
    }

    public ConfigAPI<ItemsEnum> getItemsFile(){ return itemsFile;}

    public UUID getServerId() {
        return serverId;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public FlyTimeStorage getStorage(){
        return storage;
    }

}