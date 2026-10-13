# Hytale 0.7.0 update audit — 2026-10-12

## Baselines and preservation

Old stable server: 0.6.8 / d2feeb3997f2efc9b4fe23282a3ece38f0618047 / SHA256 dcd2956cc65b950084650eadd56b889cf51f7610c127c3daac1f74dc471fec97.
Installed authoritative stable: **0.7.0 release**, revision **648ebf375a034beabf119d7d790cd11d4f665ff4**, branch release/release, Java 25. Server SHA256 **c3b5985b9e72727bf3bcf220923d7e61e78fe0f37cd1cd9c2a73f51e4f408ff8**; `java -jar ... --version` confirms HytaleServer v0.7.0 (release). Client executable Windows version resource reports 1.0.0.0, so it is not a trustworthy semantic game version. Its and quiche.dll's hashes are recorded in content-integrity.json; no old client binary snapshot was available for an honest binary comparison.

Before editing: main clean at 53e7897. Preserved `checkpoint/container11-before-0.7.0`, full Git bundle DevTesting/Update_0_7_0/before-update.bundle and old development JAR HyThief-container.11-0.6.8-rollback.jar. Proven installed container.11 hash before swap: 08ddb2033d942c901a52de3ef20429e83f21db7212bf676e5287f05b0123c233. Original v1.2 remains 45a186122e6cc3bfb2d2eb276811222e0c86039baa062af44a7855b90818bd79. Public patch checkout is `.build/public-0.7.0`, branch compatibility/1.3.1, based on v1.3.0 (0c542c7), not main. Original release/tag unchanged. All server/game assets inspected read-only; no live world, pristine master or reusable prefab edited.

## API and implementation audit

Compared server class inventories: 72 removed, 504 added, 2,008 changed, 35,970 byte-identical classes. Inspected 81 explicit HyThief imports; 24 changed classes received old/new javap public-signature dumps. Whole-byte comparison is a stronger negative check where classes are identical; a changed class is not automatically a broken contract.

| Area | Actual finding and action |
|---|---|
| World/chunks | World no longer implements old ChunkAccessor/IWorldChunks and loses convenience block/chunk access. ChunkStore now extends ChunkGrid, with most section/component access inherited. Development DevContainerTestWorld.refresh had one compile failure: World.getBlockType. Replaced it with BlockSection.get -> BlockType asset lookup on the already-loaded section. No forced load or change to ownership policy. Public Java needs no fix. Old offline validators required explicit section loading with SET_TICKING; NONE loads permit spawn deferral and produced null synthetic actors. That was a harness issue, not proof of broken gameplay. |
| ECS | Added parent/child hierarchy APIs (Store/CommandBuffer.setParent/getParent and parent-aware addEntity). Existing queries remain available. No HyThief hierarchy added. Potentially useful for provenance research, but an EntityStore parent is not verified as a persistent chest/NPC property identity. |
| NPC role signatures | Role velocity instruction overloads gain a float; addVelocity removed. HyThief does not call these. Role memory/damage flags added. StateSupport, ExecutionSupport, MarkedEntitySupport, BeaconBroadcast, BeaconSupport and reviewed targeting/interaction sensors/actions remain byte-identical. |
| Interaction/UI/player | UseNPC/ContextualUseNPC, SetInteractable/HasInteracted/lock-on classes examined remain identical. InteractionType adds Ability4. Player and CustomUIPage implementation bytes changed but used public signatures remain; native role tree and plugin loading pass, actual menu/input require client test. No LockOn added. Universe.transferPlayerAsync transform callback is now asynchronous; HyThief does not use it. |
| Inventory/packets | MoveItemStack, SmartMoveItemStack, ConnectionHandler and ItemContainerChangeEvent bytes identical. InventoryPacketHandler changed internally without public signature removals; cannot equate offline testing to live transport proof. ItemContainerBlock adds StayOpenWhenEmpty accessors. Existing public inventory methods still compile with same two removal warnings. No inventory/chest tuning. |
| Vectors | All bundled org.joml classes byte-identical; no geometry migration needed. |
| Serialization/ownership | Section-based ChunkGrid/API changes affect helpers; persistent block ownership read/codec paths compile and survive copied old-save loading, and native open/close keeps inventory/metadata. No ownership format change. |
| Alerts | Public adapter queued exact offender and Razorleaf entered native Attack.Default. Development LOCAL delivered to Razorleaf and Sharptooth in copied saved fixture, exact target and native Attack states/pursuit. No manual combat/movement added. |
| PREFAB | FromPrefabInstance and PrefabUtil bytes identical. New ECS hierarchy does not close the unproven final chest/late-spawned NPC membership gap. Previous two-copy provenance proof remains the recommended next checkpoint. No PREFAB implementation. |

44 selected behavior classes were byte-identical, including the above state/support/packet/prefab paths. Raw inventories and scoped javap output live in DevTesting/Update_0_7_0. This is not a claim that every reachable native method or every behavioral asset is unchanged.

## Assets and native Goblin migration

Assets.zip enumerated 68,756 entries; scanned NPC roles, templates and components against BuildSupport native provenance. 94 provenance paths/bytes differed. Most were folder relocation (Intelligent/Neutral or Aggressive -> Intelligent/Faction) or formatting with semantic JSON unchanged. A unique basename was required before treating a moved asset as missing. Substantive changes include the new generic Template_Intelligent architecture, rewritten Goblin templates, and optional memory/knockback additions to Predator/Trork templates. Common native component changes (target swap, detection/observe and combat composition) are runtime considerations even for private old templates that still load.

The first corrected native matrix had 13 actual Goblin builder failures. Old shipped templates referenced removed Component_Instruction_Goblin_Panic and private roots whose dependencies no longer existed. Four surviving native IDs were re-composed from current assets using the established Idle-only stealth generator and existing Pickpocket branch: **Goblin_Lobber, Goblin_Miner, Goblin_Scrapper, Goblin_Thief**. New native role paths must replace old ones, including the fallback resource copier; packaging both silently shadowed the new variant. Final package asserts one definition per role and no duplicate IDs.

Nine native-removed roles are omitted from generated/exported overrides, with resolver/rules/loot entries retained (not a new animal/exclusion rule):
- Goblin_Hermit
- Goblin_Lobber_Patrol
- Goblin_Miner_Patrol
- Goblin_Ogre
- Goblin_Scavenger
- Goblin_Scavenger_Battleaxe
- Goblin_Scavenger_Sword
- Goblin_Scrapper_Patrol
- Goblin_Thief_Patrol

No replacement species, fabricated aliases, new loot pool or auto-expanded new Goblin support. Configured catalog stays **185**; current package/native matrix validates **174 concrete + 2 abstracts**, with **9 historical inactive native IDs**. Four native deprecated Duke roles still exist and validate; their support remains. This count distinction must remain visible in release notes.

New Goblin perception/combat belongs to current native templates. Existing stealth multipliers, Idle-only protection and already-detected semantics are unchanged; current native base ranges differ (e.g. ViewRange 12/16 instead of the older templates). Fresh crouched Goblin approach therefore needs Firebeard confirmation. No global 15/4 calibration or container 20/6 tuning. Native Combat is already recognized by existing rules/busy-state checks. All 132 non-empty configured loot item IDs exist in current native item assets; Empty is the intentional no-item sentinel handled by PickpocketPlugin, not a missing loot asset.

Exact modified seams: build.gradle, manifest.json, build-environment.lock.json; BuildSupport native-retired list; four native-path Goblin variants, their private stealth roots/components and sites metadata; build/release docs. Development only: DevContainerTestWorld block lookup. Public Java/config/loot/UI backend unchanged from v1.3.0. Main default artifact now explicitly container.11 to prevent accidentally labeling development code 1.3.0.

## Official cross-check and new opportunities

Official stable docs: https://docs.hytale.com/ and API https://docs.hytale.com/api/. Update 7 modder notes: https://hytale.com/news/2026/10/modder-facing-patch-notes-update-7. Full precursor notes: https://hytale.com/news/2026/9/pre-release-patch-notes-update-7. Direct web fetch of the final modder page was unavailable; accessible official excerpts and precursor notes were cross-checked against installed 0.7.0 bytecode/assets rather than assumed final.

Relevant additions include ECS parenting, NPC memory acquisition options, GameFlag sensors, opt-in ranged leading, improved target swapping/observation, fourth item ability and section-based streaming/access. Actual native binaries/assets determine applicability. Broader worldgen density/editor, encounter weather, language/network protocol and UI polish do not require HyThief gameplay changes here. No unsupported reflection/patching added to production; test-only reflection invokes existing package-private helpers.

## Validation and publication gate

- Public clean build PASS (Java 25 / Gradle 9.1.0 / pinned 0.7.0). Same two inventory removal warnings; existing Gradle deprecation notice.
- Final native public matrix PASS: 185 configured IDs; 174 concrete spawns; two abstracts; nine historical IDs retained; actual role names/interaction trees/Idle state families/unchanged multipliers verified. Evidence public-native-pass/native.log. Initial failed logs retained, not misreported as passes.
- Public caught adapter PASS: PUBLIC_GUARD_QUEUED_PASS ExactOffender=true NativeSignal=Kweebec_All_Danger; PUBLIC_GUARD_NATIVE_PASS State=Attack.Default ExactOffender=true. Synthetic offline offender exercises the actual public signal method; not a real-client theft/UI test. Evidence public-guard-native/native.log.
- Public package: JSON parsing/ZIP CRC PASS, no duplicate role IDs, no container development/ownership/test-helper classes. 1,665 entries. SHA256 **bf398019bb07e97392414cce9ef62a4fc9c95bab29b097c7662ef82a881a9f94**.
- Development clean build PASS. Saved-fixture native ownership, control/faction/nearby visual classification, native chest open/close and exactly-one caught notifications PASS; LOCAL both-visible and Feran-only delivery/pursuit PASS. Evidence development-native-* logs. No world master promoted or live reset performed.
- No dedicated PvP feature/path exists in public Java/config. Player references/target access compile; actual multiplayer/PvP environment interaction is not proven by an offline server. No invented PvP claims.
- REAL-CLIENT REQUIRED: crouched rear prompt/menu/no-turn, item transfer, Escape/caught close, notification, guard pursuit and front trade; fresh crouched approach on a redesigned native Goblin. Native tests cannot render the client page or exercise real packet ingress.

Public candidate: `.build/release-candidates/1.3.1-0.7.0/HyThief-1.3.1.jar` plus SHA256 and release notes. Compatibility branch remains separate from container main. No v1.3.1 release tag/GitHub release/CurseForge upload before Firebeard confirms. After confirmation, tag v1.3.1 and publish this exact tested artifact for 0.7.0.

## Short client handoff

Install only the public candidate; back up the previous installed container.11 JAR. Use a pickpocket test/normal world with merchant + nearby Razorleaf. Do not open the ownership test save using the public plugin: it deliberately lacks experimental ownership components; leave that master/active save untouched for the development build. Toggle crouch keeps stance while in UI.

1. Front merchant trade still opens.
2. Crouch behind merchant: prompt -> old menu, NPC stays facing away; steal one item; Escape closes.
3. Reopen and steal until caught: caught notification/menu close and native Razorleaf pursuit.
4. Fresh current Goblin (Lobber/Miner/Scrapper/Thief): rear crouched approach -> prompt/menu/loot, without premature fresh acquisition defeating entry. Standing detection remains normal.

Report any failure and session log; publication stays pending until this passes. New native Goblin ranges/behavior and actual client transport/UI are the primary remaining risks. Original releases, other mods, prefab and worlds remain protected.

## Verified candidate deployment

The public 1.3.1 candidate is installed at C:\Users\askin\AppData\Roaming\Hytale\UserData\Mods\HyThief-1.3.1.jar, ServerVersion =0.7.0, SHA256 bf398019bb07e97392414cce9ef62a4fc9c95bab29b097c7662ef82a881a9f94. Exactly one Local:HyThief JAR is active; unrelated mod JAR hashes/count are unchanged. Previous installed 1.3.1-container.11 is preserved at DevTesting/Update_0_7_0/InstalledRollback/HyThief-1.3.1-container.11.jar, SHA256 08ddb2033d942c901a52de3ef20429e83f21db7212bf676e5287f05b0123c233. No live world, master, prefab or base-game files were changed. Publication is pending the client smoke test above. Development and public builds remain separately labeled.
