package com.wayvi.wfly.wflyv2.util;

import com.wayvi.wfly.wflyv2.WFlyV2;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    public void sendAboutMessage(CommandSender sender, String developer, String version, String storageType, String serverType, String serverVersion) {
        List<String> aboutMessage = new ArrayList<>();
        aboutMessage.add("&7==== &bAdminFly Debug Info &7====");
        aboutMessage.add("&eDeveloper: &f" + developer);
        aboutMessage.add("&eVersion: &f" + version);
        aboutMessage.add("&eStorage Type: &f" + storageType);
        aboutMessage.add("&eServer Type: &f" + serverType);
        aboutMessage.add("&eServer Version: &f" + serverVersion);

        // Récupère la dernière version et complète le message
        getLatestVersion(latestVersion -> {
            int current = parseVersion(version);
            int latest = parseVersion(latestVersion);
            String versionStatus;

            if (current < latest) {
                int behind = latest - current;
                versionStatus = "&cOutdated! " + behind + " version(s) behind (Latest: " + latestVersion + ")";
            } else {
                versionStatus = "&aUp to date (Latest: " + latestVersion + ")";
            }

            aboutMessage.add("&eUpdate Status: &f" + versionStatus);

            // Envoie le message complet
            for (String message : aboutMessage) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ColorSupportUtil.sendColorFormat(player, message);
                } else {
                    sender.sendMessage(stripColorCodes(message));
                }
            }
        });
    }

    /** Méthode utilitaire pour retirer les codes couleur pour la console */
    private String stripColorCodes(String message) {
        return message.replaceAll("&[0-9a-fk-or]", "");
    }

}
