# Stationary secondary same-family alert fixture — 0.7.2

## Baseline reverified before expansion

Installed Hytale: **0.7.2**, revision `d769dc3d57881261a62858e1394f0e4de9a21eca`. Server binary SHA-256 `23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff` matches the build dependency. Exactly one active HyThief JAR: `HyThief-1.3.1-container.12.jar`, target `=0.7.2`, SHA-256 `6b4c1500428674b4deb09c116ca6eb7d37360f9137e9a39ac527068d79dbe800`. No gameplay/JAR change or publication.

Master matched the previous `fbad943` checkpoint. The client-used Active save was preserved before the actual desktop reset restored the clean 41-file baseline. A direct probe of the dirty save lacked an expected NPC reference and is not accepted as a baseline test. The reset baseline held all five exact primary positions/facing for 60 seconds. Trork, Outlander, Skeleton and Zombie subsequently entered native combat, moved and dealt damage in disposable clones. The RED control, Kweebec FACTION, Feran FACTION, and LOCAL/both responders passed the existing native suite. Container ranges 20/6 and normal perception 15/4 remain unchanged.

One initial Trork combined combat/movement/damage assertion failed after a delivered alert. Its combined error does not identify the failing operand. A diagnostic rerun with unchanged gameplay and fixture recorded native `Chase.Attack`, 11.20958-block movement and 35 damage. That initial failure is retained in baseline evidence; no persistent baseline regression was established. Existing native weapon-variable warnings remain; this task does not repair them. Goblin `IdleEat.Default` remains outside the personal adapter's accepted state gate and was reproduced unchanged.

## Final geometry

The same existing world now contains **14 NPCs and 9 chests**: original four NPCs/chests, five unchanged passive primary stations, five new peers. No new chest, pad, path, world, shortcut or permanent wall in the original four-color area.

| Color | Primary role / exact position | Secondary role / exact position | Wall blocks (inclusive) |
|---|---|---|---|
| Orange | Goblin_Scrapper / (-36,64,55.5) | Goblin_Scrapper / (-30,64,55.5) | X -35..-32, Y 64..65, Z 49 |
| Purple | Trork_Warrior / (0,64,55.5) | Trork_Sentry / (6,64,55.5) | X 1..4, Y 64..65, Z 49 |
| Cyan | Outlander_Brute / (36,64,55.5) | Outlander_Brute / (42,64,55.5) | X 37..40, Y 64..65, Z 49 |
| White | Skeleton_Fighter / (-18,64,95.5) | Skeleton_Fighter / (-12,64,95.5) | X -17..-14, Y 64..65, Z 89 |
| Black | Zombie / (18,64,95.5) | Zombie / (24,64,95.5) | X 19..22, Y 64..65, Z 89 |

All initial head/body rotations are `(0,0,0)`; yaw zero faces -Z toward the existing theft pads. Every wall is **4 wide x 2 high x 1 thick**, matching floor cobblestone. Equal relative geometry in all five stations:

- Primary to secondary: **6.0 blocks**.
- Secondary to chest block origin: **9.604686356149273 blocks**.
- Secondary to player interaction pad: **10.0 blocks**; primary to pad: **8.0 blocks**.
- Both inside standing witness range/FOV at the pad; primary native LOS true, secondary LOS false (`occluded`). Same height/facing; no range-based explanation for hidden-peer exclusion.
- Nearest NPC in another passive station from each pad: Orange 36.878178; Purple/Cyan/Black 31.048349; White 34.176015 blocks. All exceed the 20-block theft witness range. Native Trork/Outlander broadcasts send within 30 to their own NPCGroup; different groups are filtered. Native response tests also assert no other station acquires the offender.

## Stationary implementation and peer choice

All existing primary role assets, positions, head/body rotations, saved UUIDs and starting states are preserved. Four peers use the exact same role as their primary and inherit the already-proven world-local Friendly settings and idle-only head/body `Nothing` holds with `Continue=true`. Detection, actions, native messages and state changes still execute. No Frozen NPC, zero speed, polling correction, forced attack, custom group broadcast or new gameplay adapter is added.

The Purple secondary uses **Trork_Sentry**, with one additional world-local private-parent asset copied from the installed container.12 JAR. Only `DefaultPlayerAttitude=Friendly` and the same idle hold are added. Native hybrid perception, combat, navigation and `TrorkEnemySighted` listener remain unchanged. Once alerted, its Converge/Shoot states naturally leave the hold. The Sentry may fight at range and return home; it is not forced to mimic Warrior melee pursuit.

Rejected disposable peer choices were not installed:

- A second Trork_Warrior triggered native sparring and rotated a primary during idle. Native `Server/NPC/Groups/Intelligent/Aggressive/Trork/Trork_Spar.json` lists only `Trork_Warrior`; `Component_Trork_Instruction_Spar` sends partner requests within 18 blocks. Existing primary holds correctly do not override that separate native activity.
- Trork_Chieftain avoids sparring but caused the primary to enter `Revered`, which the unchanged personal theft adapter skips. No state-gate repair was made.
- Sentry is outside the Warrior spar group; native `Template_Trork_Hybrid`'s `Spar` branch simply sets `Idle`, without invoking the spar component. This avoids both confounders without changing primaries or combat rules.

## Native validation results

A dedicated 60-second ticking run sampled **all ten NPCs 240 times each**, with friendly native surrogate targets at all five pads. Maximum position drift **0.0**, maximum head/body yaw drift **0.0**. Saved reset poses also passed native load checks. Each separate response clone checks a four-second calm control window, safe native chest open/close/deposit/rearrangement, then a real paired item withdrawal and current witness/caught/personal-handoff path. Only the primary receives a direct HyThief delivery; hidden peers are included in candidate evaluation and rejected by actual LOS.

| Family | Primary | Hidden secondary | Timing and movement |
|---|---|---|---|
| Trork | Native chase/attack | Acquired offender while still occluded; Converge -> Shoot.Ranged -> ReturnHome/Idle | Primary 0.25 s; peer 0.75 s; peer moved 6.394626 blocks and reached clear LOS by native pathing |
| Outlander | Native chase/attack | Acquired offender while still occluded; Converge -> Chase.Attack | Primary 0.25 s; peer 0.25 s; peer moved 11.153832 blocks and routed past wall |
| Goblin | Known IdleEat gate skipped delivery | Remained passive, zero movement | One caught notification; no direct handoff; no social-combat conclusion can be drawn from this skipped primary |
| Skeleton | Native chase/attack | Remained passive, zero movement | Primary 0.25 s; no offender acquisition by peer in 30 s |
| Zombie | Native chase/attack | Remained passive, zero movement | Primary 0.25 s; no offender acquisition by peer in 30 s |

The Sentry diagnostic initially demanded crossing the wall plane, which is inappropriate for ranged fighting. Its corrected proof requires movement plus regained native LOS from the blocked start; it does not force movement/state or alter geometry. Both original and corrected diagnostic outputs remain available locally. Native movement/combat state release is proven; damage recorded per station is combined and is not attributed to individual responders.

These results use native-server surrogate targets, not a connected Firebeard client. They measure this exact composition, not universal family propagation. No flock was created for Skeleton or Zombie. No new behavior is forced to make a family join.

## Master/reset preservation

Final Master has **42 files**, fixture version 1.2.0/layout 11, Hytale 0.7.2 metadata. Existing native world configs, player data, 64 starting blocks, primary role JSONs, chest ownership/inventories, pads and paths remain unchanged. Native authoring changes only three serialized region files plus fixture metadata and the one new Sentry asset. Original RED/BLUE/GREEN/YELLOW semantic fingerprint remains `dceeeb1c852f91ba1e1f89292b2538b13db5fba601e42bc24948547342f8ef97`.

Actual desktop `Reset HyThief Container Test World` was launched with its existing target/working directory and unchanged scripts. It archived the dirty Active save and restored all 42 Master files exactly. A disposable native load of that actual reset Active save verified 14 NPCs, 9 chests, all wall/pose/LOS/ownership conditions and unchanged original area. The reset cannot reintroduce the rejected drafts; only the validated Master is referenced.

Rollback: `Secondary_Peer_Field_0_7_2/Rollback/Master` (pre-peer master), `Baseline_20261016/Rollback/Active` (latest client-used save), and the reset script's existing archived save. Other worlds and public release artifacts remain untouched. Checksum-to-staged-Git-blob verification protects reset-byte reproducibility.

## Firebeard test order

**Trork -> Outlander -> Goblin -> Skeleton -> Zombie**, resetting between caught tests. Stand on the existing pad; first open/deposit/rearrange (safe), then withdraw one item while visible to the primary. Watch the named `PEER` behind its short wall. Record whether it acquires you, leaves idle, routes around the wall, attacks, or stays still. Keep NPCs/walls in place and record Goblin's current state limitation; no commands/setup needed.

## Exact native results

```
FAMILY_RESULT Role=Goblin_Scrapper SecondaryRole=Goblin_Scrapper PrimaryReacted=false PrimaryMovement=0.0 SecondaryReacted=false SecondaryAcquiredOffender=false SecondaryMovement=0.0 SecondaryPathToClearLOS=false PrimaryReactionSeconds=-1.0 SecondaryReactionSeconds=-1.0 Damage=0.0 CustomGroupBroadcast=false
FAMILY_RESULT Role=Trork_Warrior SecondaryRole=Trork_Sentry PrimaryReacted=true PrimaryMovement=10.996621371122465 SecondaryReacted=true SecondaryAcquiredOffender=true SecondaryMovement=6.394625502334843 SecondaryPathToClearLOS=true PrimaryReactionSeconds=0.25 SecondaryReactionSeconds=0.75 Damage=70.0 CustomGroupBroadcast=false
FAMILY_RESULT Role=Outlander_Brute SecondaryRole=Outlander_Brute PrimaryReacted=true PrimaryMovement=10.818841210750474 SecondaryReacted=true SecondaryAcquiredOffender=true SecondaryMovement=11.153832000181007 SecondaryPassedWall=true PrimaryReactionSeconds=0.25 SecondaryReactionSeconds=0.25 Damage=110.0 CustomGroupBroadcast=false
FAMILY_RESULT Role=Skeleton_Fighter SecondaryRole=Skeleton_Fighter PrimaryReacted=true PrimaryMovement=6.0777636592017785 SecondaryReacted=false SecondaryAcquiredOffender=false SecondaryMovement=0.0 SecondaryPathToClearLOS=false PrimaryReactionSeconds=0.25 SecondaryReactionSeconds=-1.0 Damage=192.0 CustomGroupBroadcast=false
FAMILY_RESULT Role=Zombie SecondaryRole=Zombie PrimaryReacted=true PrimaryMovement=6.823672031978091 SecondaryReacted=false SecondaryAcquiredOffender=false SecondaryMovement=0.0 SecondaryPathToClearLOS=false PrimaryReactionSeconds=0.25 SecondaryReactionSeconds=-1.0 Damage=131.0 CustomGroupBroadcast=false
```
