package com.personoid.humanoid.values;

import com.personoid.humanoid.utils.Bounds;
import com.personoid.humanoid.utils.GenericMaterial;
import com.personoid.humanoid.utils.Layer;
import com.personoid.humanoid.utils.StructureReference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum StructureType {
    TREE(getReference("tree"));

    private final StructureReference reference;

    StructureType(StructureReference reference) {
        this.reference = reference;
    }

    public StructureReference getReference() {
        return reference;
    }

    public String getFormattedName() {
        return name().toLowerCase().replace("_", " ");
    }

    public static StructureType get(String name) {
        for (StructureType structureType : values()) {
            if (structureType.getFormattedName().equals(name)) {
                return structureType;
            }
        }
        return null;
    }

    private static StructureReference getReference(String type) {
        switch (type) {
            case "tree": { // define the shape of a tree
                // any log, at least 1 block wide and 4 blocks tall
                Layer logLayer = new Layer(Collections.singletonList(new GenericMaterial("log")), new Bounds(1, 4));
                // any leaves, no minimum bounds (due to connecting trees)
                Layer leavesLayer = new Layer(Collections.singletonList(new GenericMaterial("leaves")));
                // combine the layers (bottom -> top)
                List<Layer> layers = Arrays.asList(logLayer, leavesLayer);
                // create a reference from the layers
                return new StructureReference("tree", new Bounds(1, 7), layers);
            }
        }
        return null;
    }
}
