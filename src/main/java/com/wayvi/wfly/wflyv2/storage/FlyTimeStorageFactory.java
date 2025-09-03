package com.wayvi.wfly.wflyv2.storage;

import com.wayvi.wfly.wflyv2.api.storage.FlyTimeStorage;
import com.wayvi.wfly.wflyv2.services.DatabaseService;
import com.wayvi.wfly.wflyv2.storage.redis.FlyTimeRedisRepository;
import com.wayvi.wfly.wflyv2.storage.sql.FlyTimeRepository;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.ExecutorService;

public class FlyTimeStorageFactory {

    private static FlyTimeHybridRepository hybridRepository;

    public static FlyTimeStorage create(JavaPlugin plugin, RequestHelper requestHelper, ExecutorService executor,
                                        boolean mysqlEnabled, boolean redisEnabled) {

        FlyTimeRepository mysqlRepo = new FlyTimeRepository(requestHelper, executor);
        FlyTimeRedisRepository redisRepo = createRedisRepository(plugin, executor);
        hybridRepository = new FlyTimeHybridRepository(mysqlRepo, redisRepo, true, true);
        if (mysqlEnabled && redisEnabled) {

            hybridRepository = new FlyTimeHybridRepository(mysqlRepo, redisRepo, true, true);
            hybridRepository.loadDataToRedis();
            return hybridRepository;

        } else if (redisEnabled) {
            return createRedisRepository(plugin, executor);
        } else if (mysqlEnabled) {
            return new FlyTimeRepository(requestHelper, executor);
        }
        return new FlyTimeRepository(requestHelper, executor);
    }

    private static FlyTimeRedisRepository createRedisRepository(JavaPlugin plugin, ExecutorService executor) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(plugin.getConfig().getInt("redis.pool.maxTotal", 8));
        poolConfig.setMaxIdle(plugin.getConfig().getInt("redis.pool.maxIdle", 8));
        poolConfig.setMinIdle(plugin.getConfig().getInt("redis.pool.minIdle", 0));

        String host = plugin.getConfig().getString("redis.host");
        int port = plugin.getConfig().getInt("redis.port");
        int timeout = plugin.getConfig().getInt("redis.timeout");
        int database = plugin.getConfig().getInt("redis.database");
        String password = plugin.getConfig().getString("redis.password");

        JedisPool jedisPool;
        if (password != null && !password.isEmpty()) {
            jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
        } else {
            jedisPool = new JedisPool(poolConfig, host, port, timeout, null, database);
        }

        return new FlyTimeRedisRepository(jedisPool, executor);
    }

    public static void saveDisableWithRedisAndMysql(FlyTimeStorage storage) {
        if (storage instanceof FlyTimeHybridRepository) {
            FlyTimeHybridRepository hybridRepo = (FlyTimeHybridRepository) storage;
            hybridRepo.flushRedisToMySQL();
        }
    }

    public static FlyTimeHybridRepository getFlyTimeHybridRepository() {
        return hybridRepository;
    }
}

