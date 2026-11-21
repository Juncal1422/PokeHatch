const hasBenefit=k=>st.benefits.includes(k);
const complete=list=>list.every(s=>st.dex.includes(s.id));
function awardMilestones(a){
 const registered=new Set(a.dex),evolutions=a.dex.reduce((n,id)=>n+Number(!!S[id].p),0);
 for(const g of groupIDs)if(!a.unlockedGroups.includes(g)&&(a.benefits.includes(groupBenefits[g])||scopeLists[g].every(s=>registered.has(s.id))))a.unlockedGroups.push(g);
 const give=k=>{if(!a.owned.includes(k))a.owned.push(k)};
 regions.forEach((_,i)=>{
  if(evolutions>=64*(i+1))give(snackKeys[i]);
  if(regionLists[i].every(s=>registered.has(s.id))){a.keyUnlocked=true;give(ballKeys[i])}
 });
 if(registered.size===species.length&&!a.owned.includes('master-ball')){give('master-ball');refreshTargets(a)}
}
function benefitGate(k){
 if(k==='card-key'&&!st.keyUnlocked)return 'Completa la Pokédex de una generación';
 const g=benefits[k][3];
 return g&&!st.unlockedGroups.includes(g)?'Completa la Pokédex del grupo '+G[g]:'';
}
function buyBenefit(k){
 if(!st.trips||!Object.hasOwn(benefits,k)||hasBenefit(k)||benefitGate(k)||st.rc<benefits[k][1])return;
 st.rc-=benefits[k][1];
 if(k==='parcel')earn(5);
 else {st.benefits.push(k);refreshTargets()}save();draw();notify(benefits[k][0]+' conseguido');
}
const loot={
 'amulet-coin':['Moneda Amuleto','Aumenta un 50 % las PC por eclosión. Meowth puede traerla al eclosionar.'],
 'rare-bone':['Hueso Raro','Aumenta un 100 % las PC por eclosión.'],
 'air-balloon':['Globo Helio','Facilita la eclosión de huevos Volador un 100 %.'],
 'light-ball':['Bola Luminosa','Aumenta un 50 % el ritmo de todas las incubadoras.']
};
for(const [k,[name]] of Object.entries(loot))names[k]=name;
for(const [metal,value] of [['copper',500],['silver',1000],['gold',2000]])rewards['relic-'+metal]='Concede '+value+' PC cada vez que lo encuentras.';
Object.assign(names,{'relic-copper':'Real de Cobre','relic-silver':'Real de Plata','relic-gold':'Real de Oro'});
rateItems['light-ball']=50;
const iceHatchlings=new Set([131,215,220,225,238,361,363,459,582,613,615]);
const fossils=new Set([138,140,345,347,408,410,564,566]);
const dropBonus=()=>hasBenefit('luck-incense')?2:1;
function rollReal(id){
 if(!fossils.has(id))return null;
 const roll=Math.random()/(dropBonus()*(hasBenefit('talisman')?2:1));
 if(roll<.0001)return ['relic-gold',2000];
 if(roll<.0006)return ['relic-silver',1000];
 if(roll<.0016)return ['relic-copper',500];
 return null
}
function purchaseCount(){return st.eggs.length-1+st.owned.filter(k=>goods[k]&&!drops.includes(k)).length}
function shopPrice(n,key){
 if(key==='gs-ball')return n;
 const price=owned('discount-coupon')?Math.floor(n*9/10):n;
 return evoUses[key]&&hasBenefit('odd-stone')?Math.floor(price/2):price
}
function refreshTargets(a=st){for(const e of a.eggs){const next=target(e.g,a);if(next!==e.max){e.x=e.x/e.max*next;e.max=next}}}
