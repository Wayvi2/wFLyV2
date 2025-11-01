package com.wayvi.wfly.wflyv2.managers.fly;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.FlyTimeSynchronizer;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WFlyTimeSynchronizer implements FlyTimeSynchronizer {

    private final WFlyV2 plugin;
    private boolean isOffline = true;

    private final Map<UUID, Long> playerLastUpdate = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> firstJoin = new ConcurrentHashMap<>();

    public WFlyTimeSynchronizer(WFlyV2 plugin) {
        this.plugin = plugin;



    }

    @Override
    public void handlePlayerQuitSynchronizer(Player player) {
        boolean decrementOffline = plugin.getConfigFile().get(ConfigEnum.DECREMENT_OFFLINE);
        if (!decrementOffline && WflyApi.get().getTimeFlyManager().getDecrementationDisable(player)){
            return;
        }
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        playerLastUpdate.put(uuid, now);



    }
    public void handlePlayerJoinSynchronizer(Player player) {

        boolean decrementOffline = plugin.getConfigFile().get(ConfigEnum.DECREMENT_OFFLINE);
        if (!decrementOffline && WflyApi.get().getTimeFlyManager().getDecrementationDisable(player)){
            return;
        }
        String decrementMethod = plugin.getConfigFile().get(ConfigEnum.FLY_DECREMENT_METHOD);

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        long shutdownTime = plugin.getStorage().getTimeOffOnDisable();
        long startupTime = plugin.getPluginStartupTime();
        long serverOfflineDuration = Math.max(0L, startupTime - shutdownTime);

        int remaining = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);

        boolean flying = true;
        if (decrementMethod.equals("PLAYER_FLYING_MODE")){
            flying = WflyApi.get().getTimeFlyManager().getIsFlying(uuid) && player.isFlying();
        } else if (decrementMethod.equals("PLAYER_FLY_MODE")){
            flying = WflyApi.get().getTimeFlyManager().getIsFlying(uuid);
        }



        Long lastUpdateObj = playerLastUpdate.get(uuid);
        int elapsedSeconds = 0;

        if (flying && remaining > 0) {
            long offlineMillis = 0L;

            if (lastUpdateObj != null) {
                offlineMillis = Math.max(0L, now - lastUpdateObj);

                if (!firstJoin.getOrDefault(uuid, false) && serverOfflineDuration > 0L) {
                    offlineMillis -= serverOfflineDuration;

                    if (offlineMillis < 0L) offlineMillis = 0L;

                    firstJoin.put(uuid, true);
                    //plugin.getLogger().info("[wFlyV2] Temps serveur éteint (" + (serverOfflineDuration / 1000L) + "s) ignoré pour " + player.getName());
                }
            } else {
                offlineMillis = 0L;
            }

            elapsedSeconds = (int) (offlineMillis / 1000L);
        }

        if (elapsedSeconds > 0) {
            WflyApi.get().getTimeFlyManager().removeFlyTime(player, elapsedSeconds);
        }

        //plugin.getLogger().info("[wFlyV2] Le serveur a été éteint pendant " +
        //        (serverOfflineDuration / 1000L) + "s");
        //plugin.getLogger().info("[wFlyV2] Fly data chargée pour " + player.getName() +
        //        " (temps hors ligne : " + elapsedSeconds + "s)");
    }







    @Override
    public boolean getIsOffline() {
        return isOffline;
    }

    @Override
    public long getLastUpdate(UUID uuid) {
        return playerLastUpdate.getOrDefault(uuid, System.currentTimeMillis());
    }

    @Override
    public void setPlayerLastUpdate(Player player, long time){
        playerLastUpdate.put(player.getUniqueId(),time);
    }





}
