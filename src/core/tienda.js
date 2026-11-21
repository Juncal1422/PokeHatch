// Reglas de disponibilidad y compra de objetos de tienda.
function isShopItemAcquired(item) {
  const gate = gates[item]?.g;
  return gate ? st.boosts.includes(gate) : owned(item);
}

function describeShopGate(
  item,
  evolutions = st.dex.reduce((n, id) => n + Number(!!S[id].p), 0),
) {
  const rule = gates[item];
  if (!rule) return '';
  if (rule.id && !st.dex.includes(rule.id)) return 'Registra a ' + S[rule.id].n;
  if (rule.requires && !owned(rule.requires))
    return 'Compra ' + names[rule.requires];
  if (rule.badges && evolutions < 64 * rule.badges)
    return 'Completa las 8 medallas de ' + regions[rule.badges - 1];
  if (rule.region && !complete(regionLists[rule.region - 1]))
    return 'Completa la Pokédex de ' + regions[rule.region - 1];
  if (rule.full && st.dex.length < species.length) return 'Completa la Pokédex';
  const threshold = hasBenefit('rare-incense') ? 13 : 25;
  if (rule.baby && (st.counts[rule.baby] || 0) < threshold)
    return (
      S[rule.baby].n +
      ' · ' +
      (st.counts[rule.baby] || 0) +
      ' / ' +
      threshold +
      ' eclosiones'
    );
  if (rule.purchases && purchaseCount() < rule.purchases)
    return 'Objetos comprados · ' + purchaseCount() + ' / ' + rule.purchases;
  if (rule.g && (st.groupHatches[rule.g] || 0) < 40)
    return (
      G[rule.g] + ' · ' + (st.groupHatches[rule.g] || 0) + ' / 40 eclosiones'
    );
  if (rule.shiny && st.shinyBirths < rule.shiny)
    return 'Shinies eclosionados · ' + st.shinyBirths + ' / ' + rule.shiny;
  if (rule.total && st.total < rule.total)
    return 'Eclosiones · ' + st.total + ' / ' + rule.total;
  if (rule.evol && evolutions < rule.evol)
    return 'Evoluciona ' + rule.evol + ' Pokémon';
  return '';
}

function buyShopItem(item) {
  if (
    drops.includes(item) ||
    !goods[item] ||
    isShopItemAcquired(item) ||
    describeShopGate(item)
  )
    return;
  if (item === 'gs-ball') {
    if (st.coins >= 1000)
      confirmAction('¿Seguro que quieres volver al pasado?', 'prestige');
    return;
  }
  if (!spendCoins(goods[item][0], item)) return;
  if (!owned(item)) st.owned.push(item);
  const group = gates[item]?.g;
  if (group) st.boosts.push(group);
  refreshTargets();
  save();
  renderCurrentView();
  notify(names[item] + ' conseguido');
}
