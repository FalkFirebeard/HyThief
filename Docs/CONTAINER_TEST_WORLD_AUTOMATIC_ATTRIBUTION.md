> Superseded by container.3 checkpoint 2: see `CONTAINER_THEFT_CHECKPOINT_2_VISUAL_WITNESS.md`. Custom owned-chest hints are removed; this note preserves historical checkpoint-1 evidence.

> Layout/test-order update: see `CONTAINER_TEST_WORLD_LAYOUT_OWNERSHIP_UI.md`. This document preserves the previous container.2 checkpoint layout.

# HyThief automatic container attribution test world

Checkpoint: `1.3.1-container.2`, Hytale stable `=0.6.8`. No public release.

## Firebeard workflow

Load **HyThief Container Theft Test World**. Spawn is `(0.5,64.1,7.5)`, facing +Z toward the civilian/front stage. Open the labeled FRONT chest and take items, then REAR, then UNOWNED CONTROL. No mark, clear, join, coordinates, or other commands are required.

| Object | Block position | Automatic ownership |
|---|---|---|
| Front chest | `0 64 16` | Kweebec test property |
| Rear chest | `0 64 4` | Kweebec test property |
| Control chest | `-4 64 -2` | Unowned |

All three native 36-slot chests reset to 16 copper bars, 8 iron bars and 32 fibre. Civilian is native `Kweebec_Sapling`, frozen at `(0.5,64,10.5)` with body/head yaw pi, facing +Z/front. Native `Kweebec_Razorleaf` remains unfrozen behind the opaque x=6 shield wall; walking access goes around the south end, z<0. Existing range markers 1/3/4/5/8/12/15/30/32m remain. No witness, sound, crime, warning or guard consequence is implemented here.

Expected: each valid owned withdrawal creates one `[HyThief ContainerTest] Action=REMOVE` log with the actual sender UUID, exact chest, item and withdrawn quantity. Insertion, internal moves, sorting, failed moves and control withdrawals create no owned removal. IGNORE diagnostics may describe a scoped request without a paired withdrawal. Front/rear direction does not change attribution in this checkpoint.

## Recovery and safety

Interrupted session had not applied automatic ownership: installed JAR remained container.1 with matching SHA256; all 28 pristine-master files verified. Existing uncommitted source was valid completed Checkpoint-1 work. No interrupted changes needed rollback.

Complete pre-change source, JAR, master and active save were backed up to `Rollback-container.1-before-auto` beside this README. The original diagnostic probe is byte-identical in source; existing pickpocket gameplay/stealth/combat code is unchanged except diagnostic lifecycle setup/start/shutdown wiring. Public Gradle version remains 1.3.0; diagnostic init script selects container.2.

## Automatic registration architecture

`PickpocketPlugin` enables diagnostics only for `-container.` builds. Setup registers StartWorld/RemoveWorld events; plugin start waits for Universe ready to catch already loaded worlds. The universe-ready future is not available at setup on stable 0.6.8, verified by an isolated failed boot and corrected before deployment.

Automatic configuration is stored in the **development save marker**, `container-theft-test-world.json`, under `AutoContainerDiagnostics`, not in production ownership coordinates. It requires the exact display name, fixture kind, matching world UUID, schema version, Kweebec owner, exactly two distinct owned positions and a distinct control position. Each loaded native block ID/container is validated on the world thread before registry registration. Keys are world UUID + XYZ; entries also retain world instance and live ItemContainer identity.

World-load section requests activate native containers. Automatic entries are removed on world removal/shutdown, recreated on load/restart and refreshed on inventory requests if references have changed. No tick polling. Repeated registration failures are deduplicated. Every player in the identified fixture is automatically observed; no join is necessary. Manual opt-in commands remain available outside this fixture. Control is never entered into the automatic registry. The existing scoped native inventory dispatch/probe correlates the real packet sender's inventory receipt, preserves mirrored-event deduplication and never replays a possibly applied native operation.

## Validation evidence and limits

Clean Gradle build passed against pinned stable 0.6.8 with Java 25. Only the existing two MerchantStoragePage inventory-removal warnings and existing Gradle deprecation notice remain.

`Auto-Validation/label-persistence.log` and `Auto-Restart/native.log` prove actual HyThief automatic registration of both coordinates with control unowned, on separate native server boots. The packaged registry/probe bytecode was also exercised against actual saved native containers: front and rear partial withdrawals of four copper bars each report once; insert/internal operations report zero; the 14-case native matrix includes full/partial/quick/swap/failed/sort/rapid transfers, two recipients plus untouched viewer, and combined inventories. Registry lookups exclude control and removal/rebuild clears entries.

The native verifier confirms spawn configuration/headroom, all fixed chest stacks, persisted command-free labels, the exact two NPC roles, stable frozen civilian body/head orientation, unfrozen guard, opaque guard-to-chest/approach geometry and walkable access. Saved-label mutation required native `markNeedsSaving()`; fresh-restart verification caught and corrected missing persistence before handoff.

Real client spawn, interaction UI and inbound transfer packets still require Firebeard's physical test. Automated native receipt tests are not a real network/UI session. Guard shielding was verified in the native test runs; prolonged free-running guard movement is not certified here. The frozen civilian is a deterministic future witness fixture; witness execution policy is intentionally deferred.

Individual reset executes the existing `Reset-Container-Theft.ps1` (CMD wrapper unchanged), restores only this new active save, verifies checksums and preserves dirty backups. Shared reset already contains all three worlds and is left unchanged. Its acceptance test executes the exact scripts on disposable copies of all three masters, preserving the two existing live worlds. The existing desktop shortcut still reaches the shared launcher.

## Standing development test-world rule

For Firebeard Hytale development, manual-mechanic tests must be purpose-built and plug-and-play: load â†’ spawn at stage â†’ walk to object â†’ test. Random-world searches, manual coordinates, entity/container marking and setup commands must not be part of normal testing. Fallback commands are debug tools only. A fresh reset/load must permit immediate testing.

Runtime save/master/rollback binaries and private permission data stay local under the established DevTesting ignore rule. Source, diagnostic version configuration, reset scripts, verifier sources and this note are committed as the development checkpoint; no public release is published.

## Final deployment acceptance â€” 2026-10-09

Final native validation used `Verification/Verify-Saved-World.ps1` against a disposable copy of the actual restored master. Evidence: `VerificationRuns/20261009-154713-9aa907b997fc458baec2f1fcf19d043f/native.log` and `reload-verification.json`. Front full withdrawal: 16 copper, exactly one report. Rear quick withdrawal: 16 copper, exactly one report. Each partial withdrawal: 4 copper, exactly one report. Actual control transfer: 4 copper, automatic registry remained unowned. All prior matrix cases passed again.

Final installed JAR SHA256: `020369D69BBADEBB13FD0B57257FDEE39C1970F133518BE494652FC8B6EE62DB`. Active JAR equals clean-build output, version container.2, target =0.6.8, one Local:HyThief identity. The null-container validation guard rejects a missing live native inventory before registration.

The individual live reset passed and active/master are checksum-identical (28 files). Shared-reset disposable-copy tests restored all three worlds, preserving dirty backups: HyThief 27 files, HyStash 42, Container Theft 28. `auto-boundary-verification.json` confirms both older masters/live saves, archived releases including v1.2, reusable arena and unrelated global mods remain hash-identical.

Cross-drive installation recovery: the first Python rename from C: to E: was rejected by Windows before removing the active container.1 JAR. Deployment resumed from that exact state using verified copy, then a same-drive staged replacement. Both original container.1 copies and the pre-final-validation container.2 JAR remain in the rollback directory. No reset scripts or desktop shortcuts were changed.

Run the reusable native verifier with Hytale closed and a completed diagnostic build. It compiles a disposable helper, extracts the exact packaged registry/probe bytecode, clones the local master, and boots the pinned server in an isolated VerificationRuns directory. It never modifies the master or active saves. Runtime worlds, rollback binaries and personal permissions remain local, intentionally uncommitted.
