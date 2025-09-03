package com.wayvi.wfly.wflyv2.managers.fly;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.TimeFlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.storage.models.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/**
 * This class manages the fly time for players and coordinates the decrement of fly time,
 * handles fly status, and manages saving/loading fly time from Database.
 */
public class WTimeFlyManager implements TimeFlyManager {

    // ---------- FIELDS ----------
    private final WFlyV2 plugin;

    private final Map<UUID, Integer> flyTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> isFlying = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> lastNotifiedTime = new ConcurrentHashMap<>();
    private final Set<UUID> needsUpdate = ConcurrentHashMap.newKeySet();

    private final Map<UUID, Long> lastMovementTime = new HashMap<>();
    private final Map<UUID, Location> lastLocations = new HashMap<>();

    private BukkitTask saveTask;

    // ---------- CONSTRUCTOR & INIT ----------
    /**
     * Constructor to initialize the TimeFlyManager with dependencies and load fly times.
     */
    public WTimeFlyManager(WFlyV2 plugin) {
        this.plugin = plugin;
        loadFlyTimesForOnlinePlayers();
        startDecrementTask();
    }


    private void loadFlyTimesForOnlinePlayers() {

        List<AccessPlayerDTO> flyData = plugin.getStorage().selectAll("fly", AccessPlayerDTO.class);


        Set<UUID> onlinePlayerUUIDs = Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());

        int loadedCount = 0;
        for (AccessPlayerDTO accessPlayerDTO : flyData) {
            UUID playerUUID = accessPlayerDTO.uniqueId();

            if (onlinePlayerUUIDs.contains(playerUUID)) {
                flyTimes.put(playerUUID, accessPlayerDTO.FlyTimeRemaining());
                isFlying.put(playerUUID, accessPlayerDTO.isinFly());
                loadedCount++;
            }
        }

        plugin.getLogger().info("Data loaded for " + loadedCount + " online players");
    }


    @Override
    public void loadFlyTimesForPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        AccessPlayerDTO accessPlayerDTO = plugin.getStorage().getPlayerFlyData(player.getUniqueId());
            flyTimes.put(playerUUID, accessPlayerDTO.FlyTimeRemaining());
            isFlying.put(playerUUID, accessPlayerDTO.isinFly());
    }

    // ---------- PUBLIC MAIN METHODS ----------

    @Override
    public void saveFlyTimes() {
        if (saveTask != null) {
            saveTask.cancel();
        }

        int seconds = plugin.getConfigFile().get(ConfigEnum.SAVE_DATABASE_DELAY);
        saveTask = Bukkit.getScheduler().runTaskTimer(WflyApi.get().getPlugin(), () -> {
            if (!needsUpdate.isEmpty()) {
                for (UUID playerUUID : new HashSet<>(needsUpdate)) {
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player != null) {
                        plugin.getStorage().save(player);
                    }
                }
                plugin.getLogger().info("Save " + needsUpdate.size() + " player");
                needsUpdate.clear();
            }
        }, 0L, 20L * seconds);
    }

    @Override
    public void decrementTimeRemaining() throws SQLException {
        String decrementMethod = plugin.getConfigFile().get(ConfigEnum.FLY_DECREMENT_METHOD);
        boolean flyDecrementStatic = plugin.getConfigFile().get(ConfigEnum.FLY_DECREMENT_DISABLED_BY_STATIC);

        List<Player> validPlayers = flyTimes.keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .collect(Collectors.toList());

        for (Player player : validPlayers) {

            if (isExemptFromFlyDecrement(player)) continue;

            int timeRemaining = flyTimes.getOrDefault(player.getUniqueId(), 0);
            boolean currentlyFlying = isFlying.getOrDefault(player.getUniqueId(), false);

            if (timeRemaining <= 0 && currentlyFlying) {
                handleFlyDeactivation(player.getUniqueId(), player);
                continue;
            }

            if (timeRemaining <= 0) continue;


            if (isExemptFromLastPosition(player)
                    && flyDecrementStatic
                    && decrementMethod.equalsIgnoreCase("PLAYER_FLYING_MODE")) {
                continue;
            }

            if (getDecrementationDisable(player)) {
                continue;
            }

            decrementFlyTime(player.getUniqueId(), player, currentlyFlying);
        }
    }

    @Override
    public void addFlytime(Player player, int time) {
        UUID playerUUID = player.getUniqueId();
        int newTime = flyTimes.getOrDefault(playerUUID, 0) + time;
        flyTimes.put(playerUUID, newTime);
        needsUpdate.add(playerUUID);
    }

    @Override
    public boolean removeFlyTime( Player target, int time) {

        int currentFlyTime = getTimeRemaining(target);

        if (time > currentFlyTime) {
            ColorSupportUtil.sendColorFormat(target, plugin.getMessageFile().get(MessageEnum.FLY_REMOVE_TOO_HIGH));
            return false;
        }

        UUID targetUUID = target.getUniqueId();
        int newTime = flyTimes.getOrDefault(targetUUID, 0) - time;
        flyTimes.put(targetUUID, newTime);
        needsUpdate.add(targetUUID);

        return true;
    }


    @Override
    public void resetFlytime(Player player) {
        flyTimes.put(player.getUniqueId(), 0);
        needsUpdate.add(player.getUniqueId());
    }

    // ---------- ADD/REMOVE FOR ALL PLAYERS -----------

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
    public CompletableFuture<Void> saveFlyTimeOnDisable() {


        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (UUID playerUUID : flyTimes.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                futures.add(plugin.getStorage().saveAsync(player));
            }
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }


    // ---------- decrementation-disable-by-condition ----------

    private boolean getDecrementationDisable(Player player) {
        List<Map<?, ?>> conditions = plugin.getConfigFile().get(ConfigEnum.DECREMENTATION_DISABLE_BY_CONDITION);

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
        Location safeLocation = WflyApi.get().getConditionManager().getSafeLocation(player);
        player.teleport(safeLocation);
    }

    private void decrementFlyTime(UUID playerUUID, Player player, boolean currentlyFlying) {
        String decrementMethod = plugin.getConfigFile().get(ConfigEnum.FLY_DECREMENT_METHOD);

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
        ConfigurationSection conditionsSection = plugin.getMessageFile().getRaw().getConfigurationSection("commands-time-remaining");
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
        int delayMillis = plugin.getConfigFile().get(ConfigEnum.DELAY);

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



    private void startDecrementTask() {

        Bukkit.getScheduler().runTaskTimer(WflyApi.get().getPlugin(), () -> {

            try {
                manageCommandMessageOnTimeLeft();
                decrementTimeRemaining();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }, 0L, 20);
    }

    // ---------- HELPER / UTILITY METHODS ----------

}
