const positions = [
  [50, 50],
  [29, 50],
  [71, 50],
  [50, 20],
  [50, 80],
  [29, 20],
  [71, 20],
  [29, 80],
  [71, 80],
];
function groupProgress(g, registered) {
  const hatched = pools[g].filter((s) => st.counts[s.id] > 0).length,
    total = pools[g].length;
  const full = scopeLists[g].every((s) => registered.has(s.id));
  return {
    state: full ? 'gold' : hatched === total ? 'silver' : '',
    label: hatched + ' / ' + total + ' crías',
  };
}
function periscope() {
  if (!hasBenefit('scope-lens')) return '';
  const registered = new Set(st.dex);
  const rows = groupIDs.map((g) => {
    const { state, label } = groupProgress(g, registered);
    return (
      '<div class="line group-progress ' +
      state +
      '">' +
      image('egg' + g, 'itemicon') +
      '<div><b>' +
      G[g] +
      '</b><small>' +
      label +
      '</small></div></div>'
    );
  });
  return card(
    'Progreso por grupo',
    '<div class="catalog">' + rows.join('') + '</div>',
  );
}
function generationPanel() {
  if (!hasBenefit('card-key')) return '';
  const switches = regions.map((region, index) => {
    const n = index + 1,
      on = st.generations.includes(n);
    return (
      '<button class="switch" role="switch" aria-checked="' +
      on +
      '" data-act="generation-' +
      n +
      '"><i></i>Gen. ' +
      n +
      ' · ' +
      region +
      '</button>'
    );
  });
  return card(
    'Generaciones',
    '<div class="tools">' + switches.join('') + '</div>',
  );
}
function farm() {
  return (
    heading(
      'Criadero',
      'Toca cada huevo para ayudar a la cría a salir.',
      st.eggs.length + ' / 9 PLAZAS',
    ) +
    '<section class="card"><div class="fieldhead"><b>PRADERA DE CRIANZA</b><small id="farmScore"></small></div><div class="pasture"><img class="landscape" src="' +
    A.meadow +
    '" alt="">' +
    st.eggs
      .map(
        (e, i) =>
          '<div id="nest-' +
          i +
          '" class="eggplace" style="left:' +
          positions[i][0] +
          '%;top:' +
          positions[i][1] +
          '%"><i></i><button id="egg-' +
          i +
          '" class="shell" data-act="tend-' +
          i +
          '">' +
          image('egg' + e.g) +
          '</button>' +
          (hasBenefit('silph-scope')
            ? '<progress id="progress-' +
              i +
              '" aria-label="Eclosión de la plaza ' +
              (i + 1) +
              '" max="1" value="0"></progress>'
            : '') +
          '</div>',
      )
      .join('') +
    '</div><div class="care" id="farmStatus"></div></section>' +
    generationPanel() +
    '<div id="periscope">' +
    periscope() +
    '</div>'
  );
}
function farmStatus() {
  if (!$('farmStatus')) return;
  const e = st.eggs[slot];
  $('farmScore').textContent =
    st.total + ' eclosiones · Pokédex ' + st.dex.length + '/' + species.length;
  $('farmStatus').innerHTML =
    '<div><b>Plaza ' +
    (slot + 1) +
    ' · ' +
    G[e.g] +
    '</b><small>' +
    (!availableEggPool(e.g, st).length
      ? 'En pausa por el filtro de generaciones'
      : e.x / e.max > 0.75
        ? '¡Se mueve!'
        : e.x / e.max > 0.35
          ? 'Se balancea suavemente.'
          : 'Está calentito.') +
    '</small></div><div><b>Incubadora</b><small>' +
    (e.lv ? 'Nivel ' + e.lv + ' de 5' : 'Sin instalar') +
    '</small></div>';
  for (let i = 0; i < st.eggs.length; i++) {
    const egg = st.eggs[i],
      b = $('egg-' + i);
    const bar = $('progress-' + i);
    if (bar) {
      bar.value = egg.x / egg.max;
      bar.dataset.zone =
        bar.value <= 0.2 ? 'low' : bar.value <= 0.5 ? 'mid' : 'high';
    }
    b.disabled = !availableEggPool(egg.g, st).length;
    b.title =
      (b.disabled ? 'En pausa por el filtro de generaciones · ' : '') +
      'Plaza ' +
      (i + 1) +
      ' · ' +
      G[egg.g];
    b.setAttribute('aria-label', b.title);
    b.parentNode.classList.toggle('current', i === slot);
  }
}
function reveal(i, g, id, shiny) {
  const n = $('nest-' + i);
  if (!n) return;
  const b = $('egg-' + i);
  b.querySelector('img').src = A['egg' + st.eggs[i].g];
  n.querySelector('.flash')?.remove();
  const fx = document.createElement('div');
  fx.className = 'flash';
  fx.innerHTML =
    image('egg' + g, 'crack') + image((shiny ? 'sh' : '') + id, 'pop');
  n.append(fx);
  setTimeout(() => fx.remove(), 1000);
}
