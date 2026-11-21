let bagSignature='';
function draw(){bagSignature='';nav();$('main').innerHTML=currencyText(({Criadero:farm,'Pokédex':dex,Tienda:storeView,Mochila:inventory,Ajustes:settings})[view]());if(view==='Criadero'){farmStatus()}if(view==='Tienda')updateShop();if(view==='Pokédex'){$('search').value=query;entries()}}
let livePending=false,frameQueued=false;
const pressedPointers=new Set();
function queueLiveUpdate(){
 livePending=true;
 if(frameQueued||pressedPointers.size)return;
 frameQueued=true;
 requestAnimationFrame(()=>{
  frameQueued=false;
  if(!livePending||pressedPointers.size)return;
  livePending=false;updateLive()
 })
}
document.addEventListener('pointerdown',e=>pressedPointers.add(e.pointerId),true);
function releasePointer(e){pressedPointers.delete(e.pointerId);if(livePending)queueLiveUpdate()}
window.addEventListener('pointerup',releasePointer,true);
window.addEventListener('pointercancel',releasePointer,true);
window.addEventListener('blur',()=>{pressedPointers.clear();if(livePending)queueLiveUpdate()});
function updateLive(){
 updateWallet();
 if(view==='Criadero'){farmStatus();if(hasBenefit('scope-lens'))$('periscope').innerHTML=periscope()}
 if(view==='Pokédex'){document.querySelector('.counter').textContent=st.dex.length+' / '+species.length;entries();if(detailId&&$('detail').open)refreshDetail(detailId)}
 if(view==='Mochila'){
  const signature=[st.owned.length,st.benefits.length,st.dex.length].join('/');
  if(bagSignature!==signature){$('main').innerHTML=currencyText(inventory());bagSignature=signature}
 }
 if(view==='Tienda'){
  updateShop();
  if(shopTab==='Criadero'&&hasBenefit('scope-lens')){
   const registered=new Set(st.dex);
   for(const el of document.querySelectorAll('[data-shop-group]')){
    const progress=groupProgress(+el.dataset.shopGroup,registered);
    el.className='entry group-progress '+progress.state;
    el.querySelector('p').textContent=progress.label;
   }
  }
 }
}
$('detail').addEventListener('close',()=>{detailId=0;document.body.append($('births'),$('coinToasts'))});
function go(v){view=v;draw();window.scrollTo(0,0)}
function confirmAction(text,action){modal('<p>'+text+'</p>'+button('Confirmar','confirm-'+action))}
