# Existing Container Theft Test World — passive client sections

Hytale **0.7.2**, revision `d769dc3d57881261a62858e1394f0e4de9a21eca`.
Gameplay remains **HyThief 1.3.1-container.12**, target `=0.7.2`.
JAR SHA-256 remains `6b4c1500428674b4deb09c116ca6eb7d37360f9137e9a39ac527068d79dbe800`.
No gameplay code, public release, or development JAR was changed/rebuilt.

## World and layout

The **existing HyThief Container Theft Test World** is expanded in place: same save name, world UUID, starting player, spawn, existing reset launcher and desktop shortcut. No new world or shortcut was created. The earlier separate passive proof save is untouched and is not needed for this handoff.

Original four chests/area retain their exact layout and meanings:

- RED: unowned control.
- BLUE: Kweebec FACTION.
- GREEN: NEARBY/LOCAL.
- YELLOW: Feran FACTION.

Five simple additional sections follow the gray path beyond spawn. Purple Trork is straight ahead; orange/cyan are left/right on the first branch, white/black on the farther branch. Each has one native chest and one NPC. There are **nine chests and nine NPCs total**, with no optional peers or duplicate blocked-LOS stations.

| Color | Exact NPC role | Chest (X,Y,Z) | Player seam (X,Y,Z) | Initial NPC (X,Y,Z) | Ownership |
|---|---|---|---|---|---|
| Orange | `Goblin_Scrapper` | -36,64,48 | -36,64,47.5 | -36,64,55.5 | FACTION / `Goblin` |
| Purple | `Trork_Warrior` | 0,64,48 | 0,64,47.5 | 0,64,55.5 | FACTION / `Trork` |
| Cyan | `Outlander_Brute` | 36,64,48 | 36,64,47.5 | 36,64,55.5 | FACTION / `Outlander` |
| White | `Skeleton_Fighter` | -18,64,88 | -18,64,87.5 | -18,64,95.5 | FACTION / `Skeleton` |
| Black | `Zombie` | 18,64,88 | 18,64,87.5 | 18,64,95.5 | FACTION / `Zombie` |

All five use the **already-existing native NPCGroup identifier** and current FACTION ownership codec/policy. No new ownership mode, identifier, Java logic or faction broadcast was needed. The generic diagnostic faction display still says UNRESOLVED for these families because that display helper names only Kweebec/Feran; native membership/scope eligibility is separately verified and succeeds.

Every initial witness distance is exactly **8 blocks**, standing FOV=true, head yaw=0 facing the pad, LOS=true, elevation matched. Nearest other NPC distances are 36.878178 blocks for the first row and 36.715120 for the second. New witnesses are also beyond 20 blocks from every original station pad. Thus range isolation and family ownership filtering both prevent cross-witnessing at reset positions. The original intentional shared Kweebec/Feran visibility remains unchanged.

NPCs retain their normal native AI, movement, perception, combat and idle flavors; none is Frozen. Reset restores initial positions/facing/passivity. They can move/turn after the world starts, so runtime LOS/FOV remains authoritative. Reset between caught cases to avoid carrying native pursuit into another test.

## Passive configuration and contents

The world-local asset pack `mods/ContainerPassiveClientFixture` reproduces the proven native **DefaultPlayerAttitude=Friendly** settings only: the five private HyThief role roots and Skeleton Fighter's concrete parameter override. A structured comparison verifies every other JSON value is unchanged. Existing Kweebec/Feran fixture content is unchanged and its ZIP remains byte-identical; untouched tracked JSON uses its existing Git bytes. Vanilla/global roles and other worlds are unaffected.

Every new chest is normal `Furniture_Kweebec_Chest_Large`, native `Open_Container`, with 16 copper bars and 8 iron bars. This also avoids the separate native Trork crude-chest warning interaction. There are no permanent LOS walls. The original starting hotbar retains 64 `Rock_Stone_Cobble` for manual walls. A simple four-wide wall at Z=chest Z+4, between chest and NPC, is sufficient for the tested LOS control; build it tall enough to cover head/eye rays.

Expected primary test while standing: opening/browsing/depositing/rearranging stays safe; a visible owned withdrawal produces one caught message and the personal native combat handoff. Crouched at the initial 8-block seam should be unseen under unchanged 20/6 container range. Normal 15/4 perception is untouched.

## Confirmed existing Goblin limitation — do not call this a full pass

Two unmodified native runs of the expanded Goblin station found:

`owned withdrawal → FACTION Goblin match → visual seen (8 blocks, FOV/LOS true) → one caught notification → PERSONAL_WITNESS_SKIP State=IdleEat.Default`.

Container.12's personal adapter accepts Idle/Alerted for Goblin, but **IdleEat is a separate main state**. Its existing state gate therefore rejects this calm native eating state. This is a gameplay adapter limitation, not an ownership, layout, LOS or passive-configuration failure. The fixture does **not** disable eating, force Idle, or hide the failed run to manufacture a pass. No gameplay repair is included because this assignment is fixture/client preparation only.

The previous isolated Goblin proof in Idle.Default remains valid for that state. The broader connected-client behavior is pending and may reproduce the IdleEat skip. Also observe the previously logged Goblin weapon/animation replacement-variable warning if it does attack. Report the exact state from logs before any subsequent code change.

Trork Warrior, Outlander Brute, Skeleton Fighter and Zombie passed native FACTION withdrawal → one caught message → exact offender target → native combat/damage in the expanded save. Native peer propagation is not retested here because no extra peers were included; nothing changes native social behavior or adds custom broadcasting.

## Validation and preservation

- Actual installed version/revision and server/JAR hashes checked.
- Native authoring uses a disposable, temporarily paused copy to preserve initial saved placement. The approved master/active world have `IsTicking=true`, `IsAllNPCFrozen=false` and current native codec version 4. No pause is retained in the handed-off world.
- Original area native fingerprint `dceeeb1c852f91ba1e1f89292b2538b13db5fba601e42bc24948547342f8ef97` matches before expansion, after authoring, and after save/reload. It covers original block IDs/fillers/rotations, labels, NPC positions/head rotations/state, ownership and inventories.
- Fresh native structure validation confirms all nine chests/NPCs, each role/attitude/native group, contents, exact distance/FOV/LOS, blocked-LOS rejection/recovery and crouched range rejection.
- Native open/close, insertion and internal rearrangement produce no false theft in all five sections.
- Original Kweebec FACTION, Feran FACTION, LOCAL/both, LOCAL/Kweebec-only and LOCAL/Feran-only response regressions pass with all nine NPCs loaded. Legacy test assertions only adjust counts/load bounds for the added fixture; original mechanic assertions remain.
- The actual existing desktop `Reset HyThief Container Test World.lnk` launches the unchanged `Reset-Container-Theft.cmd` / `.ps1`. The normal guarded reset was executed, archived the previous active save and restored all 41 master files exactly.
- A disposable copy of the actual desktop-reset active save is native-loaded again. No active world is used as a writable native-test run.
- Starting player data, spawn (0,64.1,10.5), 64 building blocks, other test worlds, gameplay source/assets and public release remain unchanged.
- Replacing the master exposed pre-existing cached LF/CRLF drift in otherwise unchanged tracked JSON. Those unchanged files were restored to their existing Git bytes; new/changed JSON uses LF and the existing Master `-text` attributes preserve exact bytes. The original world clock is also restored after the authoring server's one-frame advance. Final checksums are generated from the handed-off bytes and compared to staged Git blobs before commit.

One earlier combat-probe setup mistakenly attempted a surrogate attitude override on unrelated non-combat civilian roles, whose optional native attitude-memory component is absent. The diagnostic was corrected to initialize only the selected proven combat representative. This was test-only; no role/API workaround was added to the game.

## Files and rollback

The fixture modifies the existing Master save, its world-local Friendly pack, marker/config, native chunk save data and `master-checksums.json`; updates the active testing README; adds the author/validator, station table, summaries and this note. It does not alter existing reset scripts/shortcuts, gameplay source/JAR, releases, reusable prefabs or another project. See `changed-files.txt` beside the checkpoint summary for the exact committed path list.

Checkpoint: `DevTesting/Container_Theft_Test_World/Passive_Client_Expansion_0_7_2`.
Rollback: `Rollback/Master`, `Rollback/Active`, original reset/README/checksum files, and `PreSwapMaster` there. The existing reset also preserves the dirty active world in the normal container `Archives` folder.

Generated/native-run Java plugins stay outside the Master. Client runtime uses only the already installed container.12 plus the world-local fixture assets. No JAR was rebuilt, no new feature/publication began, and no larger hub was built.

## Short client handoff

1. Open **HyThief Container Theft Test World**. Follow the gray path behind spawn to **PURPLE — Trork Warrior**.
2. Stand on its colored center seam. Open/browse, deposit and rearrange first; then withdraw one copper bar while standing visibly in front.
3. Safe actions should cause no reaction; withdrawal should show one caught message and native attack/chase. Reset with the existing desktop shortcut before another caught/blocked-LOS case. Orange Goblin has the known IdleEat limitation above.

Connected-client result remains **PENDING Firebeard**. Stop for that result before a gameplay repair, role rollout, or next container mechanic.
