package com.wayvi.wfly.wflyV2.models;

import java.util.List;

public class Condition {
    private final String placeholder;
    private final String equalsValue;
    private final List<String> commands;

    public Condition(String placeholder, String equalsValue, List<String> commands) {
        this.placeholder = placeholder;
        this.equalsValue = equalsValue;
        this.commands = commands;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getEqualsValue() {
        return equalsValue;
    }

    public List<String> getCommands() {
        return commands;
    }

}