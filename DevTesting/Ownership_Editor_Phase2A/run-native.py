from pathlib import Path
import subprocess,sys,json
H=Path(r"E:\Hytale adjustments\Mod Projects\HyThief");R=H/"DevTesting/Ownership_Editor_Phase2A"
D=R/sys.argv[1];mode=sys.argv[2];label=sys.argv[3]
with (D/(label+".log")).open("w",encoding="utf-8") as log:
 result=subprocess.run([str(H.parents[1]/"Toolchains/JDK25/bin/java.exe"),"-Xmx2G","-XX:ActiveProcessorCount=4","-Dwardrobe.mode="+mode,"-jar",str(H/".build/dependencies/HytaleServer-0.7.2.jar"),"--assets",r"C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip","--auth-mode","offline","--bind","127.0.0.1:"+(sys.argv[4] if len(sys.argv)>4 else "15678"),"--disable-sentry","--disable-file-watcher","--disable-cpb-build"],cwd=D,stdout=log,stderr=subprocess.STDOUT,timeout=210)
txt=(D/(label+".log")).read_text(encoding="utf-8",errors="replace");markers=[l for l in txt.splitlines() if "OWNERSHIP_EDITOR" in l or "Exception" in l]
print("\n".join(markers[-30:]));passed=result.returncode==0 and "OWNERSHIP_EDITOR PASS Mode="+mode in txt and "OWNERSHIP_EDITOR FAILED" not in txt
(R/(label+"-result.json")).write_text(json.dumps({"Passed":passed,"Mode":mode,"Run":str(D),"Markers":markers},indent=2),encoding="utf-8")
assert passed,str(D/(label+".log"))
