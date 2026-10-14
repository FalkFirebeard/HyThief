# Two-copy native prefab-ID runtime proof — 0.7.1 / 69c03ffa58d7e2f46bce8b0356ed206cfc7f10e6

## Decision: PREFAB OWNERSHIP DEFERRED

A/B/C passed in native server processes. D (a clean existing production chest association point) did not. Do not implement PREFAB ownership or create replacement property IDs. Proceed with FACTION + NEARBY and the multi-faction hub only after Firebeard review/authorization.

| Evidence | Prefab A | Prefab B |
|---|---|---|
| Native prefab | Kweebec_Autumn_Shops_001 | same byte-identical prefab |
| Worldgen anchor | (-12,64,0) | (12,64,0) |
| FromPrefabInstance ID | 274125628 | 274840612 |
| Marker UUID | 16088ff1-8a36-3626-bd0c-2d78221a68d0 | a8aa9389-fb85-3266-b204-adf26483222c |
| Merchant role | Kweebec_Merchant | Kweebec_Merchant |
| NPC direct FromPrefabInstance | absent | absent |
| NPC SpawnMarkerReference -> marker ID | 274125628 | 274840612 |
| Native chest position | (-10,65,1) | (14,65,1) |
| Chest | Furniture_Kweebec_Chest_Small | same native type |
| Saved chest components | ItemContainerBlock | ItemContainerBlock |
| Runtime chest components | BlockStateInfo + ItemContainerBlock | same |
| Chest prefab ID/provenance | none | none |

The IDs differ by 714984 (24 * 31^3), consistent with native anchor hashing of the same retained buffer. Native marker UUIDs and IDs survived a full process exit and fresh server start. Merchants respawned with new individual UUIDs in the no-player headless setup, while their persistent marker UUID and resolved prefab ID stayed unchanged. Native chest inventories/components survived intact; runtime Refs are store-local, not persistent prefab membership.

## Real native generation path

A diagnostic-only IWorldGenProvider wraps native flat terrain. It calls current `PrefabProp.generate(Prop.Context)` for both anchors using the same retained IPrefabBuffer. The native EntityFunnel receives `EntityPlacementData.getPrefabInstanceId()`; those records pass through `GeneratedEntityChunk.addEntities` and the normal chunk load path, which attaches FromPrefabInstance. Native MaterialWriteSpace writes stock SolidMaterial block/filler/rotation and cloned block holders into the generated chunk. **No ID was calculated, invented or stamped by HyThief.** WorldgenId=0 in this isolated provider is neutral test context, not property identity. It is not an editor paste/counter-ID test.

Normal native merchant marker spawning was used via SpawnMarkerEntity.spawnNPC in headless automation; client world enables both NPC and spawn-marker spawning and uses their normal ticking path. The exact source prefab is Server/Prefabs/Npc/Kweebec/Autumn/Shops/Kweebec_Autumn_Shops_001.prefab.json from current Assets.zip. Native save codecs are Version 4, matching fresh native 0.7.1.

This prefab has a Zone1_Kweebec_Tier3 block-spawner placeholder rather than a baked chest. The stock table can roll Empty, which occurred in the preliminary runs. To provide the requested zero-setup client chests, the isolated proof pack selects the table's existing native Furniture_Kweebec_Chest_Small entry (same component/droplist), omitting Empty. This fixture-only choice does not apply outside this world and does not inject provenance. Both NPC/marker paths and the engine's final chest-replacement path remain native. The copied prefab itself is unchanged.

## Focused chest bridge findings

1. At native generation: the block holder contains BlockSpawnerId=Zone1_Kweebec_Tier3, an asset/table identifier reused across placements. It has no prefab-instance ID. The entity callback alone receives the native instance integer.
2. After native replacement, save and restart: chest holder contains only ItemContainerBlock persistently; runtime additionally carries BlockStateInfo for its position/section. Neither carries the marker's instance ID. Native block-spawner replacement supplies a fresh final component holder, so annotations on a placeholder cannot be assumed to survive.

The diagnostic generator knows which calls it made and can associate its own callback batches with its known anchors. This is **caller-controlled context**, not a demonstrated general native hook for normal base-game shops. Using it in production would require controlling/replacing the placement pipeline. Stock ChunkPreLoadProcessEvent supplies a whole chunk holder/newly-generated flag, not per-block prefab membership; normal PrefabPasteEvent/PrefabPlaceEntityEvent belong to another placement path with process-local IDs. Pairing a chest to a nearby marker, bounding box or same-chunk ID would introduce assumptions for overlapping/nested placements. No small universal stock bridge was verified after these two focused boundary checks; do not disguise the custom generator as such a bridge.

For NPC access: get SpawnMarkerReference from the NPC, resolve getReference().getEntity(accessor), then read FromPrefabInstance.getPrefabInstanceId() on that loaded marker. This requires the marker to remain resolvable. The ID is a 32-bit hash, not collision-proof global identity; no uniqueness/regeneration guarantee outside the tested saved placements is claimed. Direct pasted NPCs and other generation routes are outside this proof.

## Handoff and preservation

Separate active save: HyThief Native Prefab ID Proof; local master and evidence: DevTesting/Native_Prefab_ID_Proof_0_7_1. Source helpers and minimal proof asset are committed; raw base-game prefab, LPF/cache and native save binaries remain local-only. See README.md for no-command A/B open/trade -> complete client exit -> reload sequence. Diagnostics run automatically and are grouped PREFAB A/B. Physical Firebeard client interaction/restart remains pending; do not claim it passed.

Native proof logs: ready-native.log, restart-native.log, client-ready-native.log. Snapshots: before-restart.json, after-restart.json, final-native-snapshot.json; complete marker/chest holder dumps remain in Runs/ready/prefab-id-diagnostics. Both ID and inventory equality were checked after restart. Test-only constructor/output-list corrections and incomplete runs are preserved separately; their IDs are not the final proof IDs.

Existing Container Theft Test World master hashes were verified. The active container save differed from that clean master after client use; it was deliberately left untouched, not reset. No operation in this proof targets that active save. Its reset scripts were not modified. Global HyThief container.11 JAR remains f2dca782e22c38839640e8886f764b16ca00c36cdf8b4c812d6c099181eae08c; public 1.3.2 remains 38c3d5e464c26c1bbca01a4d57424d349b5bb45dca3b502b1937ac015bd16938. No ownership scopes, gameplay, alerts, public releases or property IDs changed. Proof helper is per-world only, target =0.7.1.
