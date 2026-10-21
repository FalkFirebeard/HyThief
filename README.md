# HyThief 1.5.0 — The Creator Update

**HyThief provides the mechanics. You create the opportunities.**

Hytale **0.7.2**, exact target **=0.7.2**. Release materials are prepared locally and await final publication approval; public downloads are not updated by this preparation.

The Creator Update expands HyThief's existing pickpocketing and owned-container theft into a configurable stealth and theft system. For Hytale mod developers, world builders, adventure-map creators, dungeon designers, server administrators, roleplay server creators, and stealth/RPG experience designers.

Use in-game tools to author guarded storage, towns and stealth encounters alongside Hytale's native building/NPC systems. HyThief configures ownership and theft participation; it does not replace native AI.

## Creator Tools — Commands & Setup

| Command | What it does | Permission |
|---|---|---|
| `/give HyThief_Ownership_Editor_Tool` | Obtain the container editor tool | Native `hytale.command.give.self` to obtain; `hythief.ownership.edit` to edit |
| `/give HyThief_NPC_Editor_Tool` | Obtain the NPC editor tool | Native `hytale.command.give.self` to obtain; `hythief.npc.edit` to edit |
| `/hythief ownership` | Open container ownership editor directly while aiming at storage | `hythief.ownership.edit` |

Use a suitably authorized administrator/operator account, or delegate these permissions to trusted creators. HyThief checks permissions, not a separate operator flag; possessing a tool alone grants no editing authority. Native /give has its own permission policy.

Equip the tool in your hotbar, aim at compatible storage or a live NPC within **8 blocks**, and use **Primary**. Apply saves; Cancel/Escape discards pending changes. There is no custom hotkey or NPC-editor chat command. [Full creator guide](Docs/CREATOR_GUIDE.md).

## Container ownership

Configure this placed container as Unowned, Nearby-owned or supported Faction-owned. Nearby permits any supported visual witness; Faction requires matching native membership. Inventories and slot arrangements stay intact, and ownership persists through native saves.

Native chests and supported optional **HyStash 1.4.0** wardrobes are verified. Native storage capabilities determine compatibility; custom/virtual inventory systems may be unsupported. Existing unmarked/player-placed containers stay unowned until configured; no retroactive migration is added.

## NPC participation

Configure Pickpocket, Witness and Crime Response separately with Default/Enabled/Disabled. Resolve each field **individual NPC → exact role/type → recognized faction → existing defaults**. Individual settings follow saved NPCs; broad rules apply throughout this world. Replacement spawns inherit applicable role/faction rules, not another NPC's individual settings.

Enabled does not create missing loot, interaction wiring or AI. Current faction scopes remain bounded and are documented in the guide; universal dynamic faction compatibility is not claimed. Native identities/factions, movement, patrols, AI definitions, spawning and arbitrary scripting/world triggers are not edited. Response Disabled is not global pacification: native hostility and social behavior may still act independently.

Individual settings passed native JSON/binary prefab serialization and placement. World-wide rules do not travel inside an NPC prefab; every clipboard/spawn/undo path is not verified.

## Existing stealth and theft gameplay

- Crouch behind a supported NPC and use normal Interact: role-specific loot, escalating risk/caught behavior, clean menu closure and normal front merchant trade.
- Native NPC fresh-acquisition perception remains **15 standing / 4 crouched**. Crouching does not erase existing awareness. Toggle crouch avoids standing when releasing a held crouch key in the UI.
- Owned-container withdrawals require a valid visual witness: **20 standing / 6 crouched**, facing and unobstructed LOS. Browsing/deposits/internal rearrangement remain safe.
- Witnessed theft displays **"You have been caught stealing!"** once, completes the transfer, closes the container and rejects queued extra withdrawals.
- Owned containers are blocked during recognized native pursuit with **"You cannot access this container while being pursued."** Access restores naturally; unowned storage remains accessible. No heat timer.
- Existing native NPC roles handle supported alerts/pursuit/combat. No universal faction-wide alarm or automatic hostility editor is added.

Existing configured pickpocket support, container ownership profiles and loot/rules remain. [Container content coverage](Docs/CONTAINER_THEFT_1_4_0_CONTENT_AUDIT.md). Custom NPC state/target contracts and unsupported storage transaction models remain limitations.

## Optional integrations and installation

HyThief works independently. **HyStash is optional**; neither mod requires the other. HyPick and HyRep are not dependencies. [HyStash compatibility](Docs/HYSTASH_COMPATIBILITY.md).

After publication, obtain HyThief-1.5.0.jar from [GitHub releases](https://github.com/FalkFirebeard/HyThief/releases) or [CurseForge](https://www.curseforge.com/hytale/mods/hythief). Back up saves, replace older active HyThief JARs, and enable the mod for Hytale 0.7.2. Do not install multiple JARs with the same plugin identity.

## Verification and feedback

Falk reported successful editor.3 manual gameplay tests; these are recorded separately from the production native tests. Production classes/assets are identical to that candidate except manifest version. See [release verification](Docs/RELEASE_1_5_0_VERIFICATION.md) and [release notes](Docs/RELEASE_1.5.0.md).

[Metadata compatibility investigation](Docs/NATIVE_METADATA_COMPATIBILITY_REVIEW.md) documents automatic discovery feasibility and current restrictions. Broader refactoring is deferred; no universal third-party compatibility promise.

No video is required. Optional screenshot placements/captions are prepared in the creator guide; publication does not depend on images.

Report reproducible issues through [GitHub](https://github.com/FalkFirebeard/HyThief/issues) or [CurseForge](https://www.curseforge.com/hytale/mods/hythief/comments), with role/block IDs, game version and logs.

## Build

Use Java 25, the pinned Hytale 0.7.2 server dependency and `./build.ps1`. See [build instructions](Docs/BUILD_ENVIRONMENT_RELEASE.md). Release tags are created only after approval; development history and test fixtures remain preserved. No test worlds or helper plugins are shipped in the JAR.

Developed by **Falk Interactive Studios**. Part of the Firebeard Archives.
