Current test: 1.3.1-container.5; fixture pack 1.0.3. See Docs/CONTAINER_THEFT_CHECKPOINT_2_LASTSEEN_HANDOFF.md for actual container.4 evidence and native LastSeen handoff correction. CONTROL then REAR then FRONT standing; native chest interaction unchanged.

Current test: 1.3.1-container.4. See Docs/CONTAINER_THEFT_CHECKPOINT_2_RUNTIME_INGRESS.md for the Quiche ingress repair and exact retest. Native chest interaction and master unchanged.

# Container theft checkpoint 2: native chest and visual witness

Development build: **1.3.1-container.3**, target **=0.6.8**, fixture pack **1.0.2**, layout **3**. No public release.

## Firebeard acceptance test

The active **HyThief Container Theft Test World** has been freshly reset. Load it, remain **standing**, use the numbered chests in this order, without commands or resets between tests:

1. **CONTROL â€” UNOWNED**, front-left: open and take items. Expected: one interaction opens native inventory; no theft/guard response.
2. **OWNED â€” REAR / UNSEEN**, behind the Sapling: open and take items while remaining behind it. Expected: one interaction opens native inventory; owned removal is recorded, visual witness is UNSEEN, no guard response.
3. **OWNED â€” FRONT / WITNESSED**, directly forward of the Sapling, last: open and take an item while standing at the front approach. Opening/browsing alone must do nothing. Expected: the successful withdrawal is witnessed and one Razorleaf receives `Kweebec_All_Danger` with the player target, then pursues/attacks using its native role.

Do not judge the third test by opening alone: the crime candidate begins only after successful item withdrawal. No random caught roll and no forced inventory closure were added. All chests display native interaction wording; ownership is indicated by the unchanged numbered labels.

| Station | Chest block | Intended player approach | Ownership |
|---|---|---|---|
| Control | `-3 64 16` | `-2.5 64 15.5` | Unregistered |
| Rear | `0 64 4` | `0.5 64 5.5` | Automatically registered |
| Front | `0 64 16` | `0.5 64 15.5` | Automatically registered |

Real-client one-click inventory opening, transfer packet handling and native player combat remain the acceptance checkpoint. Native API tests do not substitute for that session.

## Two-click regression and native restoration

The removed `HyThief_Test_Owned_Kweebec_Chest` asset changed the base hint and explicitly reused `Open_Container`; it did not define a custom Java opening interaction. A disposable run of the old checkpoint confirmed:

| Old custom base state | Resolved native block ID | Hint |
|---|---|---|
| Base | `HyThief_Test_Owned_Kweebec_Chest` | `server.hythief.containerFixture.stealFromChest` |
| OpenWindow | `*Furniture_Kweebec_Chest_Large_State_Definitions_OpenWindow` | `server.interactionHints.openDoor` |
| CloseWindow | `*Furniture_Kweebec_Chest_Large_State_Definitions_CloseWindow` | `server.interactionHints.openDoor` |

Thus the custom base crossed into the parent's generated native state family on opening, explaining the observed hint fallback. Firebeard's first-click inventory failure is confirmed runtime evidence; the precise client/page rejection was not reproduced by the offline metadata test. Attribution intercepts only move/quick-move inventory packets, not chest-open interactions. We removed the entire fixture variant rather than guessing at a custom opening repair.

Both owned placements now use the exact native `Furniture_Kweebec_Chest_Large` family, with native hint, animation, states and `Use = Open_Container`. The override JSON and localization line were removed from the save-local pack and its source. The control remains native too. No global chest override or replacement UI exists.

Ownership uses world/block coordinates and the live container identity. The refresh path accepts native base/OpenWindow/CloseWindow `BlockType` IDs. Native open/close API tests preserved the same container and ownership registration. An artificial block replacement is distinct from a state transition and was not used as evidence for native closing.

## Successful withdrawal and exact-moment visual evidence

The Checkpoint-1 transfer algorithm is preserved. `ContainerRemovalProbe` still requires successful native move transactions, positive source quantity loss and paired receipt into the responsible player's inventory. It now has an optional capture callback when the positive source change is observed. The visual snapshot is retained by removal identity, then used only if paired receipt confirms that removal.

`ContainerVisualWitness` is restricted to the one living, initialized `Kweebec_Sapling` at the controlled fixture position `(0.5,64,10.5)`, in the named/UUID-gated development world. Missing/ambiguous references fail closed. It reads actual head yaw, uses native `NPCPhysicsMath.inViewSector`, checks distance and calls native `PositionCache.hasLineOfSight`. It uses a fresh private native cache with standard block opacity (`setOpaqueBlockSet(-1)`), avoiding stale results from the frozen fixture without clearing the NPC's AI cache or targets.

Player position, stance, distance, sector and LOS are captured synchronously in the source transaction callback. No world tick interleaves that capture with the transfer. Evidence persists even if position changes before paired reporting. The downstream alert is delivered after the native inventory operation and listener cleanup, outside inventory locks.

Opening, browsing and closing have no witness callback. Insertion, rearrangement, sorting, failed/full-destination moves and unowned control withdrawals do not produce a paired owned-removal crime. Manual opt-in diagnostics in other worlds remain attribution-only; witness/alert processing is restricted to this automatic fixture.

There is no hearing, scent, proximity-only, remembered-target or existing-hostility witness shortcut. Crouch is logged but does not reduce visual range in this checkpoint; perform acceptance while standing. Crouch-aware witness policy is deferred and can be inserted at the visual range gate.

## Controlled native visual baseline

Installed stable `Template_Kweebec_Sapling` declares **15** view range and **180 degrees** view sector. The current HyThief JAR also contains historical Sapling/template declarations at **4**. A fixed 15-block witness check against that legacy role would not be an honest match of configuration and evidence.

For this save only, `Kweebec_Sapling.json` uses the stable variant fields with an isolated parent `HyThiefContainerFixture_Native_Sapling`. That parent file is a byte-for-byte copy of the current installed stable Sapling template; its hash is recorded in the save marker. This avoids the globally overridden template ID. The verifier checks the parent range/sector against the witness constants. Global HyThief role assets and all other worlds remain unchanged.

The staged Sapling retains its model, position, Frozen component and +Z body/head direction. It does not execute free-running witness AI; HyThief queries its native visual geometry at removal time. The Razorleaf remains unfrozen, shielded, with its existing test-only nullable idle-motion setting and inherited combat instructions.

## Shared native guard adapter

`HyThiefGuardAlert.signal` retains its original merchant-only pickpocket gate and now delegates to `deliver`. Container witnessing calls `deliver` directly with the confirmed witness and offender references, without calling `handleSingleSteal`.

The downstream selection is unchanged: nearest supported ready Razorleaf/Patrol within 20 blocks of the source, alive, in Idle/Alerted, with the danger message enabled and unqueued. Exactly one recipient receives `BeaconBroadcast.post(guard, "Kweebec_All_Danger", offender, 1.0, store)`. No state forcing, target-slot puppeting, movement, attacks or combat polling were added. Native guard instructions consume the message and own combat. The warning is the native danger message; no new civilian speech/sound/custom warning UI was added.

Diagnostics include owned `Action=REMOVE Result=SUCCESS`, removal-time `WITNESS=UNSEEN` or `WITNESS=Kweebec_Sapling`, and `Action=DANGER_SENT`/`DANGER_NOT_SENT` with reason/guard identity. Unowned control requests log `CONTROL_UNOWNED_IGNORE` before native fallback. No per-tick logging.

## Validation and preservation

Clean build succeeds with Java 25 and the pinned stable server. Existing two inventory-removal deprecation warnings and Gradle deprecation notices remain; they were not changed.

Native integration verification passed:

- Native chest OpenWindow/CloseWindow preserve the same live inventory and ownership.
- Control position is visible but unowned; control transfers have no owned-removal report.
- Rear position is outside the Sapling's forward sector; front is visible.
- Out-of-range and opaque-wall cases are rejected using the native LOS helper.
- A source snapshot remains front/witnessed even after the modeled test target is moved rearward before probe finish.
- The shared adapter using the actual loaded HyThief plugin queues one native danger message with the exact offender reference.
- Native LOS from the shielded guard to the front test target is false; a walkable route around the south wall remains.
- Existing 14-case attribution matrix and actual control/rear/front native-container transfer sequence pass.

The visual/queue test uses an inert modeled entity and native APIs, not a connected player. It consumes the queued diagnostic message before the dummy target can drive combat. Consequently player pursuit/attack is explicitly left to Firebeard; no automated combat claim is made.

The container save/master is reset and checksum-verified. Shared reset is verified on disposable copies of all three worlds, including dirty-save backups. Older live HyThief/HyStash saves and masters, original arena/container prefabs, archived releases and unrelated mods remain hash-identical. Other JAR gameplay classes/config/assets remain byte-identical to container.2; only the container diagnostic classes, guard adapter and manifest changed.

Rollback: `DevTesting/Container_Theft_Test_World/Rollback-container.2-before-witness` preserves previous source, installed JAR, master, dirty active save, fixture pack, verifier and reset/checksum metadata. Normal reset launchers remain unchanged. Evidence: `Old-Chest-State-Metadata/native.log`, `container.3-clean-build.log`, `candidate-native-verification.txt`, `container.3-final-native-verification.txt`, `container.3-boundary-verification.json`, and `container.3-reset-verification.json`.

References: installed stable Assets.zip and pinned server bytecode; [official PositionCache API](https://docs.hytale.com/api/com/hypixel/hytale/server/npc/role/support/PositionCache), [native container interaction API](https://docs.hytale.com/api/com/hypixel/hytale/server/core/modules/interaction/interaction/config/server/OpenContainerInteraction), [official Update 6 hotfix notes](https://hytale.com/news/2026/8/hotfixes-update-6). The global 4-versus-native-15 configuration difference is documented above, not silently treated as a documentation mismatch.

Stop for Firebeard runtime results. No generalized ownership, crouch witness reduction, sounds, integrations or public release.

Final validation: `VerificationRuns/20261009-164506-c71a35f27c9542ac8eb8ca6174b2a0a4/native.log` passed after the live reset. Shared reset passed for disposable copies of all three final masters (27/42/30 files). Active container save and its 30-file master are checksum-identical. Installed JAR SHA256: `4D9251DD3D24257151DF2B596EC0A3C11B74862D323DD8A6E5F68D8D1351E388`.
