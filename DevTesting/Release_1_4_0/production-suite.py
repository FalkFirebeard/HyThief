from pathlib import Path
import runpy,subprocess,json,hashlib
tools=runpy.run_path(str(Path(__file__).with_name('production-run.py')));run=tools['run'];P=tools['P'];R=tools['R']
tools['compile']()
results=[]
def proof(mode,label,source=None):
 print('START',mode,flush=True);run(mode,label,source);results.append({'Mode':mode,'Run':label,'Passed':True});(R/'suite-results.json').write_text(json.dumps(results,indent=2)+'\n');print('PASS',mode,flush=True)
for family in ['Kweebec','Feran','LOCAL']:proof('production-reaction-'+family,'Native-'+family)
for family in ['Trork_Warrior','Outlander_Brute','Skeleton_Fighter','Zombie']:proof('peer-response-'+family,'Family-'+family)
for kind in ['Kweebec','Feran','LocalBoth','LocalKweebec','LocalFeran']:proof(kind,'Fixture-'+kind)
proof('persistence','Ownership-Persistence')
proof('restart','Ownership-Restart',str(R/'Run-Ownership-Persistence'))
print('START public matrix',flush=True)
subprocess.run([str(Path(__import__('sys').executable)),str(R/'production-matrix.py'),'RepeatMatrix'],stdout=(R/'matrix-console.log').open('w'),stderr=subprocess.STDOUT,check=True,timeout=240)
results.append({'Mode':'Public 174 concrete role matrix','Passed':True});(R/'suite-results.json').write_text(json.dumps(results,indent=2)+'\n')
print('SUITE PASS',len(results),flush=True)
