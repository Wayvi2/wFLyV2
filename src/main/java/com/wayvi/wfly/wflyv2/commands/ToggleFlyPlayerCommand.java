package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.ToggleType;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ToggleFlyPlayerCommand extends Command<WFlyV2> {

    private WFlyV2 plugin;
    private final ConfigUtil configUtil;
    private FlyCommand flyCommand;

    public ToggleFlyPlayerCommand(WFlyV2 plugin, ConfigUtil configUtil, FlyCommand flyCommand) {
        super(plugin, "wfly");
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.flyCommand = flyCommand;
        addArgs("state", ToggleType.class);
        addArgs("player", Player.class);
        setPermission(Permissions.TOGGLE_FLY.getPermission());
    }

    @Override
    public void execute(CommandSender sender, Arguments arguments) {
        ToggleType state = arguments.get("state");
        Player targetPlayer = arguments.get("player");

        if (state == null || targetPlayer == null) {
            String message = configUtil.getCustomMessage().getString("message.arg-not-recognized");
            if (sender instanceof Player) {
                ColorSupportUtil.sendColorFormat((Player) sender, message);
            } else {
                plugin.getLogger().warning(message);
            }
            return;
        }

        boolean toActivate = state == ToggleType.ACTIVATE;

        if (toActivate) {
            boolean activated = flyCommand.tryActivateFly(targetPlayer);

            if (activated) {
                String msg = configUtil.getCustomMessage()
                        .getString("message.fly-activated-player")
                        .replace("%player%", targetPlayer.getName());

                if (sender instanceof Player) {
                    ColorSupportUtil.sendColorFormat((Player) sender, msg);
                } else {
                    plugin.getLogger().info(msg);
                }
            }

        } else {
            WflyApi.get().getFlyManager().manageFly(targetPlayer.getUniqueId(), false);

            String msgSender = configUtil.getCustomMessage()
                    .getString("message.fly-deactivated-player")
                    .replace("%player%", targetPlayer.getName());

            if (sender instanceof Player) {
                ColorSupportUtil.sendColorFormat((Player) sender, msgSender);
            } else {
                plugin.getLogger().info(msgSender);
            }

            String msgTarget = configUtil.getCustomMessage().getString("message.fly-deactivated");
            if (msgTarget != null) {
                ColorSupportUtil.sendColorFormat(targetPlayer, msgTarget);
            }
        }
    }
}
