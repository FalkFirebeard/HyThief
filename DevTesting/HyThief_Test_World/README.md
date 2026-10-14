# HyThief Test World — current general smoke fixture

Validated against installed Hytale **0.7.1**, revision **69c03ffa58d7e2f46bce8b0356ed206cfc7f10e6**, with public **HyThief 1.3.2** candidate. This remains the original cell-based world, separate from Container Theft Test World. No container mechanics were added.

## Launch and reset

Close Hytale completely, double-click Reset-Test-World.cmd, then launch **HyThief Test World**. Adventure spawn is **0.5 / 100 / 0.5**. No commands or manual fixture setup are required. Reset is HyThief-only; it calls the adjacent Reset-Test-World.ps1, checks master hashes/identity/processes, archives the dirty active world, and restores Master. It does not reset HyStash or container worlds.

Use toggle crouch so opening the menu does not make you stand accidentally. NPCs retain native AI and can turn/wander within the original open bays. Approach their actual rear; reset after caught/aggressive tests.

## Bays

NPC starting centers are X **0.5, 96.5, 192.5, 288.5** by column and Z **50.5, 146.5, 242.5, 338.5** by row. Y=100. Existing paths, walls and tally markers remain unchanged.

| Bay | Role | Purpose |
|---|---|---|
| 01 | Kweebec_Merchant | Solo rear Pickpocket/menu/steal/Escape/front trade |
| 02 | Kweebec_Sapling | Civilian |
| 03 | Kweebec_Razorleaf | Guard |
| 04 | Feran_Civilian | Feran civilian |
| 05 | Feran_Sharptooth | Feran guardian fresh crouched acquisition |
| 06 | Trork_Warrior | Hostile humanoid |
| 07 | Skeleton_Fighter | Standard skeleton |
| 08 | Skeleton_Frost_Fighter | Frost skeleton |
| 09 | Skeleton_Sand_Guard | Sand skeleton |
| 10 | Skeleton_Burnt_Soldier | Burnt skeleton |
| 11 | Skeleton_Incandescent_Fighter | Incandescent skeleton |
| 12 | Skeleton_Pirate_Striker | Pirate skeleton |
| 13 | Fox | Excluded wildlife; no Pickpocket |
| 14 | EMPTY — former Spectre_Void | Native role removed in 0.7.0; no substitute invented |
| 15 | Bear_Grizzly | Excluded wildlife; no Pickpocket |
| 16 | Kweebec_Merchant + Kweebec_Razorleaf | Caught notification/menu close/native guard pursuit; test last |

Shortest smoke: **01 → 04 → 05 → 06 → 07 → 13/15 → 16 last**. Bays 02/03/08–12 are available for broader coverage. Test front standing/crouched trade and rear crouched menu in Bay 01, successful theft and Escape, then fresh crouched approaches across families. Standing/front must not expose Pickpocket. Bay 16 tests caught/guard response; reset afterwards. This world has no Goblin bay, so the changed 0.7.0 Goblins require a separate existing normal-world encounter; no substitute/additional cell was authored.

## Preservation and validation

Original master and active world were backed up under **Archives/before-0.7.0-20261012-183703/** before changes. Native whole-save migration retired only the obsolete Spectre saved entity; all remaining **16 NPCs preserve their original UUIDs and roles**. All 16 bay floors/headroom/side walls and original NPC bay positions passed. Spawn/world UUID/native Adventure settings are preserved. All 14 supported NPCs have current interaction instruction trees; Fox/Bear remain controls.

Native load and clean saved-copy reload passed. Final reload has no nonexistent-role or asset-validation failures. Existing native animation/environment warnings are not disguised as mod failures. The master stays ticking, NPC AI enabled, natural spawning disabled, and time paused at midnight. Therefore this fixture does **not** prove normal day/night or sleep behavior.

The portable **HyThief_Test_Arena-1.0.0.zip** and prefab remain unchanged. Its old-version optional pack is disabled only in this save: all placed geometry/entities are native saved data and need no prefab repaste. Do not use the old prefab to reseed Bay 14 on 0.7.0 without reviewing its removed role. Legacy seeding Tools are historical; current saved-world validation uses WorldVerifier.java with section access and the Global spawn provider's current API.

Actual connected-client prompts, UI rendering, item transfer, crouch/no-turn and guard reaction remain **Firebeard runtime checks**, not claims from the offline verifier. The public gameplay JAR was not changed for this task.

Evidence: **.verification/Update_0_7_0/** (original/master/container hashes, native logs, exact NPC positions, candidate, reload). Reset restores this exact refreshed master; no other world/master or reusable prefab is changed.
Current repeatable verification: Tools/Verify-Current-World.ps1 compiles only a disposable development verifier and checks a fresh copy of Master. It never installs the verifier or rewrites Master. The legacy Prepare-World/Save-Master tools retain historical 0.6.8 seeding assumptions; do not use them to refresh the 0.7.0 fixture.
