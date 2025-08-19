package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;

import com.wayvi.wfly.wflyv2.util.VersionCheckerUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class AboutCommand extends Command<WFlyV2> {

    private WFlyV2 plugin;


    public AboutCommand(WFlyV2 plugin) {
        super(plugin, "wfly.about");
        this.plugin = plugin;
        setPermission(Permissions.WFLY_ABOUT.getPermission());
    }

    @Override
    public void execute(CommandSender sender, Arguments arguments) {

        String version = plugin.getDescription().getVersion();
        String developer = "Wayvi2";
        boolean mysqlEnabled = plugin.getConfigFile().get(ConfigEnum.MYSQL_ENABLED);
        String storageType = mysqlEnabled ? "MySQL" : "SQLite";
        String serverType = Bukkit.getServer().getName();
        String serverVersion = Bukkit.getServer().getVersion();

        Player player = (Player) sender;

        new VersionCheckerUtil(plugin, 118465).sendAboutMessage(player, developer, version, storageType, serverType, serverVersion);
    }
}
