package com.wayvi.wfly.wflyv2.commands.all;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.commands.TimeUnits;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class RemoveAllTimeFlyCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;

    /**
     * Constructs the AddTimeCommand.
     *
     * @param plugin     The main plugin instance.
     */
    public RemoveAllTimeFlyCommand(WFlyV2 plugin) {
        super(plugin, "wfly.removeall");
        setDescription("Manage fly time for all players");
        setUsage("/wfly addtime <player> <time>");
        addArgs("time", Integer.class, (sender, current) -> Arrays.asList("10","30","60"));
        addOptionalArgs("units", TimeUnits.class);
        addOptionalArgs("silent", String.class, (sender, partial) -> Collections.singletonList("-s"));
        setPermission(Permissions.REMOVE_FLY_TIME.getPermission());
        this.plugin = plugin;
    }

    /**
     * Executes the command logic to add fly time to a player.
     *
     * @param commandSender The command sender (player or console).
     * @param arguments   The command arguments (player and time).
     */
    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

        int basicTime = arguments.get("time");
        Optional<TimeUnits> units = arguments.getOptional("units");
        int time = units.map(u -> TimeUnits.convertTimeToType(basicTime, u))
                .orElse(basicTime);

        String silentFlag = arguments.getOptional("silent").orElse("").toString();
        boolean silent = "-s".equalsIgnoreCase(silentFlag);

        WflyApi.get().getTimeFlyManager().removeFlytimeForAllPlayers(time); // local
        removeFlyTimeAllProxy(time); // others proxy


        if (!silent) {
            String message = plugin.getMessageFile().get(MessageEnum.FLY_TIME_REMOVED);
            for (Player target : plugin.getServer().getOnlinePlayers()) {
                ColorSupportUtil.sendColorFormat(target,
                        message.replace("%time%", WFlyPlaceholder.formatTime(plugin, time, true)));
            }
        }

        if (commandSender instanceof Player) {
            Player playerSender = (Player) commandSender;
            String messageToSender = plugin.getMessageFile().get(MessageEnum.FLY_TIME_REMOVED_TO_ALL_PLAYER);
            ColorSupportUtil.sendColorFormat(playerSender,
                    messageToSender.replace("%time%", WFlyPlaceholder.formatTime(plugin, time, true)));
        } else {
            plugin.getLogger().info("You have removed " + basicTime + " " +
                    units.orElse(TimeUnits.SECONDS).getTimeUnits() + " fly time from all players");
        }
    }


    public void removeFlyTimeAllProxy(int time) {
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player == null) return;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("flyRemoveAll");
        out.writeUTF(plugin.getServerId() + ":" + time);

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}