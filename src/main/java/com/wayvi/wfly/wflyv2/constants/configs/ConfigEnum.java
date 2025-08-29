package com.wayvi.wfly.wflyv2.constants.configs;




import com.wayvi.wconfigapi.wconfigapi.ConfigKey;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public enum ConfigEnum implements ConfigKey<Object> {

    VERSION("version", "1.0.2.5"),

    MYSQL_ENABLED("mysql.enabled", false),
    MYSQL_HOST("mysql.host", "localhost"),
    MYSQL_PORT("mysql.port", 3306),
    MYSQL_DATABASE("mysql.database", "wfly"),
    MYSQL_USERNAME("mysql.username", "root"),
    MYSQL_PASSWORD("mysql.password", "root"),

    SAVE_DATABASE_DELAY("save-database-delay", 300),

    FLY_DECREMENT_METHOD("fly-decrement-method", "PLAYER_FLY_MODE"),
    FLY_DECREMENT_DISABLED_BY_STATIC("fly-decrement-disabled-by-static", false),
    DELAY("delay", 3),

    FORMAT_PLACEHOLDER_SECONDS("format-placeholder.seconds", true),
    FORMAT_PLACEHOLDER_MINUTES("format-placeholder.minutes", false),
    FORMAT_PLACEHOLDER_HOURS("format-placeholder.hours", true),
    FORMAT_PLACEHOLDER_DAYS("format-placeholder.days", true),
    FORMAT_PLACEHOLDER_UNLIMITED("format-placeholder.unlimited", "Unlimited"),
    FORMAT_PLACEHOLDER_AUTO_FORMAT("format-placeholder.auto-format", true),
    FORMAT_PLACEHOLDER_REMOVE_NULL_ENABLED("format-placeholder.remove-null-values.enabled", true),
    FORMAT_PLACEHOLDER_REMOVE_NULL_VALUE("format-placeholder.remove-null-values.value", "#FFC77A0seconds"),
    FORMAT_PLACEHOLDER_FORMAT("format-placeholder.format", "#FFC77A%seconds%#FF9D00%seconds_suffixe%#FFC77A%minutes%#FF9D00%minutes_suffixe% #FFC77A%hours%#FF9D00%hours_suffixe% #FFC77A%days%#FF9D00%days_suffixe%"),
    FORMAT_PLACEHOLDER_OTHER_SECONDS("format-placeholder.other-format.seconds_suffixe", "seconds"),
    FORMAT_PLACEHOLDER_OTHER_MINUTES("format-placeholder.other-format.minutes_suffixe", "minutes"),
    FORMAT_PLACEHOLDER_OTHER_HOURS("format-placeholder.other-format.hours_suffixe", "hours"),
    FORMAT_PLACEHOLDER_OTHER_DAYS("format-placeholder.other-format.days_suffixe", "days"),

    COMMAND_ALIAS("command.alias", Arrays.asList("wfly", "fly")),

    TP_ON_FLOOR_WHEN_FLY_DISABLED("tp-on-floor-when-fly-disabled", true),

    PVP_ENABLED_PERMISSION_RANGE("pvp.enabled-permission-range", false),
    PVP_FLY_DISABLE_RADIUS("pvp.fly-disable-radius", 5),
    PVP_BYPASS_PLACEHOLDERS("pvp.bypass.placeholders", Arrays.asList("%lands_land_name_plain%")),

    DECREMENTATION_DISABLE_BY_CONDITION(
            "decrementation-disable-by-condition",
            Collections.singletonList(Collections.singletonMap("condition", "%player_name%=Wayvi2"))
    ),

    COOLDOWN_GIVE_ENABLED("cooldown-give.enabled", false),

    COOLDOWN_GIVE_CUSTOM_ENABLED("cooldown-give.custom-cooldown.enabled", false),
    COOLDOWN_GIVE_CUSTOM_TIME("cooldown-give.custom-cooldown.cooldown", 5),
    COOLDOWN_GIVE_LIMITS_ENABLED("cooldown-give.limits.enabled", true),
    COOLDOWN_GIVE_MIN("cooldown-give.limits.give-minimum-value", 5),
    COOLDOWN_GIVE_MAX("cooldown-give.limits.give-maximum-value", 60),


    REDIS_ENABLED("redis.enabled", false),
    REDIS_HOST("redis.host", "127.0.0.1"),
    REDIS_PORT("redis.port", 6379),
    REDIS_PASSWORD("redis.password", ""),
    REDIS_DATABASE("redis.database", 0),
    REDIS_TIMEOUT("redis.timeout", 2000),

    REDIS_POOL_MAX_TOTAL("redis.pool.maxTotal", 8),
    REDIS_POOL_MAX_IDLE("redis.pool.maxIdle", 8),
    REDIS_POOL_MIN_IDLE("redis.pool.minIdle", 0);

    private final String path;
    private final Object defaultValue;



    <T> ConfigEnum(String path, T defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }



    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}