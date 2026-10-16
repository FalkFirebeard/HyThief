from pathlib import Path
import sys,shutil,json,zipfile,hashlib,subprocess,re
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');W=P/'DevTesting/Container_Theft_Test_World';R=W/'Passive_Client_Expansion_0_7_2'
JDK=P.parents[1]/'Toolchains/JDK25/bin';SERVER=P/'.build/dependencies/HytaleServer-0.7.2.jar';JAR=P/'DevTesting/Passive_Witness_0_7_2/Candidate/HyThief-1.3.1-container.12.jar'
GAME=Path(r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest');SAVES=Path(r'C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves')
EXPECTED='6b4c1500428674b4deb09c116ca6eb7d37360f9137e9a39ac527068d79dbe800';SERVERHASH='23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff'
STATIONS=[dict(Role=role,Family=family,Color=color,X=x,Z=z) for role,family,color,x,z in [('Goblin_Scrapper','Goblin','Orange',-36,48),('Trork_Warrior','Trork','Purple',0,48),('Outlander_Brute','Outlander','Cyan',36,48),('Skeleton_Fighter','Skeleton','White',-18,88),('Zombie','Zombie','Black',18,88)]]
digest=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
snapshot=lambda root:{str(f.relative_to(root)):digest(f) for f in root.rglob('*') if f.is_file()}
for f in [SERVER,GAME/'Server/HytaleServer.jar']:assert digest(f)==SERVERHASH,'Hytale changed: audit before fixture authoring'
assert digest(JAR)==EXPECTED
with zipfile.ZipFile(GAME/'Server/HytaleServer.jar') as archive:
 mf=archive.read('META-INF/MANIFEST.MF').decode();assert 'Implementation-Version: 0.7.2' in mf and 'd769dc3d57881261a62858e1394f0e4de9a21eca' in mf
def compile_helper():
 for name in ['ExpandedContainerField.java','Expand-Container-Field.py']:
  source=Path(__file__).parent/name
  if source.resolve()!=(R/name).resolve():shutil.copy2(source,R/name)
 if not (R/'NativeFieldBlocks.java').exists():shutil.copy2(P/'DevTesting/Passive_Witness_0_7_2/NativeFieldBlocks.java',R/'NativeFieldBlocks.java')
 classes=R/'classes';classes.mkdir(exist_ok=True)
 subprocess.run([str(JDK/'javac.exe'),'-cp',str(SERVER)+';'+str(JAR),'-d',str(classes),str(R/'ExpandedContainerField.java'),str(R/'NativeFieldBlocks.java')],check=True)
 with zipfile.ZipFile(JAR) as archive:
  for n in archive.namelist():
   if n.startswith('com/local/pickpocket/') and n.endswith('.class') and any(n.split('/')[-1].startswith(a) for a in ['ContainerVisualWitness','ContainerRemovalProbe','ContainerCaughtNotification','ContainerOwnershipPolicy','HyThiefPersonalWitnessAlert','PersistentContainerOwnership','OwnedContainerRegistry']):
    dest=classes/n;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(archive.read(n))
 (classes/'manifest.json').write_text(json.dumps({'Group':'FirebeardDev','Name':'ExpandedContainerField','Version':'0.7.2-fixture','ServerVersion':'=0.7.2','Main':'com.local.pickpocket.ExpandedContainerField','Dependencies':{'Local:HyThief':'*'}}))
 subprocess.run([str(JDK/'jar.exe'),'--create','--file',str(R/'ExpandedContainerField.jar'),'-C',str(classes),'.'],check=True)
def friendly_pack(mods):
 root=mods/'ContainerPassiveClientFixture';root.mkdir();edits=[]
 targets={'HyThiefStealth_'+a['Role'] for a in STATIONS}|{'Skeleton_Fighter'}
 with zipfile.ZipFile(JAR) as archive:
  for n in archive.namelist():
   if not n.startswith('Server/NPC/Roles/') or not n.endswith('.json') or Path(n).stem not in targets:continue
   original=json.loads(archive.read(n));a=json.loads(json.dumps(original))
   if isinstance(a.get('DefaultPlayerAttitude'),str) or Path(n).stem=='HyThiefStealth_Goblin_Scrapper':a['DefaultPlayerAttitude']='Friendly'
   if 'DefaultPlayerAttitude' in a.get('Parameters',{}):a['Parameters']['DefaultPlayerAttitude']['Value']='Friendly'
   def stripped(x):
    x=json.loads(json.dumps(x));x.pop('DefaultPlayerAttitude',None);x.get('Parameters',{}).pop('DefaultPlayerAttitude',None);return x
   assert a!=original and stripped(a)==stripped(original),'Not an attitude-only proof copy'
   dest=root/n;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(json.dumps(a,indent=2)+'\n');edits.append(n)
 assert len(edits)==6,edits
 (root/'manifest.json').write_text(json.dumps({'Group':'FirebeardDev','Name':'ContainerPassiveClientFixture','Version':'1.0.0','ServerVersion':'=0.7.2','IncludesAssetPack':True,'Dependencies':{'Local:HyThief':'*'}},indent=2)+'\n')
 (R/'passive-config-proof.json').write_text(json.dumps({'OnlySettingChanged':'DefaultPlayerAttitude=Friendly','NativeMovementPerceptionCombatUntouched':True,'Assets':edits},indent=2))
def marker_for(run):
 path=run/'container-theft-test-world.json';a=json.loads(path.read_text(encoding='utf-8-sig'));a['DiagnosticVersion']='1.3.1-container.12';a['FixtureVersion']='1.1.0';a['LayoutVersion']=10;a['ChestCount']=9;a['NPCCount']=9;a['WitnessRoles']+= [s['Role'] for s in STATIONS if s['Role'] not in a['WitnessRoles']];a['PassiveHumanoidStations']=STATIONS;a['PassiveWitnessDistance']=8;a['OptionalPeers']=False;path.write_text(json.dumps(a,indent=2)+'\n')
def run(mode,label,active=False):
 assert re.fullmatch('[A-Za-z0-9_-]+',label)
 dest=R/('Run-'+label);assert not dest.exists(),'Preserve prior run; choose a unique label'
 source=SAVES/'HyThief Container Theft Test World' if active else R/'Rollback/Master' if mode=='author' else R/'DraftMaster'
 shutil.copytree(source,dest);shutil.copy2(JAR,dest/'mods'/JAR.name);shutil.copy2(R/'ExpandedContainerField.jar',dest/'mods/ExpandedContainerField.jar')
 if mode=='author':friendly_pack(dest/'mods');marker_for(dest)
 shutil.copy2(R/'passive-stations.json',dest/'passive-stations.json')
 if mode!='author':shutil.copy2(R/'Run-author/original-area.sha256',dest/'original-area.sha256')
 cfg=json.loads((dest/'config.json').read_text(encoding='utf-8-sig'));cfg['Mods']['FirebeardDev:ExpandedContainerField']={'Enabled':True};cfg['Mods']['FirebeardDev:ContainerPassiveClientFixture']={'Enabled':True};(dest/'config.json').write_text(json.dumps(cfg,indent=2))
 wc=dest/'universe/worlds/default/config.json';a=json.loads(wc.read_text(encoding='utf-8-sig'));a['IsTicking']=mode.startswith('response-');wc.write_text(json.dumps(a,indent=2))
 port=15590+(next((i+1 for i,s in enumerate(STATIONS) if mode=='response-'+s['Role']),0))
 subprocess.run([str(JDK/'java.exe'),'-Xmx2G','-Xms128m','-XX:ActiveProcessorCount=4','-Dfield.mode='+mode,'-jar',str(SERVER),'--assets',str(GAME/'Assets.zip'),'--auth-mode','offline','--bind','127.0.0.1:'+str(port),'--disable-sentry','--disable-file-watcher','--disable-cpb-build'],cwd=dest,stdout=(dest/'native.log').open('w'),stderr=subprocess.STDOUT,timeout=120,check=True)
 text=(dest/'field-evidence.txt').read_text();print(text);assert 'FAILED' not in text and ('AUTHOR_PASS' if mode=='author' else 'RESPONSE_PASS' if mode.startswith('response-') else 'VERIFY_PASS') in text
 if mode=='author':
  draft=R/'DraftMaster';assert not draft.exists();draft.mkdir()
  for rel in ['bans.json','client_metadata.json','permissions.json','config.json','container-theft-test-world.json']:shutil.copy2(dest/rel,draft/rel)
  shutil.copytree(dest/'universe',draft/'universe');shutil.copytree(R/'Rollback/Master/mods',draft/'mods');shutil.copytree(dest/'mods/ContainerPassiveClientFixture',draft/'mods/ContainerPassiveClientFixture')
  cfg=json.loads((draft/'config.json').read_text());cfg['Mods'].pop('FirebeardDev:ExpandedContainerField',None);(draft/'config.json').write_text(json.dumps(cfg,indent=2)+'\n')
  wc=draft/'universe/worlds/default/config.json';a=json.loads(wc.read_text());a['IsTicking']=True;wc.write_text(json.dumps(a,indent=2)+'\n')
if sys.argv[1]=='setup':
 R.mkdir(exist_ok=True);rollback=R/'Rollback';assert not rollback.exists(),'Inspect existing checkpoint, do not overwrite rollback'
 rollback.mkdir();shutil.copytree(W/'Master',rollback/'Master');shutil.copytree(SAVES/'HyThief Container Theft Test World',rollback/'Active')
 for name in ['Reset-Container-Theft.ps1','Reset-Container-Theft.cmd','README.md','master-checksums.json']:shutil.copy2(W/name,rollback/name)
 protected={str(root):snapshot(root) for root in [P/'src-java',P/'Server',P/'Common',P/'BuildSupport',P/'DevTesting/HyThief_Test_World/Master',SAVES/'HyThief Test World',SAVES/'HyThief Passive Witness Test']}
 (R/'protected-before.json').write_text(json.dumps(protected,indent=2));(R/'passive-stations.json').write_text(json.dumps(STATIONS,indent=2)+'\n');compile_helper();print('Backed up current master/active/reset files; gameplay and other worlds fingerprinted')
elif sys.argv[1]=='compile':compile_helper()
else:run(sys.argv[1],sys.argv[2],len(sys.argv)>3 and sys.argv[3]=='active')
