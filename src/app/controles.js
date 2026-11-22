let imported = null;
let debugMode = window.debug === true;
Object.defineProperty(window, 'debug', {
  configurable: true,
  get: () => debugMode,
  set: (value) => {
    debugMode = value === true;
    if (view === 'Ajustes') renderCurrentView();
  },
});
function finishDebugAction(message) {
  save();
  renderCurrentView();
  if (!storageError) notify(message);
}
document.addEventListener('click', (ev) => {
  const b = ev.target.closest('[data-view],[data-act],.brand');
  if (!b) return;
  if (b.matches('.brand')) {
    ev.preventDefault();
    changeView('Criadero');
    return;
  }
  if (b.dataset.view) {
    changeView(b.dataset.view);
    return;
  }
  const a = b.dataset.act,
    [cmd, ...args] = a.split('-'),
    key = args.join('-'),
    i = Number(key);
  if (cmd === 'debug') {
    if (window.debug !== true) return;
    if (broken) {
      notify('Revisa el guardado antes de usar las herramientas debug.');
      return;
    }
    if (key === 'complete-generation') {
      const generation = Number($('debugGeneration').value);
      if (
        !Number.isInteger(generation) ||
        generation < 1 ||
        generation > regions.length
      ) {
        notify('Selecciona una generación válida.');
        return;
      }
      let added = 0;
      for (const s of species)
        if (s.generation === generation && addDex(s.id)) added++;
      finishDebugAction(
        'Generación ' + generation + ' completada: +' + added + ' especies.',
      );
    }
    if (key === 'add-coins') {
      const amount = Math.min(10000, 1e10 - st.coins);
      if (!amount) {
        notify('Se alcanzó el límite de PokéCoins.');
        return;
      }
      st.coins += amount;
      finishDebugAction('+' + amount.toLocaleString('es-ES') + ' PokéCoins.');
    }
    if (key === 'add-rc') {
      const amount = Math.min(10000, 1e12 - st.rc);
      if (!amount) {
        notify('Se alcanzó el límite de RestauraChapas.');
        return;
      }
      const firstRestoration = st.trips === 0;
      st.trips = Math.max(1, st.trips);
      st.rc += amount;
      finishDebugAction(
        (firstRestoration ? 'Sistema RC habilitado. ' : '') +
          '+' +
          amount.toLocaleString('es-ES') +
          ' RestauraChapas.',
      );
    }
    if (key === 'toggle-shiny') {
      debugShinyChanceEnabled = !debugShinyChanceEnabled;
      renderCurrentView();
      notify(
        debugShinyChanceEnabled
          ? 'Probabilidad shiny fijada en 50 %.'
          : 'Probabilidad shiny normal restaurada.',
      );
    }
    return;
  }
  if (
    cmd === 'generation' &&
    hasBenefit('card-key') &&
    Number.isInteger(i) &&
    i >= 1 &&
    i <= 5
  ) {
    if (st.generations.includes(i) && st.generations.length === 1) {
      notify('Debe quedar una generación activa.');
      return;
    }
    st.generations = st.generations.includes(i)
      ? st.generations.filter((n) => n !== i)
      : [...st.generations, i];
    save();
    renderCurrentView();
  }
  if (cmd === 'tend') {
    slot = i;
    careForEgg(i, calculateTapPower(), true);
  }
  if (
    cmd === 'bay' &&
    i === st.eggs.length - 1 &&
    i < 8 &&
    spendCoins(prices[i])
  ) {
    st.eggs.push(createEgg(st.active));
    save();
    renderCurrentView();
    notify('Plaza ' + (i + 2) + ' desbloqueada');
  }
  if (cmd === 'evolve') evolve(i);
  if (cmd === 'evolveAll') evolveAll();
  if (a === 'cry-volume-0' || a === 'cry-volume-100')
    setCryVolume(a === 'cry-volume-0' ? 0 : 100);
  if (a === 'sprite' && detailId) {
    showShiny = !showShiny;
    info(detailId, false);
  }
  if (cmd === 'inc') changeIncubatorLevel(i);
  if (cmd === 'shop') {
    shopTab = key;
    renderCurrentView();
  }
  if (cmd === 'info') info(i);
  if (cmd === 'buy') buyShopItem(key);
  if (cmd === 'benefit') buyBenefit(key);
  if (
    cmd === 'group' &&
    groupIDs.includes(i) &&
    !st.groups.includes(i) &&
    spendCoins(groupCost[i])
  ) {
    st.groups.push(i);
    st.active.push(i);
    save();
    renderCurrentView();
    notify('Grupo ' + G[i] + ' desbloqueado');
  }
  if (cmd === 'toggle' && hasBenefit('oval-charm')) {
    if (st.active.includes(i) && st.active.length === 1) {
      notify('Debe quedar un grupo activo.');
      return;
    }
    st.active = st.active.includes(i)
      ? st.active.filter((g) => g !== i)
      : [...st.active, i];
    save();
    renderCurrentView();
  }
  if (a === 'next' || a === 'prev') {
    dexPage += a === 'next' ? 1 : -1;
    entries();
    window.scrollTo(0, 0);
  }
  if (a === 'export') {
    const url = URL.createObjectURL(
      new Blob([JSON.stringify(st, null, 2)], { type: 'application/json' }),
    );
    const link = document.createElement('a');
    link.href = url;
    link.download = 'PokeHatch-partida.json';
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
  if (a === 'import') $('file').click();
  if (a === 'reset')
    confirmAction(
      '¿Borrar la partida y volver al primer huevo? Puedes exportar una copia antes de continuar.',
      'reset',
    );
  if (a === 'confirm-prestige') {
    if (describeShopGate('gs-ball') || st.coins < 1000) return;
    const reward = st.dex.length + st.shiny.length,
      next = createFreshSave();
    Object.assign(next, {
      coins: hasBenefit('point-card') ? st.coins : 0,
      trips: st.trips + 1,
      rc: st.rc + reward,
      keyUnlocked: st.keyUnlocked,
      generations: [...st.generations],
      benefits: [...st.benefits],
      unlockedGroups: [...st.unlockedGroups],
      shiny: hasBenefit('journal') ? [...st.shiny] : [],
    });
    st = next;
    awardShinyItems(st);
    refreshTargets();
    slot = 0;
    shopTab = 'Beneficios Temporales';
    detailId = 0;
    $('births').replaceChildren();
    $('coinToasts').replaceChildren();
    $('detail').close();
    save();
    changeView('Criadero');
    notify('Has vuelto al pasado. +' + reward + ' RestauraChapas.');
    return;
  }
  if (
    cmd === 'confirm' &&
    (key === 'reset' || (key === 'import' && imported))
  ) {
    shopTab = 'Criadero';
    $('births').replaceChildren();
    $('coinToasts').replaceChildren();
    detailId = 0;
    st = key === 'reset' ? createFreshSave() : imported;
    imported = null;
    slot = 0;
    broken = false;
    save();
    $('detail').close();
    changeView('Criadero');
    notify(key === 'reset' ? 'Nueva partida iniciada.' : 'Partida importada.');
  }
});
$('file').onchange = async (e) => {
  const f = e.target.files[0];
  e.target.value = '';
  if (!f) return;
  try {
    if (f.size > 2e6) throw Error();
    const data = decodeSavedGame(JSON.parse(await f.text()));
    imported = data;
    confirmAction(
      '¿Sustituir tu partida por esta copia de ' +
        data.total +
        ' eclosiones y ' +
        data.dex.length +
        ' especies?',
      'import',
    );
  } catch {
    notify('Se necesita una partida válida de PokeHatch en formato v7 o v8.');
  }
};
$('close').onclick = () => $('detail').close();
function setCryVolume(value) {
  cryVolume = value;
  const slider = $('cryVolume');
  slider.value = String(value);
  slider.style.setProperty('--volume', value + '%');
  $('cryVolumeValue').textContent = value + ' %';
  if (cryGain) cryGain.gain.value = value / 100;
  try {
    localStorage.setItem(cryVolumeKey, String(value));
  } catch {}
}
document.addEventListener('input', (e) => {
  if (e.target.id === 'cryVolume') {
    setCryVolume(e.target.valueAsNumber);
  }
  if (e.target.id === 'search') {
    query = e.target.value;
    dexPage = 0;
    entries();
  }
});
document.addEventListener('change', (e) => {
  if (
    e.target.id === 'gen' ||
    e.target.id === 'only' ||
    e.target.id === 'ready' ||
    e.target.id === 'shinyFilter' ||
    e.target.id === 'dexGroup'
  ) {
    filterGroup = +$('dexGroup').value;
    gen = +$('gen').value;
    only = $('only').checked;
    readyOnly = $('ready').checked;
    shinyOnly = $('shinyFilter').checked;
    dexPage = 0;
    entries();
  }
});
