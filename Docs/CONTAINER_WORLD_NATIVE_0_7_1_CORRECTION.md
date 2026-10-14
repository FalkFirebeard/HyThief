# Native 0.7.1 container world correction — 2026-10-14

**Ready for Firebeard's client visual confirmation; disappearance of the red OUTDATED indicator is not yet physically confirmed. No PREFAB work or publication.**

## Confirmed native compatibility failure

The master and active save embedded `mods/HyThief_Faction_Field.zip`, whose root `manifest.json` still declared `ServerVersion: =0.6.8`. The enabled `FirebeardDev:ContainerFactionField` pack contains the stationary witness roles and Feran responder fixture. This is an actual native asset-pack declaration consumed by Hytale, not our custom target/readme/checksum. Previous audits checked the folder manifest but missed this ZIP. Current 0.7.1 `PluginManifest.checkServerVersionCompatibility` returned INCOMPATIBLE for =0.6.8 and COMPATIBLE for =0.7.1. Only this manifest entry changed; every packed role payload is byte-identical. Client binary references also include ServerVersionCheck and the world mod-version-mismatch message. A full executable world-list branch trace was not recovered, so final confirmation that this accounts for the whole red badge requires refreshing the real client list. If the badge remains, inspect its tooltip/log before making another save change.

The suspected native config Version=4 is NOT obsolete. Both current `HytaleServerConfig.VERSION` and `WorldConfig.VERSION` are 4. A fresh native world was serialized by the installed 0.7.1 APIs and loaded in a separate current server process; its server/world configs likewise use 4. The native world generator's own version is independently 0.0.0, not the game version. Inventing a Version=0.7.1 or bumping the numeric codec version would be wrong. Existing chest/chunk/player data already loaded and persisted through the current 0.7.1 codecs. No evidence of an older incompatible chunk/schema/header was found.

## In-place native refresh, not a replacement arena

Preserved complete old master and active rollback at `DevTesting/Container_World_Correction_0_7_1_20261014/RollbackMaster` and `RollbackActive`, plus prior reset/checksum files. Kept the existing world UUID, player file, four chest locations/ownership and all NPC fixture roles. Current native `WorldConfig.load/save` reserialized the world config; current server load/shutdown saved the touched region data after native BlockOperations placed the larger markers. The corrected clean candidate, not any transaction/combat test run, became MASTER. ACTIVE was reset through the actual desktop shortcut. Fresh native comparison and cold reload prove current native codec compatibility; client visual confirmation remains separate.

## Visible native colors and preserved geometry

Each marker is now a 4-by-3 floor pad, X=-2..1 and Z=chestZ-1..chestZ+1, Y=63 (top surface 64). These are four stock blocks with separate native texture files, no custom recolor or tint:

| Color | Native block | Native texture | Ownership | Chest origin |
|---|---|---|---|---|
| RED | Cloth_Block_Wool_Red | BlockTextures/Cloth_Red.png | unowned control | (0,64,-6) |
| BLUE | Cloth_Block_Wool_Blue | BlockTextures/Cloth_Blue.png | FACTION Kweebec | (0,64,-2) |
| GREEN | Cloth_Block_Wool_Green | BlockTextures/Cloth_Green.png | NEARBY / LOCAL | (0,64,2) |
| YELLOW | Cloth_Block_Wool_Yellow | BlockTextures/Cloth_Yellow.png | FACTION Feran | (0,64,6) |

Each has its original label as secondary confirmation. Four chests only; 16 copper and 8 iron bars each. No permanent walls. NPC/player interaction geometry did not change: witnesses at (-7.5,64,0.5)/(7.5,64,0.5), responders at (-21.5,64,0.5)/(21.5,64,0.5), player pads X=0 and Z=chestZ-0.5. Mirrored distance/FOV/LOS/elevation passed on all four stations; GREEN is 7.566372975210778 blocks from each witness. Alert distances remain 14/29 blocks, both within LOCAL 30. Responders remain mobile and outside direct standing sight at the pads. Spawn (0,64.1,10.5) and saved 64 Rock_Stone_Cobble are preserved.

## Native regression and reset proof

- RED: no notification or alert.
- BLUE: one notification, Kweebec_All_Danger, Razorleaf only.
- GREEN: one notification, HyThief_Local_Theft, both responders. Both visible, Kweebec-only and Feran-only witness cases passed.
- YELLOW: one notification, Feran_Warriors_Attack, Sharptooth only.
- Native open/close, withdrawal attribution, insertion/internal rearrangement without false theft, chunk unload/reload and fresh server restart preserved ownership and inventory snapshots without reattachment.
- FOV/rear rejection and opaque-block LOS passed. Container 20/6, normal 15/4 and LOCAL delivery 30 unchanged. Crouched 5 seen, 8 unseen; standing 8/18 seen, 22 unseen.
- Native marked offender, LastSeen/Attack and pursuit/movement passed; no manual AI control.
- Full pickpocket matrix: 185 configured, 174 concrete, 2 abstract, 9 historical retired; actual roles and modern interaction trees preserved.
- Installed development JAR unchanged: `f2dca782e22c38839640e8886f764b16ca00c36cdf8b4c812d6c099181eae08c`, version 1.3.1-container.11, target =0.7.1. Public 1.3.2 remains separate/unchanged.

The actual `Reset HyThief Container Test World.lnk` targets the in-place Reset-Container-Theft.cmd with the correct working directory. It restored this MASTER successfully. A disposable copy of that restored ACTIVE passed cold native load/LOCAL regression. Reset now checks both native codec version fields and every embedded ZIP/JAR/folder manifest, in addition to installed version/revision and hashes. A negative test using the old pack was rejected before any active save changed. Old master is rollback only. Other worlds/projects/mods untouched.

Evidence: `DevTesting/Container_World_Correction_0_7_1_20261014`, including Refresh, FreshWorld-load, Persistence, Restart, Mechanics-*, Pickpocket-matrix, Actual-desktop-reset, Post-reset-native and Negative-reset-guard logs. No development JAR rebuild, no gameplay edits. Test-only author/validator now agrees on the 4x3 pads. The permanent public/development/native-world/visible-fixture patch workflow is recorded in project AGENTS.md.

## Client handoff / acceptance still pending

Restart Hytale or refresh the Worlds page so it rescans the restored save. Check that Container Theft Test World has no red OUTDATED indicator. Open it and confirm the four large real-color pads are obvious on approach. Native compatibility/regression is ready; do not claim full client acceptance or start PREFAB until Firebeard confirms the visual result.
