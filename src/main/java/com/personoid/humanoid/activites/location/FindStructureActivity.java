package com.personoid.humanoid.activites.location;

import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.movement.MovementType;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.math.Range;
import com.personoid.humanoid.utils.GenericMaterial;
import com.personoid.humanoid.utils.Layer;
import com.personoid.humanoid.utils.LocationUtils;
import com.personoid.humanoid.utils.StructureReference;
import com.personoid.humanoid.values.StructureType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class FindStructureActivity extends Activity {
    private final StructureType structureType;
    private final int maxAttempts;
    private final int travelRadius;
    private final int searchRadius;

    private final List<Location> attempted = new ArrayList<>();

    public FindStructureActivity(StructureType structureType, int travelRadius, int searchRadius, int maxAttempts) {
        super(ActivityType.SEARCHING);
        this.structureType = structureType;
        this.travelRadius = travelRadius;
        this.searchRadius = searchRadius;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public void onStart(StartType startType) {
        attempted.clear(); // acts like a "try again after a break"
        if (startType == StartType.START) checkLocation();
    }

    private void checkLocation() {
        if (attempted.size() >= maxAttempts) markAsFinished(new Result<>(Result.Type.FAILURE, attempted));
        // search for structure that matches the reference
        Block tree = search(new Range(-searchRadius, searchRadius), SearchType.CLOSEST, structureType.getReference());
        if (tree != null) {
            markAsFinished(new Result<>(Result.Type.SUCCESS, tree)); // found a tree -> return result (tree block)
            return;
        }
        Location npcLoc = getNPC().getLocation().clone();
        Location travelLoc = npcLoc.add(MathUtils.random(-travelRadius, travelRadius), 0, MathUtils.random(-travelRadius, travelRadius));
        double yLoc = LocationUtils.getAirInDir(travelLoc.subtract(0, 1, 0), BlockFace.UP).getLocation().getY();
        travelLoc = new Location(travelLoc.getWorld(), travelLoc.getX(), yLoc, travelLoc.getZ());
        Bukkit.broadcastMessage("Couldn't find " + structureType.getFormattedName() + ", going to " + LocationUtils.toStringBasic(travelLoc));
        attempted.add(travelLoc);
        GoToLocationActivity.Options options = new GoToLocationActivity.Options();
        options.setStoppingDistance(3);
        run(new GoToLocationActivity(travelLoc, MovementType.SPRINTING, options).onFinished((result) -> {
            if (result.getType() == Result.Type.FAILURE) Bukkit.broadcastMessage("Couldn't get to target destination");
            checkLocation();
        }));
    }

    // find the closest structure based on a range around the npc, needed blocks, and the radius of the structure
    private Block search(Range searchRange, SearchType searchType, StructureReference reference) {
        List<GenericMaterial> neededBlocks = new ArrayList<>();
        Location fromLoc = getNPC().getLocation();
        Block chosenStructure = null;
        // search within search range to find any block in the first layer
        for (int x = searchRange.getMin(); x <= searchRange.getMax(); x++) {
            for (int y = searchRange.getMin(); y <= searchRange.getMax(); y++) { // can't see underground?
                for (int z = searchRange.getMin(); z <= searchRange.getMax(); z++) {
                    Location checkLoc = fromLoc.clone().add(x, y, z);
                    int validLayers = 0;
                    breakpoint:
                    if (reference.getLayers().get(0).contains(checkLoc.getBlock().getType())) {
                        int highestY = 0;
                        boolean startHighestY = true;
                        for (int i = 0; i < reference.getLayers().size(); i++) { // check all other layers
                            Layer layer = reference.getLayers().get(i);
                            neededBlocks.addAll(layer.getMaterials()); // reset needed blocks list
                            Location validationLoc = checkLoc.clone();
                            if (!startHighestY) validationLoc.setY(highestY + 1);
                            Bukkit.broadcastMessage("Checking " + LocationUtils.toStringBasic(validationLoc));
                            List<Block> foundBlocks = validateLayer(layer, validationLoc.getBlock(), reference);
                            // remove found blocks from needed blocks list
                            foundBlocks.forEach(block -> neededBlocks.remove(layer.getGenericMaterial(block.getType())));
                            // get highest y pos in found blocks
                            for (Block block : foundBlocks) if (block.getY() > highestY) highestY = block.getY();
                            startHighestY = false;
                            Bukkit.broadcastMessage("Highest y pos in found blocks: " + highestY);
                            if (neededBlocks.isEmpty()) { // found all the blocks in the layer
                                String postfixMsg = i == reference.getLayers().size() - 1 ?
                                        ". All layers validated" : ", validating other layers...";
                                Bukkit.broadcastMessage("Found all blocks in layer " + (i + 1) + postfixMsg);
                                validLayers++;
                            } else { // didn't find all the blocks in the layer
                                Bukkit.broadcastMessage("Didn't find all blocks in layer " + (i + 1) + ", searching another location...");
                                neededBlocks.clear();
                                break breakpoint;
                            }
                        }
                        if (validLayers != reference.getLayers().size()) break;
                        // if we passed the for loop then we have found a matching structure
                        if (chosenStructure != null) {
                            switch (searchType) { // compare to the current chosen structure based on the search type
                                case CLOSEST: {
                                    if (checkLoc.distance(fromLoc) < chosenStructure.getLocation().distance(fromLoc)) {
                                        chosenStructure = checkLoc.getBlock();
                                    }
                                }
                                case FURTHEST: {
                                    if (checkLoc.distance(fromLoc) > chosenStructure.getLocation().distance(fromLoc)) {
                                        chosenStructure = checkLoc.getBlock();
                                    }
                                }
                            }
                        } else chosenStructure = checkLoc.getBlock(); // first structure found
                    }
                }
            }
        }
        Bukkit.broadcastMessage("Found " + (chosenStructure == null ? "no" : "a") + " structure");
        return chosenStructure;
    }

    // look for the rest of the blocks within the layer based on the layer's minimum bounds
    /** @return the blocks found within the layer **/
    private List<Block> validateLayer(Layer layer, Block from, StructureReference reference) {
        List<Block> found = new ArrayList<>();
        int searchBoundsXz = reference.getBounds().getXz();
        int searchBoundsY = reference.getBounds().getY();
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
                        if (layer.getBounds() != null) {
                            tempList.add(checkLoc.getBlock()); // if the minimum bounds given are greater than one block tall
                            if (lowestYPosBlock > checkLoc.getBlockY()) lowestYPosBlock = checkLoc.getBlockY();
                        }
                        else found.add(checkLoc.getBlock());
                    }
                }
            }
        }
        if (!tempList.isEmpty()) {
            int minBoundsXz = layer.getBounds().getXz();
            int minBoundsY = layer.getBounds().getY();
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
        Bukkit.broadcastMessage("Found total of " + found.size() + " blocks within layer");
        return found;
    }

    public enum SearchType {
        CLOSEST,
        FURTHEST
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void onStop(StopType stopType) {

    }

    @Override
    public boolean canStart(StartType startType) {
        return true;
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }
}
