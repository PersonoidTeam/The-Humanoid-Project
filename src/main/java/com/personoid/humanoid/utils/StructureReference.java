package com.personoid.humanoid.utils;

import java.util.List;

public class StructureReference {
    private final String name;
    private final Bounds bounds;
    private final List<Layer> layers;

    public StructureReference(String name, Bounds bounds, List<Layer> layers) {
        this.name = name;
        this.bounds = bounds;
        this.layers = layers;
    }

    public String getName() {
        return name;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public List<Layer> getLayers() {
        return layers;
    }
}
