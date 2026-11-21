// Relaciones de especies y selección aleatoria: lógica de juego, no presentación.
const evoUses = {},
  byParent = {},
  dropByRoot = {},
  families = {},
  growingPools = {};

for (const s of species) {
  if (!s.evolvesFromId) continue;
  (byParent[s.evolvesFromId] ??= []).push(s);
  if (s.familyRootId) {
    (families[s.familyRootId] ??= []).push(s.id);
  }
  const item = s.evolutionRequirement.requiredItem;
  if (!item) continue;
  (evoUses[item] ??= []).push(s);
  if (item !== 'ice-stone' && drops.includes(item)) {
    const keys = (dropByRoot[s.familyRootId] ??= []);
    dropByRoot[s.familyRootId] = keys;
    if (!keys.includes(item)) keys.push(item);
  }
}
for (const g of groupIDs) {
  growingPools[g] = pools[g].filter((s) => byParent[s.id]);
}
for (const item of Object.keys(evoUses)) {
  if (!drops.includes(item)) goods[item] = [500, describeEvolutionItem(item)];
}

// Devuelve el texto descriptivo usado por la tienda para objetos evolutivos.
function describeEvolutionItem(item) {
  const parents = [
    ...new Set(evoUses[item].map((s) => S[s.evolvesFromId].name)),
  ];
  return item === 'soothe-bell'
    ? 'Para evolucionar a pokémon por amistad.'
    : 'Para evolucionar a: ' + parents.join(', ') + '.';
}

// Filtra las especies disponibles por las generaciones activas en la partida.
function availableEggPool(group, a) {
  return a.generations.length === 5
    ? pools[group]
    : pools[group].filter((s) => a.generations.includes(s.generation));
}

// Aplica las preferencias de objetos y medallas al sorteo de una eclosión.
function chooseHatchingSpecies(group) {
  const pool = availableEggPool(group, st);
  const growing =
    pool === pools[group]
      ? growingPools[group]
      : pool.filter((s) => byParent[s.id]);
  const reduction = st.owned.reduce(
    (sum, key) => sum + (choiceItems[key] || 0),
    0,
  );
  const singleFactor =
    (1 - reduction / 100) * (owned('pure-incense') ? 0.5 : 1);
  const regular = () => {
    const speciesResult = pick(pool);
    return !byParent[speciesResult.id] &&
      growing.length &&
      Math.random() >= singleFactor
      ? pick(growing)
      : speciesResult;
  };
  const bonus = ballKeys.filter(owned).length * 0.2;
  let chosen = regular();
  if (bonus) {
    const registered = new Set(st.dex);
    const isPending = (s) =>
      !registered.has(s.id) ||
      (families[s.id] || []).some((id) => !registered.has(id));
    if (!isPending(chosen)) {
      const candidates = pool.filter(isPending);
      const extra = growing.length
        ? ((pool.length - growing.length) * (1 - singleFactor)) / growing.length
        : 0;
      const weight = (s) =>
        byParent[s.id] ? 1 + extra : growing.length ? singleFactor : 1;
      const sum = candidates.reduce((n, s) => n + weight(s), 0);
      if (sum && Math.random() < (bonus * sum) / (pool.length - sum)) {
        let roll = Math.random() * sum;
        chosen =
          candidates.find((s) => (roll -= weight(s)) < 0) ||
          candidates[candidates.length - 1];
      }
    }
  }
  if (
    hasBenefit('badge-case') &&
    !byParent[chosen.id] &&
    growing.length &&
    Math.random() < 0.5
  ) {
    chosen = pick(growing);
  }
  const potionChance = potions.filter(owned).length / 10;
  if (potionChance && Math.random() < potionChance) {
    const registered = new Set(st.dex);
    const isIncomplete = (s) =>
      !registered.has(s.id) ||
      (families[s.id] || []).some((id) => !registered.has(id));
    if (!isIncomplete(chosen)) {
      const candidates = pool.filter(isIncomplete);
      if (candidates.length) chosen = pick(candidates);
    }
  }
  return chosen;
}
