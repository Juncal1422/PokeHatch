'use strict';
const species = DB.species,
  S = Object.fromEntries(species.map((s) => [s.id, s])),
  G = DB.groups,
  A = DB.art;
const $ = (id) => document.getElementById(id),
  image = (k, cl = '', alt = '') =>
    '<img src="' + A[k] + '" class="' + cl + '" alt="' + alt + '">',
  rid = (n) => String(n).padStart(3, '0');
// DB usa los nombres exactos de los JSON cargados: "group-config".
const GC = DB['group-config'];
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
  return s.pool.length ? s.pool : s.g.includes(15) ? S[s.root].pool : s.g;
}
function cardPaint(s) {
  const [g, h] = dexGroups(s);
  return h
    ? 'linear-gradient(135deg,' + tint[g] + ' 12%,' + tint[h] + ' 88%)'
    : tint[g];
}

const pools = Object.fromEntries(
  groupIDs.map((g) => [g, species.filter((s) => s.pool.includes(g))]),
);
