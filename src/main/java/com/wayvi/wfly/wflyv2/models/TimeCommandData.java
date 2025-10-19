package com.wayvi.wfly.wflyv2.models;

import java.util.List;

public class TimeCommandData {
    private final List<String> commands;
    private final String type;

    public TimeCommandData(List<String> commands, String type) {
        this.commands = commands;
        this.type = type;
    }

    public List<String> getCommands() {
        return commands;
    }

    public String getType() {
        return type;
    }
}
