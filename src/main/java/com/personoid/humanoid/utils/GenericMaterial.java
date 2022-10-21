package com.personoid.humanoid.utils;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class GenericMaterial {
    private final String name;
    private final ReferenceType referenceType;

    public GenericMaterial(String name, ReferenceType referenceType) {
        this.name = name.trim().toUpperCase().replace(" ", "_");
        this.referenceType = referenceType;
    }

    public GenericMaterial(String name) {
        this.name = name.trim().toUpperCase().replace(" ", "_");
        this.referenceType = ReferenceType.ENDS_WITH;
    }

    public String getName() {
        return name;
    }

    public boolean matches(Material material) {
        switch (referenceType) {
            case STARTS_WITH: {
                return material.name().startsWith(getName());
            }
            case ENDS_WITH: {
                return material.name().endsWith(getName());
            }
            case CONTAINS: {
                return material.name().contains(getName());
            }
        }
        return false;
    }

    public List<Material> getReferences() {
        List<Material> materials = new ArrayList<>();
        for (Material material : Material.values()) {
            switch (referenceType) {
                case STARTS_WITH: {
                    if (material.name().startsWith(getName())) {
                        materials.add(material);
                    }
                }
                case ENDS_WITH: {
                    if (material.name().endsWith(getName())) {
                        materials.add(material);
                    }
                }
                case CONTAINS: {
                    if (material.name().contains(getName())) {
                        materials.add(material);
                    }
                }
            }
        }
        return materials;
    }

    public enum ReferenceType {
        STARTS_WITH,
        ENDS_WITH,
        CONTAINS,
    }
}
