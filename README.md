# PokéHatch

Juego de crianza Pokémon para escritorio, desarrollado en Java con libGDX y Scene2D. Los sprites, iconos y gritos están incluidos en el proyecto. El juego funciona sin conexión una vez instaladas las dependencias de compilación.

## Ejecutar

Abre la carpeta que contiene `settings.gradle` como proyecto Gradle. Desde esa carpeta, en Windows, ejecuta `.\gradlew.bat lwjgl3:run`. En Linux o macOS puedes usar `./gradlew lwjgl3:run`.

## Partida

El juego recupera automáticamente el perfil local al arrancar. Guarda cada cuatro segundos, al comprar o evolucionar, al pausar y al salir. En Windows la partida está en `%LOCALAPPDATA%\PokeHatch\profile.sav`. En macOS se encuentra en `~/Library/Application Support/PokeHatch/profile.sav` y en Linux en `~/.local/share/PokeHatch/profile.sav`. El volumen de los gritos se conserva en las preferencias de libGDX.

## Recursos y edición

- Las reglas de crianza y progresión están en `GameEngine.java`. Los catálogos de especies, grupos y objetos están en `core/src/main/resources/pokehatch/`.
- La paleta, marcos, botones y estilos Scene2D se encuentran en `assets/interface/`. Los atlas de texto proceden de las fuentes originales en `assets/fonts/`.
- Para ajustar la ráfaga de eclosión, abre `assets/effects/hatch.p` en el editor 2D Particle Editor de libGDX. Su imagen es `spark.png`, en la misma carpeta.
- `assets/effects/hatch.vert` y `hatch.frag` controlan el destello aplicado al sprite de la cría. `HatchSprite.java` controla el tiempo de aparición.
- El icono de la ventana y los iconos de empaquetado de escritorio proceden de `assets/art/egg.png`.
