exec((__import__('pathlib').Path(__file__).parent/'peer-field.py').read_text().split("if sys.argv[1]=='setup':")[0])
phase=sys.argv[1]
if phase=='install':
 for label,token in [('geometry-s','PEER_VERIFY_PASS'),('idle-s','PEER_IDLE_PASS'),('Trork-s2','PEER_RESPONSE_PASS'),('Outlander-s','PEER_RESPONSE_PASS'),('Goblin-s','PEER_RESPONSE_PASS'),('Skeleton-s','PEER_RESPONSE_PASS'),('Zombie-s','PEER_RESPONSE_PASS')]:
  text=(R/('Run-'+label)/'field-evidence.txt').read_text();assert token in text and 'FAILED' not in text,label
  shutil.copy2(R/('Run-'+label)/'field-evidence.txt',R/(label+'-evidence.txt'))
 assert json.loads((R/'baseline-results.json').read_text())['BaselinePassedBeforeExpansion']
 old=snap(R/'Rollback/Master');current=snap(W/'Master');assert current==old,'Live Master changed during disposable testing'
 assert snap(W/'Master/mods/ContainerPassiveClientFixture')==snap(R/'Rollback/Master/mods/ContainerPassiveClientFixture')
 # Preserve the old master and replace files only inside this explicitly checked world root.
 draft=R/'DraftMaster';marker=draft/'container-theft-test-world.json';a=json.loads(marker.read_text());a['FixtureVersion']='1.2.0';a['LayoutVersion']=11;write(marker,a)
 proposed=snap(draft);added=sorted(set(proposed)-set(old));changed=sorted(name for name in old if proposed.get(name)!=old[name]);assert not set(old)-set(proposed),'Master files unexpectedly removed'
 expected='mods\\ContainerPassiveClientFixture\\Server\\NPC\\Roles\\HyThief\\Stealth\\HyThiefStealth_Trork_Sentry.json'
 assert added==[expected],added
 assert all(name=='container-theft-test-world.json' or name.startswith('universe\\worlds\\default\\chunks\\') for name in changed),changed
 for name in added+changed:shutil.copy2(draft/name,W/'Master'/name)
 assert snap(W/'Master')==proposed
 records=[{'Relative':str(f.relative_to(W/'Master')),'Hash':sha(f).upper()} for f in sorted((W/'Master').rglob('*')) if f.is_file()];write(W/'master-checksums.json',records)
 write(R/'master-install-proof.json',{'AddedFiles':added,'ChangedFiles':changed,'AllExistingRoleAssetsUnchanged':True,'OriginalFourAreaSemanticHash':'dceeeb1c852f91ba1e1f89292b2538b13db5fba601e42bc24948547342f8ef97','PrimaryUUIDPositionFacingStatePreserved':True,'MasterFileCount':len(records)})
 protected={str(root):snap(root) for root in [P/'src-java',P/'Server',P/'Common',P/'DevTesting/HyThief_Test_World/Master',SAVES/'HyThief Test World',SAVES/'HyThief Passive Witness Test']};write(R/'protected-before-deployment.json',protected)
 print(json.dumps({'Changed':changed,'Added':added,'MasterFiles':len(records)},indent=2))
elif phase=='close':
 assert 'PEER_VERIFY_PASS' in (R/'Run-reset-active/field-evidence.txt').read_text()
 shutil.copy2(R/'Run-reset-active/field-evidence.txt',R/'reset-active-evidence.txt')
 protected=json.loads((R/'protected-before-deployment.json').read_text());assert all(snap(Path(root))==value for root,value in protected.items())
 assert snap(ACTIVE)==snap(W/'Master')
 mods=Path(r'C:\Users\askin\AppData\Roaming\Hytale\UserData\Mods');installed=list(mods.glob('*HyThief*.jar'));assert len(installed)==1 and sha(installed[0])==sha(JAR)
 assert sha(P/'.build/public-release/1.3.3/HyThief-1.3.3.jar')=='3c39a85557edd8504f5d8e080762b9028be135049a504b372106fd89b626b13d'
 summary={}
 for label in ['Goblin','Trork','Outlander','Skeleton','Zombie']:
  run='Run-Trork-s2' if label=='Trork' else 'Run-'+label+'-s';text=(R/run/'field-evidence.txt').read_text();summary[label]=next(line for line in text.splitlines() if line.startswith('FAMILY_RESULT'))
 write(R/'native-results.json',summary)
 write(R/'preservation-proof.json',{'GameplayJarUnchanged':sha(JAR),'ExactlyOneHyThiefJar':True,'PublicReleaseUnchanged':True,'ExistingRoleAssetsUnchanged':True,'OtherWorldsUnchangedDuringDeployment':True,'MasterActiveMatch':True,'DesktopResetUnchanged':True,'MasterFileCount':len(snap(W/'Master'))})
 for src,name in [(Path(__file__),'Finalize-Peer-Field.py'),(Path(__file__).parent/'generate-peer-harness.py','Generate-Peer-Harness.py'),(Path(__file__).parent/'reset-peer-desktop.ps1','Verify-Desktop-Reset.ps1')]:shutil.copy2(src,R/name)
 print(json.dumps(summary,indent=2))
