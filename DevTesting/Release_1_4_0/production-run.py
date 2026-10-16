from pathlib import Path
import subprocess,shutil,zipfile,json,sys,re,hashlib
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');R=P/'DevTesting/Release_1_4_0';W=P/'DevTesting/Container_Theft_Test_World';JDK=P.parents[1]/'Toolchains/JDK25/bin';SERVER=P/'.build/dependencies/HytaleServer-0.7.2.jar';JAR=P/'build/libs/HyThief-1.4.0.jar';GAME=Path(r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest')
def write(f,a):f.write_text(json.dumps(a,indent=2)+'\n',encoding='utf-8')
def compile():
 classes=R/'classes';classes.mkdir(exist_ok=True)
 files=['ExpandedContainerField.java','FactionFieldBuilder.java','NativeFieldBlocks.java']
 if (R/'ProductionContainerProof.java').exists():files.append('ProductionContainerProof.java')
 subprocess.run([str(JDK/'javac.exe'),'-cp',str(SERVER)+';'+str(JAR),'-d',str(classes)]+[str(R/f) for f in files],check=True)
 with zipfile.ZipFile(JAR) as z:
  for n in z.namelist():
   if n.startswith('com/local/pickpocket/') and n.endswith('.class') and n.rsplit('/',1)[-1].removesuffix('.class').split('$')[0] in ['ContainerKey','ContainerWitnessProfiles','ContainerVisualWitness','ContainerRemovalProbe','ContainerCaughtNotification','ContainerOwnershipPolicy','HyThiefPersonalWitnessAlert','PersistentContainerOwnership','HyThiefContainerAlert','HyThiefGuardAlert','HyThiefStealthAcquisition']:
    f=classes/n;f.parent.mkdir(parents=True,exist_ok=True);f.write_bytes(z.read(n))
 print('Helper compiled')
def run(mode,label,restart=None):
 assert re.fullmatch('[A-Za-z0-9_-]+',label)
 dest=R/('Run-'+label);assert not dest.exists()
 if mode.startswith('production') and not restart:
  (dest/'universe/worlds/default').mkdir(parents=True);(dest/'mods').mkdir()
  shutil.copy2(W/'Master/config.json',dest/'config.json');shutil.copy2(W/'Master/universe/worlds/default/config.json',dest/'universe/worlds/default/config.json')
 else:shutil.copytree(Path(restart) if restart else W/'Master',dest)
 if restart:
  for name in ['native.log','field-evidence.txt','faction-verification.json','persistence-verification.json']:
   if (dest/name).exists():(dest/name).unlink()
 shutil.copy2(JAR,dest/'mods'/JAR.name)
 peer=mode.startswith('peer-');prod=mode.startswith('production');cls='ExpandedContainerField' if peer else 'ProductionContainerProof' if prod else 'FactionFieldBuilder'
 classes=R/'classes';write(classes/'manifest.json',{'Group':'FirebeardDev','Name':cls,'Version':'1.4.0-candidate-proof','ServerVersion':'=0.7.2','Main':'com.local.pickpocket.'+cls,'Dependencies':{'Local:HyThief':'*'}})
 subprocess.run([str(JDK/'jar.exe'),'--create','--file',str(dest/'mods/ContainerCandidateProof.jar'),'-C',str(classes),'.'],check=True)
 cfg=json.loads((dest/'config.json').read_text(encoding='utf-8-sig'));cfg['Mods']['FirebeardDev:'+cls]={'Enabled':True};write(dest/'config.json',cfg)
 wc=dest/'universe/worlds/default/config.json';a=json.loads(wc.read_text(encoding='utf-8-sig'));a['IsTicking']=mode!='peer-verify' and (not prod or 'reaction' in mode);write(wc,a)
 for name in ['passive-stations.json','production-witness-roles.json','production-ownership-map.json']:shutil.copy2(R/name,dest/name)
 old=W/'Secondary_Peer_Field_0_7_2';shutil.copy2(old/'Run-author/primary-poses.txt',dest/'primary-poses.txt');shutil.copy2(W/'Passive_Client_Expansion_0_7_2/Run-author/original-area.sha256',dest/'original-area.sha256')
 opts=['-Dfield.mode='+mode] if peer else ['-Dproduction.mode='+mode] if prod else ['-Dfactionfield.author=false','-Dfactionfield.response='+(mode if mode in ['Kweebec','Feran','LocalBoth','LocalKweebec','LocalFeran'] else ''),'-Dfactionfield.persistence='+str(mode=='persistence').lower(),'-Dfactionfield.expectPersistence='+str(mode=='restart').lower()]
 subprocess.run([str(JDK/'java.exe'),'-Xmx2G','-Xms128m','-XX:ActiveProcessorCount=4']+opts+['-jar',str(SERVER),'--assets',str(GAME/'Assets.zip'),'--auth-mode','offline','--bind','127.0.0.1:15645','--disable-sentry','--disable-file-watcher','--disable-cpb-build'],cwd=dest,stdout=(dest/'native.log').open('w'),stderr=subprocess.STDOUT,timeout=240,check=True)
 log=(dest/'native.log').read_text(errors='replace');assert not re.search(r'\[NPC\|P\] FAIL:|FACTION_FIELD_FAILED|EXPANDED_FIELD FAILED|PRODUCTION_PROOF_FAILED',log)
 if mode.startswith('peer-response-'):assert re.search(r'FAMILY_RESULT Role='+re.escape(mode.removeprefix('peer-response-'))+r'[^\r\n]*PrimaryReacted=true',log),'Witness failed to enter native combat'
 if mode in ['Kweebec','Feran','LocalBoth','LocalKweebec','LocalFeran']:assert 'FACTION_NATIVE_RESPONSE_PASS' in log
 if mode=='persistence':assert 'OWNERSHIP_PERSISTENCE_PASS Boundary=nativeChunkUnloadReload' in log
 if mode=='restart':assert 'OWNERSHIP_PERSISTENCE_PASS Boundary=fullServerRestart' in log
 markers=[line for line in log.splitlines() if any(x in line for x in ['_PASS','FIELD_VERIFIED','FAMILY_RESULT','ALL_PEER_FIXTURES_VALID','PRODUCTION_ROLE','PRODUCTION_TABLE'])]
 print('\n'.join(markers));assert markers,'Missing proof result';print('RUN='+str(dest))
if __name__=='__main__':
 if sys.argv[1]=='compile':compile()
 else:run(sys.argv[1],sys.argv[2],sys.argv[3] if len(sys.argv)>3 else None)
