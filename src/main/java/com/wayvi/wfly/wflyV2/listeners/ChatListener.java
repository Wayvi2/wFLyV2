package com.wayvi.wfly.wflyV2.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {

        TextComponent textComponent = (TextComponent) event.message();
        MiniMessage miniMessage = MiniMessage.miniMessage();

        Component replacedText = miniMessage.deserialize(textComponent.content());
        event.message(replacedText);
    }
}
