package com.personoid.humanoid.structure.detection;

import com.personoid.api.utils.math.Range;
import com.personoid.humanoid.material.GenericMaterial;
import com.personoid.humanoid.structure.Layer;
import com.personoid.humanoid.structure.Structure;
import com.personoid.humanoid.structure.StructureRef;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

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
        //Bukkit.broadcastMessage("Locating structure... (search range: " + searchRange.getMin() + " to " + searchRange.getMax() + ")");
        List<Block> structure = new ArrayList<>();
        List<GenericMaterial> neededBlocks = new ArrayList<>();
        Block chosenStructure = null;
        // search within search range to find any block in the first layer
        for (int x = searchRange.getMin(); x <= searchRange.getMax(); x++) {
            for (int y = searchRange.getMin(); y <= searchRange.getMax(); y++) { // can't see underground?
                for (int z = searchRange.getMin(); z <= searchRange.getMax(); z++) {
                    Location checkLoc = from.clone().add(x, y, z);
                    int validLayers = 0;
                    breakpoint:
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
                        for (int i = 0; i < reference.getLayers().size(); i++) { // check all other layers
                            Layer layer = reference.getLayers().get(i);
                            neededBlocks.addAll(layer.getMaterials()); // reset needed blocks list
                            Location validationLoc = checkLoc.clone();
                            if (!startHighestY) validationLoc.setY(highestY + 1);
                            //Bukkit.broadcastMessage("Checking " + LocationUtils.toStringBasic(validationLoc));
                            List<Block> foundBlocks = validateLayer(layer, validationLoc.getBlock());
                            structureBlocks.addAll(captureLayer(layer, validationLoc.getBlock()));
                            // remove found blocks from needed blocks list
                            foundBlocks.forEach(block -> neededBlocks.remove(layer.getGenericMaterial(block.getType())));
                            // get highest y pos in found blocks
                            for (Block block : foundBlocks) if (block.getY() > highestY) highestY = block.getY();
                            startHighestY = false;
                            //Bukkit.broadcastMessage("Highest y pos in found blocks: " + highestY);
                            if (neededBlocks.isEmpty()) { // found all the blocks in the layer
/*                                String postfixMsg = i == reference.getLayers().size() - 1 ?
                                        ". All layers validated" : ", validating other layers...";
                                Bukkit.broadcastMessage("Found all blocks in layer " + (i + 1) + postfixMsg);*/
                                validLayers++;
                            } else { // didn't find all the blocks in the layer
                                //Bukkit.broadcastMessage("Didn't find all blocks in layer " + (i + 1) + ", searching another location...");
                                neededBlocks.clear();
                                break breakpoint;
                            }
                        }
                        if (validLayers < reference.getLayers().size()) break;
                        //Bukkit.broadcastMessage("--FOUND ALL LAYERS--");
                        // if we passed the for loop then we have found a matching structure
                        boolean inExclusions = false;
                        for (Structure exclusion : exclusions) {
                            if (exclusion.contains(structureBlocks, 0.25F)) {
                                inExclusions = true;
                                break;
                            }
                        }
                        if (chosenStructure != null) {
                            switch (searchType) { // compare to the current chosen structure based on the search type
                                case CLOSEST: {
                                    boolean closerDist = checkLoc.distance(from) < chosenStructure.getLocation().distance(from);
                                    if (closerDist && !inExclusions) {
                                        chosenStructure = checkLoc.getBlock();
                                        structure = new ArrayList<>(structureBlocks);
                                        //Bukkit.broadcastMessage("Found a closer structure");
                                    }
                                }
                                case FURTHEST: {
                                    boolean furtherDist = checkLoc.distance(from) > chosenStructure.getLocation().distance(from);
                                    if (furtherDist && !inExclusions) {
                                        chosenStructure = checkLoc.getBlock();
                                        structure = new ArrayList<>(structureBlocks);
                                    }
                                }
                            }
                        } else if (!inExclusions) {
                            chosenStructure = checkLoc.getBlock(); // first structure found
                            structure = new ArrayList<>(structureBlocks);
                            //Bukkit.broadcastMessage("Found first structure: " + structureBlocks.size());
                        }
                    }
                }
            }
        }
        //Bukkit.broadcastMessage("Found " + (chosenStructure == null ? "no" : "a") + " structure");
        if (structure.isEmpty()) return null;
        return new Structure(structure);
    }

    // look for the rest of the blocks within the layer based on the layer's minimum bounds
    /** @return the blocks found within the layer **/
    private List<Block> validateLayer(Layer layer, Block from) {
        List<Block> found = new ArrayList<>();
        int searchBoundsXz = layer.getMaxBounds().getXz();
        int searchBoundsY = layer.getMaxBounds().getY();
        // check minimum bounds
        int lowestYPosBlock = Integer.MAX_VALUE;
        List<Block> tempList = new ArrayList<>();
        for (int x = -searchBoundsXz; x < searchBoundsXz; x++) {
            for (int y = 0; y < searchBoundsY; y++) {
                for (int z = -searchBoundsXz; z < searchBoundsXz; z++) {
                    Location checkLoc = from.getLocation().clone().add(x, y, z);
                    Material material = checkLoc.getBlock().getType();
                    if (layer.contains(material)) { // found a block within the layer
                        //Bukkit.broadcastMessage("Found block within layer: " + material.name());
                        if (layer.getMinBounds() != null) {
                            tempList.add(checkLoc.getBlock()); // if the minimum bounds given are greater than one block tall
                            if (lowestYPosBlock > checkLoc.getBlockY()) lowestYPosBlock = checkLoc.getBlockY();
                        }
                        else found.add(checkLoc.getBlock());
                    }
                }
            }
        }
        if (!tempList.isEmpty()) {
            int minBoundsXz = layer.getMinBounds().getXz();
            int minBoundsY = layer.getMinBounds().getY();
            // epic super way to get minimum percentage of blocks needed:
            // width squared / (width + Math.ceil(width / 2) squared) * 100
            double totalSqrt = minBoundsXz + Math.ceil(minBoundsXz / 2F);
            double minPercent = (minBoundsXz * minBoundsXz) / (totalSqrt * totalSqrt) * 100;
            List<Block> tempWidthList = new ArrayList<>();
            int foundYBlocks = 0;
            int nextY = lowestYPosBlock;
            for (Block block : tempList) {
                if (block.getY() == nextY) {
                    foundYBlocks++;
                    nextY++;
                }
                // add only different Xz blocks to list to avoid percentage y bias
                boolean foundSameXz = false;
                for (Block block1 : tempWidthList) {
                    if (block.getX() == block1.getX() && block.getZ() == block1.getZ()) {
                        foundSameXz = true;
                        break;
                    }
                }
                if (!foundSameXz) tempWidthList.add(block);
            }
            //Bukkit.broadcastMessage("Total y blocks found: " + foundYBlocks);
            double percent = tempWidthList.size() / (totalSqrt * totalSqrt) * 100;
            //Bukkit.broadcastMessage("Min percent: " + minPercent + ", found percent: " + percent);
            if (percent >= minPercent && foundYBlocks >= minBoundsY) {
                //Bukkit.broadcastMessage("Valid blocks found in layer, adding to list...");
                found.addAll(tempWidthList);
            }
        }
        //Bukkit.broadcastMessage("Found total of " + found.size() + " blocks within layer");
        return found;
    }

    /** @return the blocks found within the layer **/
    private List<Block> captureLayer(Layer layer, Block from) {
        List<Block> found = new ArrayList<>();
        int searchBoundsXz = layer.getMaxBounds().getXz();
        int searchBoundsY = layer.getMaxBounds().getY();
        int lowestYPosBlock = Integer.MAX_VALUE;
        for (int x = -searchBoundsXz; x < searchBoundsXz; x++) {
            for (int y = 0; y < searchBoundsY; y++) {
                for (int z = -searchBoundsXz; z < searchBoundsXz; z++) {
                    Location checkLoc = from.getLocation().clone().add(x, y, z);
                    Material material = checkLoc.getBlock().getType();
                    if (layer.contains(material)) { // found a block within the layer
                        //Bukkit.broadcastMessage("Found block within layer: " + material.name());
                        if (layer.getMinBounds() != null) {
                            found.add(checkLoc.getBlock()); // if the minimum bounds given are greater than one block tall
                            if (lowestYPosBlock > checkLoc.getBlockY()) lowestYPosBlock = checkLoc.getBlockY();
                        }
                        else found.add(checkLoc.getBlock());
                    }
                }
            }
        }
        return found;
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
