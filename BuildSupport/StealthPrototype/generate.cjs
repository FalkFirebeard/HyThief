const fs=require('fs'),path=require('path');
const read=p=>JSON.parse(fs.readFileSync(p,'utf8').replace(/^\uFEFF/,''));
const inputs=path.join(__dirname,'inputs');
const map=read(path.join(inputs,'roles.json'));
const selected=read(path.join(__dirname,'supported-matrix.json')).map(row=>row.Role).sort();
if(selected.length!==185||new Set(selected).size!==185||selected.some(id=>id==='Skeleton'||id.startsWith('Wolf_')))throw Error('Support catalog mismatch');
const out=path.join(__dirname,'assets');fs.mkdirSync(out,{recursive:true});
const clone=x=>structuredClone(x),used=new Set(),patched=[];
function value(x,scope){if(x&&typeof x==='object'&&Object.keys(x).length===1&&'Compute'in x){if(x.Compute in scope)return scope[x.Compute];const e=x.Compute.replace(/[A-Za-z_]\w*/g,k=>typeof scope[k]==='number'?String(scope[k]):'UNRESOLVED');if(/^[-+*/().\d\s]+$/.test(e))return Function('return ('+e+')')();return x}return x}
const defaults=d=>Object.fromEntries(Object.entries(d.Parameters||{}).map(([k,v])=>[k,v&&typeof v==='object'&&'Value'in v?v.Value:v]));
function resolve(id,incoming={},chain=[]){if(chain.includes(id)||!map[id])throw Error('reference '+id);used.add(id);const d=map[id].Data,s={...defaults(d),...incoming};if(d.Type==='Variant')return resolve(d.Reference,Object.fromEntries(Object.entries(d.Modify||{}).map(([k,v])=>[k,value(v,s)])),[...chain,id]);return {id,data:d,scope:s};}
function write(asset,data){const p=path.join(out,asset);fs.mkdirSync(path.dirname(p),{recursive:true});fs.writeFileSync(p,JSON.stringify(data,null,2)+'\n')}
for(const role of selected){
 const resolved=resolve(role),root=clone(resolved.data);let sequence=0;
 for(const [key,v]of Object.entries(resolved.scope)){if(root.Parameters?.[key]&&typeof root.Parameters[key]==='object'&&'Value'in root.Parameters[key]){root.Parameters[key].Value=v;if(Array.isArray(v)&&v.length===0&&!root.Parameters[key].TypeHint)root.Parameters[key].TypeHint='String';}}
 function visit(node,scope,initial=false,context='role',channelHint=''){
  if(Array.isArray(node)){node.forEach((v,i)=>visit(v,scope,initial,context+'/'+i,channelHint));return}
  if(!node||typeof node!=='object')return;
  if(node.Sensor?.Type==='State'){
   const state=node.Sensor.State;
   if(typeof state==='string'&&!state.startsWith('.'))initial=state==='Idle';
  }
  if(initial&&node.Reference){
   const ref=value(node.Reference,scope);
   if(typeof ref==='string'&&ref!=='$Null'&&ref!==''&&map[ref]?.Data.Type==='Component'){
    used.add(ref);const component=clone(map[ref].Data);
    const cscope={...defaults(component),...Object.fromEntries(Object.entries(node.Modify||{}).map(([k,v])=>[k,value(v,scope)]))};
    const id='HyThiefStealth_'+role+'_C'+(++sequence);
    node.Reference=id;
    // Mixed Sight_Or_Sound instructions must classify their child channels separately.
    visit(component.Content,cscope,true,ref,ref.includes('Scent')?'scent':ref.includes('Sound')&&!ref.includes('Sight')?'sound':channelHint);
    write('Server/NPC/Roles/HyThief/Stealth/'+id+'.json',component);
   }
  }
  if(initial&&['Mob','Player','Entity'].includes(node.Type)&&node.Range!==undefined&&channelHint!=='sound'){
   const range=value(node.Range,scope), players=node.Type==='Player'||node.GetPlayers===undefined||value(node.GetPlayers,scope)!==false;
   const onlyLocked=node.OnlyLockedTarget!==undefined&&value(node.OnlyLockedTarget,scope);
   const projected=node.UseProjectedDistance!==undefined&&value(node.UseProjectedDistance,scope);
   if(players&&!onlyLocked){
    if(typeof range!=='number'||projected)throw Error('Unsupported acquisition range/metric '+role+' '+context);
    const filterText=JSON.stringify(node.Filters||[]);
    const channel=channelHint==='scent'?'scent':filterText.includes('ViewSector')?'sight':filterText.includes('Crouching')?'sound':node.Type==='Player'?'watch':'proximity';
    if(channel!=='sound'){
     node.Filters=[...(node.Filters||[]),{Type:'HyThiefStealthAcquisition',BaseRange:range,Channel:channel,Projected:false}];
     patched.push({Role:role,Site:context,Channel:channel,BaseRange:range});
    }
   }
  }
  for(const [k,v]of Object.entries(node))if(!['Parameters','Modify','InteractionInstruction','Reference','Filters'].includes(k))visit(v,scope,initial,context+'/'+k,channelHint);
 }
 visit(root,resolved.scope);
 if(map[role].Data.Type==='Abstract'){
  // Preserve abstract builder identity/status; do not invent a spawnable variant.
  write(map[role].Path,root);
  continue;
 }
 const id='HyThiefStealth_'+role;
 // Concrete root's behavior unchanged; only Idle sensor/component copies contain filters.
 write('Server/NPC/Roles/HyThief/Stealth/'+id+'.json',root);
 const variant={Type:'Variant',Reference:id,Parameters:clone(root.Parameters||{}),Modify:Object.fromEntries(Object.keys(root.Parameters||{}).map(k=>[k,{Compute:k}]))};
 write(map[role].Path,variant);
}
fs.writeFileSync(path.join(__dirname,'sites.json'),JSON.stringify({Version:'1.2.1-revamp.11',Roles:selected,Sites:patched},null,2)+'\n');
console.log('STEALTH_FULL_ROLLOUT roles='+selected.length+' initialSites='+patched.length+' referencedInputs='+used.size);
