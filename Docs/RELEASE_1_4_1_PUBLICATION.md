# HyThief 1.4.1 publication record

Published/submitted 2026-10-16 with Firebeard's explicit UI-hotfix publication authorization.

- Version: **1.4.1**, exact Hytale target **=0.7.2**, revision `d769dc3d57881261a62858e1394f0e4de9a21eca`.
- Release commit: `f096550fc2e1c3675b5ab3e9ac10ceed9900f730`.
- Annotated tag: `v1.4.1`.
- GitHub: https://github.com/FalkFirebeard/HyThief/releases/tag/v1.4.1
- GitHub is live/latest, not draft/prerelease. Exact JAR and SHA256SUMS.txt uploaded and downloaded verification passed.
- CurseForge existing project: **1496959**; file **9218387**, submitted once, **Processing** at final check.
- Release type **Release**, Hytale category **0.7**, automatic publication after approval enabled.
- CurseForge main public description and summary saved and verified rendered as 1.4.1 / Hytale 0.7.2. Saved file editor confirms correct rendered hotfix changelog.
- CurseForge still showed 1.4.0 as Main File pending moderation. Processing is not a claim of approval/live/default status. Do not duplicate-upload.
- Submitted-file editor: https://authors.curseforge.com/#/projects/1496959/files/9218387
- CDN: https://edge.forgecdn.net/files/9218/387/HyThief-1.4.1.jar
- CDN download succeeded; its hash equals the GitHub download and exact staged artifact.

SHA-256: `d91de629dc0d607b4172e28031c5deeb196b6dfa55f6f258d886fc721efb8395`

## Changes and validation

Only runtime change: `Server/Languages/en-US/server.lang`, the shared hint now displays exactly `Pickpocket`. Existing key and all 176 active role-definition paths are preserved. `manifest.json` and `build.gradle` bump the release version. README and BUILD_ENVIRONMENT_RELEASE.md references, RELEASE_1.4.1.md notes and RELEASE_1_4_1_VERIFICATION.md document this hotfix. This file closes out publication.

Clean Java 25 / Gradle 9.1.0 build passed against pinned Hytale 0.7.2. Strict archive comparison passed: only manifest version and shared language entry differ from approved public 1.4.0. All 48 Java classes, NPC assets and other packaged files are byte-identical. Existing deprecation notices remain; no new compiler errors. Full evidence is in local `.build/public-release/1.4.1/validation.json` and downloaded verification files.

No gameplay/AI/witness/ownership/crime/container changes, Wardrobes compatibility work, test-world/fixture/reset changes, or live-mod installation were made. The current development main contains the same hotfix and version metadata; 1.4.0 artifact/tag/history and rollback remain preserved. No new connected-client run is claimed. This documentation follow-up leaves the release tag at the verified release-source commit.
