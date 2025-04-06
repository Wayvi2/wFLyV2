package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.FlyManager;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to manage the flight speed of a player.
 */
public class FlySpeedCommand extends Command<WFlyV2> {

    private final FlyManager flyManager;

    /**
     * Constructs the FlySpeedCommand.
     *
     * @param plugin     The main plugin instance.
     * @param flyManager The fly manager responsible for handling flight speed changes.
     */
    public FlySpeedCommand(WFlyV2 plugin, FlyManager flyManager) {
        super(plugin, "flyspeed");
        setDescription("Manage the fly speed");
        setUsage("/flyspeed <number>");
        addArgs("speed", Double.class);
        this.flyManager = flyManager;
    }

    /**
     * Executes the command to set the player's flight speed.
     *
     * @param commandSender The sender of the command (must be a player).
     * @param arguments     The command arguments containing the speed value.
     */
    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        double speed = arguments.get("speed");
        flyManager.manageFlySpeed((Player) commandSender, speed);
    }
}
