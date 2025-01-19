package com.wayvi.wfly.wflyV2.commands;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import fr.traqueur.commands.impl.SimpleCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;

public class ReloadCommand extends Command<JavaPlugin> {

    ConfigUtil configUtil;


    public ReloadCommand(JavaPlugin plugin, ConfigUtil configUtil) {
        super(plugin, "wfly");
        setDescription("Reload file of the plugin.");
        setUsage("/wfly reload");
        addArgs("reload:string");
        this.configUtil = configUtil;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {


        configUtil.reload();
        commandSender.sendMessage("Plugin successfully reloaded!");

    }
}
