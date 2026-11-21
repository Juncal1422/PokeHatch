const spriteSlugs={'iris-ticket':'rainbow-pass','gs-ball':'park-ball','rare-incense':'odd-incense','badge-case':'medal-box',talisman:'old-charm',case:'prop-case','odd-stone':'intriguing-stone'};

const spriteCache='PokeHatch.itemSprites.v2';
const remoteItems=[...potions,'devon-parts',...Object.keys(rewards),...Object.keys(benefits),'toxic-orb','lustrous-orb','reset-urge','gs-ball'].filter(k=>!A[k]);
const spriteURLs={};
for(const k of remoteItems){
 const slug=spriteSlugs[k]||k;
 spriteURLs[k]='https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/'+slug+'.png';
 A[k]=spriteURLs[k];
}
try{
 const saved=JSON.parse(localStorage.getItem(spriteCache)||'{}');
 for(const k of remoteItems)if(typeof saved[k]==='string'&&/^data:image\/png;base64,[A-Za-z0-9+/=]+$/.test(saved[k]))A[k]=saved[k];
}catch{}
async function loadItemSprites(){
 const entries={};
 async function load(k){
  if(A[k]!==spriteURLs[k]){entries[k]=A[k];return}
  try{
   let response=await fetch(spriteURLs[k]);
   if(!response.ok){
    const data=await fetch('https://pokeapi.co/api/v2/item/'+(spriteSlugs[k]||k)+'/');
    const url=(await data.json()).sprites?.default;
    if(!url)return;
    response=await fetch(url);
   }
   if(!response.ok)return;
   const bitmap=await createImageBitmap(await response.blob());
   const canvas=document.createElement('canvas'),scale=Math.min(1,64/Math.max(bitmap.width,bitmap.height));
   canvas.width=Math.max(1,Math.round(bitmap.width*scale));canvas.height=Math.max(1,Math.round(bitmap.height*scale));
   canvas.getContext('2d').drawImage(bitmap,0,0,canvas.width,canvas.height);bitmap.close();
   A[k]=entries[k]=canvas.toDataURL('image/png');
   for(const img of document.querySelectorAll('[data-item-icon="'+k+'"]'))img.src=A[k];
  }catch{}
 }
 for(let i=0;i<remoteItems.length;i+=5)await Promise.all(remoteItems.slice(i,i+5).map(load));
 try{localStorage.setItem(spriteCache,JSON.stringify(entries))}catch{}
}
const itemArt=k=>'<img src="'+A[k]+'" data-item-icon="'+k+'" alt="">';
