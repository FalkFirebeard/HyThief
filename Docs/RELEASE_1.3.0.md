# HyThief 1.3.0 release record

Prepared October 6, 2026 (America/Denver), from validated 1.2.1-revamp.11.

## Build and validation

- Clean build passed with Java 25, Gradle 9.1.0, pinned stable Hytale server 0.6.8.
- Final JAR: `HyThief-1.3.0.jar`.
- SHA256: `c31a4fcae1b24fd5f5b9b783e48e634bb1d90f8566c45f9031de1de54810d64d`.
- Manifest: version 1.3.0, server target =0.6.8, author Falk Interactive Studios.
- Local:HyThief identity and main entry point preserved to retain the existing data location.
- Excluded 54 test/backup assets: old UI backup, NPC test spawn markers and testing prefabs. All remaining 1,751 production assets/config entries byte-identical to revamp.11; 1,745 packaged JSON entries parsed successfully. Source prefab files remain intact.
- No diagnostic helper, diagnostic event strings, delayed guard observations, test verifier classes, logs or development worlds in the release JAR.
- Fresh isolated native validation passed: 185 configured IDs, 183 concrete spawns, two abstract builder validations, 2,782 state checks, 732 calibration checks, zero matrix failures.
- Two existing inventory API removal warnings remain. These compile and run against the pinned 0.6.8 target; future Hytale targets will require a separate compatibility review.
- No new manual per-role gameplay pass is claimed. The user authorized release based on the existing representative gameplay results and subsequent player feedback.

## Rollback and protected scope

Complete revamp.11 source/resource/build-support/documentation/wrapper/JAR rollback is under `.build/release-preparation/1.3.0/revamp.11-rollback`, with a per-file SHA256 inventory alongside it.

Existing revamp deployments, archived v1.2 JAR, prefab, pristine world master, live worlds, installed mods and unrelated projects were not edited. The isolated validation wrote only a new disposable world under this release checkpoint.

## Current official requirements reviewed

- Hytale stable documentation identifies Release 0.6.8: https://docs.hytale.com/
- Stable Java API and asset reference: https://docs.hytale.com/api/ and https://docs.hytale.com/assets/
- Official Update 6 notes: https://hytale.com/news/2026/8/update-6-patch-notes
- Update 7 remains pre-release and is outside this release target: https://hytale.com/news/2026/9/pre-release-patch-notes-update-7
- CurseForge Hytale publishing: Mods class, upload plugin JAR, add release notes, then moderation: https://blog.curseforge.com/how-to-create-hytale-mods/
- CurseForge submission fields include clear English summary/description, accurate categories and license: https://support.curseforge.com/support/solutions/articles/9000197241-creating-and-submitting-a-project

Existing CurseForge destination: https://www.curseforge.com/hytale/mods/hythief (project 1496959). Keep existing project/categories and All Rights Reserved license. Credit Falk Interactive Studios in project description and release notes; do not rename the account or transfer ownership.

Upload display name: **HyThief 1.3.0 — Hytale 0.6.8**. Type: **Release**, not beta. Select the actual Hytale version label offered by the author console; if it only offers Early Access, specify 0.6.8 prominently in the changelog. Never claim broad compatibility beyond =0.6.8.

Release changelog: `RELEASE_NOTES_1.3.0.md`. Planned GitHub destination: `FalkFirebeard/HyThief`, tag `v1.3.0`, final JAR and SHA256 file as release assets.

Local preparation is complete. GitHub push/release and CurseForge upload status must be verified separately; this record does not assert publication.

