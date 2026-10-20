# Ownership editor Phase 2A — client test checkpoint

Date: 2026-10-20. Branch: `development/ownership-editor-phase2a`.
Base: `f6b6451` (public HyThief 1.4.2 plus HyStash compatibility documentation).
Installed stable: Hytale 0.7.2, revision `d769dc3d57881261a62858e1394f0e4de9a21eca`.
Development candidate: **1.5.0-editor.1**, target **=0.7.2**. Not a public release.
JAR SHA-256: `3da4895d8dace3f3b210bfa3012ba016e95f36a197516d6510ad105672426b92`.
HyStash 1.4.0 stays unchanged, SHA-256 `75edd948e8b86b00cdc74f95d9566f24a4b7d9ffa97e248602b7d4671b2069bb`.

## Scope and native architecture

The user-supplied Phase 1 handoff was reviewed as reference. No separate saved Phase 1 report was found; the handoff states that investigation made no files.
Installed stable server signatures/bytecode and existing component, witness and access implementations were checked directly. Official reference: https://docs.hytale.com/ and the official Update 7 modder notes at https://hytale.com/news/2026/10/2026-10-12-modder-facing-patch-notes-update-7/ . Installed stable API is the implementation authority.

`/hythief ownership` requires native permission `hythief.ownership.edit` at root, subcommand and Apply. There is no automatic selection callback, permission default grant to normal players, new dependency or new crime path.

`OwnershipEditor.target` calls native `TargetUtil.getTargetBlockOrigin` with the stable 0.7.2 continuation predicate `id -> id == 0`, resolving the hit's multiblock filler to its base. Bytecode and native runtime testing show the predicate controls continuation; the no-predicate overload stops at the initial air cell in this build. The explicit predicate is necessary. A native ray hitting the wardrobe's upper half resolved to `(4,64,2)`; real registered command dispatch also targeted/opened a chest correctly.

`BlockModule.getBlockEntity` and existing `ItemContainerBlock` identify actual storage; unsupported blocks are rejected. No marker/spawner or generation editing is implemented.

`InteractiveCustomUIPage`, typed `BuilderCodec` event data, `UICommandBuilder`, `UIEventBuilder` and `PageManager.openCustomPage` provide the editor. Native Common.ui checkbox, dropdowns, buttons/styles are reused. Scope is a native two-choice dropdown (Nearby/Faction). Scope/faction controls are disabled when unowned; faction is disabled for Nearby.

Apply executes on the native world thread and validates permission, actor/world, eight-block proximity, original entity reference, inventory instance, block ID, rotation and original ownership value. Replaced/unloaded/changed containers and concurrent ownership edits are rejected. Identical values produce no write. Duplicate Apply and post-dismiss events are ignored. Cancel/Escape write nothing.

Only the existing `HyThiefOwnershipProof` component changes. Its component ID and `Owned`, `Scope`, `Faction` codec are unchanged. Explicit unowned writes `false,"",""`; Nearby writes `true,"NEARBY",""`; Faction writes `true,"FACTION",<group>`. Storage contents, slots, identity, capacity, rotation and native opening logic are not rewritten.

Persistence uses `BlockComponentSection.markBlockNeedsSaving` and `WorldChunk.markNeedsSaving`; an available native `PrefabEditSession` is marked dirty at the edited position. Full prefab/export/clipboard correctness remains Phase 2B, not a Phase 2A claim.

The faction selector is restricted to current native groups already represented in `ContainerOwnershipPolicy` and `ContainerWitnessProfiles`: **Kweebec, Feran, Trork, Outlander, Skeleton, Zombie**. Only existing supported witness roles within those groups react; this does not add NPC support or turn all group members into witnesses.

## Changed production inputs

- `src-java/com/local/pickpocket/OwnershipEditor.java`: target snapshot, validation and component-only apply.
- `src-java/com/local/pickpocket/OwnershipEditorCommand.java`: native creator command.
- `src-java/com/local/pickpocket/OwnershipEditorPage.java`: typed native UI and draft/apply/cancel lifecycle.
- `Common/UI/Custom/Pages/HyThiefOwnershipEditor.ui`: new editor page.
- `src-java/com/local/pickpocket/PickpocketPlugin.java`: one command-registration line.
- `build.gradle`: development version only.
- `.gitattributes`: preserve exact bytes of this new fixture master through checkout.
- `DevTesting/Ownership_Editor_Phase2A`: isolated master, native verification helper/runners/evidence, candidate/master pins and new reset scripts.

No existing gameplay source/assets changed. ZIP comparison against public 1.4.2 found only new editor entries, manifest version, and the plugin registration class/nested-class debug metadata. Existing loot, perception, role assets, ownership codec, container transfers, witness/caught/pursuit code and other resources are byte-identical. No test plugin/world/fixture is packaged in the candidate.

## Automated evidence

All native tests ran in disposable copies, never the active client save.

| Check | Result |
| --- | --- |
| Final `build.ps1 clean build` | PASS; existing two inventory removal warnings and existing Gradle deprecation notice |
| Reproducible final JAR equals native-tested candidate | PASS; hash above |
| Actual registered command -> native target -> page open | PASS (`command-result.json`) |
| Native permission grant/revoke, denied command permission and denied Apply | PASS |
| Unsupported block, invalid faction, stale ownership and removed/replaced block | PASS |
| Owned/Nearby/Faction/unowned writes; unchanged Apply; independent identical chests | PASS |
| Real page open packet, typed edit/apply/cancel events, duplicate Apply suppression | PASS |
| Inventory slots and instance preserved; wardrobe remains 54 slots | PASS |
| Native upper-wardrobe filler ray resolves storage base | PASS |
| Edited chest Nearby, edited wardrobe Feran, explicit-unowned wardrobe survive full process restart | PASS (`final-restart-result.json`) |
| Existing full pickpocket matrix | PASS: 185 configured, 174 spawned, 9 retired, 2 abstract |
| Existing HyStash + HyThief theft/access native regression | PASS (`EditorAccess-result.json`) |
| Native chest + HyThief alone theft/access native regression | PASS (`EditorChest-result.json`) |
| Native guard pursuit, forced close, queued transfer rejection, safe deposits/internal moves, exact denial/caught notifications and restoration | PASS |
| Final fixture: 5 containers, correct ownership/loot, 54-slot wardrobes, NPC positions/facing and clear standing FOV/LOS | PASS (`fixture2-result.json`) |
| Fixture standing owned withdrawals can be witnessed; crouched pads outside 6 blocks | PASS |
| Actual desktop reset twice, including restoring a file-only perturbation | PASS |
| All pre-existing saves and checked general/chest/wardrobe masters unchanged | PASS (`handoff-verification.json`) |

Native UI packet construction/event execution passed; graphical rendering and client interactions still require Falk's test. No client was launched or active save resaved automatically.

## Ready-to-use fixture

World: **01 HyThief**. Existing save IDs were inventoried first: 01/02 HyStash existed; no numbered HyThief save existed. Existing named HyThief worlds remain untouched.

Master: `DevTesting/Ownership_Editor_Phase2A/Master`.
Active: `C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves\01 HyThief`.
Reset: desktop **Reset 01 HyThief**, pointing to this folder's `Reset-01-HyThief.cmd` / `.ps1`.
Close Hytale before reset. The launcher checks running clients/servers, actual stable server hash, both installed candidate hashes, master hashes and the exact destination; backs up the active fixture; resets only this world; verifies every file. It refuses stale patches/candidates. Existing shortcuts/scripts are unchanged. Windows PowerShell compatibility uses native .NET SHA-256, avoiding unavailable module-dependent Get-FileHash in the desktop shell.

The master contains only its test asset pack, native save/config/player data, not copies of the production mod archives. Both global mods are enabled, unrelated mods disabled in this save only. Hytale `RequiredPlugins` requires the HyThief Java plugin; it cannot require data-only HyStash as a Java plugin. The reset pins verify HyStash instead. No production manifest dependency was added.

| Label | Base position | Initial ownership | Native storage |
| --- | --- | --- | --- |
| A | -8,64,2 | Unowned | Kweebec large chest |
| B | -4,64,2 | FACTION / Kweebec | Identical chest |
| C | 0,64,2 | NEARBY | Identical chest |
| D | 4,64,2 | Unowned | HyStash Kweebec wardrobe, 54 slots |
| E | 8,64,2 | FACTION / Kweebec | Identical wardrobe, 54 slots |

Each has 16 copper bars in slot 0 and 8 iron bars in slot 1. Green pads are on the witness-facing side (`z=0`), keeping tall wardrobes out of witness LOS. Kweebec merchant `(0,64,-8)` and idle-stationary/combat-mobile Razorleaf `(0,64,-20)` face the row. Fixture-only stationary guard assets are isolated to this world's pack, not the mod. Spawn `(0.5,64.1,-3)` faces the row. Falk's player starts with 64 matching cobble blocks and fixture-local native admin/editor permission. Native fixture measurements: B pad 9.1924 blocks, C pad 8.5147, E pad 12.0208; all clear standing LOS/FOV, all beyond crouched 6-block container range. This is an editor/ownership fixture, not a comparative range-balance test.

## Client checklist

1. Select **01 HyThief**. Confirm five labeled containers and both NPCs. Use toggle crouch for safe initial inspection.
2. Look at A within eight blocks and run `/hythief ownership`. Confirm Unowned and disabled scope/faction. Toggle Owned, choose Nearby, Apply. Reopen to confirm; B must still be Kweebec Faction. Check A's items/slots unchanged.
3. Edit C to Faction/Kweebec; reopen and verify. Change a draft and Cancel/Escape; reopen to confirm no write.
4. Aim at D's upper half, run the command, set Faction/Kweebec, Apply. Check all 54 slots, normal opening and unchanged test items. E must be independent. Set E unowned and verify it stays safe.
5. Exit/reload this world and verify edits persist. Repeat after fully exiting/relaunching Hytale.
6. Reset with Hytale closed. On the original pads, crouched owned withdrawals remain unseen; standing B/C/E withdrawals should invoke existing caught notification, forced closure and guard response. A/D unowned stay safe. Reset after caught to clear native pursuit.
7. Check ordinary pickpocket/trade if desired; existing general/chest/wardrobe fixtures and reset shortcuts remain available.

Public rollback JAR: `DevTesting/Ownership_Editor_Phase2A/Rollback/Installed-HyThief-1.4.2.jar`; hash `a90198328c2cd16be4c119f5d489233524cbeea709ad9f7ec9a0158da7c08642`. Original public artifact and public/development baseline branches remain unchanged. Do not reset older hash-pinned fixtures with this experimental candidate unless their pins are explicitly approved; their original scripts were deliberately preserved.

## Boundaries / next gate

No publication or public-branch push. No HyStash production changes, schema changes, new crime systems, spawner outcomes, multi-faction, generation/migration, break protection, undo/redo or prefab export proof. Save/restart is proven for native containers and the tested wardrobe. Full authoring/export/rotation/clipboard round trips and rendering remain separate gates. Await Falk's Phase 2A client result before any Phase 2B work.
