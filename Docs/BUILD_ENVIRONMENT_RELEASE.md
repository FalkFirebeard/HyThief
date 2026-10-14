# Building HyThief public compatibility candidate 1.3.2

Use a complete Java 25 JDK and the included Gradle 9.1.0 wrapper. Copy the server from your licensed stable Hytale 0.7.1 installation to `.build/dependencies/HytaleServer-0.7.1.jar`.

Required SHA256: `a60fa0b31f1418691f8d8f7cc1159693e3ec504d4f0567fb91493a842d71f8ca`.

Run `./build.ps1 -JdkHome 'E:/Hytale adjustments/Toolchains/JDK25' clean build`. The default JDK lookup expects a standard project layout; the isolated public worktree must supply the explicit JDK path. Output: `build/libs/HyThief-1.3.2.jar`; exact manifest server target `=0.7.1`. Native dependencies are compile-only and are not bundled.

The main checkout is ongoing container development. The public release is separately based on tag v1.3.1, branch compatibility/1.3.2. Do not publish a main-checkout container JAR as the public patch.

The same two inventory removal warnings and Gradle deprecation notice remain. Test helpers, native server dependencies, worlds and backups are excluded from published JARs. Firebeard confirmed client smoke testing passed on Hytale 0.7.1. This approved JAR was not rebuilt for publication.
