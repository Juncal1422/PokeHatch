const hasBenefit = (k) => st.benefits.includes(k);
const complete = (list) => list.every((s) => st.dex.includes(s.id));
function awardShinyItems(a) {
  const give = (key) => {
    if (!a.owned.includes(key)) a.owned.push(key);
  };
  const shinyGroups = new Set(
    a.shiny.flatMap((id) => dexGroups(S[id])),
  );
  if (groupIDs.every((g) => shinyGroups.has(g))) give('iris-ticket');
  for (const [key, [, groups]] of Object.entries(scarves)) {
    const count = a.shiny.filter((id) =>
      dexGroups(S[id]).some((g) => groups.includes(g)),
    ).length;
    if (count >= 3) give(key);
  }
}
function awardMilestones(a) {
  const registered = new Set(a.dex),
    evolutions = a.dex.reduce(
      (n, id) => n + Number(!!S[id].evolvesFromId),
      0,
    );
  for (const g of groupIDs)
    if (
      !a.unlockedGroups.includes(g) &&
      (a.benefits.includes(groupBenefits[g]) ||
        scopeLists[g].every((s) => registered.has(s.id)))
    )
      a.unlockedGroups.push(g);
  const give = (k) => {
    if (!a.owned.includes(k)) a.owned.push(k);
  };
  regions.forEach((_, i) => {
    if (evolutions >= 64 * (i + 1)) give(snackKeys[i]);
    if (regionLists[i].every((s) => registered.has(s.id))) {
      a.keyUnlocked = true;
      give(ballKeys[i]);
    }
  });
  if (registered.size === species.length && !a.owned.includes('master-ball')) {
    give('master-ball');
    refreshTargets(a);
  }
}
function benefitGate(k) {
  if (k === 'card-key' && !st.keyUnlocked)
    return 'Completa la Pokédex de una generación';
  const g = benefits[k][3];
  return g && !st.unlockedGroups.includes(g)
    ? 'Completa la Pokédex del grupo ' + G[g]
    : '';
}
function buyBenefit(k) {
  if (
    !st.trips ||
    !Object.hasOwn(benefits, k) ||
    hasBenefit(k) ||
    benefitGate(k) ||
    st.rc < benefits[k][1]
  )
    return;
  st.rc -= benefits[k][1];
  if (k === 'parcel') earn(5);
  else {
    st.benefits.push(k);
    refreshTargets();
  }
  save();
  renderCurrentView();
  notify(benefits[k][0] + ' conseguido');
}
for (const [metal, value] of [
  ['copper', 500],
  ['silver', 1000],
  ['gold', 2000],
])
  rewards['relic-' + metal] =
    'Concede ' + value + ' PC cada vez que lo encuentras.';
Object.assign(names, {
  'relic-copper': 'Real de Cobre',
  'relic-silver': 'Real de Plata',
  'relic-gold': 'Real de Oro',
});
const dropBonus = () => (hasBenefit('luck-incense') ? 2 : 1);
function rollReal(id) {
  if (!fossils.has(id)) return null;
  const roll = Math.random() / (dropBonus() * (hasBenefit('talisman') ? 2 : 1));
  if (roll < 0.0001) return ['relic-gold', 2000];
  if (roll < 0.0006) return ['relic-silver', 1000];
  if (roll < 0.0016) return ['relic-copper', 500];
  return null;
}
function purchaseCount() {
  return (
    st.eggs.length -
    1 +
    st.owned.filter((k) => goods[k] && !drops.includes(k)).length
  );
}
function shopPrice(n, key) {
  if (key === 'gs-ball') return n;
  const price = owned('discount-coupon') ? Math.floor((n * 9) / 10) : n;
  return evoUses[key] && hasBenefit('odd-stone')
    ? Math.floor(price / 2)
    : price;
}
function refreshTargets(a = st) {
  for (const e of a.eggs) {
    const next = calculateEggTarget(e.g, a);
    if (next !== e.max) {
      e.x = (e.x / e.max) * next;
      e.max = next;
    }
  }
}
