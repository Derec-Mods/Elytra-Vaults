package io.github.derec4.elytraVaults.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

public class TextDisplayUtils {
    public static void spawnVaultTextDisplays(Location vaultLocation, Material keyItemMaterial) {
        Location titleLocation = vaultLocation.clone().add(0.5, 0.75, 0.5);
        vaultLocation.getWorld().spawn(titleLocation, TextDisplay.class, textDisplay -> {
            textDisplay.text(Component.text("Elytra").color(NamedTextColor.LIGHT_PURPLE));
            textDisplay.setBillboard(Display.Billboard.CENTER);
            textDisplay.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0)); // Transparent background
            textDisplay.setSeeThrough(false);
            textDisplay.addScoreboardTag("elytra_vault_text");
        });

        Location subtitleLocation = vaultLocation.clone().add(0.5, 0.5, 0.5);
        vaultLocation.getWorld().spawn(subtitleLocation, TextDisplay.class, textDisplay -> {
            String keyItemName = formatMaterialName(keyItemMaterial);
            Component keyText = Component.text("Open With ")
                    .append(Component.text("[" + keyItemName + "]").color(NamedTextColor.AQUA));

            textDisplay.text(keyText);
            textDisplay.setBillboard(Display.Billboard.CENTER);
            textDisplay.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0)); // Transparent background
            textDisplay.setSeeThrough(false);
            textDisplay.addScoreboardTag("elytra_vault_text");
        });
    }

    private static String formatMaterialName(Material material) {
        String[] words = material.name().toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                formatted.append(" ");
            }
            formatted.append(words[i].substring(0, 1).toUpperCase())
                    .append(words[i].substring(1));
        }

        return formatted.toString();
    }
}

