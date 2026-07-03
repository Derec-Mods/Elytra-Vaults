package io.github.derec4.elytraVaults.listeners;

import io.github.derec4.elytraVaults.ElytraVaults;
import io.github.derec4.elytraVaults.integrations.AntiDupeSupport;
import io.github.derec4.elytraVaults.utils.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Paper-only: when one of our vaults ejects its reward, pre-stamp the elytra with the
 * opener's AntiDupePro ownership tag so ADP treats it as a normally-owned item instead
 * of an untracked one that appeared from nowhere (which it would otherwise flag).
 *
 * Registered conditionally from ElytraVaults#onEnable — BlockDispenseLootEvent only
 * exists on Paper, so this class is never loaded on plain Spigot.
 */
public class VaultLootListener implements Listener {

    private final ElytraVaults plugin;

    public VaultLootListener(ElytraVaults plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDispenseLoot(BlockDispenseLootEvent event) {
        if (event.getBlock().getType() != Material.VAULT) return;
        if (!(event.getBlock().getState() instanceof Vault vault)) return;

        // Only touch OUR vaults, identified by our loot table.
        LootTable ours = Bukkit.getLootTable(BlockUtils.getElytraVaultLootTableKey());
        if (ours == null || !ours.equals(vault.getLootTable())) return;

        Player player = event.getPlayer();
        if (player == null) return;

        List<ItemStack> loot = new ArrayList<>(event.getDispensedLoot());
        boolean changed = false;
        for (ItemStack it : loot) {
            if (it != null && it.getType() == Material.ELYTRA) {
                AntiDupeSupport.tagOwner(it, player.getUniqueId());
                changed = true;
            }
        }
        if (changed) event.setDispensedLoot(loot);
    }
}
