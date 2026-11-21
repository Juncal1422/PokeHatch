const familyDrops=id=>dropByRoot[id]||[];
const children=id=>byParent[id]||[], KEY='PokeHatch.save.v8', LEGACY_KEY='PokeHatch.save.v7', SAVE_VERSION=8, legacyGroupIDs={1:12,2:7,3:1,4:3,5:6,6:2,7:5,8:8,9:10,10:11,11:9,12:4,14:13},pick=a=>a[Math.floor(Math.random()*a.length)];
// Crea una plaza nueva o recrea una después de una eclosión.
function createEgg(active,lv=0,a=st){const available=active.filter(g=>availableEggPool(g,a).length);const g=pick(available.length?available:active);return {g,lv,x:0,max:calculateEggTarget(g,a),charge:0}}
function createFreshSave(){const a={game:'PokeHatch',v:SAVE_VERSION,coins:0,rc:0,keyUnlocked:false,generations:[1,2,3,4,5],trips:0,benefits:[],unlockedGroups:[],total:0,shinyBirths:0,counts:{},dex:[],owned:[],shiny:[],since:{},groups:[1],active:[1],groupHatches:{},boosts:[],eggs:[]};a.eggs.push(createEgg(a.active,0,a));return a}
const nat=(x,max=1e9)=>Number.isSafeInteger(x)&&x>=0&&x<=max;
const saveFields=Object.keys(createFreshSave());
const eggFields=['g','lv','x','max','charge'];
function exactFields(value,fields){
 return value!==null&&typeof value==='object'&&!Array.isArray(value)&&
  Object.keys(value).length===fields.length&&fields.every(k=>Object.hasOwn(value,k));
}
function remapLegacyGroupID(id){
 if(!Number.isInteger(id)||!Object.hasOwn(legacyGroupIDs,id))throw Error('Grupo incompatible');
 return legacyGroupIDs[id];
}
function migrateLegacySave(a){
 if(a?.v!==7)return a;
 if(!exactFields(a,saveFields)||a.game!=='PokeHatch')throw Error('Formato incompatible');
 for(const field of ['groups','active','unlockedGroups','boosts']){
  if(!Array.isArray(a[field]))throw Error('Formato incompatible');
  a[field]=a[field].map(remapLegacyGroupID);
 }
 if(!a.groupHatches||typeof a.groupHatches!=='object'||Array.isArray(a.groupHatches))throw Error('Formato incompatible');
 a.groupHatches=Object.fromEntries(Object.entries(a.groupHatches).map(([id,count])=>{
  const group=Number(id);
  if(String(group)!==id)throw Error('Grupo incompatible');
  return [remapLegacyGroupID(group),count];
 }));
 if(!Array.isArray(a.eggs))throw Error('Formato incompatible');
 a.eggs=a.eggs.map(e=>{
  if(!e||typeof e!=='object'||Array.isArray(e))throw Error('Formato incompatible');
  return {...e,g:remapLegacyGroupID(e.g)};
 });
 a.v=SAVE_VERSION;
 return a;
}
function decodeSavedGame(a){
 a=migrateLegacySave(a);
 if(!exactFields(a,saveFields)||a.game!=='PokeHatch'||a.v!==SAVE_VERSION)throw Error('Formato incompatible');
 const array=(v,test)=>Array.isArray(v)&&v.every(test)&&new Set(v).size===v.length;
 if(!nat(a.rc,1e12)||(!a.trips&&a.rc)||typeof a.keyUnlocked!=='boolean'||!array(a.generations,n=>Number.isInteger(n)&&n>=1&&n<=5)||!a.generations.length||!array(a.unlockedGroups,g=>groupIDs.includes(g))||!nat(a.trips)||!array(a.benefits,k=>Object.hasOwn(benefits,k))||(!a.trips&&a.benefits.length)||a.benefits.includes('parcel')||(a.benefits.includes('card-key')?!a.keyUnlocked:a.generations.length!==5)||!nat(a.shinyBirths,a.total)||!nat(a.total)||!nat(a.coins,1e10)||!array(a.dex,id=>Number.isInteger(id)&&!!S[id])||!array(a.owned,k=>typeof k==='string'&&((Object.hasOwn(goods,k)&&k!=='gs-ball')||drops.includes(k)||Object.hasOwn(loot,k)||Object.hasOwn(rewards,k)))||!array(a.groups,g=>groupIDs.includes(g))||!array(a.active,g=>a.groups.includes(g))||!a.active.length||!a.counts||typeof a.counts!=='object'||Array.isArray(a.counts)||!Object.entries(a.counts).every(([id,n])=>S[id]?.hatchableEggGroups.length&&String(+id)===id&&a.dex.includes(+id)&&nat(n)&&n>0)||Object.values(a.counts).reduce((t,n)=>t+n,0)!==a.total)throw Error();
 if(!Array.isArray(a.eggs)||!a.eggs.length||a.eggs.length>9||!a.eggs.every(e=>exactFields(e,eggFields)&&a.groups.includes(e.g)&&nat(e.lv,5)&&Number.isFinite(e.max)&&e.max>0&&Number.isFinite(e.x)&&e.x>=0&&e.x<e.max))throw Error();

 if(!array(a.boosts,g=>groupIDs.includes(g)&&a.owned.includes(boostItems[g]))||!a.groupHatches||typeof a.groupHatches!=='object'||Array.isArray(a.groupHatches)||!Object.entries(a.groupHatches).every(([g,n])=>String(+g)===g&&groupIDs.includes(+g)&&nat(n))||Object.values(a.groupHatches).reduce((x,y)=>x+y,0)>a.total)throw Error();
 if(!a.groups.includes(1)||!array(a.shiny,i=>Number.isInteger(i)&&!!S[i]&&(a.dex.includes(i)||a.benefits.includes('journal')))||!a.since||typeof a.since!=='object'||Array.isArray(a.since)||Object.keys(a.since).length!==a.dex.length||!a.dex.every(id=>nat(a.since[id],a.counts[S[id].familyRootId]||0))||!a.eggs.every(e=>e.max===calculateEggTarget(e.g,a)&&Number.isFinite(e.charge)&&e.charge>=0&&e.charge<1))throw Error();
 return a;
}
let st=createFreshSave(),broken=false,storageError=false,migratedSave=false;
try{const raw=localStorage.getItem(KEY)||localStorage.getItem(LEGACY_KEY);if(raw){const saved=JSON.parse(raw);migratedSave=saved?.v===7;st=decodeSavedGame(saved)}}catch{broken=true}
let view='Criadero',slot=0,shopTab='Criadero',dexPage=0,query='',gen=0,filterGroup=0,only=false,readyOnly=false,shinyOnly=false,detailId=0,showShiny=false,toastTimer;
