package com.franchee.jefecabra.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;

/**
 * Fabrica de items custom del Jefe Cabra:
 * - Cuerno de Cabra Maldito: item de invocacion, se consume al usar el altar.
 * - Yelmo del Testarazo: drop unico del jefe.
 *
 * Ambos items se marcan con una PersistentDataContainer para poder
 * identificarlos de forma confiable (evita que un item "parecido"
 * craftedo a mano funcione igual).
 */
public class ItemFactory {

    public static final String CUERNO_KEY = "jefe_cabra_cuerno";
    public static final String YELMO_KEY = "yelmo_testarazo";

    private final Plugin plugin;
    private final NamespacedKey cuernoKey;
    private final NamespacedKey yelmoKey;

    public ItemFactory(Plugin plugin) {
        this.plugin = plugin;
        this.cuernoKey = new NamespacedKey(plugin, CUERNO_KEY);
        this.yelmoKey = new NamespacedKey(plugin, YELMO_KEY);
    }

    /** Crea el item de invocacion (se usa haciendo click derecho en el altar). */
    public ItemStack crearCuernoInvocador() {
        ItemStack item = new ItemStack(Material.GOAT_HORN);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Cuerno de Cabra Maldito", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Un cuerno retorcido que huele a azufre.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Usalo en el altar para invocar al Jefe Cabra.", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(cuernoKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean esCuernoInvocador(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(cuernoKey, PersistentDataType.BYTE);
    }

    /** Crea el drop unico del jefe. */
    public ItemStack crearYelmoTestarazo() {
        ItemStack item = new ItemStack(Material.NETHERITE_HELMET);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Yelmo del Testarazo", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Forjado con los cuernos del Jefe Cabra.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("25% de probabilidad de aplicar knockback extra", NamedTextColor.BLUE)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("al golpear cuerpo a cuerpo.", NamedTextColor.BLUE)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(yelmoKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean esYelmoTestarazo(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(yelmoKey, PersistentDataType.BYTE);
    }
}
