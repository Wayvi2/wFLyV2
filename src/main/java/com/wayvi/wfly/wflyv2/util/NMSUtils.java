package com.wayvi.wfly.wflyv2.util;

import org.bukkit.Bukkit;

public class NMSUtils {

    /**
     * Get minecraft serveur version
     *
     * @return version
     */
    public static double getNMSVersion() {
        if (version != 0)
            return version;
        try {
            String var1 = Bukkit.getServer().getClass().getPackage().getName();
            String[] arrayOfString = var1.replace(".", ",").split(",")[3].split("_");
            String var2 = arrayOfString[0].replace("v", "");
            String var3 = arrayOfString[1];
            return version = Double.parseDouble(var2 + "." + var3);
        } catch (Exception ignored) {
            return 1.20;
        }
    }

    public static double version = getNMSVersion();










}
