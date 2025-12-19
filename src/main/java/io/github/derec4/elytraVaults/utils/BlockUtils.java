package io.github.derec4.elytraVaults.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Vault;

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

    public static Block createElytraVault (Block block) {
        block.setType(Material.VAULT);

        if (!(block.getState() instanceof Vault vault)) {
            return null;
        }


//
//        vault.getConfig().setKeyItem(keyItem.clone());
//        LootTable lootTable = plugin.getServer().getLootTable(lootTableKey);
//        vault.getConfig().setLootTable(lootTable);
//
//        vault.getSharedData().setDisplayItem(displayItem.clone());
//        vault.getSharedData().setLootTable(lootTable);
//
//        // Apply changes
//        vault.update();
//
        return block;

    }
}
