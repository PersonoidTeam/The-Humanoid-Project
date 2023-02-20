package com.personoid.humanoid.structure.structures.nature;

import com.personoid.humanoid.material.GenericMaterial;
import com.personoid.humanoid.material.filters.NameMaterialFilter;
import com.personoid.humanoid.material.filters.WoodSetMaterialFilter;
import com.personoid.humanoid.structure.Layer;
import com.personoid.humanoid.structure.StructureRef;
import com.personoid.humanoid.utils.Bounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LargeTreeStructure extends StructureRef {
    public static final String ID = "large_tree";

    private final String id;
    private final WoodSetMaterialFilter woodSetFilter;

    private Layer logLayer;
    private Layer leavesLayer;

    public LargeTreeStructure(String id, WoodSetMaterialFilter woodSetFilter) {
        this.id = id;
        this.woodSetFilter = woodSetFilter;
        initialiseLayers();
    }

    private void initialiseLayers() {
        // any log, at least 1 block wide and 4 blocks tall
        GenericMaterial logMaterial = new GenericMaterial("log", woodSetFilter, new NameMaterialFilter("log"));
        logLayer = new Layer(Collections.singletonList(logMaterial), Bounds.of(1, 4), Bounds.of(6, 18));

        // any leaves, no minimum bounds (due to connecting trees)
        GenericMaterial leavesMaterial = new GenericMaterial("leaves", woodSetFilter, new NameMaterialFilter("leaves"));
        leavesLayer = new Layer(Collections.singletonList(leavesMaterial), Bounds.of(4), Bounds.of(8, 12));
    }

    @Override
    public String getId() {
        return ID + "_" + id;
    }

    @Override
    public List<Layer> getLayers() {
        return Arrays.asList(logLayer, leavesLayer);
    }

    public Layer getLogLayer() {
        return logLayer;
    }

    public Layer getLeavesLayer() {
        return leavesLayer;
    }
}
