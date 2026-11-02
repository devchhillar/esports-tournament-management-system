package com.esports.model;

/**
 * Standard Gaming Categories supported in tournaments.
 */
public enum GameCategory {
    BGMI("BGMI"),
    VALORANT("Valorant"),
    FREE_FIRE("Free Fire"),
    CS2("Counter-Strike 2"),
    OTHER("Other");

    private final String displayName;

    GameCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static GameCategory fromString(String text) {
        if (text == null) return OTHER;
        for (GameCategory category : GameCategory.values()) {
            if (category.displayName.equalsIgnoreCase(text.trim()) || category.name().equalsIgnoreCase(text.trim())) {
                return category;
            }
        }
        return OTHER;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
