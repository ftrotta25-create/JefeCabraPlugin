package com.franchee.jefecabra.listeners;

import com.franchee.jefecabra.JefeCabraPlugin;
import com.franchee.jefecabra.boss.JefeCabraBoss;
import com.franchee.jefecabra.util.ItemFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Detecta cuando un jugador hace click derecho en un altar valido
 * usando el Cuerno de Cabra Maldito, y ahi invoca al Jefe Cabra.
 *
 * Estructura del altar (simple y facil de ajustar):
 *  - Bloque central: LODESTONE
 *  - Los 4 bloques cardinales alrededor: POLISHED_BLACKSTONE
 * Cambiar estas constantes si preferis otra estructura.
 */
public class AltarListener implements Listener {

    private static final Material BLOQUE_CENTRAL = Material.LODESTONE;
    private static final Material BLOQUE_MARCO = Material.POLISHED_BLACKSTONE;

    private final JefeCabraPlugin plugin;
    private final ItemFactory itemFactory;

    public AltarListener(JefeCabraPlugin plugin, ItemFactory itemFactory) {
        this.plugin = plugin;
        this.itemFactory = itemFactory;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return; // evita doble disparo (mano principal + offhand)

        Block clicked = e.getClickedBlock();
        if (clicked == null || clicked.getType() != BLOQUE_CENTRAL) return;

        ItemStack enMano = e.getPlayer().getInventory().getItemInMainHand();
        if (!itemFactory.esCuernoInvocador(enMano)) return;

        if (!esAltarValido(clicked)) {
            e.getPlayer().sendMessage(Component.text(
                    "El altar no esta completo.", NamedTextColor.GRAY));
            return;
        }

        // Consumir el cuerno
        enMano.setAmount(enMano.getAmount() - 1);

        Location spawnLoc = clicked.getLocation().add(0.5, 1, 0.5);
        new JefeCabraBoss(plugin, itemFactory, spawnLoc);

        clicked.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, spawnLoc, 60, 1, 1, 1);
        clicked.getWorld().strikeLightningEffect(spawnLoc); // solo efecto visual/sonoro, no da danio

        for (var jugador : clicked.getWorld().getPlayers()) {
            if (jugador.getLocation().distance(spawnLoc) <= 40) {
                jugador.sendMessage(Component.text(
                        "¡El Jefe Cabra ha despertado!", NamedTextColor.DARK_RED));
            }
        }
    }

    private boolean esAltarValido(Block centro) {
        Block norte = centro.getRelative(0, 0, -1);
        Block sur = centro.getRelative(0, 0, 1);
        Block este = centro.getRelative(1, 0, 0);
        Block oeste = centro.getRelative(-1, 0, 0);

        return norte.getType() == BLOQUE_MARCO
                && sur.getType() == BLOQUE_MARCO
                && este.getType() == BLOQUE_MARCO
                && oeste.getType() == BLOQUE_MARCO;
    }
}
