package com.personoid.humanoid.structure;

import com.personoid.humanoid.utils.Bounds;

import java.util.List;

public abstract class StructureRef {
    public abstract String getId();
    public abstract List<Layer> getLayers();

    public Bounds getBounds() {
        return calculateBounds(getLayers());
    }

    private Bounds calculateBounds(List<Layer> layers) {
        int maxXz = 0;
        int maxY = 0;
        for (Layer layer : layers) {
            Bounds layerBounds = layer.getMaxBounds();
            if (layerBounds.getXz() > maxXz) {
                maxXz = layerBounds.getXz();
            }
            if (layerBounds.getY() > maxY) {
                maxY = layerBounds.getY();
            }
        }
        return new Bounds(maxXz, maxY);
    }
}
