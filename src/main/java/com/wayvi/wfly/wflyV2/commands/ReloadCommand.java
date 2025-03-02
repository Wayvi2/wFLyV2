package com.wayvi.wfly.wflyV2.commands;


import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.listeners.PvPListener;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;


public class ReloadCommand extends Command<JavaPlugin>  {

    private final ConfigUtil configUtil;
    private final Plugin plugin;
    private final PvPListener pvpListener;

    public ReloadCommand(JavaPlugin plugin, ConfigUtil configUtil, PvPListener pvPListener) {
        super(plugin, "wfly.reload");
        setDescription("Reload file of the plugin.");
        setUsage("/wfly reload");
        setPermission(Permissions.RELOAD.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.pvpListener = pvPListener;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

        configUtil.reloadCustomConfig();
        pvpListener.reloadConfigValues();
        String message = configUtil.getCustomMessage().getString("message.reload");
        plugin.getLogger().info("Plugin reloaded");

        if (commandSender instanceof Player) {
            ColorSupportUtil.sendColorFormat((Player) commandSender,message);
        }
    }
}
