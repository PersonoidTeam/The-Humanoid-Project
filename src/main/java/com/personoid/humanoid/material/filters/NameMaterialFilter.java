package com.personoid.humanoid.material.filters;

import com.personoid.humanoid.material.MaterialFilter;
import org.bukkit.Material;

public class NameMaterialFilter extends MaterialFilter {
    private final String name;
    private final ReferenceType referenceType;

    public NameMaterialFilter(String name) {
        this(name, ReferenceType.ENDS_WITH);
    }

    public NameMaterialFilter(String name, ReferenceType referenceType) {
        this.name = name;
        this.referenceType = referenceType;
    }

    @Override
    public boolean accepts(Material material) {
        switch (referenceType) {
            case STARTS_WITH: {
                return material.name().toLowerCase().startsWith(getName());
            }
            case ENDS_WITH: {
                return material.name().toLowerCase().endsWith(getName());
            }
            case CONTAINS: {
                return material.name().toLowerCase().contains(getName());
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public enum ReferenceType {
        STARTS_WITH,
        ENDS_WITH,
        CONTAINS
    }
}
