package com.wayvi.wfly.wflyv2.managers;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.configs.ItemsEnum;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.storage.sql.FlyTimeRepository;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WItemsManager {

    private WFlyV2 plugin;
    private WFlyPlaceholder wFlyPlaceholder;

    private final String TOKEN_NAME;
    private final List<String> TOKEN_LORE;
    private final Material TOKEN_MATERIAL;
    private final int TOKEN_CUSTOM_MODEL_DATA;
    private final String TOKEN_MESSAGE_GIVE;
    private final String TOKEN_MESSAGE_USE;
    private final String TOKEN_MESSAGE_ERROR;


    public WItemsManager(WFlyV2 plugin) {
        this.plugin = plugin;

        TOKEN_NAME = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_NAME);
        TOKEN_LORE = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_LORE);
        TOKEN_MATERIAL = Material.valueOf(plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MATERIAL));
        TOKEN_CUSTOM_MODEL_DATA =  plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_CUSTOM_MODEL_DATA);
        TOKEN_MESSAGE_GIVE =plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MESSAGE_GIVE);
        TOKEN_MESSAGE_USE = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MESSAGE_USE);
        TOKEN_MESSAGE_ERROR = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MESSAGE_ERROR);


    }


    public void giveFlyToken(Player player, int seconds) {

        int currentFly = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);
        if (currentFly < seconds) {
            ColorSupportUtil.sendColorFormat(player, TOKEN_MESSAGE_ERROR);
            return;
        }

        WflyApi.get().getTimeFlyManager().removeFlyTime(player, seconds);

        ItemStack token = new ItemStack(TOKEN_MATERIAL, 1);
        ItemMeta meta = token.getItemMeta();

        meta.setDisplayName((String) ColorSupportUtil.convertColorFormat(
                TOKEN_NAME.replace("%time%", WFlyPlaceholder.formatTime(plugin, seconds))
        ));

        List<String> lore = new ArrayList<>();
        for (String line : TOKEN_LORE) {
            lore.add((String) ColorSupportUtil.convertColorFormat(line.replace("%time%", WFlyPlaceholder.formatTime(plugin, seconds))));
        }
        meta.setLore(lore);

        applyCustomModelData(meta, TOKEN_CUSTOM_MODEL_DATA);

        token.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(token);
        nbtItem.setInteger("fly_time", seconds);
        token = nbtItem.getItem();

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), token);
            player.sendMessage((String) ColorSupportUtil.convertColorFormat("&cYour inventory is full! The Fly Token has been dropped on the ground."));
        } else {
            player.getInventory().addItem(token);
        }
        player.sendMessage((String) ColorSupportUtil.convertColorFormat(
                TOKEN_MESSAGE_GIVE.replace("%time%", WFlyPlaceholder.formatTime(plugin, seconds))
        ));
    }


    private void applyCustomModelData(ItemMeta meta, int modelData) {
        try {
            meta.getClass().getMethod("setCustomModelData", int.class).invoke(meta, modelData);
        } catch (Exception ignored) {

        }
    }











}
