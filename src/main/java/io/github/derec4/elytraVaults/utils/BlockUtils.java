package io.github.derec4.elytraVaults.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockUtils {
    public static Block placeBlock(Location location, Material block) {
        Block targetBlock = location.getBlock();
        targetBlock.setType(block);
        return targetBlock;
    }

    public static Block debugBlock(Location location) {
        Block targetBlock = location.getBlock();
        targetBlock.setType(Material.BEDROCK);
        return targetBlock;
    }
}
