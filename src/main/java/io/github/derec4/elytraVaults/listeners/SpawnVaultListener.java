package io.github.derec4.elytraVaults.listeners;

import io.github.derec4.elytraVaults.ElytraVaults;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import static io.github.derec4.elytraVaults.utils.BlockUtils.placeBlock;

public class SpawnVaultListener implements Listener {


    public SpawnVaultListener() {
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.getWorld().getEnvironment() != World.Environment.THE_END) {
            return;
        }

        if (!event.isNewChunk()) {
            return;
        }

        processChunkEntities(event);
    }

    /**
     * Processes all entities in the loaded chunk and searches for an Elytra Item Frame entity
     */
    private void processChunkEntities(ChunkLoadEvent event) {
        for (var entity : event.getChunk().getEntities()) {
            if (entity.getType() != EntityType.ITEM_FRAME) {
                continue;
            }

            ItemFrame frame = (ItemFrame) entity;
            ItemStack item = frame.getItem();

            if (item.getType() != Material.ELYTRA) {
                continue;
            }

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

        Block placedBlock = placeBlock(frame.getLocation(), Material.VAULT);

//        if (plugin.getConfigManager().isTextDisplayEnabled()) {
//            textDisplayManager.spawnDisplay(placedBlock);
//        }

        // Remove the item frame
        frame.remove();

    }
}