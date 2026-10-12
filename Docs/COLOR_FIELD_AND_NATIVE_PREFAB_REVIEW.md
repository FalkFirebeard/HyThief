# Mirrored color test field and native PREFAB ownership review

2026-10-12. Gameplay remains the real-client-proven 1.3.1-container.11, stable target =0.6.8. This checkpoint changes only the test-world fixture, author/verification helpers, reset data and documentation. No gameplay Java/assets changes, new JAR version/deployment, ownership/alert/range changes, responder role changes, PREFAB implementation, creator tools or public release.

## Color field: exact geometry

Four identical native Furniture_Kweebec_Chest_Large chests lie along X=0 at Y=64. Each has 16 copper bars and 8 iron bars, unchanged persisted Owned/Scope/Faction semantics, native Open_Container Use, and two matching colored floor tiles directly in front (X=-1 and X=0, Y=63). Stand on their center seam X=0. These two tiles form one clearly colored pad, preserving exact player symmetry rather than shifting a single tile center half a block sideways. Labels identify color and ownership name; numbers/left-right descriptions are not required.

| Color / ownership | Chest origin (X,Y,Z) | Player pad (X,Y,Z) | Distance to EACH witness | Mirrored facing offset |
|---|---|---|---:|---:|
| RED / CONTROL UNOWNED | (0,64,-6) | (0,64,-6.5) | 10.259142 | 43.025066 degrees |
| BLUE / FACTION Kweebec | (0,64,-2) | (0,64,-2.5) | 8.077747 | 21.801409 degrees |
| GREEN / NEARBY | (0,64,2) | (0,64,1.5) | 7.566373 | 7.594643 degrees |
| YELLOW / FACTION Feran | (0,64,6) | (0,64,5.5) | 9.013878 | 33.690068 degrees |

Native marker IDs are Cloth_Block_Wool_Red, Cloth_Block_Wool_Blue, Cloth_Block_Wool_Green and Cloth_Block_Wool_Yellow, verified in installed stable Assets.zip. Native-server validation checks each actual marker and persisted centered label. It also counts exactly four chest origins (excluding native multi-block fillers).

Witnesses remain the existing stationary civilian fixture roles:

- Kweebec_Sapling_HyThiefWitness at (-7.5,64,0.5), heading +X (yaw -pi/2).
- Feran_Civilian_HyThiefWitness at (7.5,64,0.5), heading -X (yaw +pi/2).

Reflection across X=0 maps one witness/facing onto the other and leaves every player pad invariant. Their distance to each actual chest origin is also equal: RED 9.924717, BLUE 7.905694, GREEN 7.648529, YELLOW 9.300538. These origin distances differ from player distances because theft witnessing checks the player, not the block origin. All standing player distances are below unchanged range 20; all exceed crouched range 6. Native visual sector and fresh LOS checks pass for both witnesses at every pad. Eye heights/models differ naturally between families; actual native LOS/FOV results, rather than invented identical eye geometry, confirm equivalent unobstructed test conditions.

Responders remain mobile, unfrozen and unmodified in role, farther behind their corresponding civilians:

- Kweebec_Razorleaf at (-21.5,64,0.5).
- Feran_Sharptooth at (21.5,64,0.5).

| Alert path | Native measured distance |
|---|---:|
| Kweebec witness -> Razorleaf | 14 |
| Kweebec witness -> Sharptooth | 29 |
| Feran witness -> Razorleaf | 29 |
| Feran witness -> Sharptooth | 14 |

**Current LOCAL 30-block range is sufficient.** Cross-side paths have a one-block margin; no radius change. Both responders are >20 from every player pad: RED 22.610838, BLUE 21.708293, GREEN 21.523243, YELLOW 22.073740. Native helper checks these using live Transform positions. Native response tests additionally prove both roles leave Idle, hold the exact offender and move in their native Attack.Default / Attack.Ranged states from either witness. No custom movement or attack code.

The open cobble field remains flat, without permanent walls, shelters or duplicate stations. Current saved spawn (0,64.1,10.5) faces -Z toward the chest line (yaw 0); spawn label names all four colors. Walk alongside the line to inspect pads, then stand on the selected colored center seam. Starting inventory retains 64 Rock_Stone_Cobble in hotbar slot 1 for manual LOS walls. Keep pursuit routes open.

## Preservation / native validation / reset

- Previous master, actual active save, helper source, README, stations and checksum rollback: DevTesting/Container_Theft_Test_World/Rollback-color-field-20261012-163359. Prior remote checkpoint d4de15e remains available; gameplay rollback f90bbcf and original releases are untouched.
- Author run: FactionVerificationRuns/20261012-163425-97c7c3a451dc4704af7ef7399a2cd109/native.log.
- Native saved-field Feran-only witness response: 20261012-163613-0382f90ca2d94d109fbfa6b0635bc1fd/native.log.
- Native saved-field Kweebec-only witness response: 20261012-163705-abfb590f21f14e10935b7e962f8cee9c/native.log.
- Final saved spawn + both-visible response: 20261012-163944-0425feb40d034016a47c1119bffdbb75/native.log. Each response trial had exactly one caught message and both responders. Manual temporary blocks were created only in disposable test copies.
- Master was promoted from Color-Field-Candidate-20261012, the clean INPUT to those response trials. No combat run, alarm queue, offender actor or temporary LOS wall was promoted. Master role packs are the unchanged previous files.
- Reset-Container-Theft.ps1 archived the prior active save at Archives/20261012-165421-7ee26b5f6af54726a45e3c8d2a04ed49, restored all 33 files, and verified refreshed master checksums. Independent post-reset SHA256 comparison passed for every file and exact file count. Master unchanged by reset.
- Other existing Saves and HyThief test-world/arena files retain the same size/mtime snapshot as before this task. This is an unchanged-file metadata check; not a claimed full content hash of every unrelated world. This task only wrote the container fixture.
- Installed JAR remains SHA256 08ddb2033d942c901a52de3ef20429e83f21db7212bf676e5287f05b0123c233. Only the requested integrity clean build was performed; no new version or Mods swap. The helper's pre-existing getFiller deprecation warning remains development-only.
- An initial attempt to feed a complete verifier run back into the runner copied its classes directory and was rejected before server launch. Subsequent validations use a clean save-only candidate, avoiding copied probe artifacts.
- Commit tracks the reproducible author/validator, spawn preparation, stations metadata, README and this note. Actual native save/player seed files and rollback copies retain the existing local-only repository policy. Prepare-Faction-Field.ps1 followed by Verify-Faction-Field.ps1 -Author recreates the field; reload verification must precede master promotion. Response runs must never become Master.

The native automation confirms fixture behavior; Firebeard must still assess the new field's real-client readability and usability. Existing container.11 gameplay proof remains valid and is not relabeled as a new build.

## Native PREFAB research: what identity exists

Read-only recheck against the installed stable server and Assets.zip, the previous Docs/PRODUCTION_CONTAINER_OWNERSHIP_ARCHITECTURE.md and CONTAINER_11_PROOF_AND_PREFAB_PROPERTY.md, and stored API/bytecode evidence in DevTesting/Production_Ownership_Reference_0_6_8. Pinned and installed server hashes still match DCD2956CC65B950084650EADD56B889CF51F7610C127C3DAAC1F74DC471FEC97.

| Mechanism | Finding | Remaining gap |
|---|---|---|
| FromPrefabInstance | A genuine codec-backed EntityStore prefab-placement ID attached by world-generation entity handling; entity provenance can survive save/load. | Not the same as a shared ChunkStore chest membership component; delayed marker NPCs and other placement paths must not be assumed to inherit it. |
| FromWorldGen / FromPrefab | Origin markers, not a complete placed-building ownership relation. FromPrefab can be cleared. | Marker existence does not tell which Shop A chest/NPC share a placement. |
| PrefabPasteEvent / PrefabPlaceEntityEvent | Runtime operation ID and entity-holder access. Current PrefabUtil gets paste IDs from AtomicInteger initialized at zero. | Process-local paste IDs cannot directly serve as durable globally reused ownership keys; event exposes no universal per-block property data. |
| PrefabUtil native holder consumers | Flag-based paste accepts both ChunkStore and EntityStore holder consumers. | A small supported integration seam for a controlled copy path; not a guarantee covering native editor, every generator and nested spawner. |
| SpawnMarkerReference | Native persistent reference to the actual spawning marker; useful for fetching its provenance. | Container does not automatically reference the marker; multiple markers may belong to one building. Marker unload/deletion and respawn inheritance need proof. |
| BlockSpawner | Current component exposes only blockSpawnerId (table asset ID), not a per-property instance ID. | Same table is reused by many buildings; table replacement sets final block/component holder and may discard annotations on the placeholder. |
| GeneratedBlockChunk / PrefabEntry | Inspected block-chunk API exposes block/section/environment data, not a matching prefab owner getter. PrefabEntry is asset path/pack/display metadata. | No verified persistent final-block-to-worldgen-prefab relationship to read at theft time. This is a scoped API finding, not proof that no internal placement context could ever be exposed. |
| SpawnLineage, NPCGroup, attitude, reputation, flock | Native lineage or role/AI grouping. | Sharing faction/role/flock is not verified proof of the same building. Do not change these systems just to express property ownership. |
| Trigger-volume worldgen groups | Native worldgen/paste remapping patterns use prefab provenance. | Optional authored trigger context; not automatic ownership on every chest/NPC. Repurposing it would be more infrastructure than necessary. |
| Prefab previews / world structures | Preview key/layer/tint and world-generation configuration. | Not persistent runtime membership for the actual chest and NPC. |

[Official FromPrefabInstance API](https://docs.hytale.com/api/com/hypixel/hytale/server/core/modules/entity/component/FromPrefabInstance) explicitly describes persistent world-generation entity placement identity, including distinguishing co-located placements. That is positive evidence for the ENTITY subset; it does not assert all chest blocks or later-spawned NPCs carry it. [TriggerVolumeWorldGenHandler API](https://docs.hytale.com/api/com/hypixel/hytale/builtin/triggervolumes/prefab/TriggerVolumeWorldGenHandler) distinguishes the worldgen entity path from BuilderTools paste events. [Stable Update 6 notes](https://hytale.com/news/2026/8/update-6-patch-notes) recommend flag-based PrefabUtil overloads. No discrepancy with the inspected binary was found; coverage gaps remain.

Raw asset examples re-read from installed Assets.zip:

- Goblin_Thief_Chest.prefab.json stores an ItemContainerBlock component holder on a direct chest block.
- Kweebec_Autumn_Shops_001.prefab.json instead has a Zone1_Kweebec_Tier3 BlockSpawner and a Kweebec_Merchant NPC_Spawn_Marker entity. Final chest and live merchant can be created at different times, explaining why a direct pasted-entity origin field is insufficient by itself.

**Answer:** reliable native worldgen entity provenance exists. A reliable, universally available shared native container/NPC prefab identity has NOT been verified. The previous partial-provenance concern remains; neither asset name, raw faction, nearest NPC nor arbitrary creator string should be treated as native ownership identity.

## Smallest next checkpoint, not a new property framework

Recommend an isolated **two-copy native-prefab provenance proof** before implementing production PREFAB. Use one simple chest + civilian + responder prefab, then place two copies with identical faction/role definitions. Inspect actual native chest holders, spawn-marker provenance, spawned NPC provenance and restart persistence at the supported placement boundary. First establish whether one real native context can be read/stamped for all members on that path. Include marker-spawn rather than testing only directly pasted NPCs.

If native identity is complete for that verified path, reuse it with world/store context. If the chest/late-NPC bridge is missing, add only one persisted prefab-context token to participating native holders at the placement/spawn boundary. A generated per-placement UUID is the minimal safe fallback for paths whose native operation ID is not persistent; it is not a registry, property network or security framework. Fresh copies must not share the token. There is no need for building polygons, linked prefabs, aggregate ownership, creator tooling or combat AI.

Runtime PREFAB branch would be:

`owned withdrawal -> scope PREFAB -> witness same prefab context -> existing stance/range/FOV/LOS -> one caught notification -> same-context ready responders -> addressed native beacon -> existing native role response`

Validate context on BOTH witness and responder selection. Keep the matching token separate from faction. Existing addressed BeaconBroadcast.post is reusable after membership filtering; do not emit an unrestricted LOCAL alert to unrelated properties. Reuse the listener/native behavior when appropriate rather than implementing another combat path. Investigate any later native alarm rebroadcast in the proof before claiming perfect property isolation. A missing context must remain explicitly unbound, never silently become NEARBY/FACTION.

Prospective modification seams, none implemented here:

- OwnershipProofData.CODEC/accessors/clone: optional bound prefab-context field with old defaults retained.
- PersistentContainerOwnership.eligible: recognize valid bound PREFAB; resolve/source native window/inventory identity stays unchanged.
- ContainerOwnershipPolicy.rejection: same-context witness comparison before existing visual checks.
- HyThiefContainerAlert.deliver: small PREFAB branch with same-context recipient filter before existing native post/readiness logic.
- PickpocketPlugin registration + one tiny entity membership component only if native marker/provenance lookup cannot reliably supply that context.
- A bounded native placement/spawn bridge for the verified path, preserving final block holder and marker/NPC context through save/reload/respawn. No automatic claim for all editor/worldgen paths.
- Diagnostics may log the context and rejection reason; existing ContainerVisualWitness.capture already calls the policy seam.

Unchanged foundation: successful withdrawal attribution, transfer packet/native inventory handling, chest Use/windows, visuals/FOV/LOS/ranges, exactly-once caught notification, FACTION and NEARBY policy/alerts, native BeaconSupport/StateSupport/marked-target/LastSeen/pursuit/combat, pickpocket frontend/UI/loot/catch and supported roles. No production PREFAB code, creator tools or new development JAR were made in this task.

## Firebeard handoff

Launch the reset Container Theft Test World using the unchanged container.11 JAR. Stand on the selected colored center seam. RED is safe, BLUE/YELLOW preserve faction tests, GREEN preserves both-responder LOCAL tests with either visual witness. Reset between caught trials. Use your matching cobble stack for temporary LOS walls and leave responder pathing open. All current fixtures are native-server verified; new color/layout readability awaits real-client review. Stop for Firebeard review before starting PREFAB implementation.

## Power-outage recovery close-out (2026-10-12)

Recovery found main at d4de15e with five intact, unstaged fixture modifications; no unexpected untracked files or Git locks. Git fsck found no corruption (unreferenced dangling objects were retained). No zero-length/truncated source or temporary editor files were found in the inspected source/docs/helper directories. All fixture PowerShell files parsed successfully. JSON and ZIP CRC checks passed for Master and the active save. Previous rollback/candidate/native evidence survived; no restore or discard was necessary. The interruption stopped before writing this documentation and before committing/pushing.

Requested recovery clean build succeeded against Java 25 / pinned Hytale 0.6.8 in 1m 7s. The same two inventory deprecation warnings and Gradle deprecation notice remain. Every rebuilt JAR entry is byte-identical to its counterpart in the proven installed container.11 JAR; the installed file was not replaced. Exactly one active plugin identity Local:HyThief exists, Version=1.3.1-container.11 and ServerVersion==0.6.8 (manifest value '=0.6.8'). Build log: DevTesting/Container_Theft_Test_World/Recovery-20261012/clean-build.log. Prior build artifact was preserved there before cleaning.

Post-recovery native saved-master validation passed all four chests, colors, ownership, spawn, labels, equal witness distances, actual FOV/LOS, open responder paths and four 14/29-block alert paths. Evidence: FactionVerificationRuns/20261012-172234-01beb8fc639f43948ab34c7743d21295/native.log. Prior both-visible and each-single-witness combat trials remain valid; no gameplay changes justify repeating them. Active save remains an exact 33-file match to Master, so no redundant reset was performed during recovery. 1,439 pre-existing files in other Saves/test-world/arena locations retain their before-task size/mtime. No gameplay source changed; PREFAB remains research only. Finish by committing and pushing fixture helpers/metadata and documentation, then stop for review.
