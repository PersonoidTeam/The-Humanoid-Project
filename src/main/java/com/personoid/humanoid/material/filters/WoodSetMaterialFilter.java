package com.personoid.humanoid.material.filters;

import com.personoid.humanoid.material.MaterialFilter;
import com.personoid.humanoid.material.WoodSet;
import org.bukkit.Material;

public class WoodSetMaterialFilter extends MaterialFilter {
    private final WoodSet[] woodSets;

    public WoodSetMaterialFilter(WoodSet woodSets) {
        this.woodSets = new WoodSet[] { woodSets };
    }

    public WoodSetMaterialFilter(WoodSet... woodSets) {
        this.woodSets = woodSets;
    }

    @Override
    public boolean accepts(Material material) {
        for (WoodSet woodSet : woodSets) {
            if (material.name().toLowerCase().startsWith(woodSet.getId())) {
                return true;
            }
        }
        return false;
    }

    public WoodSet[] getWoodSets() {
        return woodSets;
    }
}
