package com.wayvi.wfly.wflyv2.commands.items;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.commands.TimeUnits;
import com.wayvi.wfly.wflyv2.managers.WItemsManager;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;

public class GiveFlyTokenToOtherCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private final WItemsManager wItemsManager;

    public GiveFlyTokenToOtherCommand(WFlyV2 plugin, WItemsManager wItemsManager) {
        super(plugin, "wfly.convert");
        addArgs("target", Player.class);
        addArgs("time", Integer.class, (sender, current) -> Arrays.asList("10","30","60"));
        addOptionalArgs("units", TimeUnits.class);
        setPermission(Permissions.FLY_TOKEN_GIVE_OTHER.getPermission());
        this.plugin = plugin;
        this.wItemsManager = wItemsManager;
    }

    @Override
    public void execute(CommandSender sender, Arguments arguments) {

        int basicTime = arguments.get("time");
        Player target = arguments.get("target");
        Optional<TimeUnits> units = arguments.getOptional("units");
        int time = units.map(timeUnits -> TimeUnits.convertTimeToType(basicTime, timeUnits)).orElse(basicTime);
        wItemsManager.giveFlyToken(sender, target, time);

    }
}


