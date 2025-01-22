package com.wayvi.wfly.wflyV2.constants;

public enum Permissions {

    RELOAD("wfly.reload"),
    FLY("wfly.fly"),
    FLY_SPEED("wfly.fly.speed"),
    ;

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String getPermission(){
        return permission;
    }
}
