# Building HyThief 1.3.0

Use a complete Java 25 JDK and the included Gradle 9.1.0 wrapper. Obtain the Hytale server dependency from your licensed stable 0.6.8 installation and place it at `.build/dependencies/HytaleServer-0.6.8.jar`.

Required dependency SHA256: `dcd2956cc65b950084650eadd56b889cf51f7610c127c3daac1f74dc471fec97`.

On Windows, run `./build.ps1 -JdkHome 'path/to/JDK25' clean build`. When no JDK path is supplied, the script expects `../../Toolchains/JDK25` relative to this project. The script restores its environment changes when it exits.

Output: `build/libs/HyThief-1.3.0.jar`. The source and generated manifest both identify version 1.3.0 and exact server target =0.6.8. The Hytale server dependency is compile-only and is not included in the output.

All required Java sources, resources, native interaction generation inputs and stealth assets are included. Test worlds, backups, installed mods, raw logs and local dependency JARs are excluded from version control and the release JAR.

The two existing inventory deprecation/removal warnings are recorded in the release validation document. They are not compilation failures for 0.6.8.
