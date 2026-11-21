function hatch(i,manual=false){
 const e=st.eggs[i],oldGroup=e.g,s=rollSpecies(e.g),shiny=Math.random()<shinyChance(e.g);
 st.counts[s.id]=(st.counts[s.id]||0)+1;st.total++;st.groupHatches[e.g]=(st.groupHatches[e.g]||0)+1;const first=addDex(s.id),registeredShiny=st.shiny.includes(s.id);
 const ticket=!first&&registeredShiny&&owned('iris-ticket');
 earn(payout(first,shiny,registeredShiny),'',ticket?'ticket':shiny?'shiny':first?'new':'repeat');if(shiny){st.shinyBirths++;inheritShiny(s.id)}
 const possible=familyDrops(s.id).filter(k=>!owned(k));
 const items=[];
 if(s.id===52&&!owned('amulet-coin')&&Math.random()<.05*dropBonus())items.push('amulet-coin');
 if(possible.length&&Math.random()<.05*dropBonus())items.push(pick(possible));
 for(const [k,eligible,chance] of [['ice-stone',iceHatchlings.has(s.id),.025],['rare-bone',s.id===104,.01],['air-balloon',oldGroup===4,.001],['light-ball',s.id===172,.001]]){
  if(eligible&&!owned(k)&&Math.random()<chance*dropBonus())items.push(k);
 }
 st.owned.push(...items);
 if(items.includes('deep-sea-scale')&&!st.boosts.includes(9))st.boosts.push(9);
 if(items.includes('air-balloon')||items.includes('deep-sea-scale'))refreshTargets();
 const real=rollReal(s.id);if(real){if(!owned(real[0]))st.owned.push(real[0]);earn(real[1],names[real[0]],'real');items.push(real[0])}
 st.eggs[i]=egg(st.active,e.lv);if(manual)save();birthToast(s,shiny,items);playCry(s.id);

 if(view==='Criadero')reveal(i,oldGroup,s.id,shiny);
 queueLiveUpdate();
}
function care(i,amount,manual=false){
 const e=st.eggs[i];if(!e||!eligiblePool(e.g,st).length)return;
 e.x+=amount;
 if(e.x>=e.max){hatch(i,manual);return}
 if(view==='Criadero'){
  const button=$('egg-'+i);
  if(button){button.classList.remove('nudge');void button.offsetWidth;button.classList.add('nudge')}
 }
 if(manual){save();farmStatus()}
}
const lvlCost=e=>[25,50,100,250,500][e.lv];
const rates=[0,.25,.5,1,1.5,2];
function tick(i,dt){const e=st.eggs[i];if(!e.lv||!eligiblePool(e.g,st).length)return false;let credit=e.charge+dt*rates[e.lv]*rateBoost();const steps=Math.floor(credit);e.charge=credit-steps;
 for(let j=0;j<steps;j++)care(i,(owned('flame-orb')?2:1)*(hasBenefit('magma-stone')?2:1));
 st.eggs[i].charge=credit-steps;return steps>0;
}
function spend(base,key){const price=shopPrice(base,key);if(st.coins<price)return false;st.coins-=price;return true}
function incubate(i){const e=st.eggs[i];if(!e||e.lv===5||!spend(lvlCost(e)))return;e.lv++;save();draw()}
