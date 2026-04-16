package com.wayvi.wfly.wflyv2.managers;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.ItemsEnum;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WItemsManager {

    private final WFlyV2 plugin;

    public WItemsManager(WFlyV2 plugin) {
        this.plugin = plugin;
    }

    public void giveFlyToken(Player player, int seconds) {
        final String TOKEN_NAME = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_NAME);
        final List<String> TOKEN_LORE = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_LORE);
        final Material TOKEN_MATERIAL = Material.valueOf(plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MATERIAL));
        final int TOKEN_CUSTOM_MODEL_DATA = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_CUSTOM_MODEL_DATA);
        final String TOKEN_MESSAGE_GIVE = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MESSAGE_GIVE);
        final String TOKEN_MESSAGE_ERROR = plugin.getItemsFile().get(ItemsEnum.FLY_TOKEN_MESSAGE_ERROR);

        int currentFly = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);

        if (!player.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
            if (currentFly < seconds) {
                ColorSupportUtil.sendColorFormat(player, TOKEN_MESSAGE_ERROR);
                return;
            }
            WflyApi.get().getTimeFlyManager().removeFlyTime(player, seconds);
        }

        ItemStack item = createTokenStack(TOKEN_MATERIAL, TOKEN_NAME, TOKEN_LORE, TOKEN_CUSTOM_MODEL_DATA, seconds);

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
            player.sendMessage((String) ColorSupportUtil.convertColorFormat("&cYour inventory is full! The Fly Token has been dropped on the ground."));
        } else {
            player.getInventory().addItem(item);
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

        ItemStack item = createTokenStack(TOKEN_MATERIAL, TOKEN_NAME, TOKEN_LORE, TOKEN_CUSTOM_MODEL_DATA, seconds);

        if (target.getInventory().firstEmpty() == -1) {
            target.getWorld().dropItemNaturally(target.getLocation(), item);
            sender.sendMessage("§cTarget inventory is full, token dropped at player location.");
        } else {
            target.getInventory().addItem(item);
        }

        target.sendMessage((String) ColorSupportUtil.convertColorFormat(
                TOKEN_MESSAGE_GIVE.replace("%time%", WFlyPlaceholder.formatTime(plugin, seconds, true))
        ));

        if (!(sender instanceof Player)) {
            sender.sendMessage("Fly Token of " + seconds + "s given to " + target.getName());
        }
    }

    /**
     * Centralise la création de l'item pour éviter les répétitions et assurer le bon ordre NBT/Meta.
     */
    private ItemStack createTokenStack(Material material, String name, List<String> loreTemplate, int modelData, int seconds) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String timeStr = WFlyPlaceholder.formatTime(plugin, seconds, true);
            meta.setDisplayName((String) ColorSupportUtil.convertColorFormat(name.replace("%time%", timeStr)));

            List<String> lore = new ArrayList<>();
            for (String line : loreTemplate) {
                lore.add((String) ColorSupportUtil.convertColorFormat(line.replace("%time%", timeStr)));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        // Utilisation de NBTItem pour injecter les données ET le CustomModelData
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setInteger("fly_time", seconds);

        // Injecter le CustomModelData directement dans le tag NBT pour éviter les pertes
        if (modelData != 0) {
            nbtItem.setInteger("CustomModelData", modelData);
        }

        return nbtItem.getItem();
    }
}