# HyThief 1.5.0 — The Creator Update

**HyThief provides the mechanics. You create the opportunities.**

For Hytale **0.7.2** (exact target `=0.7.2`).

A creator-, developer- and world-builder-focused update: configure theft opportunities for adventure maps, dungeons, towns, guarded storage, roleplay servers, and stealth/RPG encounters. Built for mod developers, world builders, adventure-map creators, dungeon designers and server administrators.

## Creator Tools — Commands & Setup

- `/give HyThief_Ownership_Editor_Tool` — equip, target compatible storage and use Primary. Requires `hythief.ownership.edit` to edit.
- `/give HyThief_NPC_Editor_Tool` — equip, target a live NPC and use Primary. Requires `hythief.npc.edit` to edit.
- `/hythief ownership` — open the same container editor directly while aiming at storage; requires `hythief.ownership.edit`.

Native /give additionally requires `hytale.command.give.self`. Use a suitably authorized admin/operator account or delegate permissions to trusted creators. Item possession alone is not authorization. Apply saves; Cancel/Escape discards pending changes.

## New creator controls

- Set per-container ownership: Unowned, Nearby or supported Faction.
- Configure NPC Pickpocket, Witness and Crime Response participation independently.
- Default/Enabled/Disabled values resolve individual NPC → exact role → recognized faction → existing defaults.
- Saved individual NPC overrides and world-local role/faction rules persist natively.
- Keep native chests and supported optional HyStash wardrobes using their original storage and inventory.

Existing pickpocket loot/risk, merchant trade, container witness rules, caught notification, window closure and pursuit lockout remain. HyThief configures ownership and theft participation; Hytale/the originating mod retains NPC identity, factions, movement and AI. It does not add patrol editing, scripting, enemy spawning, automatic hostility editing or a universal faction-wide alarm.

## Compatibility notes

No HyStash, HyPick or HyRep dependency. Supported native storage only; universal third-party NPC/storage compatibility is not claimed. Enabled cannot manufacture missing loot or response capabilities. Recognized faction menus remain bounded; dynamic metadata refactoring is deferred. Response Disabled does not suppress unrelated native hostility or social behavior. Existing custom-pursuit limitations remain.

Individual NPC settings passed native JSON/binary prefab serialization and paste; world-wide rules are not exported inside the NPC prefab. Other transfer paths remain unverified. Existing saved container ownership format is unchanged; unmarked containers are not retroactively assigned ownership.

Falk reported manual passes for both editors, NPC participation, precedence, persistence and guard restoration. Production native regression results are recorded separately from those user-reported tests.

See [Creator Guide](CREATOR_GUIDE.md) and [release verification](RELEASE_1_5_0_VERIFICATION.md). Back up saves and replace older active HyThief JARs with HyThief-1.5.0.jar after release. No demonstration video or screenshots are required; optional screenshot captions are prepared in the guide.
