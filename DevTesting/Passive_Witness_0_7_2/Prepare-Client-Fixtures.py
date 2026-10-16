from pathlib import Path
import shutil,json,hashlib,subprocess,zipfile,sys
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');R=P/'DevTesting/Passive_Witness_0_7_2';S=Path(__file__).parent
roles=['Goblin_Scrapper','Trork_Warrior','Outlander_Brute','Skeleton_Fighter','Zombie']
label='client-master'
if len(sys.argv)>1 and sys.argv[1]=='isolate':
 label='client-isolated'
 for role in roles:assert 'AUTHOR_PASS Role='+role in (R/('Run-'+role+'-'+label)/'personal-evidence.txt').read_text()
 previous=R/'Masters';backup=R/'FixtureRollback-before-peer-isolation'
 assert previous.resolve().parent==R.resolve() and not backup.exists()
 shutil.move(str(previous),str(backup))
def snapshot(root):return {str(p.relative_to(root)):hashlib.sha256(p.read_bytes()).hexdigest() for p in root.rglob('*') if p.is_file()}
for role in roles:
 run=R/('Run-'+role+'-'+label);assert 'AUTHOR_PASS Role='+role in (run/'personal-evidence.txt').read_text()
 master=R/'Masters'/role;assert not master.exists();master.mkdir(parents=True)
 for rel in ['config.json','client_metadata.json','permissions.json','bans.json','container-theft-test-world.json']:shutil.copy2(run/rel,master/rel)
 shutil.copytree(run/'universe',master/'universe')
 # No development/proof Java plugins embedded in client saves; installed HyThief is authoritative.
 (master/'mods').mkdir();shutil.copytree(run/'mods/PersonalFriendlyFixture',master/'mods/PersonalFriendlyFixture')
 cfg=json.loads((master/'config.json').read_text());cfg['Mods'].pop('FirebeardDev:PersonalWitnessProbe',None);cfg['Mods']={'Local:HyThief':{'Enabled':True},'FirebeardDev:PersonalFriendlyFixture':{'Enabled':True}};(master/'config.json').write_text(json.dumps(cfg,indent=2))
 (R/('master-checksums-'+role+'.json')).write_text(json.dumps(snapshot(master),indent=2))
 print('Prepared isolated master:',role)
candidate=P/'build/libs/HyThief-1.3.1-container.12.jar';(R/'candidate.json').write_text(json.dumps({'Version':'1.3.1-container.12','SHA256':hashlib.sha256(candidate.read_bytes()).hexdigest()},indent=2))
for name in ['Reset-Passive-Witness.ps1','Reset-Passive-Witness.cmd']:
 if (S/name).resolve()!=(R/name).resolve():shutil.copy2(S/name,R/name)
