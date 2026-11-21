let imported=null;
document.addEventListener('click',ev=>{
 const b=ev.target.closest('[data-view],[data-act],.brand');if(!b)return;
 if(b.matches('.brand')){ev.preventDefault();go('Criadero');return}if(b.dataset.view){go(b.dataset.view);return}
 const a=b.dataset.act,[cmd,...args]=a.split('-'),key=args.join('-'),i=Number(key);
 if(cmd==='generation'&&hasBenefit('card-key')&&Number.isInteger(i)&&i>=1&&i<=5){
  if(st.generations.includes(i)&&st.generations.length===1){notify('Debe quedar una generación activa.');return}
  st.generations=st.generations.includes(i)?st.generations.filter(n=>n!==i):[...st.generations,i];
  save();draw()
 }
 if(cmd==='tend'){slot=i;care(i,power(),true)}
 if(cmd==='bay'&&i===st.eggs.length-1&&i<8&&spend(prices[i])){st.eggs.push(egg(st.active));save();draw();notify('Plaza '+(i+2)+' desbloqueada')}
 if(cmd==='evolve')evolve(i);
 if(cmd==='evolveAll')evolveAll();
 if(a==='cry-volume-0'||a==='cry-volume-100')setCryVolume(a==='cry-volume-0'?0:100);
 if(a==='sprite'&&detailId){showShiny=!showShiny;info(detailId,false)}
 if(cmd==='inc')incubate(i);
 if(cmd==='shop'){shopTab=key;draw()}
 if(cmd==='info')info(i);
 if(cmd==='buy')buy(key);
 if(cmd==='benefit')buyBenefit(key);
 if(cmd==='group'&&groupIDs.includes(i)&&!st.groups.includes(i)&&spend(groupCost[i])){st.groups.push(i);st.active.push(i);save();draw();notify('Grupo '+G[i]+' desbloqueado')}
 if(cmd==='toggle'&&hasBenefit('oval-charm')){if(st.active.includes(i)&&st.active.length===1){notify('Debe quedar un grupo activo.');return}st.active=st.active.includes(i)?st.active.filter(g=>g!==i):[...st.active,i];save();draw()}
 if(a==='next'||a==='prev'){dexPage+=a==='next'?1:-1;entries();window.scrollTo(0,0)}
 if(a==='export'){const url=URL.createObjectURL(new Blob([JSON.stringify(st,null,2)],{type:'application/json'}));const link=document.createElement('a');link.href=url;link.download='PokeHatch-partida.json';link.click();setTimeout(()=>URL.revokeObjectURL(url),1000)}
 if(a==='import')$('file').click();
 if(a==='reset')confirmAction('¿Borrar la partida y volver al primer huevo? Puedes exportar una copia antes de continuar.','reset');
 if(a==='confirm-prestige'){
  if(gateLabel('gs-ball')||st.coins<1000)return;
  const reward=st.dex.length,next=fresh();
  Object.assign(next,{coins:hasBenefit('point-card')?st.coins:0,trips:st.trips+1,rc:st.rc+reward,keyUnlocked:st.keyUnlocked,generations:[...st.generations],benefits:[...st.benefits],unlockedGroups:[...st.unlockedGroups],shiny:hasBenefit('journal')?[...st.shiny]:[]});
  st=next;awardShinyItems(st);refreshTargets();slot=0;shopTab='Beneficios Temporales';detailId=0;
  $('births').replaceChildren();$('coinToasts').replaceChildren();$('detail').close();save();go('Criadero');notify('Has vuelto al pasado. +'+reward+' RestauraChapas.');return;
 }
 if(cmd==='confirm'&&(key==='reset'||(key==='import'&&imported))){shopTab='Criadero';$('births').replaceChildren();$('coinToasts').replaceChildren();detailId=0;st=key==='reset'?fresh():imported;imported=null;slot=0;broken=false;save();$('detail').close();go('Criadero');notify(key==='reset'?'Nueva partida iniciada.':'Partida importada.')}
});
$('file').onchange=async e=>{const f=e.target.files[0];e.target.value='';if(!f)return;try{if(f.size>2e6)throw Error();const data=decode(JSON.parse(await f.text()));imported=data;confirmAction('¿Sustituir tu partida por esta copia de '+data.total+' eclosiones y '+data.dex.length+' especies?','import')}catch{notify('Se necesita una partida válida de PokeHatch en formato v7.')}};
$('close').onclick=()=>$('detail').close();
function setCryVolume(value){
 cryVolume=value;
 const slider=$('cryVolume');
 slider.value=String(value);
 slider.style.setProperty('--volume',value+'%');
 $('cryVolumeValue').textContent=value+' %';
 if(cryGain)cryGain.gain.value=value/100;
 try{localStorage.setItem(cryVolumeKey,String(value))}catch{}
}
document.addEventListener('input',e=>{
 if(e.target.id==='cryVolume'){
  setCryVolume(e.target.valueAsNumber);
 }
 if(e.target.id==='search'){query=e.target.value;dexPage=0;entries()}});
document.addEventListener('change',e=>{if(e.target.id==='gen'||e.target.id==='only'||e.target.id==='ready'||e.target.id==='shinyFilter'||e.target.id==='dexGroup'){filterGroup=+$('dexGroup').value;gen=+$('gen').value;only=$('only').checked;readyOnly=$('ready').checked;shinyOnly=$('shinyFilter').checked;dexPage=0;entries()}});
