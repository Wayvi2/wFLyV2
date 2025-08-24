package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wconfigapi.wconfigapi.ConfigAPI;
import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.listeners.PvPListener;
import com.wayvi.wfly.wflyv2.managers.WConditionManager;
import com.wayvi.wfly.wflyv2.services.DatabaseService;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to reload the plugin configuration files.
 */
public class ReloadCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private final PvPListener pvpListener;
    private final WConditionManager conditionManager;
    private DatabaseService databaseService;

    /**
     * Constructs the ReloadCommand.
     *
     * @param plugin            The main plugin instance.
     * @param pvPListener       PvP listener to reload its configuration values.
     * @param conditionManager  Manager handling fly conditions.
     */
    public ReloadCommand(WFlyV2 plugin, PvPListener pvPListener, WConditionManager conditionManager) {
        super(plugin, "wfly.reload");
        setDescription("Reload file of the plugin.");
        setUsage("/wfly reload");
        setPermission(Permissions.RELOAD.getPermission());
        this.plugin = plugin;
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


        conditionManager.loadConditions();
        plugin.getMessageFile().reload();
        plugin.getConfigFile().reload();



        pvpListener.reloadConfigValues();

        plugin.getDatabaseService().initializeDatabase();

        String message = plugin.getMessageFile().get(MessageEnum.RELOAD);
        plugin.getLogger().info("Plugin reloaded");


        if (commandSender instanceof Player) {
            ColorSupportUtil.sendColorFormat((Player) commandSender, message);
        }
    }
}
