# Container Theft Checkpoint 2 — container.4 live evidence and container.5 correction

Real-client checkpoint remains pending Firebeard's container.5 retest.

## Actual failed client run

`C:/Users/askin/AppData/Roaming/Hytale/UserData/Saves/HyThief Container Theft Test World/logs/2026-10-09_20-11-45_server.log`.

FRONT requests 4–6 at 02:12:34–35 UTC identify the actual failure after delivery. The ingress correction from container.4 worked.

| Boundary | Actual FRONT request 4 evidence |
| --- | --- |
| Transfer ingress | SmartMoveItemStack; fixture enabled; native window/source section 4 |
| Ownership | World `5b59f6f9-a7f4-4ea4-8e52-5511ea9d10f8`, chest `(0,64,16)`, source `SimpleItemContainer@692a59cc` matches registered FRONT |
| Offender | FalkFirebeard, UUID `d49a059a-84ec-4b9c-8560-ef2ff633ce44`, Store `1478134137`, Ref index 4 |
| Successful removal | Slot 2, Ingredient_Fibre, amount 32, paired native removal/player receipt, timestamp `2026-10-10T02:12:34.878098800Z` |
| Witness | Initialized controlled Kweebec_Sapling, same Store, Ref index 3 |
| Witness position | `(0.5,64,10.5)` |
| Witness head | yaw `3.1415927` radians, facing +Z, view sector 180 degrees |
| Player position | Log-rounded `(-0.1641,64,15.30)`; crouched=false |
| Distance/range | `4.844817731500303`, range 15.0, passes |
| Facing/FOV | Sector=true |
| LOS | LOS=true (native raycast executed) |
| Visual result | WITNESS=Kweebec_Sapling, Reason=seen |
| Danger adapter | Called with offender Ref index 4 and witness Ref index 3 |
| Guard selection | Kweebec_Razorleaf, Ref index 1, position approximately `(8.999,64,7.462)` |
| Readiness | State Idle.Default, slot 0, Enabled=true, Queued=false, Ready=true |
| Delivery | Kweebec_All_Danger, Queued=true, ExactOffender=true, DANGER_SENT |
| Subsequent state | At 250ms and 1250ms: Search.Default, DangerQueued=false, OffenderMarked=false |

Requests 5/6 successfully removed iron 8 and copper 16 from FRONT; same witness position, standing player, distance `4.844817737627861`, sector=true, LOS=true, seen. They found the guard already in Search.Default, therefore Ready=false and DANGER_NOT_SENT/no-ready-native-guard. This is the consequence of request 4 losing its target, not a new ownership or witness failure. Do not expand guard readiness to Search to paper over it.

REAR requests 1–3 correctly returned UNSEEN/sector at distance about 4.62368. CONTROL had native passthrough. No chest interaction correction is needed.

## Last successful stage and first failing boundary

The entire live chain through successful offender danger delivery worked. The first confirmed failed boundary is retaining that offender for the native guard's chase/combat entry: the role entered Search with no offender target immediately after receiving danger.

The actual client log does not contain an instruction-by-instruction native sensor trace. To pinpoint the mechanism rather than guess, an isolated disposable native replay was allowed to run the guard AI after posting the same signal. Native TraceSuccess/TraceFail/BeaconMessages showed:

1. SensorBeacon consumes Kweebec_All_Danger with the exact target.
2. Idle danger instruction succeeds and transitions to Attack.
3. Native chase's LastSeen read fails: the opaque shield wall prevents the guard's own LOS acquisition; the remote source is outside its own local hearing reach.
4. Native Nav At_Goal branch has no usable LastSeen position. Its five-block Target/AutoUnlockTarget check fails and clears LockedTarget.
5. The role transitions to Search; 250/1250ms reproduce the client's no-target/Search snapshots.

Replay evidence: `VerificationRuns/20261009-201804-c890553414c0494aa58d3114956f138b/native.log`, especially message receipt and instruction trace immediately afterward. This supplements the live evidence; it is not presented as an actual-client sensor trace.

Verified stable native assets: Template_Kweebec_Razorleaf, Component_Instruction_Intelligent_Chase, Component_Instruction_Target_Adjusted_Attack. Native SensorBeacon bytecode assigns LockedTarget but does not initialize LastSeen. ActionStorePosition copies the sensor's target position into the named native position slot. The original offline proof polled the beacon and removed its modeled actor before letting the AI execute, so it could not reveal this target-loss boundary.

## Small container.5 correction

Only the development save-local Razorleaf composition changes. Private parent `HyThiefContainerFixture_Razorleaf_WitnessHandoff` is copied from installed stable Template_Kweebec_Razorleaf. In its Idle/Alerted danger branches, the native Beacon sensor now runs:

`StorePosition(LastSeen)` → existing `State(Attack)`.

The old damage-event branch is preserved separately and unchanged. Remaining native attack, chase, target loss, search, hearing, vision, timers, leash, animations and combat actions are unchanged. This adds the position supplied by the already-confirmed visual crime witness to the guard's native chase context; it does not give the guard continuous knowledge of a hidden player's movement.

No Java gameplay change, forced target loops, attacks, movement/pathing, range widening, native sensor bypass, sound, random catch roll, crouch change, UI or chest modification. The shared Java delivery adapter is unchanged. Global production guard roles and other saves are unchanged. Fixture pack becomes 1.0.3; diagnostic JAR 1.3.1-container.5, target =0.6.8. This remains the narrow container checkpoint; broader guard/crime composition is not rolled out.

Official API lookup for ActionStorePosition was unavailable; installed stable asset declarations and server bytecode provide the verified implementation. No disagreement with documentation was observed. Installed/pinned stable server hash remains `DCD2956CC65B950084650EADD56B889CF51F7610C127C3DAAC1F74DC471FEC97`.

## Verification and preservation

- Clean Java25/Gradle9.1.0 build succeeded; two existing inventory-removal warnings remain.
- Native validation now lets the modeled offender persist and the guard AI consume the danger signal. At both 250/1250ms: Attack.Default, queue consumed, exact offender retained. At 1250ms, guard moved `3.7066492122163797` blocks using native chase. `NATIVE_GUARD_PURSUIT_PASS`.
- Native result: `VerificationRuns/20261009-202108-633f17b282be4db49f78102f8da085e1/native.log`.
- Original witness/range/FOV/LOS, CONTROL/REAR, attribution matrix and native chest state/ownership validation remain passing. All chest/native interaction assets are unchanged.
- Runtime native attack against Firebeard is still pending; modeled-actor pursuit is evidence of restored native chase context, not a claim of live-client acceptance.
- Full container.4 source/JAR/master/pack/verifier and failed client save preserved in `Rollback-container.4-before-lastseen`. Dirty active world is additionally archived by the existing reset script. Only the Container Theft master receives this versioned fixture role fix; old masters remain in rollback. Other worlds, original prefabs, archived releases and unrelated mods remain protected.

## Firebeard retest

Launch the reset Container Theft Test World; remain standing. No setup commands or new UI.

1. CONTROL: one-click native chest, transfer an item into inventory. Expect no reaction.
2. REAR: transfer an item into inventory. Expect no reaction.
3. FRONT last: stand visibly in front of the Kweebec on its side of the FRONT chest, transfer an item into inventory, then close inventory to watch. Expect Razorleaf to turn/pursue around the south end of the wall and attack using its native role.

If it still fails, quit to flush logs and report whether the guard moved, turned, attacked or remained idle. Existing request-tagged INGRESS → REMOVE → WITNESS → DANGER → NATIVE_GUARD_OBSERVATION records distinguish any remaining failure. Do not call this checkpoint complete until Firebeard confirms the visible FRONT response.
