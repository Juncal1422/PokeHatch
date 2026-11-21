function fadingToast(rail, element, duration) {
  rail.append(element);
  const animation = element.animate(
    [{ opacity: 1 }, { opacity: 1, offset: 0.55 }, { opacity: 0 }],
    duration,
  );
  animation.onfinish = () => element.remove();
}
function earn(amount, label = '', kind = 'other') {
  if (!amount) return;
  st.coins += amount;
  const rail = $('coinToasts'),
    r = document.querySelector('.wallet').getBoundingClientRect();
  rail.style.right = Math.max(4, innerWidth - r.right) + 'px';
  rail.style.top = r.bottom + 4 + 'px';
  const toast = document.createElement('div');
  toast.innerHTML =
    '+' + amount + ' ' + moneyIcon() + (label ? ' · ' + label : '');
  toast.dataset.kind = kind;
  fadingToast(rail, toast, 1100);
}
function save() {
  if (broken) return;
  try {
    localStorage.setItem(KEY, JSON.stringify(st));
    storageError = false;
  } catch {
    storageError = true;
    notify(
      'El guardado está bloqueado. Exporta una copia desde Configuración.',
    );
  }
}
function notify(msg) {
  $('toast').textContent = msg;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => ($('toast').textContent = ''), 2600);
}
