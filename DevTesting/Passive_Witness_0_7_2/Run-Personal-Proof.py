from pathlib import Path
import subprocess,shutil,zipfile,json,hashlib,sys,re,uuid,base64
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');R=P/'DevTesting/Passive_Witness_0_7_2'
JDK=P.parents[1]/'Toolchains/JDK25/bin';SERVER=P/'.build/dependencies/HytaleServer-0.7.2.jar'
GAME=Path(r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest')
JAR=P/'build/libs/HyThief-1.3.1-container.12.jar';ROLES=['Goblin_Scrapper','Trork_Warrior','Outlander_Brute','Skeleton_Fighter','Zombie']
assert hashlib.sha256((GAME/'Server/HytaleServer.jar').read_bytes()).hexdigest()==hashlib.sha256(SERVER.read_bytes()).hexdigest()=='23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff'
if sys.argv[1]=='setup':
 R.mkdir(exist_ok=True)
 for file in ['PersonalWitnessProbe.java','Run-Personal-Proof.py']:
  if (Path(__file__).parent/file).resolve()!=(R/file).resolve():shutil.copy2(Path(__file__).parent/file,R/file)
 if not (R/'NativeFieldBlocks.java').exists():shutil.copy2(P/'DevTesting/Update_0_7_2_20261015/Release133/NativeFieldBlocks.java',R/'NativeFieldBlocks.java')
 classes=R/'classes';classes.mkdir(exist_ok=True)
 subprocess.run([str(JDK/'javac.exe'),'-cp',str(SERVER)+';'+str(JAR),'-d',str(classes),str(R/'PersonalWitnessProbe.java'),str(R/'NativeFieldBlocks.java')],check=True)
 with zipfile.ZipFile(JAR) as z:
  for n in z.namelist():
   if n.startswith('com/local/pickpocket/') and n.endswith('.class') and any(n.split('/')[-1].startswith(c) for c in ['ContainerRemovalProbe','ContainerVisualWitness','ContainerCaughtNotification','HyThiefPersonalWitnessAlert','ContainerOwnershipPolicy','PersistentContainerOwnership','OwnedContainerRegistry']):
    dest=classes/n;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(z.read(n))
 (classes/'manifest.json').write_text(json.dumps({'Group':'FirebeardDev','Name':'PersonalWitnessProbe','Version':'0.7.2-proof','ServerVersion':'=0.7.2','Main':'com.local.pickpocket.PersonalWitnessProbe','Dependencies':{'Local:HyThief':'*'}}))
 subprocess.run([str(JDK/'jar.exe'),'--create','--file',str(R/'PersonalWitnessProbe.jar'),'-C',str(classes),'.'],check=True)
 print('Proof helper compiled; native server and installed revision hash verified')
else:
 role=sys.argv[1];label=sys.argv[2] if len(sys.argv)>2 else 'first';author=len(sys.argv)>3 and sys.argv[3]=='author';assert role in ROLES and re.fullmatch('[A-Za-z0-9_-]+',label)
 run=R/('Run-'+role+'-'+label);assert not run.exists(),'Preserve evidence; use a new label'
 shutil.copytree(P/'DevTesting/Container_Theft_Test_World/Master',run)
 # Only disposable copies are changed. Existing masters and active saves are never written.
 mods=run/'mods'
 for old in mods.glob('*.jar'):old.unlink()
 shutil.copy2(JAR,mods/JAR.name);shutil.copy2(R/'PersonalWitnessProbe.jar',mods/'PersonalWitnessProbe.jar')
 fixture=mods/'PersonalFriendlyFixture';fixture.mkdir()
 (fixture/'manifest.json').write_text(json.dumps({'Group':'FirebeardDev','Name':'PersonalFriendlyFixture','Version':'0.7.2-proof','ServerVersion':'=0.7.2','IncludesAssetPack':True,'Dependencies':{'Local:HyThief':'*'}}))
 # Native Friendly settings on copies of the exact development parent assets.
 # No AI instruction, combat, perception, role ID, or group alteration.
 edits=[]
 with zipfile.ZipFile(JAR) as z:
  for n in z.namelist():
   if not n.startswith('Server/NPC/Roles/') or not n.endswith('.json'):continue
   a=json.loads(z.read(n));name=Path(n).stem
   if a.get('Type') not in ['Abstract','Variant']:continue
   if not any(key in name for key in ['Template_Trork_Melee','Template_Intelligent','Template_Predator']+ROLES):continue
   changed=False
   if 'DefaultPlayerAttitude' in a and isinstance(a['DefaultPlayerAttitude'],str):a['DefaultPlayerAttitude']='Friendly';changed=True
   if 'DefaultPlayerAttitude' in a.get('Parameters',{}):a['Parameters']['DefaultPlayerAttitude']['Value']='Friendly';changed=True
   # Goblin root relies on default Hostile rather than declaring it.
   if 'Goblin_Scrapper' in name and 'Reference' not in a:a['DefaultPlayerAttitude']='Friendly';changed=True
   if changed:
    dest=fixture/n;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(json.dumps(a));edits.append(n)
 cfg=json.loads((run/'config.json').read_text(encoding='utf-8-sig'));cfg['Mods']={'Local:HyThief':{'Enabled':True},'FirebeardDev:PersonalWitnessProbe':{'Enabled':True},'FirebeardDev:PersonalFriendlyFixture':{'Enabled':True}};cfg['ServerName']='DISPOSABLE passive personal witness '+role;(run/'config.json').write_text(json.dumps(cfg))
 marker=run/'container-theft-test-world.json';data=json.loads(marker.read_text(encoding='utf-8-sig'));data['WitnessRoles']=[role];data['DiagnosticVersion']='1.3.1-container.12';marker.write_text(json.dumps(data))
 if author:
  identity=R/'proof-world-identity.json'
  if not identity.exists():identity.write_text(json.dumps({'$binary':base64.b64encode(uuid.uuid4().bytes).decode(),'$type':'04'}))
  worldcfg=run/'universe/worlds/default/config.json';wc=json.loads(worldcfg.read_text(encoding='utf-8-sig'));wc['UUID']=json.loads(identity.read_text());wc['SpawnProvider']['SpawnPoint'].update({'X':0.0,'Y':64.1,'Z':-1.5,'Yaw':3.141592653589793});worldcfg.write_text(json.dumps(wc))
  data={k:data[k] for k in ['Kind','Version','Name','OwnershipField','WitnessRoles','DiagnosticVersion','ValidatedHytaleVersion','ValidatedHytaleRevision']};data.update({'WorldUUID':wc['UUID'],'ProofRole':role,'ChestCount':2,'NPCCount':2,'StartingBlocks':{'Id':'Rock_Stone_Cobble','Quantity':64}});marker.write_text(json.dumps(data,indent=2))
  cfg['ServerName']='HyThief Passive Witness Test - '+role;(run/'config.json').write_text(json.dumps(cfg))
 (run/'fixture-edits.json').write_text(json.dumps(edits,indent=2))
 subprocess.run([str(JDK/'java.exe'),'-Xmx2G','-Xms128m','-XX:ActiveProcessorCount=4','-Dpersonal.role='+role,'-Dpersonal.author='+str(author).lower(),'-jar',str(SERVER),'--assets',str(GAME/'Assets.zip'),'--auth-mode','offline','--bind','127.0.0.1:'+str(15579+ROLES.index(role)),'--disable-sentry','--disable-file-watcher','--disable-cpb-build'],cwd=run,stdout=(run/'native.log').open('w'),stderr=subprocess.STDOUT,timeout=120,check=True)
 evidence=(run/'personal-evidence.txt').read_text();print(evidence)
 assert 'FAILED' not in evidence and ('AUTHOR_PASS Role='+role if author else 'RESULT Role='+role) in evidence
