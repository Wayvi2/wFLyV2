package com.wayvi.wfly.wflyV2.commands;


import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;


public class ReloadCommand extends Command<JavaPlugin>  {

    ConfigUtil configUtil;
    Plugin plugin;


    public ReloadCommand(JavaPlugin plugin, ConfigUtil configUtil) {
        super(plugin, "wfly.reload");
        setDescription("Reload file of the plugin.");
        setUsage("/wfly reload");
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

        configUtil.reloadCustomConfig();
        String message = configUtil.getCustomConfig().getString("message.reload");
        assert message != null;
        commandSender.sendMessage(message);



    }


}
