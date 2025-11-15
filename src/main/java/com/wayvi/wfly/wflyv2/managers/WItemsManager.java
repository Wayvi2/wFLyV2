package com.wayvi.wfly.wflyv2.managers;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.ItemsEnum;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.storage.sql.FlyTimeRepository;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WItemsManager {

    private WFlyV2 plugin;


    public WItemsManager(WFlyV2 plugin) {
        this.plugin = plugin;
    }


    public void giveFlyToken(Player player, int seconds) {

        final String TOKEN_NAME = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_NAME);
        final List<String> TOKEN_LORE = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_LORE);
        final Material TOKEN_MATERIAL = Material.valueOf(plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MATERIAL));
        final int TOKEN_CUSTOM_MODEL_DATA = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_CUSTOM_MODEL_DATA);
        final String TOKEN_MESSAGE_GIVE = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MESSAGE_GIVE);
        final String TOKEN_MESSAGE_USE = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MESSAGE_USE);
        final String TOKEN_MESSAGE_ERROR = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MESSAGE_ERROR);

        int currentFly = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);

        if (!player.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
            if (currentFly < seconds) {
                ColorSupportUtil.sendColorFormat(player, TOKEN_MESSAGE_ERROR);
                return;
            }

            WflyApi.get().getTimeFlyManager().removeFlyTime(player, seconds);
        }

        ItemStack token = new ItemStack(TOKEN_MATERIAL, 1);
        ItemMeta meta = token.getItemMeta();

        meta.setDisplayName((String) ColorSupportUtil.convertColorFormat(
                TOKEN_NAME.replace("%time%", WFlyPlaceholder.formatTime(plugin, seconds, true))
        ));

        List<String> lore = new ArrayList<>();
        for (String line : TOKEN_LORE) {
            lore.add((String) ColorSupportUtil.convertColorFormat(line.replace("%time%", WFlyPlaceholder.formatTime(plugin, seconds, true))));
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
                TOKEN_MESSAGE_GIVE.replace("%time%", WFlyPlaceholder.formatTime(plugin, seconds, true))
        ));
    }

    public void giveFlyToken(CommandSender sender, Player target, int seconds) {

        if (target == null) {
            sender.sendMessage("§cTarget player not found.");
            return;
        }

        final String TOKEN_NAME = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_NAME);
        final List<String> TOKEN_LORE = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_LORE);
        final Material TOKEN_MATERIAL = Material.valueOf(plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MATERIAL));
        final int TOKEN_CUSTOM_MODEL_DATA = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_CUSTOM_MODEL_DATA);
        final String TOKEN_MESSAGE_GIVE = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MESSAGE_GIVE);

        // création du token
        ItemStack token = new ItemStack(TOKEN_MATERIAL, 1);
        ItemMeta meta = token.getItemMeta();
        meta.setDisplayName((String) ColorSupportUtil.convertColorFormat(
                TOKEN_NAME.replace("%time%", WFlyPlaceholder.formatTime(plugin, seconds, true))
        ));

        List<String> lore = new ArrayList<>();
        for (String line : TOKEN_LORE) {
            lore.add((String) ColorSupportUtil.convertColorFormat(
                    line.replace("%time%", WFlyPlaceholder.formatTime(plugin, seconds, true))
            ));
        }
        meta.setLore(lore);

        applyCustomModelData(meta, TOKEN_CUSTOM_MODEL_DATA);
        token.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(token);
        nbtItem.setInteger("fly_time", seconds);
        token = nbtItem.getItem();

        // ajout ou drop
        if (target.getInventory().firstEmpty() == -1) {
            target.getWorld().dropItemNaturally(target.getLocation(), token);
            sender.sendMessage("§cTarget inventory is full, token dropped at player location.");
        } else {
            target.getInventory().addItem(token);
        }

        // feedback
        target.sendMessage((String) ColorSupportUtil.convertColorFormat(
                TOKEN_MESSAGE_GIVE.replace("%time%", WFlyPlaceholder.formatTime(plugin, seconds, true))
        ));
        if (!(sender instanceof Player)) {
            sender.sendMessage("Fly Token of " + seconds + "s given to " + target.getName());
        }
    }



    private void applyCustomModelData(ItemMeta meta, int modelData) {
        try {
            meta.getClass().getMethod("setCustomModelData", int.class).invoke(meta, modelData);
        } catch (Exception ignored) {

        }
    }











}
