# HyThief 1.3.1 — public release verified

## Published artifacts

- Public version **1.3.1**, manifest target **=0.7.0**.
- Public source commit **2ff675917e045d0d805e6b760411f5fdd9c0b5c2**, annotated tag **v1.3.1**, branch **compatibility/1.3.1**, based on public **v1.3.0**.
- GitHub: https://github.com/FalkFirebeard/HyThief/releases/tag/v1.3.1 — live Release, not draft/prerelease. Assets **HyThief-1.3.1.jar** and **SHA256SUMS.txt** downloaded and verified against local files.
- CurseForge: https://www.curseforge.com/hytale/mods/hythief/files/9152674 — **Approved**, published as Release; existing project **1496959**. CurseForge offers the coarse game-version category **0.7**; manifest/title/changelog explicitly specify **0.7.0**.
- SHA256 **bf398019bb07e97392414cce9ef62a4fc9c95bab29b097c7662ef82a881a9f94** matches final clean build, tested candidate, local installed JAR, downloaded GitHub JAR and downloaded CurseForge JAR.
- Local deliverables: **.build/public-release/1.3.1/**.
- Exactly one **Local:HyThief** JAR remains installed: **C:/Users/askin/AppData/Roaming/Hytale/UserData/Mods/HyThief-1.3.1.jar**. It already matches the final artifact exactly; no redundant replacement needed.

## Included public behavior and compatibility scope

Existing crouched rear NPC Pickpocket, individual item theft, role-specific loot, per-item/escalating risk, cooldowns, caught notification, menu closure, native NPC/guard response and normal front merchant trade are retained. Public Java/config/loot source has no changes from v1.3.0. Redesigned Goblin role trees were refreshed from native 0.7.0; nine removed native Goblin overrides are omitted while historical resolver entries remain inactive. No global perception/balance change.

**No experimental container-theft, ownership, NEARBY/FACTION container policy, LOCAL alert, persistent ownership components, property/PREFAB implementation or test-world assets/classes are in the release JAR.** Public package has 1,665 entries, valid JSON/ZIP CRC, no duplicate role IDs and no development classes/assets. Build pin uses installed Hytale 0.7.0 revision **648ebf375a034beabf119d7d790cd11d4f665ff4**.

## Validation

Final clean build PASS with Java 25 and existing workspace Gradle 9.1.0; same two inventory removal warnings and existing Gradle deprecation notice. Wrapper distribution download was canceled; successful final build used installed shared Gradle with cached dependencies. Final JAR is byte-identical to the tested candidate, so another client test was not required.

Native full support validation: **174 concrete + 2 abstract** current roles, nine historical IDs inactive. Actual public caught adapter queued exact offender via **Kweebec_All_Danger** and native Razorleaf entered **Attack.Default**. Final native saved-fixture reload validated 16 original NPC UUIDs/roles, interaction trees and all 16 bay floors/headroom/walls. No obsolete-role validation failures.

Firebeard explicitly confirmed **Bay 1 core crouched/rear pickpocket and stealing PASS**, **Bay 16 caught flow and Razorleaf response PASS** on 0.7.0. Other fixtures passed native validation. No claim every encounter or multiplayer/PvP scenario was manually tested; no dedicated PvP system was added or altered.

## Public pages

CurseForge main description was saved and verified live with 1.3.1/0.7.0, current NPC pickpocket features, refreshed role support and general future-development wording. It does not advertise unfinished mechanics. GitHub release notes, public-branch README, main README and repository description are consistent and current. Installation filename is now **HyThief-1.3.1.jar**, replacing the stale 1.3.0 instruction. Historical changelog entries retain their original version context.

## Development preservation

Main retains **1.3.1-container.11** development code and build labeling. Original pre-update checkpoint **53e7897** is preserved by remote tag **checkpoint/container11-before-0.7.0**, full local Git bundle, source history and JAR backups. Updated development compatibility commit **3222f8b** remains in main; world refresh **e8d768d** is retained. No development merge into the public branch/tag occurred.

Previous installed development JAR remains at **DevTesting/Update_0_7_0/InstalledRollback/HyThief-1.3.1-container.11.jar** (SHA256 **08ddb2033d942c901a52de3ef20429e83f21db7212bf676e5287f05b0123c233**). Container worlds, ownership masters, reusable prefab, archived releases and unrelated projects/mods are unchanged by publication. No live-world reset performed for release publication.
