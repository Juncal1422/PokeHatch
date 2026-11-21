# PokeHatch

## Edicion rapida

| Dato | Archivo |
|---|---|
| Especies, evoluciones y reglas de eclosion | `data/species.json` |
| Nombres de grupos de huevo | `data/groups.json` |
| Nombres generales de objetos | `data/items.json` |
| Precios, costos, medallas, colores e identificadores de grupos | `data/group-config.json` |
| Beneficios temporales y beneficios por grupo | `data/benefits.json` |
| Objetos de tienda, objetos de eclosion, inciensos, bufos y recompensas | `data/item-config.json` |
| Arte y rutas de imagen | `data/art.json` |
| Orden de carga de JavaScript | `data/scripts.json` |

Los JSON se cargan desde `src/app/inicio.js`. En la ejecucion normal se leen con `fetch`; al abrir el HTML desde disco se selecciona manualmente la carpeta `data`.

## Datos y conexiones

- `data/group-config.json` alimenta `src/config/grupos.js`: identifica grupos, costes, pasos, plazas, regiones, medallas y colores.
- `data/benefits.json` alimenta `src/config/beneficios.js`; `src/core/recompensas.js` usa el objeto `benefits` y `groupBenefits`.
- `data/item-config.json` alimenta `src/config/objetos.js`; ese archivo crea `names`, `goods`, `gates`, `clickItems`, `rateItems`, `boostItems`, `rewards`, `loot` y las relaciones de objetos.
- `data/species.json` crea `species`, `S` y todas las relaciones de familias, padres y grupos.
- `data/groups.json` proporciona `G`, las etiquetas visibles de los grupos.
- `data/items.json` aporta nombres base y `data/item-config.json` los completa o sustituye nombres de objeto.
- `data/art.json` proporciona rutas de sprites a las UI y a `src/assets/sprites.js`.

### Esquema de `data/species.json`

Cada registro usa nombres descriptivos en lugar de abreviaturas:

| Campo | Significado |
|---|---|
| `id` | Número de Pokédex y clave del sprite. |
| `name` | Nombre de la especie. |
| `evolvesFromId` | ID de la especie de la que evoluciona, o `null` si es una especie base. |
| `generation` | Generación a la que pertenece. |
| `eggGroups` | Grupos de huevo propios de la especie. |
| `isBaby` | Indica si es un Pokémon bebé. |
| `hatchCycles` | Ciclos de eclosión base. |
| `familyRootId` | ID de la especie inicial de su familia evolutiva. |
| `hatchableEggGroups` | Grupos en los que puede eclosionar; puede estar vacío para evoluciones. |
| `evolutionMethod` | Método original de evolución: `null` o `{ "type": "level", "level": n }`, `{ "type": "item", "itemId": "..." }` o `{ "type": "special", "description": "..." }`. |
| `evolutionRequirement` | Requisito de juego: `null` o `{ "baseHatches": n, "requiredItem": "...", "requiresHatchesAfterParentRegistered": boolean }`. `requiredItem` puede ser `null`. |

El campo `evolutionMethod` conserva cómo evoluciona la especie en los juegos de Pokémon; `evolutionRequirement` describe las reglas propias de PokeHatch. El cargador y las relaciones de especies se inicializan en `src/config/grupos.js` y `src/core/especies.js`; los requisitos se aplican desde `src/core/progreso.js` y `src/core/evolucion.js`.

## Ubicacion de metodos

| Metodo o utilidad | Archivo | Responsabilidad |
|---|---|---|
| `calculateEggTarget`, `hatchEgg`, `careForEgg`, `advanceIncubator`, `calculateShinyChance`, `changeIncubatorLevel` | `src/core/incubacion.js` | Eclosiones, progreso, probabilidad shiny y mejora de incubadoras |
| `spendCoins`, `calculateTapPower`, `calculateIncubatorRateBoost`, `required`, `payout` | `src/core/progreso.js` | Economía compartida, potencia de toque, ritmo y costes de evolucion |
| `addDex`, `progress`, `ready`, `inheritShiny`, `applyEvolution`, `evolve`, `evolveAll`, `condition`, `birthToast` | `src/core/evolucion.js` | Evoluciones y registro de especies |
| `awardMilestones`, `benefitGate`, `buyBenefit`, `rollReal`, `purchaseCount`, `shopPrice`, `refreshTargets` | `src/core/recompensas.js` | Progresion, beneficios, regalos y recompensas |
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

## Variables y constantes principales

| Archivo | Variables principales | Papel |
|---|---|---|
| `src/config/grupos.js` | `species`, `S`, `G`, `A`, `groupIDs`, `stepCost`, `groupCost`, `prices`, `regions`, `badges`, `pools`, `tint` | Estado derivado de especies y grupos |
| `src/config/objetos.js` | `names`, `goods`, `gates`, `clickItems`, `rateItems`, `boostItems`, `choiceItems`, `potions`, `rewards`, `loot`, `scarves` | Configuracion ejecutable de objetos |
| `src/config/beneficios.js` | `benefits`, `groupBenefits`, `scopeLists`, `regionLists` | Beneficios temporales y desbloqueos |
| `src/core/incubacion.js` | `lvlCost`, `rates` | Niveles de incubadora y aceleracion |
| `src/core/especies.js` | `evoUses`, `byParent`, `dropByRoot`, `growingPools`, `families` | Relaciones entre objetos, familias y especie |
| `src/storage/partida.js` | `st`, `view`, `slot`, `shopTab`, `KEY`, `saveFields`, `eggFields` | Estado global y campos persistidos |
| `src/ui/actualizar.js` | `bagSignature`, `livePending`, `frameQueued`, `pressedPointers` | Optimizacion de renders y ritmo de actualizacion |
| `src/assets/sprites.js` | `spriteCache`, `remoteItems`, `spriteURLs` | Cache de sprites remotos |

## Grafo principal de carga

`PokeHatch.html`
→ `src/app/inicio.js`
→ JSON de `data/`
→ scripts declarados en `data/scripts.json`
→ estado creado por `src/storage/partida.js`
→ renderizado iniciado por `src/app/reloj.js` y `src/app/controles.js`

El orden de carga esta controlado por `data/scripts.json`. Los modulos config deben cargarse antes que los modulos core y UI que consumen sus variables globales.
