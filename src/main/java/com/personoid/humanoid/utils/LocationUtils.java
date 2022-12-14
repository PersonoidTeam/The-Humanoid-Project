package com.personoid.humanoid.utils;

import com.personoid.api.PersonoidAPI;
import com.personoid.api.npc.NPC;
import com.personoid.api.utils.math.Range;
import com.personoid.api.utils.types.BlockTags;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class LocationUtils {
    private static final List<BlockFace> relativeBlockFaces = new ArrayList<>();

    static {
        relativeBlockFaces.add(BlockFace.NORTH);
        relativeBlockFaces.add(BlockFace.SOUTH);
        relativeBlockFaces.add(BlockFace.EAST);
        relativeBlockFaces.add(BlockFace.WEST);
    }

    public static NPC getClosestNPC(Location location, List<NPC> excluding) {
        NPC closestNPC = null;
        for (NPC npc : PersonoidAPI.getRegistry().getNPCs()) {
            if ((closestNPC == null || npc.getLocation().distance(location) < closestNPC.getLocation().distance(location)) && !excluding.contains(npc)) {
                closestNPC = npc;
            }
        }
        return closestNPC;
    }

    public static NPC getClosestNPC(Location location) {
        NPC closestNPC = null;
        for (NPC npc : PersonoidAPI.getRegistry().getNPCs()) {
            if (closestNPC == null || npc.getLocation().distance(location) < closestNPC.getLocation().distance(location)) {
                closestNPC = npc;
            }
        }
        return closestNPC;
    }

    public static Block getBlockInDir(Location location, BlockFace direction) {
        while (true) {
            if (location.getBlock().getType().isSolid()) {
                return location.getBlock();
            } else if (BlockTags.CLIMBABLE.is(location.getBlock().getType())) {
                return location.getBlock();
            }
            location = location.getBlock().getRelative(direction).getLocation();
        }
    }

    public static Block getAirInDir(Location location, BlockFace direction) {
        while (true) {
            location = location.getBlock().getRelative(direction).getLocation();
            if (location.getBlock().getType().isAir()) {
                return location.getBlock();
            }
        }
    }

    public static boolean canReach(Location to, Location from){
        return from.clone().add(0, 2, 0).distance(to) < 5;
    }

    public static Location getPathableLocation(Location to, Location from){
        Location validLocation = to.clone();
        Location justDownwards = null;
        for (int i = 1; i <= 10; i++){
            if (!to.clone().subtract(0,i,0).getBlock().getType().isSolid()){
                Location above = to.clone().subtract(0,i,0);
                above.add(0,1,0);
                if (!above.getBlock().getType().isSolid()){
                    justDownwards = above.clone().subtract(0,1,0);
                }
            }
        }
        if (justDownwards != null){
            return justDownwards;
        }
        else {
            for (BlockFace blockFace : relativeBlockFaces){
                Block adjacent = to.getBlock().getRelative(blockFace);
                Location location = validDownwardsSearch(adjacent.getLocation());
                if (location != null){
                    if (validLocation == to){
                        validLocation = location;
                    }
                    else {
                        if (location.distance(from) < validLocation.distance(from)){
                            validLocation = location;
                        }
                    }
                }

            }
        }
        return validLocation.add(0.5,0.5,0.5);
    }

    private static Location validDownwardsSearch(Location location){
        Location valid = null;
        for (int i = 0; i <= 10; i++){
            Location checkedUnder = location.clone().subtract(0,i,0);
            if (!checkedUnder.clone().add(0,1,0).getBlock().getType().isSolid()){
                if (!checkedUnder.getBlock().getType().isSolid()){
                    valid = checkedUnder;
                    break;
                }
            }
        }
        return valid;
    }

    public static boolean solidAt(Location loc) {
        Block block = loc.getBlock();
        BoundingBox box = block.getBoundingBox();
        Vector position = loc.toVector();

        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();

        double minX = box.getMinX();
        double minY = box.getMinY();
        double minZ = box.getMinZ();

        double maxX = box.getMaxX();
        double maxY = box.getMaxY();
        double maxZ = box.getMaxZ();

        return x > minX && x < maxX && y > minY && y < maxY && z > minZ && z < maxZ;
    }

    public static boolean isSolid(Block block) {
        return block.getType().isSolid() || block.getType().toString().contains("LEAVES");
    }

    public static Block rayTraceBlocks(Location from, Location to, int maxDistance, boolean stopOnLiquid){
        Block block = null;
        Vector vector = to.toVector().subtract(from.toVector()).normalize();
        vector = vector.normalize();
        for (int i = 1; i <= maxDistance; i++){
            Location loc = from.clone().add(vector.clone().multiply(i));
            Block b1 = loc.getBlock();
            loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 5, new Particle.DustTransition(Color.BLUE, Color.AQUA, 1));
            if (LocationUtils.isSolid(b1)){
                block = b1;
                break;
            }
            else if (stopOnLiquid && b1.isLiquid()){
                block = b1;
                break;
            }
        }
        return block;
    }

    public static Block getBlockInFront(Location location, int distance) {
        BlockIterator blocks = new BlockIterator(location, 1, distance);
        Block lastNonSolidBlock = null;
        while (blocks.hasNext()) {
            Block block = blocks.next();
            if (block.getType().isSolid()) {
                return lastNonSolidBlock;
            } else {
                lastNonSolidBlock = block;
            }
        }
        return null;
    }

    public static Location validRandom(Location from, Range range, float directionBias) {
        Location loc = from.clone();
        int x = MathUtils.random(range.getMin(), range.getMax());
        int z = MathUtils.random(range.getMin(), range.getMax());
        x *= Math.random() < directionBias ? 1 : -1;
        z *= Math.random() < directionBias ? 1 : -1;
        loc.add(x, 0, z).setY(320);
        return getBlockInDir(loc, BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
    }

    public static boolean isSolid(Location location) {
        return location.getBlock().getType().isSolid();
    }

    public static boolean canStandAt(Location location) {
        return !isSolid(location) && !isSolid(location.clone().add(0, 1, 0)) && isSolid(location.clone().add(0, -1, 0));
    }

    public static Location fromString(String string) {
        String[] split = string.split(",");
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
    }

    public static String toStringBasic(Location location) {
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }
}
