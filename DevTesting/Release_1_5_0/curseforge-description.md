# HyThief 1.5.0 — The Creator Update

**HyThief provides the mechanics. You create the opportunities.**

For Hytale 0.7.2. A creator-, developer- and world-builder-focused update for mod developers, world builders, adventure-map creators, dungeon designers, server administrators, roleplay creators and stealth/RPG experience designers.

Configure per-container ownership and supported NPC participation to build towns, guarded storage and stealth encounters using Hytale's native systems.

## Creator Tools — Commands & Setup

- `/give HyThief_Ownership_Editor_Tool` — equips creators with the container ownership tool. Editing requires `hythief.ownership.edit`.
- `/give HyThief_NPC_Editor_Tool` — equips creators with the NPC participation tool. Editing requires `hythief.npc.edit`.
- `/hythief ownership` — aim at storage to open the ownership editor without a tool; requires `hythief.ownership.edit`.

Native /give requires `hytale.command.give.self`. Use an authorized admin/operator or grant the permissions to trusted creators. Item possession alone is not editing permission.

Equip a tool in the hotbar, aim within 8 blocks and use Primary. Apply saves; Cancel/Escape discards changes. [Complete creator setup](https://github.com/FalkFirebeard/HyThief/blob/v1.5.0/Docs/CREATOR_GUIDE.md).

## Container ownership

Choose Unowned, Nearby or supported Faction ownership for an individual compatible container. Inventories remain intact; ownership saves natively. Nearby allows supported visual witnesses; Faction additionally checks membership. Native chests and supported optional HyStash wardrobes are verified. Unmarked/player-placed containers remain unowned until configured; no existing-world backfill.

## NPC participation

Pickpocket, Witness and Crime Response each have Default/Enabled/Disabled. Per-field precedence: individual NPC → role/type → recognized faction → existing defaults. Individual settings follow saved NPCs; broad rules are world-local. Enabled cannot create missing loot or compatible AI. Supported faction scopes are bounded, not universal dynamic discovery.

## Existing gameplay remains

Crouch behind supported NPCs to pickpocket; normal merchant trade remains. Owned-container withdrawals use visual witnesses, not a random container catch roll. Caught withdrawals keep the item, show one notification and close the window. Recognized native pursuit blocks owned storage; unowned storage stays accessible and access restores after escape. Native perception remains 15/4, container witnesses 20/6.

HyThief configures ownership and theft participation. Hytale/the originating mod retains NPC identities, factions, movement, patrols and AI. No scripting, enemy spawning, automatic hostility editor or universal faction-wide alarm is supplied. Disabling HyThief response does not suppress unrelated native hostility/social reactions. Custom AI states and non-native storage may be unsupported.

HyStash is optional; no HyPick/HyRep/third bridge dependency. Native JSON/binary NPC prefab export/import and paste were tested for individual settings; world rules do not travel with the NPC, and all transfer paths are not claimed supported. Existing saved ownership remains compatible.

Screenshots are optional and may be added separately. No demonstration video is required.

Developed by Falk Interactive Studios.
