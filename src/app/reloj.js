let clock = performance.now(),
  lastSave = clock;
document.addEventListener('visibilitychange', () => {
  clock = performance.now();
  if (document.hidden) save();
});
window.addEventListener('pagehide', save);
setInterval(() => {
  const t = performance.now(),
    dt = Math.min(2, (t - clock) / 1000);
  clock = t;
  if (document.hidden || !st.eggs.some((e) => e.lv)) return;
  let advanced = false;
  for (let i = 0; i < st.eggs.length; i++)
    if (advanceIncubator(i, dt)) advanced = true;
  if (advanced && view === 'Criadero' && !livePending) farmStatus();
  if (t - lastSave > 2000) {
    save();
    lastSave = t;
  }
}, 80);
$('logo').src = A.egg;
renderCurrentView();
preloadAllSprites();
if (migratedSave) save();
if (broken) {
  changeView('Ajustes');
  notify('Revisa tu guardado antes de continuar.');
}
