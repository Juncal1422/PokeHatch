const spriteSlugs = {
  'iris-ticket': 'rainbow-pass',
  'gs-ball': 'park-ball',
  'rare-incense': 'odd-incense',
  'badge-case': 'medal-box',
  talisman: 'old-charm',
  case: 'prop-case',
  'odd-stone': 'intriguing-stone',
};

const spriteCache = 'PokeHatch.itemSprites.v2';
const spriteLoadConcurrency = 12;
const shinySpriteKey = /^sh\d+$/;
const remoteItems = [
  ...potions,
  'devon-parts',
  ...Object.keys(rewards),
  ...Object.keys(benefits),
  'toxic-orb',
  'lustrous-orb',
  'reset-urge',
  'gs-ball',
].filter((k) => !A[k]);
const spriteURLs = {};
for (const k of remoteItems) {
  const slug = spriteSlugs[k] || k;
  spriteURLs[k] =
    'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/' +
    slug +
    '.png';
  A[k] = spriteURLs[k];
}
try {
  const saved = JSON.parse(localStorage.getItem(spriteCache) || '{}');
  for (const k of remoteItems)
    if (
      typeof saved[k] === 'string' &&
      /^data:image\/png;base64,[A-Za-z0-9+/=]+$/.test(saved[k])
    )
      A[k] = saved[k];
} catch {}
async function loadItemSprites() {
  const entries = {};
  async function load(k) {
    if (A[k] !== spriteURLs[k]) {
      entries[k] = A[k];
      return true;
    }
    try {
      let response = await fetch(spriteURLs[k]);
      if (!response.ok) {
        const data = await fetch(
          'https://pokeapi.co/api/v2/item/' + (spriteSlugs[k] || k) + '/',
        );
        const url = (await data.json()).sprites?.default;
        if (!url) return false;
        response = await fetch(url);
      }
      if (!response.ok) return false;
      const bitmap = await createImageBitmap(await response.blob());
      const canvas = document.createElement('canvas'),
        scale = Math.min(1, 64 / Math.max(bitmap.width, bitmap.height));
      canvas.width = Math.max(1, Math.round(bitmap.width * scale));
      canvas.height = Math.max(1, Math.round(bitmap.height * scale));
      canvas
        .getContext('2d')
        .drawImage(bitmap, 0, 0, canvas.width, canvas.height);
      bitmap.close();
      A[k] = entries[k] = canvas.toDataURL('image/png');
      for (const img of document.querySelectorAll(
        '[data-item-icon="' + k + '"]',
      ))
        img.src = A[k];
      return true;
    } catch {}
    return false;
  }
  let nextItem = 0;
  const failedItems = [];
  await Promise.all(
    Array.from(
      { length: Math.min(spriteLoadConcurrency, remoteItems.length) },
      async () => {
        while (nextItem < remoteItems.length) {
          const item = remoteItems[nextItem++];
          if (!(await load(item))) failedItems.push(item);
        }
      },
    ),
  );
  try {
    localStorage.setItem(spriteCache, JSON.stringify(entries));
  } catch {}
  if (failedItems.length)
    console.warn('No se pudieron cargar iconos de objetos:', failedItems);
}

async function preloadArtSprites(entries, priority = 'auto') {
  const urls = [...new Set(entries.map(([key]) => A[key]).filter(Boolean))];
  let nextUrl = 0;
  const failedUrls = [];
  await Promise.all(
    Array.from(
      { length: Math.min(spriteLoadConcurrency, urls.length) },
      async () => {
        while (nextUrl < urls.length) {
          const url = urls[nextUrl++];
          const loaded = await new Promise((resolve) => {
            const image = new Image();
            image.decoding = 'async';
            image.fetchPriority = priority;
            image.onload = () => resolve(true);
            image.onerror = () => resolve(false);
            image.src = url;
          });
          if (!loaded) failedUrls.push(url);
        }
      },
    ),
  );
  if (failedUrls.length)
    console.warn('No se pudieron precargar sprites:', failedUrls);
  return failedUrls.length;
}

async function preloadAllSprites() {
  const entries = Object.entries(DB.art);
  const regularSprites = entries.filter(([key]) => !shinySpriteKey.test(key));
  const shinySprites = entries.filter(([key]) => shinySpriteKey.test(key));

  await Promise.all([preloadArtSprites(regularSprites), loadItemSprites()]);
  await preloadArtSprites(shinySprites, 'low');
}
const itemArt = (k) =>
  '<img src="' + A[k] + '" data-item-icon="' + k + '" alt="">';
