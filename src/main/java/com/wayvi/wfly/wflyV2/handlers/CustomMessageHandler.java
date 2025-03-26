package com.wayvi.wfly.wflyV2.handlers;

import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import fr.traqueur.commands.api.logging.MessageHandler;

/**
 * Custom message handler for handling various command-related messages.
 */
public class CustomMessageHandler implements MessageHandler {

    private final ConfigUtil configUtil;

    /**
     * Constructs the CustomMessageHandler.
     *
     * @param configUtil Utility class for managing configuration files.
     */
    public CustomMessageHandler(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    /**
     * Retrieves the no permission message.
     *
     * @return The formatted no permission message.
     */
    @Override
    public String getNoPermissionMessage() {
        return (String) ColorSupportUtil.convertColorFormat(configUtil.getCustomMessage().getString("message.no-permission"));
    }

    /**
     * Retrieves the only in-game message.
     *
     * @return The formatted only in-game message.
     */
    @Override
    public String getOnlyInGameMessage() {
        return (String) ColorSupportUtil.convertColorFormat(configUtil.getCustomMessage().getString("message.only-in-game"));
    }

    /**
     * Retrieves the missing arguments message.
     *
     * @return The formatted missing arguments message.
     */
    @Override
    public String getMissingArgsMessage() {
        return (String) ColorSupportUtil.convertColorFormat(configUtil.getCustomMessage().getString("message.missing-args"));
    }

    /**
     * Retrieves the argument not recognized message.
     *
     * @return The formatted argument not recognized message.
     */
    @Override
    public String getArgNotRecognized() {
        return (String) ColorSupportUtil.convertColorFormat(configUtil.getCustomMessage().getString("message.arg-not-recognized"));
    }

    /**
     * Retrieves the requirement message.
     *
     * @return The formatted requirement message.
     */
    @Override
    public String getRequirementMessage() {
        return (String) ColorSupportUtil.convertColorFormat(configUtil.getCustomMessage().getString("message.message-requirement"));
    }
}
