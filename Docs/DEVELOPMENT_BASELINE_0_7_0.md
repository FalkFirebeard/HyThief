# HyThief 0.7.0 development baseline — verified

## Compatibility parity

Public tag **v1.3.1** / commit **2ff6759** was compared against development starting at **af49b1b**. Container.11 already contains the full required public 0.7.0 patch. **BuildSupport**, resolver/loot/rules config and build-environment lock are identical. Build logic and manifest differ only in the development version **1.3.1-container.11** versus public **1.3.1**. Shared source differences are the intentionally preserved container modules and integration; no public compatibility Java changes were missing.

Four redesigned Goblin roots/private components/native-path variants are already current. Nine native-retired Goblin overrides are excluded from both JARs; historical inactive resolver entries remain. Existing ECS/API migrations are present, including the development-only block lookup through BlockSection replacing removed World.getBlockType. Active runtime source/assets/config/build files have no 0.6.8 target reference. Live installed server and pinned dependency match **0.7.0**, revision **648ebf375a034beabf119d7d790cd11d4f665ff4**, SHA256 **c3b5985b9e72727bf3bcf220923d7e61e78fe0f37cd1cd9c2a73f51e4f408ff8**.

Semantic JAR comparison confirms all public entries are present in development. JSON/model/UI/language/loot resources match after accounting for LF/CRLF serialization; no resource rewrite was necessary. Common class differences are only PickpocketPlugin and its nested classes, HyThiefGuardAlert and manifest, reflecting preserved development integration.

## Necessary corrections

No gameplay/source/API merge was needed. The container test infrastructure still had stale **=0.6.8** metadata. Changed only:

- DevTesting/Container_Theft_Test_World/Pack/manifest.json: ServerVersion **=0.7.0**.
- DevTesting/Container_Theft_Test_World/Master/mods/ContainerTheftFixture/manifest.json: same metadata correction.
- Active container save's matching fixture manifest: same correction.
- Container master-checksums.json regenerated.
- Container README target corrected to **=0.7.0**.
- This baseline note and machine-readable validation record.

Fixture version remains **1.0.3**; no fixture behavior was changed. No NPC tree, ownership data, gameplay policy, loot, UI, notification text, distance, position or geometry was changed. No live world reset or redesign occurred. Existing reset process/path/identity safeguards remain intact.

## Build and native validation

Clean development build **PASS**, Java 25 / Gradle 9.1.0 against the exact installed 0.7.0 server. Same two inventory removal warnings and existing Gradle deprecation notice. Output **build/libs/HyThief-1.3.1-container.11.jar**, target **=0.7.0**, SHA256 **f8ca7abd0a53298da8e1e3a1ac8f982caf044d3d4d88f08e160a5f841995e029**. This is byte-identical to the prior 0.7.0 development artifact; no new version iteration or gameplay change was required.

Five fresh, disposable native-server saved-world runs passed:

| Test | Alert branch | Responders | Caught notifications |
|---|---|---:|---:|
| Kweebec FACTION | Kweebec_All_Danger | Razorleaf only | 1 |
| Feran FACTION | Feran_Warriors_Attack | Sharptooth only | 1 |
| NEARBY, both visible | HyThief_Local_Theft | Both | 1 |
| NEARBY, Feran witness blocked | HyThief_Local_Theft | Both | 1 |
| NEARBY, Kweebec witness blocked | HyThief_Local_Theft | Both | 1 |

Every run verifies real persisted ownership components survive load and native chest OpenWindow/CloseWindow; paired native inventory withdrawal attribution; unowned control gives zero notification; faction witness filtering; NEARBY allows both witnesses; correct sensor signal/exact offender target; native Attack state and measurable pursuit. Existing LastSeen fixture instruction/target-slot paths remain byte-unchanged and are accepted during current native role initialization. HyThief does not manually control combat/pathing. A distinct real-client lost-LOS test was not repeated in this metadata-only checkpoint.

Container visual range remains **20 standing / 6 crouched**; normal HyThief perception remains **15/4**; LOCAL delivery remains **30**. Shared public pickpocket/menu/backend assets and unchanged compiled shared classes remain present. Prior public 0.7.0 matrix validation (174 concrete + two abstract roles) and Firebeard's public interaction/caught checks still apply to the unchanged shared feature set. This audit did not claim a new connected-client test.

## Color field and reset integrity

Exactly four owned-policy stations / four NPCs remain. **Red=UNOWNED, Blue=KWEEBEC FACTION, Green=NEARBY, Yellow=FERAN FACTION**. Standing pads at X=0, Z=-6.5/-2.5/1.5/5.5, chests at X=0, Z=-6/-2/2/6. Witnesses remain at **(-7.5,64,0.5)** and **(7.5,64,0.5)**, stationary, mirrored/inward-facing. Responders remain at **(-21.5,64,0.5)** and **(21.5,64,0.5)**, mobile and unobstructed. Equal witness distances/FOV/elevation and clear LOS pass at every pad; witness-to-responder distances are **14 or 29**, inside LOCAL 30, while responders remain outside direct standing sight of all pads.

Master has **33 checksummed files**. Updated reset staging copies match all hashes; the fresh stage itself passes native saved-world validation without reattaching ownership. Original/current master and active file sets match, with only the fixture manifest target changed. General HyThief master/active world hashes are completely unchanged. Reusable prefab and public release artifacts remain untouched.

## Rollback and local installation

Before audit: source **af49b1b**, checkpoint tag **checkpoint/dev-0.7.0-before-baseline-check**. Full master/active snapshots, previous fixture metadata/checksums, previous development JAR and previous installed public JAR are preserved under **DevTesting/Development_Baseline_0_7_0/Rollback/**. The older pre-update container checkpoint/tag/bundle remain intact too.

Verified exactly one active Local:HyThief: **C:/Users/askin/AppData/Roaming/Hytale/UserData/Mods/HyThief-1.3.1-container.11.jar**, hash above, target =0.7.0. Previous installed public version is backed up at **Rollback/Installed/HyThief-1.3.1.jar**. Public 1.3.1 release artifacts remain separately preserved and unchanged. Unrelated mod JAR hashes/count are unchanged. Nothing was published.

Evidence: **DevTesting/Development_Baseline_0_7_0/** (clean-build log, five result logs, before/validation/deployment JSON, both reset stages and Rollback); underlying native logs use the RUN paths recorded in each result log under DevTesting/Update_0_7_0/.

**HyThief development baseline is now fully aligned with public 1.3.1 / Hytale 0.7.0 and ready for PREFAB ownership work.** No PREFAB work was started.
