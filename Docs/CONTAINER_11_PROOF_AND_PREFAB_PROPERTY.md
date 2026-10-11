# Container.11 end-of-night checkpoint and minimal PREFAB/property investigation

Date: 2026-10-10. Documentation/research only. Target inspected: installed stable Hytale 0.6.8. Container gameplay remains 1.3.1-container.11; no new JAR, source/asset edits, world reset, creator tools or release tonight.

## 1. Container.11 is REAL-CLIENT PROVEN

Firebeard reported the following results after the symmetric fixture correction in commit 86fcedc (remote main verified). These are reported real-client results, not conclusions drawn from the offline harness or independently re-inspected client logs.

| NEARBY trial | Result | Caught messages | Native responders |
|---|---|---|---|
| Both witnesses visible | PASS | Exactly one | Razorleaf and Sharptooth |
| Only Kweebec witness LOS blocked | PASS; Feran can witness | Exactly one | Razorleaf and Sharptooth |
| Only Feran witness LOS blocked | PASS; Kweebec can witness | Exactly one | Razorleaf and Sharptooth |

Both witnesses were 11.884864324 blocks from player position (0,64,11.5), with mirrored 22.249024-degree facing angles, equal elevation and unobstructed baseline LOS. Geometry therefore did not favor either family. Preserve prior gameplay rollback f90bbcf and the saved geometry rollback documented in CONTAINER_FIELD_SYMMETRY.md.

Current proven model:

`persistent container ownership scope -> theft type -> eligible visual witness confirms successful withdrawal -> one caught notification -> theft type selects native alert branch -> native role handles target, pursuit and combat`

- FACTION: ownership selects the faction; only matching witnesses qualify. Existing native Kweebec_All_Danger / Feran_Warriors_Attack adapter selects the appropriate ready faction responder (current 20-block handoff).
- NEARBY: any eligible theft-aware visual witness qualifies; emits LOCAL via HyThief_Local_Theft, independently of witness faction. Existing 30-block witness-centered delivery selects compatible ready native listeners; both scoped test responders are proven.
- Successful owned removal, player attribution, native chest opening, exactly-one caught message, 20/6 container visual range, 15/4 normal perception, LOS/FOV, native target/LastSeen/combat and pickpocket behavior remain the reusable foundation. This proof covers the configured test participants, not every arbitrary native role.
- PREFAB is NOT implemented or runtime-proven yet.

## 2. Native-first recheck

The pinned development server and read-only installed Server/HytaleServer.jar have the same SHA256: DCD2956CC65B950084650EADD56B889CF51F7610C127C3DAAC1F74DC471FEC97. Fresh javap signatures/bytecode were checked from that binary; earlier detailed analysis remains in DevTesting/Production_Ownership_Reference_0_6_8 and Docs/PRODUCTION_CONTAINER_OWNERSHIP_ARCHITECTURE.md. This is inspection, not a new lifecycle/paste runtime proof.

| Native mechanism | Verified meaning / useful part | Why it is not sufficient alone |
|---|---|---|
| FromPrefabInstance | Codec-backed Component<EntityStore>, integer getPrefabInstanceId(); registered by EntityModule. GeneratedEntityChunk attaches world-generation provenance. | It is not a shared ChunkStore container component. Ordinary PrefabUtil paste inspected here adds FromPrefab, not FromPrefabInstance; later marker-spawned NPCs are not shown inheriting it. No universal chest-to-NPC property relationship is established. |
| FromPrefab | Entity marker; native ClearFromPrefabMarker system clears it. | No per-property value; not persistent ownership identity. |
| PrefabPasteEvent / PrefabPlaceEntityEvent | Runtime paste ID; latter exposes Holder<EntityStore>. Useful for entity-paste integration. | PrefabUtil.PREFAB_ID_SOURCE is static AtomicInteger initialized to zero, using getAndIncrement. A process-local operation ID is not a durable group key. These events do not expose a common per-block ownership holder. |
| PrefabUtil.paste holder consumers | Flag-based overloads expose Consumer<Holder<ChunkStore>> and Consumer<Holder<EntityStore>>. Cloned holders can be annotated consistently by one controlled placement operation. | A practical integration point, not automatic provenance for every editor/worldgen/spawner path. Do not assume registering a paste event covers all block callbacks. |
| SpawnMarkerReference / SpawnReference | Codec-backed InvalidatablePersistentRef to the actual spawn marker; current SpawnMarkerEntity code establishes this reference. | Identifies a marker, not a building. Multiple markers may serve one property; a chest is not automatically associated with that marker. Useful to carry explicit property membership through respawn later. |
| SpawnLineage | Serializable lineage string with native inherit helper. | No verified common container/property-instance semantics. Do not overwrite native lineage merely to express ownership. |
| NPCGroup, WorldSupport, reputation/attitude | Native role-based participation/faction grouping. | Two Kweebec shops can have identical faction/roles but different property owners. Already sufficient for FACTION, not PREFAB. |
| Flock membership / native trigger groups | Native group identity and per-placement remapping patterns exist. | Flocks govern AI; trigger groups are optional authored content, not automatic chest+NPC ownership. Repurposing them risks changing behavior and creates more coupling than a tiny component. |
| PersistentPrefabPreview / PrefabCopyableComponent | Preview stores prefabKey, visible layers/tints; copy marker allows inclusion in prefabs. | Preview configuration and copy eligibility, not membership of placed chests and their NPCs. |
| HytaleGenerator WorldStructure | World-generation biome/position-provider configuration. | Not an inspected persistent building membership component shared with runtime container/NPC entities. No universal property association found here. |
| Native block/entity UUIDs, prefab asset name, coordinates | Each can identify an object, template or location. | Distinct objects have distinct entity UUIDs; the same template can be pasted twice; proximity/location does not establish rights. None alone identifies both Shop A chest and Shop A NPCs. |

Installed Assets.zip examples were read directly without extraction or mutation:

- Server/Prefabs/Goblin_Thief_Chest.prefab.json has a per-block components.Components.ItemContainerBlock holder. Native prefab component serialization is a suitable carrier for extra ownership data.
- Server/Prefabs/Npc/Kweebec/Autumn/Shops/Kweebec_Autumn_Shops_001.prefab.json has BlockSpawnerId=Zone1_Kweebec_Tier3 and an NPC_Spawn_Marker with SpawnMarker=Kweebec_Merchant. The shop template is not itself a shared runtime property ID; containers may be generated by replacement and NPCs spawned later.
- Earlier inspected SelectionPrefabSerializer / BsonPrefabBufferDeserializer encode/decode lowercase components through ChunkStore registry. PrefabUtil passes cloned holder data onward. This supports the small-component approach but does not yet prove every custom entity/block component survives every placement path.

Official context: [Update 6 patch notes](https://hytale.com/news/2026/8/update-6-patch-notes) direct modders to flag-based PrefabUtil overloads and describe universe-scoped persistent resources. Neither feature promises universal property membership. A universe registry is unnecessary for the proposed first slice. Official API pages for [FromPrefabInstance](https://docs.hytale.com/api/com/hypixel/hytale/server/core/modules/entity/component/FromPrefabInstance) and its [package](https://docs.hytale.com/api/com/hypixel/hytale/server/core/modules/entity/component/package-summary) were located, but full-page fetch returned cache-miss errors; detailed conclusions rely on the installed 0.6.8 public API/bytecode instead. No observed documentation-versus-installed-data disagreement; coverage/guarantees remain incomplete.

**Conclusion:** native storage and placement hooks are sufficient building blocks; native identity alone is not verified as sufficient for reliable property matching across all relevant paths. The previous concern about partial provenance remains valid.

## 3. Recommended smallest architecture

Use a tiny HyThief membership tag, persisted using native ECS codecs. Its value should be a generated PropertyGroupId UUID per live property instance. This is the small metadata approach (B) with a collision-safe durable value (C), justified by the missing common native identity; it is not a new property framework.

Minimum authoritative data:

- Container's existing HyThiefOwnershipProof: Owned=true, Scope=PREFAB, PropertyGroupId=<UUID string>. Keep Faction optional/irrelevant for this scope. Retain existing component ID and existing Owned/Scope/Faction defaults so old saves remain readable; clone must include the new field. Missing/malformed property binding must reject PREFAB witnessing/delivery with an explicit diagnostic, never silently become NEARBY or FACTION.
- NPC (and later its spawn marker): one codec-backed HyThiefPropertyMembership entity component with the same PropertyGroupId. Membership is per instance, not a new role or family rule. Responders need it as well as witnesses. Compare within the same owning world/store; do not link worlds through equal IDs.
- No mandatory region, property anchor, world-position registry, database, faction rewrite, ownership polygon, per-tick tracking or movement/combat system. UUIDs can live directly in the participating components.

PREFAB flow:

`successful owned removal -> scope=PREFAB -> candidate witness has same nonempty property UUID -> unchanged stance/range/FOV/LOS -> one caught message -> property-specific dispatch branch -> same-property compatible ready responders receive native offender beacon -> native role behavior`

Property membership is required at **both** witness selection and responder delivery. Filtering witnesses alone would let Shop B guards receive an unrestricted LOCAL alarm from Shop A. Same faction does not override a property mismatch. The dispatcher should recheck the selected witness membership and source snapshot before delivery.

Smallest dispatch can classify Result.type as PROPERTY (scope name remains PREFAB), then reuse the proven addressed BeaconBroadcast.post recipient handoff and existing HyThief_Local_Theft role listener, filtering candidates by PropertyGroupId first. Current bytecode shows post(ref,...) sends to that recipient's BeaconSupport.postMessage; it does not itself perform an area broadcast. A second signal name/listener is therefore not required solely for routing. Keep existing 30-block candidate collection/readiness for the first property proof rather than introducing another tuning value. Existing FACTION/NEARBY branches must remain byte-for-byte behaviorally equivalent.

This reuses native perception and combat; it does not manually puppet guards. Whether a later native combat/alarm action re-broadcasts to outsiders must still be verified in the property proof. If it does, stop and isolate that specific secondary path; do not claim complete property confinement just because initial post is filtered. Normal independent aggro, combat assistance and damage reactions remain native behavior, outside theft classification.

Plain creator strings could work for a manually managed small save, but repeated exports/pastes can accidentally reuse them. UUID generation avoids manual uniqueness bookkeeping. The human-readable name is only a label. Even UUIDs copied verbatim are not enough: each newly placed property copy must receive a fresh UUID, shared only by its declared members.

## 4. Creator metadata / eventual low-friction workflow

No creator tools are being implemented tonight.

For an existing placed building: creator selects the intended chests, witnesses and responders and assigns one named property. Tool creates/stamps the UUID; creator never types it. Only owned chests receive Owned=true/Scope=PREFAB; NPCs/markers receive membership. An NPC may be intentionally unassigned. Multiple membership, nested property hierarchies and settlement networks are deferred.

For a reusable prefab: creator assigns a simple local PropertyKey such as shop_a to relevant holders, plus container scope/owned metadata. A supported placement operation maps each distinct local key to one new UUID and stamps both block and entity holders through the native consumers. Explicitly preserve ItemContainerBlock/native NPC role data. Two copies get different UUIDs; rotation does not change membership. Local key is optional authoring metadata, not live authorization. Saving an existing property and pasting it again must remap membership rather than duplicating its live UUID.

Spawn-marker membership must transfer to NPCs on initial spawn/respawn; nested BlockSpawner/PrefabSpawner replacement must stamp the final chest holder. Those integrations are separate verified placement boundaries, not assumed side effects. Native editor paths lacking a verified remap hook must be explicitly bound/validated after placement until supported; no global current-prefab variable or nearest-NPC inference. Generic native editors have not been proved to expose arbitrary HyThief component fields directly.

## 5. Eventual code seams and size

All names below are prospective changes, not edits performed tonight.

| Existing file / seam | Small change needed |
|---|---|
| src-java/com/local/pickpocket/OwnershipProofData.java: CODEC, accessors, clone() | Optional PropertyGroupId field with backward-compatible defaults and validation. |
| src-java/com/local/pickpocket/PersistentContainerOwnership.java: eligible() | Accept bound PREFAB; retain source.read()/resolve() native identity/inventory checks unchanged. |
| src-java/com/local/pickpocket/ContainerOwnershipPolicy.java: rejection() | Exact property membership comparison before visual evaluation; leave faction/member selectors unchanged. |
| src-java/com/local/pickpocket/HyThiefContainerAlert.java: deliver() | Add scoped PROPERTY branch; filter ready native recipients by matching UUID, reuse addressed post/readiness/target verification. Leave FACTION/LOCAL branches unchanged. |
| src-java/com/local/pickpocket/PickpocketPlugin.java: setup() | Register one codec-backed EntityStore membership component alongside existing ownership registration. |
| New small HyThiefPropertyMembership component/helper | UUID field, clone/codec, pure same-property check; no property manager/registry. |
| src-java/com/local/pickpocket/HyThiefContainerDiagnostics.java | Log property IDs/mismatch/selected recipients; existing caughtOwnership.clone() carries new field automatically once clone is updated. |
| Later supported placement/spawn integration | Small per-placement local-key mapping plus native holder callbacks; propagate marker membership. Defer until basic same-property persistence/filtering proof. |

Core slice estimate: one small component/helper plus about 5-6 existing Java files; roughly 100-200 new/changed production lines and focused development tests. This is a rough planning estimate, not a measured implementation. Automated per-paste remapping and delayed marker/spawner propagation are additional bounded checkpoints with uncertain integration size; they are not included in that estimate and should not be hidden inside the first patch.

Untouched: packet ingress/transfer attribution, successful-removal probe, native windows/chest Use, loot/inventory, notification text/exactly-once flow, ContainerVisualWitness.evaluate math, standing/crouched ranges, existing pickpocket backend/frontend, guard native attack/pursuit/LastSeen actions, responder roles and current four-chest test master. ContainerVisualWitness.capture already calls ContainerOwnershipPolicy.rejection; that policy seam can enforce property eligibility without changing its visual algorithm. Production-wide discovery/creator UX is future scope, not needed to prove matching.

## 6. Risks and first checkpoint tomorrow

Risks: unbound/corrupt IDs; accidentally copying one UUID to multiple placed properties; losing IDs during cloned block-holder replacement; marker respawns dropping membership; custom-component prefab export/import coverage; stale references/chunk unloading; independently acquired native hostility or secondary role broadcasts mistaken for theft routing. Persistent property data does not make an arbitrary unsupported NPC theft-aware or give it a native response contract.

Proposed first checkpoint (requires approval to implement tomorrow): a development-only property membership/persistence proof with two distinct explicitly assigned property IDs, A and B, using equivalent supported roles/factions so faction cannot explain separation. Use disposable test copies and preserve the current four-chest master. Do not build a broad new property framework or automatic vanilla ownership map.

1. Add codec field + one membership component, save/reload, verify chest/NPC/ready responder UUIDs remain identical within A and distinct from B. Retain f90bbcf/86fcedc/container.11 rollback.
2. Demonstrate policy rejection when only B can see A's withdrawal, even when both are Kweebec. A witness must pass unchanged visual checks before caught notification.
3. With an A witness, deliver only to matching A compatible ready responders; B receives no theft beacon/marked offender. Prove native A pursuit/combat without manual movement. Confirm any secondary native alarm does not invalidate the isolation claim.
4. Keep current FACTION and NEARBY regression cases and native safe withdrawal/open/browse cases unchanged. All comparisons use measured symmetric distance/FOV/LOS/elevation/access.
5. Stop for a small runtime handoff after implementation is approved and native validation passes. Controlled two-copy prefab remap (rotation/restart/respawn) is the next checkpoint, not assumed proven by manual assignment.

No implementation started tonight. Container.11 real-client proof is the stopping point; native PREFAB identity limitations and the small property metadata recommendation are documented for tomorrow.
