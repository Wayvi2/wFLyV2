package com.wayvi.wfly.wflyv2.api;

import com.wayvi.wfly.wflyv2.models.Condition;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Interface for managing conditions that determine whether players
 * are authorized to fly.
 * <p>
 * Handles loading conditions from configuration, evaluating placeholders,
 * checking permissions, providing safe locations, and executing commands
 * when players are not authorized to fly.
 */
public interface ConditionManager {

    /**
     * Checks if the specified player is authorized to fly based on all active conditions.
     *
     * @param player the player to evaluate
     * @return true if the player is authorized to fly, false otherwise
     */
    boolean isFlyAuthorized(Player player);

    /**
     * Loads all flight-related conditions from the configuration.
     * <p>
     * This should typically be called at plugin startup or on config reload.
     */
    void loadConditions();

    /**
     * Loads conditions from a specific configuration path.
     *
     * @param path the configuration section or path to read conditions from
     * @return a list of {@link Condition} objects loaded from the config
     */
    List<Condition> loadConditionsFromConfig(String path);

    /**
     * Checks whether a placeholder value for a specific condition
     * is valid for the given player.
     *
     * @param player the player whose placeholder is being evaluated
     * @param c      the condition containing the placeholder to check
     * @return true if the placeholder passes the condition, false otherwise
     */
    boolean checkPlaceholder(Player player, Condition c);

    /**
     * Logs an error message when a placeholder is invalid or cannot be resolved.
     *
     * @param placeholder the placeholder string that caused the error
     */
    void logPlaceholderError(String placeholder);

    /**
     * Performs a global check on all tracked players to determine if they
     * are still allowed to fly. Players who no longer meet the conditions
     * may have their flight disabled.
     */
    void checkCanFly();

    /**
     * Returns a safe location for the player to be teleported if flight is disabled
     * in their current location (e.g., if they're midair and no longer authorized).
     *
     * @param player the player who needs a safe location
     * @return a safe {@link Location} where the player can be moved
     */
    Location getSafeLocation(Player player);

    /**
     * Resets any placeholders that are no longer registered or valid.
     * <p>
     * Useful when plugins providing placeholders are reloaded or removed.
     */
    void resetUnregisteredPlaceholders();

    /**
     * Checks if the player has a bypass permission, allowing them to fly
     * regardless of configured conditions.
     *
     * @param player the player to check
     * @return true if the player has bypass permission, false otherwise
     */
    boolean hasBypassPermission(Player player);

    /**
     * Executes predefined commands when a player is not authorized to fly.
     *
     * @param player the player for whom the commands will be executed
     */
    void executeNotAuthorizedCommands(Player player);
}
