const cryVolumeKey='PokeHatch.cryVolume';
let cryVolume=35,cryContext,cryGain;
const cryBuffers=new Map();
try{
 const value=localStorage.getItem(cryVolumeKey);
 if(value!==null&&Number.isFinite(Number(value)))cryVolume=Math.max(0,Math.min(100,Number(value)));
}catch{}
document.addEventListener('click',()=>{
 if(!cryVolume||typeof AudioContext==='undefined')return;
 if(!cryContext){
  cryContext=new AudioContext();
  cryGain=cryContext.createGain();
  cryGain.gain.value=cryVolume/100;
  cryGain.connect(cryContext.destination);
 }
 if(cryContext.state!=='running')cryContext.resume().catch(()=>{});
},true);
function cryBuffer(id){
 if(!cryBuffers.has(id)){
  const load=async()=>{
   for(const variant of ['latest','legacy']){
    try{
     const response=await fetch('https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/'+variant+'/'+id+'.ogg');
     if(response.ok)return await cryContext.decodeAudioData(await response.arrayBuffer());
    }catch{}
   }
   cryBuffers.delete(id);
   return null;
  };
  cryBuffers.set(id,load());
 }
 return cryBuffers.get(id);
}
async function playCry(id){
 if(!cryVolume||!cryContext)return;
 try{
  const buffer=await cryBuffer(id);
  if(!buffer||!cryVolume||cryContext.state!=='running')return;
  const source=cryContext.createBufferSource();
  source.buffer=buffer;
  source.connect(cryGain);
  source.onended=()=>source.disconnect();
  source.start();
 }catch{}
}
