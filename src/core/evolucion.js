function addDex(id) {
  if (st.dex.includes(id)) return false;
  st.dex.push(id);
  st.since[id] = st.counts[S[id].root] || 0;
  awardMilestones(st);
  return true;
}
function progress(s) {
  const rule = s.rule;
  return (st.counts[s.root] || 0) - (rule.extra ? st.since[s.p] || 0 : 0);
}
function ready(s) {
  return (
    s.id !== 292 &&
    s.p &&
    st.dex.includes(s.p) &&
    !st.dex.includes(s.id) &&
    progress(s) >= required(s) &&
    (!s.rule.item || owned(s.rule.item))
  );
}
function inheritShiny(root) {
  for (const id of st.dex)
    if (S[id].root === root && !st.shiny.includes(id)) st.shiny.push(id);
  awardShinyItems(st);
}
function applyEvolution(s) {
  addDex(s.id);
  if (s.id === 291) addDex(292);
  earn(
    Math.floor(
      25 *
        (1 +
          0.5 *
            (snackKeys.filter(owned).length + Number(owned('devon-parts')))) *
        (hasBenefit('everstone') ? 2 : 1),
    ),
    '',
    'evolution',
  );
  if (st.shiny.includes(s.root)) inheritShiny(s.root);
}
function evolve(id) {
  const s = S[id];
  if (!ready(s)) return;
  applyEvolution(s);
  detailId = 0;
  if ($('detail').open) $('detail').close();
  save();
  refreshLiveViews();
  notify(
    '¡' +
      (id === 291
        ? 'Ninjask y Shedinja se han registrado'
        : s.n + ' se ha registrado') +
      ' en la Pokédex!',
  );
}
function evolveAll() {
  const before = new Set(st.dex);
  let pending = species.filter(ready);
  while (pending.length) {
    pending.forEach(applyEvolution);
    pending = species.filter(ready);
  }
  const evolved = st.dex.filter((id) => !before.has(id)).map((id) => S[id].n);
  if (evolved.length) {
    save();
    refreshLiveViews();
  }
  const report = $('evolutionReport'),
    message = document.createElement('p');
  report.replaceChildren();
  message.textContent = evolved.length
    ? 'Evoluciones (' + evolved.length + '): ' + evolved.join(', ') + '.'
    : 'Ningún Pokémon está listo para evolucionar.';
  fadingToast(report, message, Math.max(4500, evolved.length * 750));
}
function condition(s) {
  if (s.id === 292) return 'Se registra al evolucionar a Nincada en Ninjask.';
  if (!s.p)
    return s.pool.length
      ? 'Nace en ' + s.pool.map((g) => G[g]).join(' o ') + '.'
      : 'Todavía no tiene un método de obtención.';
  const r = s.rule;
  return (
    required(s) +
    ' eclosiones de ' +
    S[s.root].n +
    (r.extra ? ' después de registrar a ' + S[s.p].n : ' en total') +
    (r.item ? ' y ' + names[r.item] : '') +
    '.' +
    (s.id === 291 ? ' También registra a Shedinja.' : '')
  );
}
function showBirthToast(s, shiny, items) {
  const row = document.createElement('div');
  row.className = 'birth' + (shiny ? ' rare' : '');
  row.innerHTML =
    image((shiny ? 'sh' : '') + s.id) +
    '<span>' +
    (shiny ? '✦ ' : '') +
    s.n +
    '</span>' +
    items.map((k) => image(k) + '<small>' + names[k] + '</small>').join('');
  const rail = $('births');
  fadingToast(rail, row, 2000);
}
