from pathlib import Path
import shutil,json,subprocess,sys
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');R=P/'DevTesting/Passive_Witness_0_7_2'
role=sys.argv[1];roles=['Goblin_Scrapper','Trork_Warrior','Outlander_Brute','Skeleton_Fighter','Zombie'];assert role in roles
run=R/('Reload-'+role+('-'+sys.argv[2] if len(sys.argv)>2 else ''));assert not run.exists();shutil.copytree(R/'Masters'/role,run)
shutil.copy2(P/'build/libs/HyThief-1.3.1-container.12.jar',run/'mods/HyThief-1.3.1-container.12.jar');shutil.copy2(R/'PersonalWitnessProbe.jar',run/'mods/PersonalWitnessProbe.jar')
cfg=json.loads((run/'config.json').read_text());cfg['Mods']['FirebeardDev:PersonalWitnessProbe']={'Enabled':True};(run/'config.json').write_text(json.dumps(cfg))
jdk=P.parents[1]/'Toolchains/JDK25/bin';server=P/'.build/dependencies/HytaleServer-0.7.2.jar';assets=Path(r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip')
subprocess.run([str(jdk/'java.exe'),'-Xmx2G','-Xms128m','-XX:ActiveProcessorCount=4','-Dpersonal.fixtureverify=true','-Dpersonal.role='+role,'-jar',str(server),'--assets',str(assets),'--auth-mode','offline','--bind','127.0.0.1:'+str(15579+roles.index(role)),'--disable-sentry','--disable-file-watcher','--disable-cpb-build'],cwd=run,stdout=(run/'reload.log').open('w'),stderr=subprocess.STDOUT,timeout=120,check=True)
e=(run/'personal-evidence.txt').read_text();print(e);assert 'FAILED' not in e and 'FIXTURE_RELOAD_PASS' in e
