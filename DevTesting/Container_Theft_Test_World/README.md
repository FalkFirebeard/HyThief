Current test: 1.3.1-container.8. CONTAINER-ONLY 20 standing / 6 crouched; normal NPC perception unchanged.

# Container theft sight tuning pass 1 — container.8

Container.7 15/4 stance checkpoint passed Firebeard's real-client test. Container.8 is an experimental container-only balance build: standing visual witness range 20 blocks, crouched 6 blocks. No additional modifiers. The normal HyThief initial-acquisition multiplier and pickpocket behavior remain byte-identical (native Sapling ViewRange 15, normal sight multiplier 4/15 = crouched 4). ContainerVisualWitness uses separate constants 20 and 6. Removal-time ECS crouch, facing, native LOS, ownership attribution, native chest open/close, notification and guard adapter are unchanged.

## Compact playable stage

Same frozen Kweebec_Sapling at (0.5,64,10.5), facing +Z, on the existing platform. Each owned station is a native Furniture_Kweebec_Chest_Large with fixed Copper bars 16, Iron bars 8 and Fibre 32. The floor pads are ordinary cobble blocks with floating distance/expectation labels; stand on the Kweebec-facing side of each chest. No commands/setup.

|Station|Chest position|Player pad center|Distance|Standing|Crouched|
|---|---|---|---|---|---|
|CROUCH INSIDE|(0,64,16)|(0.5,64,15.5)|5|Caught|Caught|
|CROUCH OUTSIDE / STANDING INSIDE|(0,64,19)|(0.5,64,18.5)|8|Caught|Unseen|
|STANDING NEAR EDGE|(0,64,29)|(0.5,64,28.5)|18|Caught|Unseen|
|STANDING OUTSIDE|(0,64,33)|(0.5,64,32.5)|22|Unseen|Unseen|
|CONTROL unowned|(-3,64,16)|(-2.5,64,15.5)|5.831|Safe|Safe|
|REAR owned|(0,64,4)|(0.5,64,5.5)|5|Unseen by facing|Unseen by facing|

All four front pads are forward and have independently verified clear native LOS, including the 22-block pad where the witness check normally short-circuits on range. Pads avoid thresholds by 1, 2, 2 and 2 blocks respectively. Six chests, five automatic owned stations, one unowned control; only the bounded development fixture station count changed. Existing ownership validity rules remain. Prior MID/CLOSE chests/pads are replaced in this saved layout; original reusable prefab is preserved as its base reference.

Same Razorleaf and unchanged native role assets/LastSeen handoff. Its stage placement moves from (8.999,64,7.462) to (8.999,64,16.5) within the existing shielded alcove. This keeps caught stations within native pursuit entry distance, without tuning guard AI/perception. Guard-to-player LOS is blocked at every pad by the unchanged wall. Witness-to-player LOS remains clear. Native pathing chooses its own route around that wall.

## Validation

Clean build on Java 25 / stable Hytale 0.6.8, manifest =0.6.8. Existing inventory/Gradle deprecation warnings unchanged. Native authoring then playable save reload: six native chests, fixed loot, five ownership registrations, 22 label entities, same frozen witness/unfrozen guard. Eight stance cases at 5/8/18/22 all pass. For each, actual ECS movement stance, exact distance, FOV, independent LOS and notification count are checked. 5 crouched caught; 8 crouched unseen; 8 standing caught; 18 standing caught; 22 both unseen. Rear facing rejection and control exclusion pass. Paired-withdrawal, insertion, rearrangement/failure and native chest state tests pass. Native danger delivered from 18-block standing pad; guard retains exact offender in Attack.Default and moves with native chase. Real-client feel/attack confirmation remains Firebeard's checkpoint.

Normal acquisition source and all other Java gameplay/UI/guard code and all packaged NPC/loot/config assets are byte-identical to container.7. Role pack, original prefabs, other test worlds and archived releases are preserved. The unchanged safe reset system archives the dirty live save and restores the checksum-verified new master. Native-generated .bak resource copies are excluded from the candidate, while old master is fully backed up.

## Firebeard test order

Use toggle crouch; each pad is measured to the player's floor position, not the chest center. Check the distance feel while approaching/standing on each pad. Opening or browsing alone must never notify. Use one successful withdrawal per caught test.

Run A, before any caught event: standing CONTROL; standing and crouched REAR; 22 standing and crouched; 18 crouched; 8 crouched. All safe. Then 5 crouched: exactly one `You have been caught stealing!` notification and native Razorleaf response.

Exit Hytale and use the existing Reset Container Theft Test World desktop launcher before each remaining caught test: run B = 8 standing; run C = 18 standing; optional run D = 5 standing. Each caught withdrawal should notify and produce guard response. These separate runs preserve a fresh guard rather than weakening aggression between tests. No commands or manual chest setup.

Report whether 20/6 feels believable, and any difference from the expected table. If failure occurs, quit to flush logs and report station + actual stance. Logs include actual Distance, effective Range, StandingRange, Crouched, Sector, LOS/Reason, caught message and danger/guard observations.

Rollback: DevTesting/Container_Theft_Test_World/Rollback-container.7-before-sight-tuning, including source, old master, role pack/prefabs, verification files, reset/checksum/station metadata and installed container.7 JAR. Runtime balance acceptance pending Firebeard; no public release promotion.

Stance API verified against installed stable server and official API: https://docs.hytale.com/api/com/hypixel/hytale/server/core/entity/movement/MovementStatesComponent
