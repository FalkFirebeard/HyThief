from pathlib import Path
import json,shutil,hashlib,subprocess,sys
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');W=P/'DevTesting/Container_Theft_Test_World';R=W/'Stationary_Witness_0_7_2';PREV=W/'Passive_Client_Expansion_0_7_2'
PY=Path(sys.executable); GIT=Path(r'C:\Users\askin\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\git\cmd\git.exe')
SAVES=Path(r'C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves');ACTIVE=SAVES/'HyThief Container Theft Test World'
sha=lambda f:hashlib.sha256(f.read_bytes()).hexdigest()
snap=lambda root:{str(f.relative_to(root)):sha(f) for f in root.rglob('*') if f.is_file()}
def write(f,a):f.write_bytes((json.dumps(a,indent=2)+'\n').encode())
def walk(a):
 if isinstance(a,dict):
  if a.get('Type')=='State' and isinstance(a.get('State'),str) and not a['State'].startswith('.'):yield a['State']
  for v in a.values():yield from walk(v)
 elif isinstance(a,list):
  for v in a:yield from walk(v)
if sys.argv[1]=='inspect':
 for f in (W/'Master/mods/ContainerPassiveClientFixture').rglob('HyThiefStealth*.json'):print(f.name,sorted(set(walk(json.loads(f.read_text())['Instructions']))))
elif sys.argv[1] in ['prepare','resume-preparation']:
 if sys.argv[1]=='prepare':
  assert not R.exists(),'Preserve checkpoint; inspect before reuse'
  R.mkdir();(R/'Rollback').mkdir();shutil.copytree(W/'Master',R/'Rollback/Master');shutil.copytree(ACTIVE,R/'Rollback/Active')
  for name in ['README.md','master-checksums.json','Reset-Container-Theft.cmd','Reset-Container-Theft.ps1']:shutil.copy2(W/name,R/'Rollback'/name)
 else:assert (R/'Rollback/Master').exists() and not (R/'DraftMaster').exists()
 protected={}
 for root in [P/'src-java',P/'Server',P/'Common',P/'DevTesting/HyThief_Test_World/Master',SAVES/'HyThief Test World',SAVES/'HyThief Passive Witness Test']:
  print('Fingerprinting',root,flush=True);protected[str(root)]=snap(root);print('Files',len(protected[str(root)]),flush=True)
 write(R/'protected-before.json',protected)
 shutil.copytree(W/'Master',R/'DraftMaster');shutil.copy2(PREV/'passive-stations.json',R/'passive-stations.json')
 proof=[]
 for f in (R/'DraftMaster/mods/ContainerPassiveClientFixture').rglob('HyThiefStealth*.json'):
  a=json.loads(f.read_text());original=json.loads(f.read_text())
  available=sorted(set(walk(a['Instructions'])))
  states=[x for x in available if '.' not in x and (x.startswith('Idle') or x in ['NeutralPlayer','Revered','Sleep'])]
  assert 'Idle' in states
  hold={'$Comment':'Development fixture only: reserve idle head/body motion while continuing native perception, actions and state transitions. Combat/chase motions are not reserved.','Continue':True,'Sensor':{'Type':'Or','Sensors':[{'Type':'State','State':state} for state in states]},'HeadMotion':{'Type':'Nothing'},'BodyMotion':{'Type':'Nothing'}}
  a['Instructions'].insert(0,hold)
  assert a['Instructions'][1:]==original['Instructions']
  copy=json.loads(json.dumps(a));copy['Instructions'].pop(0);assert copy==original
  write(f,a);proof.append({'File':str(f.relative_to(R/'DraftMaster')),'HeldStates':states,'OnlyChange':'Prepend Continue=true idle-only Nothing head/body motions','NativeInstructionsUnchanged':True})
 assert len(proof)==5
 write(R/'asset-diff-proof.json',proof)
 print(json.dumps(proof,indent=2))
