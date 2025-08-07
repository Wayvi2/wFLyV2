package com.wayvi.wfly.wflyv2.managers;

import com.wayvi.wfly.wflyv2.api.ExchangeManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import org.bukkit.entity.Player;

import java.io.Externalizable;

public class WExchangeManager implements ExchangeManager {


    @Override
    public void exchangeTimeFly(Player donator, Player receiver, int time) {
        WflyApi.get().getTimeFlyManager().removeFlyTime(donator, time);
        WflyApi.get().getTimeFlyManager().addFlytime(receiver, time);
    }
}
