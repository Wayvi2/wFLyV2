package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetFlyStatusCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;

    public GetFlyStatusCommand(WFlyV2 plugin) {
        super(plugin, "wfly.status");
        addOptionalArgs("target", Player.class);
        setPermission(Permissions.FLY_STATUS.getPermission());
        this.plugin = plugin;

    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

        Player player = arguments.get("target");
        String message = plugin.getMessageFile().get(MessageEnum.FLY_STATUS);
        message = message
                .replace("%player%", player.getName())
                .replace("%status%", String.valueOf(WflyApi.get().getTimeFlyManager().getIsFlying(player.getUniqueId())));


        if (!(commandSender instanceof Player)){
            commandSender.sendMessage((String) ColorSupportUtil.convertColorFormat(message));
        } else {

            ColorSupportUtil.sendColorFormat((Player) commandSender, message);
        }
    }
}
