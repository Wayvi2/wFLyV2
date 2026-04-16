package com.wayvi.wfly.wflyv2.managers;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class TimedFlyManager {

    private WFlyV2 plugin;
    private Map<UUID, Integer> cooldown = new HashMap<>();

    public TimedFlyManager(WFlyV2 plugin) {
        this.plugin = plugin;
        startCooldownTask();
    }


    public Group getHighestPriorityGroup(UUID playerUUID) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(playerUUID);

        if (user == null) return null;

        return user.getNodes().stream()
                .filter(node -> node instanceof InheritanceNode)
                .map(node -> (InheritanceNode) node)
                .map(node -> luckPerms.getGroupManager().getGroup(node.getGroupName()))
                .filter(group -> group != null)
                .max(Comparator.comparingInt(group -> group.getWeight().orElse(0)))
                .orElse(null);
    }

    public Integer getPlayerCooldown(UUID playerUUID) {
        return cooldown.getOrDefault(playerUUID, 0);
    }


    public int getGroupCooldown(Group group) {
        if (group == null) {
            return 300;
        }
        String groupName = group.getName();
        String path = "timed-fly.group." + groupName + ".cooldown";

        return plugin.getConfigFile().getRaw().getInt(path, 60);
    }

    public int getGroupFlyTime(Group group) {
        if (group == null) {
            return 60;
        }
        String groupName = group.getName().toLowerCase();
        String path = "timed-fly.group." + groupName + ".fly-time";
        return plugin.getConfigFile().getRaw().getInt(path, 60);
    }


    public boolean isEnabled() {
        return plugin.getConfigFile().getRaw().getBoolean("timed-fly.enabled", false);
    }

    public int getPlayerCooldownLimit(UUID playerUUID) {
        Group highestGroup = getHighestPriorityGroup(playerUUID);
        if (highestGroup == null) return 60;
        return getGroupCooldown(highestGroup);
    }

    public int getPlayerFlyTimeLimit(UUID playerUUID) {
        Group highestGroup = getHighestPriorityGroup(playerUUID);
        return getGroupFlyTime(highestGroup);
    }

    public void setCooldown(UUID playerUUID, int seconds) {
        this.cooldown.put(playerUUID, seconds);
    }


    public void startCooldownTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (cooldown.isEmpty()) return;

            boolean decrementOffline = plugin.getConfigFile().get(ConfigEnum.TIMED_FLY_DECREMENT_COOLDOWN_OFFLINE);

            Iterator<Map.Entry<UUID, Integer>> iterator = cooldown.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                UUID uuid = entry.getKey();

                if (!decrementOffline && Bukkit.getPlayer(uuid) == null) {
                    continue;
                }

                int timeLeft = entry.getValue();

                if (timeLeft <= 1) {
                    iterator.remove();

                    Group group = getHighestPriorityGroup(uuid);
                    int flyTimeLimit = getGroupFlyTime(group);

                    Bukkit.getScheduler().runTask(plugin, () -> {

                        WflyApi.get().getTimeFlyManager().setFlytime((Player) Bukkit.getOfflinePlayer(uuid), flyTimeLimit);

                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            String rawMessage = plugin.getMessageFile().get(MessageEnum.COOLDOWN_FINISHED);
                            String formattedMessage = rawMessage.replace("%time%", WFlyPlaceholder.formatTime(plugin, flyTimeLimit, true));
                            ColorSupportUtil.sendColorFormat(player, formattedMessage);
                        }
                    });
                } else {
                    entry.setValue(timeLeft - 1);
                }
            }
        }, 20L, 20L);
    }


}