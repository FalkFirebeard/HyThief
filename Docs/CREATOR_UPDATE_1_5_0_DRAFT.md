Status: superseded by the prepared [1.5.0 release notes](RELEASE_1.5.0.md), [Creator Guide](CREATOR_GUIDE.md) and release-verification gate. Falk reported manual passes. Publication still requires final approval. Historical draft follows.

# HyThief 1.5.0 â€” The Creator Update (unpublished draft)

**HyThief provides the mechanics. You create the opportunities.**

## Proposed release notes / changelog

- Native in-game Container Ownership Editor and inventory tool.
- NPC participation editor/tool with independent Pickpocket, Witness and Crime Response settings.
- Individual settings plus supported world-local role/faction overrides; narrower scopes take precedence.
- Preserve native storage, role-specific loot, merchant trade and existing caught/pursuit behavior.
- Optional supported HyStash storage; neither mod requires the other.

Creator tools serve adventure maps, world-building, PvE and roleplay servers. Individual NPC settings passed native JSON/binary prefab serialization and paste. World-wide rules stay in their world. Do not advertise every prefab/clipboard workflow as verified.

## Creator instructions

`/give HyThief_Ownership_Editor_Tool`; permission `hythief.ownership.edit`. Aim at compatible storage and Primary. Existing `/hythief ownership` remains available.

`/give HyThief_NPC_Editor_Tool`; permission `hythief.npc.edit`. Aim at a live NPC within 8 blocks and Primary. Choose scope and values, then Apply; Cancel/Escape does not save. Editors do not change faction membership, native AI or inventories.

## CurseForge description draft

HyThief 1.5.0 â€” The Creator Update expands stealth pickpocketing and witnessed container theft with native in-game creator tools. Configure placed container ownership and supported NPC participation to create opportunities for your maps and servers. Existing theft, visual witnesses, native NPC responses, forced closure when caught and pursuit restrictions remain. HyThief works independently; HyStash is optional compatible storage.

Do not publish until Falk approves testing and final scope. No claim of universal third-party AI/storage support or automatic faction discovery in this candidate.

## Available category review â€” no live change

CurseForge public Hytale Mods filters expose Gameplay, Utility, Quality of Life, Mobs/Characters, Prefab and World Gen. Proposed closest fit: Gameplay + Utility. Describe creator/server/world-building use in prose. Prefab/World Gen should not imply shipped prefab packs or world generation. No dedicated Server Utilities/Creator Tools category was visible; author-side choices/limits still require review at publication. [Category list](https://www.curseforge.com/hytale/search?class=mods&page=1&pageSize=20&sortBy=relevancy).

## Release gates

Development candidate editor.3 passed native proofs but is not installed. Final public 1.5.0 artifact/tag/release is intentionally not created. Remaining: installation and active-world pin approval; Falk's real-client UI/gameplay checks; review of metadata limitations/final scope; final artifact/hash verification; separate publication approval. Do not imply HyPick, HyRep or HyStash is required.
