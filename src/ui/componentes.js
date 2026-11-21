const navs = [
  ['Criadero', 'egg'],
  ['Pokédex', 'poke-ball'],
  ['Tienda', 'coin-case'],
  ['Mochila', 'explorer-kit'],
];
function moneyIcon(cap = false) {
  return cap
    ? '<img class="money-icon cap" data-item-icon="reset-urge" src="' +
        A['reset-urge'] +
        '" alt="RestauraChapas" title="RestauraChapas">'
    : '<i class="money-icon coin" role="img" aria-label="PokéCoins" title="PokéCoins"></i>';
}
function currencyText(html) {
  return html.replace(/>[^<>]*</g, (text) =>
    text
      .replace(/\bPC\b/g, () => moneyIcon())
      .replace(/\bRC\b/g, () => moneyIcon(true)),
  );
}
function updateWallet() {
  $('wallet').textContent = st.coins.toLocaleString('es-ES');
  $('capWallet').hidden = !st.trips;
  $('caps').textContent = st.rc.toLocaleString('es-ES');
}
function nav() {
  $('capIcon').innerHTML = moneyIcon(true);
  document.querySelector('nav').innerHTML = navs
    .map(
      ([v, k]) =>
        '<button data-view="' +
        v +
        '" class="' +
        (view === v ? 'sel' : '') +
        '">' +
        image(k) +
        ' ' +
        v +
        '</button>',
    )
    .join('');
  updateWallet();
}
function heading(t, sub, tag = '') {
  return (
    '<div class="headbar"><div><h1>' +
    t +
    '</h1><p>' +
    sub +
    '</p></div>' +
    (tag ? '<span class="counter">' + tag + '</span>' : '') +
    '</div>'
  );
}
function card(t, body, k = '', aside = '') {
  return (
    '<section class="card"><div class="cardhead"><h2>' +
    (k ? image(k) : '') +
    t +
    '</h2>' +
    aside +
    '</div><div class="pad">' +
    body +
    '</div></section>'
  );
}
function button(text, action, disabled = false) {
  return (
    '<button class="btn" data-act="' +
    action +
    '" ' +
    (disabled ? 'disabled' : '') +
    '>' +
    text +
    '</button>'
  );
}
