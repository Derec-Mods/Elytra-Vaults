package io.github.derec4.elytraVaults;

import io.github.derec4.elytraVaults.config.ConfigManager;
import io.github.derec4.elytraVaults.handlers.DatapackHandler;
import io.github.derec4.elytraVaults.listeners.SpawnVaultListener;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

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

        // Plugin startup logic
        this.getLogger().info("");
        Bukkit.getServer().getConsoleSender().sendMessage(NamedTextColor.GREEN + "  |_______|                             " +
                "  ");
        Bukkit.getServer().getConsoleSender().sendMessage(NamedTextColor.GREEN + "  | Derex |     Elytra Vaults v" + getPluginMeta().getVersion());
        Bukkit.getServer().getConsoleSender().sendMessage(NamedTextColor.GREEN + "  |_______|     Original by atlasplays");
        Bukkit.getServer().getConsoleSender().sendMessage(NamedTextColor.GREEN + "  |_______|     Running on " + Bukkit.getName() + " - " + Bukkit.getVersion());
        this.getLogger().info("");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
