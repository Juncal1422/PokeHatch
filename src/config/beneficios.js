const benefits = Object.fromEntries(
  Object.entries(DB.benefits).filter(([k]) => k !== 'groupBenefits'),
);
const groupBenefits = {};
for (const [g, [key, name]] of Object.entries(DB.benefits.groupBenefits)) {
  const group = +g;
  groupBenefits[group] = key;
  benefits[key] = [
    name,
    100,
    'Facilita la eclosión de los huevos de ' + G[group] + ' un 50%.',
    group,
  ];
}
const scopeLists = Object.fromEntries(
  groupIDs.map((g) => [g, species.filter((s) => dexGroups(s).includes(g))]),
);
const regionLists = regions.map((_, i) =>
  species.filter((s) => s.gen === i + 1),
);
