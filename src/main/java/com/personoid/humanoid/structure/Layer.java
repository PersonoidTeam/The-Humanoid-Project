package com.personoid.humanoid.structure;

import com.personoid.humanoid.utils.Bounds;
import com.personoid.humanoid.material.GenericMaterial;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Layer {
    private final List<GenericMaterial> materials;
    private final Bounds minBounds;
    private final Bounds maxBounds;
    private boolean connected;

    public Layer(@NotNull List<GenericMaterial> materials, @NotNull Bounds minBounds, @NotNull Bounds maxBounds, boolean connected) {
        Validate.notEmpty(materials, "Materials cannot be empty");
        this.materials = materials;
        this.minBounds = minBounds;
        this.maxBounds = maxBounds;
        this.connected = connected;
    }

    public List<GenericMaterial> getMaterials() {
        return materials;
    }

    public Bounds getMinBounds() {
        return minBounds;
    }

    public Bounds getMaxBounds() {
        return maxBounds;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean contains(Material material) {
        for (GenericMaterial genericMaterial : materials) {
            if (genericMaterial.matches(material)) {
                return true;
            }
        }
        return false;
    }

    public GenericMaterial getGenericMaterial(Material material) {
        for (GenericMaterial genericMaterial : materials) {
            if (genericMaterial.matches(material)) {
                return genericMaterial;
            }
        }
        return null;
    }
}
