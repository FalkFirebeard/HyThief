from pathlib import Path
import subprocess,shutil,json,zipfile,sys
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');R=P/'DevTesting/Ownership_Editor_Tool'
J=P.parents[1]/'Toolchains/JDK25/bin';S=P/'.build/dependencies/HytaleServer-0.7.2.jar';JAR=P/'build/libs/HyThief-1.5.0-editor.2.jar'
W=Path(r'E:\Hytale adjustments\Mod Projects\WonderousWardrobes');MASTER=W/'DevTesting/HyThief_Wardrobe_Phase2A/Master'
C=R/'classes';C.mkdir(exist_ok=True)
subprocess.run([str(J/'javac.exe'),'-cp',str(S),'-d',str(C),str(P/'DevTesting/Release_1_4_2/ContainerAccessProof.java')],check=True)
mode=sys.argv[1];label=sys.argv[2];D=R/('Run-'+label);assert not D.exists()
shutil.copytree(MASTER,D,ignore=shutil.ignore_patterns('*.log','logs'))
for f in (D/'mods').glob('*.jar'):
 assert f.resolve().parent==(D/'mods').resolve();f.unlink()
for f in (D/'mods').glob('*.zip'):
 assert f.resolve().parent==(D/'mods').resolve();f.unlink()
shutil.copy2(JAR,D/'mods'/JAR.name)
if mode not in ['hy-alone','access-chest']:shutil.copy2(W/'Builds/Releases/1.4.0/HyStash_v1.4.0.zip',D/'mods/HyStash_v1.4.0.zip')
with zipfile.ZipFile(D/'mods/AccessNativeProof.jar','w') as z:
 z.writestr('manifest.json',json.dumps({'Group':'FirebeardDev','Name':'AccessNativeProof','Version':'1.0.0','ServerVersion':'=0.7.2','Main':'falk.wardrobeproof.ContainerAccessProof','Dependencies':{'Local:HyThief':'*'}}))
 for f in C.rglob('*.class'):z.write(f,f.relative_to(C).as_posix())
cfg=json.loads((D/'config.json').read_bytes());cfg['Mods']['FirebeardDev:AccessNativeProof']={'Enabled':True};cfg['Mods']['FalkFirebeard:HyStash']={'Enabled':mode not in ['hy-alone','access-chest']};(D/'config.json').write_text(json.dumps(cfg,indent=2))
for config_path in [D/'config.json',D/'universe/worlds/default/config.json']:
 data=json.loads(config_path.read_text(encoding='utf-8-sig'))
 def repin(value):
  if isinstance(value,dict):
   for key,entry in value.items():
    if key=='Local:HyThief' and isinstance(entry,str):value[key]='=1.5.0-editor.2'
    else:repin(entry)
  elif isinstance(value,list):
   for entry in value:repin(entry)
 repin(data);config_path.write_text(json.dumps(data,indent=2)+'\n')
with (D/'native.log').open('w') as log:
 result=subprocess.run([str(J/'java.exe'),'-Xmx2G','-XX:ActiveProcessorCount=4','-Dwardrobe.mode='+mode,'-jar',str(S),'--assets',r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip','--auth-mode','offline','--bind','127.0.0.1:15658','--disable-sentry','--disable-file-watcher','--disable-cpb-build'],cwd=D,stdout=log,stderr=subprocess.STDOUT,timeout=210)
txt=(D/'native.log').read_text(errors='replace');markers=[l for l in txt.splitlines() if 'WARDROBE_PHASE2A' in l or 'Exception' in l]
print('\n'.join(markers[-35:]));passed=result.returncode==0 and 'WARDROBE_PHASE2A PASS Mode='+mode in txt and 'WARDROBE_PHASE2A FAILED' not in txt
(R/(label+'-result.json')).write_text(json.dumps({'Passed':passed,'Mode':mode,'Run':str(D),'Markers':markers},indent=2))
assert passed,str(D/'native.log')
