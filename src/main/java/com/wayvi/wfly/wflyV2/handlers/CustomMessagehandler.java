package com.wayvi.wfly.wflyV2.handlers;

import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import fr.traqueur.commands.api.logging.MessageHandler;

public class CustomMessagehandler implements MessageHandler {

    private ConfigUtil configUtil;

    public CustomMessagehandler(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }


    @Override
    public String getNoPermissionMessage() {

        return (String) ColorSupportUtil.convertMiniMessageFormat(configUtil.getCustomMessage().getString("message.no-permission"));
    }

    @Override
    public String getOnlyInGameMessage() {
        return (String) ColorSupportUtil.convertMiniMessageFormat(configUtil.getCustomMessage().getString("message.only-in-game"));
    }

    @Override
    public String getMissingArgsMessage() {
        return (String) ColorSupportUtil.convertMiniMessageFormat(configUtil.getCustomMessage().getString("message.missing-args"));
    }

    @Override
    public String getArgNotRecognized() {
        return (String) ColorSupportUtil.convertMiniMessageFormat(configUtil.getCustomMessage().getString("message.arg-not-recognized"));
    }

    @Override
    public String getRequirementMessage() {
        return (String) ColorSupportUtil.convertMiniMessageFormat(configUtil.getCustomMessage().getString("message.message-requirement"));
    }
}
