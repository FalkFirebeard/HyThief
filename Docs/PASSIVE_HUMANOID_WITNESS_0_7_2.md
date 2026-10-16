# Passive humanoid personal theft witnesses — container.12

Hytale 0.7.2, revision `d769dc3d57881261a62858e1394f0e4de9a21eca`.
Development build: `1.3.1-container.12`, target `=0.7.2`.
Public 1.3.3 is unchanged. No publication is authorized by this checkpoint.

## Decision and scope

The personal-witness requirement is implementable for all five tested representatives without replacing native combat AI. **SUPPORTED below means native-server proof for these exact profiles, with Firebeard's connected-client confirmation still pending.** It does not certify every class/variant in a family or arbitrary renamed creator roles.

| Family / exact role | Passive config confirmed | Personal witness confirmed | Native entry | Native attack/damage confirmed | Native secondary response | Custom HyThief broadcast | Decision |
|---|---|---|---|---|---|---|---|
| Goblin / `Goblin_Scrapper` | YES | YES | offender-specific Hostile attitude + `LockedTarget` + existing `Combat.Default` | YES | peer stayed passive | NO | SUPPORTED |
| Trork / `Trork_Warrior` | YES | YES | one-recipient `TrorkEnemySighted` beacon carrying offender | YES | peer naturally joined `Chase.Attack` | NO | SUPPORTED |
| Outlander / `Outlander_Brute` | YES | YES | one-recipient `TrorkEnemySighted` beacon carrying offender | YES | peer naturally joined `Chase.Attack` | NO | SUPPORTED |
| Skeleton / `Skeleton_Fighter` | YES | YES | offender-specific Hostile attitude + `LockedTarget` + existing `Combat.Default` | YES | peer stayed passive; no flock created | NO | SUPPORTED |
| Zombie / `Zombie` | YES | YES | offender-specific Hostile attitude + `LockedTarget` + existing `Combat.Default` | YES | peer stayed passive | NO | SUPPORTED |

Other family variants remain unimplemented by this small adapter, rather than being enabled by a broad prefix/state-name guess. Ranger Trorks, fleeing Goblin classes, CAE/special undead, and arbitrary creator role IDs need their own verified entry contract before expansion.

## Passive configuration

Use the native `DefaultPlayerAttitude = Friendly` setting. For computed templates such as Skeleton Fighter, set the corresponding parameter at the concrete variant as well: the leaf can override the parent's default. Goblin and Predator detection can accept Neutral targets, so Neutral alone is not a universal peaceful setting.

Only the isolated proof asset packs change this setting. Production/development gameplay assets are byte-identical to container.11. No family is globally converted to Friendly. Normal movement, perception, weapons, combat instructions, role IDs, and native NPCGroup membership remain intact.

Trork/Outlander native greeting may store the player in `LockedTarget` and enter `NeutralPlayer.Default` without attacking. That target alone is not aggression. The adapter accepts this native peaceful greeting state for the two verified beacon profiles. Other adapters accept Idle/Alerted. Existing combat, flee, death, and unrelated scripted states are not replaced.

Use `Furniture_Kweebec_Chest_Large` in these proofs. The native Trork/Outlander crude-chest BlockChange/Interaction warning path is separate from owned-item theft; this chest avoids that unrelated native chest-warning fixture confound. No native chest behavior is disabled or edited.

## Implementation

Changed gameplay files:

1. `src-java/com/local/pickpocket/HyThiefContainerDiagnostics.java`: one call after paired removal confirmation, after inventory callbacks/locks unwind, inside the existing one-notification gate.
2. `src-java/com/local/pickpocket/HyThiefPersonalWitnessAlert.java`: the bounded personal-witness adapter.

Metadata only: `build.gradle`, `manifest.json` bump to container.12; Hytale target stays `=0.7.2`.

The adapter consumes the already-captured confirmed visual evidence. It requires `seen`, valid same-store offender/witness references, a living witness, and the existing ownership-scope match. It deduplicates witness references, so duplicate evidence from a request cannot cause duplicate personal handoffs. Every qualifying personal witness can receive a handoff; unseen neighbors are not selected here.

For Trork/Outlander, `BeaconBroadcast.post(witness, "TrorkEnemySighted", offender, 1.0, store)` posts to exactly one receiver. The adapter verifies the native slot is present/enabled/not already queued and checks the exact queued offender. The current native listener subsequently assigns the target and enters Converge/Chase; native role actions may broadcast to their normal group. No HyThief radius/group broadcast is added. Immediately after posting, the marked target can still be the old greeting target or unset; queued payload acceptance and subsequent native state/target observations are the relevant evidence.

For Goblin/Skeleton/Zombie, the adapter verifies the existing Combat.Default state, uses ECS `WorldSupport.overrideAttitude(offender, HOSTILE, 60)`, current `Role.setMarkedTarget(witness, store, "LockedTarget", offender)`, and ECS `StateSupport.setState(witness, "Combat", "Default", store)`. Sixty seconds matches the native damage-hostility override duration in the inspected combat trees. This does not synthesize damage. It bypasses the *rally acceptance lottery*, which is inappropriate for the NPC that personally confirmed the crime, but leaves actual attacks, movement, chase, target changes, retreat and disengagement to the original native tree. No flock is manufactured.

No old Role support getters, custom movement/combat loop, permanent attitude rewrite, event polling, fake damage, universal social signal, or custom group broadcast was introduced.

The existing caught notification and Kweebec/Feran ownership-selected alert adapter remain in place. The current development fixture still supplies its permitted witness-role list; arbitrary world-wide creator registration/public release enablement is not part of this checkpoint.

## Native evidence

Accepted final runs under `DevTesting/Passive_Witness_0_7_2`:

- `Run-Goblin_Scrapper-integrated-final`
- `Run-Trork_Warrior-native-greeting`
- `Run-Outlander_Brute-native-greeting`
- `Run-Skeleton_Fighter-integrated`
- `Run-Zombie-integrated`

Each used the exact candidate adapter, real native container transactions and an NPC dummy offender surrogate. Native Friendly player defaults and NPCGroup membership were also asserted. They are **not connected-player/packet/client tests**.

All five proved:

- four-second peaceful functional-AI window;
- unchanged native Open_Container asset; no removal on browsing/opening alone;
- insertion/rearrangement/empty-slot failed-transfer did not produce theft;
- unowned removal: no notification/handoff;
- wrong-faction removal: no notification/handoff;
- rear-facing removal: no notification/handoff;
- actual opaque-block LOS obstruction: no notification/handoff;
- crouched at 8 blocks: unseen;
- standing at 22 blocks: unseen;
- standing at 8 blocks, FOV+LOS true: exactly one caught notification and one personal delivery, even with duplicate evidence;
- native target retention, chase/combat and actual damage to the surrogate;
- secondary behavior observed without direct delivery to that peer.

Early rejected runs are retained locally. Their failures exposed proof setup assumptions: wrong copied parent selected for Friendly, Skeleton leaf parameter overriding its parent, and mistaking a peaceful native greeting target for aggression. They are not counted as successful results.

Existing container regressions all passed again: Kweebec FACTION, Feran FACTION, LOCAL with both witnesses, LOCAL with only Kweebec, LOCAL with only Feran. Four-chest ownership, scope filtering, notification count, FOV/LOS, 20/6 range tests, native responder target/state paths and mirrored geometry passed. Native inventory operations, chunk unload/reload, and a separate full-server restart preserved ownership/inventory without reattaching data.

Strict JAR comparison against preserved container.11 found only the new adapter class, the pipeline class, its nested command's debug line-table shift, and manifest metadata. The nested command's executable `javap -c -p` output is identical. Every packaged gameplay asset and every other Java class is byte-identical. Pickpocket frontend/backend, normal 15/4 perception, ownership persistence/format, container 20/6 values and legacy alerts were not edited.

Clean build passed against the installed 0.7.2 server hash `23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff` using JDK25/Gradle9.1. Existing two inventory removal warnings remain. Native logs retain custom-filter unknown-attribute warnings. Goblin combat also logged a weapon replacement-variable warning while native attacks still dealt damage; no unrelated weapon/asset repair was made. Watch this in the client if Goblin attacks fail.

## Small client fixture and reset

Separate save: **HyThief Passive Witness Test**. Existing general and Container Theft worlds/masters are preserved byte-for-byte.

Five selectable local masters, one family loaded at a time. Each contains two native chests (RED unowned, GREEN owned NEARBY), one primary NPC, one same-family secondary NPC, no permanent walls, readable labels, fixed test loot, and the existing 64 cobblestone starting stack. NPCs retain normal movement; they are not Frozen.

Initial geometry is identical across the five fixtures: primary `(0,64,9.5)`, theft pad `(0,64,1.5)` gives 8 blocks; secondary `(24,64,9.5)` is 25.298221 blocks from the pad and 24 blocks from the primary. Thus the secondary begins outside standing personal-witness range but inside the native 30-block Trork/Outlander rally distance. NPCs can move normally afterward; runtime diagnostics remain authoritative if they wander into a different position. Original short-range native secondary proofs deliberately faced the peer away at capture time.

Each final master is checked with a fresh native-server reload for the measured geometry, two NPCs/two chests, saved Friendly state, serialized ownership, native chest inventory, spawn and 64 starting blocks. Java author/probe plugins and HyThief JARs are not embedded in client masters; the installed candidate is authoritative. The proof Friendly asset pack is world-local.

Launcher: `DevTesting/Passive_Witness_0_7_2/Reset-Passive-Witness.cmd`. Choose 1–5. Close Hytale first. It stages and hashes the selected master, checks installed Hytale and candidate hashes, refuses redirected/unidentified paths, backs up any previous proof save, and replaces only `UserData/Saves/HyThief Passive Witness Test`. Existing reset launchers/shortcuts are not modified.

Client test, repeat per family:

1. Initial ready family is Trork Warrior. Open **HyThief Passive Witness Test** and confirm calm approach.
2. RED chest withdrawal: no caught message/reaction.
3. GREEN chest opening/browsing, deposit/rearrange: no theft reaction.
4. Stand on GREEN pad and withdraw one item while the primary faces you: one caught message, primary native attack/chase. Record any natural peer reaction; no propagation is required for success.
5. Quit Hytale; run the launcher and select the same family to reset. Use cobblestone to fully block primary LOS before GREEN withdrawal. Expect no caught message/reaction while no other NPC personally sees the theft.
6. Reset/select the next family. Optional crouched 8-block withdrawal should be unseen while the NPC stays beyond 6 blocks; use diagnostics for actual distance if it moves.

Connected-client result: **PENDING Firebeard**. Stop here for review/runtime feedback. Do not build a hub or add further container mechanics.

## Preservation and evidence

Candidate JAR SHA-256: `6b4c1500428674b4deb09c116ca6eb7d37360f9137e9a39ac527068d79dbe800`.
Rollback: `DevTesting/Passive_Witness_0_7_2/Rollback-container.11` (source/assets/config/build metadata and original JAR). Installed old JAR backup: `InstalledRollback/HyThief-1.3.1-container.11.jar` beside it.

Public 1.3.3 remains `3c39a85557edd8504f5d8e080762b9028be135049a504b372106fd89b626b13d`. No public source, archived release, other mod, existing test world, prefab, or pristine master is changed by this task. Main development worktree status is reported after commit; the separate public worktree's previously documented stat/index flags are not repaired here.

See `results.json`, `artifact-comparison.json`, `deployment.json`, reset evidence and native run logs in the checkpoint folder. Native/reference copied assets and save data stay local; only diagnostic source/summary/reset instructions are committed.

Sources: installed 0.7.2 role trees and copied native-server API/bytecode, prior `NATIVE_NPC_AGGRESSION_SOCIAL_ALERT_0_7_2.md`, [official WorldSupport API](https://docs.hytale.com/api/com/hypixel/hytale/server/npc/role/support/WorldSupport), and [official Update 7 modder notes](https://hytale.com/news/2026/10/modder-facing-patch-notes-update-7).

## Expanded-field follow-up: Goblin idle flavor

The later existing-world client fixture exposed a container.12 limitation: Goblin_Scrapper in IdleEat.Default personally sees the theft, but the adapter state gate skips it. The earlier Idle.Default proof is still valid; it did not cover every passive main state. Gameplay remains unchanged. See `CONTAINER_PASSIVE_CLIENT_FIELD_0_7_2.md` for exact evidence and the ready client fixture.
