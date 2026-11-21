const prices=[100,250,500,750,1000,1250,1500,1750];
const names={...DB.items,'magnet':'Piedra Imán','ice-stone':'Piedra Hielo','soothe-bell':'Cascabel Alivio','linking-cord':'Cable Link','black-belt':'Cinturón Negro','punching-glove':'Guantes de Boxeo','focus-band':'Banda Foco','up-grade':'Mejora','kings-rock':'Roca del Rey','metal-coat':'Revest. Metálico','dragon-scale':'Escama Dragón','prism-scale':'Escama Bella','deep-sea-tooth':'Diente Marino','deep-sea-scale':'Escama Marina',protector:'Protector',electirizer:'Electrizador',magmarizer:'Magmatizador','reaper-cloth':'Tela Terrible','power-bracer':'Brazal Recio','macho-brace':'Brazal Firme','heat-rock':'Roca Calor','oval-charm':'Amuleto Oval'};
const drops='ice-stone kings-rock metal-coat dragon-scale up-grade prism-scale deep-sea-tooth deep-sea-scale protector electirizer magmarizer reaper-cloth black-belt punching-glove focus-band'.split(' ');
const goods={};
const clickItems={},rateItems={},boostItems={},gates={};
function product(key,name,price,description,gate){names[key]=name;goods[key]=[price,description];if(gate)gates[key]=gate}
for(const [k,name,price,value,id] of [
 ['macho-brace','Brazal Firme',50,1],['lucky-egg','Huevo Suerte',200,2],['power-weight','Pesa Recia',250,3,242],['power-bracer','Brazal Recio',250,2,409],['power-belt','Cinto Recio',250,1,213]
]){clickItems[k]=value;product(k,name,price,'Aumenta en '+value+' la eficacia de cada toque.',id?{id}:null)}
for(const [k,name,price,pct,id] of [
 ['power-lens','Lente Recia',250,50,609],['power-band','Banda Recia',250,50,476],['power-anklet','Franja Recia',250,25,291],['heat-rock','Roca Calor',500,25]
]){rateItems[k]=pct;product(k,name,price,'Aumenta un '+pct+' % el ritmo de todas las incubadoras.',id?{id}:null)}
for(const [g,k,name] of [[11,'black-sludge','Lodo Negro'],[2,'damp-rock','Roca Lluvia'],[3,'silver-powder','Polvo Plata'],[9,'deep-sea-scale','Escama Marina'],[12,'mystic-water','Agua Mística'],[14,'dragon-fang','Colmillo Dragón'],[1,'leftovers','Restos'],[4,'sharp-beak','Pico Afilado'],[5,'soft-sand','Arena Fina'],[6,'metronome','Metrónomo'],[7,'miracle-seed','Semilla Milagro'],[8,'wise-glasses','Gafas Especiales'],[10,'hard-stone','Piedra Dura']]){
 boostItems[g]=k;product(k,name,400,'Facilita la eclosión de los huevos de '+G[g]+' un 25 %.', {g});
}
product('rare-candy','Carameloraro',50,'Reduce un 10 % las eclosiones necesarias para evolucionar, con una rebaja mínima de 1',{evol:5});
product('nugget','Pepita',50,'Obtienes 1 PC por cada eclosión de un Pokémon ya registrado');
product('pearl','Perla',100,'Obtienes 1 PC más por cada eclosión de un Pokémon ya registrado',{requires:'nugget'});
product('star-piece','Trozo Estrella',250,'Obtienes 2 PC más por cada eclosión de un Pokémon ya registrado',{requires:'nugget'});
product('devon-parts','Piezas Devon',200,'Aumenta un 50 % las PC por evolución');
const potions=['potion','super-potion','hyper-potion','max-potion','full-restore'];
['Poción','Superpoción','Hiperpoción','Max Poción','Restaurar Todo'].forEach((name,i)=>
 product(potions[i],name,100+25*i,'Reduce un 10 % la probabilidad de obtener una especie ya completada en la Pokédex'));
const choiceItems={'choice-scarf':10,'choice-specs':15,'choice-band':25};
product('choice-scarf','Pañuelo Elección',250,'Reduce un 10 % la probabilidad de obtener una especie sin evolución.',{evol:5});
product('choice-specs','Gafas Elección',300,'Reduce un 15 % la probabilidad de obtener una especie sin evolución.',{evol:10});
product('choice-band','Cinta Elección',500,'Reduce un 25 % la probabilidad de obtener una especie sin evolución.',{evol:15});
product('shiny-charm','Amuleto Iris',1000,'Aumenta de 0,05 % a 0,25 % la probabilidad de obtener shinies.',{shiny:10});
product('charcoal','Carbón',500,'Aumenta un 50 % el ritmo de todas las incubadoras.',{total:1000});
rateItems.charcoal=50;
const flutes=['blue-flute','yellow-flute','red-flute','black-flute','white-flute','azure-flute','poke-flute','eon-flute'];
const fluteNames=['Flauta Azul','Flauta Amarilla','Flauta Roja','Flauta Negra','Flauta Blanca','Flauta Azur','Poké Flauta','Flauta Eón'];
const badges='Boulder Cascade Thunder Rainbow Soul Marsh Volcano Earth Zephyr Hive Plain Fog Storm Mineral Glacier Rising Stone Knuckle Dynamo Heat Balance Feather Mind Rain Coal Forest Cobble Fen Relic Mine Icicle Beacon Trio Basic Insect Bolt Quake Jet Freeze Legend'.split(' ');
const badgeNames='Roca Cascada Trueno Arcoíris Alma Pantano Volcán Tierra Céfiro Colmena Planicie Niebla Tormenta Mineral Glaciar Dragón Piedra Puño Dínamo Calor Equilibrio Pluma Mente Lluvia Lignito Bosque Adoquín Ciénaga Reliquia Mina Carámbano Faro Trío Base Élitro Voltio Temblor Jet Témpano Leyenda'.split(' ');
const scentRules={
 'sea-incense':[298,[12,2],'Facilita la eclosión de los huevos Marino y Costa un 100 %.'],
 'rock-incense':[438,[5,10],'Facilita la eclosión de los huevos Campo y Mineral un 100 %.'],
 'rose-incense':[440,[6,7],'Facilita la eclosión de los huevos Hada y Planta un 100 %.'],
 'wave-incense':[458,[12,2,9],'Facilita la eclosión de los huevos Marino, Costa y Abisal un 100 %.'],
 'lax-incense':[360,[],'Facilita la eclosión de todos los huevos un 25 %.'],
 'full-incense':[446,[10,1,14],'Dificulta la eclosión de Bicho, Hada y Volador un 200 % y facilita la de Mineral, Monstruo y Dragón un 100 %.'],
 'pure-incense':[433,[],'Reduce un 50 % la probabilidad de eclosionar una especie sin evolución.']
};
scents.forEach((k,i)=>product(k,scentNames[i],500,scentRules[k][2],{baby:scentRules[k][0]}));
product('discount-coupon','Cupón de Descuento',250,'Reduce un 10 % los precios de la tienda.',{purchases:10});
product('stardust','Polvo Estelar',500,'Aumenta un 50 % las PC por eclosión.');
const regions=['Kanto','Johto','Hoenn','Sinnoh','Teselia'];
const snacks=['Limonada','Caramelo Furia','Galleta Lava','Barrita Plus','Porcelanita'];
const balls=['Poké Ball','Super Ball','Ultra Ball','Gloria Ball','Ensueño Ball'];
const ballKeys=['poke-ball','great-ball','ultra-ball','cherish-ball','dream-ball'];
const snackKeys=['lemonade','rage-candy-bar','lava-cookie','old-gateau','casteliacone'];
const rewards={};
const scarves={
 'green-scarf':['Pañuelo Verde',[3,7,5]],
 'blue-scarf':['Pañuelo Azul',[12,2,9]],
 'pink-scarf':['Pañuelo Rosa',[6,8,11]],
 'yellow-scarf':['Pañuelo Amarillo',[4,10,1]],
 'red-scarf':['Pañuelo Rojo',[14]]
};
reward('iris-ticket','Iris-ticket','Duplica las PC por eclosión repetida de un Pokémon ya registrado como shiny');
for(const [key,[name,groups]] of Object.entries(scarves)){
 const labels=groups.map(g=>G[g]).join(', ');
 reward(key,name,'Aumenta en 0,1 puntos porcentuales la probabilidad shiny en '+labels);
}
function awardShinyItems(a){
 if(!a.shiny.length)return;
 if(!a.owned.includes('iris-ticket')&&groupIDs.every(g=>a.shiny.some(id=>dexGroups(S[id]).includes(g))))a.owned.push('iris-ticket');
 for(const [key,[,groups]] of Object.entries(scarves)){
  if(!a.owned.includes(key)&&a.shiny.filter(id=>dexGroups(S[id]).some(g=>groups.includes(g))).length>=3)a.owned.push(key);
 }
}
function reward(key,name,description){names[key]=name;rewards[key]=description}
regions.forEach((region,i)=>{
 reward(snackKeys[i],snacks[i],'Aumenta un 50% las PC por evolución.');
 reward(ballKeys[i],balls[i],'Aumenta un 20% la probabilidad de obtener una especie aún bloqueada o con evoluciones bloqueadas.');
});
reward('master-ball','Master Ball','Facilita la eclosión de los huevos un 50% y aumenta un 100% el ritmo de todas las incubadoras.');
rateItems['master-ball']=100;
product('gs-ball','GS Ball',1000,'Úsala para resonar con Celebi y volver al pasado.',{badges:1});
product('flame-orb','Llamasfera',1000,'Potencia las incubadoras.');
product('toxic-orb','Toxisfera',1000,'Potencia cada toque.');
product('lustrous-orb','Lustresfera',1000,'Duplica la probabilidad de obtener shinies.');
