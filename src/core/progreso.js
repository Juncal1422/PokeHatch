const owned=k=>st.owned.includes(k);
const power=()=>(1+st.owned.reduce((n,k)=>n+(clickItems[k]||0),0))*(owned('toxic-orb')?2:1);
const rateBoost=()=>(1+st.owned.reduce((n,k)=>n+(rateItems[k]||0),0)/100)*(hasBenefit('magma-emblem')?2:1);
function required(s){
 const previous=Math.ceil(s.rule.n*.6);
 const base=previous>20?previous*.75:s.rule.n*.6;
 const threshold=n=>{
  const full=Math.ceil(n*6/5);
  return hasBenefit('exp-share')?Math.max(1,Math.min(full-1,Math.ceil(Math.ceil(n*.75)*6/5))):full;
 };
 const normal=threshold(Math.ceil(base));
 if(!owned('rare-candy'))return normal;
 return Math.max(1,Math.min(normal-1,threshold(Math.ceil(base*90/100))));
}
function payout(first,shiny,registeredShiny=false){
 const base=first?10:owned('nugget')?1+Number(owned('pearl'))+2*Number(owned('star-piece')):0;
 const bonus=(owned('amulet-coin')?.5:0)+(owned('stardust')?.5:0)+Number(owned('rare-bone'));return Math.trunc((base+(shiny?10:0))*(1+bonus))*(hasBenefit('blue-card')?2:1)*(!first&&registeredShiny&&owned('iris-ticket')?2:1)
}
