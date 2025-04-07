package com.wayvi.wfly.wflyv2.pluginhook.cluescroll;

import com.electro2560.dev.cluescrolls.api.*;
import com.wayvi.wfly.wflyv2.WFlyV2;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Handles the integration of fly-related clues in the FlyQuest system for the WFly plugin.
 * This class interacts with the ClueScrolls API to track players' flying status and update clues accordingly.
 */
public class FlyQuest {

    private final WFlyV2 plugin;

    /**
     * Constructs a new instance of FlyQuest.
     *
     * @param plugin the main WFlyV2 plugin instance, used for accessing plugin methods and configurations
     */
    public FlyQuest(WFlyV2 plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the custom fly clue for the ClueScrolls system.
     * This clue tracks players' flying status and updates their progress on the clue.
     * The clue is registered with the identifier "wfly_flytime_to_fly" and tracks integer data.
     * A scheduled task is used to check each player’s flying state and updates the clue accordingly.
     */
    public void initializeFlyClue() {

        // Register the custom clue with ClueScrolls API
        CustomClue flyClue = ClueScrollsAPI.getInstance().registerCustomClue(
                plugin,
                "wfly_flytime_to_fly",
                new ClueConfigData("wfly_flytime_to_fly", DataType.NUMBER_INTEGER)
        );

        // Initial clue data pair array (can be expanded if needed)
        final ClueDataPair[] clueData = new ClueDataPair[]{
                new ClueDataPair("wfly_flytime_to_fly", ""),
        };

        // Schedule a task to check each player’s fly status every 1 second (20 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Loop through all online players to check if they are flying
            for (Player player : Bukkit.getOnlinePlayers()) {
                // If the player is flying, update their flytime clue progress
                if (player.isFlying()) {
                    flyClue.handle(player, new ClueDataPair("wfly_flytime_to_fly", "1"));
                }
            }
        }, 20L, 20L);  // Delay of 20 ticks (1 second) and repeats every 20 ticks
    }
}
