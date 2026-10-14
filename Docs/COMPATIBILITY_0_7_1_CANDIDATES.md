# Hytale 0.7.1 metadata-only candidates — 2026-10-13

Status: **ready for Firebeard client smoke test; not published**.

Exact target `=0.7.1`; installed stable server revision `69c03ffa58d7e2f46bce8b0356ed206cfc7f10e6`, SHA-256 `a60fa0b31f1418691f8d8f7cc1159693e3ec504d4f0567fb91493a842d71f8ca`.

## Candidates

- **Public: 1.3.2**, target `=0.7.1`.
  Artifact: `E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\Update_0_7_1\Candidates\Public\HyThief-1.3.2.jar`
  SHA-256: `38c3d5e464c26c1bbca01a4d57424d349b5bb45dca3b502b1937ac015bd16938`

- **Development: 1.3.1-container.11**, target `=0.7.1`.
  Artifact: `E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\Update_0_7_1\Candidates\Development\HyThief-1.3.1-container.11.jar`
  SHA-256: `f2dca782e22c38839640e8886f764b16ca00c36cdf8b4c812d6c099181eae08c`

- **Wardrobes: 1.2.2**, target `=0.7.1`.
  Artifact: `E:\Hytale adjustments\Mod Projects\WonderousWardrobes\Builds\Candidates\1.2.2-0.7.1\WonderousWardrobes_v1.2.2.zip`
  SHA-256: `6a766e4f9027a345749c245dc15696c2f7f9405ee27b9a8a854471959e603c49`

## Changes and validation

Public HyThief 1.3.2 preserves public 1.3.1. Only version/target/build pin and candidate documentation changed. No container ownership/theft or experimental fixtures are packaged. Development remains 1.3.1-container.11, with its target/build pin updated only. Fixture manifest target and checksum changed; all saved geometry, NPCs, ownership, inventories and other master files are byte-identical. The general-world verification tool now points to the current candidate/server.

Both HyThief clean builds passed on JDK 25 / Gradle 9.1.0. Final archives differ from their baselines **only in manifest.json**; Java classes and every non-manifest asset are byte-identical. The public build checkout's Goblin inputs retain released LF bytes to avoid Git checkout CRLF packaging differences. No tracked asset/content changes. Existing inventory/Gradle deprecation warnings remain; no compatibility repair was needed.

Public native validation: 185 catalog entries; 174 concrete spawns, 2 abstract validation entries, 9 historical retired IDs correctly omitted. Actual role, interaction trees, assets and plugin load passed. Exact offender delivery via Kweebec_All_Danger caused native Razorleaf Attack.Default. General world: 16 NPCs / 16 bays passed, unchanged master.

Development native validation: Kweebec FACTION and Feran FACTION each selected one matching responder; LOCAL with both witnesses, Kweebec only, and Feran only selected both responders. Persistent ownership and real container withdrawals, unowned control, exactly one caught notification, native pursuit/LastSeen, 20/6 container range, 15/4 normal range and 30-block LOCAL delivery passed. Four color-coded stations, mirrored witnesses/responders, LOS/FOV/elevation and current green pad distances 7.566372975210778 blocks each passed. No geometry changes.

Wardrobes 1.2.2 was packaged from the exact released 1.2.1 archive, changing only manifest version/target. Archive integrity and JSON parsing passed; all non-manifest entries are byte-identical, including visuals, 220 prefab overrides and loot mappings. Exact candidate native validation passed all 16 variants, 54 slots, open/close native state, transfer mutation, restart persistence and area unload/reload persistence. No Java compilation applies to this asset-only pack.

## Installation and rollback

The development candidate was installed and hash/identity verified separately, then preserved outside Mods. Final active Mods contains exactly one Local:HyThief (public 1.3.2) and one FalkFirebeard:Wonderous Wardrobes (1.2.2). Unrelated mods unchanged.

Installed: `C:\Users\askin\AppData\Roaming\Hytale\UserData\Mods\HyThief-1.3.2.jar` and `C:\Users\askin\AppData\Roaming\Hytale\UserData\Mods\WonderousWardrobes_v1.2.2.zip`.

Previous installed files: `E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\Update_0_7_1\Candidates\Rollback\Installed`. Full container.11 source archive, previous development JAR, master and dirty active save rollback: `E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\Update_0_7_1\Candidates\Rollback`. Public 1.3.1, Wardrobes 1.2.1 and v1.2 release references unchanged. Active container fixture changed only ServerVersion; staging reset copy passed all 33 checksums; reset scripts unchanged. No world reset/redesign occurred.

**Do not load the Container Theft Test World with the public candidate active**: that world requires the separately preserved development candidate and its ownership components.

Wardrobes is not a Git repository and has no remote configured. Its manifest, release note, exact candidate archive and rollback are saved locally; no Wardrobes commit or remote sync can be claimed. HyThief public/development Git branches are committed and pushed separately; no release tag/publication is created.

## Client smoke test

HyThief: normal front trade; toggle crouch and approach rear; open Pickpocket; steal an item; deliberately get caught; confirm notification, UI closes and nearby Razorleaf reacts.

Wardrobes: open; transfer a recognizable item; close/reopen; leave and reload world; confirm item persists. Offline/native checks do not replace this connected-client check. Stop for these results; no further gameplay work.

## Draft release notes

HyThief 1.3.2: updated exact compatibility target to Hytale 0.7.1; existing gameplay unchanged; compatibility validated against the current update.

Wonderous Wardrobes 1.2.2: updated exact compatibility target to Hytale 0.7.1; existing wardrobe/storage behavior unchanged; compatibility validated against the current update.
