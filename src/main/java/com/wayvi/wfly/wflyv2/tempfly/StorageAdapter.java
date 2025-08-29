package com.wayvi.wfly.wflyv2.tempfly;

import com.wayvi.wfly.wflyv2.util.ConsoleColorsUtil;
import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.storage.models.AccessPlayerDTO;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

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
        plugin.getLogger().info(ConsoleColorsUtil.GRAY + "[" + ConsoleColorsUtil.CYAN + "TempFly Migrator" + ConsoleColorsUtil.GRAY + "] " + ConsoleColorsUtil.GREEN + "Starting migration..." + ConsoleColorsUtil.RESET);
        long start = System.currentTimeMillis();

        if (!data.contains("players")) {
            plugin.getLogger().warning(ConsoleColorsUtil.GRAY + "[" + ConsoleColorsUtil.CYAN + "TempFly Migrator" + ConsoleColorsUtil.GRAY + "] " + ConsoleColorsUtil.RED + "No 'players' section found in TempFly's data.yml!" + ConsoleColorsUtil.RESET);
            return;
        }

        Set<String> keys = data.getConfigurationSection("players").getKeys(false);
        if (keys.isEmpty()) {
            plugin.getLogger().info(ConsoleColorsUtil.GRAY + "[" + ConsoleColorsUtil.CYAN + "TempFly Migrator" + ConsoleColorsUtil.GRAY + "] " + ConsoleColorsUtil.YELLOW + "No players found to migrate." + ConsoleColorsUtil.RESET);
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

                    plugin.getLogger().info(ConsoleColorsUtil.GRAY + "[" + ConsoleColorsUtil.CYAN + "TempFly Migrator" + ConsoleColorsUtil.GRAY + "] " +
                            ConsoleColorsUtil.YELLOW + uuid + " " + ConsoleColorsUtil.GREEN + "→ Time: " + ConsoleColorsUtil.CYAN + time + "s " +
                            ConsoleColorsUtil.GREEN + "| Flying: " + ConsoleColorsUtil.CYAN + isFlying + ConsoleColorsUtil.RESET);
                    migratedCount.incrementAndGet();

                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().warning(ConsoleColorsUtil.GRAY + "[" + ConsoleColorsUtil.CYAN + "TempFly Migrator" + ConsoleColorsUtil.GRAY + "] " +
                            ConsoleColorsUtil.RED + "Invalid UUID: " + uuidStr + ConsoleColorsUtil.RESET);
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

                plugin.getLogger().info(ConsoleColorsUtil.GRAY + "[" + ConsoleColorsUtil.CYAN + "TempFly Migrator" + ConsoleColorsUtil.GRAY + "] " +
                        ConsoleColorsUtil.GREEN + "Migration completed successfully!" + ConsoleColorsUtil.RESET);
                plugin.getLogger().info(ConsoleColorsUtil.GRAY + "[" + ConsoleColorsUtil.CYAN + "TempFly Migrator" + ConsoleColorsUtil.GRAY + "] " +
                        ConsoleColorsUtil.GREEN + migratedCount.get() + " player(s) migrated." + ConsoleColorsUtil.RESET);
                plugin.getLogger().info(ConsoleColorsUtil.GRAY + "[" + ConsoleColorsUtil.CYAN + "TempFly Migrator" + ConsoleColorsUtil.GRAY + "] " +
                        ConsoleColorsUtil.GREEN + "Elapsed time: " + ConsoleColorsUtil.CYAN + (duration / 1000.0) + " seconds." + ConsoleColorsUtil.RESET);
                plugin.getLogger().info(ConsoleColorsUtil.GRAY + "[" + ConsoleColorsUtil.CYAN + "TempFly Migrator" + ConsoleColorsUtil.GRAY + "] " +
                        ConsoleColorsUtil.GOLD + "Please restart the server to apply all changes properly." + ConsoleColorsUtil.RESET);
            } catch (InterruptedException e) {
                plugin.getLogger().severe(ConsoleColorsUtil.GRAY + "[" + ConsoleColorsUtil.CYAN + "TempFly Migrator" + ConsoleColorsUtil.GRAY + "] " +
                        ConsoleColorsUtil.RED + "Migration was interrupted." + ConsoleColorsUtil.RESET);
            }
        });
    }


}
