package com.wayvi.wfly.wflyv2.bungeepackage;

import com.wayvi.wfly.wflyv2.bungeepackage.commands.AddAllCommand;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMain extends Plugin {
    @Override
    public void onEnable() {

        getProxy().getPluginManager().registerCommand(this, new AddAllCommand());
    }

    @Override
    public void onDisable() {
        // ton code
    }
}
