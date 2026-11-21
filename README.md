# PokeHatch
Abre **PokeHatch v1.0.html** para jugar. Es la versión empaquetada que descarga el lanzador.

## Actualizar el juego
1. Descomprime el ZIP en la raíz de la rama OnlyhtmlVersion.
2. Edita los archivos fuente según la tabla.
3. Desde esta carpeta ejecuta `py generar.py` en Windows o `python3 generar.py` en macOS/Linux. Solo necesitas Python 3.
4. Abre el HTML generado para revisar tu cambio. Sube a GitHub las fuentes modificadas y **PokeHatch v1.0.html** en la raíz de OnlyhtmlVersion. El lanzador existente usa ese nombre exacto.

| Qué quieres cambiar | Dónde editar |
|---|---|
| Especies, familias y condiciones evolutivas | data/species.json |
| Nombres de grupos y objetos | data/groups.json y data/items.json |
| Costes de grupos y pasos de huevo | src/config/grupos.js |
| Precios, efectos, desbloqueos y beneficios temporales | src/config/objetos.js y beneficios.js |
| Reglas de selección, compra, incubación, evolución y recompensas | src/core/ |
| Validación y carga de partidas | src/storage/partida.js |
| Pantallas, filtros y avisos | src/ui/ |
| Controles y temporizador | src/app/ |
| Sonidos y carga de sprites externos | src/audio/ y src/assets/ |
| Estilos, fuentes y estructura de la página | styles/juego.css, assets/fuentes/ y src/plantilla.html |
| Sprites del juego | assets/ (el nombre del PNG sin extensión es su clave) |

Los scripts comparten el mismo ámbito y se ensamblan en el orden de **src/orden.json**. Si añades un script, incluye su ruta en esa lista. El generador incrusta datos, imágenes, fuentes, estilos y scripts en un único HTML. Cada generación sobrescribe el HTML de distribución.
