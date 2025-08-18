package com.wayvi.wfly.wflyv2.handlers;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.logging.MessageHandler;

/**
 * Custom message handler for handling various command-related messages.
 */
public class CustomMessageHandler implements MessageHandler {

    private WFlyV2 plugin;

    /**
     * Constructs the CustomMessageHandler.
     *
     * @param configUtil Utility class for managing configuration files.
     */
    public CustomMessageHandler(WFlyV2 plugin) {
        this.plugin = plugin;
    }

    /**
     * Retrieves the no permission message.
     *
     * @return The formatted no permission message.
     */
    @Override
    public String getNoPermissionMessage() {
        return (String) ColorSupportUtil.convertColorFormat(plugin.getMessageFile().get(MessageEnum.NO_PERMISSION));
    }

    /**
     * Retrieves the only in-game message.
     *
     * @return The formatted only in-game message.
     */
    @Override
    public String getOnlyInGameMessage() {
        return (String) ColorSupportUtil.convertColorFormat(plugin.getMessageFile().get(MessageEnum.ONLY_IN_GAME));
    }


    /**
     * Retrieves the argument not recognized message.
     *
     * @return The formatted argument not recognized message.
     */
    @Override
    public String getArgNotRecognized() {
        return (String) ColorSupportUtil.convertColorFormat(plugin.getMessageFile().get(MessageEnum.ARG_NOT_RECOGNIZED));
    }

    /**
     * Retrieves the requirement message.
     *
     * @return The formatted requirement message.
     */
    @Override
    public String getRequirementMessage() {
        return (String) ColorSupportUtil.convertColorFormat(plugin.getMessageFile().get(MessageEnum.MESSAGE_REQUIREMENT));
    }
}
