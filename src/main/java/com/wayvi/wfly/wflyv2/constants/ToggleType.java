package com.wayvi.wfly.wflyv2.constants;

public enum ToggleType {
    ACTIVATE("activate"),
    DEACTIVATE("deactivate");

    private final String toggleType;

    ToggleType(String toggleType) {
        this.toggleType = toggleType;
    }

    /**
     * Gets the permission string associated with this enum constant.
     *
     * @return the permission string
     */
    public String getToggleType() {
        return toggleType;
    }
}
