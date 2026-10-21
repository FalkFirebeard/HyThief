# NPC Editor â€” verified development checkpoint

2026-10-20 (America/Denver). Hytale 0.7.2 / d769dc3d57881261a62858e1394f0e4de9a21eca.

## Recovery

Recovered development/ownership-editor-phase2a at aec6304 with uncommitted NPC editor implementation and the first native test passing. Preserved this work. Falk approved HyThiefNpcParticipation and HyThiefNpcRules. Later steering prioritizes this bounded editor, followed by metadata investigation without a broad rewrite.

The accepted Ownership Editor/page/tool, ownership component, transaction service, source attribution and pursuit access code remain byte-identical to aec6304. No HyStash changes. Active world, master and installed mods/pins remain untouched.

## Candidate

Version 1.5.0-editor.3, exact target =0.7.2. Artifact: DevTesting/Npc_Editor/Candidates/HyThief-1.5.0-editor.3.jar.

SHA-256: 89334a1850735a0f50f5caa4b3ab0f579a75447acd5db088aeacbd8f903328ac.

Deployment update: Falk approved installation and pin updates. editor.3 is now installed as the only active HyThief JAR. Previous editor.2 and old pins are backed up under DevTesting/Npc_Editor/Deployment-editor.3-736f9de2-285d-43fd-a5e3-6b9bb7fdbdeb. Only the active/master required-plugin version, reset candidate version/hash and affected master checksum changed. No reset ran. All other active/master files and unrelated mods, including HyStash, remained byte-identical. No public release/tag/publication is authorized.

## Implementation

`/give HyThief_NPC_Editor_Tool`; permission `hythief.npc.edit`. Equip the native brush-style tool, aim at a live NPC within 8 blocks, use Primary. Native ray plus fresh LOS validates the target. No attack, trade, theft or target lock-on is invoked.

The native page offers independent Default/Enabled/Disabled values for Pickpocket, Witness and Crime Response. Individual overrides are native HyThiefNpcParticipation components with Dirty saving. Role/faction overrides use world-local HyThiefNpcRules maps keyed by stable strings, not runtime indexes. Each field resolves individual > exact role > recognized faction > existing defaults. All-Default individual removes the empty component.

Opening, dropdown edits, Cancel and Escape do not write. Apply rechecks permission, actor/world, NPC Ref/UUID/role/faction, distance, LOS, original scope values and saving capability. Duplicate activation and Apply are suppressed. Stale/unsupported operations fail safely. Replacement spawns inherit world rules, not a previous NPC's individual settings.

Shared pickpocket sensor and backend safety check enforce Pickpocket; visual candidates and victim catch enforce Witness; direct target/state/beacon receivers enforce Response. Existing loot/resolver/interaction and native reaction contracts remain the capability ceiling. Enabled overrides a broader Disabled but cannot create missing loot or AI.

## Boundaries

Individual and exact-role scopes target live native NPCs. Faction scope currently recognizes Kweebec, Feran, Goblin, Trork, Outlander, Skeleton and Zombie via native membership, using the prototype's first recognized match. Unresolved factions cannot receive a faction-scope edit. This is a documented bounded limitation, NOT universal dynamic faction discovery; see the metadata review. The accepted Ownership Editor's six-group menu is unchanged.

Disabled limits HyThief participation, not all native behavior: ambient hostility, damage, ongoing pursuit and native social propagation remain independent. It does not change faction membership, AI, patrols or native trade. Unsupported roles can use Default/Disabled; Enabled is rejected without an existing supported path.

## Automated native proofs

Disposable copies only; synthetic players exercise real native interactions, pages, stores, transactions and NPC roles. Connected-client UI/gameplay remains for Falk.

| Proof | Result |
|---|---|
| Npc1 | PASS: native tool/ray, permissions/revocation, Apply/Cancel, duplicate suppression, independent NPCs, three settings, per-field precedence, actual pickpocket sensor, witness/guard filters, stale data/ref, unsupported safety, no edit-induced NPC reaction |
| Restart1 | PASS: full process restart restored exact saved NPCs and individual/role/faction settings |
| Prefab2 | PASS: native JSON and binary export/import plus paste preserve individual settings; copies get distinct UUIDs |
| Command1 | PASS: native give acquires NPC tool; existing ownership command still opens chest editor |
| OwnershipTool1 | PASS: accepted tool, chest and both wardrobe halves, permissions, duplicates, Cancel/Apply, unchanged inventory, no damage |
| Matrix1 | PASS: 185 configured roles, 174 concrete spawns, 9 retired, 2 abstract; actual roles/interaction trees preserved |
| Chest1/Wardrobe1 | PASS: item kept, one caught notice, native guard Attack, closure, queued transfer rejection, pursuit lock/notice/restoration, safe unowned/deposit/internal/unseen transfers |
| Alone1 | PASS: no HyStash installed |

Clean build succeeded using JDK25 and pinned current server. Existing two Inventory-removal warnings, ContainerTheftService deprecated-API note and Gradle future-version warning remain. Gradle has no unit-test sources; native integration proofs above provide meaningful validation.

The final clean JAR is entry-byte-identical to every native-tested copy; ZIP timestamps need not match. No helper classes, research assets, fixtures or test worlds are packaged. Evidence: DevTesting/Npc_Editor/*-result.json, final-build-equivalence.json and each Run-*/native.log.

First prefab proof read native deferred-placement references too soon and failed. Corrected proof awaits insertion and validates real entities; no production workaround. Individual settings transfer, but world resource rules do not travel inside an NPC prefab. Other clipboard/spawn paths and undo/redo remain unverified.

## Preservation and manual test

Rollback: DevTesting/Npc_Editor/editor.2-source-rollback.zip and preserved editor.2 JAR. Hash comparison: all 28 master files and 63 active 01 HyThief files unchanged, no additions. No reset or shortcut launch occurred.

After separately approved installation, use existing 01 HyThief without resetting:

1. `/give HyThief_NPC_Editor_Tool`; aim at merchant and Primary. Confirm readable UI, identity, three settings and no damage/trade/turn. Air/non-NPC/out-of-range must not open it.
2. Individual Pickpocket Disabled then Cancel: no change. Apply Disabled: this NPC loses Pickpocket; front trade remains. Restore Default: rear-crouched Pickpocket returns. Another merchant must stay independent.
3. Individual Witness Disabled, Response Default: standing withdrawal from suitable owned fixture, with no other valid witnesses, must not be caught by this NPC. Restore Default and verify existing caught/closure/guard flow.
4. Guard Response Disabled, merchant Witness Default: theft remains caught/closed, but direct HyThief handoff skips this guard. Native ambient/social reactions are separate. Restore Default.
5. Test one field with faction Disabled, role Enabled, individual Disabled. Narrower wins; other fields inherit. Broad rules affect this world and future spawns. Restore every broad rule to Default afterward.
6. Reopen, reload world and restart game: settings persist, inventories/ownership stay intact; Cancel/Escape never save. Optional native prefab test must not assume world rules travel.
7. Existing ownership tool on chest and both wardrobe halves; normal trade, rear pickpocket, theft/caught, owned-container closure, pursuit denial and restoration.

Permissions: hythief.npc.edit and hythief.ownership.edit as applicable. No active installation or required-plugin pin changes until Falk approves. Public 1.5.0 readiness still requires manual testing and final scope review.

## Approved deployment verification

Installed exact candidate SHA above, one Local:HyThief identity. Active/master RequiredPlugins now =1.5.0-editor.3. Master checksum inventory and installed reset candidate pins pass; reset PS1/CMD are byte-identical. Existing desktop shortcut is inspected, not launched, to preserve active edits. Manual connected-client testing is next. Prior preservation results above describe the pre-deployment checkpoint; deployment changed only the stated version pins.
