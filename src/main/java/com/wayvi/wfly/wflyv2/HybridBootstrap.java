package com.wayvi.wfly.wflyv2;

import com.wayvi.wfly.wflyv2.bungeepackage.BungeeMain;
import com.wayvi.wfly.wflyv2.spigotpackage.WFlyV2;

public class HybridBootstrap {
    private Object delegate;

    public void onLoad() {
        if (isBungee()) {
            delegate = new BungeeMain();
            call(delegate, "onLoad");
        }
    }

    public void onEnable() {
        if (isSpigot()) {
            delegate = new WFlyV2();
            call(delegate, "onEnable");
        } else if (isBungee()) {
            if (delegate == null) {
                delegate = new BungeeMain();
            }
            call(delegate, "onEnable");
        }
    }

    public void onDisable() {
        if (delegate != null) {
            call(delegate, "onDisable");
        }
    }

    private void call(Object obj, String methodName) {
        try {
            obj.getClass().getMethod(methodName).invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isSpigot() {
        try {
            Class.forName("org.bukkit.Bukkit");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean isBungee() {
        try {
            Class.forName("net.md_5.bungee.api.ProxyServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}