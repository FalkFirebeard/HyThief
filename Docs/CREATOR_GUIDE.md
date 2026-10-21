# HyThief 1.5.0 — Creator Tools — Commands & Setup

**HyThief provides the mechanics. You create the opportunities.**

The Creator Update is for Hytale mod developers, world builders, adventure-map creators, dungeon designers, server administrators, roleplay server creators, and stealth/RPG experience designers. Combine container ownership and NPC participation with Hytale's own world-building tools to author theft opportunities and consequences.

## Commands and permissions

| Working command | Purpose | Required permission |
|---|---|---|
| `/give HyThief_Ownership_Editor_Tool` | Obtain the container paintbrush-style tool | Native `hytale.command.give.self` to obtain it; `hythief.ownership.edit` to use it |
| `/give HyThief_NPC_Editor_Tool` | Obtain the NPC paintbrush-style tool | Native `hytale.command.give.self` to obtain it; `hythief.npc.edit` to use it |
| `/hythief ownership` | Open the container editor directly while looking at storage | `hythief.ownership.edit` |

Run these in-game as a player. Server owners should grant the relevant permissions to trusted creators or use an administrator/operator account with effective grants. Operator status is not a separate HyThief requirement: properly delegated permissions suffice. Merely possessing a tool grants no editing authority; permission is rechecked when applying. Creative mode alone does not replace HyThief's permission checks. Native /give has its own permission policy.

Both tools are registered in native `Tool.PrefabEditing` / `PrefabTools` item categories; the native catalog's presentation/access depends on Hytale. The commands above are the verified acquisition route. No crafting recipe, custom hotkey, NPC-editor chat command, or bulk editor command is supplied. `/hythief ownership` takes no extra arguments. The production plugin registers only this HyThief chat-command tree; development diagnostic commands are not packaged.

## Container Ownership Editor

Equip the Ownership Editor Tool in the hotbar, look at compatible storage within 8 blocks and use Primary (the game's normal primary-use control). Alternatively, aim at the container and run `/hythief ownership`.

- Owned off: this placed container is unowned under HyThief.
- Owned on + Nearby: any supported eligible visual witness may confirm a withdrawal; existing LOCAL response applies.
- Owned on + Faction: the witness must belong to the chosen supported native group; existing supported faction response applies.
- Apply saves this container's ownership. Cancel or Escape discards pending changes.

The current faction menu offers registered Kweebec, Feran, Trork, Outlander, Skeleton and Zombie groups. Faction membership does not guarantee every role has a supported reaction. Existing native alarm/combat behavior stays role-dependent; this is not a universal faction-wide alert system.

Native chests and supported HyStash 1.4.0 wardrobes are verified, including upper/lower multiblock targeting. Other storage must expose a compatible native block entity, ItemContainerBlock/ItemContainer and native window/transaction path; universal third-party storage is not promised. Ownership is per container, not per block type. Player-placed/unmarked storage stays unowned unless configured. HyStash remains optional.

Existing inventories, slot layouts and storage implementations are preserved. Ownership uses the existing HyThiefOwnershipProof data and native save/reload. Browsing, depositing and internal rearrangement remain safe; a successful withdrawal can be theft. Existing visual checks, caught notification, native response, forced closure and pursuit restrictions remain. Witness range stays 20 blocks standing / 6 crouched. No retroactive migration of unmarked containers is added.

## NPC Configuration Editor

Equip the NPC Editor Tool, aim at a live NPC within 8 blocks with clear LOS, and use Primary. The editor does not attack, trade, pickpocket or force the NPC to face you.

Three independent settings:

- Pickpocket Eligibility: allow or prevent the supported pickpocket path.
- Witness Eligibility: allow or prevent the NPC establishing a HyThief theft observation/catch.
- Crime Response Eligibility: allow or prevent direct supported HyThief crime-response handoff to that NPC.

Each setting has Default, Enabled and Disabled. Default inherits broader configuration. Enabled may override a broader Disabled setting on a supported path; it cannot invent missing loot, interaction assets or compatible AI. Disabled excludes that participation. Restore all values to Default to return to inherited/default behavior.

Choose scope deliberately:

1. Individual NPC: affects only this saved NPC.
2. Role: affects the exact native role ID throughout this world, subject to individual overrides.
3. Faction: affects a currently recognized native group throughout this world, subject to individual and role overrides.

Resolution is per field: **individual NPC → exact role/type → recognized faction → existing defaults**. Unchanged fields inherit independently. Scope is not a faction-membership editor. The current recognized NPC faction resolver covers Kweebec, Feran, Goblin, Trork, Outlander, Skeleton and Zombie; unresolved factions have no faction scope. Goblin recognition does not grant unsupported container-witness/response capabilities. This is not universal dynamic faction discovery.

Apply commits the chosen scope; Cancel/Escape does not save. An invalid, obstructed, moved or replaced target, revoked permission or concurrent edit may require reopening the page. A role/faction edit is deliberately world-wide; use Individual for one encounter.

Individual settings persist with genuinely saved NPCs across reload/restart. A fresh replacement spawn inherits applicable world rules, not the previous entity's individual override. Role/faction rules remain world-local. Individual components passed native JSON and binary prefab export/import and paste; world rules do not travel inside an NPC prefab. Other spawn/clipboard/undo workflows are not universally verified.

## What remains native

HyThief does not change faction membership, patrol routes, movement/pathfinding, AI definitions, enemy spawning, arbitrary world triggers or scripting. Native Hytale/the originating mod still supplies those systems. Response Disabled is not global pacification: damage, ambient hostility, pre-existing pursuit and native social propagation may still cause reactions. Witness Disabled does not erase native awareness. Existing pickpocket loot, risks, trade and crouch gates remain.

Custom pursuit states/target slots, virtual storage and incompatible transaction models may need separate verification. Dynamic metadata research is documented separately and deferred; new mod registration alone does not establish compatible gameplay. HyPick, HyRep and HyStash are not dependencies.

## Optional screenshots — no video requirement

Screenshots may be added later; none block release approval. Do not use fabricated gameplay screenshots.

1. Ownership tool equipped in hotbar — caption: "Equip the Ownership Editor Tool and target compatible storage."
2. Container editor interface — caption: "Assign ownership to this container; keep its inventory intact."
3. NPC tool equipped in hotbar — caption: "Use Primary on an NPC to configure HyThief participation."
4. NPC configuration page — caption: "Control Pickpocket, Witness and Crime Response independently."

Place the first pair beside container instructions and the second pair beside NPC instructions in the public listing/release gallery when Falk supplies images.
