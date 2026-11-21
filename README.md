# PokeHatch

## Edicion rapida

| Dato | Archivo |
|---|---|
| Especies, evoluciones y reglas de eclosion | `data/species.json` |
| Nombres, precios, costos, medallas, colores e identificadores de grupos | `data/group-config.json` |
| Nombres y configuración de objetos | `data/item-config.json` |
| Beneficios temporales y beneficios por grupo | `data/benefits.json` |
| Objetos de tienda, objetos de eclosion, inciensos, bufos y recompensas | `data/item-config.json` |
| Arte y rutas de imagen | `data/art.json` |
| Orden de carga de JavaScript | `data/scripts.json` |

Los JSON se cargan desde `src/app/inicio.js`. En la ejecucion normal se leen con `fetch`; al abrir el HTML desde disco se selecciona manualmente la carpeta `data`. Los scripts se descargan en paralelo y se ejecutan en el orden definido por `data/scripts.json`.

El criadero muestra `assets/objetos-y-criadero/meadow.png` como fondo adaptable al tamaño de su frame. Para mostrar las herramientas de depuracion en Configuracion, escribe `debug=true` en la consola web; `debug=false` las oculta. Incluyen completar una generacion de la Pokedex, sumar PC y RC y activar una probabilidad shiny del 50 %.

## Datos y conexiones

- `data/group-config.json` alimenta `src/config/grupos.js`: proporciona nombres de grupos, costes, pasos, plazas, regiones, medallas y colores.
- `data/benefits.json` alimenta `src/config/beneficios.js`; `src/core/recompensas.js` usa el objeto `benefits` y `groupBenefits`.
- `data/item-config.json` alimenta `src/config/objetos.js`; ese archivo crea `names`, `goods`, `gates`, `clickItems`, `rateItems`, `boostItems`, `rewards`, `loot` y las relaciones de objetos.
- `data/species.json` crea `species`, `S` y todas las relaciones de familias, padres y grupos.
- `data/art.json` contiene rutas para objetos y medallas. Los sprites Pokémon se generan desde `data/species.json`: `id` produce `assets/pokemon/{id}.png` y `sh{id}` produce `assets/shiny/sh{id}.png`.

Los IDs de grupo de huevo son: 1 Bicho, 2 Hada, 3 Volador, 4 Marino, 5 Planta, 6 Campo, 7 Costa, 8 Humanoide, 9 Amorfo, 10 Abisal, 11 Mineral, 12 Monstruo y 13 Dragón. Las partidas v7 se convierten automáticamente al esquema v8 al abrirlas.

### Esquema de `data/species.json`

Cada registro usa nombres descriptivos en lugar de abreviaturas:

| Campo | Significado |
|---|---|
| `id` | Número de Pokédex y clave del sprite. |
| `name` | Nombre de la especie. |
| `evolvesFromId` | ID de la especie de la que evoluciona, o `null` si es una especie base. |
| `generation` | Generación a la que pertenece. |
| `eggGroups` | Grupos propios de la especie; Ditto usa el valor especial `"Ditto"` y `15` representa especies no eclosionables. |
| `isBaby` | Indica si es un Pokémon bebé. |
| `hatchCycles` | Ciclos de eclosión base. |
| `familyRootId` | ID de la especie inicial de su familia evolutiva. |
| `hatchableEggGroups` | Grupos en los que puede eclosionar; puede estar vacío para evoluciones. |
| `evolutionMethod` | Método original de evolución: `null` o `{ "type": "level", "level": n }`, `{ "type": "item", "itemId": "..." }` o `{ "type": "special", "description": "..." }`. |
| `evolutionRequirement` | Requisito de juego: `null` o `{ "baseHatches": n, "requiredItem": "...", "requiresHatchesAfterParentRegistered": boolean }`. `requiredItem` puede ser `null`. |

## Ubicacion de metodos

| Metodo o utilidad | Archivo | Responsabilidad |
|---|---|---|
| `calculateEggTarget`, `hatchEgg`, `careForEgg`, `advanceIncubator`, `calculateShinyChance`, `changeIncubatorLevel` | `src/core/incubacion.js` | Eclosiones, progreso, probabilidad shiny y mejora de incubadoras |
| `spendCoins`, `calculateTapPower`, `calculateIncubatorRateBoost`, `required`, `payout` | `src/core/progreso.js` | Economía compartida, potencia de toque, ritmo y costes de evolucion |
| `addDex`, `progress`, `ready`, `inheritShiny`, `applyEvolution`, `evolve`, `evolveAll`, `condition`, `birthToast` | `src/core/evolucion.js` | Evoluciones y registro de especies |
| `awardShinyItems`, `awardMilestones`, `benefitGate`, `buyBenefit`, `rollReal`, `purchaseCount`, `shopPrice`, `refreshTargets` | `src/core/recompensas.js` | Progresion, beneficios, regalos y recompensas |
| `chooseHatchingSpecies`, `availableEggPool`, `describeEvolutionItem` | `src/core/especies.js` | Sorteo de especies y relaciones evolutivas |
| `calculateEggTarget`, `hatchEgg`, `careForEgg`, `advanceIncubator`, `calculateShinyChance`, `changeIncubatorLevel` | `src/core/incubacion.js` | Eclosiones, progreso, shinies e incubadoras |
| `isShopItemAcquired`, `describeShopGate`, `buyShopItem` | `src/core/tienda.js` | Disponibilidad y compra de objetos |
| `egg`, `fresh`, `decode`, `saveFields` y validadores | `src/storage/partida.js` | Creacion, migracion y validacion de partidas |
| `save`, `notify`, `earn`, `fadingToast` | `src/ui/avisos.js` | Persistencia y avisos visuales |
| `renderCurrentView`, `refreshLiveViews`, `queueLiveUpdate`, `changeView` | `src/ui/actualizar.js` | Renderizado y actualizacion periódica |
| `farm`, `farmStatus`, `groupProgress`, `periscope`, `reveal` | `src/ui/criadero.js` | Pantalla de criadero |
| `storeView`, `updateShop`, `sectionOf`, `categoryOrder` | `src/ui/tienda.js` | Catalogo y estado de compra |
| `inventory`, `unlockSource` | `src/ui/mochila.js` | Mochila y procedencia de objetos |
| `dex`, `entries`, `info`, `refreshDetail`, `settings` | `src/ui/pokedex.js` | Pokedex y configuracion |
| `loadItemSprites`, `preloadArtSprites`, `preloadAllSprites`, `itemArt` | `src/assets/sprites.js` | Precarga los sprites normales al iniciar y los shiny en segundo plano |
| Controlador de eventos y acciones | `src/app/controles.js` | Navegacion, tienda, eclosion, evolucion e importacion |

## Grafo principal de carga

`PokeHatch.html`
→ `src/app/inicio.js`
→ JSON de `data/`
→ scripts declarados en `data/scripts.json`
→ estado creado por `src/storage/partida.js`
→ renderizado iniciado por `src/app/reloj.js` y `src/app/controles.js`