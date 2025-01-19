package com.wayvi.wfly.wflyV2.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MiniMessageSupportUtil {


    public Component sendMiniMessageFormat(String message) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        return miniMessage.deserialize(message);
    }
}