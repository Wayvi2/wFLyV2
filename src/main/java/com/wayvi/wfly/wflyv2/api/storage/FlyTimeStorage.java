package com.wayvi.wfly.wflyv2.api.storage;

import com.wayvi.wfly.wflyv2.storage.models.AccessPlayerDTO;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface FlyTimeStorage {

    void save(Player player);

    CompletableFuture<Void> saveAsync(Player player);

    void upsertFlyStatus(Player player, boolean isFlying);

    AccessPlayerDTO getPlayerFlyData(UUID uuid);

    void createNewPlayer(UUID uuid);

    List<AccessPlayerDTO> selectAll(String tableName, Class<?> clazz);

    void saveDTO(AccessPlayerDTO playerData);

}
