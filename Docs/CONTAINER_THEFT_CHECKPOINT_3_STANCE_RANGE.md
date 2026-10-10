# Container Theft Checkpoint 3 — stance range, container.7

Container.6 real-client standing pipeline passed, including exact caught message and native guard reaction.

## Actual existing range

HyThiefStealthAcquisition.multiplier("sight", role) returns 4.0 / 15.0. The stable Sapling declaration used by this fixture is ViewRange=15, ViewSector=180 degrees. Result: standing 15 blocks, crouched 4 blocks. No values were tuned. ContainerVisualWitness previously recorded crouch but always used 15. It now reuses this same shared multiplier with removal-time MovementStatesComponent.crouching, retaining existing facing, native opacity/LOS, notification and danger adapter.

This borrows the range calculation, not the acquisition filter's idle/marked-target exceptions: a visual observation of a new theft is not a request to clear or mutate NPC combat awareness. Pursuit/combat memory is untouched. Full 3D distance remains the existing fixture metric; all marked pads are at the witness floor height.

## Layout

Same stationary Kweebec_Sapling at (0.5,64,10.5), facing +Z. Same native guard role, opaque wall and native LastSeen handoff. Four ordinary native Furniture_Kweebec_Chest_Large chests:

|Station|Chest coordinate|Marked approach position|Distance|Expected|
|---|---|---|---|---|
|CONTROL unowned|(-3,64,16)|(-2.5,64,15.5)|5.831|No caught notification/response|
|REAR owned|(0,64,4)|(0.5,64,5.5)|5|Unseen by facing while standing|
|MID owned|(0,64,20)|(0.5,64,19.5)|9|Standing caught; crouched unseen|
|CLOSE owned|(0,64,14)|(0.5,64,13.5)|3|Caught in both stances|

Use the labeled cobble floor pads on the Kweebec side of MID and CLOSE. CLOSE's far side can put the player beyond 4 blocks, so the marked pad matters. MID has a five-block margin outside the crouched limit and six-block margin inside standing range. CLOSE has a one-block margin inside crouched range. Positions remain comfortably inside the forward sector. Native standing LOS at MID and both stance LOS at CLOSE verified clear.

The old FRONT chest at (0,64,16) is removed from this save, replaced by MID and CLOSE. The unowned control stays where it was. Only the bounded development fixture's station count accepts two or three distinct owned stations; its world/marker/container validity rules remain unchanged. The original reusable prefab and other test worlds are preserved; this new stage is authored in the saved Container Theft master, with the existing prefab retained as its base reference.

## Validation

Clean container.7 build, stable 0.6.8, manifest =0.6.8. Existing inventory warnings unchanged. Native save reload: four chests, 18 labels, same stationary witness, same unfrozen guard, three automatic owned registrations, control unowned. Native chest state/open/close and attribution tests pass. Using real ECS movement stance: MID standing witnessed, MID crouched range rejection, CLOSE standing and crouched witnessed. Notification dispatch counts match stance results; existing guard native pursuit retains the offender. Pickpocket sources, stealth acquisition policy/multipliers, guard adapter and private native guard role bytes unchanged. No sound/random chance/ownership policy/UI changes.

## Firebeard test order

Use toggle crouch so opening a native chest does not accidentally stand you up. Observe one withdrawal per caught test.

1. Run A: standing CONTROL, standing REAR, crouched MID on its pad (all no notification/reaction), then crouched CLOSE on its pad (caught message and Razorleaf response).
2. Exit Hytale and use the existing Reset Container Theft launcher. Run B: standing MID (caught message and guard response).
3. Exit/reset again. Run C: standing CLOSE (caught message and guard response).

Each caught case starts with a fresh guard. Opening/browsing alone remains harmless. If results differ, quit to flush the live log and report station/stance. Diagnostics now show Distance, effective Range, StandingRange, Crouched, Sector, LOS, reason, notification and native danger delivery.

Rollback: DevTesting/Container_Theft_Test_World/Rollback-container.6-before-stance includes full old source, verification, master, station plan, reset/checksum files and previous JAR. The live dirty save is archived by the unchanged checked reset script. Real-client stance acceptance pending Firebeard.

API verified against installed stable binary and official MovementStatesComponent documentation: https://docs.hytale.com/api/com/hypixel/hytale/server/core/entity/movement/MovementStatesComponent
