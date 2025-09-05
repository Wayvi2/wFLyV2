package com.wayvi.wfly.wflyv2.constants.commands;

public enum TimeUnits {

    SECONDS("seconds", 1),
    MINUTES("minutes", 60),
    HOURS("hours", 3600),
    DAYS("days", 86400);

    private final String timeUnits;
    private final int secondsFactor;

    TimeUnits(String timeUnits, int secondsFactor) {
        this.timeUnits = timeUnits;
        this.secondsFactor = secondsFactor;
    }

    public String getTimeUnits() {
        return timeUnits;
    }

    public int toSeconds(int amount) {
        return amount * secondsFactor;
    }

    public static int convertTimeToType(int amount, TimeUnits unit) {
        if (unit == null) {
            return amount;
        }

        try {
            return unit.toSeconds(amount);
        } catch (Exception e) {
            return amount;
        }
    }


}
