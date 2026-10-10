from pathlib import Path
import hashlib,json,shutil,subprocess,sys
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief')
R=P/'DevTesting/Persistent_Container_Ownership_Proof'
S=Path(__file__).parent
if len(sys.argv)>1:
    candidate=Path(sys.argv[1]).resolve()
    assert candidate.is_relative_to((R/'Runs').resolve()), 'Output must be inside isolated proof Runs'
    R=candidate
assert not R.exists(), 'Proof already exists; inspect it instead of overwriting'
R.mkdir(parents=True)
def inv(p):
    return {str(f.relative_to(p)):hashlib.sha256(f.read_bytes()).hexdigest() for f in p.rglob('*') if f.is_file()}
old=json.loads((P/'DevTesting/Production_Ownership_Reference_0_6_8/protected-before.json').read_text())
paths=list(map(Path,old))
paths += [P/'Common',P/'config',P/'DevTesting/HyThief_Test_World']
before={str(p):inv(p) for p in paths if p.exists()}
(R/'protected-before.json').write_text(json.dumps(before,indent=2))
for n in ['OwnershipProof.java','OwnershipProofData.java','prepare.py']:
    shutil.copy2(S/n,R/n)
manifest={'Group':'FirebeardDev','Name':'HyThiefOwnershipProof','Version':'0.1.0','ServerVersion':'=0.6.8','Main':'com.local.hythief.devownership.OwnershipProof','Dependencies':{'Local:HyThief':'*'}}
(R/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
classes=R/'classes'; classes.mkdir()
jdk=Path(r'E:\Hytale adjustments\Toolchains\JDK25\bin')
server=P/'.build/dependencies/HytaleServer-0.6.8.jar'
assert hashlib.sha256(server.read_bytes()).hexdigest()=='dcd2956cc65b950084650eadd56b889cf51f7610c127c3daac1f74dc471fec97'
subprocess.run([str(jdk/'javac.exe'),'--release','25','-cp',str(server),'-d',str(classes),str(R/'OwnershipProofData.java'),str(R/'OwnershipProof.java')],check=True)
subprocess.run([str(jdk/'jar.exe'),'--create','--file',str(R/'HyThiefOwnershipProof-0.1.0.jar'),'-C',str(classes),'.','-C',str(R),'manifest.json'],check=True)
F=R/'Fixture'
shutil.copytree(P/'DevTesting/Container_Theft_Test_World/Master',F)
shutil.copy2(R/'HyThiefOwnershipProof-0.1.0.jar',F/'mods/HyThiefOwnershipProof-0.1.0.jar')
mod=Path(r'C:\Users\askin\AppData\Roaming\Hytale\UserData\Mods\HyThief-1.3.1-container.8.jar')
shutil.copy2(mod,F/'mods'/mod.name)
cfg=json.loads((F/'config.json').read_text(encoding='utf-8-sig'))
cfg['Mods']['FirebeardDev:HyThiefOwnershipProof']={'Enabled':True}
(F/'config.json').write_text(json.dumps(cfg,indent=2)+'\n')
wc=F/'universe/worlds/default/config.json'
w=json.loads(wc.read_text(encoding='utf-8-sig'));w['DeleteOnRemove']=False
wc.write_text(json.dumps(w,indent=2)+'\n')
(F/'OWNERSHIP_PROOF_FIXTURE').write_text('Isolated native persistence fixture. Never point at live Saves.\n')
print('Prepared isolated proof',R)
