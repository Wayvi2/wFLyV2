package com.wayvi.wfly.wflyv2.managers.fly;

import com.wayvi.wfly.wflyv2.api.TimeFlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;



/**
 * This class manages the fly time for players and coordinates the decrement of fly time, as well as handling the
 * fly status of players based on remaining time. It also manages saving and loading fly time to/from a database.
 */
public class WTimeFlyManager implements TimeFlyManager {

    private final RequestHelper requestHelper;
    private final ConfigUtil configUtil;
    private final Map<UUID, Integer> flyTimes = new ConcurrentHashMap<>();
    private Map<UUID, Boolean> isFlying = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> lastNotifiedTime = new ConcurrentHashMap<>();
    private final Set<UUID> needsUpdate = ConcurrentHashMap.newKeySet();
    private Map<UUID, Location> lastSafeLocation = new HashMap<>();
    private final Map<UUID, Long> lastMovementTime = new HashMap<>();
    private final Map<UUID, Location> lastLocations = new HashMap<>();

    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREADS);

    static int threads = Runtime.getRuntime().availableProcessors();
    public static ExecutorService sqlExecutor = Executors.newFixedThreadPool(threads);

    /**
     * Constructor to initialize the TimeFlyManager with plugin dependencies and load existing fly times from the database.
     *
     * @param requestHelper  Helper for database requests.
     * @param configUtil     Configuration utility for custom messages and settings.
     */
    public WTimeFlyManager(RequestHelper requestHelper, ConfigUtil configUtil) {
        this.requestHelper = requestHelper;
        this.configUtil = configUtil;
        loadFlyTimes();
        startDecrementTask();
    }



    /**
     * Loads fly time data from the database into memory.
     */
    public void loadFlyTimes() {
        List<AccessPlayerDTO> flyData = this.requestHelper.selectAll("fly", AccessPlayerDTO.class);
        for (AccessPlayerDTO accessPlayerDTO : flyData) {
            flyTimes.put(accessPlayerDTO.uniqueId(), accessPlayerDTO.FlyTimeRemaining());
            isFlying.put(accessPlayerDTO.uniqueId(), accessPlayerDTO.isinFly());
        }
    }

    @Override
    public void loadFlyTimesForPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        List<AccessPlayerDTO> flyData = this.requestHelper.selectAll("fly", AccessPlayerDTO.class);

        for (AccessPlayerDTO accessPlayerDTO : flyData) {
            if (accessPlayerDTO.uniqueId().equals(playerUUID)) {
                flyTimes.put(playerUUID, accessPlayerDTO.FlyTimeRemaining());
                isFlying.put(playerUUID, accessPlayerDTO.isinFly());
                break;
            }
        }
    }



    /**
     * Saves all updated fly times to the database periodically based on the configured delay.
     */
    @Override
    public void saveFlyTimes() {
        int seconds = configUtil.getCustomConfig().getInt("save-database-delay");
        Bukkit.getScheduler().runTaskTimer(WflyApi.get().getPlugin(), () -> {
            for (UUID playerUUID : needsUpdate) {
                saveFlyTimeOnDisableOnline();
            }
            needsUpdate.clear();
        }, 0L, 20L * seconds);
    }

    /**
     * Saves fly times to the database when the server is disabled.
     */
    @Override
    public void saveFlyTimeOnDisableOnline() {
        for (Map.Entry<UUID, Integer> entry : flyTimes.entrySet()) {
            saveInDbFlyTime(Bukkit.getPlayer(entry.getKey()));
        }
        WflyApi.get().getPlugin().getLogger().info("Fly time saved");
    }


    /**
     * Starts a task to decrement the fly time of players who are flying and have a time limit.
     */
    private void startDecrementTask() {
        Bukkit.getScheduler().runTaskTimer(WflyApi.get().getPlugin(), () -> {
            try {
                decrementTimeRemaining();
                manageCommandMessageOnTimeLeft();
            } catch (SQLException e) {
                WflyApi.get().getPlugin().getLogger().severe("Error managing fly time: " + e.getMessage());
            }

        }, 0, 20);
    }

    /**
     * Decrements the remaining fly time for each player and handles fly status accordingly.
     *
     * @throws SQLException If there is an issue with the database.
     */
    @Override
    public void decrementTimeRemaining() throws SQLException {
        String decrementMethod = configUtil.getCustomConfig().getString("fly-decrement-method", "PLAYER_FLY_MODE");
        for (UUID playerUUID : flyTimes.keySet()) {

            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null || !player.isOnline()) {
                continue;
            }

            if (isExemptFromFlyDecrement(player)) {
                continue;
            }

            int timeRemaining = flyTimes.getOrDefault(playerUUID, 0);
            boolean currentlyFlying = isFlying.getOrDefault(playerUUID, false);

            if (timeRemaining <= 0 && currentlyFlying) {
                handleFlyDeactivation(playerUUID, player);
                continue;
            }

            if (timeRemaining <= 0) {
                continue;
            }

            if (isExemptFromLastPosition(player) && configUtil.getCustomConfig().getBoolean("fly-decrement-disabled-by-static") && decrementMethod.equalsIgnoreCase("PLAYER_FLYING_MODE")) {
                continue;
            }

            decrementFlyTime(playerUUID, player, currentlyFlying);

        }
    }

    private boolean isExemptFromFlyDecrement(Player player) {
        return player.hasPermission(Permissions.INFINITE_FLY.getPermission()) || player.isOp()  || player.getGameMode() == GameMode.SPECTATOR ;
    }

    private void handleFlyDeactivation(UUID playerUUID, Player player) throws SQLException {
        WflyApi.get().getFlyManager().manageFly(playerUUID, false);
        isFlying.put(playerUUID, false);
        Location safeLocation = getSafeLocation(player);

        if (!safeLocation.equals(lastSafeLocation.get(playerUUID))) {
            ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.fly-deactivated"));
            if (player.getWorld().getEnvironment() != World.Environment.NETHER) {
                player.teleport(safeLocation);
                lastSafeLocation.put(playerUUID, safeLocation);
            }
        }
    }

    private void decrementFlyTime(UUID playerUUID, Player player, boolean currentlyFlying) {
        String decrementMethod = configUtil.getCustomConfig().getString("fly-decrement-method", "PLAYER_FLY_MODE");

        if (decrementMethod.equalsIgnoreCase("PLAYER_FLYING_MODE") && currentlyFlying && player.isFlying()) {
            decrementTimeForPlayer(playerUUID);
        } else if (!decrementMethod.equalsIgnoreCase("PLAYER_FLYING_MODE") && currentlyFlying) {
            decrementTimeForPlayer(playerUUID);
        }
    }

    private void decrementTimeForPlayer(UUID playerUUID) {
        int timeRemaining = flyTimes.getOrDefault(playerUUID, 0);
        timeRemaining--;
        flyTimes.put(playerUUID, timeRemaining);
        needsUpdate.add(playerUUID);
    }

    /**
     * Adds fly time to a player's remaining time.
     *
     * @param player The player to add time for.
     * @param time   The amount of time to add (in seconds).
     * @throws SQLException If there is an issue with the database.
     */
    @Override
    public void addFlytime(Player player, int time) throws SQLException {
        UUID playerUUID = player.getUniqueId();
        int newTime = flyTimes.getOrDefault(playerUUID, 0) + time;
        flyTimes.put(playerUUID, newTime);
        needsUpdate.add(playerUUID);
        saveInDbFlyTime(player);
    }

    /**
     * Removes fly time from a player's remaining time.
     *
     * @param sender The player removing time.
     * @param target The target player whose time is being removed.
     * @param time   The amount of time to remove (in seconds).
     * @return True if time was successfully removed, false otherwise.
     */
    @Override
    public boolean removeFlyTime(Player sender, Player target, int time) {
        UUID playerUUID = sender.getUniqueId();
        int currentFlyTime = getTimeRemaining(target);

        if (time > currentFlyTime) {
            ColorSupportUtil.sendColorFormat(target, configUtil.getCustomMessage().getString("message.fly-remove-too-high"));
            return false;
        }

        int newTime = flyTimes.getOrDefault(playerUUID, 0) - time;
        flyTimes.put(playerUUID, newTime);
        needsUpdate.add(playerUUID);
        saveInDbFlyTime(target);
        return true;

    }

    /**
     * Resets the fly time for a player to 0.
     *
     * @param player The player whose fly time is being reset.
     */
    @Override
    public void resetFlytime(Player player) {
        flyTimes.put(player.getUniqueId(), 0);
        needsUpdate.add(player.getUniqueId());
        saveInDbFlyTime(player);
    }

    /**
     * Manages and executes commands based on the remaining fly time for each player.
     *
     * @throws SQLException If there is an issue with the database.
     */
    public void manageCommandMessageOnTimeLeft() throws SQLException {
        FileConfiguration config = configUtil.getCustomMessage();
        ConfigurationSection conditionsSection = config.getConfigurationSection("commands-time-remaining");
        if (conditionsSection == null) {
            return;
        }

        Map<Integer, String> timeCommandMap = new HashMap<>();
        for (String key : conditionsSection.getKeys(false)) {
            try {
                int timeKey = Integer.parseInt(key);
                String command = conditionsSection.getString(key + ".commands");
                if (command != null) {
                    timeCommandMap.put(timeKey, command);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        for (Map.Entry<UUID, Integer> entry : flyTimes.entrySet()) {
            UUID playerUUID = entry.getKey();
            int playerTimeRemaining = entry.getValue();

            if (lastNotifiedTime.getOrDefault(playerUUID, -1) == playerTimeRemaining) {
                continue;
            }

            String command = timeCommandMap.get(playerTimeRemaining);
            if (command == null) {
                continue;
            }

            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null || !player.isOnline()) {
                continue;
            }

            if (player.hasPermission(Permissions.INFINITE_FLY.getPermission()) || player.isOp()) {
                continue;
            }

            lastNotifiedTime.put(playerUUID, playerTimeRemaining);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }
    }

    private boolean isExemptFromLastPosition(Player player) {
        if (player == null || !player.isOnline()) return true;

        UUID uuid = player.getUniqueId();
        boolean currentlyFlying = isFlying.getOrDefault(uuid, false);

        if (!currentlyFlying) {
            lastLocations.remove(uuid);
            lastMovementTime.remove(uuid);
            return false;
        }

        Location currentLocation = player.getLocation();
        Location lastLocation = lastLocations.get(uuid);

        long currentTime = System.currentTimeMillis();
        int delayMillis = configUtil.getCustomConfig().getInt("delay", 3000); // fallback 3000ms

        if (lastLocation == null || !locationsEqual(currentLocation, lastLocation)) {
            // Il a bougé → on met à jour
            lastLocations.put(uuid, currentLocation.clone());
            lastMovementTime.put(uuid, currentTime);
            return false;
        }

        // Pas bougé → on vérifie le temps écoulé
        long lastMoveTime = lastMovementTime.getOrDefault(uuid, currentTime);
        if ((currentTime - lastMoveTime) >= delayMillis) {
            return true; // immobile assez longtemps → exempté
        }

        return false;
    }



    private boolean locationsEqual(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }


    @Override
    public void saveInDbFlyTime(Player player) {
        if (player == null) return;
        EXECUTOR.execute(() -> {
            final UUID uuid = player.getUniqueId();
            final List<AccessPlayerDTO> records = requestHelper.select("fly", AccessPlayerDTO.class,
                    table -> table.where("uniqueId", uuid));

            if (records.isEmpty()) {
                requestHelper.insert("fly", table -> {
                    table.uuid("uniqueId", uuid).primary();
                    table.bool("isinFly", getIsFlying(uuid));
                    table.bigInt("FlyTimeRemaining", WflyApi.get().getTimeFlyManager().getTimeRemaining(player) );
                });
            } else {
                requestHelper.update("fly", table -> {
                    table.where("uniqueId", uuid);
                    table.bool("isinFly", getIsFlying(uuid));
                    table.bigInt("FlyTimeRemaining", WflyApi.get().getTimeFlyManager().getTimeRemaining(player) );
                });
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveInDbFlyTimeDisable(Player player) {
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            final UUID uuid = player.getUniqueId();
            final List<AccessPlayerDTO> records = requestHelper.select("fly", AccessPlayerDTO.class,
                    table -> table.where("uniqueId", uuid));

            if (records.isEmpty()) {
                requestHelper.insert("fly", table -> {
                    table.uuid("uniqueId", uuid).primary();
                    table.bool("isinFly", getIsFlying(uuid));
                    table.bigInt("FlyTimeRemaining", WflyApi.get().getTimeFlyManager().getTimeRemaining(player));
                });
            } else {
                requestHelper.update("fly", table -> {
                    table.where("uniqueId", uuid);
                    table.bool("isinFly", getIsFlying(uuid));
                    table.bigInt("FlyTimeRemaining", WflyApi.get().getTimeFlyManager().getTimeRemaining(player));
                });
            }
        }, EXECUTOR);
    }

    @Override
    public CompletableFuture<Void> saveFlyTimeOnDisable() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (UUID playerUUID : flyTimes.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                futures.add(saveInDbFlyTimeDisable(player));
            }
        }

        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return allDone;
    }
    /*
    @Override
    public void loadFromDbFlyTime(Player player) {
        if (player == null) return;

        final UUID uuid = player.getUniqueId();

        EXECUTOR.execute(() -> {
            final List<AccessPlayerDTO> records = requestHelper.select("fly", AccessPlayerDTO.class,
                    table -> table.where("uniqueId", uuid));

            if (!records.isEmpty()) {
                AccessPlayerDTO dto = records.get(0);
                flyTimes.put(uuid, dto.FlyTimeRemaining());
                isFlying.put(uuid, dto.isinFly());
            } else {
                flyTimes.put(uuid, 0);
                isFlying.put(uuid, false);
            }
        });
    }
    */




    /**
     * Gets the remaining fly time for a player.
     *
     * @param player The player to check.
     * @return The remaining fly time (in seconds).
     */
    @Override
    public int getTimeRemaining(Player player) {
        return flyTimes.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Updates the flying status of a player.
     *
     * @param playerUUID The player's UUID.
     * @param isFlying   Whether the player is currently flying.
     */
    @Override
    public void updateFlyStatus(UUID playerUUID, boolean isFlying) {
        this.isFlying.put(playerUUID, isFlying);
    }

    @Override
    public boolean getIsFlying(UUID playerUUID) {
        return this.isFlying.getOrDefault(playerUUID, false);
    }

    /**
     * Returns a safe location for the player to teleport to when their fly time expires.
     *
     * @param player The player to check.
     * @return A safe location for the player.
     */
    private Location getSafeLocation(Player player) {
        boolean tpOnFloorWhenFlyDisabled = configUtil.getCustomConfig().getBoolean("tp-on-floor-when-fly-disabled");
        if (!tpOnFloorWhenFlyDisabled) {
            return player.getLocation();
        }

        Location loc = player.getLocation();
        World world = player.getWorld();
        int y = loc.getBlockY();

        while (world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() == Material.AIR) {
            y--;
        }

        return new Location(world, loc.getX(), y + 1, loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public static void shutdownExecutor() {
        EXECUTOR.shutdownNow();
    }
}
