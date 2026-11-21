const familyDrops=id=>dropByRoot[id]||[];
const children=id=>byParent[id]||[], KEY='PokeHatch.save.v7',pick=a=>a[Math.floor(Math.random()*a.length)];
function egg(active,lv=0,a=st){const available=active.filter(g=>eligiblePool(g,a).length);const g=pick(available.length?available:active);return {g,lv,x:0,max:target(g,a),charge:0}}
function fresh(){const a={game:'PokeHatch',v:7,coins:0,rc:0,keyUnlocked:false,generations:[1,2,3,4,5],trips:0,benefits:[],unlockedGroups:[],total:0,shinyBirths:0,counts:{},dex:[],owned:[],shiny:[],since:{},groups:[3],active:[3],groupHatches:{},boosts:[],eggs:[]};a.eggs.push(egg(a.active,0,a));return a}
const nat=(x,max=1e9)=>Number.isSafeInteger(x)&&x>=0&&x<=max;
const saveFields=Object.keys(fresh());
const eggFields=['g','lv','x','max','charge'];
function exactFields(value,fields){
 return value!==null&&typeof value==='object'&&!Array.isArray(value)&&
  Object.keys(value).length===fields.length&&fields.every(k=>Object.hasOwn(value,k));
}
function decode(a){
 if(!exactFields(a,saveFields)||a.game!=='PokeHatch'||a.v!==7)throw Error('Formato incompatible');
 const array=(v,test)=>Array.isArray(v)&&v.every(test)&&new Set(v).size===v.length;
 if(!nat(a.rc,1e12)||(!a.trips&&a.rc)||typeof a.keyUnlocked!=='boolean'||!array(a.generations,n=>Number.isInteger(n)&&n>=1&&n<=5)||!a.generations.length||!array(a.unlockedGroups,g=>groupIDs.includes(g))||!nat(a.trips)||!array(a.benefits,k=>Object.hasOwn(benefits,k))||(!a.trips&&a.benefits.length)||a.benefits.includes('parcel')||(a.benefits.includes('card-key')?!a.keyUnlocked:a.generations.length!==5)||!nat(a.shinyBirths,a.total)||!nat(a.total)||!nat(a.coins,1e10)||!array(a.dex,id=>Number.isInteger(id)&&!!S[id])||!array(a.owned,k=>typeof k==='string'&&((Object.hasOwn(goods,k)&&k!=='gs-ball')||drops.includes(k)||Object.hasOwn(loot,k)||Object.hasOwn(rewards,k)))||!array(a.groups,g=>groupIDs.includes(g))||!array(a.active,g=>a.groups.includes(g))||!a.active.length||!a.counts||typeof a.counts!=='object'||Array.isArray(a.counts)||!Object.entries(a.counts).every(([id,n])=>S[id]?.pool.length&&String(+id)===id&&a.dex.includes(+id)&&nat(n)&&n>0)||Object.values(a.counts).reduce((t,n)=>t+n,0)!==a.total)throw Error();
 if(!Array.isArray(a.eggs)||!a.eggs.length||a.eggs.length>9||!a.eggs.every(e=>exactFields(e,eggFields)&&a.groups.includes(e.g)&&nat(e.lv,5)&&Number.isFinite(e.max)&&e.max>0&&Number.isFinite(e.x)&&e.x>=0&&e.x<e.max))throw Error();

 if(!array(a.boosts,g=>groupIDs.includes(g)&&a.owned.includes(boostItems[g]))||!a.groupHatches||typeof a.groupHatches!=='object'||Array.isArray(a.groupHatches)||!Object.entries(a.groupHatches).every(([g,n])=>String(+g)===g&&groupIDs.includes(+g)&&nat(n))||Object.values(a.groupHatches).reduce((x,y)=>x+y,0)>a.total)throw Error();
 if(!a.groups.includes(3)||!array(a.shiny,i=>Number.isInteger(i)&&!!S[i]&&(a.dex.includes(i)||a.benefits.includes('journal')))||!a.since||typeof a.since!=='object'||Array.isArray(a.since)||Object.keys(a.since).length!==a.dex.length||!a.dex.every(id=>nat(a.since[id],a.counts[S[id].root]||0))||!a.eggs.every(e=>e.max===target(e.g,a)&&Number.isFinite(e.charge)&&e.charge>=0&&e.charge<1))throw Error();
 return a;
}
let st=fresh(),broken=false,storageError=false;
try{const raw=localStorage.getItem(KEY);if(raw)st=decode(JSON.parse(raw))}catch{broken=true}
let view='Criadero',slot=0,shopTab='Criadero',dexPage=0,query='',gen=0,filterGroup=0,only=false,readyOnly=false,shinyOnly=false,detailId=0,showShiny=false,toastTimer;
