# PokeHatch

Descomprime el contenido del ZIP en la raíz de `develop`. La entrada del juego es **PokeHatch.html**. Si actualizas la distribución anterior, elimina del repositorio `generar.py`, `PokeHatch v1.0.html`, `src/plantilla.html` y `src/orden.json`. Actualiza cualquier enlace de esa rama para que apunte a `PokeHatch.html`.

En un alojamiento estático, abre la URL de `PokeHatch.html`. Desde el disco, abre ese mismo archivo y selecciona la carpeta **data** cuando el navegador lo pida. En ambos casos los cambios se aplican al recargar. Algunos sonidos y sprites adicionales se descargan de Internet. Para trasladar una partida entre ubicaciones, usa las opciones de exportación e importación en Ajustes.

| Qué editar | Archivo o carpeta |
|---|---|
| Especies y evoluciones, grupos y nombres de objetos | `data/species.json`, `data/groups.json`, `data/items.json` |
| Precios, pasos y beneficios | `src/config/` |
| Compras, recompensas, incubación y evolución | `src/core/` |
| Guardado e importación | `src/storage/partida.js` |
| Pantallas y acciones del jugador | `src/ui/` y `src/app/controles.js` |
| Temporizador y arranque | `src/app/reloj.js` y `src/app/inicio.js` |
| Sonidos y sprites adicionales | `src/audio/` y `src/assets/` |
| Imágenes y fuentes locales | `assets/` |
| Estructura y estilos | `PokeHatch.html` y `styles/juego.css` |

Para añadir una imagen, guarda el PNG en `assets/` y registra su clave y ruta en `data/art.json`. Para añadir un script del juego, incluye su ruta en `data/scripts.json` después de sus dependencias. Las rutas parten de la raíz del proyecto.
