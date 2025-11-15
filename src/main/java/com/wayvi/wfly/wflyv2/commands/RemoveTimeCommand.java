package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.commands.TimeUnits;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Command to remove fly time from a player.
 */
public class RemoveTimeCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;

    /**
     * Constructs the RemoveTimeCommand.
     *
     * @param plugin The main plugin instance.
     */
    public RemoveTimeCommand(WFlyV2 plugin) {
        super(plugin, "wfly.removetime");
        setDescription("Manage fly time for players.");
        setUsage("/fly removetime <player> <time>");
        addArgs("player", Player.class);
        addArgs("time", Integer.class, (sender, current) -> Arrays.asList("10","30","60"));
        addOptionalArgs("units", TimeUnits.class);
        addOptionalArgs("silent", String.class, (sender, partial) -> Collections.singletonList("-s"));
        setPermission(Permissions.REMOVE_FLY_TIME.getPermission());
        this.plugin = plugin;
    }

    /**
     * Executes the remove fly time command.
     *
     * @param sender    The sender of the command.
     * @param arguments The command arguments: player and time.
     */
    @Override
    public void execute(CommandSender sender, Arguments arguments) {
        Player target = arguments.get("player");

        int basicTime = arguments.get("time");
        Optional<TimeUnits> units = arguments.getOptional("units");
        int time = units.map(u -> TimeUnits.convertTimeToType(basicTime, u))
                .orElse(basicTime);

        String silentFlag = arguments.getOptional("silent").orElse("").toString();
        boolean silent = "-s".equalsIgnoreCase(silentFlag);

        if (WflyApi.get().getTimeFlyManager().removeFlyTime(target, time)) {

            if (!silent) {
                String message = plugin.getMessageFile().get(MessageEnum.FLY_TIME_REMOVED);
                ColorSupportUtil.sendColorFormat(target,
                        message.replace("%time%", WFlyPlaceholder.formatTime(plugin, time, true)));
            }

            if (sender instanceof Player) {
                Player playerSender = (Player) sender;
                String messageRemove = plugin.getMessageFile().get(MessageEnum.FLY_TIME_REMOVE_TO_PLAYER);
                ColorSupportUtil.sendColorFormat(playerSender,
                        messageRemove.replace("%time%", WFlyPlaceholder.formatTime(plugin, time, true))
                                .replace("%player%", target.getName()));
            } else {
                plugin.getLogger().info("You have removed " + basicTime + " " +
                        units.orElse(TimeUnits.SECONDS).getTimeUnits() +
                        " fly time from " + target.getName());
            }
        }
    }
}



