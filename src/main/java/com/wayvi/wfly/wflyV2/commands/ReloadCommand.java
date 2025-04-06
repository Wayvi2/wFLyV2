package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.listeners.PvPListener;
import com.wayvi.wfly.wflyV2.managers.WConditionManager;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;

/**
 * Command to reload the plugin configuration files.
 */
public class ReloadCommand extends Command<WFlyV2> {

    private final ConfigUtil configUtil;
    private final WFlyV2 plugin;
    private final PvPListener pvpListener;
    private final WConditionManager conditionManager;

    /**
     * Constructs the ReloadCommand.
     *
     * @param plugin            The main plugin instance.
     * @param configUtil        Utility class for managing configuration files.
     * @param pvPListener       PvP listener to reload its configuration values.
     * @param conditionManager  Manager handling fly conditions.
     */
    public ReloadCommand(WFlyV2 plugin, ConfigUtil configUtil, PvPListener pvPListener, WConditionManager conditionManager) {
        super(plugin, "wfly.reload");
        setDescription("Reload file of the plugin.");
        setUsage("/wfly reload");
        setPermission(Permissions.RELOAD.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.pvpListener = pvPListener;
        this.conditionManager = conditionManager;
    }

    /**
     * Executes the reload command, reloading configuration files and notifying the sender.
     *
     * @param commandSender The sender of the command.
     * @param arguments     The command arguments (not used).
     */
    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        // Reload configurations

        configUtil.reloadCustomConfig();
        conditionManager.loadConditions();
        pvpListener.reloadConfigValues();

        // Log reload message
        String message = configUtil.getCustomMessage().getString("message.reload");
        plugin.getLogger().info("Plugin reloaded");

        // Notify player if applicable
        if (commandSender instanceof Player) {
            ColorSupportUtil.sendColorFormat((Player) commandSender, message);
        }
    }
}
