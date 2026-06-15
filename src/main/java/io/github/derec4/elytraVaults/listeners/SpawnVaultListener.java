package io.github.derec4.elytraVaults.listeners;

import io.github.derec4.elytraVaults.ElytraVaults;
import io.github.derec4.elytraVaults.utils.BlockUtils;
import io.github.derec4.elytraVaults.utils.TextDisplayUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Vault;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static io.github.derec4.elytraVaults.utils.BlockUtils.createElytraVault;
import static io.github.derec4.elytraVaults.utils.TextDisplayUtils.spawnVaultTextDisplays;

public class SpawnVaultListener implements Listener {

    private final ElytraVaults plugin;

    public SpawnVaultListener(ElytraVaults plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.getWorld().getEnvironment() != World.Environment.THE_END) {
            return;
        }

        if (event.isNewChunk()) {
            processItemFrames(event);
        } else {
            processVaultBlocks(event);
            processTextDisplays(event);
        }
    }

    /**
     * Processes all entities in the loaded chunk and searches for an Elytra Item Frame entity
     */
    private void processItemFrames(ChunkLoadEvent event) {
        for (var entity : event.getChunk().getEntities()) {
            if (entity.getType() != EntityType.ITEM_FRAME) {
                continue;
            }

            ItemFrame frame = (ItemFrame) entity;
            ItemStack item = frame.getItem();

            if (item.getType() != Material.ELYTRA) {
                continue;
            }

            plugin.getLogger().info("Elytra ItemFrame detected at " + frame.getLocation());

            if (shouldProcessFrame(frame)) {
                processFrame(frame);
            }
        }
    }

    /**
     * Determines if a frame should be processed
     */
    private boolean shouldProcessFrame(ItemFrame frame) {
        return true;
    }

    /**
     * Processes a valid Elytra item frame
     * First we will mark it as processed, so we do not check it again, ever.
     * The Vault block is then placed
     * Text display is placed as well
     */
    private void processFrame(ItemFrame frame) {
//        pdcManager.markAsProcessed(frame);
//        Block placedBlock = placeBlock(frame.getLocation(), Material.VAULT);

        Location frameLocation = frame.getLocation();
        plugin.getLogger().info("Processing frame at " + frameLocation);

        var vaultLocation = frameLocation.clone().subtract(0, 1, 0);
        Block vaultBlock = vaultLocation.getBlock();
        plugin.getLogger().info("Placing vault at " + vaultLocation);

        // Extra debug: world/datapacks details
        try {
            World world = frame.getWorld();
            Path worldFolder = world.getWorldFolder().toPath();
            Path datapacksDir = worldFolder.resolve("datapacks");
            plugin.getLogger().info("World folder: " + worldFolder.toAbsolutePath());
            plugin.getLogger().info("Datapacks dir exists: " + Files.exists(datapacksDir));
            if (Files.exists(datapacksDir)) {
                try {
                    var entries = Files.list(datapacksDir)
                            .map(Path::getFileName)
                            .map(Path::toString)
                            .collect(Collectors.joining(", "));
                    plugin.getLogger().info("Datapacks entries: [" + entries + "]");
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed listing datapacks dir: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to read world folder/datapacks: " + e.getMessage());
        }

        Material keyItemMaterial = plugin.getConfigManager().getKeyItem();

        // Debug expected loot table key
        try {
            var expectedKey = BlockUtils.getElytraVaultLootTableKey();
            plugin.getLogger().info("Expected Elytra Vault loot table key: " + expectedKey);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to get expected loot table key: " + e.getMessage());
        }

        // Wrap createElytraVault with more context logging to catch new-format issues
        try {
            plugin.getLogger().info("Calling createElytraVault for block " + vaultBlock.getLocation() + " with key item " + keyItemMaterial);
            createElytraVault(vaultBlock, plugin, keyItemMaterial);
            plugin.getLogger().info("Vault created with key item: " + keyItemMaterial);
        } catch (Throwable t) {
            plugin.getLogger().severe("Failed to create Elytra Vault at " + vaultBlock.getLocation() + ": " + t.getClass().getName() + " - " + t.getMessage());
            t.printStackTrace();
            // log some block info
            try {
                plugin.getLogger().severe("Vault block type: " + vaultBlock.getType());
                plugin.getLogger().severe("Vault block state: " + vaultBlock.getState());
            } catch (Exception ex) {
                plugin.getLogger().severe("Failed to read vault block details: " + ex.getMessage());
            }
            return; // bail out so we don't remove the frame if creation failed
        }

        if (plugin.getConfigManager().isTextDisplayEnabled()) {
            plugin.getLogger().info("Spawning text displays at " + vaultLocation);
            spawnVaultTextDisplays(vaultLocation, keyItemMaterial);
        }

        frame.remove();
        plugin.getLogger().info("Frame removed");
    }

    /**
     * Processes all block entities to update all the vaults
     */
    private void processVaultBlocks(ChunkLoadEvent event) {
        for (var block : event.getChunk().getTileEntities()) {
            if (block.getType() != Material.VAULT) {
                continue;
            }
            Vault vault = (Vault) block;
            LootTable lootTable;
            try {
                lootTable = vault.getLootTable();
            } catch (Throwable t) {
                plugin.getLogger().severe("Error getting loot table from vault at " + vault.getLocation() + ": " + t.getClass().getName() + " - " + t.getMessage());
                t.printStackTrace();
                continue;
            }
            if (lootTable == null) {
                plugin.getLogger().info("Vault at " + vault.getLocation() + " has null LootTable (skipping)");
                continue;
            }
            try {
                plugin.getLogger().info("Vault loot table key: " + lootTable.getKey());
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to read loot table key for vault at " + vault.getLocation() + ": " + e.getMessage());
            }
            if (!lootTable.getKey().equals(BlockUtils.getElytraVaultLootTableKey())) {
                continue;
            }
            plugin.getLogger().info("Elytra Vault detected at " + vault.getLocation());
            processVault(vault);
        }
    }

    /**
     * Processes an Elytra Vault
     * This method updates the key item
     */
    private void processVault(Vault vault) {
        Material keyItemMaterial = plugin.getConfigManager().getKeyItem();
        vault.setKeyItem(new ItemStack(keyItemMaterial));
        vault.update();
    }

    /**
     * Processes all text displays
     */
    private void processTextDisplays(ChunkLoadEvent event) {
        for (var entity : event.getChunk().getEntities()) {
            if (entity.getType() != EntityType.TEXT_DISPLAY) {
                continue;
            }
            TextDisplay textDisplay = (TextDisplay) entity;
            if (textDisplay.text().toString().contains("Open With ")
                    && textDisplay.getScoreboardTags().contains("elytra_vault_text")) {
                plugin.getLogger().info("Key Item Text Display detected at " + textDisplay.getLocation());
                processKeyItemTextDisplay(textDisplay);
            }
        }
    }

    /**
     * Processes a Text Display
     * This method updates the shown item in the display
     */
    private void processKeyItemTextDisplay(TextDisplay textDisplay) {
        // This works perfectly fine now, but it may not have good enough stability.
        Material keyItemMaterial = plugin.getConfigManager().getKeyItem();
        textDisplay.text(TextDisplayUtils.getKeyDisplayText(keyItemMaterial));
    }
}
