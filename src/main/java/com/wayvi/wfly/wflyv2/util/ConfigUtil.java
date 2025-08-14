package com.wayvi.wfly.wflyv2.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Utility class to handle the loading, saving, and managing of configuration files
 * (config.yml and message.yml) for the plugin.
 * This class ensures that the necessary configuration files exist, creates them if missing,
 * and provides methods to access and modify the configurations.
 */
public class ConfigUtil {

    //message.yml
    private File messageFile;
    private FileConfiguration messageConfig;

    //config.yml
    private File configFile;
    private FileConfiguration configConfig;

    private String version = "1.0.2.6";
    private final Plugin plugin;

    /**
     * Constructor that initializes the ConfigUtil class and ensures the custom config files are created.
     *
     * @param plugin the plugin instance that owns this configuration utility
     */
    public ConfigUtil(Plugin plugin) {
        this.plugin = plugin;
        createCustomConfig();
        checkAndAddMissingLines();
    }

    /**
     * Creates the custom configuration files (message.yml and config.yml) if they do not exist.
     * It also loads the configurations into memory for use.
     */
    public void createCustomConfig() {
        //message.yml
        messageFile = new File(plugin.getDataFolder(), "message.yml");
        ifNotExistCreateCustomConfig(messageFile, "message.yml");

        //config.yml
        configFile = new File(plugin.getDataFolder(), "config.yml");
        ifNotExistCreateCustomConfig(configFile, "config.yml");

        messageConfig = new YamlConfiguration();
        configConfig = new YamlConfiguration();

        reloadCustomConfig();
    }

    /**
     * Checks if the given configuration file exists. If not, it creates the necessary directories
     * and saves the resource to the disk.
     *
     * @param file the configuration file to check
     * @param name the name of the configuration file to save if it doesn't exist
     */
    public void ifNotExistCreateCustomConfig(File file, String name) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(name, false);
        }
    }

    /**
     * Saves the current state of the message and config configurations to disk.
     * This method ensures any changes made to the configurations are persisted.
     */
    public void saveCustomConfig() {
        try {
            messageConfig.save(messageFile);
            configConfig.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reloads the message and config configurations from their respective files.
     * This is useful when you want to refresh the configuration data during runtime.
     */
    public void reloadCustomConfig() {
        try {
            messageConfig.load(messageFile);
            configConfig.load(configFile);
            checkAndAddMissingLines();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if certain configuration values are missing in the config.yml file. If missing,
     * it adds default values for those keys.
     */
    public void checkAndAddMissingLines() {

        boolean changed = false;

        // Delay

        if (!configConfig.contains("fly-decrement-disabled-by-static")) {
            configConfig.set("fly-decrement-disabled-by-static", false);
            changed = true;
        }

        if (!configConfig.contains("delay")) {
            configConfig.set("delay", 3);
            changed = true;
        }
        // Version
        if (!configConfig.contains("version")) {
            configConfig.set("version", version);
            changed = true;
        }

        // Save database delay
        if (!configConfig.contains("save-database-delay")) {
            configConfig.set("save-database-delay", 60);
            changed = true;
        }

        // Aliases
        if (!configConfig.contains("command.alias")) {
            configConfig.set("command.alias", new ArrayList<String>());
            changed = true;
        }


        // Fly decrement method
        if (!configConfig.contains("fly-decrement-method")) {
            configConfig.set("fly-decrement-method", "PLAYER_FLY_MODE");
            changed = true;
        }

        // Format placeholder
        if (!configConfig.contains("format-placeholder.seconds")) {
            configConfig.set("format-placeholder.seconds", true);
            changed = true;
        }
        if (!configConfig.contains("format-placeholder.minutes")) {
            configConfig.set("format-placeholder.minutes", false);
            changed = true;
        }
        if (!configConfig.contains("format-placeholder.hours")) {
            configConfig.set("format-placeholder.hours", true);
            changed = true;
        }
        if (!configConfig.contains("format-placeholder.days")) {
            configConfig.set("format-placeholder.days", true);
            changed = true;
        }
        if (!configConfig.contains("format-placeholder.unlimited")) {
            configConfig.set("format-placeholder.unlimited", "Unlimited");
            changed = true;
        }
        if (!configConfig.contains("format-placeholder.auto-format")) {
            configConfig.set("format-placeholder.auto-format", true);
            changed = true;
        }
        if (!configConfig.contains("format-placeholder.remove-null-values.enabled")) {
            configConfig.set("format-placeholder.remove-null-values.enabled", true);
            changed = true;
        }
        if (!configConfig.contains("format-placeholder.remove-null-values.value")) {
            configConfig.set("format-placeholder.remove-null-values.value", "#FFC77A0seconds");
            changed = true;
        }
        if (!configConfig.contains("format-placeholder.format")) {
            configConfig.set("format-placeholder.format", "#FFC77A%seconds%#FF9D00%seconds_suffixe%#FFC77A%minutes%#FF9D00%minutes_suffixe% #FFC77A%hours%#FF9D00%hours_suffixe% #FFC77A%days%#FF9D00%days_suffixe%");
            changed = true;
        }
        if (!configConfig.contains("format-placeholder.other-format.seconds_suffixe")) {
            configConfig.set("format-placeholder.other-format.seconds_suffixe", "seconds");
            changed = true;
        }
        if (!configConfig.contains("format-placeholder.other-format.minutes_suffixe")) {
            configConfig.set("format-placeholder.other-format.minutes_suffixe", "minutes");
            changed = true;
        }
        if (!configConfig.contains("format-placeholder.other-format.hours_suffixe")) {
            configConfig.set("format-placeholder.other-format.hours_suffixe", "hours");
            changed = true;
        }
        if (!configConfig.contains("format-placeholder.other-format.days_suffixe")) {
            configConfig.set("format-placeholder.other-format.days_suffixe", "days");
            changed = true;
        }

        // Conditions
        if (!configConfig.contains("conditions")) {
            configConfig.createSection("conditions");
            changed = true;
        }


        // Teleport on floor when fly disabled
        if (!configConfig.contains("tp-on-floor-when-fly-disabled")) {
            configConfig.set("tp-on-floor-when-fly-disabled", true);
            changed = true;
        }

        // PvP Settings
        if (!configConfig.contains("pvp.enabled-permission-range")) {
            configConfig.set("pvp.enabled-permission-range", false);
            changed = true;
        }

        if (!configConfig.contains("mysql.enabled")) {
            configConfig.set("mysql.enabled", false);
            changed = true;
        }
        if (!configConfig.contains("mysql.host")) {
            configConfig.set("mysql.host", "localhost");
            changed = true;
        }
        if (!configConfig.contains("mysql.port")) {
            configConfig.set("mysql.port", 3306);
            changed = true;
        }
        if (!configConfig.contains("mysql.database")) {
            configConfig.set("mysql.database", "wfly");
            changed = true;
        }
        if (!configConfig.contains("mysql.username")) {
            configConfig.set("mysql.username", "root");
            changed = true;
        }
        if (!configConfig.contains("mysql.password")) {
            configConfig.set("mysql.password", "root");
            changed = true;
        }


        if (!configConfig.contains("pvp.fly-disable-radius")) {
            configConfig.set("pvp.fly-disable-radius", 5);
            changed = true;
        }
        if (!configConfig.contains("pvp.bypass.placeholders")) {
            configConfig.set("pvp.bypass.placeholders", Collections.singletonList("%lands_land_name_plain%"));
            changed = true;
        }

        // Messages
        if (!messageConfig.contains("message.reload")) {
            messageConfig.set("message.reload", "&cPlugin has been reloaded!");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-activated")) {
            messageConfig.set("message.fly-activated", "&aYou have been set to fly! Use /fly to disable fly.");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-deactivated")) {
            messageConfig.set("message.fly-deactivated", "&cYou have been set to walk! Use /fly to enable fly.");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-speed-too-high")) {
            messageConfig.set("message.fly-speed-too-high", "&cSpeed too high! Maximum speed is %speed%");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-speed")) {
            messageConfig.set("message.fly-speed", "&aYou have set your fly speed to &e%speed%");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-speed-no-permission")) {
            messageConfig.set("message.fly-speed-no-permission", "&cYou do not have permission to set your fly speed to &e%speed%");
            changed = true;
        }
        if (!messageConfig.contains("message.no-timefly-remaining")) {
            messageConfig.set("message.no-timefly-remaining", "&cYou have no timefly remaining!");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-time-added")) {
            messageConfig.set("message.fly-time-added", "&aYou have been given &e%time% &atimefly!");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-time-added-to-player")) {
            messageConfig.set("message.fly-time-added-to-player", "&aYou have been given &e%time% &atimefly to &e%player%");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-time-remove-to-player")) {
            messageConfig.set("message.fly-time-remove-to-player", "&aYou have removed &e%time% &atimefly from &e%player%");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-time-reset-to-player")) {
            messageConfig.set("message.fly-time-reset-to-player", "&aYou have been given reset &atimefly to &e%player%");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-time-removed")) {
            messageConfig.set("message.fly-time-removed", "&cYou have been taken &e%time% &ctimefly!");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-time-reset")) {
            messageConfig.set("message.fly-time-reset", "&aYou have been given &e0 &atimefly!");
            changed = true;
        }
        if (!messageConfig.contains("message.no-fly-here")) {
            messageConfig.set("message.no-fly-here", "&cYou cannot fly here!");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-remove-too-high")) {
            messageConfig.set("message.fly-remove-too-high", "&cYou cannot remove too much timefly!");
            changed = true;
        }
        if (!messageConfig.contains("message.no-permission")) {
            messageConfig.set("message.no-permission", "&cYou do not have permission to use this command!");
            changed = true;
        }
        if (!messageConfig.contains("message.only-in-game")) {
            messageConfig.set("message.only-in-game", "&cThis command can only be used in game!");
            changed = true;
        }
        if (!messageConfig.contains("message.missing-args")) {
            messageConfig.set("message.missing-args", "&cMissing arguments!");
            changed = true;
        }
        if (!messageConfig.contains("message.arg-not-recognized")) {
            messageConfig.set("message.arg-not-recognized", "&cArgument not recognized!");
            changed = true;
        }
        if (!messageConfig.contains("message.message-requirement")) {
            messageConfig.set("message.message-requirement", "&cYou do not meet the requirements to use this command!");
            changed = true;
        }
        if (!messageConfig.contains("message.player-in-range")) {
            messageConfig.set("message.player-in-range", "&cPlayer is too far away!");
            changed = true;
        }
        if (!messageConfig.contains("message.no-spectator")) {
            messageConfig.set("message.no-spectator", "&cYou can deactivate fly in spectator mode!");
            changed = true;
        }

        if (!messageConfig.contains("message.fly-time-added-to-all-player")) {
            messageConfig.set("message.fly-time-added-to-all-player", "&aYou have been given &e%time% &atimefly to all players!");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-time-removed-to-all-player")) {
            messageConfig.set("message.fly-time-removed-to-all-player", "&aYou have removed &e%time% &atimefly from all players!");
            changed = true;
        }
        if (!messageConfig.contains("message.fly-time-reset-to-all-player")) {
            messageConfig.set("message.fly-time-reset-to-all-player", "&aYou have been given reset &atimefly to all players!");
            changed = true;
        }
        if (!messageConfig.contains("message.get-player-fly-time")) {
            messageConfig.set("message.get-player-fly-time", "&a%player% have %fly_remaining%");
            changed = true;
        }
        if (!messageConfig.contains("message.exchange-receiver")) {
            messageConfig.set("message.exchange-receiver", "&a%donator% give you &e%time%");
            changed = true;
        }
        if (!messageConfig.contains("message.exchange-donator")) {
            messageConfig.set("message.exchange-donator", "&aYou have been given %time%s to %receiver%");
            changed = true;
        }
        if (!messageConfig.contains("message.exchange-cannot-the-same")) {
            messageConfig.set("message.exchange-cannot-the-same", "&cYou cannot exchange time fly with you!");
            changed = true;
        }
        if (!messageConfig.contains("message.only-get-his-fly-time")) {
            messageConfig.set("message.only-get-his-fly-time", "&cYou cannot get other time fly than you.");
            changed = true;
        }
        if (!messageConfig.contains("message.have-your-fly-time")) {
            messageConfig.set("message.have-your-fly-time", "&aYou have %fly_remaining%");
            changed = true;
        }


        if (!configConfig.contains("decrementation-disable-by-condition")) {
            Map<String, Object> conditionMap = new HashMap<>();
            conditionMap.put("condition", "%player_name%=Wayvi2");
            configConfig.set("decrementation-disable-by-condition", Collections.singletonList(conditionMap));
            changed = true;
        }


        if (!messageConfig.contains("message.exchange-time-out")) {
            messageConfig.set("message.exchange-time-out", "&cYou cannot give too much!");
            changed = true;
        }

        if (!messageConfig.contains("message.fly-activated-player")) {
            messageConfig.set("message.fly-activated-player", "&aYou have activated fly for %player%");
            changed = true;
        }

        if (!messageConfig.contains("message.fly-deactivated-player")) {
            messageConfig.set("message.fly-deactivated-player", "&cYou have deactivated fly for %player%");
            changed = true;
        }

        if (!messageConfig.contains("message.player-not-in-fly")) {
            messageConfig.set("message.player-not-in-fly", "&cThe %player% is not in fly!");
            changed = true;
        }

        if (!messageConfig.contains("message.cannot-add-time-unlimited")) {
            messageConfig.set("message.cannot-add-time-unlimited", "&cYou cannot add time to an unlimited player!");
            changed = true;
        }



        if (!messageConfig.contains("message.exchange-time-zero")) {
            messageConfig.set("message.exchange-time-zero", "&cYou cannot give zero and negative fly time!");
            changed = true;
        }

        if (!messageConfig.contains("message.help-message-player")) {
            messageConfig.set("message.help-message-player", Arrays.asList(
                    "&8&m──────&7 » &b&lw&bFlight &7Player &7« &8&m──────",
                    " &8| &b/fly",
                    " &8| &7Toggle between flight statuses.",
                    "&r",
                    " &8| &b/flyspeed <number>",
                    " &8| &7Toggle between flight statuses.",
                    "&r",
                    " &8| &b/flytime",
                    " &8| &7Check your remaining flight time.",
                    "&r",
                    " &8| &b/flytime <player>",
                    " &8| &7Check another player's remaining flight time.",
                    "&r",
                    " &8| &b/fly give <player> <amount in seconds>",
                    " &8| &7Give a player flight time out of your remaining flight time.",
                    "&r"
            ));
            changed = true;
        }

        if (!messageConfig.contains("message.help-message-admin")) {
            messageConfig.set("message.help-message-admin", Arrays.asList(
                    "&8&m──────&7 » &b&lw&bFlight &7Admin &7« &8&m──────",
                    " &8| &b/wfly addtime <player> <amount>",
                    " &8| &7Add flight time, in seconds, to a player.",
                    "&r",
                    " &8| &b/wfly removetime <player> <amount>",
                    " &8| &7Remove flight time, in seconds, from a player.",
                    "&r",
                    " &8| &b/wfly reset <player>",
                    " &8| &7Sets a player's flight time to 0 seconds.",
                    "&r",
                    " &8| &b/wfly addall <amount>",
                    " &8| &7Add flight time to all players.",
                    "&r",
                    " &8| &b/wfly removeall <amount>",
                    " &8| &7Remove flight time from all players.",
                    "&r",
                    " &8| &b/wfly reload",
                    " &8| &7Reloads the plugins configurations.",
                    "&r"
            ));
            changed = true;
        }

        if (!configConfig.contains("cooldown-give.enabled")) {
            configConfig.set("cooldown-give.enabled", false);
            changed = true;
        }

        if (!configConfig.contains("cooldown-give.custom-cooldown.enabled")) {
            configConfig.set("cooldown-give.custom-cooldown.enabled", false);
            changed = true;
        }

        if (!configConfig.contains("cooldown-give.custom-cooldown.cooldown")) {
            configConfig.set("cooldown-give.custom-cooldown.cooldown", 5);
            changed = true;
        }

        if (!configConfig.contains("cooldown-give.limits.enabled")) {
            configConfig.set("cooldown-give.limits.enabled", true);
            changed = true;
        }

        if (!configConfig.contains("cooldown-give.limits.give-minimum-value")) {
            configConfig.set("cooldown-give.limits.give-minimum-value", 5);
            changed = true;
        }

        if (!configConfig.contains("cooldown-give.limits.give-maximum-value")) {
            configConfig.set("cooldown-give.limits.give-maximum-value", 60);
            changed = true;
        }

        if (!messageConfig.contains("message.cooldown-give")) {
            messageConfig.set("message.cooldown-give", "&cYou must wait %seconds% before give fly again!");
            changed = true;
        }

        if (!messageConfig.contains("message.exchange-time-below-minimum")) {
            messageConfig.set("message.exchange-time-below-minimum", "&cYou must give at least %min% seconds of fly time.");
            changed = true;
        }

        if (!messageConfig.contains("message.exchange-time-above-maximum")) {
            messageConfig.set("message.exchange-time-above-maximum", "&cYou cannot give more than %max% seconds of fly time.");
            changed = true;
        }

        if (changed){
            saveCustomConfig();
        }
    }

    /**
     * Gets the version of the plugin.
     *
     * @return the version string of the plugin
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the custom config.yml file configuration.
     *
     * @return the custom config.yml file configuration
     */
    public FileConfiguration getCustomConfig() {
        return this.configConfig;
    }

    /**
     * Gets the custom message.yml file configuration.
     *
     * @return the custom message.yml file configuration
     */
    public FileConfiguration getCustomMessage() {
        return this.messageConfig;
    }
}
