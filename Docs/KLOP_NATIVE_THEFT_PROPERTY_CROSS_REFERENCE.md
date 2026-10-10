# Klop native theft/property cross-reference — Hytale 0.6.8

Investigation date: 2026-10-09. Investigation only; no gameplay, configuration, JAR, deployment, or world changes.

## Finding

The installed 0.6.8 assets contain an explicit Klop chest-protection component, but **none of the eight shipped Klop roles reference it**, directly or through their inspected dependencies. No server JSON or prefab references its exact ID, and no copied server-JAR class contains that ID. Consequently, the component is evidence of an older/intended chest reaction, **not evidence that current native Klops actually execute it**. A real-client demonstration on a precisely identified role would be needed to reconcile Firebeard's recollection with this build.

If connected to a compatible role, the component reacts to **chest Use interaction or damage**, not successful inventory withdrawal. It is a nearby-block-event reaction without a visual witness test. It has no owner registry or faction ownership query. The equivalent Trork components remain connected to current Trork/Outlander roles.

Keep HyThief's existing successful-withdrawal → owner attribution → visual witness → caught notification → native danger handoff. There is no verified Klop ownership implementation here to substitute for it.

## Scope and evidence

Read-only installation: `C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest`.

- Native `Assets.zip` SHA256: `F554BE4AD5E32B4968CBDCC426325A346FCD5202C607DB33141AF0506A1E9279`.
- Inspected copied `HytaleServer-0.6.8.jar` SHA256: `DCD2956CC65B950084650EADD56B889CF51F7610C127C3DAAC1F74DC471FEC97`.
- Scanned Server JSON for Klop content and BlockChange consumers; inspected all 1,000 NPC-role JSON assets for reference connectivity.
- 534 Klop-named archive entries; 332 Server JSON files mention Klop content. These counts include presentation/worldgen references, not 534 different NPC definitions.
- Inspected all 115 Klop-name/content JSON prefabs, including 106,544 block entries and 37 entity markers. Content search found no additional Klop-containing prefabs outside the Klop cave subtree.
- Bytecode inspection used `javap -c -p` on the project copy, not modifications to the installed server.
- Local research copies/evidence: `DevTesting/Klop_Property_Reference_0_6_8` (ignored development material). The committed asset index lists exact native paths; native implementation files are not redistributed in Git.
- This is asset/bytecode verification, not a new gameplay test. Negative findings are limited to these Klop paths and the searched assets; they do not assert that Hytale has no ownership system anywhere.

Official references consulted: [NPC technical rundown](https://hytale.com/news/2026/2/npc-technical-rundown), [Update 6 patch notes](https://hytale.com/news/2026/8/update-6-patch-notes), and the official [Sensor API endpoint](https://docs.hytale.com/api/com/hypixel/hytale/server/npc/instructions/Sensor). The API endpoint failed to return usable content on the final check; local stable bytecode and assets resolve the specific implementation questions. The official material supplies architecture/change context, not a claim that current Klops have theft AI. No documentation-versus-installed-data contradiction was established.

## All Klop roles and faction identity

Paths below are inside native `Assets.zip`. Prefix `P` = `Server/NPC/Roles/Intelligent/Passive/`.

| Exact role ID | Exact asset path | Current implementation | Chest/property handling |
|---|---|---|---|
| `Klops_Gentleman` | `P/Klops_Gentleman.json` | Variant of `Template_Intelligent`; 74 HP, neutral players, no equipped weapons; generic intelligent behavior | No BlockChange component in role dependency graph |
| `Klops_Miner` | `P/Klops_Miner.json` | Variant of `Template_Intelligent`; 74 HP, neutral players, no equipped weapons; Patrol and FollowPatrolPath false | No BlockChange component |
| `Klops_Miner_Patrol` | `P/Klops_Miner_Patrol.json` | Miner variant; Patrol and FollowPatrolPath true | Inherits miner, no property sensor |
| `Klops_Merchant` | `P/Klops_Merchant.json` | Standalone Generic role, 74 HP; neutral player, NPC Ignore; barter interaction, watch states | No property/combat chest branch |
| `Klops_Merchant_Patrol` | `P/Klops_Merchant_Patrol.json` | Variant referencing `Klops_Merchant`, with no Modify block | Name alone does not introduce patrol or theft behavior |
| `Klops_Merchant_Wandering` | `P/Klops_Merchant_Wandering.json` | Standalone Generic role, 62 HP; timed wandering and barter/watch interaction | No property/combat chest branch |
| `Temple_Klops` | `P/Temple/Temple_Klops.json` | Variant of `Template_Temple`; merchant appearance, MotionStatic true | Temple social/greeting role; appearance does not make it the merchant barter role |
| `Temple_Klops_Merchant` | `P/Temple/Temple_Klops_Merchant.json` | Variant of `Klops_Merchant`, Invulnerable true | Merchant branch only; no property sensor |

No dedicated `Klops_Guard`, warrior, or combat-specialist role was found. Generic intelligent damage retaliation on Gentleman/Miner must not be mistaken for a dedicated guard implementation.

No Klop-specific attitude/faction group asset or Klop-specific group reference was found in the inspected Server JSON. Gentleman/Miner inherit `AttitudeGroup=Empty` from `Server/NPC/Roles/_Core/Templates/Template_Intelligent.json`, while overriding the player attitude to Neutral. Merchant roles declare direct Neutral/Ignore attitudes. Model/audio categories named Klops are presentation categories, not proof of a faction ownership identity.

Related exact assets:

- `Server/BarterShops/Klops_Merchant.json`: normal barter shop; one fixed construction-sign exchange, Stock 1, daily refresh, RestockHour 6. Stock is barter availability, not a world-chest owner.
- `Server/Drops/NPCs/Intelligent/Klops/Drop_Klops_Gentleman.json`, `Drop_Klops_Merchant.json`, `Drop_Klops_Miner.json`: NPC drop lists, not property rights.
- `Server/NPC/Spawn/Markers/Klops_Gentleman.json`, `Klops_Miner.json`, `Klops_Miner_Patrol.json`, and `Instance/Forgotten_Temple/Temple_Klops.json`.
- Models under `Server/Models/Intelligent/Klops/`; sounds under `Server/Audio/SoundEvents/SFX/NPC/Intelligent/Klops/`. Full paths are in the companion index.

## Exact chest-protection component and reachability

Asset: `Server/NPC/Roles/Intelligent/Passive/Component_Instruction_Klops_Idle.json`.

It is an Instruction Component with interface `Hytale.Instruction.Intelligent.Idle`, importing `Alerted`, `Search`, and `Chase`. Defaults: ViewRange 15, ViewSector 180, HearingRange 8, AlertedRange 30.

Its two chest branches are:

| Sensor | Parameters | Action |
|---|---|---|
| `BlockChange` | EventType `Damage`, BlockSet `Chest`, SearchType `PlayerOnly`, Range `ViewRange`, TargetSlot `LockedTarget` | `ParentState` → `Chase` |
| `BlockChange` | EventType `Interaction`, same block set/range/target | `ParentState` → `Chase` |

`Server/Item/Block/Sets/Chest.json` selects `Furniture*_Chest*`. This selects a block **type**, not an owner, location, prefab, merchant, or faction. A player-built matching chest could match too if this branch were active nearby.

The separate sight/sound branches at the end of the component are ordinary hostile detection. Their ViewSector and hearing settings do **not** make the earlier BlockChange branches visual.

Reachability checks:

1. No native Server JSON or prefab references `Component_Instruction_Klops_Idle`.
2. No copied server-JAR class contains that exact component ID.
3. Gentleman/Miner inherit current `Template_Intelligent`, whose Idle implementation is inline plus `WakingIdleBehaviorComponent=Component_Instruction_Waking_Idle`.
4. The waking component interface is `Hytale.Instruction.Idle`, different from the old component's interface. There is no verified automatic interface substitution selecting the Klop component.
5. Merchant and Temple roles follow their own non-property paths.

Conclusion: **dormant/unreferenced in this stable asset graph**. Do not reconnect it as a supposed compatibility fix without a separate design/runtime decision.

## What event actually means: opening versus removal

Verified native implementation classes:

- `com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.UseBlockInteraction`
- `com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction`
- `com.hypixel.hytale.server.npc.systems.BlackboardSystems$UseBlockEventSystem`
- `com.hypixel.hytale.server.npc.blackboard.Blackboard`
- `com.hypixel.hytale.server.npc.blackboard.view.event.block.BlockEventView`
- `com.hypixel.hytale.server.npc.corecomponents.world.SensorBlockChange`
- `com.hypixel.hytale.server.npc.corecomponents.world.SensorEvent`
- `com.hypixel.hytale.server.npc.components.messaging.EventSupport`

Native chain:

`UseBlockInteraction` server execution (uncancelled Pre) → block interaction root executed → `UseBlockEvent.Post` on actor → `BlackboardSystems.UseBlockEventSystem` → `Blackboard.onEntityUseBlock` → `BlockEventView.onEntityUseBlock` → block event notification → subscribed NPC `PlayerBlockEventSupport` → `SensorBlockChange` → event initiator assigned as `LockedTarget` → role action.

`BlockEventView` accepts only `InteractionType.Use` here and emits `BlockEventType.INTERACTION`. It checks ordinary NPC detectability of the actor. It does **not** wait for a container item transaction or verify that an item was taken. A normal Use that opens a matching chest is sufficient to generate this event, even if the player only browses. Post is a block-use boundary, not proof of successful inventory movement or every downstream UI result.

Damage uses `DamageBlockEvent` → `BlockEventType.DAMAGE`, ignoring cancelled damage events. Destruction has a distinct event type but is not explicitly selected by the old Klop chest component.

| Candidate trigger | Result in this mechanism |
|---|---|
| Open/use matching chest | Yes, when Use Post reaches an active subscribed role branch |
| Browse chest after opening | No item-specific check; the earlier opening can already cause reaction |
| Successful item removal | No withdrawal sensor/transaction requirement in this component |
| Insertion or internal rearrangement | Not independently distinguished; opening can already have reacted |
| Chest damage | Explicit trigger, provided event is not cancelled |
| Enter camp/protected zone | No trespass/volume trigger in inspected Klop path |
| Merchant stock exchange | Separate native barter interaction, not chest ownership detection |
| Any owned object | No owner test; only the configured block set |

For **shipped current Klops**, these are conditional semantics of an unconnected component, not a confirmed live reaction.

## Visibility, crouch, range, and offender attribution

`BlockEventView` places the event at the affected block center (integer block coordinates + 0.5 on each axis). `EventSupport` delivers in-range event messages, with a two-second event lifetime. `SensorBlockChange` then checks event-position distance against the NPC's current Transform position. Matching requires distance-squared **less than** Range-squared. The legacy Klop default would therefore be 15 blocks from the **chest center**, not necessarily 15 blocks from the player.

There is no FOV, facing, LOS raycast, crouch check, or attitude/faction relationship filter in the inspected chest-event branch. Walls and a rear approach do not inherently invalidate it. General detectability remains: Intangible and Spectating actors are excluded; Creative players require Allow NPC Detection. This is not ordinary stealth/crouch filtering.

The event carries the actor's `Ref<EntityStore>` as initiator. `PlayerOnly` selects player-event support; polling yields that initiator. `SensorEvent` validates it through `EntityPositionProvider` and writes it through current `ExecutionSupport.getMarkedEntitySupport().setMarkedEntity(slot, ref, lockRebind, accessor)`. The named slot is `LockedTarget`; `LockRebind` defaults false in `BuilderSensorEvent`.

The block event stores the **block event position** plus actor reference. It does not write a `LastSeen` offender position, a crime record, or a persistent wanted flag. Event lifetime is not combat-memory lifetime; those are separate role concerns.

## Hostility and propagation

The old Klop branch itself performs target assignment followed by `ParentState Chase` (state ID `Chase`; no special crime state). It contains **no Beacon, alarm, faction-wide alert, or explicit LastSeen action**. Attack behavior would require the consuming role's Chase/combat implementation; the component cannot prove that current Klops attack because current Klops do not consume it.

Current Gentleman/Miner instead inherit generic intelligent behavior:

- Damage check can enter Combat/Search.
- Normal hostile perception can enter Alerted.
- Idle can receive `Under_Attack` into `LockedTarget`, within InvestigateRange 40, entering `Investigate.Maybe`; the follow-up probability defaults to 70%.
- Generic combat can emit `Under_Attack` and flock messages. `CombatMessageTargetGroups` defaults to an empty list in these Klop variants; no Klop-specific recipient/guard group is configured.
- These are ordinary combat/help paths, not a verified chest/theft alert. Do not equate `Under_Attack` with HyThief's `Kweebec_All_Danger`.

Merchant roles use Idle/Watching/`$Interaction`: Player range 8 enters watch behavior, range 12 maintains watching; trade uses CanInteract/SetInteractable/HasInteracted, LockOnInteractionTarget, OpenBarterShop, and interaction-state cleanup. This explains normal merchant facing, not a theft reaction.

No evidence establishes native town-wide Klop crime memory, nearby Klop guard selection, or theft-specific alarm propagation.

## Prefabs, storage, camps, and ownership

All 115 matching prefabs are in `Server/Prefabs/Cave/Klops/`: Basalt 40, Dry 32, Oak 43. They form cave homes with Bathroom, Bedroom, Cellar, Cave, Entrance, Main/Main_2/Main_3, Stairs, and Start sections. Exact paths and per-prefab marker counts are in `KLOP_NATIVE_PROPERTY_ASSET_INDEX.md`.

Across 106,544 inspected block entries:

- **Zero chest/storage-named blocks**; none match the native `Furniture*_Chest*` set.
- 68 container-named entries, all `Container_Bucket`; these are bucket block references, not evidence of a chest inventory or merchant-stock container.
- 37 NPC marker entities, all `SpawnMarker=Klops_Gentleman`.
- Entity components: Nameplate, HiddenFromAdventurePlayer, SpawnMarkerComponent, Transform, Model, UUID, HeadRotation; two also have Intangible.
- No owner/faction/protected-area/trigger property appeared in the inspected block records. The prefab top-level data is geometry/anchors/entities, not an ownership registry.

Worldgen references span Zone1_Tier1/2/3 and Zone2_Tier1/2/3 (33 Klop-named entries each), plus Zone3_Tier1/2/3 (24 each). Native roots: `Server/World/Default/Zones/<zone>/Cave/Klops/`; these compose the cave sections. Nearby worldgen faction-noise references do not establish chest ownership.

Spawn markers identify what NPC to spawn; spatial co-location is not a persisted property association. No role-linked chest ID, container registration, shop stock block, owner component, or protected trigger volume was found in this Klop content. The potential chest-protection mechanism lives on an NPC instruction and a block-type set; it does not know that a particular chest belongs to Klops.

## Other roles using the same event mechanism

Only five NPC assets directly contain BlockChange sensors:

- `Server/NPC/Roles/Intelligent/Passive/Component_Instruction_Klops_Idle.json` (unconnected).
- `Server/NPC/Roles/Intelligent/Aggressive/Trork/Components/Component_Trork_Instruction_Pre_Checks.json`.
- Same directory: `Component_Trork_Instruction_Greeting.json`.
- Same directory: `Component_Trork_Instruction_Return_Home.json`.
- `Server/NPC/Roles/_Core/Tests/Test_Block_Change.json` (test role, not a faction system).

The three Trork components gate chest interaction/damage by `ReactToChests` (default true), Range ViewRange, block set ChestBlockSet, PlayerOnly, LockedTarget → Chase. `Server/Item/Block/Sets/Trork_Chest.json` matches `Furniture_Crude_Chest_*`. Outlander role variants also configure `ChestBlockSet=Trork_Chest`; this demonstrates type-based protection rather than exclusive faction ownership.

Their reference graphs include these concrete roles (branch execution still depends on current native state):

- Trorks: `Trork_Brawler`, `Trork_Chieftain`, `Trork_Doctor_Witch`, `Trork_Guard`, `Trork_Hunter`, `Trork_Mauler`, `Trork_Sentry`, `Trork_Sentry_Patrol`, `Trork_Shaman`, `Trork_Unarmed`, `Trork_Warrior`, `Trork_Warrior_Patrol`.
- Outlanders: `Outlander_Berserker`, `Outlander_Brute`, `Outlander_Cultist`, `Outlander_Hunter`, `Outlander_Marauder`, `Outlander_Peon`, `Outlander_Priest`, `Outlander_Sorcerer`, `Outlander_Stalker`.

A verified melee example: `Template_Trork_Melee` → Idle component/pre-checks → chest target → Chase.Default → `Component_Trork_Instruction_Offensive_Default` → target attitude Hostile for 120 seconds → Chase.Attack. Attack-state entry sends `TrorkEnemySighted` at range 30, TargetGroups from the role, SendTargetSlot LockedTarget. Receiving roles can enter Converge. This is local recipient-group combat messaging, not global faction crime registration. Different ranged/chieftain branches must retain their own native state semantics.

## HyThief comparison and recommendation

| Concern | Native Klop chest component | Current HyThief container prototype |
|---|---|---|
| Trigger | Use/damage on matching chest type, if connected | Successful attributed owned withdrawal |
| Owner | No specific owner, block-type category only | Existing owned-container registry |
| Offender | Native event initiator Ref | Validated transfer/removal attribution |
| Witness | Nearby NPC event subscriber, no visual test | Controlled Kweebec visual witness |
| Range | Default 15, NPC-to-block center | Container experiment 20 standing / 6 crouched, NPC-to-player |
| Facing/LOS/crouch | Not checked by chest sensor | Native view-sector/LOS check and movement crouch state |
| Caught notification | None in component | Existing caught notification |
| Hostility | LockedTarget → Chase; dependent consuming role | Proven Kweebec danger → Razorleaf native combat handoff |
| Current native activation | Not connected to current Klop roles | Proven by Firebeard real-client tests |

HyThief evidence: `src-java/com/local/pickpocket/ContainerVisualWitness.java` retains RANGE 20 and CROUCHED_RANGE 6, 180-degree native view-sector test, and fresh native PositionCache LOS. Its fixture selection is deliberately limited to the staged Kweebec; this is not yet generalized faction witnessing. Normal NPC/pickpocket perception remains the separate 15/4 system.

Useful native infrastructure to retain/consider:

1. ECS actor references, block positions, and native event attribution for **use/damage diagnostics**. These do not replace successful withdrawal attribution.
2. Current ECS-backed marked targets, native role state, and Beacon messaging for faction-specific hostility handoff after HyThief confirms a crime.
3. Native block sets as optional type classification, never as proof of who owns a chest.
4. Per-role native recipient groups/state contracts when later extending beyond Kweebecs. Reuse the architecture, not a universal Chase or Kweebec signal for every faction.

Do not adopt the old component wholesale: it would classify harmless chest opening as theft, ignore visual stealth, protect arbitrary matching blocks, and is not connected to current Klop roles. Do not replace the ownership registry based on prefab proximity or barter Stock.

Smallest follow-up, only if approved: observe an **unmodified native** Klop role with a known chest in a separate future test and record exact role + Use/damage/removal boundary. No such world or build was created in this investigation. If that shows a reaction absent from the inspected graph, trace its actual event/role overrides before changing HyThief.

## Checkpoint

Documentation only. No compilation or deployment was needed. Current container test remains `1.3.1-container.8`; no balance, UI, inventory, perception, guard, registry, prefab, master, reset-script, or installed-JAR change was made. Protected-content hash comparison is recorded locally with the research evidence. Stop for Firebeard review.
