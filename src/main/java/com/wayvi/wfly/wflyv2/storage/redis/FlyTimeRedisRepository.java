package com.wayvi.wfly.wflyv2.storage.redis;

import com.google.gson.Gson;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.api.storage.FlyTimeStorage;
import com.wayvi.wfly.wflyv2.storage.models.AccessPlayerDTO;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlyTimeRedisRepository implements FlyTimeStorage {

    private static final String FLY_KEY_PREFIX = "fly:player:";
    private static final String FLY_ALL_PLAYERS = "fly:all_players";
    private static final String SERVER_SHUTDOWN_KEY = "server:shutdown_time";

    private final JedisPool jedisPool;
    private final ExecutorService executor;
    private final Gson gson;
    private final Logger logger;

    public FlyTimeRedisRepository(JedisPool jedisPool, ExecutorService executor) {
        this.jedisPool = jedisPool;
        this.executor = executor;
        this.gson = new Gson();
        this.logger = Logger.getLogger(FlyTimeRedisRepository.class.getName());
    }

    @Override
    public void save(Player player) {
        if (player == null) return;

        UUID uuid = player.getUniqueId();
        int flyTime = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);
        boolean isFlying = WflyApi.get().getTimeFlyManager().getIsFlying(uuid);
        long lastUpdate = WflyApi.get().getFlyTimeSynchronizer().getLastUpdate(uuid);

        try (Jedis jedis = jedisPool.getResource()) {
            AccessPlayerDTO playerData = new AccessPlayerDTO(uuid, isFlying, flyTime, lastUpdate);
            String key = FLY_KEY_PREFIX + uuid.toString();
            String jsonData = gson.toJson(playerData);

            jedis.hset(key, "data", jsonData);
            jedis.sadd(FLY_ALL_PLAYERS, uuid.toString());

        } catch (JedisException e) {
            logger.log(Level.SEVERE, "Error saving Redis for player " + uuid, e);
        }
    }

    @Override
    public CompletableFuture<Void> saveAsync(Player player) {
        if (player == null) return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> save(player), executor);
    }

    @Override
    public void upsertFlyStatus(final Player player, final boolean isFlying) {
        executor.execute(() -> {
            if (player == null) return;

            final UUID uuid = player.getUniqueId();
            final int flyTime = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);
            final long lastUpdate = WflyApi.get().getFlyTimeSynchronizer().getLastUpdate(uuid);

            try (Jedis jedis = jedisPool.getResource()) {
                AccessPlayerDTO playerData = new AccessPlayerDTO(uuid, isFlying, flyTime, lastUpdate);
                String key = FLY_KEY_PREFIX + uuid.toString();
                String jsonData = gson.toJson(playerData);

                jedis.hset(key, "data", jsonData);
                jedis.sadd(FLY_ALL_PLAYERS, uuid.toString());

            } catch (JedisException e) {
                logger.log(Level.SEVERE, "Error updating Redis for player" + uuid, e);
            }
        });
    }


    @Override
    public AccessPlayerDTO getPlayerFlyData(final UUID uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = FLY_KEY_PREFIX + uuid.toString();
            String jsonData = jedis.hget(key, "data");

            if (jsonData == null) {
                return new AccessPlayerDTO(uuid, false, 0,0);
            }

            return gson.fromJson(jsonData, AccessPlayerDTO.class);

        } catch (JedisException e) {
            logger.log(Level.SEVERE, "Error fetching Redis for player" + uuid, e);
            return new AccessPlayerDTO(uuid, false, 0,0);
        }
    }

    @Override
    public void createNewPlayer(final UUID uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            AccessPlayerDTO playerData = new AccessPlayerDTO(uuid, false, 0,0);
            String key = FLY_KEY_PREFIX + uuid.toString();
            String jsonData = gson.toJson(playerData);

            jedis.hset(key, "data", jsonData);
            jedis.sadd(FLY_ALL_PLAYERS, uuid.toString());

        } catch (JedisException e) {
            logger.log(Level.SEVERE, "Error creating new Redis player " + uuid, e);
        }
    }

    @Override
    public List<AccessPlayerDTO> selectAll(String tableName, Class<?> clazz) {
        List<AccessPlayerDTO> players = new ArrayList<>();

        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> playerUuids = jedis.smembers(FLY_ALL_PLAYERS);

            for (String uuidString : playerUuids) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    AccessPlayerDTO playerData = getPlayerFlyData(uuid);
                    if (playerData != null) {
                        players.add(playerData);
                    }
                } catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING, "Invalid UUID found in Redis: " + uuidString, e);
                }
            }

        } catch (JedisException e) {
            logger.log(Level.SEVERE, "Error retrieving all Redis players", e);
        }

        return players;
    }

    @Override
    public void saveDTO(AccessPlayerDTO playerData) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = FLY_KEY_PREFIX + playerData.uniqueId().toString();
            String jsonData = gson.toJson(playerData);

            jedis.hset(key, "data", jsonData);
            jedis.sadd(FLY_ALL_PLAYERS, playerData.uniqueId().toString());
        } catch (JedisException e) {
            logger.log(Level.SEVERE, "Error saving Redis for player " + playerData.uniqueId(), e);
        }
    }

    @Override
    public void saveTimeOffOnDisable() {
        long now = System.currentTimeMillis();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(SERVER_SHUTDOWN_KEY, String.valueOf(now));
        } catch (JedisException e) {
            logger.log(Level.SEVERE, "Error saving server shutdown time to Redis", e);
        }
    }

    @Override
    public long getTimeOffOnDisable() {
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(SERVER_SHUTDOWN_KEY);
            if (value == null) {
                return 0L;
            }
            return Long.parseLong(value);

        } catch (NumberFormatException e) {

            logger.log(Level.WARNING, "Invalid shutdown time format in Redis for key " + SERVER_SHUTDOWN_KEY, e);
        } catch (JedisException e) {
            logger.log(Level.SEVERE, "Error getting server shutdown time from Redis", e);
        }
        return 0L;
    }



}
