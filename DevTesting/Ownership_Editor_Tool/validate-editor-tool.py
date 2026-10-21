from pathlib import Path
import subprocess,shutil,json,zipfile,sys
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');R=P/'DevTesting/Ownership_Editor_Tool'
J=P.parents[1]/'Toolchains/JDK25/bin';S=P/'.build/dependencies/HytaleServer-0.7.2.jar';JAR=P/'build/libs/HyThief-1.5.0-editor.2.jar'
mode,label=sys.argv[1:3];D=R/('Run-'+label);assert not D.exists();shutil.copytree(Path(sys.argv[3]) if len(sys.argv)>3 else P/'DevTesting/Ownership_Editor_Phase2A/Master',D,ignore=shutil.ignore_patterns('logs','native.log','proof-classes'))
C=D/'proof-classes';C.mkdir();(D/'mods').mkdir(exist_ok=True)
for f in (D/'mods').glob('*.jar'):f.unlink()
subprocess.run([str(J/'javac.exe'),'-cp',str(S),'-d',str(C),str(R/'OwnershipEditorProof.java')],check=True)
with zipfile.ZipFile(D/'mods/EditorToolProof.jar','w') as z:
 z.writestr('manifest.json',json.dumps({'Group':'FirebeardDev','Name':'EditorToolProof','Version':'1.0.0','ServerVersion':'=0.7.2','Main':'falk.wardrobeproof.OwnershipEditorProof','Dependencies':{'Local:HyThief':'*'}}))
 for f in C.rglob('*.class'):z.write(f,f.relative_to(C).as_posix())
shutil.copy2(JAR,D/'mods'/JAR.name)
shutil.copy2(Path(r'E:\Hytale adjustments\Mod Projects\WonderousWardrobes\Builds\Releases\1.4.0\HyStash_v1.4.0.zip'),D/'mods/HyStash_v1.4.0.zip')
cfg=json.loads((D/'config.json').read_text(encoding='utf-8-sig'));cfg['Mods']['FirebeardDev:EditorToolProof']={'Enabled':True};(D/'config.json').write_text(json.dumps(cfg,indent=2))
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
 result=subprocess.run([str(J/'java.exe'),'-Xmx2G','-XX:ActiveProcessorCount=4','-Dwardrobe.mode='+mode,'-jar',str(S),'--assets',r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip','--auth-mode','offline','--bind',('127.0.0.1:15676' if mode=='tool' else ('127.0.0.1:15678' if mode=='editor-command' else '127.0.0.1:15677')),'--disable-sentry','--disable-file-watcher','--disable-cpb-build'],cwd=D,stdout=log,stderr=subprocess.STDOUT,timeout=210)
txt=(D/'native.log').read_text(errors='replace');markers=[l for l in txt.splitlines() if 'OWNERSHIP_EDITOR' in l]
passed=result.returncode==0 and 'OWNERSHIP_EDITOR PASS Mode='+mode in txt and 'OWNERSHIP_EDITOR FAILED' not in txt
(R/(label+'-result.json')).write_text(json.dumps({'Passed':passed,'Mode':mode,'Run':str(D),'Markers':markers},indent=2));print('\n'.join(markers));assert passed,str(D/'native.log')
