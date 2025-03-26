package com.wayvi.wfly.wflyV2.constants;

public enum Permissions {


    RELOAD("wfly.reload"),
    FLY("wfly.fly"),
    FLY_SPEED("wfly.fly.speed"),
    ADD_FLY_TIME("wfly.add.time"),
    REMOVE_FLY_TIME("wfly.remove.time"),
    ADD_RESET_TIME("wfly.reset.time"),
    INFINITE_FLY("wfly.infinite.fly"),
    BYPASS_FLY("wfly.bypass.fly"),
    ;

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String getPermission(){
        return permission;
    }
}
