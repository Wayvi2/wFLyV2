package com.wayvi.wfly.wflyv2.models;

import java.util.List;

/**
 * Represents a condition used for checking whether a player is authorized to perform certain actions
 * based on placeholder values and the associated commands to be executed.
 */
public class Condition {

    private final String placeholder;
    private final String equalsValue;
    private final List<String> commands;

    /**
     * Constructs a Condition object with a placeholder, equals value, and list of commands.
     *
     * @param placeholder The placeholder used to retrieve a value for comparison.
     * @param equalsValue The value that the placeholder's value is compared against.
     * @param commands A list of commands to be executed if the condition is met.
     */
    public Condition(String placeholder, String equalsValue, List<String> commands) {
        this.placeholder = placeholder;
        this.equalsValue = equalsValue;
        this.commands = commands;
    }

    /**
     * Gets the placeholder used for condition evaluation.
     *
     * @return The placeholder string.
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * Gets the value that the placeholder is compared to.
     *
     * @return The value to compare against.
     */
    public String getEqualsValue() {
        return equalsValue;
    }

    /**
     * Gets the list of commands to be executed when the condition is met.
     *
     * @return A list of commands.
     */
    public List<String> getCommands() {
        return commands;
    }

}
