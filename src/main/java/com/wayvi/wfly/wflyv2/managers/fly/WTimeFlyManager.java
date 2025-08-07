package com.wayvi.wfly.wflyv2.managers.fly;

import com.wayvi.wfly.wflyv2.api.TimeFlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import org.bukkit.command.CommandSender;
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
 * This class manages the fly time for players and coordinates the decrement of fly time,
 * handles fly status, and manages saving/loading fly time from database.
 */
public class WTimeFlyManager implements TimeFlyManager {

    // ---------- FIELDS ----------
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

    // ---------- CONSTRUCTOR & INIT ----------
    /**
     * Constructor to initialize the TimeFlyManager with dependencies and load fly times.
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

    // ---------- PUBLIC MAIN METHODS ----------

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

    @Override
    public void saveFlyTimeOnDisableOnline() {
        for (Map.Entry<UUID, Integer> entry : flyTimes.entrySet()) {
            saveInDbFlyTime(Bukkit.getPlayer(entry.getKey()));
        }
        WflyApi.get().getPlugin().getLogger().info("Fly time saved");
    }

    @Override
    public void decrementTimeRemaining() throws SQLException {
        String decrementMethod = configUtil.getCustomConfig().getString("fly-decrement-method", "PLAYER_FLY_MODE");
        for (UUID playerUUID : flyTimes.keySet()) {

            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null || !player.isOnline()) continue;
            if (isExemptFromFlyDecrement(player)) continue;

            int timeRemaining = flyTimes.getOrDefault(playerUUID, 0);
            boolean currentlyFlying = isFlying.getOrDefault(playerUUID, false);

            if (timeRemaining <= 0 && currentlyFlying) {
                handleFlyDeactivation(playerUUID, player);
                continue;
            }

            if (timeRemaining <= 0) continue;

            if (isExemptFromLastPosition(player)
                    && configUtil.getCustomConfig().getBoolean("fly-decrement-disabled-by-static")
                    && decrementMethod.equalsIgnoreCase("PLAYER_FLYING_MODE")) {
                continue;
            }

            if (getDecrementationDisable(player)) {
                continue;
            }

            decrementFlyTime(playerUUID, player, currentlyFlying);
        }
    }

    @Override
    public void addFlytime(Player player, int time) {
        UUID playerUUID = player.getUniqueId();
        int newTime = flyTimes.getOrDefault(playerUUID, 0) + time;
        flyTimes.put(playerUUID, newTime);
        needsUpdate.add(playerUUID);
        saveInDbFlyTime(player);
    }

    @Override
    public boolean removeFlyTime( Player target, int time) {
        UUID senderUUID = null;


        int currentFlyTime = getTimeRemaining(target);

        if (time > currentFlyTime) {
            ColorSupportUtil.sendColorFormat(target, configUtil.getCustomMessage().getString("message.fly-remove-too-high"));
            return false;
        }

        if (senderUUID != null) {
            int newTime = flyTimes.getOrDefault(senderUUID, 0) - time;
            flyTimes.put(senderUUID, newTime);
            needsUpdate.add(senderUUID);
        } else {
            UUID targetUUID = target.getUniqueId();
            int newTime = flyTimes.getOrDefault(targetUUID, 0) - time;
            flyTimes.put(targetUUID, newTime);
            needsUpdate.add(targetUUID);
        }

        saveInDbFlyTime(target);
        return true;
    }


    @Override
    public void resetFlytime(Player player) {
        flyTimes.put(player.getUniqueId(), 0);
        needsUpdate.add(player.getUniqueId());
        saveInDbFlyTime(player);
    }

    // ---------- ADD/REMOVE/RESET FOR ALL PLAYERS -----------
    @Override
    public void resetFlytimeForAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            resetFlytime(player);
        }
    }
    @Override
    public void addFlytimeForAllPlayers(int time) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            addFlytime(player, time);
        }
    }
    @Override
    public void removeFlytimeForAllPlayers(int time) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeFlyTime(player, time);
        }
    }

    @Override
    public int getTimeRemaining(Player player) {
        return flyTimes.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void updateFlyStatus(UUID playerUUID, boolean isFlying) {
        this.isFlying.put(playerUUID, isFlying);
    }

    @Override
    public boolean getIsFlying(UUID playerUUID) {
        return this.isFlying.getOrDefault(playerUUID, false);
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
                    table.bigInt("FlyTimeRemaining", WflyApi.get().getTimeFlyManager().getTimeRemaining(player));
                });
            } else {
                requestHelper.update("fly", table -> {
                    table.where("uniqueId", uuid);
                    table.bool("isinFly", getIsFlying(uuid));
                    table.bigInt("FlyTimeRemaining", WflyApi.get().getTimeFlyManager().getTimeRemaining(player));
                });
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveInDbFlyTimeDisable(Player player) {
        if (player == null) return CompletableFuture.completedFuture(null);

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

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }


    // ---------- decrementation-disable-by-condition ----------

    private boolean getDecrementationDisable(Player player) {
        List<Map<?, ?>> conditions = configUtil.getCustomConfig().getMapList("decrementation-disable-by-condition");

        for (Map<?, ?> entry : conditions) {
            String condition = (String) entry.get("condition");
            if (condition == null || !condition.contains("=")) continue;

            String[] parts = condition.split("=", 2);
            if (parts.length != 2) continue;

            String left = parts[0].trim();
            String right = parts[1].trim();

            left = applyPlaceholders(player, left);
            right = applyPlaceholders(player, right);

            if (left.equalsIgnoreCase(right)) {
                return true;
            }
        }

        return false;
    }





    // ---------- INTERNAL LOGIC HANDLERS ----------
    private String applyPlaceholders(Player player, String text) {
        if (text == null) return "";
            try {
                return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
                return text;
            }
    }



    private boolean isExemptFromFlyDecrement(Player player) {
        return player.hasPermission(Permissions.INFINITE_FLY.getPermission()) || player.isOp() || player.getGameMode() == GameMode.SPECTATOR;
    }

    private void handleFlyDeactivation(UUID playerUUID, Player player) {
        WflyApi.get().getFlyManager().manageFly(playerUUID, false);
        isFlying.put(playerUUID, false);
        Location safeLocation = getSafeLocation(player);
        player.teleport(safeLocation);
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

    public void manageCommandMessageOnTimeLeft() throws SQLException {
        FileConfiguration config = configUtil.getCustomMessage();
        ConfigurationSection conditionsSection = config.getConfigurationSection("commands-time-remaining");
        if (conditionsSection == null) return;

        Map<Integer, List<String>> timeCommandMap = new HashMap<>();

        for (String key : conditionsSection.getKeys(false)) {
            try {
                int timeKey = Integer.parseInt(key);
                Object value = conditionsSection.get(key + ".commands");

                if (value instanceof String) {
                    timeCommandMap.put(timeKey, Collections.singletonList((String) value));
                } else if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> commands = (List<String>) value;
                    timeCommandMap.put(timeKey, commands);
                }
            } catch (NumberFormatException ignored) {}
        }

        for (Map.Entry<UUID, Integer> entry : flyTimes.entrySet()) {
            UUID playerUUID = entry.getKey();
            int playerTimeRemaining = entry.getValue();

            if (lastNotifiedTime.getOrDefault(playerUUID, -1) == playerTimeRemaining) continue;

            List<String> commands = timeCommandMap.get(playerTimeRemaining);
            if (commands == null) continue;

            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null || !player.isOnline()) continue;

            if (player.hasPermission(Permissions.INFINITE_FLY.getPermission()) || player.isOp()) continue;

            lastNotifiedTime.put(playerUUID, playerTimeRemaining);

            for (String cmd : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }
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
        int delayMillis = configUtil.getCustomConfig().getInt("delay", 3000);

        if (lastLocation == null || !locationsEqual(currentLocation, lastLocation)) {
            lastLocations.put(uuid, currentLocation.clone());
            lastMovementTime.put(uuid, currentTime);
            return false;
        }

        long lastMoveTime = lastMovementTime.getOrDefault(uuid, currentTime);
        return (currentTime - lastMoveTime) >= delayMillis;
    }

    private boolean locationsEqual(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

    public Location getSafeLocation(Player player) {
        boolean tpOnFloor = configUtil.getCustomConfig().getBoolean("tp-on-floor-when-fly-disabled");
        if (!tpOnFloor) return player.getLocation();

        Location loc = player.getLocation();
        World world = player.getWorld();
        int y = loc.getBlockY();

        while (world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() == Material.AIR) {
            y--;
        }

        return new Location(world, loc.getX(), y + 1, loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    private void startDecrementTask() {
        int interval = configUtil.getCustomConfig().getInt("fly-decrement-interval", 20);
        Bukkit.getScheduler().runTaskTimer(WflyApi.get().getPlugin(), () -> {
            try {
                decrementTimeRemaining();
                manageCommandMessageOnTimeLeft();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, 0L, interval);
    }

    // ---------- HELPER / UTILITY METHODS ----------

    // No additional helper methods in current code.

}
