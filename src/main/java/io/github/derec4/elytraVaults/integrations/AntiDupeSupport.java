package io.github.derec4.elytraVaults.integrations;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Soft, dependency-free integration with AntiDupePro (ADP).
 *
 * ADP marks "valuable" items with an invisible owner note — a PersistentDataContainer
 * STRING (the holder's UUID) stored under a key an admin can rename (default
 * "antidupepro:adp_owner"). Everything here goes through public Bukkit API plus ADP's
 * own config/data files, so ElytraVaults never links against ADP and runs fine whether
 * or not it is installed.
 *
 * NOTE: uses getItemMeta()/setItemMeta() (not Paper's editMeta) so the class loads on
 * plain Spigot too.
 */
public final class AntiDupeSupport {

    private AntiDupeSupport() {}

    private static Plugin adp() {
        Plugin p = Bukkit.getPluginManager().getPlugin("AntiDupePro");
        return (p != null && p.isEnabled()) ? p : null;
    }

    public static boolean isPresent() {
        return adp() != null;
    }

    /** Every ownership key ADP currently recognises (configured primary + legacy + rename marker). */
    public static List<NamespacedKey> ownershipKeys() {
        Plugin plugin = adp();
        if (plugin == null) return Collections.emptyList();

        var cfg = plugin.getConfig();
        String defaultNs = plugin.getName().toLowerCase(Locale.ROOT);
        LinkedHashSet<NamespacedKey> keys = new LinkedHashSet<>();

        String ns = cfg.getString("ownership.namespace", defaultNs).toLowerCase(Locale.ROOT).trim();
        String key = cfg.getString("ownership.key", "adp_owner").toLowerCase(Locale.ROOT).trim();
        NamespacedKey primary = ns.equals("minecraft") ? null : NamespacedKey.fromString(ns + ":" + key);
        if (primary == null) primary = NamespacedKey.fromString(defaultNs + ":adp_owner");
        if (primary != null) keys.add(primary);

        for (String raw : cfg.getStringList("ownership.legacy_keys")) {
            NamespacedKey k = NamespacedKey.fromString(raw.toLowerCase(Locale.ROOT).trim());
            if (k != null) keys.add(k);
        }
        try {
            File marker = new File(plugin.getDataFolder(), "ownership-key");
            if (marker.isFile()) {
                NamespacedKey k = NamespacedKey.fromString(
                        java.nio.file.Files.readString(marker.toPath()).trim());
                if (k != null) keys.add(k);
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>(keys);
    }

    /** Stamp the item as owned by this player, under ADP's primary key. No-op without ADP. */
    public static void tagOwner(ItemStack item, UUID owner) {
        List<NamespacedKey> keys = ownershipKeys();
        if (keys.isEmpty()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(keys.get(0), PersistentDataType.STRING, owner.toString());
        item.setItemMeta(meta);
    }

    /**
     * Remove ADP ownership tags from an item — used on the vault KEY item. A key carrying an
     * ownership tag would make the vault demand that exact tag from every player, so nobody
     * could ever open it. Belt-and-braces: also drops any non-minecraft STRING entry whose value
     * parses as a UUID (a renamed/de-branded key still stores the holder UUID).
     *
     * @return true if anything was removed.
     */
    public static boolean stripOwnership(ItemStack item) {
        if (!isPresent()) return false;
        Set<NamespacedKey> declared = new HashSet<>(ownershipKeys());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        var pdc = meta.getPersistentDataContainer();
        boolean removed = false;
        for (NamespacedKey k : new ArrayList<>(pdc.getKeys())) {
            String v = pdc.get(k, PersistentDataType.STRING);
            boolean hit = declared.contains(k)
                    || (!k.getNamespace().equals(NamespacedKey.MINECRAFT) && v != null && isUuid(v));
            if (hit) {
                pdc.remove(k);
                removed = true;
            }
        }
        if (removed) item.setItemMeta(meta);
        return removed;
    }

    /** True if ADP tracks this material — a tracked material makes a broken (unopenable) vault key. */
    public static boolean isTrackedMaterial(Material material) {
        Plugin plugin = adp();
        if (plugin == null) return false;
        if (material.name().endsWith("SHULKER_BOX")) return true; // hardcoded-tracked in ADP
        try {
            File f = new File(plugin.getDataFolder(), "materials.yml");
            if (!f.isFile()) return false;
            return YamlConfiguration.loadConfiguration(f)
                    .getStringList("tracked_materials").stream()
                    .anyMatch(s -> Material.matchMaterial(s) == material);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isUuid(String s) {
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
