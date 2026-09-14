package com.franchee.jefecabra;

import com.franchee.jefecabra.listeners.AltarListener;
import com.franchee.jefecabra.listeners.BossCombatListener;
import com.franchee.jefecabra.util.ItemFactory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class JefeCabraPlugin extends JavaPlugin {

    private ItemFactory itemFactory;

    @Override
    public void onEnable() {
        this.itemFactory = new ItemFactory(this);

        getServer().getPluginManager().registerEvents(new AltarListener(this, itemFactory), this);
        getServer().getPluginManager().registerEvents(new BossCombatListener(itemFactory), this);

        getLogger().info("JefeCabraPlugin habilitado.");
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
