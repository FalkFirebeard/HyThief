"""Read-only native NPC inventory. Writes reports only to an explicit output directory.

This is a structural audit, not Hytale's expression evaluator or a runtime validator.
Computed expressions other than parameter names remain visibly unresolved.
"""
import argparse, collections, csv, fnmatch, hashlib, json, pathlib, re, zipfile

def load(data): return json.loads(data.decode('utf-8-sig'))
def sha(path):
    h=hashlib.sha256()
    with open(path,'rb') as f:
        for b in iter(lambda:f.read(1024*1024),b''): h.update(b)
    return h.hexdigest()
def walk(v):
    if isinstance(v,dict):
        yield v
        for x in v.values(): yield from walk(x)
    elif isinstance(v,list):
        for x in v: yield from walk(x)
def merge(a,b):
    out=dict(a)
    for k,v in b.items():
        out[k]=merge(out[k],v) if isinstance(v,dict) and isinstance(out.get(k),dict) else v
    return out

ap=argparse.ArgumentParser()
ap.add_argument('--project',required=True);ap.add_argument('--assets',required=True)
ap.add_argument('--jar',required=True);ap.add_argument('--out',required=True)
ap.add_argument('--previous-assets'); args=ap.parse_args()
p=pathlib.Path(args.project);out=pathlib.Path(args.out);out.mkdir(parents=True,exist_ok=True)
z=zipfile.ZipFile(args.assets)
paths=[n for n in z.namelist() if n.startswith('Server/NPC/Roles/') and n.endswith('.json')]
roles={pathlib.PurePosixPath(n).stem:load(z.read(n)) for n in paths}
duplicate_paths={r:[n for n in paths if pathlib.PurePosixPath(n).stem==r] for r in roles if sum(pathlib.PurePosixPath(n).stem==r for n in paths)>1}
rpaths={pathlib.PurePosixPath(n).stem:n for n in paths}
groups={pathlib.PurePosixPath(n).stem:load(z.read(n)) for n in z.namelist() if n.startswith('Server/NPC/Groups/') and n.endswith('.json')}
models={pathlib.PurePosixPath(n).stem:load(z.read(n)) for n in z.namelist() if n.startswith('Server/Models/') and n.endswith('.json')}
j=zipfile.ZipFile(args.jar)
cfg={n:load(j.read('config/'+n+'.json')) for n in ['pickpocket-role-map','pickpocket-loot-resolver','pickpocket-loot-tables']}
for n,data in cfg.items():
    assert data==load((p/'config'/(n+'.json')).read_bytes()),'Source/installed defaults differ: '+n
rm=cfg['pickpocket-role-map']; lr=cfg['pickpocket-loot-resolver'];tables=cfg['pickpocket-loot-tables']['tables']
role_groups={r:g for g,rs in rm['groups'].items() for r in rs}
blocked=set(rm['non_pickpocketable_roles'])|set(lr['blockedRoles'])
blocked_groups=set(rm['non_pickpocketable_groups'])|set(lr['blockedGroups'])
def profile(r):
    g=role_groups.get(r)
    if r in blocked or g in blocked_groups: return None
    a=lr['groupAssignments'].get(g,{})
    c=a.get('byRole',{}).get(r)
    if not c:
        prefixes=[k for k in a.get('byPrefix',{}) if r.startswith(k)]
        c=a['byPrefix'][max(prefixes,key=len)] if prefixes else a
    return dict(c,group=g) if c and c.get('table') in tables else None
supported={r:profile(r) for r in role_groups if profile(r)}
matrix=load((p/'BuildSupport/supported-matrix.json').read_bytes())
assert set(supported)=={x['Role'] for x in matrix},'Matrix/resolver mismatch'
cache={}
def resolve(r,seen=()):
    if r in cache:return cache[r]
    if r in seen or r not in roles:return {},{},[r]
    raw=roles[r];base,params,chain=resolve(raw['Reference'],seen+(r,)) if raw.get('Reference') in roles else ({},{},[])
    params=dict(params);params.update({k:v.get('Value') for k,v in raw.get('Parameters',{}).items() if isinstance(v,dict) and 'Value' in v})
    mod=raw.get('Modify',{})
    params.update(mod)
    body=merge(base,{k:v for k,v in raw.items() if k not in ['Parameters','Modify','Reference']})
    # Modify values primarily override template parameters; keep only matching native fields.
    body=merge(body,{k:v for k,v in mod.items() if k in body})
    cache[r]=(body,params,[r]+chain)
    return cache[r]
def value(v,params,seen=()):
    if isinstance(v,dict) and set(v)=={'Compute'}:
        s=v['Compute']
        if s in params and s not in seen:return value(params[s],params,seen+(s,))
        return 'EXPR:'+str(s)
    return v
def behavior(r):
    body,params,chain=resolve(r); visited=set();signals=set();actions=set();refs=set()
    def scan(v,env,context='Instruction'):
        if isinstance(v,list):
            for x in v:scan(x,env,context)
            return
        if not isinstance(v,dict):return
        t=v.get('Type')
        if isinstance(t,str):actions.add(t)
        if t in ['Beacon','BeaconBroadcast']:
            msg=value(v.get('Message',''),env)
            signals.add(context+'Beacon:'+str(msg))
        ref=value(v.get('Reference',''),env)
        if isinstance(ref,str) and ref in roles and roles[ref].get('Type')=='Component':
            refs.add(ref)
            key=(ref,context)
            if key not in visited:
                visited.add(key);b,pa,_=resolve(ref);en=dict(pa);en.update(env);en.update({k:value(x,env) for k,x in v.get('Modify',{}).items()});scan(b,en,context)
        for k,x in v.items():
            scan(x,env,'Sensor' if k in ['Sensor','Sensors'] else 'Action' if k in ['Actions','Action'] else context)
    scan(body,params)
    return sorted(signals),sorted(actions),sorted(refs)
def direct_groups(r):
    result=[]
    for g,d in groups.items():
        if any(fnmatch.fnmatchcase(r,x) for x in d.get('IncludeRoles',[])) and not any(fnmatch.fnmatchcase(r,x) for x in d.get('ExcludeRoles',[])): result.append(g)
    return sorted(result)
def family(r,path,appearance):
    if '/_Core/' in path:return 'Core/test scaffolding'
    if 'Klops' in str(appearance) or '/Klops/' in path:return 'Klops'
    if r in ['Dungeon_Feastmaster_Boss','Dungeon_Feastmaster_Feeding','Goblin_Pilot']:return 'Goblin'
    if '/Faction/' in path:
        return path.split('/Faction/')[1].split('/')[0]
    if '/Skeleton/' in path:return 'Skeleton'
    if '/Zombie/' in path:return 'Zombie'
    if '/Temple/' in path:
        for f in ['Kweebec','Feran','Klops','Goblin']:
            if f in str(appearance) or f in r:return f
        if r=='Temple_Mithril_Guard':return 'Human'
        return 'Temple wildlife/scaffolding'
    if '/Gobliterator/' in path or '/DungeonFeastmaster/' in path:return 'Goblin encounter'
    if '/Other/' in path:
        if r.startswith('Bramblekin'):return 'Bramblekin'
        if r=='Hedera':return 'Hedera'
        if r=='Slothian':return 'Slothian'
        if r=='Quest_Master':return 'Human'
    if r=='Wraith':return 'Wraith'
    if r=='Cinematic_Shrouded_Figure':return 'Human'
    if r in ['Ghoul','Werewolf','Shadow_Knight'] or '/Void/' in path:return 'Ambiguous/void' if r not in ['Ghoul','Werewolf','Shadow_Knight'] else r
    return path.split('/')[3] if len(path.split('/'))>3 else 'Other excluded'

families={'Kweebec','Feran','Klops','Goblin','Trork','Outlander','Skeleton','Zombie','Tuluk','Bramblekin','Hedera','Human','Slothian','Wraith'}
review={'Ghoul','Werewolf','Shadow_Knight','Void_Spawn','Void_Spawn_Entombed','Void_Spawn_Surge','Void_Spawn_Entombed_Anomaly'}
rows=[];evidence={}
for r in sorted(roles):
    raw=roles[r];path=rpaths[r];body,params,chain=resolve(r)
    app=value(body.get('Appearance',params.get('Appearance','')),params)
    fam=family(r,path,app);typ=raw.get('Type','Unknown');concrete=typ in ['Generic','Variant'];prof=supported.get(r)
    sig,acts,refs=behavior(r)
    model=models.get(str(app),{})
    native=direct_groups(r);att=value(body.get('AttitudeGroup',params.get('AttitudeGroup','')),params)
    notes=[];category='D';wit='No';fw='No';lw='No';resp='No';pick='No'
    if fam in families:
        category='A' if prof else ('C' if not concrete else 'B')
        wit='Yes' if concrete else 'N/A';fw='Yes: native group match required' if native and concrete else ('Review: identity/group needed' if concrete else 'N/A');lw=wit
        combat=any(x in acts for x in ['Attack','AttackSequence','MeleeAttack','AttackRange','AttackCheck','CombatAbility','CombatActionEvaluator']) or any('Attack_Sequence' in x for x in refs) or 'Combat' in json.dumps(body) and any(x in chain for x in ['Template_Intelligent','Template_Skeleton','Template_Feran_Sharptooth'])
        resp='Candidate: native combat; listener proof required' if combat and concrete else 'No: civilian/scripted or no verified combat'
        pick='Yes: '+prof['table'] if prof else 'Candidate; no loot configured'
        if not concrete: notes.append('Native '+typ+'; not a normal concrete spawn');wit=fw=lw='N/A';resp='N/A';pick='Configured only: '+prof['table'] if prof else 'N/A'
    if fam=='Goblin encounter':
        category='D' if concrete else 'C';wit=fw=lw=resp=pick='No: mechanical encounter object' if concrete else 'N/A';notes.append('Gobliterator mechanical boss part/phase/barrel; not a social Goblin simply because of group/name')
    if r in ['Dungeon_Feastmaster_Boss','Dungeon_Feastmaster_Feeding','Goblin_Pilot','Skeleton_Elite','Skeleton_Elite_Phase_2']:
        category='C';wit=fw=lw='Review: encounter only';resp='Review: encounter state';pick='Review: encounter; no loot configured';notes.append('Special encounter/phase role; no automatic pickpocket expansion')
    if r.startswith('Wolf_') or r=='Skeleton_Incandescent_Head' or r.startswith('Edible_') or fam=='Scarak':
        category='D';wit=fw=lw=resp=pick='No';notes.append('Animal/insectoid/edible creature/detached head; not a social theft participant')
    if r in ['Risen_Gunner','Risen_Knight']:
        category='C';wit=fw=lw='Review: summoned ally';resp='Review: summoner relationship';pick='No: existing explicit exclusion';notes.append('Template_Summoned_Ally; retain current policy, no blanket summoned-undead rule')
    if r in blocked or role_groups.get(r) in blocked_groups:
        pick='No: existing exclusion';notes.append('HyThief exclusion: '+str(role_groups.get(r,'explicit role')))
        if role_groups.get(r)=='neutral_youth':category='C';wit=fw=lw='Review: youth policy';resp='No: youth';notes.append('No automatic youth pickpocket addition')
    if '/Temple/' in path and concrete and category in ['B','C']:
        category='C';notes.append('Temple/scripted variant; model does not imply faction or normal combat');pick='Review: scripted role; no loot configured' if 'No:' not in pick else pick
        if fam in ['Goblin','Kweebec','Feran','Klops','Human']:wit=lw='Review: scripted/social';fw='Review: native membership must be checked'
    if '/_Deprecated/' in path:
        notes.append('Native deprecated asset retained; not a normal expansion candidate')
        if concrete and category=='B': category='C'
    if r in review:
        category='C' if concrete or typ=='Abstract' else 'D';wit=fw=lw=resp='Review';pick='No: existing exclusion' if r in blocked else 'Review: sapience/social use unproven';notes.append('Manual review; not included in headline humanoid count')
    if fam=='Core/test scaffolding' or typ=='Component':
        category='C';wit=fw=lw=resp=pick='N/A';notes.append('Reusable instruction/component/template or dev fixture; not a production NPC')
    if r=='Skeleton':notes.append('CURRENT native Variant -> Template_Skeleton. No pool configured; ordinary spawn use not established. Earlier template-only inference is insufficient.')
    if r=='Hedera':notes.append('Hedera model/role family; model parent Trork is not faction membership')
    if r=='Wraith':notes.append('Template_Intelligent; Player-derived model; wielded scythe. Armed humanoid undead candidate; social/faction identity requires creator decision')
    if fam in ['Human','Slothian']: category='C';wit=fw=lw=resp='Review: scripted/static';pick='Review: no normal configured target'
    if not notes and category=='D':notes.append('Outside humanoid/social production scope; native path/appearance/inheritance in evidence')
    row={'Family':fam,'Role ID':r,'Native Type':typ,'Class':category,'Pickpocket':pick,'Witness':wit,'Faction Witness':fw,'Local Witness':lw,'Responder':resp,'Native group patterns':', '.join(native) or 'None direct','Attitude group':str(att),'Appearance':str(app),'Parent':raw.get('Reference',''),'Loot pool':prof['table'] if prof else '', 'Profile':prof.get('classProfile','') if prof else '', 'Asset':path,'Notes':'; '.join(notes)}
    rows.append(row);evidence[r]={'asset':path,'duplicatePaths':duplicate_paths.get(r,[]),'inheritance':chain,'appearance':app,'modelParent':model.get('Parent',''),'attitudeGroup':att,'directGroupPatterns':native,'nativeBeaconDeclarations':sig,'instructionTypes':acts,'componentReferences':refs,'parameters':{k:params[k] for k in params if any(x in k.lower() for x in ['range','sight','view','weapon','attitude','attack','wake','sleep','broadcast'])},'resolver':prof,'notes':notes}

with open(out/'NPC_ROSTER_0_7_1.csv','w',encoding='utf-8-sig',newline='') as f:
    w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
(out/'NPC_ROSTER_0_7_1_EVIDENCE.json').write_text(json.dumps(evidence,indent=2,ensure_ascii=False)+'\n',encoding='utf8')
delta={}
if args.previous_assets:
    old=zipfile.ZipFile(args.previous_assets);oldp={pathlib.PurePosixPath(n).stem:n for n in old.namelist() if n.startswith('Server/NPC/Roles/') and n.endswith('.json')}
    delta={'added':sorted(set(roles)-set(oldp)),'removed':sorted(set(oldp)-set(roles)),'changed':sorted(r for r in set(roles)&set(oldp) if load(z.read(rpaths[r]))!=load(old.read(oldp[r]))),'previousAssetsSha256':sha(args.previous_assets)}
headline=[x for x in rows if x['Family'] in families and x['Native Type'] in ['Generic','Variant'] and x['Class']!='D']
summary={'roleFilesScanned':len(paths),'distinctRoleIds':len(rows),'duplicatePaths':duplicate_paths,'types':dict(collections.Counter(x['Native Type'] for x in rows)),'families':sorted(families),'concreteRelevant':len(headline),'ordinarySupportedConcrete':sum(x['Class']=='A' for x in headline),'suitableUnsupported':sum(x['Class']=='B' for x in headline),'specialConcreteReview':sum(x['Class']=='C' for x in headline),'configuredSupported':len(supported),'configuredMissing':sorted(set(supported)-set(roles)),'configuredAbstract':sorted(r for r in supported if r in roles and roles[r]['Type']=='Abstract'),'assetsSha256':sha(args.assets),'installedJarSha256':sha(args.jar),'deltaFrom070':delta,'familyCounts':{f:dict(collections.Counter(x['Class'] for x in headline if x['Family']==f)) for f in sorted(families)},'draugrRoleIds':[r for r in roles if 'draugr' in r.lower()]}
(out/'NPC_ROSTER_0_7_1_SUMMARY.json').write_text(json.dumps(summary,indent=2)+'\n',encoding='utf8')
print(json.dumps(summary,indent=2))
