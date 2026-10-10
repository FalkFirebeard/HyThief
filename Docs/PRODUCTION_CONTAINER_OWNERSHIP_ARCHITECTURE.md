# Production container ownership architecture — stable Hytale 0.6.8

Date: 2026-10-10. Investigation only. No production implementation, build, deployment, range change, or world modification.

## Decision proposed for review

Use a **serialized HyThief ownership component on the actual placed container**, independent of its model and loot. Keep a separate, data-driven NPC participation/faction profile. Keep witnessing event-driven and reuse the existing transaction attribution, visual check, caught notification and native reaction adapter.

Retain the proposed defaults: absent metadata = unowned; explicit Owned false = unowned; Owned true with omitted Scope = NEARBY. Missing/invalid required FACTION or PREFAB data must **not** silently widen to NEARBY; report invalid configuration and treat it as unresolved/unowned until corrected.

PREFAB requires additional integration. Hytale retains some entity prefab provenance, but it does **not** provide a verified universal persistent container↔NPC property-instance identity. Use a HyThief property-group UUID remapped at supported placement boundaries, with explicit creator assignment for other paths. Do not use prefab asset name, a process-local paste integer, NPC flock membership, or nearest-NPC distance as ownership identity.

Minimum first implementation after approval: prove one ordinary container's component persistence/state preservation; then expose a permission-controlled look-at-container authoring command/tool and NEARBY ownership with an explicit Kweebec witness profile. Leave automatic PREFAB/worldgen mapping for a subsequent checkpoint. Preserve 20/6 container witnessing and 15/4 normal perception throughout.

## Evidence and confidence

Native read-only installation: `C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest`.

Installed server JAR and inspected project copy both match SHA256 `DCD2956CC65B950084650EADD56B889CF51F7610C127C3DAAC1F74DC471FEC97`. The investigation targets installed stable 0.6.8, not newer pre-release APIs. Working tree was clean before investigation.

Local evidence includes `javap -c -p` output from `.build/dependencies/HytaleServer-0.6.8.jar`, archive scans, and protected-content hashes; stored under `DevTesting/Production_Ownership_Reference_0_6_8`. No reference files in the live installation were edited. No proof plugin or runtime server was needed for the architectural conclusions. API support is verified statically; **custom-component restart/state/prefab round trips have not yet been proven in a live world**.

Official references:

- [Block asset reference](https://docs.hytale.com/assets/item/block/blocks/): documents BlockEntity as a component Holder. This is an asset template, not a per-placement editor panel.
- [Stable Update 6 patch notes](https://hytale.com/news/2026/8/update-6-patch-notes): covers section-based block APIs, ECS support migration, prefab/trigger behavior and plugin persistent resources.
- [NPC technical rundown](https://hytale.com/news/2026/2/npc-technical-rundown): general data-driven NPC architecture context.
- [Update 7 pre-release notes](https://hytale.com/news/2026/9/pre-release-patch-notes-update-7): future compatibility watch only, including carried-container and plugin-resource save fixes. Do not build the stable ownership design against those changes.
- Official direct API endpoint for FromPrefabInstance returned no usable content; the installed public signatures and bytecode supplied the detailed evidence instead. No claimed official guarantee of arbitrary editor fields or universal prefab membership was found.

## 1. Persist ownership directly on the container

Relevant stable types:

- `com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock implements Component<ChunkStore>` — serialized native container contents/capacity/droplist, with transient open-window bookkeeping.
- `com.hypixel.hytale.server.core.modules.block.BlockModule.getBlockEntity(...)` — resolve the block instance through section/coordinates; already used by HyThief's diagnostic registry.
- `com.hypixel.hytale.server.core.modules.block.BlockEntity.ensureBlockEntity(...)`, `setBlockEntity(...)`, `takeBlockEntity(...)` — current holder/ref lifecycle helpers. Do not reintroduce removed BlockModule.ensureBlockEntity.
- `com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection` — block refs/holders, dirty block tracking, inherited Savable marking.
- `com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk` — serializable holders/legacy column representation.
- `com.hypixel.hytale.component.ComponentRegistryProxy.registerComponent(Class, String, BuilderCodec)` and `PluginBase.getChunkStoreRegistry()` — supported codec-backed plugin component registration.
- `RocksDbChunkStorageProvider$Saver` snapshots serializable block holders and writes them through the component registry. Registering a codec is necessary; a supplier-only/runtime component is not a persistence design.

Proposed HyThief-owned names below are **new design names, not existing native APIs**:

```java
// Design sketch only; no code implemented.
enum OwnershipScope { NEARBY, PREFAB, FACTION }
record OwnershipData(
    int schemaVersion,
    boolean owned,
    OwnershipScope scope,
    String factionId,
    UUID propertyGroupId,
    String localGroupKey // authoring token; never runtime identity by itself
) {}
// Wrapper implements Component<ChunkStore>, with BuilderCodec and clone().
// Optional counterpart implements Component<EntityStore> for non-block containers.
```

Do not store ItemContainer object identity, ECS Ref, asset-map integer indices, open-window ID, or absolute coordinates as the persistent ownership record. Resolve the live container on the world thread when handling an attributed withdrawal. Normalize filler/multiblock positions to the actual owning block entity.

An existing normal native/custom chest can receive an additional ownership component beside ItemContainerBlock; no special chest model or replacement interaction is required. Custom entity-backed inventories need a separate ownership-host adapter using EntityStore, not a ChunkStore component forced onto an entity. An arbitrary remote/virtual inventory is not automatically supported just because it has ItemContainer; its location/identity must be resolvable.

### Persistence and lifecycle caveat

Save/reload support is real, but not every block operation automatically preserves every custom component. `BlockOperations.setBlock` can clone the new block asset's holder and call `BlockEntity.setBlockEntity`, replacing the old holder. A creator-attached component may disappear unless the same logical-container transition preserves it.

Native `ItemContainerSystems$OnReplaced` and `$OnReplacedHolder` handle `BlockReplaceEvent` and preserve inventory between replacement holders. This is a current-native example of the required lifecycle integration, not permission to change chest behavior. A future HyThief ownership transfer handler should copy ownership **only for the same logical container/state transition**, preserve explicit incoming metadata, and handle holder and live-ref cases. Do not blindly inherit ownership into any new chest built at the same XYZ.

Administrative edits must use the proper store-thread component path and dirty/saving mechanisms; in-place mutation alone is not a verified save guarantee. Deletion, break/replacement, carried blocks, editor copy, explosion, unload/reload and multi-block containers need explicit tests. Breaking and placing a new chest should default to unowned unless an intentionally preserved instance is being moved. Carry semantics remain a design/test decision, not an implemented policy.

| Lifecycle | Evidence now | Required checkpoint |
|---|---|---|
| Codec save/load and server restart | Native serialized holder machinery exists | Custom component round-trip and real save/restart |
| Open/closed state changes | Native replacement lifecycle exists | Ownership survives repeated native open/close; item contents unchanged |
| Item transfers | Separate ownership component need not touch contents | Withdrawal/insertion/rearrangement regression and exactly-once attribution |
| Prefab save/paste | Components are encoded in block holders | Two-copy, rotation and reload tests |
| Worldgen structures | Generated holders/spawners support component data | Direct and nested spawner placement tests |
| Break/new placement at same position | Old holder may be removed/replaced | No ownership inheritance leak |
| Plugin absent then restored | UnknownComponents exists, but this path was not proven | No promise until tested; document plugin requirement |

## 2. Attached metadata versus external registry

| Property | Attached serialized component (recommended) | External world/position registry |
|---|---|---|
| Ownership lookup | Resolve live block/entity, read component | Hash lookup plus live-instance verification |
| Save ownership with container | Native save pipeline | Separate persistence/atomicity and lifecycle management |
| Prefab portability | Holder serialization supports component export | Registry is not automatically inside prefab |
| Break/rebuild at same position | Removed with old instance if lifecycle is correct | Risk of stale owner applying to new chest |
| Open/close holder replacement | Requires tested preservation handler | Position can survive, but logical-instance ambiguity remains |
| Offline bulk editing | Requires component-aware data/tooling | Easy external manifest edits |
| Custom inventories | Host-specific component adapter | Host-specific registry key still needed |
| Plugin unavailable | Needs absence/unknown-component tests | External data remains separate, gameplay unavailable |

The existing `src-java/com/local/pickpocket/OwnedContainerRegistry.java` is explicitly ephemeral diagnostic code: world UUID+position, live ItemContainer object identity, one manual marker plus small fixture sets. It is not a production persistent registry. Preserve its test role rather than silently treating it as a save format.

Use a registry only as an index/cache, explicit migration manifest, or unsupported-host fallback. If a persistent fallback is required, use world UUID plus a verified container-instance token; handle destruction/replacement and backups, and exclude runtime Refs. Do not keep two competing authoritative owner records.

## 3. Creator authoring without Java or coordinates

Current native JSON prefab format supports per-block components:

- `BsonPrefabBufferDeserializer.deserializeState` reads a block's lowercase `components` object and decodes a ChunkStore Holder.
- `SelectionPrefabSerializer` writes the holder through ChunkStore.REGISTRY.serialize under the same field.
- `PrefabBufferBlockEntry.state` carries it through prefab processing; PrefabUtil passes cloned holders into native block placement.
- BinaryPrefabBufferCodec is also part of the current prefab pipeline; copying old JSON alone is not a guarantee that every editor operation preserves custom fields.

Concrete native example: `Server/Prefabs/Goblin_Thief_Chest.prefab.json` contains a block with `components: { Components: { ItemContainerBlock: ... } }`. Ownership can be an additional registered component. Preserve the original holder/ItemContainerBlock fields when authoring; do not replace the contents with an ownership-only holder.

Conceptual fragment after registration (field names are proposed):

```json
"components": {
  "Components": {
    "HyThiefOwnership": {
      "SchemaVersion": 1,
      "Owned": true,
      "Scope": "FACTION",
      "FactionId": "firebeard:ancient_scholar"
    }
  }
}
```

Asset `BlockType.BlockEntity` can declare components, but changing the chest asset would apply defaults to every instance of that asset. That is not the desired creator workflow and must not become a requirement.

No verified native editor extension point exposes arbitrary HyThief block-component fields as selectable ownership controls. Generic data support and entity editor tools do not prove a generic placed-block property inspector. Recommend:

1. Support component-authored prefab/world data for advanced creators.
2. Provide a HyThief creator/admin command/tool that targets the looked-at container and presents Owned/Scope/Faction; validates it and marks the save dirty. No manually entered XYZ.
3. Reuse that tool to inspect ownership and assign NPC/property membership, then save/export the ordinary prefab using the native tools.
4. Do not hijack the native chest Use action or add a special chest UI. Creator tooling is a separate privileged operation.

Native PrefabSpawnerSettingsPage/BlockSpawnerSettingsPage show targeted configuration UI patterns. They are not arbitrary component editors. Creator commands/UI and asset configuration are proposed, not implemented or UI-tested here.

## 4. True prefab-instance identity: partial native support, not universal

Important verified distinctions:

| Native object | Meaning | Limitation |
|---|---|---|
| `FromPrefab` | Marker for pasted entities | No asset or instance value; ClearFromPrefabMarker removes it |
| `FromPrefabInstance` | EntityStore component with serialized integer PrefabInstanceId | Worldgen entity path attaches it; not a common ChunkStore container component |
| `PrefabPasteEvent.getPrefabId()` | ID for one runtime paste operation | PrefabUtil obtains it from static AtomicInteger starting at 0; not a durable global key |
| `PrefabPlaceEntityEvent` | Same paste ID plus EntityStore Holder | Can remap entity metadata, but has no per-block holder, asset path or universal structure record |
| `SpawnMarkerReference` | Persistent reference to the actual NPC spawn marker | Useful marker→NPC association; not a property association to containers |
| `SpawnLineage` | Spawned lineage string | Spawn/family semantics, not a verified container↔structure instance ID |
| Flock membership | Native AI group UUID, remapped during paste | Controls AI; do not repurpose it as property membership |

`GeneratedEntityChunk.generateEntityHolders` attaches FromWorldGen and FromPrefabInstance from generated wrapper entries; EntityModule registers FromPrefabInstance with CODEC, so the integer is serializable. However, ordinary PrefabUtil paste does not attach FromPrefabInstance in its inspected code. No FromPrefabInstance use in NPC spawn-marker spawning was found; entities spawned later from a marker must not be assumed to inherit the marker's provenance automatically. Containers are in ChunkStore; the component is EntityStore-specific.

Native trigger-volume worldgen handlers use FromPrefabInstance to remap named trigger groups; TriggerVolumeManager exposes ensureWorldGenGroup/upsertWorldGenGroup, prefab group remapping, and unique pasted-group names. This is useful evidence that **per-placement remapping** is the native pattern. Trigger groups are optional authored content, not automatically retained identity for every container and NPC. Region groups are a possible explicit authoring fallback; they should not silently convert moving NPC proximity into same-prefab origin membership.

**Answer:** some true worldgen instance provenance survives on entities, but the inspected current APIs do not satisfy the complete persistent container+NPC relationship for all generation, paste and spawn paths. Do not claim that an asset ID or integer alone solves PREFAB.

## 5. Recommended PREFAB design and fallback

Persist `PropertyGroupId` as a UUID on owned containers and on associated NPCs/spawn markers; include world UUID in comparisons/cache keys. Separate the authored local key (for example `shop`) from the generated live UUID.

For a controlled HyThief placement path, current `PrefabUtil.paste` overloads accept **both** `Consumer<Holder<ChunkStore>>` and `Consumer<Holder<EntityStore>>`. This is a supported interception point: one placement operation creates a fresh UUID per local group key, and both holder consumers stamp the same mapping before placement. Rotation remains the engine's job. A new copy gets a new mapping. Preserve the actual native container and NPC roles.

This can prove Shop_A ≠ Shop_B without Java work by the creator, but does not automatically cover native editor paste, arbitrary third-party worldgen, nested spawners, and every structure builder. PrefabPasteEvent alone cannot stamp arbitrary block holders. Avoid a global ThreadLocal/current-prefab variable: deferred placement, nested pastes and concurrent generation make it unsafe.

For unsupported placement paths, use explicit creator-assigned property grouping via selection/look-at tooling or an authored property anchor with explicit members. A property anchor/region system would require its own persistence, copy-remap and NPC-spawn propagation tests. Until a path is supported, display an unbound-PREFAB validation error; never fall back to all copies of an asset or silently Nearby.

NPC respawn must inherit the group's membership through its configured marker/reference; roaming must not erase original membership. A role change may change theft-awareness/faction, but not necessarily property-instance origin. Explicitly test two adjacent identical prefab copies, repeated paste after restart, rotations, chunk boundaries, nested prefabs, and marker respawn. A wider settlement should use FACTION or an intentionally shared explicit group, not accidental prefab asset-name equality.

## 6. Faction identity and custom factions

Hytale's native concepts are distinct:

- `WorldSupport` ECS component holds attitudeGroup and itemAttitudeGroup; `getAttitudeGroup()` is a runtime index into `AttitudeGroup.getAssetMap()`. Attitude groups model relationships, not guaranteed civil/property affiliation.
- `Server/NPC/Attitude/...` defines relationships. Examples: Kweebec Friendly Kweebec/Hostile Trork; Feran Hostile Scarak/Spiders/Scorpions/Snakes; Outlander variants differ by role. A class-specific attitude group need not equal one faction.
- `ReputationGroupComponent.getReputationGroupId()` is reputation grouping, not a universal ownership component or crime policy. Some NPCs have no reputation group.
- `NPCGroup` assets contain IncludeRoles/ExcludeRoles and IncludeGroups/ExcludeGroups. `WorldSupport.isGroupMember(..., accessor)` uses the entity's **current role index**. These groups are a better declarative selector than race inferred from appearance or role-prefix Java code.

Exact group examples:

- `Server/NPC/Groups/Intelligent/Neutral/Kweebec/Kweebec.json`
- `Server/NPC/Groups/Intelligent/Neutral/Feran/Feran.json`
- `Server/NPC/Groups/Intelligent/Aggressive/Trork/Trork.json`
- `Server/NPC/Groups/Intelligent/Aggressive/Outlander/Outlander.json`

Recommendation: HyThief faction IDs are explicit creator-facing identifiers with data-defined membership selectors, using native NPCGroup assets where appropriate. An optional explicit per-entity faction override wins over profile/native group defaults. Resolve against the current role; cache only with role/config generation invalidation. Persist names/IDs, not native integer indices.

Custom namespaced IDs such as `firebeard:ancient_scholar` are HyThief policy IDs; they do not need to be valid native attitude group names. Creators can provide new group/profile assets or JSON config without changing HyThief Java. Do not change native NPC attitudes/reputation merely to make ownership matching work. Multiple declared memberships can be supported as a set; a container FACTION selector still has one explicit faction ID unless later designed otherwise.

## 7. Theft-aware participation independent of pickpocket eligibility

Add declarative `WitnessProfile` data selected by exact Role IDs and/or native NPCGroup selectors. Do not inject unknown custom fields into native Role JSON and assume its strict builder will accept them. Own the extension schema in HyThief assets/config or a registered component.

```java
// Conceptual policy, not production classes.
record WitnessProfile(
    String id,
    boolean theftAware,
    Set<String> roleIds,
    Set<String> npcGroupIds,
    Set<String> propertyFactionIds,
    String reactionProfileId
) {}
record PropertyMembership(UUID groupId) {}
```

Participation precedence: explicit per-instance opt-out/override → resolved HyThief profile → false by default. Profiles may be supplied by third-party packs/config. Validate overlapping profile conflicts instead of relying on file order. Being pickpocketable, humanoid-looking, in the old 185-role resolver, or already hostile does not automatically grant property-crime participation.

Start with the proven Kweebec witness/reaction profile. Hostile Skeletons remain outside property witnessing unless explicitly opted in. Custom Ancient Scholar roles can select theft-awareness and faction membership in data; their reaction profile must still map to a supported native signal/state contract. Arbitrary factions do not automatically understand Kweebec_All_Danger. Do not promise working custom guards solely because membership configuration parses.

## 8. Runtime resolution and performance

Keep the intended flow:

1. Existing successful withdrawal attribution identifies player and live source container; no theft on open/browse/insertion/rearrangement/failed transaction.
2. Read normalized ownership from that specific live instance. Stop if unowned/unresolved.
3. Read player stance/position once at the withdrawal boundary.
4. Query nearby NPCs through `NPCPlugin.get().getNpcSpatialResource()` and `SpatialResource.getSpatialStructure().collect(position, maxRange, candidates)`; `NPCSpatialSystem` already indexes NPC+Transform entities.
5. Apply cheap validity, theft-awareness, and scope membership filters. NEARBY ignores faction/prefab; PREFAB matches explicit instance UUID; FACTION matches profile/current membership.
6. Use unchanged 20/6 container range, native facing/FOV and fresh LOS. Filter range/FOV before expensive LOS. Normal perception stays 15/4.
7. Classify the withdrawal as witnessed if an eligible witness passes; emit one existing caught message for that theft classification and call the existing native reaction adapter.

Do not retain the fixture's whole-store scan/coordinate-pinned Sapling selection as production architecture. `ContainerVisualWitness.capture` currently intentionally restricts discovery to its staged Kweebec; the reusable evaluate/math portion is valuable, discovery is a separate future replacement. Reaction selection policy remains a separate boundary; ownership data does not authorize town-wide alerts.

Keep this on the owning world/store thread. Snapshot data or enqueue work if packet ingress is on another thread. Validate Ref/store affiliation and player disconnects. Do not force-load distant chunks, scan all structures, or iterate every owned container per withdrawal. No continuous perception polling.

Use small immutable loaded profile maps/native group indexes with asset-reload invalidation. Resolve ownership once; scope/filter before LOS; candidate cost scales with nearby NPC density, not world/container count. Use normal chunk unload cleanup for transient caches. Multiplayer needs transaction-level coalescing/exactly-once notifications; retain player attribution and do not infer the offender from the last opener of a shared chest. Do not cache visual witness results between thefts, since stance/facing/LOS can change.

## 9. HyStash compatibility

Ownership and loot remain separate components/data concerns. A native chest, custom chest or future wardrobe can carry `HyThiefOwnership` regardless of its ItemContainer contents or droplist. HyStash may eventually supply the same native block holder or call a narrow HyThief ownership assignment API if explicitly integrated. No HyStash code/dependency is needed now.

Valid example: loot = Feran artifact pool; owner faction = Ancient Scholar. Do not infer owner from Droplist, chest skin, biome, NPC appearance or generated item IDs. Do not infer loot from owner. This is a potential future integration interface, not a proposal to move either project's systems into shared infrastructure without review.

## 10. Vanilla-prefab release fallback

A broader scan found **7,828 JSON prefabs**. Only two contain literal chest-named blocks (one blockset showcase and Goblin_Thief_Chest); many real settlement chests are realized through **Block_Spawner_Block** and nested Prefab_Spawner_Block, not direct chest blocks. The previous Klop report's zero-chest count described direct blocks only; it did not mean Klop caves cannot generate chests.

46 native BlockSpawner tables include chest/ItemContainer entries. Concrete native mapping sources:

| Family | Explicit table IDs in family-named prefabs | Authored placeholder occurrences | Feasibility |
|---|---|---:|---|
| Kweebec | Zone1_Kweebec_Tier1/2/3 | 30 / 1 / 15 | Best first fallback; proven witness/reaction |
| Feran | Zone2_Feran_Tier1/2; also Portals_Oasis | 7 / 8; 9 | Good property-placement candidates; reaction profile still needs verification |
| Trork | Zone1_Trork_Tier1/2/3 | 5 / 8 / 16 | Mapping feasible; coarse native chest protection may already react on opening |
| Outlander | Zone3_Outlander_Tier1/2/3 | 34 / 19 / 60 | Mapping feasible; native Trork-derived chest/event behavior may conflict |
| Goblin | Zone1_Goblin_Tier1/2/3 | 1 / 6 / 95 | Mapping feasible; existing hostility often makes social witnessing unnecessary |
| Klop | Generic Zone1/2/3_Encounters_Tier3 and Zone3_Encounters_Tier1 | 34 / 23 / 50 / 1 | Requires explicit prefab-origin mapping; generic table alone cannot imply Klop ownership |

These are static occurrences, not guaranteed generated chest totals. Weighted Empty entries, nested prefab choices and placement rules change actual generation. Family name selection is an inventory aid, not proof of rights. Some Feran/Outlander/Goblin prefabs also use generic encounter tables; do not automatically classify those globally.

Exact examples:

- `Server/Prefabs/Npc/Kweebec/Autumn/Shops/Kweebec_Autumn_Shops_001.prefab.json` contains a Zone1_Kweebec_Tier3 placeholder.
- `Server/Item/Block/Spawners/New/Zone1_Kweebec_Tier1.json` selects Furniture_Kweebec_Chest_Small at weight 100 and Empty at 20; tier2 Empty 50, tier3 Empty 100. ItemContainerBlock supplies native droplist. An ownership policy must be declared separately.
- `Server/Item/Block/Spawners/New/Zone2_Feran_Tier1.json` selects Furniture_Feran_Chest_Small; Portals_Oasis is a separate table.
- `Server/Item/Block/Spawners/New/Zone1_Trork_Tier3.json` selects Furniture_Lumberjack_Chest_Small, demonstrating why chest model is not reliable faction identity.
- `Server/Item/Block/Spawners/New/Zone3_Outlander_Tier3.json` selects Furniture_Temple_Dark_Chest_Small.
- `Server/Prefabs/Goblin_Thief_Chest.prefab.json` is a direct chest component example; its Drop_Goblin_Thief contents do not themselves declare ownership.

A reasonable interim release maps a curated small set of **known placement origins/spawner sites** to explicit faction/ownership metadata during generation, then uses the same pipeline. Do not mark every Kweebec-looking chest globally: player-built chests could share the asset. New-generation mapping is easier than retrospectively classifying existing worlds after placeholders disappear.

Native `BlockSpawnerPlugin$BlockSpawnerSystem` realizes entries by setting the selected block and then setting a cloned entry Components holder. A naive ownership copy attached to the old placeholder may be discarded. Use deliberately stamped final holders or a verified placement integration; do not assume the generic replacement event alone retains the spawner context through both operations. Native table component overrides are possible data mechanisms, but asset conflicts/Empty handling and legacy-world migration need review before choosing them.

Manual work estimate: a small family-specific pilot can curate a few table/site policies rather than hundreds of world XYZ positions; full vanilla coverage requires reviewing nested prefabs, generic encounter sites, old saves, and each faction's native reaction contract. It is a reasonable staged fallback, **not an already-complete vanilla ownership map**. Keep existing player worlds unmodified until explicit migration policy is approved.

## 11. Minimal production checkpoints after review

1. Isolated DevTesting proof first: register codec-backed ownership component, attach to one ordinary chest, save/reload/restart, open/close repeatedly, export/paste two copies, break and rebuild at same XYZ. Prove dirty marking and holder replacement preservation. No active gameplay installation for this proof unless separately approved.
2. First production slice: Owned flag + NEARBY, privileged targeted creator command/tool, explicit Kweebec theft-aware profile, component lookup after existing withdrawal attribution. Regression test safe/open/browse/unseen cases, caught message, guard LastSeen, and unchanged pickpocket.
3. FACTION: data-driven NPCGroup/profile membership, explicit per-instance overrides, correct custom faction IDs, no change to native attitudes. Verify Kweebec vs Feran scope separation.
4. PREFAB: controlled placement holder callbacks, fresh property UUID per local group, NPC marker propagation, copies/restart/chunk/nested tests. Document unsupported generation paths instead of guessing.
5. Only if creator coverage blocks a release: curated vanilla placement-origin mapping, initially Kweebec. No global chest-type/loot inference.

These are recommendations, not authorization to start production implementation. The first go/no-go is container lifecycle persistence; the second is prefab membership remapping. Neither requires weakening witnessed-theft design.

## 12. Supporting implementation paths and close-out

Stable source-level evidence in copied server binary:

- `component/ComponentRegistry`, `ComponentRegistryProxy`, `Holder`, codec-backed registration/serialization.
- `server/core/plugin/PluginBase` — chunk/entity registries.
- `server/core/modules/block/BlockEntity`, `BlockModule`, `BlockReplaceEvent`.
- `server/core/modules/block/system/ItemContainerSystems$OnReplaced`, `$OnReplacedHolder`.
- `server/core/universe/world/chunk/BlockOperations`, `section/BlockComponentSection`, `BlockComponentChunk`.
- `server/core/universe/world/storage/provider/RocksDbChunkStorageProvider$Saver` — component snapshot/save path.
- `server/core/prefab/config/SelectionPrefabSerializer`, `selection/buffer/BsonPrefabBufferDeserializer`, `BinaryPrefabBufferCodec`, `impl/PrefabBufferBlockEntry`.
- `server/core/util/PrefabUtil`; `prefab/event/PrefabPasteEvent`, `PrefabPlaceEntityEvent`.
- `server/core/modules/entity/component/FromPrefabInstance`, `FromPrefab`; `EntityModule`; `universe/world/worldgen/GeneratedEntityChunk`.
- `server/npc/components/SpawnMarkerReference`, `SpawnReference`; `server/spawning/spawnmarkers/SpawnMarkerSystems$Ticking`, `SpawnLineage`.
- `builtin/triggervolumes/prefab/TriggerVolumeWorldGenHandler`; `manager/TriggerVolumeManager`.
- `server/npc/role/support/WorldSupport`, `config/AttitudeGroup`, `builtin/tagset/config/NPCGroup`, `builtin/adventure/reputation/ReputationGroupComponent`.
- `server/npc/NPCPlugin`, `systems/NPCSpatialSystem`, `component/spatial/SpatialStructure`.
- `builtin/blockspawner/BlockSpawnerPlugin$BlockSpawnerSystem`, `BlockSpawnerTable`, and native `Server/Item/Block/Spawners/New/*.json`.

The companion `PRODUCTION_CONTAINER_OWNERSHIP_NATIVE_MAP.md` records exact scanned spawner/table/prefab examples. Local research outputs include failed class lookups as diagnostic evidence; those failed guesses are not used as API claims.

No production code or isolated proof code was written. No JAR was built/installed. Protected snapshots cover source, Server assets, BuildSupport, archived releases, Mods, all existing Saves, and reusable test masters/packs/prefabs. Verification is recorded locally. Stop for Firebeard architecture review.
