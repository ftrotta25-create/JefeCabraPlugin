package com.franchee.jefecabra.listeners;

import com.franchee.jefecabra.boss.JefeCabraBoss;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Maneja el efecto de aturdimiento al conectar una embestida
 * y entrega el drop unico cuando el jefe muere.
 */
public class BossCombatListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Goat cabra)) return;
        if (!(e.getEntity() instanceof Player jugador)) return;
        if (!JefeCabraBoss.esJefeCabra(cabra)) return;

        // La embestida nativa de la cabra dispara este mismo evento;
        // aprovechamos para aplicar Lentitud como "aturdimiento".
        jugador.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
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
