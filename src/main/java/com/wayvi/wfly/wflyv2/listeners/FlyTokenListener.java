package com.wayvi.wfly.wflyv2.listeners;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.configs.ItemsEnum;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FlyTokenListener implements Listener {

    private final WFlyV2 plugin;

    private final String TOKEN_MESSAGE_USE;
    private final Material TOKEN_MATERIAL;

    public FlyTokenListener(WFlyV2 plugin) {
        this.plugin = plugin;
        TOKEN_MESSAGE_USE = (String) plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MESSAGE_USE);
        TOKEN_MATERIAL = Material.valueOf((String) plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MATERIAL));
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != TOKEN_MATERIAL) return;
        if (!item.hasItemMeta()) return;

        Action action = event.getAction();
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;

        NBTItem nbtItem = new NBTItem(item);
        if (!nbtItem.hasKey("fly_time")) return;

        int flyTime = nbtItem.getInteger("fly_time");

        WflyApi.get().getTimeFlyManager().addFlytime(player, flyTime);

        item.setAmount(item.getAmount() - 1);

        player.sendMessage((String) ColorSupportUtil.convertColorFormat(
                TOKEN_MESSAGE_USE.replace("%time%", WFlyPlaceholder.formatTime(plugin, flyTime))
        ));


        event.setCancelled(true);
    }
}

