package com.wayvi.wfly.wflyv2.constants.conditions;

import com.wayvi.wfly.wflyv2.api.conditions.Condition;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public enum ConditionType {


    HAS_PERMISSION {
        @Override
        public Condition create(ConfigurationSection section) {
            String permission = section.getString("permission");
            return player -> player.hasPermission(permission);
        }
    },


    STRING_EQUALS {
        @Override
        public Condition create(ConfigurationSection section) {
            String inputRaw = section.getString("input");
            String valueRaw = section.getString("value");

            return player -> {
                if (inputRaw == null || valueRaw == null) return false;

                // On traduit les placeholders des DEUX côtés
                String parsedInput = PlaceholderAPI.setPlaceholders(player, inputRaw);
                String parsedValue = PlaceholderAPI.setPlaceholders(player, valueRaw);

                return parsedInput.equals(parsedValue);
            };
        }
    },


    STRING_EQUALS_IGNORECASE {
        @Override
        public Condition create(ConfigurationSection section) {
            String inputRaw = section.getString("input");
            String valueRaw = section.getString("value");

            return player -> {
                if (inputRaw == null || valueRaw == null) return false;

                String parsedInput = PlaceholderAPI.setPlaceholders(player, inputRaw);
                String parsedValue = PlaceholderAPI.setPlaceholders(player, valueRaw);

                return parsedInput.equalsIgnoreCase(parsedValue);
            };
        }
    },


    OR {
        @Override
        public Condition create(ConfigurationSection section) {
            return player -> true;
        }
    };


    public abstract Condition create(ConfigurationSection section);

    public static Condition parse(ConfigurationSection section) {
        if (section == null) return p -> false;

        String typeStr = section.getString("type");
        if (typeStr == null) return p -> false;

        boolean inverted = false;
        if (typeStr.startsWith("!")) {
            inverted = true;
            typeStr = typeStr.substring(1);
        }

        try {

            ConditionType type = valueOf(typeStr.toUpperCase());
            Condition condition = type.create(section);

            if (inverted) {
                return player -> !condition.check(player);
            }

            return condition;

        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("[WlyV2] Unknown condition type in the configuration : " + typeStr);
            return p -> false;
        }
    }
}