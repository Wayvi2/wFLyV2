package com.wayvi.wfly.wflyv2.spigotpackage.commands.all;

import com.wayvi.wfly.wflyv2.spigotpackage.WFlyV2;
import com.wayvi.wfly.wflyv2.spigotpackage.api.WflyApi;
import com.wayvi.wfly.wflyv2.spigotpackage.api.bungeecordhook.ProxyCommandManager;
import com.wayvi.wfly.wflyv2.spigotpackage.constants.Permissions;
import com.wayvi.wfly.wflyv2.spigotpackage.util.ColorSupportUtil;
import com.wayvi.wfly.wflyv2.spigotpackage.util.ConfigUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class addAllTimeCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private ConfigUtil configUtil;
    private ProxyCommandManager proxyCommandManager;

    /**
     * Constructs the AddTimeCommand.
     *
     * @param plugin     The main plugin instance.
     * @param configUtil Configuration utility to manage custom messages.
     */
    public addAllTimeCommand(WFlyV2 plugin, ConfigUtil configUtil, ProxyCommandManager proxyCommandManager) {
        super(plugin, "wfly.addall");
        setDescription("Manage fly time for players");
        setUsage("/wfly addtime <player> <time>");
        addArgs("time", Integer.class);
        setPermission(Permissions.ADD_FLY_TIME.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.proxyCommandManager = proxyCommandManager;
    }

    /**
     * Executes the command logic to add fly time to a player.
     *
     * @param commandSender The command sender (player or console).
     * @param arguments   The command arguments (player and time).
     */
    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        int time = arguments.get("time");




        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wayviwflyv2 addall "  + time);


        if (commandSender instanceof Player) {
            Bukkit.dispatchCommand(commandSender, "wayviwflyv2 addall " + time);
            Player playerSender = (Player) commandSender;
            for (Player target : Bukkit.getOnlinePlayers()) {
                ColorSupportUtil.sendColorFormat(playerSender, configUtil.getCustomMessage()
                        .getString("message.fly-time-added-to-player")
                        .replace("%time%", String.valueOf(time))
                        .replace("%player%", target.getName()));

            }
        }
    }
}
