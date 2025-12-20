package io.github.derec4.elytraVaults.handlers;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LootTableHandler {

    private final JavaPlugin plugin;

    public LootTableHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void createElytraLootTable() {
        // plugins/[PluginName]/data/[namespace]/loot_tables/[name].json

        File lootTableDir = new File(plugin.getDataFolder(),
                "data/" + plugin.getName().toLowerCase() + "/loot_tables");

        if (!lootTableDir.exists()) {
            lootTableDir.mkdirs();
        }

        File lootTableFile = new File(lootTableDir, "elytra_vault.json");

        if (lootTableFile.exists()) {
            plugin.getLogger().info("Elytra loot table already exists");
            return;
        }

        // Credits: AtlasPlays
        String lootTableJson = """
                {
                  "pools": [
                      {
                          "rolls": 1,
                          "entries": []
                      },
                      {
                          "rolls": 1,
                          "entries": [
                              {
                                  "type": "minecraft:item",
                                  "name": "minecraft:elytra"
                              }
                          ]
                      }
                  ]
                }
                """;

        try (FileWriter writer = new FileWriter(lootTableFile)) {
            writer.write(lootTableJson);
            plugin.getLogger().info("Created Elytra loot table: " + lootTableFile.getPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create loot table: " + e.getMessage());
        }
    }
}