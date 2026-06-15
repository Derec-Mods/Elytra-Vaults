package io.github.derec4.elytraVaults.handlers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DatapackHandler {

    private static final String DATAPACK_NAME = "ElytraVaultsHelper";
    private final JavaPlugin plugin;

    public DatapackHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean serverUsesNewWorldLayout() {
        try {
            String ver = null;
            try {
                ver = Bukkit.getMinecraftVersion();
            } catch (NoSuchMethodError | NoClassDefFoundError e) {
                plugin.getLogger().severe(e.getMessage());
            }

            if (ver == null || ver.isEmpty()) {
                ver = Bukkit.getBukkitVersion();
            }

            if (ver == null) {
                return false;
            }

            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?").matcher(ver);
            if (m.find()) {
                int major = Integer.parseInt(m.group(1));
                return major >= 26; // threshold where layout changed (Paper/Minecraft 26+)
            }
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public void installDatapack() {
        World world = Bukkit.getWorlds().getFirst();

        if (world == null) {
            plugin.getLogger().severe("Could not find main world for datapack installation!");
            return;
        }

        // Prefer the server world container + world name for newer server layouts introduced in recent
        // Minecraft/Paper versions. Fall back to the Bukkit-provided world folder for older servers.
        File datapacksFolder;
        try {
            if (serverUsesNewWorldLayout()) {
                File worldRoot = new File(Bukkit.getWorldContainer(), world.getName());
                if (worldRoot.exists()) {
                    datapacksFolder = new File(worldRoot, "datapacks");
                } else {
                    datapacksFolder = new File(world.getWorldFolder(), "datapacks");
                }
            } else {
                datapacksFolder = new File(world.getWorldFolder(), "datapacks");
            }
        } catch (Exception ex) {
            datapacksFolder = new File(world.getWorldFolder(), "datapacks");
        }
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
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:datapack list");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:datapack enable \"file/" + DATAPACK_NAME +
                    "\"");
//            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload");
            plugin.getLogger().info("Datapack enabled");
        }, 20L);
    }
}
