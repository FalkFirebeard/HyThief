# Ownership Editor inventory tool — 1.5.0-editor.2

Date: 2026-10-20 (America/Denver). Hytale 0.7.2, revision d769dc3d57881261a62858e1394f0e4de9a21eca.
Accepted baseline: Phase 2A editor.1, commit 9b15dae. Falk confirmed all five containers, fresh native chests, persistence, inventories and theft/pursuit regression behavior.

## Implementation

Item: `HyThief_Ownership_Editor_Tool` — HyThief Ownership Editor Tool.
Native acquisition verified: `/give HyThief_Ownership_Editor_Tool` (quantity defaults to one; native give permission still applies).
The item is categorized under the existing Creative Library `Tool.PrefabEditing` / `PrefabTools` category, uses the native prefab icon and brush model/texture, and has MaxStack 1. No recipe, loot entry, starter-inventory grant, BuilderTool selection registration or native editor-tool override.
Client presentation/search placement remains for Falk to verify; server item/category/assets and native give were checked.

Primary resolves the `HyThief_Ownership_Editor` root, registered `HyThiefOwnershipEditor` SimpleInstantInteraction, then CommandBuffer.run invokes the shared OwnershipEditor.open on the world thread. The simulation pass does not open UI. RequireNewClick and the existing ownership-page guard prevent repeated activation from replacing a live page or losing unsaved edits. No polling, key gesture, client modification, third-party code, dependency or timer.

Both the command and item use the same opener, permission, 8-block raycast, native multiblock base resolution, selection and OwnershipEditorPage. Invalid/unauthorized tool uses are silent; command errors retain messages. The item itself confers no permission. Apply still independently rechecks permission, actor/world, reach, entity, block, rotation, inventory identity and original ownership. No ownership format changes.

Native conventions inspected read-only: EditorTool_PrefabEditing_SelectPrefab.json, CreativeLibrary/Tool.json, SimpleInstantInteraction, CommandBuffer.run and PageManager.openCustomPage in installed 0.7.2. The native SelectPrefab root uses RequireNewClick. HyThief uses its own simple interaction, not the native BuilderTool action. Official references: https://docs.hytale.com/assets/item/interactions/ and https://hytale.com/news/2026/10/2026-10-12-modder-facing-patch-notes-update-7 . This implementation does not copy MMO Skills or RPG Leveling code.

## Exact production/build files

- build.gradle — development version editor.1 → editor.2.
- src-java/com/local/pickpocket/OwnershipEditor.java — shared read-only opener and active-page guard.
- src-java/com/local/pickpocket/OwnershipEditorCommand.java — delegates to shared opener.
- src-java/com/local/pickpocket/OwnershipEditorToolInteraction.java — new native Primary handler.
- src-java/com/local/pickpocket/PickpocketPlugin.java — registers interaction codec.
- Server/Item/Items/EditorTool/HyThief_Ownership_Editor_Tool.json — new tool asset.
- Server/Item/RootInteractions/HyThief_Ownership_Editor.json — new click-gated root.
- Server/Languages/en-US/server.lang — tool name/description.

OwnershipEditorPage, ownership schema, storage, witness/crime, AI and pursuit implementations unchanged. HyStash production files untouched. Existing OwnershipEditorPage, OwnershipProofData, NativePursuit, OwnedContainerAccess, ContainerTheftService and HyThiefContainerAlert classes are byte-identical to editor.1. No test code/assets enter the development JAR.

## Automated evidence

Clean Gradle build passed against pinned stable server SHA-256 23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff. Existing two inventory removal warnings and deprecated-API/Gradle notices remain; no new production warnings.

Disposable native servers only:
- Tool8: actual loaded item/root and native interaction chains; unauthorized denied; chest opens read-only; duplicate does not replace page; Cancel no write; permission revoked before Apply denied; authorized Apply succeeds; both wardrobe halves resolve same base; invalid/empty/out-of-reach uses silent; inventory and block preserved.
- EditorBackend: real UI codecs, Apply/Cancel, stale ownership/replaced block rejection, independent ownership, 54-slot wardrobe and unchanged inventories.
- EditorRestart: full-process save/restart preserves edited chest/wardrobe ownership and items.
- EditorCommandGive: registered native /give grants exact item and /hythief ownership still opens the page.
- AccessChest and AccessWardrobe: existing real service withdrawal/notification/forced closure, queued transfer rejection, native danger/pursuit, lockout, restoration, deposits/internal moves, unseen withdrawals and notification deduplication passed.
- HyThiefAlone: no mandatory HyStash dependency.
- ToolMatrix: 185 configured roles, 174 spawned roles, 9 retired IDs, 2 validated abstracts; actual role and interaction trees preserved.

Tool proof uses a synthetic native actor without a connected client. Early unsuccessful proof runs exposed test-harness requirements (root actor traversal, network ID, native cooldowns, packet-sink distinction), not changes to production logic. Only Tool8 is the passing final native-chain result. Physical click routing, Creative/Prefab UI placement, held appearance and client UI rendering require Falk's manual confirmation.

## Preservation / installation / reset

Candidate SHA-256: f496b908a07e02289fbb32e6da4a824786acb01f6390f57a94540661c8873939.
Installed after Falk explicitly approved outside-project writes: C:/Users/askin/AppData/Roaming/Hytale/UserData/Mods/HyThief-1.5.0-editor.2.jar. Exactly one HyThief identity active; unrelated installed files including HyStash unchanged.

Rollback: DevTesting/Ownership_Editor_Tool/Phase2A-editor.1-source-rollback.zip and DeploymentRollback/ (previous installed JAR, native active/master config and fixture pin/checksum files). Restore the old JAR and the matching version pins together if rolling back.

Only fixture metadata changed under DevTesting/Ownership_Editor_Phase2A:
- candidate-pin.json
- master-checksums.json
- Master/universe/worlds/default/config.json (RequiredPlugins.Local:HyThief =1.5.0-editor.2)

The active 01 HyThief save changed only the identical native RequiredPlugins version field. All other active/master files, including entity/chunk/player data, ownership and inventory, hash-match the pre-task snapshot. No live world reset ran. No tool was inserted into Falk's inventory.

Reset 01 HyThief desktop shortcut and original reset scripts unchanged. Shortcut inspected at C:/Users/askin/OneDrive/Desktop/Reset 01 HyThief.lnk. Updated candidate/server/master checksum checks passed. Reset logic ran successfully against a disposable redirected save; its output matches the master. It was deliberately not launched against Falk's edited active save.

## Manual checkpoint — no publication

Use the existing active 01 HyThief world without resetting it. Obtain the item once using `/give HyThief_Ownership_Editor_Tool`, put it in an empty hotbar slot, equip, aim within 8 blocks, and use Primary.

1. Green A, blue B, yellow C: each native chest opens its existing ownership editor with its CURRENT saved values. Opening must not normally open/break the chest or change contents/ownership.
2. Orange D and red E: same behavior; test lower and upper wardrobe halves. All 54 slots remain intact.
3. Edit one target, Cancel: unchanged. Reopen, Apply a deliberate change: only that target changes. Save/reload and check persistence; retain current edits unless choosing to change them.
4. Hold Primary / click repeatedly with UI open: no duplicate page or lost edit state. Aim at empty air/floor or beyond reach: no UI/message/block damage.
5. `/hythief ownership` remains usable. An account without hythief.ownership.edit cannot open through possession; revocation while UI is open prevents Apply (automated proof already passed).
6. Brief witnessed-theft/pursuit regression only if desired; automated suite passed. Do not reset current edits merely for this test.

Expected: same verified editor, alternative native inventory entry only. No 1.5.0 public release until Falk approves client testing.
