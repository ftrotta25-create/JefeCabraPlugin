package com.franchee.jefecabra;

import com.franchee.jefecabra.listeners.AltarListener;
import com.franchee.jefecabra.listeners.BossCombatListener;
import com.franchee.jefecabra.util.ItemFactory;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class JefeCabraPlugin extends JavaPlugin {

    private ItemFactory itemFactory;

    @Override
    public void onEnable() {
        this.itemFactory = new ItemFactory(this);

        getServer().getPluginManager().registerEvents(new AltarListener(this, itemFactory), this);
        getServer().getPluginManager().registerEvents(new BossCombatListener(itemFactory), this);

        registrarRecetaCuerno();

        getLogger().info("JefeCabraPlugin habilitado.");
    }

    /**
     * Receta del Cuerno de Cabra Maldito:
     *   . R .
     *   B H B
     *   . R .
     * H = cualquier Cuerno de Cabra (Material.GOAT_HORN matchea los 8
     *     sonidos, no filtra por el NBT "instrument").
     * B = Polvo de Blaze, R = Redstone.
     */
    private void registrarRecetaCuerno() {
        ShapedRecipe receta = new ShapedRecipe(
                new NamespacedKey(this, "cuerno_invocador"),
                itemFactory.crearCuernoInvocador()
        );
        receta.shape(" R ", "BHB", " R ");
        receta.setIngredient('R', Material.REDSTONE);
        receta.setIngredient('B', Material.BLAZE_POWDER);
        receta.setIngredient('H', Material.GOAT_HORN);

        getServer().addRecipe(receta);
    }

    @Override
    public void onDisable() {
        getLogger().info("JefeCabraPlugin deshabilitado.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("darcuerno")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Este comando solo lo puede usar un jugador.");
                return true;
            }
            player.getInventory().addItem(itemFactory.crearCuernoInvocador());
            player.sendMessage("Recibiste el Cuerno de Cabra Maldito.");
            return true;
        }
        return false;
    }
}
