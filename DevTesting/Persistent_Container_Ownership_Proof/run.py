from pathlib import Path
import subprocess,json,time,hashlib,sys
P=Path(r'E:\Hytale adjustments\Mod Projects\HyThief');R=P/'DevTesting/Persistent_Container_Ownership_Proof';F=R/'Fixture'
if len(sys.argv)>1:
    candidate=Path(sys.argv[1]).resolve()
    assert candidate.is_relative_to((R/'Runs').resolve()), 'Output must be inside isolated proof Runs'
    R=candidate;F=R/'Fixture'
assert F.resolve().parent==R.resolve()
java=r'E:\Hytale adjustments\Toolchains\JDK25\bin\java.exe'
server=str(P/'.build/dependencies/HytaleServer-0.6.8.jar')
assets=r'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip'
for phase in ['seed','restart-1','restart-2']:
    result=F/('result-'+phase+'.json')
    assert not result.exists(), 'Phase already completed; do not rerun blindly'
    args=[java,'-Downershipproof.phase='+phase]
    if phase=='seed':args+=['-Downershipproof.seed=true']
    args+=['-jar',server,'--assets',assets,'--auth-mode','offline','--bind','127.0.0.1:15546','--disable-sentry','--disable-file-watcher','--disable-cpb-build']
    with (R/(phase+'.log')).open('w',encoding='utf-8') as log:
        proc=subprocess.Popen(args,cwd=F,stdout=log,stderr=subprocess.STDOUT)
        print('START',phase,'PID',proc.pid,flush=True)
        try:code=proc.wait(timeout=180)
        except subprocess.TimeoutExpired:
            proc.terminate();proc.wait(timeout=20)
            raise RuntimeError('Proof timed out; retained log/save for diagnosis')
    text=(R/(phase+'.log')).read_text(errors='replace')
    lines=[x for x in text.splitlines() if '[HyThief OwnershipTest]' in x]
    print('\n'.join(lines),flush=True)
    assert code==0 and '[HyThief OwnershipTest] FAILED' not in text and result.exists(), 'Native phase failed; see '+str(R/(phase+'.log'))
    assert 'Shutdown completed!' in text, 'Shutdown/save did not finish'
    assert json.loads(result.read_text())['passed']
    if phase!='seed':assert '[HyThief OwnershipTest] ATTACHED' not in text, 'Restart secretly reattached ownership'
def inv(p):return {str(f.relative_to(p)):hashlib.sha256(f.read_bytes()).hexdigest() for f in p.rglob('*') if f.is_file()}
before=json.loads((R/'protected-before.json').read_text());diff={}
for n,old in before.items():
    new=inv(Path(n))
    if old!=new:diff[n]={'added':sorted(new.keys()-old.keys()),'removed':sorted(old.keys()-new.keys()),'changed':[k for k in new.keys()&old.keys() if new[k]!=old[k]]}
(R/'protected-verification.json').write_text(json.dumps({'Identical':not diff,'Differences':diff},indent=2))
assert not diff, 'Protected paths changed: '+str(diff)
print('All three fresh-process runs passed. Protected content unchanged.',flush=True)
