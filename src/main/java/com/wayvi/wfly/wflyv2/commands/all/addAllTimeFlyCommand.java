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

public class addAllTimeFlyCommand  extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private final String SERVER = Bukkit.getServer().getName();


    /**
     * Constructs the AddTimeCommand.
     *
     * @param plugin     The main plugin instance.
     */
    public addAllTimeFlyCommand(WFlyV2 plugin) {
        super(plugin, "wfly.addall");
        setDescription("Manage fly time for all players");
        setUsage("/wfly addtime <player> <time>");
        addArgs("time", Integer.class, Arrays.asList("10","20","30","40"));
        addArgs("units", TimeUnits.class);
        setPermission(Permissions.ADD_FLY_TIME.getPermission());
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
        TimeUnits units = arguments.get("units");

        int time = TimeUnits.convertTimeToType(basicTime, units);

        WflyApi.get().getTimeFlyManager().addFlytimeForAllPlayers(time); //to actual server
        sendFlyTimeAllProxy(time); //to other proxy server


        String message = plugin.getMessageFile().get(MessageEnum.FLY_TIME_ADDED);
        for (Player target : plugin.getServer().getOnlinePlayers()) {
            ColorSupportUtil.sendColorFormat(target, message.replace("%time%", WFlyPlaceholder.formatTime(plugin,time)));
        }


        if (commandSender instanceof Player) {
            Player playerSender = (Player) commandSender;
            String messageToSender = plugin.getMessageFile().get(MessageEnum.FLY_TIME_ADDED_TO_ALL_PLAYER);
            ColorSupportUtil.sendColorFormat(playerSender, messageToSender.replace("%time%", WFlyPlaceholder.formatTime(plugin,time)));
        } else {
            plugin.getLogger().info("You have given " + time  + " " + units.getTimeUnits() + " fly time to all players");
        }
    }

    public void sendFlyTimeAllProxy(int time) {
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player == null) return;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("flyAddAll");
        out.writeUTF(plugin.getServerId() + ":" + time);

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

}