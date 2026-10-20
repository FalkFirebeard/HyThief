# 01 HyThief visual identification update

2026-10-20. Test-fixture decoration only; HyThief 1.5.0-editor.1 and HyStash 1.4.0 unchanged.

| Target | Test case | Actual native wool block |
| --- | --- | --- |
| A / GREEN | Unowned chest | Cloth_Block_Wool_Green |
| B / BLUE | Kweebec faction chest | Cloth_Block_Wool_Blue |
| C / YELLOW | Nearby chest | Cloth_Block_Wool_Yellow |
| D / ORANGE | Unowned wardrobe | Cloth_Block_Wool_Orange |
| E / RED | Kweebec faction wardrobe | Cloth_Block_Wool_Red |

No substitutions. Labels identify assigned test cases; editor-modified ownership is preserved, not reset to the label's original fixture purpose.

Each target gets matching three-block floor strips on both approaches: x = container x +/-1, y=63, z=0 and z=5. Matching overhead wool cube at container x,y=69,z=2; existing overhead label moved above it to y=70.3. Matching native floating text labels at y=66.5,z=-0.5 and z=5.5. Every label includes letter, color and ownership test case. The legend maps all five colors. All decorative blocks preserve normal walking headroom and witness sight lines. Spawn coordinates, orientation and world name remain unchanged; visible front strips and overhead identifiers address approaching the containers from their backs without rotating containers or moving the player.

Both active and clean-master saves were fully backed up in `DevTesting/Ownership_Editor_Phase2A/ColorCoding/Before`, including copies of workspace/project AGENTS.md. Native painting ran only in disposable copies. A test-only native IndexedStorageFile/BSON merge carried over only decorative Block fields/derived heightmap data; changed original label holders and added ten matching label holders. Original native block-entity component holders (all inventories, ownership, other storage metadata) and all original non-label entities/NPCs were retained directly from each respective original save. The active world was never reset/recreated or used as a running native test server.

Native reload verification compared 131,072 affected-section cells per save: only the 35 planned marker locations differ; every other block ID, all block rotations and all multiblock fillers match the pre-change original. Each reload contains 15 target labels and all five original containers. Original saved NPC holders and storage holders are BSON-identical before/after the merge. Only two region files changed per actual save; configs, player data, permissions, spawn, mod packages and all other files are unchanged. The fixture authoring helper now reproduces the same decoration without any production source changes.

The master checksum pin is updated. Desktop shortcut/launcher/scripts and candidate pins remain unchanged. Reset verification must use disposable APPDATA and the exact referenced script, not the active world. Evidence: `color-VerifyActive-result.json`, `color-VerifyMaster-result.json`, `color-phase2a-result.json` and the color verification result in this folder.

The standing rule was appended to `E:\Hytale adjustments\AGENTS.md`: unique target colors visible front/rear, overhead markers where useful, short labels, spawn visibility, documented mappings, no letters/identical markers alone, and `[Two-digit ID] [Mod Name]` naming with next available IDs. It applies across future Falk Interactive Hytale fixtures. No existing world was renamed.

Client gate: inspect actual label readability and colors after opening 01 HyThief. No new gameplay feature, production build or public release.
