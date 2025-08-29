package com.wayvi.wfly.wflyv2.constants.configs;

import com.wayvi.wconfigapi.wconfigapi.ConfigKey;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public enum ItemsEnum implements ConfigKey<Object> {


    FLY_TOKEN_NAME("items.fly-token.items.fly-token.name", "&bFly Token &7(%time%)"),
    FLY_TOKEN_LORE("items.fly-token.items.fly-token.lore",
            Arrays.asList(
                    "&7Right-click to claim",
                    "&e%time% &7of fly time.",
                    "&8Tradable and exchangeable."
            )),
    FLY_TOKEN_MATERIAL("items.fly-token.items.fly-token.material", "PAPER"),
    FLY_TOKEN_CUSTOM_MODEL_DATA("items.fly-token.items.fly-token.custom-model-data", 12345),
    FLY_TOKEN_MESSAGE_GIVE("items.fly-token.items.fly-token.messages.give",
            "&aYou received a &bFly Token &7(%time%)"),
    FLY_TOKEN_MESSAGE_USE("items.fly-token.items.fly-token.messages.use",
            "&aYou gained &e%time% &aof fly time!"),
    FLY_TOKEN_MESSAGE_ERROR("items.fly-token.items.fly-token.messages.error",
            "&cThis item is not valid.");

    private final String path;
    private final Object defaultValue;


    <T> ItemsEnum(String path, T defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }


    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}