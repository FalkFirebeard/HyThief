# Native NPC aggression and social-alert comparison — Hytale 0.7.2

2026-10-15 (America/Denver). Investigation and isolated diagnostics only. Development remains container.11, target `=0.7.2`. No HyThief gameplay/source/assets, installed mods, public release, active/master test world, reset system, or other project was changed.

## Decision

Reuse native relationship, beacon, instruction and combat systems. There is no single faction-alarm key or identical alarm-entry contract across these families. A message slot existing does not establish that posting to one witness will make it rally others.

- **Goblin: NEEDS SMALL ADAPTATION.** Friendly suppresses ordinary acquisition; native group combat works, but posting `Goblin_Attack` to one Scrapper does not make it rebroadcast. An eventual witnessed-theft entry must enter the existing rally-producing path, rather than treating an incoming rally as an outgoing offense.
- **Trork: READY for the tested Warrior/melee path; NEEDS SMALL ADAPTATION at whole-family level.** The melee/hybrid/mage/chieftain trees have `TrorkEnemySighted`; Hunter/Guard use the ranger/companion stack and cannot be assumed to share that contract.
- **Outlander: READY for the tested Brute path.** All nine inspected ordinary Outlander definitions reference Trork melee/hybrid/mage/chieftain templates and supply `TargetGroups=["Outlander"]`. These templates declare the shared alarm. Brute runtime passed; other classes have static coverage, not new runtime certification.
- **Skeleton: NEEDS SMALL ADAPTATION.** Native investigation and native flock combat both exist, but same-family proximity is not flock membership. `Under_Attack` sends the broadcaster, not the offender. A verified one-time flock fixture showed one member join combat and another enter investigation first, missing the short-lived flock alarm. Full neutral-village group combat is not yet proven.
- **Zombie: PROBLEMATIC for the requested stock same-family group response.** Friendly conversion works, and an individually provoked Zombie fights. Ordinary nearby Zombies did not join. The shipped Zombie uses a predator template with an empty `FlockArray` and no faction-wide combat broadcast. This is not a reason to exclude Zombies permanently; a separately approved native flock/configuration proof is the next avenue.

## Evidence and limits

The installed server manifest is 0.7.2, revision `d769dc3d57881261a62858e1394f0e4de9a21eca`. The installed and project dependency JARs both hash to `23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff`.

Primary evidence: current installed `Assets.zip`, current server API/bytecode (`javap` against the matching project copy), current container.11 sources/JAR, nine valid disposable native runs, and the existing real-client Kweebec/Feran proofs. Native JSON/reference copies and full logs remain local in `DevTesting/Native_Social_Alert_0_7_2`; they are not redistributed as release assets.

Official context checked: [NPC technical rundown](https://hytale.com/news/2026/2/npc-technical-rundown), [WorldSupport API](https://docs.hytale.com/api/com/hypixel/hytale/server/npc/role/support/WorldSupport), [Update 7 modder notes](https://hytale.com/news/2026/10/modder-facing-patch-notes-update-7), [0.7.2 hotfix notes](https://hytale.com/news/2026/10/hotfixes-update-7). Some initially attempted API/asset URLs did not resolve; the successful API endpoint and installed binaries supplied the exact signatures. No native-data/documentation contradiction was established. The hotfix's old Goblin spawn-beacon repair must not be confused with NPC combat-message beacons.

**These are offline server proofs using `Test_Dummy_Stationary` as the offender surrogate, not a connected human player.** Per-entity native attitudes establish the controlled relationship to that surrogate; no synthetic damage, forced combat state, marked target, movement, or attack loop was injected. The actor is intentionally an NPC, so player-only chest/reputation branches are not newly client-tested here. In particular Trork greeting logic changed some Friendly surrogate attitudes to Ignore. The separate config run verifies actual `DefaultPlayerAttitude=Friendly` construction for all five representatives, not a human walking through every possible idle state. Remaining real-client/role-specific checks are stated below.

## Comparison matrix

Native values below are native role settings, not proposed HyThief range changes. "Perception retained" means sensor trees/ranges remain installed; Friendly targets being filtered out of hostile acquisition is intended, not blindness.

| Family | Default player relationship | Attack-on-sight trigger | Primary aggression control | Perception retained when passive? | Native faction/social alert? | Nearby members join? | Clean passive conversion? | Likely HyThief integration point | Notes |
|---|---|---|---|---|---|---|---|---|---|
| Kweebec | Neutral for Merchant/Sapling/Razorleaf | Hostile targets or damage/danger; civilians flee rather than universally attack | Attitude plus class-specific role trees | Yes | `Kweebec_All_Danger`, `Kweebec_All_Axe`; group `Kweebec` | Razorleaf attacks; Sapling flees/relays | Already non-aggressive normally | Preserve proven danger adapter; native civilian/guard composition | Standalone Merchant has no native danger/combat tree; HyThief supplies caught-to-guard handoff |
| Feran | Neutral | Scent accepts Neutral and Hostile; guardians progress through alert/threat/proximity behavior; damage/beacons can enter Attack | Attitude, guardian threat tree, target proximity | Yes | `Feran_All_Attack`, `Feran_Warriors_Attack`, `Feran_Warriors_Converge`; group `Feran` | Appropriate warriors respond; civilians have their own attack/flight logic | Friendly is the smallest candidate; Neutral alone is insufficient | Existing `Feran_Warriors_Attack` handoff; native Attack/Converge paths | Do not alter retained awareness, threat reset or pursuit |
| Goblin | Hostile (Scrapper verified even though template omits the field) | Generic detection; Idle often includes both Hostile and Neutral, then Alerted -> Combat.Message | Default/effective attitude plus Generic_Detection and role exports | Yes; config and native reacquisition passed | `Goblin_Attack`, target group `Goblin` | Yes after source's native rally; **not** automatically after a single incoming rally post | Friendly: yes; Neutral: not reliable | Small native theft-entry component -> existing Alerted/rally path | Scrapper accepts rally with native 80% weight; Thief flees and alerts instead of fighting |
| Trork | Hostile | Pre_Checks senses target then reputation switch; damage/crude-chest events are separate triggers | Attitude/reputation switch plus role state and sensors | Yes | `TrorkEnemySighted` for melee/hybrid/mage/chieftain; ranger companion messages differ | Warrior: yes, including after direct one-NPC alarm post | Friendly candidate validates; Neutral has native greeting behavior | `BeaconBroadcast.post(witness,"TrorkEnemySighted",offender,...)` for a compatible role | Hunter/Guard ranger exceptions; ordinary alert send radius 30 |
| Outlander | Hostile | Inherited Trork sensing/reputation/offense tree | Same native controls, Outlander parameters/groups | Yes | Same message name, **Outlander** recipient groups | Brute: yes after source acquisition; other classes statically traced | Friendly candidate validates on Brute | Same native template capability route, retaining Outlander group filtering | Shared message name does not make Trorks and Outlanders one faction |
| Skeleton | Hostile | Template_Intelligent Hostile-only Idle detection -> Alerted -> Combat.Message | Default/effective attitude; native combat and optional flock | Yes | `Under_Attack` family investigation plus `Flock_Attack` combat | Loose same-family members: investigate, no offender combat in this test; established flock: partial combat response | Friendly validates; Neutral avoids initial Hostile-only scan, but later scans admit Neutral | Native offender-bearing flock route, subject to group membership and receiver-priority proof | Fighter's AttitudeGroup is `Empty`; NPC group is still `Skeleton`; do not conflate the two |
| Zombie | Hostile | Template_Predator detection admits Hostile and Neutral -> Alerted -> Combat | Default/effective attitude, predator role | Yes | `Flock_Attack` machinery exists, but default `FlockArray=[]` disables ordinary idle reception; no Zombie faction alarm | No in stock loose group test | Friendly validates; Neutral insufficient | Separate native flock/configuration proof before a theft adapter | AttitudeGroup inherits `Predators`; NPC group `Zombie` is a different concept |
| Klops (context) | Miner/Gentleman override Neutral | Generic intelligent damage/hostile response | Native player attitude and generic role | Yes | Generic mechanisms only; no dedicated Klop faction response established | Do not assume | Already Neutral on Miner | No new theft adapter recommended in this review | Do not revive old chest-Use behavior as ownership/withdrawal detection |

## Exact inheritance and identities

Paths inside `Assets.zip`:

- `Server/NPC/Roles/Intelligent/Faction/Kweebec/`: Merchant is Generic; Sapling/Razorleaf reference their same-named `Templates/Template_Kweebec_*` parents. Their group/attitude assets live under `Server/NPC/Groups/Intelligent/Neutral/` and `Server/NPC/Attitude/Roles/Intelligent/Neutral/` (exact paths in local asset index).
- `.../Intelligent/Faction/Feran/`: Civilian -> `Template_Feran_Civilian`; Sharptooth/Longtooth -> `Template_Feran_Sharptooth`. All five supported Feran classes retain distinct role trees. Scent defaults 20 and alerted defaults 30 on the inspected Civilian/guardian templates. The guardian threat tree is not generic attack-on-sight.
- `.../Intelligent/Faction/Goblin/Goblin_Scrapper.json` -> `Templates/Template_Goblin_Scrapper.json` -> `Component_Instruction_Generic_Detection` / `Component_Sensor_Standard_Detection`. Native Scrapper: view 12, sector 160, hearing 8, absolute proximity 2, alerted 45. Group `Goblin` matches `*Goblin_*`, excluding `Voidtaken*`. Attitude maps Goblin/Dungeon_Goblin_Pack Friendly, Void Neutral, Spiders/Skeleton Hostile. Miner uses `Goblin_Brave`; Thief lacks a normal fighting response. New boss/turret/lab roles are not certified by a Scrapper test or newly enabled in HyThief.
- `.../Intelligent/Faction/Trork/Trork_Warrior.json` -> `Templates/Template_Trork_Melee.json` -> `Component_Trork_Instruction_Idle`, `Pre_Checks`, `Reputation_Switch`, `Converge`, chase/attack components. Native Warrior: view 15, sector 180, hearing 8, alerted 50. Native `Trork` group matches `*Trork*`.
- `.../Intelligent/Faction/Outlander/`: Berserker/Brute/Peon -> Trork Melee; Cultist/Hunter/Stalker -> Hybrid; Marauder -> Chieftain; Priest/Sorcerer -> Mage. **Nine** ordinary definitions were traced. Each sets `TargetGroups=["Outlander"]`; Brute also sets AttitudeGroup Outlander and sector 270/hearing 6. Native group matches `*Outlander*`. Outlander Hunter is Hybrid, unlike Trork Hunter's Ranger.
- `.../Undead/Skeleton/Skeleton/Skeleton_Fighter.json` -> `_Core/Templates/Template_Intelligent.json`, not `Template_Skeleton`. Fighter: view 15, sector 270, hearing 10, alerted 20; `CombatMessageTargetGroups=["Skeleton"]`; allowed flock roles Fighter/Archer; no actual flock is implied by those names. Frost Knight also uses Template_Intelligent. Do not assume every skeleton boss/turret/class has that template.
- `.../Undead/Zombie/Zombie.json` -> `_Core/Templates/Template_Predator.json`. Standard Zombie: view 12, hearing 8; parent sector 120, absolute 4, alerted 28; parent AttitudeGroup Predators, FlockArray empty. Aberrant/Big/Small/Burnt/Frost/Sand also reference Predator; their complete gameplay is not runtime-certified here.
- `.../Intelligent/Other/Klops/Klops_Miner.json` -> Template_Intelligent; `DefaultPlayerAttitude=Neutral`, weapons empty; no new Klop faction/group infrastructure inferred.

The Outlander count is nine, including Stalker. No policy or resolver entries were added.

## Smallest passive setting, verified without removing AI

The isolated `NativeFriendlyFixture` changed four native template settings only:

1. Goblin Scrapper template: add root `DefaultPlayerAttitude: "Friendly"` (default was Hostile).
2. Trork Melee template: root Hostile -> Friendly; this also covers Outlander Brute's inherited default.
3. Intelligent template: `Parameters.DefaultPlayerAttitude.Value` Hostile -> Friendly; keep root Compute binding (covers Skeleton Fighter).
4. Predator template: root Hostile -> Friendly (covers standard Zombie).

All five roles instantiated with `WorldSupport.getDefaultPlayerAttitude()==FRIENDLY`, a root instruction, and ECS CombatSupport. Native trees, sensor values, group settings and weapons were retained. These template overrides existed only in the disposable config-run pack, not in HyThief, native installation, or installed Mods.

For an individual creator-configured NPC/player pair, current ECS `WorldSupport.overrideAttitude(targetRef, Attitude.FRIENDLY, duration)` is the smaller transient diagnostic control and preserves role/group identity. It is not persistent configuration, must not be renewed as a production polling loop, and does not clear existing threat/targets or defeat independent offense sensors. A future theft handoff must replace/escalate only the offender's relationship when justified, never make all vanilla hostile NPCs passive globally.

## Runtime observations

Common acquisition probe: fresh disposable flat save; actor at (0,64,0); source at (0,64,8); same-role peers at (10,64,8), (18,64,8), distant control (52,64,8). Same starting geometry across families, noon, ticking enabled; normal native wandering thereafter. Friendly toward the surrogate before trigger; four-second sampled calm window; only source then receives a native Hostile relationship override. Watch state is not automatically combat. Transitions sampled temporarily every 250 ms and only changes logged. Native BeaconMessages debug flag was enabled for subsequent probes. No HyThief JAR loaded in these native-isolation runs.

| Valid probe | Observed source path | Nearby members | Distant control |
|---|---|---|---|
| Scrapper acquisition | Idle -> Alerted -> Combat.Message -> Combat.Chase/Default | Both entered Combat.Maybe, then native chase/attack; native chase changed their effective attitude Hostile | Idle/no offender target |
| Warrior acquisition | NeutralPlayer -> Chase.Attack | Both Converge -> Chase.Attack | No offender combat |
| Brute acquisition | NeutralPlayer -> Chase.Attack | Both Converge -> Chase.Attack; native logs show shared message | Idle/no offender target |
| Fighter acquisition, no flock | Alerted -> Combat.Message -> Combat.Chase/Default | Under_Attack delivered; one investigated then searched/returned Idle; neither fought offender | No offender combat |
| Zombie acquisition, no flock | Alerted -> Combat.Chase/Default | Neither acquired offender | No offender combat |
| Scrapper, direct one-NPC Goblin_Attack | Combat.Maybe -> Combat.Chase/Default | Both remained calm; **no native outgoing rally logged** | Calm |
| Warrior, direct one-NPC TrorkEnemySighted | Converge -> Chase.Attack | Both received offender-bearing message, Converged, entered chase/attack states and rebroadcast | No offender combat |
| Fighter, one-time native flock setup | Alerted -> Combat; native Flock_Attack carried offender | Member 2 joined combat; Member 1 handled Under_Attack first, investigated and missed flock combat | Outside flock, no offender combat |
| Friendly-default config | All five representatives constructed Friendly | Root instruction + CombatSupport present | Not a movement/client test |

Accepted Skeleton flock proof is `Run-Skeleton_Fighter-flock-once`: one `FlockPlugin.createFlock` and three successful `FlockMembershipSystems.canJoinFlock/join` calls during setup only. Earlier `Run-Skeleton_Fighter-flock` is **discarded evidence** because a diagnostic helper recreated membership each sample. It is not used for conclusions. No production code repair was involved.

The positive receiver results cannot be attributed to independent hostile acquisition: only the source was switched Hostile, and receivers stayed Friendly/Ignore until their native message/combat paths changed them. This is still a surrogate-target proof; it does not establish real-player reputation behavior or every receiver's unobstructed path/weapon damage in a client.

## Native transport contract and ranges

Current components: ECS WorldSupport (relationships), StateSupport (states), MarkedEntitySupport (LockedTarget/LastSeen), BeaconSupport (registered/enabled/queued messages), PositionCache and the native NPC spatial resource. `ExecutionSupport` is acquired/cleared by role execution; there is no need to poke removed Role fields or manually notify sensors after a normal queued beacon.

- `BeaconBroadcast.post(receiver,message,offender,1.0,accessor)` is **one-recipient delivery**, not a radius broadcast. It does not enforce ownership, faction, visual witnessing or make the witness a broadcaster. HyThief must validate the selected receiver and payload first.
- Native ActionBeacon selects nearby eligible NPC groups using PositionCache and sends via BeaconBroadcast.post. `TargetGroups` uses native NPCGroup membership; AttitudeGroup is not that membership. No LOS/FOV filter appears in the messaging transport itself. A subsequent Converge/chase/attack instruction can require offender LOS/range.
- `SendTargetSlot: "LockedTarget"` preserves the offender. Omitting it sends the broadcasting NPC itself. This explains `Under_Attack`'s source-position/investigation semantics.
- Native SensorBeacon validates its queued **payload entity** and compares receiver-to-payload distance against its Range. For an offender-bearing message, that is receiver-to-offender, not receiver-to-broadcaster. It then populates the configured target slot and normal role execution continues. Merely being inside the send radius is insufficient.
- Builder defaults verified from current bytecode: ActionBeacon Range 64, ExpirationTime 1 second; SensorBeacon Range 64 unless overridden.
- Scrapper rally send 45; Miner send uses AlertedRange (20 on native Miner); Lobber/Guardian default message range 30; Thief flee broadcasts use AlertedRange (30 on native Thief), frequency [4,8]. Generic_Detection's beacon receiver default is 45. These are role-specific, not one universal Goblin radius.
- Trork/Outlander melee/hybrid/mage/chieftain send radius 30, target groups supplied by role. Ranger Hunter/Guard use `CompanionAttack` / `CompanionCeaseAttack` / `CompanionDeath` with real flock/companion semantics. No generic TrorkEnemySighted listener was found in the inspected ranger path.
- Kweebec and Feran inspected alarms send 30 through native group selectors. Current HyThief FACTION nearest-guard adapter separately selects within 20; LOCAL explicitly selects eligible listeners within 30. These are different operations.
- Skeleton Under_Attack ActionBeacon uses default 64, receiver InvestigateRange default 40; investigation has native 70% weighting. Fighter Flock_Attack receiver range is hearing*2 = 20 in Idle; sleep branch differs. Actual flock membership is required for outgoing FlockBeacon.
- Zombie Flock_Attack receiver is guarded by non-empty FlockArray, range HearingRange (8 for standard Zombie); Combat.Maybe uses native 70% flock-alert weighting. A nearby role matching the Zombie NPC group does not automatically create a flock or enable this listener.

## Independent offense paths and role exceptions

Trork Pre_Checks has native BlockChange PlayerOnly branches for chest Damage -> Chase and Interaction -> Warn, range ViewRange. `Trork_Chest` block set currently matches `Furniture_Crude_Chest_*`; Outlander Brute retains that set. Warn can approach and execute a close melee action. Those branches do not call HyThief ownership/withdrawal/visual-witness evaluation. A Friendly default must not be advertised as disabling them.

For a later isolated theft fixture, use a native chest outside that block set (for example the existing Kweebec chest) to avoid confusing native chest-opening protection with HyThief withdrawal detection. Re-check the block set when building that fixture. No chest set, sensor, or native property behavior was changed now.

Goblin Thief normally flees and alerts; forcing it to fight would be a gameplay redesign. Goblin Scrapper/Miners can decline rally according to their native acceptance setting. Skeleton investigation similarly has weighting. Preserve these distinctions or explicitly review a deterministic all-members policy; do not silently tune native probability to make a test pass.

## Comparison with container.11 and smallest next integration

Existing code remains unchanged:

- `ContainerVisualWitness`: successful owned withdrawal's stance/range/FOV/LOS evidence remains the authority for whether theft happened in view. No NPC alarm, chest-open event, or native acquisition should replace it.
- `ContainerOwnershipPolicy.member`: already uses native NPCGroup/WorldSupport membership. `faction()` currently recognizes Kweebec/Feran only; this would require a small data-driven extension later, not an AttitudeGroup heuristic.
- `ContainerCaughtNotification`: keep exactly one notification per confirmed transaction.
- `HyThiefContainerAlert.deliver`: ownership selects FACTION vs LOCAL; FACTION currently explicitly supports Kweebec/Feran. LOCAL delivers to opted-in `HyThief_Local_Theft` listeners; it is not proof of vanilla family-wide response.
- `HyThiefGuardAlert.deliverNative`: current 20-block nearest ready Razorleaf or Sharptooth/Longtooth selection is intentionally narrower than a witness-self-rally architecture. Do not merely extend its guard-ID sets for these new families.

The current development JAR's inspected Intelligent/Trork_Melee/Predator native beacon declarations remain equivalent to the installed templates; Goblin's modern stealth root retains its outgoing Goblin_Attack declaration. HyThief's acquisition/interaction decoration does not itself provide missing faction alarm contracts. This investigation did not run a new integrated container theft test or change 20/6 container or 15/4 normal perception settings.

Recommended first implementation checkpoint, after review: **one passive Trork Warrior witness + two ordinary compatible Trorks**, using the existing successful-withdrawal/visual-witness gate and a one-recipient native `TrorkEnemySighted` handoff. The offline one-NPC-post test already proves native self-response and onward rally for this stack. Include an unseen withdrawal control and a different-faction bystander; verify no opening-only warning from the chosen chest type. No dedicated "guard Trork" class is needed.

For Goblins, prototype one shared native theft-entry instruction that receives the confirmed offender, applies the justified per-offender hostile relationship, and enters the existing Alerted -> Combat.Message path. Preserve native rally/acceptance/flee branches. A relationship override alone depends on native sensory reach, which can be shorter than HyThief's container witness range; a plain Goblin_Attack post avoids that reach problem but did not rally peers. Neither is a complete unconditional adapter as-is.

For Skeletons, prove offender delivery and receiver ordering with an established native flock before selecting an adapter. Do not treat Under_Attack as a crime-target alarm or change the entire social-combat tree. For Zombies, first prove a small creator-configured native flock/listener setup; no Zombie faction broadcaster exists to reuse unchanged in the stock role. No permanent exclusion or new custom faction bus is recommended.

Future routing should be a small role/template capability contract (native group, supported offender message, witness-entry behavior), not a Java switch per concrete role and not universal opt-in for all entities. Do not implement the full matrix/hub or any property/PREFAB framework from this report.

## Files, reproducibility and stop point

Own diagnostic sources: `DevTesting/Native_Social_Alert_0_7_2/SocialAlertProbe.java`, `Run-Probe.py`, `results.json` and `README.md`. The helper JAR is local disposable tooling, never deployed. No native JSON/bytecode/full world copies are committed or published. Full native output and research references are ignored local material.

The runner creates new isolated Run-* directories, aborts if one already exists, reads only a copy of the Container Master's world configuration to create a fresh flat save, and never resets or writes Master/ACTIVE. Its config branch temporarily packages the four minimal native Friendly overrides in that disposable run only. The original fixture, reset paths, installed public/development artifacts, and other projects remain unchanged.

Stop for Firebeard review. No multi-faction hub, permanent theft-family rollout, public release, PREFAB work or gameplay build/deployment is part of this checkpoint.

## Personal-witness follow-up

The later personal-witness-only requirement is implemented/proven in `PASSIVE_HUMANOID_WITNESS_0_7_2.md`. The earlier Zombie/Skeleton group-propagation limitations do not block individual native combat. No universal social broadcast is required or added. Five exact representatives are native-server SUPPORTED; connected-client confirmation is pending.
