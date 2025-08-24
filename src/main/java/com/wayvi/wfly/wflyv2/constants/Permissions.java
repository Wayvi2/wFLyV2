package com.wayvi.wfly.wflyv2.constants;

/**
 * Enum representing the various permissions used in the WFly plugin.
 * Each constant corresponds to a specific permission node for different functionalities in the plugin.
 */
public enum Permissions {

    /**
     * Permission to reload the plugin.
     */
    RELOAD("wfly.reload"),

    /**
     * Permission to toggle fly mode for the player.
     */
    FLY("wfly.fly"),

    /**
     * Permission to change the fly speed of the player.
     */
    FLY_SPEED("wfly.fly.speed"),

    /**
     * Permission to add fly time to a player.
     */
    ADD_FLY_TIME("wfly.add.time"),

    /**
     * Permission to remove fly time from a player.
     */
    REMOVE_FLY_TIME("wfly.remove.time"),

    /**
     * Permission to reset the player's fly time.
     */
    ADD_RESET_TIME("wfly.reset.time"),

    /**
     * Permission to enable infinite fly time for the player.
     */
    INFINITE_FLY("wfly.infinite.fly"),

    /**
     * Permission to bypass the fly restrictions (e.g., for staff or admins).
     */
    BYPASS_FLY("wfly.bypass.fly"),

    MANAGE_FLY("wfly.manage.fly"),

    GET_FLY_TIME_ADMIN("wfly.getflytime.player"),

    GET_FLY_TIME("wfly.getflytime"),

    HELP_PLAYER("wfly.help.player"),

    TOGGLE_FLY("wfly.toggle.fly"),

    EXCHANGE_FLY_TIME("wfly.give.fly.time"),

    HELP_ADMIN("wfly.help.admin"),

    WFLY_ABOUT("wfly.about");

    private final String permission;

    /**
     * Constructor for the Permissions enum. Each permission corresponds to a string value.
     *
     * @param permission the permission string
     */
    Permissions(String permission) {
        this.permission = permission;
    }

    /**
     * Gets the permission string associated with this enum constant.
     *
     * @return the permission string
     */
    public String getPermission() {
        return permission;
    }
}
