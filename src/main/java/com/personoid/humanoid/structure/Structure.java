package com.personoid.humanoid.structure;

import com.personoid.humanoid.material.GenericMaterial;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Structure {
    private final List<Block> blocks;

    public Structure(List<Block> blocks) {
        this.blocks = blocks.stream().distinct().collect(Collectors.toList());
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public Block getOrigin() {
        return blocks.get(0);
    }

    public boolean contains(List<Block> blocks, float percent) {
        int count = 0;
        for (Block block : blocks) {
            if (this.blocks.contains(block)) {
                count++;
            }
        }
        return count >= blocks.size() * percent;
    }

    public List<Block> getBlocksFrom(GenericMaterial material, Direction direction) {
        List<Block> blocks = new ArrayList<>();
        for (Block block : this.blocks) {
            if (material.matches(block.getType())) {
                blocks.add(block);
            }
        }
        if (direction == Direction.UP) {
            blocks.sort(Structure::compareY);
        } else if (direction == Direction.DOWN) {
            blocks.sort((block1, block2) -> compareY(block2, block1));
        } else if (direction == Direction.LEFT) {
            blocks.sort(Structure::compareXZ);
        } else if (direction == Direction.RIGHT) {
            blocks.sort((block1, block2) -> Structure.compareXZ(block2, block1));
        }
        return blocks;
    }

    private static int compareXZ(Block block1, Block block2) {
        if (block1.getX() + block1.getZ() > block2.getX() + block2.getZ()) {
            return 1;
        } else if (block1.getX() + block1.getZ() < block2.getX() + block2.getZ()) {
            return -1;
        }
        return 0;
    }

    private static int compareY(Block block1, Block block2) {
        if (block1.getY() > block2.getY()) {
            return 1;
        } else if (block1.getY() < block2.getY()) {
            return -1;
        }
        return 0;
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
    }
}
