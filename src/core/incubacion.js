// Convierte los modificadores activos en el número de pasos de una eclosión.
function calculateEggTarget(group, a) {
  let speed = 1;
  for (const item of a.owned) {
    if (scentRules[item]?.[1].includes(group)) speed++;
    else if (item === 'lax-incense') speed += 0.25;
    else if (item === 'air-balloon' && group === 4) speed++;
  }
  const slow =
    a.owned.includes('full-incense') && [3, 6, 4].includes(group) ? 3 : 1;
  return (
    ((stepCost[group] * (a.boosts.includes(group) ? 0.75 : 1) * slow) / speed) *
    (a.owned.includes('master-ball') ? 0.5 : 1) *
    (a.benefits.includes('life-orb') ? 0.75 : 1) *
    (a.benefits.includes(groupBenefits[group]) ? 0.5 : 1)
  );
}

// Resuelve una eclosión completa: especie, shiny, recompensas y objetos encontrados.
function hatchEgg(i, manual = false) {
  const e = st.eggs[i],
    oldGroup = e.g,
    s = chooseHatchingSpecies(e.g),
    shiny = Math.random() < calculateShinyChance(e.g);
  st.counts[s.id] = (st.counts[s.id] || 0) + 1;
  st.total++;
  st.groupHatches[e.g] = (st.groupHatches[e.g] || 0) + 1;
  const first = addDex(s.id),
    registeredShiny = st.shiny.includes(s.id);
  const ticket = !first && registeredShiny && owned('iris-ticket');
  earn(
    payout(first, shiny, registeredShiny),
    '',
    ticket ? 'ticket' : shiny ? 'shiny' : first ? 'new' : 'repeat',
  );
  if (shiny) {
    st.shinyBirths++;
    inheritShiny(s.id);
  }
  const possible = familyDrops(s.id).filter((k) => !owned(k));
  const items = [];
  if (
    s.id === 52 &&
    !owned('amulet-coin') &&
    Math.random() < 0.05 * dropBonus()
  )
    items.push('amulet-coin');
  if (possible.length && Math.random() < 0.05 * dropBonus())
    items.push(pick(possible));
  for (const [k, eligible, chance] of [
    ['ice-stone', iceHatchlings.has(s.id), 0.025],
    ['rare-bone', s.id === 104, 0.01],
    ['air-balloon', oldGroup === 4, 0.001],
    ['light-ball', s.id === 172, 0.001],
  ]) {
    if (eligible && !owned(k) && Math.random() < chance * dropBonus())
      items.push(k);
  }
  st.owned.push(...items);
  if (items.includes('deep-sea-scale') && !st.boosts.includes(9))
    st.boosts.push(9);
  if (items.includes('air-balloon') || items.includes('deep-sea-scale'))
    refreshTargets();
  const real = rollReal(s.id);
  if (real) {
    if (!owned(real[0])) st.owned.push(real[0]);
    earn(real[1], names[real[0]], 'real');
    items.push(real[0]);
  }
  st.eggs[i] = createEgg(st.active, e.lv);
  if (manual) save();
  showBirthToast(s, shiny, items);
  playCry(s.id);

  if (view === 'Criadero') reveal(i, oldGroup, s.id, shiny);
  queueLiveUpdate();
}
// Aplica progreso manual o automático a una plaza concreta.
function careForEgg(i, amount, manual = false) {
  const e = st.eggs[i];
  if (!e || !availableEggPool(e.g, st).length) return;
  e.x += amount;
  if (e.x >= e.max) {
    hatchEgg(i, manual);
    return;
  }
  if (view === 'Criadero') {
    const button = $('egg-' + i);
    if (button) {
      button.classList.remove('nudge');
      void button.offsetWidth;
      button.classList.add('nudge');
    }
  }
  if (manual) {
    save();
    farmStatus();
  }
}
const lvlCost = (e) => [25, 50, 100, 250, 500][e.lv];
const rates = [0, 0.25, 0.5, 1, 1.5, 2];
function advanceIncubator(i, dt) {
  const e = st.eggs[i];
  if (!e.lv || !availableEggPool(e.g, st).length) return false;
  let credit = e.charge + dt * rates[e.lv] * calculateIncubatorRateBoost();
  const steps = Math.floor(credit);
  e.charge = credit - steps;
  for (let j = 0; j < steps; j++)
    careForEgg(
      i,
      (owned('flame-orb') ? 2 : 1) * (hasBenefit('magma-stone') ? 2 : 1),
    );
  st.eggs[i].charge = credit - steps;
  return steps > 0;
}
function calculateShinyChance(g) {
  const base =
    (owned('shiny-charm') ? 0.0025 : 0.0005) *
    (owned('lustrous-orb') ? 2 : 1) *
    (hasBenefit('case') ? 2 : 1);
  return (
    base +
    (Object.entries(scarves).some(
      ([k, [, groups]]) => owned(k) && groups.includes(g),
    )
      ? 0.001
      : 0)
  );
}
function changeIncubatorLevel(i) {
  const e = st.eggs[i];
  if (!e || e.lv === 5 || !spendCoins(lvlCost(e))) return;
  e.lv++;
  save();
  renderCurrentView();
}
