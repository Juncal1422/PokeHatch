function groupSwitch(g){const on=st.active.includes(g);return '<button role="switch" aria-checked="'+on+'" aria-label="Grupo '+G[g]+'" class="switch" data-act="toggle-'+g+'" '+(hasBenefit('oval-charm')?'':'disabled title="Requiere Amuleto Oval"')+'><i></i>'+(on?'Activo':'Inactivo')+'</button>'}
function purchaseButton(action){return '<button class="btn purchase" data-act="'+action+'"></button>'}
function updateShop(){
 const evos=st.dex.reduce((n,id)=>n+Number(!!S[id].p),0);
 for(const b of document.querySelectorAll('.purchase')){
  const [kind,...parts]=b.dataset.act.split('-'),key=parts.join('-'),i=Number(key);
  let price,gate='',done='';
  if(kind==='buy'){price=goods[key][0];gate=gateLabel(key,evos);if(acquired(key))done='Adquirido'}
  if(kind==='bay'){price=prices[i];if(i+1<st.eggs.length)done='Adquirida';else if(i+1>st.eggs.length)gate='Compra primero la plaza '+(i+1)}
  if(kind==='group')price=groupCost[i];
  if(kind==='inc'){const e=st.eggs[i];price=lvlCost(e);if(e.lv===5)done='Nivel máximo'}
  if(kind==='benefit'){price=benefits[key][1];gate=benefitGate(key);if(hasBenefit(key))done='Adquirido'}
  else if(!done)price=shopPrice(price,kind==='buy'?key:null);
  const state=done?'done':gate?'locked':(kind==='benefit'?st.rc:st.coins)<price?'poor':'available';
  b.dataset.state=state;b.disabled=state!=='available';
  const label=done||(gate?'Bloqueado':price+' '+moneyIcon(kind==='benefit'));
  if(b.dataset.label!==label){b.innerHTML=label;b.dataset.label=label}
  const note=b.nextElementSibling;if(note?.classList.contains('gate'))note.textContent=done?'':gate;
 }
}
const itemSections=['Económicos','Cuidador','Crianza','Huevos','Optimización','Evolutivos'];
function sectionOf(k){
 if(snackKeys.includes(k)||k==='devon-parts'||k==='iris-ticket')return 'Económicos';
 if(potions.includes(k)||Object.hasOwn(scarves,k)||k==='shiny-charm'||k==='lustrous-orb'||k==='rare-candy'||choiceItems[k]||k==='pure-incense')return 'Optimización';
 if(k==='toxic-orb')return 'Cuidador';
 if(k==='flame-orb')return 'Crianza';
 if(evoUses[k])return 'Evolutivos';
 if(clickItems[k])return 'Cuidador';
 if(rateItems[k])return 'Crianza';
 if(['amulet-coin','shiny-charm','nugget','pearl','star-piece','discount-coupon','stardust','rare-bone'].includes(k))return 'Económicos';
 return 'Huevos'
}
function categoryOrder(a,b){
 const last=k=>k==='toxic-orb'||k==='flame-orb';
 if(last(a)!==last(b))return last(a)?1:-1;
 const order=['nugget','pearl','star-piece','amulet-coin','stardust','rare-bone',...snackKeys,'devon-parts','discount-coupon',...potions,'rare-candy','choice-scarf','choice-specs','choice-band','pure-incense','shiny-charm','lustrous-orb',...ballKeys,'master-ball','gs-ball'];
 const rank=k=>order.includes(k)?order.indexOf(k):order.length;
 if(rank(a)!==rank(b))return rank(a)-rank(b);
 return (evoUses[a]?.length||0)-(evoUses[b]?.length||0)||Number(a.startsWith('power-'))-Number(b.startsWith('power-'))
}
function storeView(){
 const tabs=['Criadero','Mejoras','Objetos Evolutivos','Incubadoras'];
 if(st.trips)tabs.push('Beneficios Temporales');
 const grid=(cards)=>'<div class="catalog">'+cards.join('')+'</div>';
 const tile=(key,name,desc,action,cl='',group=0)=>'<div class="entry '+cl+'"'+(group?' data-shop-group="'+group+'"':'')+'>'+itemArt(key)+'<h3>'+name+'</h3><p>'+desc.replace(/\.$/,'')+'</p>'+action+'</div>';
 const item=k=>tile(k,names[k],goods[k][1],purchaseButton('buy-'+k)+'<small class="gate"></small>');
 const section=(title,cards)=>'<h2 class="spacer">'+title+'</h2>'+grid(cards);
 let body;
 if(shopTab==='Criadero'){
  const registered=new Set(st.dex);
  body=section('Grupos de huevo',groupIDs.map(g=>{
   const progress=hasBenefit('scope-lens')?groupProgress(g,registered):null;
   return tile('egg'+g,G[g],progress?progress.label:pools[g].length+' crías',st.groups.includes(g)?groupSwitch(g):purchaseButton('group-'+g),progress?'group-progress '+progress.state:'',g);
  }))+
  section('Plazas adicionales',flutes.map((k,i)=>tile(k,'Plaza '+(i+2),fluteNames[i],purchaseButton('bay-'+i)+'<small class="gate"></small>')));
  if(!gateLabel('gs-ball'))body+=section('Viaje al pasado',[item('gs-ball')]);
 }else if(shopTab==='Incubadoras'){
  body=grid(st.eggs.map((e,i)=>tile('egg'+e.g,'Plaza '+(i+1),e.lv?'Nivel '+e.lv+' / 5':'Sin incubadora',purchaseButton('inc-'+i))));
 }else if(shopTab==='Beneficios Temporales'&&st.trips){
  const temporal=k=>{const [name,,desc]=benefits[k];return tile(k,name,desc,purchaseButton('benefit-'+k)+'<small class="gate"></small>')};
  const keys=Object.keys(benefits).sort((a,b)=>benefits[a][1]-benefits[b][1]);
  body=grid(keys.filter(k=>!benefits[k][3]).map(temporal))+section('Huevos',keys.filter(k=>benefits[k][3]).map(temporal));
 }else{
  const keys=Object.keys(goods).filter(k=>k!=='gs-ball'&&!drops.includes(k)).sort(categoryOrder);
  body=itemSections.filter(title=>(shopTab==='Objetos Evolutivos')===(title==='Evolutivos')).map(title=>section(title,keys.filter(k=>sectionOf(k)===title).map(item))).join('');
 }
 return heading('Tienda','Mejoras y equipo para tu granja.')+'<div class="tools">'+tabs.map(t=>button(t+(t===shopTab?' ✓':''),'shop-'+t)).join('')+'</div>'+body;
}
