package com.wayvi.wfly.wflyV2.util;

import com.wayvi.wfly.wflyV2.WFlyV2;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Utility class for checking the latest version of the plugin on Spigot.
 * This class retrieves the latest version from the Spigot API using the resource ID.
 */
public class VersionCheckerUtil {

    private final WFlyV2 plugin;
    private final int resourceId;

    /**
     * Constructs a new VersionCheckerUtil instance.
     *
     * @param plugin the plugin instance
     * @param resourceId the Spigot resource ID of the plugin
     */
    public VersionCheckerUtil(WFlyV2 plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    /**
     * Retrieves the latest version of the plugin from the Spigot API asynchronously.
     * The version is returned via a callback consumer.
     *
     * @param consumer the consumer that will process the retrieved version string
     */
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
}
