# Building HyThief 1.4.2 / Hytale 0.7.2

Check out tag **v1.4.2**. Use a complete Java 25 JDK and the included Gradle 9.1.0 wrapper. Copy the server from your licensed Hytale 0.7.2 installation to `.build/dependencies/HytaleServer-0.7.2.jar`.

Pinned server SHA-256: `23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff`, revision `d769dc3d57881261a62858e1394f0e4de9a21eca`.

Run `./build.ps1 -JdkHome 'E:/Hytale adjustments/Toolchains/JDK25' clean build`. Output: `build/libs/HyThief-1.4.2.jar`, exact manifest target `=0.7.2`. Native dependencies are compile-only and not bundled.

1.4.2 intentionally includes production container theft. Earlier 1.3.x compatibility releases excluded container development; those historical tags and artifacts remain unchanged. Development test helpers, worlds and diagnostic registries are excluded from the public JAR. Existing inventory/Gradle deprecation notes are documented in the container architecture note.

Production text resources have explicit LF attributes; saved-world bytes retain their independent -text protection. See [release notes](RELEASE_1.4.2.md) and [container scope](CONTAINER_THEFT_1_4_0_CONTENT_AUDIT.md).
