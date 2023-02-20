package com.personoid.humanoid.material;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class GenericMaterial {
    private final String name;
    private final MaterialFilter[] filters;
    private final List<Material> materials;

    public GenericMaterial(String id, MaterialFilter... filters) {
        this.name = id;
        this.filters = filters;
        this.materials = collectMaterials();
    }

    private List<Material> collectMaterials() {
        List<Material> materials = new ArrayList<>();
        for (Material material : Material.values()) {
            boolean matches = true;
            for (MaterialFilter filter : filters) {
                if (!filter.accepts(material)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                materials.add(material);
            }
        }
        return materials;
    }

    public String getName() {
        return name;
    }

    public boolean matches(Material other) {
        for (Material material : materials) {
            if (material.equals(other)) {
                return true;
            }
        }
        return false;
    }

    public List<Material> getMaterials() {
        return materials;
    }
}
