package com.franchee.jefecabra.listeners;

import com.franchee.jefecabra.boss.JefeCabraBoss;
import com.franchee.jefecabra.util.ItemFactory;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Maneja:
 * - Aturdimiento al conectar una embestida.
 * - Dano fijo (bajo) al impactar con una bola de fuego del jefe.
 * - Knockback extra al golpear usando el Yelmo del Testarazo.
 * - Entrega del drop unico cuando el jefe muere.
 */
public class BossCombatListener implements Listener {

    /** Dano fijo de la bola de fuego del jefe (no escala con nada, siempre bajo). */
    public static final double DANIO_BOLA_FUEGO = 4.0;

    /** Probabilidad de knockback extra del Yelmo del Testarazo al pegar. */
    private static final double PROB_KNOCKBACK_YELMO = 0.25;

    private final ItemFactory itemFactory;

    public BossCombatListener(ItemFactory itemFactory) {
        this.itemFactory = itemFactory;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        // Embestida nativa de la cabra -> aturdimiento (Lentitud)
        if (e.getDamager() instanceof Goat cabra
                && e.getEntity() instanceof Player jugador
                && JefeCabraBoss.esJefeCabra(cabra)) {
            jugador.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
            return;
        }

        // Bola de fuego del jefe -> dano fijo, bajo, sin importar el yield de la explosion
        if (e.getDamager() instanceof Fireball bola
                && bola.getShooter() instanceof Goat cabra
                && JefeCabraBoss.esJefeCabra(cabra)) {
            e.setDamage(DANIO_BOLA_FUEGO);
            return;
        }

        // Yelmo del Testarazo -> chance de knockback extra al golpear
        if (e.getDamager() instanceof Player atacante
                && e.getEntity() instanceof LivingEntity victima
                && itemFactory.esYelmoTestarazo(atacante.getInventory().getHelmet())
                && Math.random() < PROB_KNOCKBACK_YELMO) {

            Vector direccion = victima.getLocation().toVector()
                    .subtract(atacante.getLocation().toVector())
                    .normalize()
                    .multiply(0.6);
            direccion.setY(0.45);
            victima.setVelocity(victima.getVelocity().add(direccion));
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Goat cabra)) return;
        if (!JefeCabraBoss.esJefeCabra(cabra)) return;

        JefeCabraBoss jefe = JefeCabraBoss.get(cabra.getUniqueId());
        if (jefe != null) {
            e.getDrops().clear(); // evita el drop vanilla de leche/cuerno normal
            jefe.alMorir();
        }
    }
}
