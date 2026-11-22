// DB conserva los nombres exactos de los archivos JSON cargados.
const IC = DB['item-config'];
const names = { ...IC.names };
const drops = IC.drops;
const goods = {},
  clickItems = { ...IC.clickItems },
  rateItems = { ...IC.rateItems },
  boostItems = {},
  gates = {};
const choiceItems = IC.choiceItems;
const potions = IC.potions;
const flutes = IC.flutes;
const fluteNames = IC.fluteNames;
const scents = IC.scents;
const scentNames = [
  'Incienso Marino',
  'Incienso Suave',
  'Incienso Roca',
  'Incienso Rosa',
  'Incienso Puro',
  'Incienso Aqua',
  'Incienso Lento',
];
const scentRules = Object.fromEntries(
  Object.entries(IC.scentRules).map(([k, v]) => [k, [v[0], v[1], v[2]]]),
);
const scarves = IC.scarves;
const snacks = IC.snacks;
const balls = IC.balls;
const ballKeys = IC.ballKeys;
const snackKeys = IC.snackKeys;
const loot = Object.fromEntries(
  Object.entries(IC.loot).map(([k, v]) => [k, [v[0], v[1]]]),
);

function product(key, name, price, description, gate) {
  names[key] = name;
  goods[key] = [price, description];
  if (gate) gates[key] = gate;
}

for (const [k, name, price, value, id] of [
  ['macho-brace', 'Brazal Firme', 50, 1],
  ['lucky-egg', 'Huevo Suerte', 200, 2],
  ['power-weight', 'Pesa Recia', 250, 3, 242],
  ['power-bracer', 'Brazal Recio', 250, 2, 409],
  ['power-belt', 'Cinto Recio', 250, 1, 213],
]) {
  clickItems[k] = value;
  product(
    k,
    name,
    price,
    'Aumenta en ' + value + ' la eficacia de cada toque.',
    id ? { id } : null,
  );
}
for (const [k, name, price, pct, id] of [
  ['power-lens', 'Lente Recia', 250, 50, 609],
  ['power-band', 'Banda Recia', 250, 50, 476],
  ['power-anklet', 'Franja Recia', 250, 25, 291],
  ['heat-rock', 'Roca Calor', 500, 25],
]) {
  rateItems[k] = pct;
  product(
    k,
    name,
    price,
    'Aumenta un ' + pct + ' % el ritmo de todas las incubadoras.',
    id ? { id } : null,
  );
}
for (const [g, k] of Object.entries(IC.boostItems)) {
  const group = +g;
  boostItems[group] = k;
  product(
    k,
    names[k],
    400,
    'Facilita la eclosión de los huevos de ' + G[group] + ' un 25 %.',
    { g: group },
  );
}

product(
  'rare-candy',
  'Carameloraro',
  50,
  'Reduce un 10 % las eclosiones necesarias para evolucionar, con una rebaja mínima de 1',
  { evol: 5 },
);
product(
  'nugget',
  'Pepita',
  25,
  'Obtienes 1 PC por cada eclosión de un Pokémon ya registrado',
);
product(
  'pearl',
  'Perla',
  100,
  'Obtienes 1 PC más por cada eclosión de un Pokémon ya registrado',
  { requires: 'nugget' },
);
product(
  'star-piece',
  'Trozo Estrella',
  250,
  'Obtienes 2 PC más por cada eclosión de un Pokémon ya registrado',
  { requires: 'nugget' },
);
product(
  'devon-parts',
  'Piezas Devon',
  200,
  'Aumenta un 50 % las PC por evolución',
);
[
  'Poción',
  'Superpoción',
  'Hiperpoción',
  'Max Poción',
  'Restaurar Todo',
].forEach((name, i) =>
  product(
    potions[i],
    name,
    100 + 25 * i,
    'Reduce un 10 % la probabilidad de obtener una especie ya completada en la Pokédex',
  ),
);
product(
  'choice-scarf',
  'Pañuelo Elección',
  250,
  'Reduce un 10 % la probabilidad de obtener una especie sin evolución.',
  { evol: 5 },
);
product(
  'choice-specs',
  'Gafas Elección',
  300,
  'Reduce un 15 % la probabilidad de obtener una especie sin evolución.',
  { evol: 10 },
);
product(
  'choice-band',
  'Cinta Elección',
  500,
  'Reduce un 25 % la probabilidad de obtener una especie sin evolución.',
  { evol: 15 },
);
product(
  'shiny-charm',
  'Amuleto Iris',
  1000,
  'Aumenta de 0,05 % a 0,25 % la probabilidad de obtener shinies.',
  { shiny: 10 },
);
product(
  'charcoal',
  'Carbón',
  500,
  'Aumenta un 50 % el ritmo de todas las incubadoras.',
  { total: 1000 },
);
scents.forEach((k) => {
  const rule = scentRules[k];
  const name =
    k === 'sea-incense'
      ? 'Incienso Marino'
      : k === 'lax-incense'
        ? 'Incienso Suave'
        : k === 'rock-incense'
          ? 'Incienso Roca'
          : k === 'rose-incense'
            ? 'Incienso Rosa'
            : k === 'pure-incense'
              ? 'Incienso Puro'
              : k === 'wave-incense'
                ? 'Incienso Aqua'
                : 'Incienso Lento';
  product(k, name, 500, rule[2], { baby: rule[0] });
});
product(
  'discount-coupon',
  'Cupón de Descuento',
  250,
  'Reduce un 10 % los precios de la tienda.',
  { purchases: 10 },
);
product(
  'stardust',
  'Polvo Estelar',
  500,
  'Aumenta un 50 % las PC por eclosión.',
);

const rewards = {};
function reward(key, name, description) {
  names[key] = name;
  rewards[key] = description;
}
for (const [key, [name, groups]] of Object.entries(scarves)) {
  const labels = groups.map((g) => G[g]).join(', ');
  reward(
    key,
    name,
    'Aumenta en 0,1 puntos porcentuales la probabilidad shiny en ' + labels,
  );
}
reward(
  'iris-ticket',
  'Iris-ticket',
  'Duplica las PC por eclosión repetida de un Pokémon ya registrado como shiny',
);
regions.forEach((region, i) => {
  reward(snackKeys[i], snacks[i], 'Aumenta un 50% las PC por evolución.');
  reward(
    ballKeys[i],
    balls[i],
    'Aumenta un 20% la probabilidad de obtener una especie aún bloqueada o con evoluciones bloqueadas.',
  );
});
reward(
  'master-ball',
  'Master Ball',
  'Facilita la eclosión de los huevos un 50% y aumenta un 100% el ritmo de todas las incubadoras.',
);
product(
  'gs-ball',
  'GS Ball',
  1000,
  'Úsala para resonar con Celebi y volver al pasado.',
  { badges: 1 },
);
product(
  'flame-orb',
  'Llamasfera',
  1000,
  'Duplica la potencia de las incubadoras.',
);
product('toxic-orb', 'Toxisfera', 1000, 'Duplica la potencia de cada toque.');
product(
  'lustrous-orb',
  'Lustresfera',
  1000,
  'Duplica la probabilidad de obtener shinies.',
);

for (const [k, [name]] of Object.entries(loot)) names[k] = name;
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
const iceHatchlings = new Set(IC.iceHatchlings);
const fossils = new Set(IC.fossils);
