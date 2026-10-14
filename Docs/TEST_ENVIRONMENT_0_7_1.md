# Test-environment integrity — 2026-10-13

**READY for public HyThief / Wardrobes client smoke tests.** Installed Hytale 0.7.1, revision 69c03ffa58d7e2f46bce8b0356ed206cfc7f10e6. No gameplay/build/release/publication change.

## Worlds and exact reset verification

- **General**: native load PASS; active/master equality PASS (27 files). Save `C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves\HyThief Test World`; master `E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\HyThief_Test_World\Master`. Spawn `0.5, 100.0, 0.5`.
- **Container**: native load PASS; active/master equality PASS (33 files). Save `C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves\HyThief Container Theft Test World`; master `E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\Container_Theft_Test_World\Master`. Spawn `0.5, 64.1, 7.5`.
- **Wardrobes**: native load PASS; active/master equality PASS (31 files). Save `C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves\Wonderous Wardrobes 0.7.1 Smoke Test`; master `E:\Hytale adjustments\Mod Projects\WonderousWardrobes\DevTesting\Wardrobes_Smoke_Test_World\Master`. Spawn `0.5, 64.1, 0.5`.

General: original 16-cell/16-NPC fixture retained; current role UUIDs/interaction trees, floor/headroom/walls and configuration passed. Bay 14 stays intentionally empty after native Spectre_Void removal; no invented substitute. Native save schema Version 4 is not a Hytale version target. No active stale fixture target; optional historical arena assetpack.disabled remains inactive/read-only reference. Starter inventory is intentionally empty (no saved player); reset returns to the configured Adventure spawn.

Container: fixture manifest =0.7.1, version 1.0.3. Exactly four chests / four NPCs; red unowned, blue Kweebec, green nearby, yellow Feran. Mirrored witnesses/responders and measured LOS/FOV/elevation/distances, persistent ownership, one caught message/native responders passed. 64 matching Rock_Stone_Cobble starting blocks. Corrected stale descriptive marker fields only (fixture version, witness position, mobile responder description and ownership authority); no saved block/entity/ownership change. Native loading was strictly a disposable copy with development, never the live save with public.

Wardrobes: dedicated **Wonderous Wardrobes 0.7.1 Smoke Test** now preserves the existing gallery as a 31-file healthy master. Only title/marker/player start/inventory and transient-file cleanup changed; original chunk files/wardrobes/context loot/floor labels are unchanged. Native load verified all 16 distinct variants across the original gallery/context/player anchors, each 54 slots, all 12 original numbered floor labels and spawn floor/headroom. Spawn centered, with 16 Ingredient_Fibre in hotbar slot 1; Creative mode retained. Original 0.7.0 smoke save is preserved twice under Wardrobes/Rollbacks/test-infrastructure-0.7.1-20261013. The inherited HyStash marker was removed only from this dedicated smoke copy. Existing HyStash world/project fixtures were not modified.

## Desktop shortcuts

All shortcuts live on `C:\Users\askin\OneDrive\Desktop`:

- Reset HyThief Test World.lnk → HyThief/DevTesting/HyThief_Test_World/Reset-Test-World.cmd.
- Reset HyThief Container Test World.lnk → HyThief/DevTesting/Container_Theft_Test_World/Reset-Container-Theft.cmd.
- Reset Wardrobes Smoke Test.lnk → WonderousWardrobes/DevTesting/Wardrobes_Smoke_Test_World/Reset-Wardrobes-Smoke.cmd.

Each actual .lnk was ShellExecuted, not substituted by calling the underlying script. Stored targets, blank arguments and matching working directories verified. Audit-only environment flags suppress pause/capture logs; normal double-click still displays output/pause. Each calls only its adjacent fixed-world reset. No menu/combined/cross-project reset exists on an active desktop shortcut. Historical combined scripts remain preserved and unused. Every reset preserves dirty-save backups and validates master/active checksums. Logs and shortcut details: desktop-reset-results.json. Other desktop shortcuts and installed mods are unchanged.

## Installed build pairing

- `C:\Users\askin\AppData\Roaming\Hytale\UserData\Mods\HyThief-1.3.2.jar`; version `1.3.2`, target `=0.7.1`, SHA-256 `38c3d5e464c26c1bbca01a4d57424d349b5bb45dca3b502b1937ac015bd16938`. Exactly one identity.
- `C:\Users\askin\AppData\Roaming\Hytale\UserData\Mods\WonderousWardrobes_v1.2.2.zip`; version `1.2.2`, target `=0.7.1`, SHA-256 `6a766e4f9027a345749c245dc15696c2f7f9405ee27b9a8a854471959e603c49`. Exactly one identity.

Development safely preserved: `E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\Update_0_7_1\Candidates\Development\HyThief-1.3.1-container.11.jar`; SHA-256 `f2dca782e22c38839640e8886f764b16ca00c36cdf8b4c812d6c099181eae08c`. No JAR/ZIP changed. **Do not open Container Theft Test World while public 1.3.2 is active.**

## Standing future-patch rule and next client checks

The required nine-step installed-version/build/master/shortcut/roles/reset/equality/native-validation gate is documented in workspace AGENTS.md, HyThief AGENTS.md and Wardrobes AGENTS.md/testing README. Keep working fixtures untouched if no relevant change; never blindly rebuild worlds or invoke unrelated reset scripts.

HyThief public: front trade → toggle crouch/rear Pickpocket → steal → caught → notification/UI closure/Razorleaf. Wardrobes: open → transfer one supplied fibre → close/reopen → reload → persistence. No extra client matrix testing required.

Only HyThief has Git configured; infrastructure metadata/scripts/docs are committed/pushed on main. Public candidate branch is unchanged. Wardrobes and workspace rules are local files with rollback backups; Git/remote status is not applicable to those locations. No PREFAB work or publication.
