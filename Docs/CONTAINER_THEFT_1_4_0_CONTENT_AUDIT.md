# HyThief 1.4.0 container content audit

Candidate only; Hytale 0.7.2, revision `d769dc3d57881261a62858e1394f0e4de9a21eca`. Source: installed read-only Assets.zip and the matching server API/bytecode. The complete machine-readable audit is local at `DevTesting/Release_1_4_0/container-landscape.json`.

Scanned all 9,282 native prefabs. The broad name/inventory scan found 106 candidate item IDs, including false-positive armor chest pieces; only 49 resolve an `ItemContainerBlock`. Chest furniture alone does not imply ownership. No container block defaults or prefab files are changed.

## Ownership classifications

| Class | Decision |
|---|---|
| A: clear FACTION | Eleven currently used Kweebec/Feran/Trork/Outlander loot-spawner tables below. |
| B: LOCAL | Existing explicitly authored persistent NEARBY ownership is supported. No unambiguous stock mixed-owner mapping was found; no speculative automatic LOCAL assignment. |
| C: unowned | Craftable/player-placed defaults, decorative/non-inventory props, unmarked generated or existing containers. |
| D: special native | Klop property/station behavior, Goblin/Breach content and other special systems left native. |
| E: deferred | Generic encounter tables, ancient/undead/dungeon treasure, unused Zone3 Kweebec tables, uncertain owners and instance-specific provenance. |

## Exact automatic mappings

| Native spawner table | Faction |
|---|---|
| `Zone1_Kweebec_Tier1` | Kweebec |
| `Zone1_Kweebec_Tier2` | Kweebec |
| `Zone1_Kweebec_Tier3` | Kweebec |
| `Zone2_Feran_Tier1` | Feran |
| `Zone2_Feran_Tier2` | Feran |
| `Zone1_Trork_Tier1` | Trork |
| `Zone1_Trork_Tier2` | Trork |
| `Zone1_Trork_Tier3` | Trork |
| `Zone3_Outlander_Tier1` | Outlander |
| `Zone3_Outlander_Tier2` | Outlander |
| `Zone3_Outlander_Tier3` | Outlander |

Only inventory-bearing entries gain `HyThiefOwnershipProof: {Owned:true,Scope:"FACTION",Faction:"..."}`. Native weights, empty rolls, droplists, rotation and chest identities are preserved. Native `BlockSpawnerEntry.getBlockComponents()` provides a holder; `BlockSpawnerPlugin.BlockSpawnerSystem` clones it into `BlockEntity.setBlockEntity`. This is faction ownership on a prefab-contained chest, not prefab-instance ownership.

**Existing resolved chests are not retroactively tagged.** Their original table/instance provenance is not reliably available. The rollout applies at future native spawner resolution; explicit saved ownership continues working. No nearest-NPC, furniture-style or droplist heuristic backfills ownership.

Concrete generated furniture: Kweebec small chest, Feran small chest, Lumberjack small chest (Trork), Human Ruins small chest and Dark Temple small chest (Outlander). These IDs remain unowned when placed normally; only the selected table entry stamps ownership.

## Native interactive inventory IDs

| Exact ID | Native asset | Default policy |
|---|---|
| `Furniture_Kweebec_Chest_Large` | `Server/Item/Items/Container/Furniture_Kweebec_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Ancient_Chest_Small` | `Server/Item/Items/Furniture/Ancient/Furniture_Ancient_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Ancient_Chest_Large` | `Server/Item/Items/Furniture/Ancient/Unique/Furniture_Ancient_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Christmas_Chest_Small` | `Server/Item/Items/Furniture/Christmas/Furniture_Christmas_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Christmas_Chest_Small_Green` | `Server/Item/Items/Furniture/Christmas/Furniture_Christmas_Chest_Small_Green.json` | Unowned unless an authoritative component is attached |
| `Furniture_Christmas_Chest_Small_Red` | `Server/Item/Items/Furniture/Christmas/Furniture_Christmas_Chest_Small_Red.json` | Unowned unless an authoritative component is attached |
| `Furniture_Christmas_Chest_Small_RedDotted` | `Server/Item/Items/Furniture/Christmas/Furniture_Christmas_Chest_Small_RedDotted.json` | Unowned unless an authoritative component is attached |
| `Furniture_Christmas_Chest_Small_White` | `Server/Item/Items/Furniture/Christmas/Furniture_Christmas_Chest_Small_White.json` | Unowned unless an authoritative component is attached |
| `Furniture_Crude_Chest_Small` | `Server/Item/Items/Furniture/Crude/Furniture_Crude_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Crude_Chest_Large` | `Server/Item/Items/Furniture/Crude/Unique/Furniture_Crude_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Desert_Chest_Small` | `Server/Item/Items/Furniture/Desert/Furniture_Desert_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Desert_Chest_Large` | `Server/Item/Items/Furniture/Desert/Unique/Furniture_Desert_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Dungeon_Chest_Epic` | `Server/Item/Items/Furniture/Dungeon/Furniture_Dungeon_Chest_Epic.json` | Unowned unless an authoritative component is attached |
| `Furniture_Dungeon_Chest_Epic_Large` | `Server/Item/Items/Furniture/Dungeon/Furniture_Dungeon_Chest_Epic_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Dungeon_Chest_Legendary_Large` | `Server/Item/Items/Furniture/Dungeon/Furniture_Dungeon_Chest_Legendary_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Feran_Chest_Small` | `Server/Item/Items/Furniture/Feran/Furniture_Feran_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Feran_Chest_Large` | `Server/Item/Items/Furniture/Feran/Unique/Furniture_Feran_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Frozen_Castle_Chest_Small` | `Server/Item/Items/Furniture/Frozen/Furniture_Frozen_Castle_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Frozen_Castle_Chest_Large` | `Server/Item/Items/Furniture/Frozen/Unique/Furniture_Frozen_Castle_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Goblin_ChestStolen_Small` | `Server/Item/Items/Furniture/Goblin/Furniture_Goblin_ChestStolen_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Goblin_Chest_Large` | `Server/Item/Items/Furniture/Goblin/Furniture_Goblin_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Goblin_Chest_Small` | `Server/Item/Items/Furniture/Goblin/Furniture_Goblin_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Goblin_Coffer` | `Server/Item/Items/Furniture/Goblin/Furniture_Goblin_Coffer.json` | Unowned unless an authoritative component is attached |
| `Furniture_Royal_Magic_Chest_Small` | `Server/Item/Items/Furniture/Grand_Wizard/Furniture_Royal_Magic_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Royal_Magic_Chest_Large` | `Server/Item/Items/Furniture/Grand_Wizard/Unique/Furniture_Royal_Magic_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Human_Ruins_Chest_Small` | `Server/Item/Items/Furniture/Human/Furniture_Human_Ruins_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Human_Ruins_Chest_Large` | `Server/Item/Items/Furniture/Human/Unique/Furniture_Human_Ruins_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Jungle_Chest_Small` | `Server/Item/Items/Furniture/Jungle/Furniture_Jungle_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Jungle_Chest_Large` | `Server/Item/Items/Furniture/Jungle/Unique/Furniture_Jungle_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Kweebec_Chest_Small` | `Server/Item/Items/Furniture/Kweebec/Furniture_Kweebec_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Lumberjack_Chest_Small` | `Server/Item/Items/Furniture/Lumberjack/Furniture_Lumberjack_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Lumberjack_Chest_Large` | `Server/Item/Items/Furniture/Lumberjack/Unique/Furniture_Lumberjack_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Scarak_Hive_Chest_Small` | `Server/Item/Items/Furniture/Scarak_Hive/Furniture_Scarak_Hive_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Scarak_Hive_Chest_Large` | `Server/Item/Items/Furniture/Scarak_Hive/Unique/Furniture_Scarak_Hive_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Tavern_Chest_Small` | `Server/Item/Items/Furniture/Tavern/Furniture_Tavern_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Tavern_Chest_Large` | `Server/Item/Items/Furniture/Tavern/Unique/Furniture_Tavern_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Temple_Dark_Chest_Small` | `Server/Item/Items/Furniture/Temple_Dark/Furniture_Temple_Dark_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Temple_Dark_Chest_Large` | `Server/Item/Items/Furniture/Temple_Dark/Unique/Furniture_Temple_Dark_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Temple_Emerald_Chest_Small` | `Server/Item/Items/Furniture/Temple_Emerald/Furniture_Temple_Emerald_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Temple_Emerald_Chest_Large` | `Server/Item/Items/Furniture/Temple_Emerald/Unique/Furniture_Temple_Emerald_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Temple_Light_Chest_Small` | `Server/Item/Items/Furniture/Temple_Light/Furniture_Temple_Light_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Temple_Light_Chest_Large` | `Server/Item/Items/Furniture/Temple_Light/Unique/Furniture_Temple_Light_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Temple_Scarak_Chest_Small` | `Server/Item/Items/Furniture/Temple_Scarak/Furniture_Temple_Scarak_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Temple_Scarak_Chest_Large` | `Server/Item/Items/Furniture/Temple_Scarak/Unique/Furniture_Temple_Scarak_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Temple_Wind_Chest_Small` | `Server/Item/Items/Furniture/Temple_Wind/Furniture_Temple_Wind_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Temple_Wind_Chest_Large` | `Server/Item/Items/Furniture/Temple_Wind/Unique/Furniture_Temple_Wind_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Furniture_Village_Chest_Small` | `Server/Item/Items/Furniture/Village/Furniture_Village_Chest_Small.json` | Unowned unless an authoritative component is attached |
| `Furniture_Village_Chest_Large` | `Server/Item/Items/Furniture/Village/Unique/Furniture_Village_Chest_Large.json` | Unowned unless an authoritative component is attached |
| `Debug_MusicPlayer` | `Server/Item/Items/_Debug/Debug_MusicPlayer.json` | Unowned unless an authoritative component is attached |

## Concrete witness coverage

33 ordinary roles. Native NPC-group assets are the explicit allowlist; Java selects one of the proven Guard, TrorkEnemySighted or Combat.Default contracts. Native candidate validation verifies the expected state/message capability. No new pickpocket support policy is introduced.

### Kweebec

- `Kweebec_Merchant`
- `Kweebec_Razorleaf`
- `Kweebec_Razorleaf_Patrol`
- `Kweebec_Rootling`
- `Kweebec_Sapling`
- `Kweebec_Sapling_Orange`
- `Kweebec_Sapling_Pink`

### Feran

- `Feran_Burrower`
- `Feran_Civilian`
- `Feran_Longtooth`
- `Feran_Sharptooth`
- `Feran_Windwalker`

### Rally

- `Trork_Brawler`
- `Trork_Mauler`
- `Trork_Sentry`
- `Trork_Sentry_Patrol`
- `Trork_Unarmed`
- `Trork_Warrior`
- `Trork_Warrior_Patrol`
- `Outlander_Berserker`
- `Outlander_Brute`
- `Outlander_Cultist`
- `Outlander_Hunter`
- `Outlander_Peon`
- `Outlander_Stalker`

### Combat

- `Skeleton_Fighter`
- `Zombie`
- `Zombie_Aberrant`
- `Zombie_Aberrant_Big`
- `Zombie_Aberrant_Small`
- `Zombie_Burnt`
- `Zombie_Frost`
- `Zombie_Sand`

Deferred from container witnessing: Goblins, Klops, wildlife/companions, Trork Ranger/leader/mage paths, Outlander leaders/casters, all Skeleton roles other than Fighter in this first bounded rollout, and special/scripted/dungeon/summoned roles. Their existing pickpocket support remains unchanged. Undead have no speculative automatic property assignments; explicitly owned containers can still be witnessed by supported Skeleton/Zombie roles.

The six representative native response proofs are not connected-client certification of every listed role. Final client approval remains required.

## Native prefab usage evidence

| Table | Occurrences | Example native prefab paths |
|---|---:|---|
| `Zone1_Kweebec_Tier1` | 30 | `Server/Prefabs/Npc/Kweebec/Oak/Houses_Guard/Kweebec_Oak_Houses_Guard_001.prefab.json`; `Server/Prefabs/Npc/Kweebec/Oak/Houses_Guard/Kweebec_Oak_Houses_Guard_002.prefab.json` |
| `Zone1_Kweebec_Tier2` | 1 | `Server/Prefabs/Npc/Kweebec/Azure/Kweebec_Azure_KweebecCamps_Azure_001.prefab.json` |
| `Zone1_Kweebec_Tier3` | 15 | `Server/Prefabs/Npc/Kweebec/Autumn/Shops/Kweebec_Autumn_Shops_001.prefab.json`; `Server/Prefabs/Npc/Kweebec/Autumn/Shops/Kweebec_Autumn_Shops_002.prefab.json` |
| `Zone2_Feran_Tier1` | 7 | `Server/Prefabs/Npc/Feran/Tier1/Corners/Feran_Tier1_Corners_001.prefab.json`; `Server/Prefabs/Npc/Feran/Tier1/Corners/Feran_Tier1_Corners_002.prefab.json` |
| `Zone2_Feran_Tier2` | 8 | `Server/Prefabs/Npc/Feran/Tier1/Straight/Normal/Feran_Tier1_Straight_Normal_002.prefab.json`; `Server/Prefabs/Npc/Feran/Tier2/Chieftain/Feran_Tier2_Chieftain_001.prefab.json` |
| `Zone1_Trork_Tier1` | 5 | `Server/Prefabs/Monuments/Encounter/Zone3/Tier3/Trork/Coast/Tower/Encounter_Coast_Tower_001.prefab.json`; `Server/Prefabs/Npc/Trork/Tier_1/Encampment/Hunter/Forest_Birch/Trork_Tier_1_Encampment_Hunter_Forest_Birch_001.prefab.json` |
| `Zone1_Trork_Tier2` | 8 | `Server/Prefabs/Npc/Trork/Tier_2/Encampment/Lumber/Forest_Aspen/Trork_Tier_2_Encampment_Lumber_Forest_Aspen_001.prefab.json`; `Server/Prefabs/Npc/Trork/Tier_2/Encampment/Lumber/Forest_Autumn/Trork_Tier_2_Encampment_Lumber_Forest_Autumn_001.prefab.json` |
| `Zone1_Trork_Tier3` | 16 | `Server/Prefabs/Monuments/Encounter/Zone3/Tier3/Trork/Coast/Large/Encounter_Coast_Large_Trork_Zone3_Tier3_Coast_Large_001PLACEHOLDER.prefab.json`; `Server/Prefabs/Monuments/Encounter/Zone3/Tier3/Trork/Coast/Small/High/Encounter_Small_High_Trork_Zone3_Tier3_Coast_Small_001PLACEHOLDER.prefab.json` |
| `Zone3_Outlander_Tier1` | 37 | `Server/Prefabs/Monuments/Encounter/Zone3/Tier1/Quarry/Encounter_Zone3_Tier1_001.prefab.json`; `Server/Prefabs/Npc/Outlander/Boats/Large/Outlander_Boats_Large_001.prefab.json` |
| `Zone3_Outlander_Tier2` | 19 | `Server/Prefabs/Dungeon/Outlander_Temple/Stage2/Room/Temple_Stage2_Room_17x35_N_001v2.prefab.json`; `Server/Prefabs/Monuments/Encounter/Zone3/Tier2/Battlefield/Outlander_Ruins/Encounter_Tier2_Outlander_Ruins_001.prefab.json` |
| `Zone3_Outlander_Tier3` | 60 | `Server/Prefabs/Dungeon/Outlander_Temple/Stage2/Corridor/Turn/18x9/E/Outlander_Temple_Stage2_Corridor_Turn_18x9_E_001.prefab.json`; `Server/Prefabs/Dungeon/Outlander_Temple/Stage2/Corridor/Turn/18x9/W-E/Outlander_Temple_Stage2_Corridor_Turn_18x9_W-E_001.prefab.json` |

The native Trork chest-interference block set matches `Furniture_Crude_Chest_*`; the mapped Trork tables use `Furniture_Lumberjack_Chest_Small`. No crude-chest native warning/damage path is replaced. Klop stations and generic encounter tables remain untouched. Native hostility/property responses independent of HyThief are preserved.
