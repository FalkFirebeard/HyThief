> Superseded by container.3 checkpoint 2: see `CONTAINER_THEFT_CHECKPOINT_2_VISUAL_WITNESS.md`. Custom owned-chest hints are removed; this note preserves historical checkpoint-1 evidence.

# Container Theft Test World: forward control and ownership hints

Current diagnostic JAR remains **HyThief 1.3.1-container.2**, stable target **=0.6.8**. Its SHA256 remains `020369D69BBADEBB13FD0B57257FDEE39C1970F133518BE494652FC8B6EE62DB`. No Java/backend source or global mod changes. The save-local `ContainerTheftFixture` asset pack is now **1.0.1**, layout version **2**.

## Firebeard: one run, zero commands

Load **HyThief Container Theft Test World**. Spawn remains `(0.5,64.1,7.5)`, looking +Z at the central civilian/front stage. Follow the numbered labels:

1. **CONTROL â€” UNOWNED**: the left front-side chest. Open it and remove items.
2. **OWNED â€” REAR / UNSEEN**: behind the civilian. Open it and remove items.
3. **OWNED â€” FRONT / WITNESSED**: directly in front of the civilian. Open it and remove items last.

No reset between these tests. No mark/clear/join/coordinates/setup commands. Each chest starts with its own 16 copper bars, 8 iron bars and 32 fibre in a native 36-slot container.

| Chest | Block position | Approach position | Hint | Attribution |
|---|---|---|---|---|
| Control | `-3 64 16` | `-2.5 64 15.5` | Normal native wording | Unowned; no owned-removal diagnostic |
| Rear | `0 64 4` | `0.5 64 5.5` | **Steal from Chest** | Automatically owned |
| Front | `0 64 16` | `0.5 64 15.5` | **Steal from Chest** | Automatically owned |

The old off-side control at `-4 64 -2` was removed from the new stage. Ownership labels are visible before removing anything; hint rendering on the real client remains part of Firebeard's acceptance test.

## Current behavior versus future behavior

**Current checkpoint is attribution only.** Valid rear/front withdrawals should each log one correctly attributed `[HyThief ContainerTest] Action=REMOVE`; control withdrawal should not. Native insertion/internal movement/sort/no-op behavior remains unchanged. None of the three chest tests should trigger a newly added theft warning or guard response.

The labels **WITNESSED** and **UNSEEN** describe staged geometry for the future witness checkpoint. Actual witness evaluation, NPC warning, Kweebec danger messages, guard aggression, sound, faction ownership and integrations are not implemented by this update. Future order remains control â†’ unseen rear â†’ witnessed front, with the potentially aggressive front test last.

The native `Kweebec_Sapling` remains frozen at `(0.5,64,10.5)` with body/head yaw pi, facing +Z. Its native template declares ViewRange 15 and ViewSector 180. The control approach is 5.83095 blocks away at 30.96376 degrees from forward; the front approach is 5 blocks directly forward. Both approach eye-level block sightlines are clear. This is geometry/perception-configuration validation, not proof that the frozen NPC currently runs witness AI. No perception or crouch calibration was changed.

The native Razorleaf remains unfrozen in the shielded alcove. Native verification checks opaque guard sightline intersections to all three chests and player approaches, and walkable access around the south end of the x=6 wall. Existing range markers and reusable prefabs remain intact. A post-reset check exposed the old idle-wander escape: its native motion component hard-codes radius 10 and does not consume the fixture WanderRadius=1 setting. The save-local variant now sets only `Modify.MotionComponent = "$Null"`, using the native template nullable idle-motion hook. The guard stays unfrozen; inherited beacon/alert/search/attack branches and movement controller remain unchanged. A 60-second native idle check measured zero displacement and preserved shielding. No hostility signal was sent; combat execution itself remains reserved for the future witness checkpoint.

## Test-only native ownership hint

New asset: `Pack/Server/Item/Items/Container/HyThief_Test_Owned_Kweebec_Chest.json`.

It inherits `Furniture_Kweebec_Chest_Large`, overriding only the block interaction hint and explicitly retaining `Use = Open_Container`. The native model/scale, textures, open/close states, capacity, sounds and other parent behavior are inherited; no replacement custom UI. Localization in `Pack/Server/Languages/en-US/server.lang` defines `server.hythief.containerFixture.stealFromChest` as **Steal from Chest**. The control uses the untouched native chest asset and its existing `server.interactionHints.openDoor` key. The two automatically owned entries now validate the test-only block ID; the control stays outside the registry.

This pack resides only inside this development save's `mods/ContainerTheftFixture`; it is not installed as a global chest override and is not added to the production HyThief JAR. The named-world/UUID/marker gating and native attribution code are unchanged.

Verified source references: [stable block asset reference](https://docs.hytale.com/assets/item/block/blocks/), [native container interaction reference](https://docs.hytale.com/api/com/hypixel/hytale/server/core/modules/interaction/interaction/config/server/OpenContainerInteraction), [official Update 6 hotfix notes](https://hytale.com/news/2026/8/hotfixes-update-6). The installed stable Assets.zip supplied the native chest definition and Kweebec template; the live installation was read-only. No discrepancy affecting this hint approach was found. The simple native hint string meets this checkpoint; no custom second-line ownership UI was introduced.

## Validation, rollback and handoff

`Layout-Authoring/native.log` proves native asset inheritance/localization and authoring. `Layout-Reload/native.log` proves a fresh saved-world load automatically registers both owned chests with the new live block IDs, preserves the numbered labels/control position/frozen orientation, and excludes the native control. The packaged attribution probe/registry and existing 14-case matrix passed on native containers. A shared recipient took four copper from control, rear and front in that order without reset between transfers: twelve received, zero control reports, two owned reports.

The reusable `Verification/Verify-Saved-World.ps1` now checks this layout, ownership hints, normal Open_Container wiring, front/control geometry and one-run sequence. It still operates only on disposable master copies. Physical client hover hints and chest opening/transfer UI remain Firebeard's acceptance test.

Pre-change master, active save, JAR, verifier and notes are preserved in `Rollback-layout.1-before-forward-control`. Individual reset restores the new stage and its fixed inventories; reset scripts and shared/desktop launchers are unchanged. Shared-reset verification uses disposable copies of all three worlds, keeping both older live saves/masters untouched. No public release is published.

Final idle-staging evidence: `Layout-Idle-Guard-Verification/native.log` and `reload-verification.json`, 60 seconds, displacement 0.0. The prior failed disposable run is preserved in `VerificationRuns/20261009-160336-c1225c599b6c4bf787c52e026cf32ef7/native.log`; it did not change the active save/master. The reusable verifier accepts `-LongGuardCheck` to repeat the 60-second check; default checks remain short. This is a test-save-only guard idle change, not a production NPC policy change.

Final acceptance validation (2026-10-09): the freshly reset 30-file master and active Container Theft save are checksum-identical. The final reusable native-server run passed at `VerificationRuns/20261009-160925-dba535542ece4034b3017185c61beb98`, including automatic ownership, hint localization/Open_Container inheritance, layout geometry, fixed inventories and the control/rear/front one-run attribution sequence. Final shared-reset verification passed on disposable copies of all three current masters, including dirty-save backup checks; the existing HyThief and HyStash live worlds were not reset. `layout-boundary-verification.json` confirms unchanged Java source, global installed mods, archived releases, reusable prefabs and both older worlds/masters. Firebeard must still confirm real-client hints, opening and item transfers; no witness/guard consequence is implemented by this checkpoint.
