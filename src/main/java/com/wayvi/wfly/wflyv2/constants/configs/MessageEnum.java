package com.wayvi.wfly.wflyv2.constants.configs;

import com.wayvi.wconfigapi.wconfigapi.ConfigKey;

import java.util.Arrays;

public enum MessageEnum implements ConfigKey<Object> {

    // Commands Time Remaining
    COMMANDS_TIME_REMAINING_0("commands-time-remaining.0.commands",
            "title %player% title {\"text\":\"You have 0 seconds fly left!\",\"bold\":true,\"color\":\"gold\"}"),
    COMMANDS_TIME_REMAINING_1("commands-time-remaining.1.commands",
            "title %player% title {\"text\":\"You have 1 seconds fly left!\",\"bold\":true,\"color\":\"gold\"}"),
    COMMANDS_TIME_REMAINING_2("commands-time-remaining.2.commands",
            "title %player% title {\"text\":\"You have 2 seconds fly left!\",\"bold\":true,\"color\":\"gold\"}"),
    COMMANDS_TIME_REMAINING_3("commands-time-remaining.3.commands",
            "title %player% title {\"text\":\"You have 3 seconds fly left!\",\"bold\":true,\"color\":\"gold\"}"),
    COMMANDS_TIME_REMAINING_5("commands-time-remaining.5.commands",
            "title %player% title {\"text\":\"You have 5 seconds fly left!\",\"bold\":true,\"color\":\"gold\"}"),

    // Messages
    RELOAD("message.reload", "&cPlugin has been reloaded!"),
    FLY_ACTIVATED("message.fly-activated", "&aYou have been set to fly! Use /fly to disable fly."),
    FLY_DEACTIVATED("message.fly-deactivated", "&cYou have been set to walk! Use /fly to enable fly."),
    FLY_SPEED_TOO_HIGH("message.fly-speed-too-high", "&cSpeed too high! Maximum speed is 10"),
    FLY_SPEED("message.fly-speed", "&aYou have set your fly speed to &e%speed%"),
    FLY_SPEED_NO_PERMISSION("message.fly-speed-no-permission", "&cYou do not have permission to set your fly speed to &e%speed%"),
    NO_TIMEFLY_REMAINING("message.no-timefly-remaining", "&cYou have no timefly remaining!"),
    FLY_TIME_ADDED("message.fly-time-added", "&aYou have been given &e%time% &atimefly!"),
    FLY_TIME_ADDED_TO_PLAYER("message.fly-time-added-to-player", "&aYou have been given &e%time% &atimefly to &e%player%"),
    FLY_TIME_REMOVE_TO_PLAYER("message.fly-time-remove-to-player", "&aYou have removed &e%time% &atimefly from &e%player%"),
    FLY_TIME_RESET_TO_PLAYER("message.fly-time-reset-to-player", "&aYou have been given reset &atimefly to &e%player%"),
    FLY_TIME_REMOVED("message.fly-time-removed", "&cYou have been taken &e%time% &ctimefly!"),
    FLY_TIME_RESET("message.fly-time-reset", "&aYou have been given &e0 &atimefly!"),
    NO_FLY_HERE("message.no-fly-here", "&cYou cannot fly here!"),
    FLY_REMOVE_TOO_HIGH("message.fly-remove-too-high", "&cYou cannot remove too much timefly!"),
    NO_PERMISSION("message.no-permission", "&cYou do not have permission to use this command!"),
    ONLY_IN_GAME("message.only-in-game", "&cThis command can only be used in game!"),
    MISSING_ARGS("message.missing-args", "&cMissing arguments!"),
    ARG_NOT_RECOGNIZED("message.arg-not-recognized", "&cArgument not recognized!"),
    MESSAGE_REQUIREMENT("message.message-requirement", "&cYou do not meet the requirements to use this command!"),
    PLAYER_IN_RANGE("message.player-in-range", "&cPlayer is too far away!"),
    NO_SPECTATOR("message.no-spectator", "&cYou can deactivate fly in spectator mode!"),
    FLY_TIME_ADDED_TO_ALL_PLAYER("message.fly-time-added-to-all-player", "&aYou have been given &e%time% &atimefly to all players!"),
    FLY_TIME_REMOVED_TO_ALL_PLAYER("message.fly-time-removed-to-all-player", "&aYou have removed &e%time% &atimefly from all players!"),
    FLY_TIME_RESET_TO_ALL_PLAYER("message.fly-time-reset-to-all-player", "&aYou have been given reset &atimefly to all players!"),
    GET_PLAYER_FLY_TIME("message.get-player-fly-time", "&a%player% have %fly_remaining%"),
    EXCHANGE_RECEIVER("message.exchange-receiver", "&a%donator% give you &e%time%"),
    EXCHANGE_DONATOR("message.exchange-donator", "&aYou have been given %time%s to %receiver%"),
    EXCHANGE_CANNOT_THE_SAME("message.exchange-cannot-the-same", "&cYou cannot exchange time fly with you!"),
    ONLY_GET_HIS_FLY_TIME("message.only-get-his-fly-time", "&cYou cannot get other time fly than you."),
    HAVE_YOUR_FLY_TIME("message.have-your-fly-time", "&aYou have %fly_remaining%"),
    EXCHANGE_TIME_OUT("message.exchange-time-out", "&cYou cannot give too much!"),
    EXCHANGE_TIME_ZERO("message.exchange-time-zero", "&cYou cannot give zero and negative fly time!"),
    FLY_ACTIVATED_PLAYER("message.fly-activated-player", "&aYou have activated fly for %player%"),
    FLY_DEACTIVATED_PLAYER("message.fly-deactivated-player", "&cYou have deactivated fly for %player%"),
    PLAYER_NOT_IN_FLY("message.player-not-in-fly", "&cThe %player% is not in fly!"),
    CANNOT_ADD_TIME_UNLIMITED("message.cannot-add-time-unlimited", "&cYou cannot add time to an unlimited player!"),
    PLAYER_HAS_UNLIMITED("message.player-has-unlimited", "&a%player% has unlimited fly!"),
    COOLDOWN_GIVE("message.cooldown-give", "&cYou must wait %seconds% before give fly again!"),
    EXCHANGE_TIME_BELOW_MINIMUM("message.exchange-time-below-minimum", "&cYou must give at least %min% seconds of fly time."),
    EXCHANGE_TIME_ABOVE_MAXIMUM("message.exchange-time-above-maximum", "&cYou cannot give more than %max% seconds of fly time."),

    // Help messages player
    HELP_MESSAGE_PLAYER("message.help-message-player", Arrays.asList(
            "&8&m──────&7 » &b&lw&bFlight &7Player &7« &8&m──────",
            " &8| &b/fly",
            " &8| &7Toggle between flight statuses.",
            "&r",
            " &8| &b/flyspeed <number>",
            " &8| &7Set your fly speed.",
            "&r",
            " &8| &b/flytime",
            " &8| &7Check your remaining flight time.",
            "&r",
            " &8| &b/flytime <player>",
            " &8| &7Check another player's remaining flight time.",
            "&r",
            " &8| &b/fly give <player> <amount in seconds>",
            " &8| &7Give a player flight time out of your remaining flight time.",
            "&r"
    )),

    // Help messages admin
    HELP_MESSAGE_ADMIN("message.help-message-admin", Arrays.asList(
            "&8&m──────&7 » &b&lw&bFlight &7Admin &7« &8&m──────",
            " &8| &b/wfly addtime <player> <amount>",
            " &8| &7Add flight time, in seconds, to a player.",
            "&r",
            " &8| &b/wfly removetime <player> <amount>",
            " &8| &7Remove flight time, in seconds, from a player.",
            "&r",
            " &8| &b/wfly reset <player>",
            " &8| &7Sets a player's flight time to 0 seconds.",
            "&r",
            " &8| &b/wfly addall <amount>",
            " &8| &7Add flight time to all players.",
            "&r",
            " &8| &b/wfly removeall <amount>",
            " &8| &7Remove flight time from all players.",
            "&r",
            " &8| &b/wfly reload",
            " &8| &7Reloads the plugins configurations.",
            "&r"
    ));


    private final String path;
    private final Object defaultValue;

    MessageEnum(String path, Object defaultValue) {
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
