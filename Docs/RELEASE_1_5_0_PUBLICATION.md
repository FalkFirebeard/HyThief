# HyThief 1.5.0 — The Creator Update: final publication checkpoint

2026-10-20 (America/Denver). Successfully published and verified on both established platforms. The former Processing status is historical and no longer applies.

## Exact release checkpoint

- Version **1.5.0**; Hytale **=0.7.2**, revision d769dc3d57881261a62858e1394f0e4de9a21eca.
- Approved release commit **64449b272100c7ee188b93637dabb4c320d4d314**.
- Annotated **v1.5.0** resolves exactly to the approved commit.
- Public **main** was normally fast-forwarded from f6b6451 to exactly 64449b2 after explicit approval. No force push or history rewrite.
- Final JAR SHA-256: **c57bfbcb81fe619fe0e6beb8bf96ccb1066b4273d8fbb7404d1122597e5c709e**.

## Both platforms live

[GitHub release](https://github.com/FalkFirebeard/HyThief/releases/tag/v1.5.0): live/latest, not draft/prerelease; release ID419084232. Exactly one v1.5.0 release and one production JAR asset. JAR and SHA256SUMS.txt were publicly downloaded and verified. A fresh final-closeout JAR download matches the approved hash.

[CurseForge file9273461](https://www.curseforge.com/hytale/mods/hythief/files/9273461), project1496959: **approved and live**. Falk personally confirmed approval. Independent public-page verification shows the correct title, Release type, Hytale0.7, download link and Creator Update changelog. The public file list has exactly one 1.5.0 entry, at the top. Submitted only once, with automatic publication after approval. No remaining moderation requirement.

[CurseForge CDN artifact](https://edge.forgecdn.net/files/9273/461/HyThief-1.5.0.jar) was freshly downloaded and matches both the approved hash and GitHub JAR. Live status is established by public file/project views, not merely CDN availability.

## Documentation and successful verification

Creator Update description, summary and changelogs are current. Approved main README and versioned Creator Guide contain both tool commands and the source/native-dispatch verified `/hythief ownership` command:

- `/give HyThief_Ownership_Editor_Tool`, editing permission `hythief.ownership.edit`.
- `/give HyThief_NPC_Editor_Tool`, editing permission `hythief.npc.edit`.
- Native /give permission `hytale.command.give.self`; suitably delegated effective permissions suffice.

Setup, Primary use, targets, Apply/Cancel, inheritance/persistence and limitations remain documented. No universal third-party, scripting, spawning, hostility editor or faction-wide alarm claim. No required video/screenshots.

Falk's successful editor.3 gameplay tests remain explicitly user-reported: tool/targeting/UI, participation settings, witness/response behavior, guard restoration, precedence, merchant trade, persistence, native chest/HyStash ownership and inventory preservation. They are separate from the automated clean build and ten production native suites covering editors, permissions, Apply/Cancel, precedence, entity/world-rule restart persistence, prefab paths, commands, native transactions/caught closure/pursuit, optional HyStash/standalone and the 185-configured-role matrix. Existing warnings/test-harness initialization/cleanup findings are recorded in RELEASE_1_5_0_VERIFICATION.md. No release-blocking production defect remained.

## Preservation and final administration

The artifact was not rebuilt or modified. `.build/public-release/1.5.0` and DevTesting/Release_1_5_0 retain the exact JAR/checksum and verified public downloads. Development/ownership-editor-phase2a stays at **a6897a6**. Accepted editor source/JAR rollback resources, deployment backups, public history, installations, HyStash, worlds, masters and reset scripts remain preserved. No reset or runtime deployment occurred.

The release branch contained known documentation-only ddf014b after approved 64449b2. Falk explicitly approved main -> 64449b2 despite that tip difference. Final documentation is separately committed on release/1.5.0-preparation; main/tag stay at the exact approved release commit. Future work remains separate.

**HyThief 1.5.0 — The Creator Update is fully published and verified. No remaining administrative or moderation tasks.**

Evidence: github-publication.json, curseforge-publication.json, final-closeout-verification.json, artifact comparison, native verification summary, manual-test record and downloaded artifacts under DevTesting/Release_1_5_0. Final documentation commit is reported in the completion handoff.
