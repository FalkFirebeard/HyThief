from pathlib import Path
import sys,shutil,json,zipfile,hashlib,subprocess,re
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');W=P/'DevTesting/Container_Theft_Test_World';R=W/'Secondary_Peer_Field_0_7_2'
PREV=W/'Stationary_Witness_0_7_2';JDK=P.parents[1]/'Toolchains/JDK25/bin';SERVER=P/'.build/dependencies/HytaleServer-0.7.2.jar';JAR=P/'DevTesting/Passive_Witness_0_7_2/Candidate/HyThief-1.3.1-container.12.jar'
GAME=Path(r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest');SAVES=Path(r'C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves');ACTIVE=SAVES/'HyThief Container Theft Test World'
sha=lambda f:hashlib.sha256(f.read_bytes()).hexdigest()
snap=lambda root:{str(f.relative_to(root)):sha(f) for f in root.rglob('*') if f.is_file()}
def write(f,a):f.write_bytes((json.dumps(a,indent=2)+'\n').encode())
assert sha(GAME/'Server/HytaleServer.jar')==sha(SERVER)=='23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff'
assert sha(JAR)=='6b4c1500428674b4deb09c116ca6eb7d37360f9137e9a39ac527068d79dbe800'
with zipfile.ZipFile(SERVER) as z:
 mf=z.read('META-INF/MANIFEST.MF').decode();assert 'Implementation-Version: 0.7.2' in mf and 'd769dc3d57881261a62858e1394f0e4de9a21eca' in mf
def compile():
 for name in ['ExpandedContainerField.java','NativeFieldBlocks.java']:
  src=Path(__file__).parent/('PeerContainerField.java' if name.startswith('Expanded') else name)
  if not src.exists():src=Path(__file__).parent/name if (Path(__file__).parent/name).exists() else PREV/name
  if src.resolve()!=(R/name).resolve():shutil.copy2(src,R/name)
 shutil.copy2(Path(__file__),R/'Run-Peer-Field.py') if Path(__file__).resolve()!=(R/'Run-Peer-Field.py').resolve() else None
 classes=R/'classes';classes.mkdir(exist_ok=True)
 subprocess.run([str(JDK/'javac.exe'),'-cp',str(SERVER)+';'+str(JAR),'-d',str(classes),str(R/'ExpandedContainerField.java'),str(R/'NativeFieldBlocks.java')],check=True)
 with zipfile.ZipFile(JAR) as z:
  for n in z.namelist():
   if n.startswith('com/local/pickpocket/') and n.endswith('.class') and any(n.split('/')[-1].startswith(a) for a in ['ContainerVisualWitness','ContainerRemovalProbe','ContainerCaughtNotification','ContainerOwnershipPolicy','HyThiefPersonalWitnessAlert','PersistentContainerOwnership','OwnedContainerRegistry']):
    dest=classes/n;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(z.read(n))
 write(classes/'manifest.json',{'Group':'FirebeardDev','Name':'ExpandedContainerField','Version':'0.7.2-peer-fixture','ServerVersion':'=0.7.2','Main':'com.local.pickpocket.ExpandedContainerField','Dependencies':{'Local:HyThief':'*'}})
 subprocess.run([str(JDK/'jar.exe'),'--create','--file',str(R/'ExpandedContainerField.jar'),'-C',str(classes),'.'],check=True)
def run(mode,label,active=False):
 assert re.fullmatch('[A-Za-z0-9_-]+',label)
 dest=R/('Run-'+label);assert not dest.exists()
 source=ACTIVE if active else R/'Rollback/Master' if mode=='peer-author' else R/'DraftMaster'
 shutil.copytree(source,dest);shutil.copy2(JAR,dest/'mods'/JAR.name);shutil.copy2(R/'ExpandedContainerField.jar',dest/'mods/ExpandedContainerField.jar');shutil.copy2(R/'passive-stations.json',dest/'passive-stations.json')
 if mode=='peer-author':
  with zipfile.ZipFile(JAR) as z:
   name='Server/NPC/Roles/HyThief/Stealth/HyThiefStealth_Trork_Sentry.json';a=json.loads(z.read(name));original=json.loads(z.read(name))
  assert a['DefaultPlayerAttitude']=='Hostile';a['DefaultPlayerAttitude']='Friendly'
  hold=json.loads((W/'Master/mods/ContainerPassiveClientFixture/Server/NPC/Roles/HyThief/Stealth/HyThiefStealth_Trork_Warrior.json').read_text())['Instructions'][0]
  a['Instructions'].insert(0,hold);path=dest/'mods/ContainerPassiveClientFixture'/name;write(path,a)
  check=json.loads(json.dumps(a));check['DefaultPlayerAttitude']='Hostile';check['Instructions'].pop(0);assert check==original
  write(R/'sentry-fixture-diff.json',{'Asset':name,'OnlyChanges':['DefaultPlayerAttitude=Friendly','Prepend native idle-only head/body Nothing hold'],'NativeSparBranch':'Spar immediately returns to Idle; no spar partner broadcast','CombatPerceptionAlertInstructionsUnchanged':True,'PrimaryAssetsUntouched':True})
 shutil.copy2(W/'Passive_Client_Expansion_0_7_2/Run-author/original-area.sha256',dest/'original-area.sha256')
 if mode!='peer-author':shutil.copy2(R/'Run-author/primary-poses.txt',dest/'primary-poses.txt')
 cfg=json.loads((dest/'config.json').read_text(encoding='utf-8-sig'));cfg['Mods']['FirebeardDev:ExpandedContainerField']={'Enabled':True};write(dest/'config.json',cfg)
 wc=dest/'universe/worlds/default/config.json';a=json.loads(wc.read_text(encoding='utf-8-sig'));a['IsTicking']=mode not in ['peer-author','peer-verify'];write(wc,a)
 stations=json.loads((R/'passive-stations.json').read_text());port=15600+next((i+1 for i,s in enumerate(stations) if mode=='peer-response-'+s['Role']),0)
 subprocess.run([str(JDK/'java.exe'),'-Xmx2G','-Xms128m','-XX:ActiveProcessorCount=4','-Dfield.mode='+mode,'-jar',str(SERVER),'--assets',str(GAME/'Assets.zip'),'--auth-mode','offline','--bind','127.0.0.1:'+str(port),'--disable-sentry','--disable-file-watcher','--disable-cpb-build'],cwd=dest,stdout=(dest/'native.log').open('w'),stderr=subprocess.STDOUT,timeout=210,check=True)
 text=(dest/'field-evidence.txt').read_text();print(text);assert 'FAILED' not in text and 'PEER_'+('AUTHOR' if mode=='peer-author' else 'VERIFY' if mode=='peer-verify' else 'IDLE' if mode=='peer-idle' else 'RESPONSE')+'_PASS' in text
 assert not re.search(r'\[NPC\|P\] FAIL:',(dest/'native.log').read_text()),'Native role validation failed'
 if mode=='peer-author':
  draft=R/'DraftMaster';assert not draft.exists();shutil.copytree(R/'Rollback/Master',draft,ignore=shutil.ignore_patterns('universe'))
  shutil.copytree(dest/'universe',draft/'universe')
  extra='mods/ContainerPassiveClientFixture/Server/NPC/Roles/HyThief/Stealth/HyThiefStealth_Trork_Sentry.json';shutil.copy2(dest/extra,draft/extra)
  # Keep original native metadata/player/time bytes; author only adds walls and five entities.
  for f in (R/'Rollback/Master/universe').rglob('*'):
   if f.is_file() and (f.name.endswith('.json') or f.name.endswith('.json.bak')):shutil.copy2(f,draft/f.relative_to(R/'Rollback/Master'))
  for f in list((draft/'universe').rglob('*.bak')):
   if not (R/'Rollback/Master'/f.relative_to(draft)).exists():f.unlink()
  marker=json.loads((draft/'container-theft-test-world.json').read_text());marker.update({'NPCCount':14,'OptionalPeers':True,'PermanentLOSWalls':True,'SecondaryPeers':{'Count':5,'PrimaryOffset':[6,0,0],'Yaw':0,'WallOffsetX':[1,4],'WallY':[64,65],'WallOffsetZ':1,'NativeBroadcastOnly':True,'TrorkPeerRole':'Trork_Sentry','OtherPeerRoles':'Same as primary'}});marker['WitnessRoles'].append('Trork_Sentry');write(draft/'container-theft-test-world.json',marker)
if sys.argv[1]=='setup':
 assert not R.exists();R.mkdir();(R/'Rollback').mkdir();shutil.copytree(W/'Master',R/'Rollback/Master');shutil.copytree(ACTIVE,R/'Rollback/Active')
 for name in ['Reset-Container-Theft.cmd','Reset-Container-Theft.ps1','README.md','master-checksums.json']:shutil.copy2(W/name,R/'Rollback'/name)
 protected={str(root):snap(root) for root in [P/'src-java',P/'Server',P/'Common',P/'DevTesting/HyThief_Test_World/Master',SAVES/'HyThief Test World',SAVES/'HyThief Passive Witness Test']};write(R/'protected-before.json',protected)
 shutil.copy2(PREV/'passive-stations.json',R/'passive-stations.json');compile()
elif sys.argv[1]=='compile':compile()
else:run(sys.argv[1],sys.argv[2],len(sys.argv)>3 and sys.argv[3]=='active')
