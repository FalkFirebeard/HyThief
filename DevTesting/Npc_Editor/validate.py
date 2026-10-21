from pathlib import Path
import subprocess,shutil,json,zipfile,sys
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');R=P/'DevTesting/Npc_Editor';J=P.parents[1]/'Toolchains/JDK25/bin';S=P/'.build/dependencies/HytaleServer-0.7.2.jar';JAR=P/'build/libs/HyThief-1.5.0-editor.3.jar'
mode,label=sys.argv[1:3];D=R/('Run-'+label);assert not D.exists();shutil.copytree(Path(sys.argv[3]) if len(sys.argv)>3 else P/'DevTesting/Ownership_Editor_Phase2A/Master',D,ignore=shutil.ignore_patterns('logs','native.log','proof-classes'))
C=D/'proof-classes';C.mkdir();(D/'mods').mkdir(exist_ok=True)
for f in (D/'mods').glob('*.jar'):f.unlink()
src=(P/'DevTesting/Ownership_Editor_Tool/OwnershipEditorProof.java').read_text().replace('if(mode.equals("tool")){tool();return;}','if(mode.startsWith("npc")){npcTest();return;}\n    if(mode.equals("tool")){tool();return;}')
src=src.replace('  void tool()throws Exception {',(R/'npc-proof-methods.java.inc').read_text()+'\n  void tool()throws Exception {')
if mode=='editor-command':src=src.replace('give HyThief_Ownership_Editor_Tool','give HyThief_NPC_Editor_Tool').replace('getItemId().equals("HyThief_Ownership_Editor_Tool")','getItemId().equals("HyThief_NPC_Editor_Tool")')
source=D/'OwnershipEditorProof.java';source.write_text(src)
subprocess.run([str(J/'javac.exe'),'-cp',str(S)+';'+str(JAR),'-d',str(C),str(source)],check=True)
with zipfile.ZipFile(D/'mods/NpcEditorProof.jar','w') as z:
 z.writestr('manifest.json',json.dumps({'Group':'FirebeardDev','Name':'NpcEditorProof','Version':'1.0.0','ServerVersion':'=0.7.2','Main':'falk.wardrobeproof.OwnershipEditorProof','Dependencies':{'Local:HyThief':'*','Hytale:BuilderTools':'*'}}))
 for f in C.rglob('*.class'):z.write(f,f.relative_to(C).as_posix())
if mode=='npc-metadata':
 fixture=D/'mods/MetadataResearch';(fixture/'Server/NPC/Groups').mkdir(parents=True);(fixture/'Server/NPC/Roles').mkdir(parents=True)
 (fixture/'manifest.json').write_text(json.dumps({'Group':'FirebeardDev','Name':'MetadataResearch','Version':'1.0.0','ServerVersion':'=0.7.2','IncludesAssetPack':True}))
 (fixture/'Server/NPC/Groups/HyThiefResearch_Lizardfolk.json').write_text(json.dumps({'IncludeRoles':['HyThiefResearch_Lizardfolk']}))
 (fixture/'Server/NPC/Roles/HyThiefResearch_Lizardfolk.json').write_text(json.dumps({'Type':'Variant','Reference':'Kweebec_Merchant'}))
shutil.copy2(JAR,D/'mods'/JAR.name);shutil.copy2(Path(r'E:\Hytale adjustments\Mod Projects\WonderousWardrobes\Builds\Releases\1.4.0\HyStash_v1.4.0.zip'),D/'mods/HyStash_v1.4.0.zip')
for cp in [D/'config.json',D/'universe/worlds/default/config.json']:
 data=json.loads(cp.read_text(encoding='utf-8-sig'))
 if cp==D/'config.json':data['Mods']['FirebeardDev:NpcEditorProof']={'Enabled':True};data['Mods']['FirebeardDev:MetadataResearch']={'Enabled':mode=='npc-metadata'}
 else:data['RequiredPlugins']['Local:HyThief']='=1.5.0-editor.3'
 cp.write_text(json.dumps(data,indent=2))
with (D/'native.log').open('w') as log:
 result=subprocess.run([str(J/'java.exe'),'-Xmx2G','-XX:ActiveProcessorCount=4','-Dwardrobe.mode='+mode,'-jar',str(S),'--assets',r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip','--auth-mode','offline','--bind',('127.0.0.1:15686' if mode.startswith('npc') else '127.0.0.1:15687'),'--disable-sentry','--disable-file-watcher','--disable-cpb-build'],cwd=D,stdout=log,stderr=subprocess.STDOUT,timeout=210)
txt=(D/'native.log').read_text(errors='replace');markers=[l for l in txt.splitlines() if 'OWNERSHIP_EDITOR' in l];passed=result.returncode==0 and 'OWNERSHIP_EDITOR PASS Mode='+mode in txt and 'OWNERSHIP_EDITOR FAILED' not in txt
(R/(label+'-result.json')).write_text(json.dumps({'Passed':passed,'Mode':mode,'Run':str(D),'Markers':markers},indent=2));print('\n'.join(markers[-20:]));assert passed,str(D/'native.log')
