'use strict';

let DB;

(() => {
  const nombres = ['species', 'groups', 'items', 'art', 'scripts'];
  const aviso = document.getElementById('carga');
  const selector = document.getElementById('selectorDatos');
  const entrada = document.getElementById('carpetaDatos');

  function cargarScript(ruta) {
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = ruta;
      script.onload = resolve;
      script.onerror = () => reject(new Error('No se pudo cargar ' + ruta));
      document.body.appendChild(script);
    });
  }

  async function iniciar(leer) {
    const datos = await Promise.all(nombres.map(async nombre => [nombre, await leer(nombre + '.json')]));
    DB = Object.fromEntries(datos);
    selector.hidden = true;
    try {
      for (const ruta of DB.scripts) await cargarScript(ruta);
    } catch (error) {
      aviso.textContent = error.message + '. Revisa la ruta y vuelve a abrir el juego.';
    }
  }

  if (location.protocol === 'file:') {
    aviso.textContent = 'Abrir PokeHatch desde el disco';
    selector.hidden = false;
    entrada.addEventListener('change', async () => {
      const archivos = new Map(Array.from(entrada.files, f => [f.webkitRelativePath, f]));
      entrada.value = '';
      entrada.disabled = true;
      try {
        await iniciar(async nombre => {
          const archivo = archivos.get('data/' + nombre);
          if (!archivo) throw new Error('Selecciona la carpeta data que contiene ' + nombre);
          return JSON.parse(await archivo.text());
        });
      } catch (error) {
        aviso.textContent = error.message;
      } finally {
        entrada.disabled = false;
      }
    });
  } else {
    iniciar(async nombre => {
      const ruta = 'data/' + nombre;
      const respuesta = await fetch(ruta);
      if (!respuesta.ok) throw new Error('No se pudo leer ' + ruta);
      return respuesta.json();
    }).catch(error => { aviso.textContent = error.message; });
  }
})();
