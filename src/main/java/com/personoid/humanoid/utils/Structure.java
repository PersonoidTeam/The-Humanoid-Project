package com.personoid.humanoid.utils;

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

    public List<Block> getBlocksFrom(GenericMaterial material, Direction direction) {
        List<Block> blocks = new ArrayList<>();
        for (Block block : this.blocks) {
            if (material.matches(block.getType())) {
                blocks.add(block);
            }
        }
        // sort blocks by material and direction
        // if direction is up, sort by y ascending
        // if direction is down, sort by y descending
        // if direction is left, sort by xz ascending
        // if direction is right, sort by xz descending
        if (direction == Direction.UP) {
            blocks.sort((block1, block2) -> {
                if (block1.getY() > block2.getY()) {
                    return 1;
                } else if (block1.getY() < block2.getY()) {
                    return -1;
                }
                return 0;
            });
        } else if (direction == Direction.DOWN) {
            blocks.sort((block1, block2) -> {
                if (block1.getY() < block2.getY()) {
                    return 1;
                } else if (block1.getY() > block2.getY()) {
                    return -1;
                }
                return 0;
            });
        } else if (direction == Direction.LEFT) {
            blocks.sort((block1, block2) -> {
                if (block1.getX() + block1.getZ() > block2.getX() + block2.getZ()) {
                    return 1;
                } else if (block1.getX() + block1.getZ() < block2.getX() + block2.getZ()) {
                    return -1;
                }
                return 0;
            });
        } else if (direction == Direction.RIGHT) {
            blocks.sort((block1, block2) -> {
                if (block1.getX() + block1.getZ() < block2.getX() + block2.getZ()) {
                    return 1;
                } else if (block1.getX() + block1.getZ() > block2.getX() + block2.getZ()) {
                    return -1;
                }
                return 0;
            });
        }
        return blocks;
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
    }
}
