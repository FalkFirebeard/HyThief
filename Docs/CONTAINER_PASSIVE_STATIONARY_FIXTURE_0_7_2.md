# Stationary passive container witnesses — Hytale 0.7.2

## Scope and method

Fixture-only correction for the existing Container Theft Test World. Installed HyThief remains `1.3.1-container.12`, target `=0.7.2`, SHA-256 `6b4c1500428674b4deb09c116ca6eb7d37360f9137e9a39ac527068d79dbe800`. Hytale is 0.7.2 revision `d769dc3d57881261a62858e1394f0e4de9a21eca`; server SHA-256 `23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff`.

Only the five `Master/mods/ContainerPassiveClientFixture/Server/NPC/Roles/HyThief/Stealth/HyThiefStealth_{role}.json` assets change. Each prepends a native instruction with `Continue=true`, a passive-state sensor, and `Nothing` head/body motions. The original instruction tree follows byte-for-JSON-value unchanged: perception, actions, interaction, damage checks, and state transitions still execute. Native motion selection is first-match per head/body channel. Once the state becomes combat/chase/converge, the passive hold does not match and native pursuit/attack motions take over. No Frozen component, zero-speed override, teleport loop, forced state, or production plugin change is introduced.

Held main states (including their native substates): Goblin `Idle`, `IdleBlock`, `IdleBrawl`, `IdleChaseNPC`, `IdleEat`, `IdleWatchJuggle`; Trork/Outlander `Idle`, `NeutralPlayer`, `Revered`; Skeleton/Zombie `Idle`, `Sleep`. The existing Friendly settings and all motion-controller speeds remain unchanged. Return-home/combat/search/flee state logic is not rewritten. The hold stops wandering and watch rotation during passive idle; it is not a physical immovability flag.

## Exact saved poses

| Color / role | NPC X,Y,Z | Head/body rotation | Player pad X,Y,Z |
|---|---|---|---|
| Orange / Goblin_Scrapper | -36,64,55.5 | pitch/yaw/roll 0,0,0 | -36,64,47.5 |
| Purple / Trork_Warrior | 0,64,55.5 | 0,0,0 | 0,64,47.5 |
| Cyan / Outlander_Brute | 36,64,55.5 | 0,0,0 | 36,64,47.5 |
| White / Skeleton_Fighter | -18,64,95.5 | 0,0,0 | -18,64,87.5 |
| Black / Zombie | 18,64,95.5 | 0,0,0 | 18,64,87.5 |

All face -Z toward their own pad, eight blocks away. Chests, ownership, labels, floor geometry, spawn, 64 starting blocks, and all native save/chunk bytes are unchanged. Exactly nine total chests/NPCs remain: original four plus these five. No peers added.

## Native evidence

Disposable save clones only; test helper is not embedded in Master, Active, or the installed gameplay JAR.

- Geometry validation: all nine stations, five new Friendly/perception/combat components, native open/close/deposit/rearrangement, FACTION ownership, standing visibility, crouched eight-block rejection, temporary wall LOS rejection/recovery, and starting inventory pass. Original field fingerprint remains `dceeeb1c852f91ba1e1f89292b2538b13db5fba601e42bc24948547342f8ef97`.
- Sustained idle: 240 samples per NPC over 60 seconds with five nearby friendly native dummy targets. Maximum position drift **0.0 blocks**; maximum head/body yaw drift **0.0 radians**. Full final pitch/yaw/roll are also zero. Observed Goblin IdleEat and Trork/Outlander NeutralPlayer transitions remain functional.
- Purple Trork combat proof: another 60-second stationary window, including a friendly surrogate moved off-axis halfway through. No witness position/facing repair at theft. Native owned transfer attribution, visual witness, one caught notification, and personal handoff then run. Native target acquisition, movement over one block, and damage are required for RESPONSE_PASS. Exact measured result follows below.

These are native-server surrogate tests, not a claim of connected-client confirmation. Firebeard should check idle stability and one Purple theft in the real client.

## Preserved limitations and systems

Goblin `IdleEat.Default` remains excluded by the existing container.12 personal-alert gate. It still occurs during idle validation; this correction does not hide or fix it. No Goblin combat-state, weapon, or new gameplay fix is included. Container sight remains 20/6, normal perception 15/4, ownership/witness/notification/guard adapters unchanged. Original RED/BLUE/GREEN/YELLOW assets, native chunks, and geometry are byte-preserved.

## Reset and rollback

The actual desktop `Reset HyThief Container Test World` shortcut remains unchanged, targeting the existing `.cmd` and current 0.7.2 Master. Reset archives the dirty active save, then restores the exact Master (all files verified). Master and Active saved poses are additionally checked through native load validation. Rollback: `DevTesting/Container_Theft_Test_World/Stationary_Witness_0_7_2/Rollback/{Master,Active}`. Reset scripts, shortcut and unrelated worlds are untouched.

## Native references

Installed server `Instruction.execute`, `MotionContextSupport.setNextHeadMotionStep/setNextBodyMotionStep`, `HeadMotionNothing.computeSteering`, `BodyMotionNothing.computeSteering` were inspected directly using javap. They confirm first-selected motion channels, Continue traversal, and empty steering without disabling AI. Official background: https://hytale.com/news/2026/2/npc-technical-rundown ; current patch review: https://hytale.com/news/2026/10/2026-10-12-modder-facing-patch-notes-update-7/ . Current installed binaries/assets remain the authoritative implementation checked here.

## Combat result

```
OFF_AXIS_FRIENDLY_TARGET Role=Trork_Warrior OffsetX=2
STATIONARY_IDLE_PASS Seconds=60 SamplesPerNPC=240 MaxDistanceDrift=0.0 MaxYawDrift=0.0 States=[Goblin_Scrapper:Idle.Default, Goblin_Scrapper:IdleBlock.Default, Goblin_Scrapper:IdleEat.Default, Outlander_Brute:Idle.Default, Skeleton_Fighter:Idle.Default, Trork_Warrior:Idle.Default, Trork_Warrior:NeutralPlayer.Default, Zombie:Idle.Default, Zombie:Sleep.Nap]
PASSIVE_WINDOW_PASS Role=Trork_Warrior State=Idle.Default
FACTION_WITHDRAWAL_PASS Role=Trork_Warrior Notifications=1 PersonalDeliveries=1
NATIVE_RESPONSE_PASS Role=Trork_Warrior NativeMovementDistance=8.81380305727416 TargetOffender=true Damage=45.0 CustomBroadcast=false
RESPONSE_PASS
```
