function addDex(id) {
  if (st.dex.includes(id)) return false;
  st.dex.push(id);
  st.since[id] = st.counts[S[id].familyRootId] || 0;
  awardMilestones(st);
  return true;
}
function progress(s) {
  return (
    (st.counts[s.familyRootId] || 0) -
    (s.evolutionRequirement.requiresHatchesAfterParentRegistered
      ? st.since[s.evolvesFromId] || 0
      : 0)
  );
}
function ready(s) {
  return (
    s.id !== 292 &&
    s.evolvesFromId &&
    st.dex.includes(s.evolvesFromId) &&
    !st.dex.includes(s.id) &&
    progress(s) >= required(s) &&
    (!s.evolutionRequirement.requiredItem ||
      owned(s.evolutionRequirement.requiredItem))
  );
}
function inheritShiny(root) {
  for (const id of st.dex)
    if (S[id].familyRootId === root && !st.shiny.includes(id))
      st.shiny.push(id);
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
  if (st.shiny.includes(s.familyRootId)) inheritShiny(s.familyRootId);
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
        : s.name + ' se ha registrado') +
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
  const evolved = st.dex
    .filter((id) => !before.has(id))
    .map((id) => S[id].name);
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
  if (!s.evolvesFromId)
    return s.hatchableEggGroups.length
      ? 'Nace en ' + s.hatchableEggGroups.map((g) => G[g]).join(' o ') + '.'
      : 'Todavía no tiene un método de obtención.';
  const requirement = s.evolutionRequirement;
  return (
    required(s) +
    ' eclosiones de ' +
    S[s.familyRootId].name +
    (requirement.requiresHatchesAfterParentRegistered
      ? ' después de registrar a ' + S[s.evolvesFromId].name
      : ' en total') +
    (requirement.requiredItem ? ' y ' + names[requirement.requiredItem] : '') +
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
    s.name +
    '</span>' +
    items.map((k) => image(k) + '<small>' + names[k] + '</small>').join('');
  const rail = $('births');
  fadingToast(rail, row, 2000);
}
