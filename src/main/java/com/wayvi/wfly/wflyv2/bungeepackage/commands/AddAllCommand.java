package com.wayvi.wfly.wflyv2.bungeepackage.commands;


import com.wayvi.wfly.wflyv2.bungeepackage.api.ProxyCommandSender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

import net.md_5.bungee.api.plugin.Command;



public class AddAllCommand extends Command {

    private final ProxyCommandSender proxyCommandSender;

    public AddAllCommand() {
        super("wayviwflyv2");
        this.proxyCommandSender = new ProxyCommandSender(ProxyServer.getInstance());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /broadcastmsg <subCommand> [args...]");
            return;
        }

        String subCommand = args[0];
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);

        proxyCommandSender.sendToAllServers(subCommand, subArgs);
        sender.sendMessage("Message envoyé à tous les serveurs !");
    }
}

