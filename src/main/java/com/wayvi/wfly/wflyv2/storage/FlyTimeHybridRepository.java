package com.wayvi.wfly.wflyv2.storage;

import com.wayvi.wfly.wflyv2.api.storage.FlyTimeStorage;
import com.wayvi.wfly.wflyv2.storage.models.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.storage.redis.FlyTimeRedisRepository;
import com.wayvi.wfly.wflyv2.storage.sql.FlyTimeRepository;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FlyTimeHybridRepository implements FlyTimeStorage {

    private final FlyTimeRepository mysqlRepository;
    private final FlyTimeRedisRepository redisRepository;
    private final boolean redisEnabled;
    private final boolean mysqlEnabled;

    public FlyTimeHybridRepository(FlyTimeRepository mysqlRepository, FlyTimeRedisRepository redisRepository,
                                   boolean mysqlEnabled, boolean redisEnabled) {
        this.mysqlRepository = mysqlRepository;
        this.redisRepository = redisRepository;
        this.mysqlEnabled = mysqlEnabled;
        this.redisEnabled = redisEnabled;
    }

    public void loadDataToRedis() {
        if (mysqlEnabled && redisEnabled) {
            List<AccessPlayerDTO> players = mysqlRepository.selectAll("fly", AccessPlayerDTO.class);
            for (AccessPlayerDTO player : players) {
                redisRepository.saveDTO(player);
            }
        }
    }

    public void flushRedisToMySQL() {
        if (mysqlEnabled && redisEnabled) {
            List<AccessPlayerDTO> players = redisRepository.selectAll("fly", AccessPlayerDTO.class);
            for (AccessPlayerDTO player : players) {
                mysqlRepository.saveDTO(player);
            }
        }
    }
    @Override
    public void save(Player player) {
        if (redisEnabled) {
            redisRepository.save(player);
        } else if (mysqlEnabled) {
            mysqlRepository.save(player);
        }
    }

    @Override
    public CompletableFuture<Void> saveAsync(Player player) {
        if (redisEnabled) {
            return redisRepository.saveAsync(player);
        } else if (mysqlEnabled) {
            return mysqlRepository.saveAsync(player);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void upsertFlyStatus(Player player, boolean isFlying) {
        if (redisEnabled) {
            redisRepository.upsertFlyStatus(player, isFlying);
        } else if (mysqlEnabled) {
            mysqlRepository.upsertFlyStatus(player, isFlying);
        }
    }

    @Override
    public AccessPlayerDTO getPlayerFlyData(UUID uuid) {
        if (redisEnabled) {
            return redisRepository.getPlayerFlyData(uuid);
        } else if (mysqlEnabled) {
            return mysqlRepository.getPlayerFlyData(uuid);
        }
        return new AccessPlayerDTO(uuid, false, 0);
    }

    @Override
    public void createNewPlayer(UUID uuid) {
        if (redisEnabled) {
            redisRepository.createNewPlayer(uuid);
        } else if (mysqlEnabled) {
            mysqlRepository.createNewPlayer(uuid);
        }
    }

    @Override
    public List<AccessPlayerDTO> selectAll(String tableName, Class<?> clazz) {
        if (redisEnabled) {
            return redisRepository.selectAll(tableName, clazz);
        } else if (mysqlEnabled) {
            return mysqlRepository.selectAll(tableName, clazz);
        }
        return new ArrayList<>();
    }

    @Override
    public void saveDTO(AccessPlayerDTO playerData) {
        if (redisEnabled) {
            redisRepository.saveDTO(playerData);
        } else if (mysqlEnabled) {
            mysqlRepository.saveDTO(playerData);
        }
    }
}
