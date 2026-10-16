import pathlib,shutil,json,zipfile,subprocess,sys,hashlib,re
R=pathlib.Path(__file__).resolve().parent;P=R.parents[1];jdk=P.parents[1]/'Toolchains/JDK25/bin';server=P/'.build/dependencies/HytaleServer-0.7.2.jar'
installed=pathlib.Path(r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest');expected='23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff'
for candidate in [server,installed/'Server/HytaleServer.jar']:
 if hashlib.sha256(candidate.read_bytes()).hexdigest()!=expected:raise RuntimeError('Hytale patch changed; review this diagnostic before running')
if sys.argv[1]=='setup':
 classes=R/'classes';classes.mkdir(exist_ok=True)
 subprocess.run([str(jdk/'javac.exe'),'-cp',str(server),'-d',str(classes),str(R/'SocialAlertProbe.java')],check=True)
 (classes/'manifest.json').write_text(json.dumps({'Group':'FirebeardDev','Name':'SocialAlertProbe','Version':'0.7.2-investigation','ServerVersion':'=0.7.2','Main':'com.firebeard.diagnostics.SocialAlertProbe'}))
 subprocess.run([str(jdk/'jar.exe'),'--create','--file',str(R/'SocialAlertProbe.jar'),'-C',str(classes),'.'],check=True)
else:
 role=sys.argv[1];mode=sys.argv[2] if len(sys.argv)>2 else 'acquire';label=sys.argv[3] if len(sys.argv)>3 else ''
 if not re.fullmatch(r'[A-Za-z0-9_]+',role) or mode not in {'acquire','beacon','flock-once','config'} or (label and not re.fullmatch(r'[A-Za-z0-9_-]+',label)):raise ValueError('Invalid diagnostic role/mode/run label')
 run=R/('Run-'+role+('' if mode=='acquire' else '-'+mode)+('-'+label if label else ''))
 if run.exists():raise RuntimeError('Preserve existing run; choose a new label')
 run.mkdir();(run/'mods').mkdir();shutil.copy2(R/'SocialAlertProbe.jar',run/'mods/SocialAlertProbe.jar')
 master=P/'DevTesting/Container_Theft_Test_World/Master';world=run/'universe/worlds/default';(world/'resources').mkdir(parents=True);shutil.copy2(master/'universe/worlds/default/config.json',world/'config.json')
 cfg=json.loads((master/'config.json').read_text(encoding='utf-8-sig'));cfg['Mods']={'FirebeardDev:SocialAlertProbe':{'Enabled':True}};cfg['ServerName']='DISPOSABLE Native social investigation';(run/'config.json').write_text(json.dumps(cfg))
 if mode=='config':
  fixture=run/'mods/NativeFriendlyFixture';fixture.mkdir();(fixture/'manifest.json').write_text(json.dumps({'Group':'FirebeardDev','Name':'NativeFriendlyFixture','Version':'0.7.2-diagnostic','ServerVersion':'=0.7.2','IncludesAssetPack':True}))
  native=zipfile.ZipFile(installed/'Assets.zip');index={pathlib.PurePosixPath(n).stem:n for n in native.namelist() if n.startswith('Server/NPC/Roles/') and n.endswith('.json')}
  for name in ['Template_Goblin_Scrapper','Template_Trork_Melee','Template_Intelligent','Template_Predator']:
   asset=json.loads(native.read(index[name]))
   if name=='Template_Intelligent':asset['Parameters']['DefaultPlayerAttitude']['Value']='Friendly'
   else:asset['DefaultPlayerAttitude']='Friendly'
   target=fixture/index[name];target.parent.mkdir(parents=True,exist_ok=True);target.write_text(json.dumps(asset))
  cfg['Mods']['FirebeardDev:NativeFriendlyFixture']={'Enabled':True};(run/'config.json').write_text(json.dumps(cfg))
 time=run/'universe/worlds/default/resources/Time.json';time.write_text(json.dumps({'Now':'1970-01-01T12:00:00Z'}))
 subprocess.run([str(jdk/'java.exe'),'-Xmx2G','-Xms128m','-XX:ActiveProcessorCount=4','-Dsocial.role='+role,'-Dsocial.mode='+mode,'-Dsocial.configcheck='+str(mode=='config').lower(),'-jar',str(server),'--assets',r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip','--auth-mode','offline','--bind','127.0.0.1:15578','--disable-sentry','--disable-file-watcher','--disable-cpb-build'],cwd=run,stdout=(run/'native.log').open('w'),stderr=subprocess.STDOUT,timeout=110,check=True)
 print((run/'social-evidence.txt').read_text())
