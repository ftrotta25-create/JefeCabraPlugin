# JefeCabraPlugin

Plugin standalone para Paper **1.20.1**. Implementa al **Jefe Cabra**:
un jefe mid-game, invocable con un altar, con 3 fases y un drop unico.

> **Sobre el tamaño de la cabra:** en 1.20.1 no existe `Attribute.GENERIC_SCALE`
> (Mojang lo agregó recién en 1.20.5), así que por API vanilla no hay forma
> de agrandar la entidad. El jefe queda con el tamaño normal de una cabra
> por ahora. La única forma real de lograr el tamaño mayor en esta versión
> es con un modelo 3D custom vía resource pack (encaja con tu FranWorld) —
> avisame si querés que lo armemos así.

## Como conseguir el .jar sin instalar nada (GitHub Actions)

Este proyecto ya incluye `.github/workflows/build.yml`, que compila el
plugin automaticamente en los servidores de GitHub.

1. Creá un repositorio nuevo en GitHub (puede ser privado).
2. Subí el contenido de esta carpeta (arrastrando los archivos desde
   la web de GitHub alcanza, no hace falta git en la terminal:
   "Add file" -> "Upload files").
3. Andá a la pestaña **Actions** del repo. Deberia arrancar solo un
   run llamado "Compilar JefeCabraPlugin" (si no arranca, entrá al
   workflow y tocá "Run workflow").
4. Cuando termine (tarda ~1 minuto), entrá al run finalizado y
   descargá el artifact **JefeCabraPlugin-jar** — ahi está tu .jar
   listo para poner en `plugins/`.

## Como probarlo (compilando local, si en algun momento tenés Maven)


1. Compilar con Maven:
   ```
   mvn clean package
   ```
   El .jar queda en `target/JefeCabraPlugin.jar`.

2. Copiarlo a la carpeta `plugins/` del server y reiniciar.

3. En el juego:
   - Armar el altar: un bloque de **Lodestone** en el centro, rodeado
     en los 4 costados (norte/sur/este/oeste) por **Polished Blackstone**.
   - Conseguir el item de invocacion con `/darcuerno` (requiere el
     permiso `jefecabra.admin`, dárselo con tu plugin de permisos
     o LuckPerms).
   - Click derecho sobre el Lodestone del altar sosteniendo el cuerno.
   - El Jefe Cabra aparece con tamaño aumentado (escala 1.8x), 200 HP,
     y bossbar visible para jugadores a menos de 40 bloques.

## Fases

- **100%-66% HP:** embestidas con aturdimiento (Lentitud) al jugador golpeado.
- **66%-33% HP:** invoca 3 "Cabras Poseidas" menores.
- **33%-0% HP:** enrage (+30% velocidad y daño, partículas de lava).

## Drop

Al morir dropea el **Yelmo del Testarazo** (netherite helmet reskineado
por lore, unbreakable, marcado con PersistentDataContainer).

## Cosas para ajustar a gusto

- `JefeCabraBoss.VIDA_MAXIMA`, `DANIO_BASE`, `VELOCIDAD_BASE`, `TAMANO`
  en `JefeCabraBoss.java` — todo el balance está ahí arriba en constantes.
- La estructura del altar (`BLOQUE_CENTRAL` / `BLOQUE_MARCO`) en
  `AltarListener.java`.
- El knockback extra del yelmo al golpear NO está implementado todavía
  como efecto activo (solo está en el lore) — si querés que funcione
  de verdad, se agrega con un listener de `EntityDamageByEntityEvent`
  que chequee si el atacante tiene el yelmo puesto vía
  `ItemFactory.esYelmoTestarazo(...)`. Te lo agrego si querés.

## Integrar con MatiasPlugin

Este proyecto está armado standalone (paquete `com.franchee.jefecabra`)
para que puedas probarlo suelto. Para fusionarlo con MatiasPlugin,
lo más simple es mover las 4 clases Java a un subpaquete de tu proyecto
existente y registrar los 2 listeners en tu `onEnable()` actual, en vez
de tener un plugin.yml aparte.
