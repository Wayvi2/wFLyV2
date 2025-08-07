package com.wayvi.wfly.wflyv2.tempfly;

import com.wayvi.wfly.wflyv2.ConsoleColors;
import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class StorageAdapter {


    WFlyV2 plugin;
    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREADS);
    private final FileConfiguration data;
    private final RequestHelper requestHelper;

    public StorageAdapter(WFlyV2 plugin, RequestHelper requestHelper) {
        this.requestHelper = requestHelper;
        this.plugin = plugin;

        File file = new File(Bukkit.getPluginManager().getPlugin("TempFly").getDataFolder(), "data.yml");
        if (!file.exists()) throw new IllegalStateException("Fichier data.yml introuvable");
        this.data = YamlConfiguration.loadConfiguration(file);
    }


    public double getTime(UUID uuid) {
        return data.getDouble("players." + uuid + ".time", 0.0);
    }

    public boolean isFlightEnabled(UUID uuid) {
        return data.getBoolean("players." + uuid + ".logged_in_flight", false);
    }

    public void migrateTempFly() {
        plugin.getLogger().info(ConsoleColors.GRAY + "[" + ConsoleColors.CYAN + "TempFly Migrator" + ConsoleColors.GRAY + "] " + ConsoleColors.GREEN + "Starting migration..." + ConsoleColors.RESET);
        long start = System.currentTimeMillis();

        if (!data.contains("players")) {
            plugin.getLogger().warning(ConsoleColors.GRAY + "[" + ConsoleColors.CYAN + "TempFly Migrator" + ConsoleColors.GRAY + "] " + ConsoleColors.RED + "No 'players' section found in TempFly's data.yml!" + ConsoleColors.RESET);
            return;
        }

        Set<String> keys = data.getConfigurationSection("players").getKeys(false);
        if (keys.isEmpty()) {
            plugin.getLogger().info(ConsoleColors.GRAY + "[" + ConsoleColors.CYAN + "TempFly Migrator" + ConsoleColors.GRAY + "] " + ConsoleColors.YELLOW + "No players found to migrate." + ConsoleColors.RESET);
            return;
        }

        AtomicInteger migratedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(keys.size());

        for (String uuidStr : keys) {
            EXECUTOR.execute(() -> {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    boolean isFlying = isFlightEnabled(uuid);
                    double time = getTime(uuid);

                    final List<AccessPlayerDTO> records = requestHelper.select("fly", AccessPlayerDTO.class,
                            table -> table.where("uniqueId", uuid));
                    if (records.isEmpty()) {
                        requestHelper.insert("fly", table -> {
                            table.uuid("uniqueId", uuid).primary();
                            table.bool("isinFly", isFlying);
                            table.bigInt("FlyTimeRemaining", (long) time);
                        });
                    } else {
                        requestHelper.update("fly", table -> {
                            table.where("uniqueId", uuid);
                            table.bool("isinFly", isFlying);
                            table.bigInt("FlyTimeRemaining", (long) time);
                        });
                    }

                    plugin.getLogger().info(ConsoleColors.GRAY + "[" + ConsoleColors.CYAN + "TempFly Migrator" + ConsoleColors.GRAY + "] " +
                            ConsoleColors.YELLOW + uuid + " " + ConsoleColors.GREEN + "→ Time: " + ConsoleColors.CYAN + time + "s " +
                            ConsoleColors.GREEN + "| Flying: " + ConsoleColors.CYAN + isFlying + ConsoleColors.RESET);
                    migratedCount.incrementAndGet();

                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().warning(ConsoleColors.GRAY + "[" + ConsoleColors.CYAN + "TempFly Migrator" + ConsoleColors.GRAY + "] " +
                            ConsoleColors.RED + "Invalid UUID: " + uuidStr + ConsoleColors.RESET);
                } finally {
                    latch.countDown();
                }
            });
        }

        EXECUTOR.execute(() -> {
            try {
                latch.await();
                long end = System.currentTimeMillis();
                long duration = end - start;

                plugin.getLogger().info(ConsoleColors.GRAY + "[" + ConsoleColors.CYAN + "TempFly Migrator" + ConsoleColors.GRAY + "] " +
                        ConsoleColors.GREEN + "Migration completed successfully!" + ConsoleColors.RESET);
                plugin.getLogger().info(ConsoleColors.GRAY + "[" + ConsoleColors.CYAN + "TempFly Migrator" + ConsoleColors.GRAY + "] " +
                        ConsoleColors.GREEN + migratedCount.get() + " player(s) migrated." + ConsoleColors.RESET);
                plugin.getLogger().info(ConsoleColors.GRAY + "[" + ConsoleColors.CYAN + "TempFly Migrator" + ConsoleColors.GRAY + "] " +
                        ConsoleColors.GREEN + "Elapsed time: " + ConsoleColors.CYAN + (duration / 1000.0) + " seconds." + ConsoleColors.RESET);
                plugin.getLogger().info(ConsoleColors.GRAY + "[" + ConsoleColors.CYAN + "TempFly Migrator" + ConsoleColors.GRAY + "] " +
                        ConsoleColors.GOLD + "Please restart the server to apply all changes properly." + ConsoleColors.RESET);
            } catch (InterruptedException e) {
                plugin.getLogger().severe(ConsoleColors.GRAY + "[" + ConsoleColors.CYAN + "TempFly Migrator" + ConsoleColors.GRAY + "] " +
                        ConsoleColors.RED + "Migration was interrupted." + ConsoleColors.RESET);
            }
        });
    }


}
