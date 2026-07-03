package io.github.derec4.elytraVaults;

import io.github.derec4.elytraVaults.config.ConfigManager;
import io.github.derec4.elytraVaults.handlers.DatapackHandler;
import io.github.derec4.elytraVaults.integrations.AntiDupeSupport;
import io.github.derec4.elytraVaults.listeners.SpawnVaultListener;
import io.github.derec4.elytraVaults.listeners.VaultLootListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

public final class ElytraVaults extends JavaPlugin {

    private ConfigManager configManager;
    private DatapackHandler datapackHandler;

    @Override
    public void onEnable() {
        // Initialize config
        configManager = new ConfigManager(this);

        // Install datapack to world folder if not present
        datapackHandler = new DatapackHandler(this);
        datapackHandler.installDatapack();

        getServer().getPluginManager().registerEvents(new SpawnVaultListener(this), this);

        // AntiDupePro-friendly loot tagging needs Paper's BlockDispenseLootEvent.
        // Registered only when that class exists, so plain Spigot/Bukkit still load cleanly.
        try {
            Class.forName("org.bukkit.event.block.BlockDispenseLootEvent");
            getServer().getPluginManager().registerEvents(new VaultLootListener(this), this);
        } catch (ClassNotFoundException e) {
            getLogger().info("Paper not detected — AntiDupePro loot tagging disabled (everything else works normally).");
        }

        warnIfKeyItemTracked();
        warnIfStandaloneDatapackPresent();

        // Plugin startup logic
        Bukkit.getLogger().info("");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "  |_______|                             " +
                "  ");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "  | Derex |     Elytra Vaults v" + getDescription().getVersion());
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "  |_______|     Original by atlasplays, improved by darkstarworks");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "  |_______|     Running on " + Bukkit.getName() + " - " + Bukkit.getVersion());
        Bukkit.getLogger().info("");
    }

    /** Warn if the configured key material is one AntiDupePro tracks (would make an unopenable vault). */
    private void warnIfKeyItemTracked() {
        Material keyItem = configManager.getKeyItem();
        if (AntiDupeSupport.isTrackedMaterial(keyItem)) {
            getLogger().warning("key-item " + keyItem + " is tracked by AntiDupePro — every player's copy carries a"
                    + " unique ownership tag, so it will NEVER match the vault's key and the vault can't be opened."
                    + " Pick a material AntiDupePro doesn't track.");
        }
    }

    /** Warn if the standalone datapack edition is also installed (both would convert the same frames). */
    private void warnIfStandaloneDatapackPresent() {
        boolean present = Bukkit.getWorlds().stream()
                .flatMap(w -> {
                    File[] packs = new File(w.getWorldFolder(), "datapacks").listFiles();
                    return packs == null ? Stream.<File>empty() : Arrays.stream(packs);
                })
                .anyMatch(f -> f.getName().toLowerCase(Locale.ROOT).startsWith("elytra vaults"));
        if (present) {
            getLogger().warning("The standalone Elytra Vaults DATAPACK is also installed — on a plugin server you only"
                    + " need one of the two. Remove the datapack to avoid double-converting ship frames.");
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
