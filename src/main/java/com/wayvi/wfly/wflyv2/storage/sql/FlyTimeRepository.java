package com.wayvi.wfly.wflyv2.storage.sql;

import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.api.storage.FlyTimeStorage;
import com.wayvi.wfly.wflyv2.storage.models.AccessPlayerDTO;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class FlyTimeRepository implements FlyTimeStorage {

    private final RequestHelper requestHelper;
    private final ExecutorService executor;

    public FlyTimeRepository(RequestHelper requestHelper, ExecutorService executor) {
        this.requestHelper = requestHelper;
        this.executor = executor;
    }

    @Override
    public void save(Player player) {
        if (player == null) return;

        UUID uuid = player.getUniqueId();
        List<AccessPlayerDTO> records = requestHelper.select("fly", AccessPlayerDTO.class,
                table -> table.where("uniqueId", uuid));

        int flyTime = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);
        boolean isFlying = WflyApi.get().getTimeFlyManager().getIsFlying(uuid);

        if (records.isEmpty()) {
            requestHelper.insert("fly", table -> {
                table.uuid("uniqueId", uuid).primary();
                table.bool("isinFly", isFlying);
                table.bigInt("FlyTimeRemaining", flyTime);
            });
        } else {
            requestHelper.update("fly", table -> {
                table.where("uniqueId", uuid);
                table.bool("isinFly", isFlying);
                table.bigInt("FlyTimeRemaining", flyTime);
            });
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
            final UUID uuid = player.getUniqueId();
            final List<AccessPlayerDTO> records = requestHelper.select("fly", AccessPlayerDTO.class,
                    table -> table.where("uniqueId", uuid));

            if (records.isEmpty()) {
                requestHelper.insert("fly", table -> {
                    table.uuid("uniqueId", uuid).primary();
                    table.bool("isinFly", isFlying);
                    table.bigInt("FlyTimeRemaining", WflyApi.get().getTimeFlyManager().getTimeRemaining(player));
                });
            } else {
                requestHelper.update("fly", table -> {
                    table.where("uniqueId", uuid);
                    table.bool("isinFly", isFlying);
                    table.bigInt("FlyTimeRemaining", WflyApi.get().getTimeFlyManager().getTimeRemaining(player));
                });
            }
        });
    }

    /**
     * Retrieves flight-related data for a specific player from the database.
     *
     * @param uuid the UUID of the player
     * @return an {@link AccessPlayerDTO} containing player flight data
     */
    @Override
    public AccessPlayerDTO getPlayerFlyData(final UUID uuid) {
        final List<AccessPlayerDTO> records = requestHelper.select("fly", AccessPlayerDTO.class,
                table -> table.where("uniqueId", uuid));

        return records.isEmpty() ? new AccessPlayerDTO(uuid, false, 0) : records.get(0);
    }

    /**
     * Creates a new database entry for a player when they are first seen.
     *
     * @param uuid the UUID of the new player
     */
    public void createNewPlayer(final UUID uuid) {
        requestHelper.insert("fly", table -> {
            table.uuid("uniqueId", uuid).primary();
            table.bool("isinFly", false);
            table.bigInt("FlyTimeRemaining", 0);
        });
    }


    @Override
    public List<AccessPlayerDTO> selectAll(String tableName, Class<?> clazz){
        return this.requestHelper.selectAll(tableName, AccessPlayerDTO.class);
    }

    @Override
    public void saveDTO(AccessPlayerDTO playerData) {
        List<AccessPlayerDTO> records = requestHelper.select("fly", AccessPlayerDTO.class,
                table -> table.where("uniqueId", playerData.uniqueId()));

        if (records.isEmpty()) {
            requestHelper.insert("fly", table -> {
                table.uuid("uniqueId", playerData.uniqueId()).primary();
                table.bool("isinFly", playerData.isinFly());
                table.bigInt("FlyTimeRemaining", playerData.FlyTimeRemaining());
            });
        } else {
            requestHelper.update("fly", table -> {
                table.where("uniqueId", playerData.uniqueId());
                table.bool("isinFly", playerData.isinFly());
                table.bigInt("FlyTimeRemaining", playerData.FlyTimeRemaining());
            });
        }
    }


}

