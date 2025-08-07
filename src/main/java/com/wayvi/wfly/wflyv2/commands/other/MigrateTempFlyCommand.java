package com.wayvi.wfly.wflyv2.commands.other;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.tempfly.StorageAdapter;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MigrateTempFlyCommand  extends Command<WFlyV2> {


    private WFlyV2 plugin;
    private StorageAdapter storageAdapter;


    public MigrateTempFlyCommand(WFlyV2 plugin, StorageAdapter storageAdapter) {
        super(plugin, "wfly.migrate.tempfly");
        this.plugin = plugin;
        this.storageAdapter = storageAdapter;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

        Plugin tempFly = Bukkit.getPluginManager().getPlugin("TempFly");

        if (tempFly == null) {
            plugin.getLogger().warning("TempFly plugin not found.");
            return;
        }

        if (!tempFly.isEnabled()) {
            plugin.getLogger().warning("TempFly plugin is present but not enabled.");
            return;
        }

        if (commandSender instanceof Player) {
            ColorSupportUtil.sendColorFormat((Player) commandSender, "&cYou cannot use this command in game.");
            return;
        }

        storageAdapter.migrateTempFly();

    }
}
