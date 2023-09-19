package com.personoid.humanoid.material;

public enum WoodSet {
    OAK("oak"),
    SPRUCE("spruce"),
    BIRCH("birch"),
    JUNGLE("jungle"),
    ACACIA("acacia"),
    DARK_OAK("dark_oak"),
    CRIMSON("crimson"),
    WARPED("warped"),
    MANGROVE("mangrove"),
    CHERRY("cherry");

    public static final WoodSet[] ALL = values();

    private final String id;

    WoodSet(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static WoodSet fromString(String name) {
        for (WoodSet woodSet : values()) {
            if (woodSet.name().equalsIgnoreCase(name)) {
                return woodSet;
            }
        }
        return null;
    }

    public String getFormattedName() {
        return name().toLowerCase().replace("_", " ");
    }
}
