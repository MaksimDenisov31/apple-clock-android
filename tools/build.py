"""Build a signed APK using the official Android SDK and JDK (no third-party dependencies)."""
from pathlib import Path
import os, subprocess, shutil, json, secrets, zipfile, hashlib, argparse

ROOT=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--sdk',default=os.environ.get('ANDROID_HOME',str(ROOT/'.tools/sdk')));p.add_argument('--jdk',default=os.environ.get('JAVA_HOME',r'C:\Program Files\Microsoft\jdk-11.0.12.7-hotspot'));args=p.parse_args()
sdk=Path(args.sdk);jdk=Path(args.jdk);windows=os.name=='nt';exe='.exe' if windows else ''
def run(*cmd,**kw):
 subprocess.run([str(x) for x in cmd],check=True,**kw)
def tool(name):
 matches=list(sdk.glob('**/'+name+exe));assert matches, f'Missing SDK tool: {name}';return matches[-1]
build=ROOT/'build';build.mkdir(exist_ok=True);dist=ROOT/'dist';dist.mkdir(exist_ok=True)
classes=build/'classes';classes.mkdir(exist_ok=True)
jar=list(sdk.glob('**/android.jar'))[-1];src=ROOT/'app/src/main'
run(tool('aapt2'),'compile','--dir',src/'res','-o',build/'resources.zip')
run(tool('aapt2'),'link','-o',build/'unsigned.apk','-I',jar,'--manifest',src/'AndroidManifest.xml','--java',build/'generated',build/'resources.zip')
sources=list((src/'java').rglob('*.java'))+list((build/'generated').rglob('*.java'))
run(jdk/'bin'/('javac'+exe),'-encoding','UTF-8','--release','8','-classpath',jar,'-d',classes,*sources)
with zipfile.ZipFile(build/'classes.jar','w') as z:
 for f in classes.rglob('*.class'):z.write(f,f.relative_to(classes).as_posix())
d8=tool('aapt2').parent/'lib/d8.jar'
run(jdk/'bin'/('java'+exe),'-cp',d8,'com.android.tools.r8.D8','--lib',jar,'--min-api','26','--output',build,build/'classes.jar')
with zipfile.ZipFile(build/'unsigned.apk','a',compression=zipfile.ZIP_DEFLATED) as z:z.write(build/'classes.dex','classes.dex')
run(tool('zipalign'),'-f','4',build/'unsigned.apk',build/'aligned.apk')
private=ROOT/'.tools';private.mkdir(exist_ok=True);config=private/'signing.json';key=private/'apple-clock-release.jks'
if not config.exists():
 assert not key.exists(),'Existing keystore needs its signing configuration; do not replace it'
 config.write_text(json.dumps({'password':secrets.token_urlsafe(32)}))
password=json.loads(config.read_text())['password'];env=dict(os.environ,APPLE_CLOCK_KEY_PASSWORD=password)
if not key.exists():
 run(jdk/'bin'/('keytool'+exe),'-genkeypair','-keystore',key,'-storepass:env','APPLE_CLOCK_KEY_PASSWORD','-keypass:env','APPLE_CLOCK_KEY_PASSWORD','-alias','appleclock','-keyalg','RSA','-keysize','3072','-validity','10000','-dname','CN=Apple Clock Android, O=MaksimDenisov31',env=env)
apk=dist/'AppleClock-Android-1.0.0.apk'
signer=tool('aapt2').parent/'lib/apksigner.jar'
run(jdk/'bin'/('java'+exe),'-jar',signer,'sign','--ks',key,'--ks-pass','env:APPLE_CLOCK_KEY_PASSWORD','--key-pass','env:APPLE_CLOCK_KEY_PASSWORD','--out',apk,build/'aligned.apk',env=env)
run(jdk/'bin'/('java'+exe),'-jar',signer,'verify','--verbose',apk)
run(tool('aapt2'),'dump','badging',apk)
(dist/'SHA256SUMS.txt').write_text(hashlib.sha256(apk.read_bytes()).hexdigest()+'  '+apk.name+'\n',encoding='ascii')
print('APK:',apk)
