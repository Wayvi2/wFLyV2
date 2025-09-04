package com.wayvi.wfly.wflyv2.api.storage;

import com.wayvi.wfly.wflyv2.storage.models.AccessPlayerDTO;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for managing fly time data and player flight status.
 * <p>
 * Implementations of this interface are responsible for saving,
 * retrieving, and updating flight-related data for players.
 */
public interface FlyTimeStorage {

    /**
     * Saves the flight data of a player synchronously.
     *
     * @param player the player whose data will be saved
     */
    void save(Player player);

    /**
     * Saves the flight data of a player asynchronously.
     *
     * @param player the player whose data will be saved
     * @return a CompletableFuture representing the asynchronous save operation
     */
    CompletableFuture<Void> saveAsync(Player player);

    /**
     * Updates or inserts the flight status of a player.
     *
     * @param player    the player whose flight status will be updated
     * @param isFlying  true if the player is flying, false otherwise
     */
    void upsertFlyStatus(Player player, boolean isFlying);

    /**
     * Retrieves the flight data of a player by UUID.
     *
     * @param uuid the UUID of the player
     * @return an AccessPlayerDTO containing the player's flight data, or null if not found
     */
    AccessPlayerDTO getPlayerFlyData(UUID uuid);

    /**
     * Creates a new entry for a player in the storage.
     *
     * @param uuid the UUID of the new player
     */
    void createNewPlayer(UUID uuid);

    /**
     * Selects all records from a given table and maps them to a list of DTOs.
     *
     * @param tableName the name of the table to query
     * @param clazz     the class type of the DTO
     * @return a list of AccessPlayerDTO objects
     */
    List<AccessPlayerDTO> selectAll(String tableName, Class<?> clazz);

    /**
     * Saves a player DTO to the storage.
     *
     * @param playerData the player data to save
     */
    void saveDTO(AccessPlayerDTO playerData);
}
