package com.franchee.jefecabra.boss;

import com.franchee.jefecabra.JefeCabraPlugin;
import com.franchee.jefecabra.util.ItemFactory;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.GameMode;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Representa una instancia viva del Jefe Cabra.
 * Maneja: spawn con tamano aumentado, vida/danio de jefe, bossbar,
 * y las 3 fases de combate (embestida -> invoca manada -> enrage).
 */
public class JefeCabraBoss {

    public static final String METADATA_KEY = "esJefeCabra";

    // --- Balance mid-game. Ajustar libremente segun testeo ---
    private static final double VIDA_MAXIMA = 200.0;
    private static final double DANIO_BASE = 12.0;
    private static final double VELOCIDAD_BASE = 0.22; // antes 0.3
    private static final double RANGO_PERSECUCION = 20.0;
    private static final double RANGO_ATAQUE = 2.5;
    private static final long COOLDOWN_ATAQUE_MS = 1200L;
    private static final double RANGO_DISPARO_MAX = 18.0;
    private static final long COOLDOWN_DISPARO_MS = 3000L;
    private static final float YIELD_BOLA_FUEGO = 0f; // sin dano a bloques ni fuego
    // NOTA: el escalado de tamano (Attribute.GENERIC_SCALE) recien existe
    // desde Paper/Bukkit 1.20.5. En 1.20.1 no hay forma de agrandar la
    // entidad vía API vanilla; para eso hace falta un modelo custom via
    // resource pack (ver README).

    private final JefeCabraPlugin plugin;
    private final ItemFactory itemFactory;
    private final Goat entidad;
    private final BossBar bossBar;

    private boolean fase2Activada = false;
    private boolean fase3Activada = false;
    private long proximoAtaquePermitido = 0L;
    private long proximoDisparoPermitido = 0L;
    private BukkitRunnable tickTask;

    // Registro de jefes activos para poder consultarlos desde otros listeners
    private static final Map<UUID, JefeCabraBoss> ACTIVOS = new HashMap<>();

    public JefeCabraBoss(JefeCabraPlugin plugin, ItemFactory itemFactory, Location location) {
        this.plugin = plugin;
        this.itemFactory = itemFactory;
        this.entidad = location.getWorld().spawn(location, Goat.class);
        this.bossBar = BossBar.bossBar(
                Component.text("Jefe Cabra", NamedTextColor.DARK_RED),
                1.0f,
                BossBar.Color.RED,
                BossBar.Overlay.NOTCHED_10
        );

        configurarEntidad();
        ACTIVOS.put(entidad.getUniqueId(), this);
        iniciarTick();
    }

    private void configurarEntidad() {
        entidad.customName(Component.text("Jefe Cabra", NamedTextColor.DARK_RED));
        entidad.setCustomNameVisible(true);
        entidad.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, true));
        entidad.setRemoveWhenFarAway(false);
        entidad.setPersistent(true);

        AttributeInstance vida = entidad.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (vida != null) {
            vida.setBaseValue(VIDA_MAXIMA);
        }
        entidad.setHealth(VIDA_MAXIMA);

        AttributeInstance danio = entidad.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (danio != null) danio.setBaseValue(DANIO_BASE);

        AttributeInstance velocidad = entidad.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (velocidad != null) velocidad.setBaseValue(VELOCIDAD_BASE);
    }

    private void iniciarTick() {
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!entidad.isValid() || entidad.isDead()) {
                    finalizar();
                    return;
                }
                actualizarBossBar();
                revisarFases();
                manejarCombate();
            }
        };
        tickTask.runTaskTimer(plugin, 0L, 10L); // cada 0.5s
    }

    private void actualizarBossBar() {
        double vidaActual = entidad.getHealth();
        double vidaMax = entidad.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        float progreso = (float) Math.max(0.0, Math.min(1.0, vidaActual / vidaMax));
        bossBar.progress(progreso);

        for (Player jugador : entidad.getWorld().getPlayers()) {
            if (jugador.getLocation().distance(entidad.getLocation()) <= 40) {
                bossBar.addViewer(jugador);
            } else {
                bossBar.removeViewer(jugador);
            }
        }
    }

    /**
     * Los Goat vanilla no tienen IA de ataque cuerpo a cuerpo (solo
     * embisten esporadicamente por su cuenta), asi que el "ataque" del
     * jefe lo manejamos a mano: persigue al jugador valido mas cercano
     * dentro de RANGO_PERSECUCION, y si esta a RANGO_ATAQUE le pega con
     * un cooldown fijo.
     */
    private void manejarCombate() {
        Player objetivo = entidad.getWorld().getPlayers().stream()
                .filter(p -> p.getGameMode() != GameMode.SPECTATOR && p.getGameMode() != GameMode.CREATIVE)
                .filter(p -> p.getLocation().distanceSquared(entidad.getLocation()) <= RANGO_PERSECUCION * RANGO_PERSECUCION)
                .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(entidad.getLocation())))
                .orElse(null);

        if (objetivo == null) return;

        entidad.setTarget(objetivo);

        double distancia = objetivo.getLocation().distance(entidad.getLocation());
        long ahora = System.currentTimeMillis();

        if (distancia <= RANGO_ATAQUE && ahora >= proximoAtaquePermitido) {
            AttributeInstance danio = entidad.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            double valorDanio = danio != null ? danio.getValue() : DANIO_BASE;
            objetivo.damage(valorDanio, entidad);
            proximoAtaquePermitido = ahora + COOLDOWN_ATAQUE_MS;
        } else if (distancia > RANGO_ATAQUE && distancia <= RANGO_DISPARO_MAX && ahora >= proximoDisparoPermitido) {
            dispararBolaFuego(objetivo);
            proximoDisparoPermitido = ahora + COOLDOWN_DISPARO_MS;
        }
    }

    /** Dispara una bola de fuego chica hacia el jugador. Dano bajo, fijo (ver BossCombatListener). */
    private void dispararBolaFuego(Player objetivo) {
        Vector direccion = objetivo.getEyeLocation().toVector()
                .subtract(entidad.getEyeLocation().toVector())
                .normalize();

        SmallFireball bola = entidad.launchProjectile(SmallFireball.class, direccion);
        bola.setYield(YIELD_BOLA_FUEGO);
        bola.setIsIncendiary(false);
    }

    private void revisarFases() {
        double vidaActual = entidad.getHealth();
        double vidaMax = entidad.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double porcentaje = vidaActual / vidaMax;

        if (!fase2Activada && porcentaje <= 0.66) {
            fase2Activada = true;
            activarFase2();
        }
        if (!fase3Activada && porcentaje <= 0.33) {
            fase3Activada = true;
            activarFase3();
        }
    }

    /** Fase 2: invoca cabras menores "poseidas". */
    private void activarFase2() {
        bossBar.name(Component.text("Jefe Cabra (¡invoca a su manada!)", NamedTextColor.RED));
        Location base = entidad.getLocation();
        for (int i = 0; i < 3; i++) {
            Location spawnLoc = base.clone().add(
                    (Math.random() - 0.5) * 4,
                    0,
                    (Math.random() - 0.5) * 4
            );
            Goat menor = base.getWorld().spawn(spawnLoc, Goat.class);
            menor.customName(Component.text("Cabra Poseida", NamedTextColor.RED));
            menor.setCustomNameVisible(true);
            AttributeInstance danioMenor = menor.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            if (danioMenor != null) danioMenor.setBaseValue(6.0);
            base.getWorld().spawnParticle(Particle.SOUL, spawnLoc, 20, 0.5, 0.5, 0.5);
        }
    }

    /** Fase 3: enrage, +30% velocidad y danio. */
    private void activarFase3() {
        bossBar.name(Component.text("Jefe Cabra (¡Enfurecido!)", NamedTextColor.DARK_RED));

        AttributeInstance velocidad = entidad.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (velocidad != null) velocidad.setBaseValue(velocidad.getBaseValue() * 1.3);

        AttributeInstance danio = entidad.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (danio != null) danio.setBaseValue(danio.getBaseValue() * 1.3);

        entidad.getWorld().spawnParticle(Particle.LAVA, entidad.getLocation(), 40, 1, 0.5, 1);
    }

    /** Se llama desde el listener de EntityDeathEvent. */
    public void alMorir() {
        entidad.getWorld().dropItemNaturally(entidad.getLocation(), itemFactory.crearYelmoTestarazo());
        finalizar();
    }

    private void finalizar() {
        for (Player p : entidad.getWorld().getPlayers()) {
            bossBar.removeViewer(p);
        }
        if (tickTask != null) tickTask.cancel();
        ACTIVOS.remove(entidad.getUniqueId());
    }

    public static boolean esJefeCabra(LivingEntity entidad) {
        return entidad.hasMetadata(METADATA_KEY);
    }

    public static JefeCabraBoss get(UUID uuid) {
        return ACTIVOS.get(uuid);
    }

    public Goat getEntidad() {
        return entidad;
    }
}
