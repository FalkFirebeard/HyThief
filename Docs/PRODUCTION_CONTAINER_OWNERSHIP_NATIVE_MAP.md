# Native container placement sources — ownership architecture investigation

Date: 2026-10-10. Static native data inventory, not ownership assignments. Counts are authored placeholders, not actual generated chest counts. Loot/furniture identity is not property identity.

## Family-prefab spawner sites

| Family | Table ID | Occurrences | Exact example prefab |
|---|---|---:|---|
| Klops | `Zone3_Encounters_Tier3` | 50 | `Server/Prefabs/Cave/Klops/Basalt/Bathroom/Klops_Basalt_Bathroom_001.prefab.json` |
| Klops | `Zone2_Encounters_Tier3` | 23 | `Server/Prefabs/Cave/Klops/Dry/Bedroom/Klops_Dry_Bedroom_001.prefab.json` |
| Klops | `Zone1_Encounters_Tier3` | 34 | `Server/Prefabs/Cave/Klops/Dry/Main_2/Klops_Dry_Main2_001.prefab.json` |
| Klops | `Zone3_Encounters_Tier1` | 1 | `Server/Prefabs/Cave/Klops/Oak/Main_2/Klops_Oak_Main2_001.prefab.json` |
| Goblin | `Zone1_Goblin_Tier2` | 6 | `Server/Prefabs/Dungeon/Goblin_Lair/Entrance_Mine/Abandoned_Village/Layout/Goblin_Lair_Entrance_Abandoned_Village_Layout_001.prefab.json` |
| Goblin | `Zone1_Goblin_Tier1` | 1 | `Server/Prefabs/Dungeon/Goblin_Lair/Prefabs_Goblin/Cap/Hidden_Treasure/Goblin_Lair_Cap_Hidden_Treasure_001.prefab.json` |
| Goblin | `Zone1_Goblin_Tier3` | 95 | `Server/Prefabs/Dungeon/Goblin_Lair/Prefabs_Goblin/Corner/Goblin_Lair_Corner_001.prefab.json` |
| Outlander | `Zone3_Outlander_Tier3` | 60 | `Server/Prefabs/Dungeon/Outlander_Temple/Stage2/Corridor/Turn/18x9/E/Outlander_Temple_Stage2_Corridor_Turn_18x9_E_001.prefab.json` |
| Outlander | `Zone3_Outlander_Tier2` | 19 | `Server/Prefabs/Dungeon/Outlander_Temple/Stage2/Room/Temple_Stage2_Room_17x35_N_001v2.prefab.json` |
| Trork | `Zone1_Trork_Tier3` | 16 | `Server/Prefabs/Monuments/Encounter/Zone3/Tier3/Trork/Coast/Large/Encounter_Coast_Large_Trork_Zone3_Tier3_Coast_Large_001PLACEHOLDER.prefab.json` |
| Trork | `Zone1_Trork_Tier1` | 5 | `Server/Prefabs/Monuments/Encounter/Zone3/Tier3/Trork/Coast/Tower/Encounter_Coast_Tower_001.prefab.json` |
| Feran | `Portals_Oasis` | 9 | `Server/Prefabs/Npc/Feran/Portals_Oasis/Chieftain/Feran_Tier2_Chieftain_001.prefab.json` |
| Feran | `Zone2_Feran_Tier1` | 7 | `Server/Prefabs/Npc/Feran/Tier1/Corners/Feran_Tier1_Corners_001.prefab.json` |
| Feran | `Zone2_Feran_Tier2` | 8 | `Server/Prefabs/Npc/Feran/Tier1/Straight/Normal/Feran_Tier1_Straight_Normal_002.prefab.json` |
| Feran | `Zone2_Encounters_Tier3` | 1 | `Server/Prefabs/Npc/Feran/Tier3/Chieftain/Feran_Tier3_Chieftain_001.prefab.json` |
| Kweebec | `Zone1_Kweebec_Tier3` | 15 | `Server/Prefabs/Npc/Kweebec/Autumn/Shops/Kweebec_Autumn_Shops_001.prefab.json` |
| Kweebec | `Zone1_Kweebec_Tier2` | 1 | `Server/Prefabs/Npc/Kweebec/Azure/Kweebec_Azure_KweebecCamps_Azure_001.prefab.json` |
| Kweebec | `Zone1_Kweebec_Tier1` | 30 | `Server/Prefabs/Npc/Kweebec/Oak/Houses_Guard/Kweebec_Oak_Houses_Guard_001.prefab.json` |
| Outlander | `Zone3_Outlander_Tier1` | 34 | `Server/Prefabs/Npc/Outlander/Boats/Large/Outlander_Boats_Large_001.prefab.json` |
| Outlander | `Zone3_Encounters_Tier1` | 2 | `Server/Prefabs/Npc/Outlander/Houses/Tier0/Outlander_Houses_Tier0_002.prefab.json` |
| Outlander | `Zone3_Encounters_Tier2` | 1 | `Server/Prefabs/Npc/Outlander/Misc/Outlander_Misc_004.prefab.json` |
| Trork | `Zone1_Trork_Tier2` | 8 | `Server/Prefabs/Npc/Trork/Tier_2/Encampment/Lumber/Forest_Aspen/Trork_Tier_2_Encampment_Lumber_Forest_Aspen_001.prefab.json` |
| Goblin | `Zone1_Encounters_Tier1` | 1 | `Server/Prefabs/Spawn/Room_Goblin/Spawn_Room_Goblin_001.prefab.json` |
| Goblin | `Zone1_Encounters_Tier2` | 1 | `Server/Prefabs/Spawn/Room_Goblin/Spawn_Room_Goblin_002.prefab.json` |

## Native tables containing chest/ItemContainer entries

| Exact table asset | Selected container block(s) |
|---|---|
| `Server/Item/Block/Spawners/New/Portals_Oasis.json` | `Furniture_Feran_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Encounters_Tier1.json` | `Furniture_Crude_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Encounters_Tier2.json` | `Furniture_Village_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Encounters_Tier3.json` | `Furniture_Dungeon_Chest_Epic` |
| `Server/Item/Block/Spawners/New/Zone1_Encounters_Tier4.json` | `Furniture_Temple_Emerald_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Goblin_Tier1.json` | `Furniture_Goblin_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Goblin_Tier2.json` | `Furniture_Goblin_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Goblin_Tier3.json` | `Furniture_Goblin_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Kweebec_Tier1.json` | `Furniture_Kweebec_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Kweebec_Tier2.json` | `Furniture_Kweebec_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Kweebec_Tier3.json` | `Furniture_Kweebec_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Trork_Tier1.json` | `Furniture_Lumberjack_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Trork_Tier2.json` | `Furniture_Lumberjack_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Trork_Tier3.json` | `Furniture_Lumberjack_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Undead_Tier1.json` | `Furniture_Ancient_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Undead_Tier2.json` | `Furniture_Ancient_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone1_Undead_Tier3.json` | `Furniture_Ancient_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone2_Encounters_Tier1.json` | `Furniture_Crude_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone2_Encounters_Tier2.json` | `Furniture_Village_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone2_Encounters_Tier3.json` | `Furniture_Dungeon_Chest_Epic` |
| `Server/Item/Block/Spawners/New/Zone2_Encounters_Tier4.json` | `Furniture_Temple_Wind_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone2_Feran_Tier1.json` | `Furniture_Feran_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone2_Feran_Tier2.json` | `Furniture_Feran_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone2_Undead_Tier1.json` | `Furniture_Ancient_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone2_Undead_Tier2.json` | `Furniture_Ancient_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone2_Undead_Tier3.json` | `Furniture_Ancient_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone3_Encounters_Tier1.json` | `Furniture_Crude_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone3_Encounters_Tier2.json` | `Furniture_Village_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone3_Encounters_Tier3.json` | `Furniture_Dungeon_Chest_Epic` |
| `Server/Item/Block/Spawners/New/Zone3_Encounters_Tier4.json` | `Furniture_Temple_Dark_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone3_Kweebec_Tier1.json` | `Furniture_Kweebec_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone3_Kweebec_Tier2.json` | `Furniture_Kweebec_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone3_Kweebec_Tier3.json` | `Furniture_Kweebec_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone3_Outlander_Tier1.json` | `Furniture_Human_Ruins_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone3_Outlander_Tier2.json` | `Furniture_Human_Ruins_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone3_Outlander_Tier3.json` | `Furniture_Temple_Dark_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone3_Undead_Tier1.json` | `Furniture_Ancient_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone3_Undead_Tier2.json` | `Furniture_Ancient_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone3_Undead_Tier3.json` | `Furniture_Ancient_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone4_Encounters_Tier1.json` | `Furniture_Crude_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone4_Encounters_Tier2.json` | `Furniture_Village_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone4_Encounters_Tier3.json` | `Furniture_Dungeon_Chest_Epic` |
| `Server/Item/Block/Spawners/New/Zone4_Encounters_Tier4.json` | `Furniture_Human_Ruins_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone4_Undead_Tier1.json` | `Furniture_Ancient_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone4_Undead_Tier2.json` | `Furniture_Ancient_Chest_Small` |
| `Server/Item/Block/Spawners/New/Zone4_Undead_Tier3.json` | `Furniture_Ancient_Chest_Small` |

## Native NPCGroup definitions inspected

| Exact native asset | IncludeRoles | IncludeGroups | Exclusions |
|---|---|---|---|
| `Server/NPC/Groups/Intelligent/Aggressive/Goblin/Edible_Goblin_Scrapper.json` | ['Edible_Goblin_Scrapper'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Aggressive/Goblin/Goblin.json` | ['*Goblin_*'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Aggressive/Goblin/Goblin_Lobber.json` | ['Goblin_Lobber'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Aggressive/Goblin/Goblin_No_Bosses.json` | ['Goblin_*'] | [] | ['Goblin_Duke*', 'Goblin_Ogre'] |
| `Server/NPC/Groups/Intelligent/Aggressive/Goblin/Goblin_Scrapper.json` | ['Goblin_Scrapper'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Aggressive/Outlander/Outlander.json` | ['*Outlander*'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Aggressive/Outlander/Outlander_Marauder.json` | ['Outlander_Marauder'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Aggressive/Outlander/Outlander_Spar.json` | ['Outlander_Berserker'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Aggressive/Outlander/Outlander_Underlings.json` | [] | ['Outlander'] | ['Outlander_Marauder'] |
| `Server/NPC/Groups/Intelligent/Aggressive/Trork/Trork.json` | ['*Trork*'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Aggressive/Trork/Trork_Chieftain.json` | ['Trork_Chieftain'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Aggressive/Trork/Trork_Spar.json` | ['Trork_Warrior'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Aggressive/Trork/Trork_Underlings.json` | [] | ['Trork'] | ['Trork_Chieftain'] |
| `Server/NPC/Groups/Intelligent/Aggressive/Trork/Trork_Warrior.json` | ['Trork_Warrior'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Neutral/Feran/Feran.json` | ['Feran*'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Neutral/Feran/Feran_Civilian.json` | ['Feran_Civilian'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Neutral/Kweebec/Kweebec.json` | ['Kweebec*', 'Template_Kweebec*'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Neutral/Kweebec/Kweebec_Civilian.json` | ['Kweebec_Sapling*', 'Template_Kweebec_Sapling*'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Neutral/Kweebec/Kweebec_Pets.json` | ['Rabbit', 'Bunny'] | [] | [] |
| `Server/NPC/Groups/Intelligent/Neutral/Kweebec/Kweebec_Prisoner.json` | ['Kweebec_Prisoner'] | [] | [] |

## Direct chest prefab examples

- `Server/Prefabs/Goblin_Thief_Chest.prefab.json`: direct Furniture_Goblin_Chest_Small with ItemContainerBlock holder.
- `Server/Prefabs/Blocksets/Blocksets_08.prefab.json`: chest examples in a blockset showcase; not a settlement ownership declaration.

Local full derived data and bytecode excerpts: `DevTesting/Production_Ownership_Reference_0_6_8`. No native implementation files are committed.
