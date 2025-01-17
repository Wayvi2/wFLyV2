package com.wayvi.wfly.wflyV2.commands;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.impl.SimpleCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;

public class ReloadCommand extends SimpleCommand {

    ConfigUtil configUtil;


    public ReloadCommand(JavaPlugin plugin, String name, ConfigUtil configUtil) {
        super(plugin, "wfly reload");
        setDescription("Reload file of the plugin.");
        setUsage("/wfly reload");
        this.configUtil = configUtil;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {


        configUtil.reload();
        commandSender.sendMessage("Plugin successfully reloaded!");

    }
}
