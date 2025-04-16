package com.wayvi.wfly.wflyv2.util;

import com.wayvi.wfly.wflyv2.WFlyV2;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class VersionCheckerUtil {

    private final WFlyV2 plugin;
    private final int resourceId;

    public VersionCheckerUtil(WFlyV2 plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getLatestVersion(Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream();
                 Scanner scanner = new Scanner(inputStream)) {

                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Version checker is broken, can't find version: " + e.getMessage());
            }
        });
    }

    public static int parseVersion(String version) {
        String[] parts = version.split("\\.");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            builder.append(part);
        }

        while (builder.length() < 4) {
            builder.append("0");
        }

        return Integer.parseInt(builder.toString());
    }

    public void checkAndNotify() {
        getLatestVersion(latestVersion -> {
            String currentVersion = plugin.getDescription().getVersion();
            int current = parseVersion(currentVersion);
            int latest = parseVersion(latestVersion);

            if (current < latest) {
                int behind = latest - current;
                plugin.getLogger().warning("Plugin is not up to date! You are " + behind + " version(s) behind. Latest: " + latestVersion);
            } else {
                plugin.getLogger().info("Plugin is up to date (v" + currentVersion + ")");
            }
        });
    }
}
