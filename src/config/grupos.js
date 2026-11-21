'use strict';



const DB=JSON.parse(document.getElementById('db').textContent),species=DB.species,S=Object.fromEntries(species.map(s=>[s.id,s])),G=DB.groups,A=DB.art;
const $=id=>document.getElementById(id),image=(k,cl='',alt='')=>'<img src="'+A[k]+'" class="'+cl+'" alt="'+alt+'">',rid=n=>String(n).padStart(3,'0');
const groupIDs=[3,6,4,12,7,5,2,8,11,9,10,1,14];
const stepCost={3:25,6:30,4:40,12:50,7:60,5:70,2:80,8:100,11:120,9:140,10:160,1:180,14:200};
const groupCost={3:0,6:50,4:75,12:100,7:125,5:150,2:200,8:250,11:300,9:350,10:400,1:450,14:500};

const tint={1:'#d8b08b',2:'#a5ddd0',3:'#c2dc8a',4:'#bbd9f2',5:'#fff0b5',6:'#f1c2d9',7:'#abd49b',8:'#eba99a',9:'#b7b0db',10:'#c7cfd2',11:'#d4b6e6',12:'#a2cee8',14:'#8ac5bb'};
function dexGroups(s){return s.pool.length?s.pool:s.g.includes(15)?S[s.root].pool:s.g}
function cardPaint(s){const [g,h]=dexGroups(s);return h?'linear-gradient(135deg,'+tint[g]+' 12%,'+tint[h]+' 88%)':tint[g]}

const pools=Object.fromEntries(groupIDs.map(g=>[g,species.filter(s=>s.pool.includes(g))]));
const scents=['sea-incense','lax-incense','rock-incense','rose-incense','pure-incense','wave-incense','full-incense'];
const scentNames=['Incienso Marino','Incienso Suave','Incienso Roca','Incienso Rosa','Incienso Puro','Incienso Aqua','Incienso Lento'];
