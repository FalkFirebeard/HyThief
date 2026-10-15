# Building HyThief public compatibility candidate 1.3.3

Use a complete Java 25 JDK and the included Gradle 9.1.0 wrapper. Copy the server from your licensed stable Hytale 0.7.2 installation to `.build/dependencies/HytaleServer-0.7.2.jar`.

Required SHA256: `23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff`.

Run `./build.ps1 -JdkHome 'E:/Hytale adjustments/Toolchains/JDK25' clean build`. The default JDK lookup expects a standard project layout; the isolated public worktree must supply the explicit JDK path. Output: `build/libs/HyThief-1.3.3.jar`; exact manifest server target `=0.7.2`. Native dependencies are compile-only and are not bundled.

The main checkout is ongoing container development. The public release is separately based on tag v1.3.2, branch compatibility/1.3.3. Do not publish a main-checkout container JAR as the public patch.

The same two inventory removal warnings and Gradle deprecation notice remain. Test helpers, native server dependencies, worlds and backups are excluded from published JARs. Publication requires a clean current build, native matrix/guard validation, and proof that only manifest.json differs from the approved public 1.3.2 JAR. Prior real-client testing applies to unchanged gameplay; no new 0.7.2 client result is claimed.
