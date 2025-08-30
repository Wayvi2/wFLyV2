package com.wayvi.wfly.wflyv2.commands.items;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.managers.WItemsManager;
import com.wayvi.wfly.wflyv2.util.VersionCheckerUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveFlyTokenCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private final WItemsManager wItemsManager;

    public GiveFlyTokenCommand(WFlyV2 plugin, WItemsManager wItemsManager) {
        super(plugin, "fly.convert");
        addArgs("time", Integer.class);
        setPermission(Permissions.FLY_TOKEN.getPermission());
        this.plugin = plugin;
        this.wItemsManager = wItemsManager;
    }

    @Override
    public void execute(CommandSender sender, Arguments arguments) {

        int time = arguments.get("time");
        wItemsManager.giveFlyToken((Player) sender,time);




    }
}


