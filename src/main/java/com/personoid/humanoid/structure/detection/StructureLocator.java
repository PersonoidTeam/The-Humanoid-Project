package com.personoid.humanoid.structure.detection;

import com.personoid.api.utils.math.Range;
import com.personoid.humanoid.material.GenericMaterial;
import com.personoid.humanoid.structure.Layer;
import com.personoid.humanoid.structure.Structure;
import com.personoid.humanoid.structure.StructureRef;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StructureLocator {
    private final StructureRef reference;
    private final SearchType searchType;
    private final Range searchRange;

    public StructureLocator(StructureRef reference, SearchType searchType, Range searchRange) {
        this.reference = reference;
        this.searchType = searchType;
        this.searchRange = searchRange;
    }

    public Structure locateStructure(Location from) {
        return locateStructure(from, new ArrayList<>());
    }

    // find the closest structure based on a range around the npc, needed blocks, and the radius of the structure
    public Structure locateStructure(Location from, List<Structure> exclusions) {
        List<Block> structure = new ArrayList<>();
        List<GenericMaterial> neededBlocks = new ArrayList<>();
        Block chosenStructure = null;
        for (int x = searchRange.getMin(); x <= searchRange.getMax(); x++) {
            for (int y = searchRange.getMin(); y <= searchRange.getMax(); y++) {
                for (int z = searchRange.getMin(); z <= searchRange.getMax(); z++) {
                    Location checkLoc = from.clone().add(x, y, z);
                    int validLayers = 0;
                    if (reference.getLayers().get(0).contains(checkLoc.getBlock().getType())) {
                        Location tempCheckLoc = checkLoc.clone();
                        for (int i = 0; i < reference.getBounds().getY(); i++) {
                            if (reference.getLayers().get(0).contains(tempCheckLoc.clone().subtract(0, i, 0).getBlock().getType())) {
                                checkLoc = checkLoc.clone().subtract(0, 1, 0);
                            }
                        }
                        int highestY = 0;
                        boolean startHighestY = true;
                        List<Block> structureBlocks = new ArrayList<>();
                        for (int i = 0; i < reference.getLayers().size(); i++) {
                            Layer layer = reference.getLayers().get(i);
                            neededBlocks.addAll(layer.getMaterials());
                            Location validationLoc = checkLoc.clone();
                            if (!startHighestY) validationLoc.setY(highestY + 1);
                            List<Block> foundBlocks = validateLayer(layer, validationLoc);
                            structureBlocks.addAll(foundBlocks);
                            foundBlocks.forEach(block -> neededBlocks.remove(layer.getGenericMaterial(block.getType())));
                            for (Block block : foundBlocks) {
                                if (block.getY() > highestY) {
                                    highestY = block.getY();
                                }
                            }
                            startHighestY = false;
                            if (neededBlocks.isEmpty()) {
                                validLayers++;
                            } else {
                                neededBlocks.clear();
                                break;
                            }
                        }
                        if (validLayers < reference.getLayers().size()) break;
                        boolean inExclusions = false;
                        for (Structure exclusion : exclusions) {
                            if (exclusion.contains(structureBlocks, 0.25F)) {
                                inExclusions = true;
                                break;
                            }
                        }
                        if (chosenStructure != null) {
                            switch (searchType) {
                                case CLOSEST: {
                                    boolean closerDist = checkLoc.distance(from) < chosenStructure.getLocation().distance(from);
                                    if (closerDist && !inExclusions) {
                                        chosenStructure = checkLoc.getBlock();
                                        structure = new ArrayList<>(structureBlocks);
                                    }
                                    break;
                                }
                                case FURTHEST: {
                                    boolean furtherDist = checkLoc.distance(from) > chosenStructure.getLocation().distance(from);
                                    if (furtherDist && !inExclusions) {
                                        chosenStructure = checkLoc.getBlock();
                                        structure = new ArrayList<>(structureBlocks);
                                    }
                                    break;
                                }
                            }
                        } else if (!inExclusions) {
                            chosenStructure = checkLoc.getBlock();
                            structure = new ArrayList<>(structureBlocks);
                        }
                    }
                }
            }
        }
        if (structure.isEmpty()) return null;
        return new Structure(structure);
    }

    // look for the rest of the blocks within the layer based on the layer's minimum bounds
    /** @return the blocks found within the layer **/
    public List<Block> validateLayer(Layer layer, Location from) {
        Set<Block> found = new HashSet<>();
        int searchBoundsXz = layer.getMaxBounds().getXz();
        int searchBoundsY = layer.getMaxBounds().getY();
        int lowestYPosBlock = Integer.MAX_VALUE;
        Set<Block> tempList = new HashSet<>();

        for (int x = -searchBoundsXz; x < searchBoundsXz; x++) {
            for (int y = 0; y < searchBoundsY; y++) {
                for (int z = -searchBoundsXz; z < searchBoundsXz; z++) {
                    Location checkLoc = from.clone().add(x, y, z);
                    Material material = checkLoc.getBlock().getType();
                    if (layer.contains(material)) {
                        Block block = checkLoc.getBlock();
                        for (BlockFace face : BlockFace.values()) {
                            Block relative = block.getRelative(face);
                            if (layer.contains(relative.getType())) {
                                tempList.add(block);
                                if (block.getY() < lowestYPosBlock) {
                                    lowestYPosBlock = block.getY();
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (!tempList.isEmpty()) {
            int minBoundsXz = layer.getMinBounds().getXz();
            int minBoundsY = layer.getMinBounds().getY();
            double total = Math.pow(minBoundsXz + Math.ceil(minBoundsXz / 2F), 2);
            double minPercent = Math.pow(minBoundsXz, 2) / total * 100;
            Set<Block> tempWidthList = new HashSet<>();
            int foundYBlocks = 0;
            int nextY = lowestYPosBlock;
            Set<Location> tempXzSet = new HashSet<>();
            for (Block block : tempList) {
                if (block.getY() == nextY) {
                    foundYBlocks++;
                    nextY++;
                }
                Location checkLoc = block.getLocation();
                if (!tempXzSet.contains(checkLoc)) {
                    tempXzSet.add(checkLoc);
                    tempWidthList.add(block);
                }
            }
            double percent = (tempWidthList.size() / total) * 100;
            if (percent >= minPercent && foundYBlocks >= minBoundsY) {
                found.addAll(tempWidthList);
            }
        }

        return new ArrayList<>(found);
    }

    public Range getSearchRange() {
        return searchRange;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public StructureRef getReference() {
        return reference;
    }

    public enum SearchType {
        CLOSEST,
        FURTHEST
    }
}
