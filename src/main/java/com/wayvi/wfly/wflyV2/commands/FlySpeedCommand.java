package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.managers.FlyManager;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FlySpeedCommand extends Command<JavaPlugin> {

    private final FlyManager flyManager;
    private final ConfigUtil configUtil;
    private final MiniMessageSupportUtil miniMessageSupportUtil;

    public FlySpeedCommand(JavaPlugin plugin, FlyManager flyManager, ConfigUtil configUtil, MiniMessageSupportUtil miniMessageSupportUtil) {
        super(plugin, "flyspeed");
        setDescription("Manage the fly speed");
        setUsage("/flyspeed <number>");
        setPermission(Permissions.FLY_SPEED.getPermission());
        addArgs("speed:double");
        this.flyManager = flyManager;
        this.configUtil = configUtil;
        this.miniMessageSupportUtil = miniMessageSupportUtil;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

        double speed = arguments.get("speed");
        double fspeed = speed / 10.0F;

        String messageFlySpeed = speed > 10 ? configUtil.getCustomMessage().getString("message.fly-speed-too-high") : configUtil.getCustomMessage().getString("message.fly-speed").replace("%speed%", String.valueOf(speed));

        flyManager.manageFlySpeed((Player) commandSender, fspeed);

        commandSender.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(messageFlySpeed));

    }
}
