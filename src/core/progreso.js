const owned = (k) => st.owned.includes(k);
// Potencia compartida de los toques manuales y las incubadoras automáticas.
const calculateTapPower = () =>
  (1 + st.owned.reduce((n, k) => n + (clickItems[k] || 0), 0)) *
  (owned('toxic-orb') ? 2 : 1);
const calculateIncubatorRateBoost = () =>
  (1 + st.owned.reduce((n, k) => n + (rateItems[k] || 0), 0) / 100) *
  (hasBenefit('magma-emblem') ? 2 : 1);
function spendCoins(base, key) {
  const price = shopPrice(base, key);
  if (st.coins < price) return false;
  st.coins -= price;
  return true;
}
function required(s) {
  const baseHatches = s.evolutionRequirement.baseHatches;
  const previous = Math.ceil(baseHatches * 0.6);
  const base = previous > 20 ? previous * 0.75 : baseHatches * 0.6;
  const threshold = (n) => {
    const full = Math.ceil((n * 6) / 5);
    return hasBenefit('exp-share')
      ? Math.max(
          1,
          Math.min(full - 1, Math.ceil((Math.ceil(n * 0.75) * 6) / 5)),
        )
      : full;
  };
  const normal = threshold(Math.ceil(base));
  if (!owned('rare-candy')) return normal;
  return Math.max(
    1,
    Math.min(normal - 1, threshold(Math.ceil((base * 90) / 100))),
  );
}
function payout(first, shiny, registeredShiny = false) {
  const base = first
    ? 10
    : owned('nugget')
      ? 1 + Number(owned('pearl')) + 2 * Number(owned('star-piece'))
      : 0;
  const bonus =
    (owned('amulet-coin') ? 0.5 : 0) +
    (owned('stardust') ? 0.5 : 0) +
    Number(owned('rare-bone'));
  return (
    Math.trunc((base + (shiny ? 10 : 0)) * (1 + bonus)) *
    (hasBenefit('blue-card') ? 2 : 1) *
    (!first && registeredShiny && owned('iris-ticket') ? 2 : 1)
  );
}
