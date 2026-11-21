const benefits={
 'point-card':['Tarjeta de puntos',75,'Conserva tus PC al usar la GS Ball.'],
 'luck-incense':['Incienso Duplo',125,'Aumenta un 100 % la probabilidad de encontrar objetos al eclosionar Pokémon'],
 'oval-charm':['Amuleto Oval',50,'Permite activar y desactivar grupos de huevo'],
 'card-key':['Llave Magnética',150,'Permite bloquear y desbloquear generaciones en el Criadero.'],
 parcel:['Paquete',1,'Otorga 5 PC. Puedes comprarlo varias veces.'],
 'scope-lens':['Periscopio',50,'Sigue tu progreso en el Criadero'],
 'blue-card':['Tarjeta Azul',150,'Aumenta un 100 % las PC por eclosión.'],
 'magma-emblem':['Emblema Magma',150,'Aumenta un 100 % el ritmo de todas las incubadoras.'],
 'silph-scope':['Visor Silph',75,'Muestra una barra de progreso bajo cada huevo.'],
 'rare-incense':['Incienso Raro',100,'Reduce un 50% las eclosiones necesarias para desbloquear los inciensos.'],
 'badge-case':['Caja de Insignias',100,'Reduce un 50% la probabilidad de obtener una especie sin evolución.'],
 talisman:['Talismán',100,'Duplica la probabilidad de encontrar Reales de Cobre, de Plata y de Oro.'],
 everstone:['Piedra Eterna',125,'Aumenta un 100% las PC por evolución.'],
 journal:['Diario',25,'Conserva los shinies registrados al usar la GS Ball.'],
 case:['Neceser',175,'Duplica la probabilidad de obtener shinies.'],
 'odd-stone':['Piedra Insólita',200,'Reduce un 50% los precios de los objetos evolutivos.'],
 'exp-share':['Repartir Experiencia',225,'Reduce un 25% las eclosiones necesarias para evolucionar.'],
 'life-orb':['Vidasfera',250,'Facilita la eclosión de todos los huevos un 25%.'],
 'magma-stone':['Piedra Magma',350,'Potencia las incubadoras']
};
const groupBenefits={};
for(const [g,key,name] of [
 [11,'soot-sack','Saco Hollín'],[2,'good-rod','Caña Buena'],[3,'gracidea','Gracídea'],
 [9,'devon-scuba-gear','Bombona Devon'],[12,'aqua-suit','Traje Aqua'],[14,'dragon-skull','Cráneo Dragón'],
 [1,'slowpoke-tail','Cola Slowpoke'],[4,'silver-wing','Ala Plateada'],[5,'dowsing-machine','Zahorí'],
 [6,'clear-bell','Cascabel Claro'],[7,'rich-mulch','Abono Fértil'],[8,'go-goggles','Gafas Aislantes'],[10,'enigma-stone','Misticristal']
]){
 groupBenefits[g]=key;benefits[key]=[name,100,'Facilita la eclosión de los huevos de '+G[g]+' un 50%.',g];
}
const scopeLists=Object.fromEntries(groupIDs.map(g=>[g,species.filter(s=>dexGroups(s).includes(g))]));
const regionLists=regions.map((_,i)=>species.filter(s=>s.gen===i+1));
