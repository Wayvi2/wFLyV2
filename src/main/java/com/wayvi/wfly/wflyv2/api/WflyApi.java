package com.wayvi.wfly.wflyv2.api;

import com.wayvi.wfly.wflyv2.WFlyV2;
import org.jetbrains.annotations.ApiStatus;

public class WflyApi {

    private static final WflyApi INSTANCE = new WflyApi();

    private static WFlyV2 plugin;
    private static FlyManager flyManager;
    private static TimeFlyManager timeFlyManager;
    private static ConditionManager conditionManager;
    private static ExchangeManager exchangeManager;

    @ApiStatus.Internal
    public static void inject(WFlyV2 plugin) {
        WflyApi.plugin = plugin;
    }

    @ApiStatus.Internal
    public static void inject(FlyManager flyManager) {
        WflyApi.flyManager = flyManager;
    }

    @ApiStatus.Internal
    public static void inject(TimeFlyManager timeFlyManager) {
        WflyApi.timeFlyManager = timeFlyManager;
    }

    @ApiStatus.Internal
    public static void inject(ConditionManager conditionManager) { WflyApi.conditionManager = conditionManager; }

    @ApiStatus.Internal
    public static void inject(ExchangeManager exchangeManager) {
        WflyApi.exchangeManager = exchangeManager;
    }

    // ---------------------- UNINJECT ----------------------
    @ApiStatus.Internal
    public static void uninjectPlugin() {
        plugin = null;
    }

    @ApiStatus.Internal
    public static void uninjectFlyManager() {
        flyManager = null;
    }

    @ApiStatus.Internal
    public static void uninjectTimeFlyManager() {
        timeFlyManager = null;
    }

    @ApiStatus.Internal
    public static void uninjectConditionManager() {
        conditionManager = null;
    }

    @ApiStatus.Internal
    public static void uninjectExchangeManager() {
        exchangeManager = null;
    }

    @ApiStatus.Internal
    public static void uninjectAll() {
        plugin = null;
        flyManager = null;
        timeFlyManager = null;
        conditionManager = null;
        exchangeManager = null;
    }
    // ------------------------------------------------------

    public static WflyApi get() {
        return INSTANCE;
    }

    public WFlyV2 getPlugin() {
        return plugin;
    }

    public FlyManager getFlyManager() {
        return flyManager;
    }

    public TimeFlyManager getTimeFlyManager() {
        return timeFlyManager;
    }

    public ConditionManager getConditionManager() {
        return conditionManager;
    }

    public ExchangeManager getExchangeManager() {
        return exchangeManager;
    }
}
