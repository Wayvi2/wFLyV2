package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.managers.fly.FlyManager;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Command to manage the flight speed of a player.
 */
public class FlySpeedCommand extends Command<JavaPlugin> {

    private final FlyManager flyManager;

    /**
     * Constructs the FlySpeedCommand.
     *
     * @param plugin     The main plugin instance.
     * @param flyManager The fly manager responsible for handling flight speed changes.
     */
    public FlySpeedCommand(JavaPlugin plugin, FlyManager flyManager) {
        super(plugin, "flyspeed");
        setDescription("Manage the fly speed");
        setUsage("/flyspeed <number>");
        addArgs("speed:double");
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
