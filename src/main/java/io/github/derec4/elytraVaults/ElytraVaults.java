package io.github.derec4.elytraVaults;

import io.github.derec4.elytraVaults.config.ConfigManager;
import io.github.derec4.elytraVaults.handlers.LootTableHandler;
import io.github.derec4.elytraVaults.listeners.SpawnVaultListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class ElytraVaults extends JavaPlugin {

    private ConfigManager configManager;
    private LootTableHandler lootTableHandler;

    @Override
    public void onEnable() {
        // Initialize config
        configManager = new ConfigManager(this);

        getServer().getPluginManager().registerEvents(new SpawnVaultListener(this), this);

        // Plugin startup logic
        Bukkit.getLogger().info("");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "  |_______|                             " +
                "  ");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "  | Derex |     Elytra Vaults v" + getDescription().getVersion());
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "  |_______|     Original by atlasplays");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "  |_______|     Running on " + Bukkit.getName() + " - " + Bukkit.getVersion());
        Bukkit.getLogger().info("");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
