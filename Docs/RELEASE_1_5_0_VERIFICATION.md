# HyThief 1.5.0 — final release verification

2026-10-20 (America/Denver). Final publication approval pending. No push/tag/upload, live-page change, installation or world reset.

## Baseline and manual evidence

Recovered clean a6897a6 on development/ownership-editor-phase2a. Installed editor.3 SHA-256: 89334a1850735a0f50f5caa4b3ab0f579a75447acd5db088aeacbd8f903328ac. Hytale remains 0.7.2 / d769dc3d57881261a62858e1394f0e4de9a21eca; pinned server SHA 23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff.

Falk reports manual passes for NPC tool acquisition/targeting/page, Apply/restoration, Pickpocket, Witness Enabled/Disabled, Response Disabled/Default, Kweebec guard restoration, save/reload, role/faction precedence and merchant interaction. The accepted container tool/native chest/HyStash ownership, persistence and inventory preservation also remain successful. These are user-reported tests, not newly executed automated client tests by Dex.

## Production identity and comparison

Version 1.5.0; exact target =0.7.2; identity Local:HyThief. Native Hytale:NPC dependency only; HyStash/HyPick/HyRep remain optional or independent, never required.

Artifact: `.build/public-release/1.5.0/HyThief-1.5.0.jar`, identical to `DevTesting/Release_1_5_0/Candidate/HyThief-1.5.0.jar`. SHA256SUMS.txt is staged alongside it.

SHA-256: `c57bfbcb81fe619fe0e6beb8bf96ccb1066b4273d8fbb7404d1122597e5c709e`.

Strict comparison: 1674 file entries; only manifest.json differs from verified editor.3, with Version 1.5.0-editor.3 -> 1.5.0. Every Java class, config and other asset is byte-identical. Both creator tools, pages, language resources, interactions and approved NPC codecs are present. No helper plugins, test worlds, fixture/research assets, diagnostic class or source files are shipped. ZIP metadata is not content. No public development suffix or third-party dependency.

Release source changes are build.gradle/manifest version plus documentation and disposable verification code. No Java/gameplay/loot/rule/AI/ownership-format changes. HyThiefOwnershipProof remains compatible; new NPC component/resource codecs match the manually verified candidate. Default behavior remains unchanged.

## Production automated tests

Clean Java25/Gradle9.1 build succeeded against the pinned current server. Gradle has no unit-test source; actual native proofs are recorded separately:

| Proof | Verified |
|---|---|
| FinalNpcClean | Tool/native ray, permissions/revocation, Apply/Cancel, duplicate protection, invalid/stale targets, individual independence, all three settings, per-field precedence, real sensor/witness/response filtering, unsupported safety, holder/unload/reload, fresh-spawn inheritance |
| FinalRestart | Full process restart of exact saved NPCs and individual/role/faction rules |
| FinalPrefab | JSON/binary export/import and native paste; individual data and independent UUIDs |
| FinalOwnershipClean | Accepted ownership tool, native chest/both wardrobe halves, preserved inventory, permissions, duplicate protection, Apply/Cancel |
| FinalOwnershipCommandClean | Actual `/give HyThief_Ownership_Editor_Tool` and `/hythief ownership` dispatch |
| FinalNpcCommandClean | Actual `/give HyThief_NPC_Editor_Tool` and `/hythief ownership` dispatch |
| FinalChest/FinalWardrobe | Attributed native transfers, retained item, one caught notice, closure/queued rejection, safe deposits/internal/unseen transfers, native guard Attack/pursuit, denied-access deduplication, unowned access and restoration |
| FinalAlone | HyThief without HyStash |
| FinalMatrix | 185 configured; 174 concrete spawns, 9 retired, 2 abstract; actual roles/interaction trees preserved |

Proofs use synthetic players/disposable worlds, not new connected-client sessions. Every encounter, arbitrary modded AI/storage and all undo/clipboard/spawn paths are not claimed tested. Final results/log audit are in DevTesting/Release_1_5_0/verification-summary.json and Run-*/native.log.

## Warning and defect classification

Existing nonblocking compiler notices: two Inventory removal warnings, ContainerTheftService deprecated API, Gradle10 future incompatibility. Native asset/parser/duplicate notices remain, including existing stealth-filter attributes/fixture role replacement. Native stderr emits allocation resize diagnostics at SEVERE, also present in baseline. Do not claim warning-free startup.

Initial proofs reproduced baseline synthetic-player UI teardown exceptions after prematurely removing Player, plus a command-test inventory event with an uninitialized synthetic game mode. Only the test harness was corrected: wait for queued UI updates before removal and initialize the fake command actor's normal game mode. No production workaround or gameplay change. Clean reruns must have no non-allocation SEVERE messages and normal shutdown; final summary records this gate. The artifact was not rebuilt.

## Compatibility and preservation

Native metadata investigation is complete. Registries can discover independent roles/groups, but overlapping NPC groups do not define one universal faction or crime-AI contract. Current recognized faction scopes/response adapters remain documented. Falk explicitly deferred broad metadata refactoring from 1.5.0; no defect in bounded promised behavior was found. Recommend a separately approved later update, not a release delay or universal compatibility claim.

Storage uses native ItemContainerBlock/ItemContainer/ContainerBlockWindow and transaction identity, not duplicated wardrobe implementation. HyStash1.4.0 is verified optional content. Custom/virtual windows, unsupported native pursuit states/slots and unknown persistence remain limitations. See CREATOR_GUIDE.md and NATIVE_METADATA_COMPATIBILITY_REVIEW.md.

Active 01 HyThief, its master and installed Mods are before/after hash-checked. No reset/script/pin/world/install changes are allowed in this preparation. editor.3 remains installed. Development branch stays at a6897a6; release changes are isolated on release/1.5.0-preparation. Rollback is switching to that preserved development branch; no runtime rollback is necessary. Previous releases/history remain intact. Future downgrade behavior after saving new NPC data is not claimed verified.

## Publication plan — explicit approval required

After approval: push the verified commit, create annotated v1.5.0, publish GitHub `HyThief 1.5.0 — The Creator Update`, attach the exact preserved JAR and SHA256SUMS.txt, download and verify SHA. Do not rebuild after approval unless necessary and separately reviewed.

CurseForge existing HyThief project 1496959: one upload of the identical JAR, Release, version1.5.0, Hytale0.7, normal automatic publication-after-approval flow. Use staged creator description/changelog, record file ID/status, avoid duplicate uploads while Processing and verify public hash once downloadable. Do not equate submission/processing with live.

Public Hytale categories confirmed: Gameplay, Utility, Quality of Life, Mobs/Characters, Prefab, World Gen. Recommend Gameplay + Utility, not implying bundled worldgen/prefab packs. No dedicated Creator Tools/Server Utilities tag visible; author-side availability still requires review when publication is approved. No listing change. Source: https://www.curseforge.com/hytale/search?class=mods&page=1&pageSize=20&sortBy=relevancy .

README, guide, version changelog/release notes and platform drafts prominently document both give commands, source-verified ownership command, effective permissions, usage/targets, Apply/Cancel, precedence, persistence and limitations. No new command was invented. Optional four screenshot placements/captions are ready; no video or screenshot is required. No scripting/spawning/hostility-editor/universal-alarm claims.

**HYTHIEF 1.5.0 — READY FOR PUBLICATION APPROVAL**

No release-blocking gameplay defect found. Exact release-preparation commit is reported in the final approval handoff; it contains this record. Working tree must be clean before handoff. No public push/tag/upload authorized yet.
