from pathlib import Path
import subprocess,shutil,json,sys
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');R=P/'DevTesting/Release_1_5_0';D=R/('Run-'+(sys.argv[1] if len(sys.argv)>1 else 'FinalMatrix'));JDK=P.parents[1]/'Toolchains/JDK25/bin';SERVER=P/'.build/dependencies/HytaleServer-0.7.2.jar';JAR=P/'build/libs/HyThief-1.5.0.jar'
assert not D.exists();shutil.copytree(P/'DevTesting/HyThief_Test_World/Master',D);C=D/'classes';C.mkdir();(D/'mods').mkdir(exist_ok=True)
subprocess.run([str(JDK/'javac.exe'),'-cp',str(SERVER)+';'+str(JAR),'-d',str(C),str(P/'DevTesting/Release_1_4_2/MatrixVerifier.java')],check=True)
(C/'manifest.json').write_text(json.dumps({'Group':'FirebeardDev','Name':'MatrixVerifier','Version':'1.4.0-candidate-proof','Main':'com.local.pickpocket.MatrixVerifier','ServerVersion':'=0.7.2','Dependencies':{'Hytale:BuilderTools':'*','Local:HyThief':'*'}}))
subprocess.run([str(JDK/'jar.exe'),'--create','--file',str(D/'mods/MatrixVerifier.jar'),'-C',str(C),'.'],check=True);shutil.copy2(JAR,D/'mods'/JAR.name);shutil.copy2(P/'BuildSupport/supported-matrix.json',D/'supported-matrix.json')
cfg=json.loads((D/'config.json').read_text(encoding='utf-8-sig'));cfg['Mods']['FirebeardDev:MatrixVerifier']={'Enabled':True};(D/'config.json').write_text(json.dumps(cfg,indent=2)+'\n')
for config_path in [D/'config.json',D/'universe/worlds/default/config.json']:
 data=json.loads(config_path.read_text(encoding='utf-8-sig'))
 def repin(value):
  if isinstance(value,dict):
   for key,entry in value.items():
    if key=='Local:HyThief' and isinstance(entry,str):value[key]='=1.5.0'
    else:repin(entry)
  elif isinstance(value,list):
   for entry in value:repin(entry)
 repin(data);config_path.write_text(json.dumps(data,indent=2)+'\n')
with (D/'native.log').open('w') as log:subprocess.run([str(JDK/'java.exe'),'-Xmx2G','-XX:ActiveProcessorCount=4','-jar',str(SERVER),'--assets',r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip','--auth-mode','offline','--bind','127.0.0.1:15646','--disable-sentry','--disable-file-watcher','--disable-cpb-build','--boot-command','hythiefmatrix-verify'],cwd=D,stdout=log,stderr=subprocess.STDOUT,check=True,timeout=240)
log=(D/'native.log').read_text();assert 'FULL_MATRIX_VERIFIED' in log and 'MATRIX_ROLE_FAILED' not in log
print('\n'.join(line for line in log.splitlines() if 'FULL_MATRIX_VERIFIED' in line));print('RUN='+str(D))
