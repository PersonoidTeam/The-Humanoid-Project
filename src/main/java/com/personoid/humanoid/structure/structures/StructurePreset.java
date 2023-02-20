package com.personoid.humanoid.structure.structures;

import com.personoid.humanoid.material.WoodSet;
import com.personoid.humanoid.material.filters.WoodSetMaterialFilter;
import com.personoid.humanoid.structure.StructureRef;
import com.personoid.humanoid.structure.structures.nature.SmallTreeStructure;

public enum StructurePreset {
    SMALL_TREE_ANY(new SmallTreeStructure("any", new WoodSetMaterialFilter(WoodSet.ALL))),
    SMALL_TREE_OAK(new SmallTreeStructure("oak", new WoodSetMaterialFilter(WoodSet.OAK)));

    private final StructureRef reference;

    StructurePreset(StructureRef reference) {
        this.reference = reference;
    }

    public StructureRef getReference() {
        return reference;
    }

    public String getFormattedName() {
        return name().toLowerCase().replace("_", " ");
    }

    public static StructurePreset get(String name) {
        for (StructurePreset structureType : values()) {
            if (structureType.getFormattedName().equals(name)) {
                return structureType;
            }
        }
        return null;
    }
}
