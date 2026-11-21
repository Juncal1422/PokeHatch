function dex() {
  return (
    heading(
      'Pokédex',
      'Consulta las familias y sus condiciones de evolución.',
      st.dex.length + ' / ' + species.length,
    ) +
    '<div id="evolutionReport" role="status"></div><div class="tools"><input id="search" placeholder="Nombre o número" aria-label="Buscar especie"><select id="gen" aria-label="Generación"><option value="0">Todas las generaciones</option>' +
    [1, 2, 3, 4, 5]
      .map(
        (i) =>
          '<option value="' +
          i +
          '" ' +
          (i === gen ? 'selected' : '') +
          '>Generación ' +
          i +
          '</option>',
      )
      .join('') +
    '</select><select id="dexGroup" aria-label="Grupo de huevo"><option value="0">Todos los grupos</option>' +
    groupIDs
      .map(
        (g) =>
          '<option value="' +
          g +
          '" ' +
          (g === filterGroup ? 'selected' : '') +
          '>' +
          G[g] +
          '</option>',
      )
      .join('') +
    '</select><label><input id="only" type="checkbox" ' +
    (only ? 'checked' : '') +
    '> Solo registradas</label><label><input id="ready" type="checkbox" ' +
    (readyOnly ? 'checked' : '') +
    '> Listos para evolucionar</label><label><input id="shinyFilter" type="checkbox" ' +
    (shinyOnly ? 'checked' : '') +
    '> Solo shinies</label><button class="btn" id="bulkEvolve" data-act="evolveAll" ' +
    (readyOnly ? '' : 'hidden') +
    '>Evolucionar todos</button></div><div id="dexlist"></div>'
  );
}
let dexSignature = '';
function entries() {
  $('bulkEvolve').hidden = !readyOnly;
  const readyParents = new Set(
    readyOnly ? species.filter(ready).map((s) => s.p) : [],
  );
  const matches = species.filter(
    (s) =>
      (!only || st.dex.includes(s.id)) &&
      (!shinyOnly || st.shiny.includes(s.id)) &&
      (!readyOnly || readyParents.has(s.id)) &&
      (!gen || s.gen === gen) &&
      (!filterGroup || dexGroups(s).includes(filterGroup)) &&
      (s.n.toLowerCase().includes(query.toLowerCase()) ||
        rid(s.id).includes(query)),
  );
  const pages = Math.ceil(matches.length / 64);
  dexPage = Math.min(dexPage, Math.max(0, pages - 1));
  const signature = [
    dexPage,
    pages,
    ...matches
      .slice(dexPage * 64, dexPage * 64 + 64)
      .map(
        (s) =>
          s.id +
          ':' +
          Number(st.dex.includes(s.id)) +
          ':' +
          Number(st.shiny.includes(s.id)),
      ),
  ].join('|');
  if ($('dexlist').firstChild && dexSignature === signature) return;
  dexSignature = signature;
  $('dexlist').innerHTML =
    '<div class="catalog dex">' +
    matches
      .slice(dexPage * 64, dexPage * 64 + 64)
      .map(
        (s) =>
          '<button data-act="info-' +
          s.id +
          '" class="entry" style="background:' +
          cardPaint(s) +
          '" title="' +
          dexGroups(s)
            .map((g) => G[g])
            .join(' / ') +
          '"><small>N.º ' +
          rid(s.id) +
          '</small><br>' +
          image(
            (st.shiny.includes(s.id) ? 'sh' : '') + s.id,
            st.dex.includes(s.id) || st.shiny.includes(s.id) ? '' : 'unknown',
          ) +
          '<h3>' +
          (st.shiny.includes(s.id) ? '✦ ' : '') +
          s.n +
          '</h3></button>',
      )
      .join('') +
    '</div>' +
    (pages
      ? '<div class="tools pager">' +
        button('←', 'prev', !dexPage) +
        '<span>' +
        (dexPage + 1) +
        ' / ' +
        pages +
        '</span>' +
        button('→', 'next', dexPage + 1 >= pages) +
        '</div>'
      : '<p>No hay coincidencias.</p>');
}
function modal(html) {
  $('detailBody').innerHTML = html;
  if (!$('detail').open) {
    $('detail').showModal();
    $('detail').append($('births'), $('coinToasts'));
  }
}
function settings() {
  return (
    heading('Configuración', 'Gestiona la partida de este navegador.') +
    card(
      'Partida',
      '<p>' +
        (broken
          ? 'El guardado anterior no se ha podido leer. Importa una copia o confirma el reinicio para volver a guardar.'
          : storageError
            ? 'El navegador ha bloqueado el guardado. Exporta una copia.'
            : 'La partida se guarda automáticamente en este navegador.') +
        '</p><div class="tools">' +
        button('Exportar JSON', 'export') +
        button('Importar JSON', 'import') +
        button('Empezar de cero', 'reset') +
        '</div><small>Importar sustituye la partida actual. Las incubadoras funcionan mientras el juego está abierto y visible.</small>',
    ) +
    card(
      'Sonido',
      '<div class="cry-control"><div class="cry-heading"><label for="cryVolume">Volumen de los gritos</label><output id="cryVolumeValue" for="cryVolume">' +
        cryVolume +
        ' %</output></div><input id="cryVolume" type="range" min="0" max="100" step="1" value="' +
        cryVolume +
        '" style="--volume:' +
        cryVolume +
        '%"><div class="cry-scale">' +
        button('0 %', 'cry-volume-0') +
        button('100 %', 'cry-volume-100') +
        '</div></div>',
    )
  );
}
function info(i, reset = true) {
  const s = S[i];
  if (reset) {
    showShiny = st.shiny.includes(i);
    detailId = i;
  }
  const list = children(i)
    .filter((t) => t.id !== 292)
    .map(
      (t) =>
        '<div class="evrow" data-evolution="' +
        t.id +
        '"><b>' +
        t.n +
        '</b><small>' +
        condition(t) +
        '</small><small>' +
        Math.max(0, progress(t)) +
        ' / ' +
        required(t) +
        (t.rule.item
          ? ' · ' + (owned(t.rule.item) ? '✓ ' : 'Falta ') + names[t.rule.item]
          : '') +
        '</small>' +
        button(
          st.dex.includes(t.id) ? 'Registrado' : 'Evolucionar a ' + t.n,
          'evolve-' + t.id,
          !ready(t),
        ) +
        '</div>',
    )
    .join('');
  modal(
    '<div class="spriteTools">' +
      image((st.shiny.includes(i) && showShiny ? 'sh' : '') + i) +
      (st.shiny.includes(i)
        ? '<button class="switch" role="switch" aria-checked="' +
          showShiny +
          '" data-act="sprite"><i></i>Shiny</button>'
        : '') +
      '</div>' +
      '<h2>N.º ' +
      rid(i) +
      ' · ' +
      s.n +
      '</h2><p>Generación ' +
      s.gen +
      ' · ' +
      dexGroups(s)
        .map((g) => G[g])
        .join(' / ') +
      '</p>' +
      '<p>' +
      condition(s) +
      '</p><p>Crías de ' +
      S[s.root].n +
      ' obtenidas: <span id="detailCount">' +
      (st.counts[s.root] || 0) +
      '</span></p>' +
      list,
  );
}
function refreshDetail(id) {
  const s = S[id];
  $('detailCount').textContent = st.counts[s.root] || 0;
  const tools = $('detailBody').querySelector('.spriteTools');
  if (st.shiny.includes(id) && !tools.querySelector('button'))
    tools.insertAdjacentHTML(
      'beforeend',
      '<button class="switch" role="switch" aria-checked="false" data-act="sprite"><i></i>Shiny</button>',
    );
  for (const row of $('detailBody').querySelectorAll('[data-evolution]')) {
    const t = S[row.dataset.evolution],
      button = row.querySelector('button');
    row.querySelector('small:last-of-type').textContent =
      Math.max(0, progress(t)) +
      ' / ' +
      required(t) +
      (t.rule.item
        ? ' · ' + (owned(t.rule.item) ? '✓ ' : 'Falta ') + names[t.rule.item]
        : '');
    button.disabled = !ready(t);
  }
}
