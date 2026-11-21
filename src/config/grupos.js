'use strict';
// DB conserva los nombres exactos de los archivos JSON cargados.
const GC = DB['group-config'];
const species = DB.species,
  S = Object.fromEntries(species.map((s) => [s.id, s])),
  G = GC.groups;
const pokemonArt = Object.fromEntries(
  species.flatMap((s) => [
    [String(s.id), 'assets/pokemon/' + s.id + '.png'],
    ['sh' + s.id, 'assets/shiny/sh' + s.id + '.png'],
  ]),
);
const A = { ...DB.art, ...pokemonArt };
const $ = (id) => document.getElementById(id),
  image = (k, cl = '', alt = '') =>
    '<img src="' + A[k] + '" class="' + cl + '" alt="' + alt + '">',
  rid = (n) => String(n).padStart(3, '0');
const groupIDs = GC.ids;
const stepCost = Object.fromEntries(
  Object.entries(GC.stepCost).map(([g, v]) => [+g, v]),
);
const groupCost = Object.fromEntries(
  Object.entries(GC.groupCost).map(([g, v]) => [+g, v]),
);
const prices = GC.bayPrices;
const regions = GC.regions;
const badges = GC.badges;
const badgeNames = GC.badgeNames;

const tint = Object.fromEntries(
  Object.entries(GC.tints).map(([g, v]) => [+g, v]),
);
function dexGroups(s) {
  return s.hatchableEggGroups.length
    ? s.hatchableEggGroups
    : s.eggGroups.includes(15)
      ? S[s.familyRootId].hatchableEggGroups
      : s.eggGroups;
}
function cardPaint(s) {
  const [g, h] = dexGroups(s);
  return h
    ? 'linear-gradient(135deg,' + tint[g] + ' 12%,' + tint[h] + ' 88%)'
    : tint[g];
}

const pools = Object.fromEntries(
  groupIDs.map((g) => [
    g,
    species.filter((s) => s.hatchableEggGroups.includes(g)),
  ]),
);
