package com.personoid.humanoid.utils;

import org.bukkit.Material;

import javax.annotation.Nullable;
import java.util.List;

public class Layer {
    private final List<GenericMaterial> materials;
    private final Bounds bounds;

    public Layer(List<GenericMaterial> materials, Bounds bounds) {
        this.materials = materials;
        this.bounds = bounds;
    }

    public Layer(List<GenericMaterial> materials) {
        this.materials = materials;
        this.bounds = null;
    }

    public List<GenericMaterial> getMaterials() {
        return materials;
    }

    @Nullable
    public Bounds getBounds() {
        return bounds;
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
