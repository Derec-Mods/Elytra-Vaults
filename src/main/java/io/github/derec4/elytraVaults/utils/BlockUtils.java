package io.github.derec4.elytraVaults.utils;

import io.github.derec4.elytraVaults.integrations.AntiDupeSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Vault;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.java.JavaPlugin;

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

    public static NamespacedKey getElytraVaultLootTableKey() {
        return new NamespacedKey("elytra_vault", "elytra_vault");
    }

    public static Vault createElytraVault(Block block, JavaPlugin plugin, Material keyItemMaterial) {
        // The elytra reward comes from the ElytraVaultsHelper datapack loot table. Doing it this
        // way keeps Spigot support (a Paper-only approach would exclude Spigot servers).
        block.setType(Material.VAULT);

        if (!(block.getState() instanceof Vault vault)) {
            return null;
        }

        LootTable lootTable = Bukkit.getLootTable(getElytraVaultLootTableKey());
        if (lootTable == null) {
            // The datapack didn't load (usually an out-of-date pack.mcmeta on a newer MC version).
            // Bail loudly instead of silently letting the vault serve vanilla trial-chamber loot.
            plugin.getLogger().severe("Elytra vault loot table not loaded — is the ElytraVaultsHelper datapack enabled?"
                    + " Check /datapack list. Skipping vault creation so it can't hand out trial loot instead.");
            block.setType(Material.AIR); // don't leave a broken vault behind
            return null;
        }

        // Strip any AntiDupePro ownership tag off the key item. A tagged key would make the vault
        // demand that exact tag and open for nobody. No-op when AntiDupePro isn't installed.
        ItemStack keyItem = new ItemStack(keyItemMaterial);
        AntiDupeSupport.stripOwnership(keyItem);

        vault.setKeyItem(keyItem);
        vault.setLootTable(lootTable);
        vault.setDisplayedItem(new ItemStack(Material.ELYTRA));
        vault.update();
        return vault;
    }
}
