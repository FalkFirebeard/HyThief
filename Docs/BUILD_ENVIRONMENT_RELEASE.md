# Building HyThief development container.11

Use a complete Java 25 JDK and the included Gradle 9.1.0 wrapper. Copy the server from your licensed stable Hytale 0.7.0 installation to `.build/dependencies/HytaleServer-0.7.0.jar`.

Required SHA256: `c3b5985b9e72727bf3bcf220923d7e61e78fe0f37cd1cd9c2a73f51e4f408ff8`.

Run `./build.ps1 -JdkHome 'E:/Hytale adjustments/Toolchains/JDK25' clean build`. The default JDK lookup expects a standard project layout; the isolated public worktree must supply the explicit JDK path. Output: `build/libs/HyThief-1.3.1-container.11.jar`; exact manifest server target `=0.7.0`. Native dependencies are compile-only and are not bundled.

The main checkout is ongoing container development. The public candidate is separately based on tag v1.3.0, branch compatibility/1.3.1. Do not publish a main-checkout container JAR as the public patch.

The same two inventory removal warnings and Gradle deprecation notice remain. Test helpers, native server dependencies, worlds and backups are excluded from published JARs. Client testing is required before publishing 1.3.1.
