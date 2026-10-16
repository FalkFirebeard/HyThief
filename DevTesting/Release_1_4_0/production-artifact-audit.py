from pathlib import Path
import json,zipfile,hashlib
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');R=P/'DevTesting/Release_1_4_0';old=P/'.build/public-release/1.3.3/HyThief-1.3.3.jar';new=P/'build/libs/HyThief-1.4.0.jar'
sha=lambda f:hashlib.sha256(f.read_bytes()).hexdigest()
assert sha(old)=='3c39a85557edd8504f5d8e080762b9028be135049a504b372106fd89b626b13d'
a=zipfile.ZipFile(old);b=zipfile.ZipFile(new);an=set(a.namelist());bn=set(b.namelist());added=sorted(bn-an);removed=sorted(an-bn);changed=sorted(n for n in an&bn if a.read(n)!=b.read(n))
assert removed==['Server/NPC/Spawn/Markers/Kweebec_Merchant_Test.json'],removed
container_classes={'ContainerCaughtNotification','ContainerOwnershipPolicy','ContainerPacketBridge','ContainerRemovalProbe','ContainerVisualWitness','ContainerKey','ContainerWitnessProfiles','ContainerTheftService','PersistentContainerOwnership','OwnershipProofData','HyThiefPersonalWitnessAlert','HyThiefContainerAlert','HyThiefGuardAlert'}
def basename(n):return n.rsplit('/',1)[-1].removesuffix('.class').split('$')[0]
def allowed_added(n):return n.endswith('/') or n.startswith('Server/Item/Block/Spawners/') or n.startswith('Server/NPC/Groups/HyThief/HyThief_Container_') or (n.startswith('com/local/pickpocket/') and basename(n) in container_classes)
assert all(allowed_added(n) for n in added),[n for n in added if not allowed_added(n)]
def strip_local(x):
 if isinstance(x,dict):
  if x.get('Type')=='Or' and len(x.get('Sensors',[]))==2 and x['Sensors'][1].get('Message')=='HyThief_Local_Theft':return strip_local(x['Sensors'][0])
  return {k:strip_local(v) for k,v in x.items()}
 if isinstance(x,list):return [strip_local(v) for v in x]
 return x
unexpected=[];line_endings=[]
for n in changed:
 if a.read(n).replace(b'\r\n',b'\n')==b.read(n).replace(b'\r\n',b'\n'):
  line_endings.append(n);continue
 if n=='manifest.json':continue
 if n.startswith('com/local/pickpocket/PickpocketPlugin') and n.endswith('.class'):continue
 if n.startswith('com/local/pickpocket/HyThiefGuardAlert') and n.endswith('.class'):continue # baseline container adapter methods, public caught path retained
 if n.startswith('Server/NPC/Roles/HyThief/Stealth/HyThiefStealth_') and any(n.endswith('_'+r+'.json') for r in ['Kweebec_Razorleaf','Kweebec_Razorleaf_Patrol','Feran_Sharptooth','Feran_Longtooth']):
  assert strip_local(json.loads(b.read(n)))==json.loads(a.read(n)),n
  continue
 unexpected.append(n)
assert not unexpected,unexpected[:30]
manifest=json.loads(b.read('manifest.json'));assert manifest['Version']=='1.4.0' and manifest['ServerVersion']=='=0.7.2'
for forbidden in ['DevContainerTestWorld','HyThiefContainerDiagnostics','OwnedContainerRegistry','ContainerTheftFixture','HyThiefWitness','DevTesting/','Testing/']:
 assert not any(forbidden in n for n in bn),forbidden
 for n in bn:
  if n.endswith(('.class','.json')):assert forbidden.encode() not in b.read(n),(forbidden,n)
native=zipfile.ZipFile(r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip')
for n in added:
 if n.startswith('Server/Item/Block/Spawners/') and n.endswith('.json'):
  x=json.loads(b.read(n))
  for e in x['Entries']:e.get('Components',{}).get('Components',{}).pop('HyThiefOwnershipProof',None)
  assert x==json.loads(native.read(n)),n
result={'PreviousPublicSHA256':sha(old),'CandidateSHA256':sha(new),'Added':added,'Changed':changed,'Removed':removed,'UnexpectedContent':unexpected,'FlaggedExistingLFvsPublicCRLF':line_endings,'LineEndingExplanation':f'Current development inputs are LF; approved public 1.3.3 had CRLF for these {len(line_endings)} resources. Normalized bytes are identical. No source reformat or gameplay change was made to these inputs.','NativeSpawnerContentsPreserved':True,'PublicPickpocketClassesUnchangedExceptPluginLifecycleAndExistingGuardAdapter':True,'NoFixtureClassesOrIDs':True}
(R/'artifact-comparison.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps({'SHA256':sha(new),'Added':len(added),'Changed':len(changed),'LineEndingsOnlyFlagged':len(line_endings),'UnexpectedContent':unexpected},indent=2))
