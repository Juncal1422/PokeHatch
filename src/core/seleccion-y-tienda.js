const evoUses={},byParent={},dropByRoot={};
for(const s of species){
 if(!s.p)continue;
 if(S[s.p].p)s.rule.extra=true;
 (byParent[s.p]??=[]).push(s);
 const k=s.rule.item;if(!k)continue;
 (evoUses[k]??=[]).push(s);
 if(k!=='ice-stone'&&drops.includes(k)){const keys=dropByRoot[s.root]??=[];if(!keys.includes(k))keys.push(k)}
}
function evoText(k){const parents=[...new Set(evoUses[k].map(s=>S[s.p].n))];return (k==='soothe-bell'?'Para evolucionar a pokémon por amistad. ':'Para evolucionar a: '+parents.join(', ')+'.')}
for(const k of Object.keys(evoUses))if(!drops.includes(k))goods[k]=[500,evoText(k)];
const growingPools=Object.fromEntries(groupIDs.map(g=>[g,pools[g].filter(s=>byParent[s.id])]));
const families={};for(const s of species)if(s.p)(families[s.root]??=[]).push(s.id);
function eligiblePool(g,a){
 return a.generations.length===5?pools[g]:pools[g].filter(s=>a.generations.includes(s.gen));
}
function rollSpecies(g){
 const pool=eligiblePool(g,st),growing=pool===pools[g]?growingPools[g]:pool.filter(s=>byParent[s.id]);
 const reduction=st.owned.reduce((sum,k)=>sum+(choiceItems[k]||0),0);
 const singleFactor=(1-reduction/100)*(owned('pure-incense')?.5:1);
 const regular=()=>{const s=pick(pool);return !byParent[s.id]&&growing.length&&Math.random()>=singleFactor?pick(growing):s};
 const bonus=ballKeys.filter(owned).length*.2;
 let chosen=regular();
 if(bonus){
  const registered=new Set(st.dex);
  const pending=s=>!registered.has(s.id)||(families[s.id]||[]).some(id=>!registered.has(id));
  if(!pending(chosen)){
   const candidates=pool.filter(pending);
   const extra=growing.length?(pool.length-growing.length)*(1-singleFactor)/growing.length:0;
   const weight=s=>byParent[s.id]?1+extra:growing.length?singleFactor:1;
   const sum=candidates.reduce((n,s)=>n+weight(s),0);
   if(sum&&Math.random()<bonus*sum/(pool.length-sum)){
    let r=Math.random()*sum;
    chosen=candidates.find(s=>(r-=weight(s))<0)||candidates[candidates.length-1];
   }
  }
 }
 if(hasBenefit('badge-case')&&!byParent[chosen.id]&&growing.length&&Math.random()<.5)chosen=pick(growing);
 const potionChance=potions.filter(owned).length/10;
 if(potionChance&&Math.random()<potionChance){
  const registered=new Set(st.dex);
  const incomplete=s=>!registered.has(s.id)||(families[s.id]||[]).some(id=>!registered.has(id));
  if(!incomplete(chosen)){
   const candidates=pool.filter(incomplete);
   if(candidates.length)chosen=pick(candidates);
  }
 }
 return chosen
}
function target(g,a){
 let speed=1;for(const k of a.owned){if(scentRules[k]?.[1].includes(g))speed++;else if(k==='lax-incense')speed+=.25;else if(k==='air-balloon'&&g===4)speed++}
 const slow=a.owned.includes('full-incense')&&[3,6,4].includes(g)?3:1;
 return stepCost[g]*(a.boosts.includes(g)?.75:1)*slow/speed*(a.owned.includes('master-ball')?.5:1)*(a.benefits.includes('life-orb')?.75:1)*(a.benefits.includes(groupBenefits[g])?.5:1)
}
function acquired(k){const g=gates[k]?.g;return g?st.boosts.includes(g):owned(k)}
function gateLabel(k,evolutions=st.dex.reduce((n,id)=>n+Number(!!S[id].p),0)){const r=gates[k];if(!r)return '';if(r.id&&!st.dex.includes(r.id))return 'Registra a '+S[r.id].n;
 if(r.requires&&!owned(r.requires))return 'Compra '+names[r.requires];
 if(r.badges&&evolutions<64*r.badges)return 'Completa las 8 medallas de '+regions[r.badges-1];
 if(r.region&&!complete(regionLists[r.region-1]))return 'Completa la Pokédex de '+regions[r.region-1];
 if(r.full&&st.dex.length<species.length)return 'Completa la Pokédex';
 const threshold=hasBenefit('rare-incense')?13:25;
 if(r.baby&&(st.counts[r.baby]||0)<threshold)return S[r.baby].n+' · '+(st.counts[r.baby]||0)+' / '+threshold+' eclosiones';
 if(r.purchases&&purchaseCount()<r.purchases)return 'Objetos comprados · '+purchaseCount()+' / '+r.purchases;
 if(r.g&&(st.groupHatches[r.g]||0)<40)return G[r.g]+' · '+(st.groupHatches[r.g]||0)+' / 40 eclosiones';
 if(r.shiny&&st.shinyBirths<r.shiny)return 'Shinies eclosionados · '+st.shinyBirths+' / '+r.shiny;
 if(r.total&&st.total<r.total)return 'Eclosiones · '+st.total+' / '+r.total;
 if(r.evol&&evolutions<r.evol)return 'Evoluciona '+r.evol+' Pokémon';return ''}
function buy(k){
 if(drops.includes(k)||!goods[k]||acquired(k)||gateLabel(k))return;
 if(k==='gs-ball'){
  if(st.coins>=1000)confirmAction('¿Seguro que quieres volver al pasado?','prestige');
  return;
 }
 if(!spend(goods[k][0],k))return;
 if(!owned(k))st.owned.push(k);const g=gates[k]?.g;
 if(g)st.boosts.push(g);refreshTargets();
 save();draw();notify(names[k]+' conseguido')
}
