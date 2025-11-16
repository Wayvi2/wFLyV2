package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.commands.TimeUnits;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Time;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Command to add fly time to a player.
 */
public class AddTimeCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;

    /**
     * Constructs the AddTimeCommand.
     *
     * @param plugin The main plugin instance.
     */
    public AddTimeCommand(WFlyV2 plugin) {
        super(plugin, "wfly.addtime");
        setDescription("Manage fly time for players");
        setUsage("/wfly addtime <player> <time>");
        addArgs("player", Player.class);
        addArgs("time", Integer.class, (sender, current) -> Arrays.asList("10","30","60"));
        addOptionalArgs("units", TimeUnits.class);
        addOptionalArgs("silent", String.class, (sender, partial) -> Collections.singletonList("-s"));
        setPermission(Permissions.ADD_FLY_TIME.getPermission());
        this.plugin = plugin;
    }

    /**
     * Executes the command logic to add fly time to a player.
     *
     * @param commandSender The command sender (player or console).
     * @param arguments     The command arguments (player and time).
     */
    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Player target = arguments.get("player");

        int basicTime = arguments.get("time");
        Optional<TimeUnits> units = arguments.getOptional("units");
        int time = units.map(u -> TimeUnits.convertTimeToType(basicTime, u))
                .orElse(basicTime);

        String silentFlag = arguments.getOptional("silent").orElse("").toString();
        boolean silent = "-s".equalsIgnoreCase(silentFlag);

        if (target.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
            if (commandSender instanceof Player) {
                String unlimitedMsg = plugin.getMessageFile().get(MessageEnum.CANNOT_ADD_TIME_UNLIMITED);
                ColorSupportUtil.sendColorFormat((Player) commandSender, unlimitedMsg);
            } else {
                plugin.getLogger().info("You cannot add fly time to " + target.getName() + " because they have unlimited fly time.");
            }
            return;
        }

        WflyApi.get().getTimeFlyManager().addFlytime(target, time);

        if (!silent) {
            String flyAddedMsg = plugin.getMessageFile().get(MessageEnum.FLY_TIME_ADDED);
            String formattedFlyAdded = flyAddedMsg.replace("%time%", WFlyPlaceholder.formatTime(plugin, time, true));
            ColorSupportUtil.sendColorFormat(target, formattedFlyAdded);
        }

        if (commandSender instanceof Player) {
            Player playerSender = (Player) commandSender;
            String playerMsg = plugin.getMessageFile().get(MessageEnum.FLY_TIME_ADDED_TO_PLAYER);
            String formattedPlayerMsg = playerMsg.replace("%time%", WFlyPlaceholder.formatTime(plugin, time, true))
                    .replace("%player%", target.getName());
            ColorSupportUtil.sendColorFormat(playerSender, formattedPlayerMsg);
        } else {
            plugin.getLogger().info("You have given " + basicTime + " " +
                    units.orElse(TimeUnits.SECONDS).getTimeUnits() + " fly time to " + target.getName());
        }
    }
}
