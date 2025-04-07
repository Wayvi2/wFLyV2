package com.wayvi.wfly.wflyv2.api;

import com.wayvi.wfly.wflyv2.WFlyV2;
import org.jetbrains.annotations.ApiStatus;

public class WflyApi {

    private static WflyApi instance;

    private static WFlyV2 plugin;
    private static FlyManager flyManager;
    private static TimeFlyManager timeFlyManager;
    private static ConditionManager conditionManager;

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
    public static void inject(ConditionManager conditionManager) {
        WflyApi.conditionManager = conditionManager;
    }

    public static WflyApi get() {
        if (instance == null) {
            instance = new WflyApi();
        }
        return instance;
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





}
