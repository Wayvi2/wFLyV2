package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.managers.FlyManager;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class FlyCommand extends Command<JavaPlugin> {

    FlyManager flyManager;
    MiniMessageSupportUtil miniMessageSupportUtil;
    ConfigUtil configUtil;
    private Plugin plugin;

    public FlyCommand(JavaPlugin plugin, FlyManager flyManager, MiniMessageSupportUtil miniMessageSupportUtil, ConfigUtil configUtil) {
        super(plugin, "fly");
        setDescription("Fly command");
        setUsage("/fly");
        setPermission(Permissions.RELOAD);
        this.flyManager = flyManager;
        this.miniMessageSupportUtil = miniMessageSupportUtil;
        this.configUtil = configUtil;
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

        Player player = (Player) commandSender;
        // MESSAGE
        String messageActivateFly = configUtil.getCustomMessage().getString("message.fly-activated");
        String messageDisabledFly = configUtil.getCustomMessage().getString("message.fly-deactivated");

        try {
            // Vérifie si le joueur est actuellement en mode vol
            List<AccessPlayerDTO> players = flyManager.getIsInFlyBeforeDeconnect(player);

            boolean isCurrentlyFlying = !players.isEmpty() && players.get(0).getIsInFly() == 1;

            // Active ou désactive le mode vol selon l'état actuel
            if (isCurrentlyFlying) {
                flyManager.manageFly(player, false); // Désactive le vol
                player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(messageDisabledFly));
            } else {
                flyManager.manageFly(player, true); // Active le vol
                player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(messageActivateFly));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("Une erreur s'est produite lors de la gestion du vol.");
        }
    }
}
