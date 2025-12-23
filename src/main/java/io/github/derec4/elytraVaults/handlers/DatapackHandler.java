package io.github.derec4.elytraVaults.handlers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class DatapackHandler {

    private final JavaPlugin plugin;
    private static final String DATAPACK_NAME = "ElytraVaultsHelper";

    public DatapackHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void installDatapack() {
        // Get the main world (overworld)
        World world = Bukkit.getWorlds().getFirst();

        if (world == null) {
            plugin.getLogger().severe("Could not find main world for datapack installation!");
            return;
        }

        File datapacksFolder = new File(world.getWorldFolder(), "datapacks");
        File targetDatapackFolder = new File(datapacksFolder, DATAPACK_NAME);

        if (targetDatapackFolder.exists() && isValidDatapack(targetDatapackFolder)) {
            plugin.getLogger().info("Datapack '" + DATAPACK_NAME + "' already exists in world datapacks");
            reloadDatapacks();
            return;
        }

        // Create datapacks folder if it doesn't exist and copy it to the world datapack folder
        if (!datapacksFolder.exists()) {
            datapacksFolder.mkdirs();
        }

        try {
            copyDatapackFromResources(targetDatapackFolder);
            plugin.getLogger().info("Successfully installed datapack '" + DATAPACK_NAME + "' to " + targetDatapackFolder.getPath());
            
            // Reload datapacks so the server registers it
            reloadDatapacks();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to install datapack: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Copies the datapack from plugin resources to the target folder. Duct taped thing, but it works.
     */
    private void copyDatapackFromResources(File targetFolder) throws IOException {
        targetFolder.mkdirs();

        copyResourceFile(DATAPACK_NAME + "/pack.mcmeta", new File(targetFolder, "pack.mcmeta"));

        // Create the data structure
        File dataFolder = new File(targetFolder, "data/elytra_vault/loot_table");
        dataFolder.mkdirs();

        // Copy loot table
        copyResourceFile(
            DATAPACK_NAME + "/data/elytra_vault/loot_table/elytra_vault.json",
            new File(dataFolder, "elytra_vault.json")
        );
    }

    private void copyResourceFile(String resourcePath, File targetFile) throws IOException {
        try (InputStream inputStream = plugin.getResource(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            
            Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().fine("Copied resource: " + resourcePath + " to " + targetFile.getPath());
        }
    }

    private boolean isValidDatapack(File datapackFolder) {
        File packMeta = new File(datapackFolder, "pack.mcmeta");
        return packMeta.exists() && packMeta.isFile();
    }

    private void reloadDatapacks() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:datapack list");
                plugin.getLogger().info("Datapacks reloaded");
            } catch (Exception e) {
                plugin.getLogger().warning("Could not reload datapacks automatically: " + e.getMessage());
                plugin.getLogger().warning("You may need to run '/reload' or restart the server");
            }
        });
    }
}
