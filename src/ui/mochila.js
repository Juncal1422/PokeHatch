function unlockSource(k) {
  if (snackKeys.includes(k))
    return 'Completa las 8 medallas de ' + regions[snackKeys.indexOf(k)];
  if (ballKeys.includes(k))
    return 'Completa la Pokédex de ' + regions[ballKeys.indexOf(k)];
  if (k === 'master-ball') return 'Completa toda la Pokédex';
  if (k === 'iris-ticket')
    return 'Registra 1 Pokémon shiny de cada grupo de huevo';
  if (Object.hasOwn(scarves, k))
    return (
      'Registra 3 especies shiny entre los grupos ' +
      scarves[k][1].map((g) => G[g]).join(', ')
    );
  if (k.startsWith('relic-'))
    return 'Encuéntralo al eclosionar un Pokémon fósil';
  const sources = {
    'ice-stone': 'un Pokémon de tipo Hielo',
    'amulet-coin': 'Meowth',
    'rare-bone': 'Cubone',
    'air-balloon': 'un huevo Volador',
    'light-ball': 'Pichu',
  };
  if (sources[k]) return 'Encuéntralo al eclosionar ' + sources[k];
  const roots = Object.keys(dropByRoot).filter((id) =>
    dropByRoot[id].includes(k),
  );
  return roots.length
    ? 'Encuéntralo al eclosionar ' + roots.map((id) => S[id].n).join(', ')
    : '';
}
function inventory() {
  const tile = (k, n, desc) => {
    const source = unlockSource(k);
    return (
      '<div class="entry">' +
      itemArt(k) +
      '<h3>' +
      n +
      '</h3><p>' +
      desc.replace(/\.$/, '') +
      '</p>' +
      (source ? '<small class="gate">' + source + '</small>' : '') +
      '</div>'
    );
  };
  const grid = (a) => '<div class="catalog bag">' + a.join('') + '</div>';
  const earned = Math.floor(
    st.dex.reduce((n, id) => n + Number(!!S[id].p), 0) / 8,
  );
  const icons = badges
    .map(
      (k, i) =>
        '<img class="' +
        (i < earned ? 'earned' : 'locked') +
        '" src="' +
        A['badge-' + k] +
        '" alt="Medalla ' +
        badgeNames[i] +
        '" title="Medalla ' +
        badgeNames[i] +
        '">',
    )
    .join('');
  const descr = (k) =>
    rewards[k] ||
    loot[k]?.[1] ||
    (evoUses[k]
      ? describeEvolutionItem(k) +
        (k === 'deep-sea-scale'
          ? ' Facilita la eclosión de los huevos Abisal un 25 %.'
          : '')
      : goods[k][1]);
  const sections = itemSections
    .map((title) => {
      const keys = st.owned
        .filter((k) => sectionOf(k) === title)
        .sort(categoryOrder);
      return card(
        title,
        keys.length
          ? grid(keys.map((k) => tile(k, names[k], descr(k))))
          : 'Aún no tienes objetos de esta categoría.',
      );
    })
    .join('');
  const permanent = st.benefits.length
    ? card(
        'Beneficios Temporales',
        grid(st.benefits.map((k) => tile(k, benefits[k][0], benefits[k][2]))),
      )
    : '';
  return (
    heading('Mochila', 'Objetos y medallas de la granja.') +
    sections +
    permanent +
    card('Medallas', '<div class="badges">' + icons + '</div>')
  );
}
