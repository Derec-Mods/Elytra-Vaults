package io.github.derec4.elytraVaults.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Vault;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;

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
        // 12.19.2025 after looking a ton, this part with loot table can be done SO easily by adding a datapack
        // with this plugin using Paper, but then Spigot servers cant use it. For now I will manually create since
        // it is just one loot table
        block.setType(Material.VAULT);

        if (!(block.getState() instanceof Vault vault)) {
            return null;
        }

        LootTable lootTable
        vault.setKeyItem(new ItemStack(Material.SHULKER_SHELL));
        vault.loot

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
