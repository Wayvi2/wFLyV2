package com.wayvi.wfly.wflyV2.commands;


import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;


public class ReloadCommand extends Command<JavaPlugin>  {

    private final MiniMessageSupportUtil miniMessageSupportUtil;
    private final ConfigUtil configUtil;
    private final Plugin plugin;

    public ReloadCommand(JavaPlugin plugin, ConfigUtil configUtil, MiniMessageSupportUtil miniMessageSupportUtil) {
        super(plugin, "wfly.reload");
        setDescription("Reload file of the plugin.");
        setUsage("/wfly reload");
        setPermission(Permissions.RELOAD.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.miniMessageSupportUtil = miniMessageSupportUtil;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        configUtil.reloadCustomConfig();
        String message = configUtil.getCustomMessage().getString("message.reload");
        assert message != null;
        plugin.getLogger().info(message);
        commandSender.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(message));
    }
}
