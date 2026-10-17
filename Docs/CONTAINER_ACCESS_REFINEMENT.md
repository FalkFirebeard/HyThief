# Owned container closure and native pursuit lockout — development checkpoint

2026-10-17. Development candidate **1.4.2-access.1**, Hytale **=0.7.2**, installed revision `d769dc3d57881261a62858e1394f0e4de9a21eca`. Not published. Source baseline is current main 1.4.1: the 1.4.0 gameplay plus the approved Pickpocket text hotfix, preserved.

## Native API conclusion

Native `UseBlockInteraction.doInteraction` invokes cancellable ECS `UseBlockEvent.Pre` before executing the block's native root interaction. `OwnedContainerAccess` cancels that event only for a live `ItemContainerBlock` with eligible existing `HyThiefOwnershipProof` metadata and a matched native engagement. Native chest and wardrobe Use/Open_Container assets are unchanged. Normal trade and the NPC Pickpocket interaction are unaffected.

`Window.close(ref, accessor)` delegates to `WindowManager.closeWindow`: it sends native CloseWindow, removes the server window, unregisters inventory listeners and runs close events. Calling it after `probe.finish()` and callback cleanup preserves the successful transaction and native block close listeners. No inventory rollback and no container asset/UI replacement are introduced. Subsequent world-queued requests re-resolve their window section; a removed section cannot transfer or notify again. A currently open owned window also refuses withdrawal and closes if native pursuit began since opening.

There is **no universal native isPursued flag**. `NativePursuit` reads loaded NPC ECS components on demand on the world thread. A live NPC must have this exact player's `MarkedEntitySupport.LockedTarget` and an offensive state: Attack, Combat, Chase, Search, Converge (including substates), or Feran Threaten.Angry/Threaten.Converge. These cover inspected Kweebec Razorleaf, Feran Sharptooth, Trork and generic Skeleton combat trees. Idle, ordinary Alerted, idle watch targets, neutral Threaten, ReturnHome and hostile-memory lists alone do not count. Dead NPCs do not count. No distance cutoff is imposed on an already active target.

A live enabled queued native danger beacon for this exact offender also blocks during the handoff before the next AI evaluation: Kweebec_All_Danger, Feran_Warriors_Attack, TrorkEnemySighted, HyThief_Local_Theft. Reading/peeking does not consume, extend or change it. Its existing native lifetime is preserved. No heat timer, polling task, persistent cache, target writes or AI changes. Work is an on-demand scan of loaded NPCs per owned open/withdrawal attempt.

Official context: https://hytale.com/news/2026/2/npc-technical-rundown describes LockedTarget as the usual combat target, not a universal guarantee. https://hytale.com/news/2026/10/modder-facing-patch-notes-update-7 describes current native target switching. Installed 0.7.2 JAR signatures/bytecode and Assets.zip are authoritative for this implementation; both were read-only.

### Reliability boundary

This is a bounded adapter for the inspected native target/state architecture, **not an exact universal combat oracle**. Custom/scripted NPCs using other target slots, custom offensive state names, or position-only tracking after clearing their offender reference need an explicit verified profile/adapter. Do not assume hostility memory, a nearby hostile attitude or a generic busy flag proves pursuit; those would incorrectly block safe storage. No fallback timer was added. Custom mods which open windows directly without native UseBlock bypass the opening event; the supported native chest/wardrobe paths do emit it, and successful withdrawals remain gated. Real client escape and player-initiated damage must still be checked below.

## Changed production files

- src-java/com/local/pickpocket/NativePursuit.java — on-demand native engagement reader.
- src-java/com/local/pickpocket/OwnedContainerAccess.java — native cancellable pre-use gate.
- src-java/com/local/pickpocket/ContainerTheftService.java — post-success source-window close, stale request rejection and open-window pursuit check.
- src-java/com/local/pickpocket/PickpocketPlugin.java — registers the gate.
- build.gradle and manifest.json — development version only, target remains =0.7.2.

No NPC, witness, range, ownership schema, faction, loot, wardrobe production asset or pickpocket behavior changes. Recommended final version **1.4.2**, after Firebeard accepts this refinement; development identifier is not a public release.

## Verification

Clean Java 25 / Gradle 9.1.0 build passed. Existing two MerchantStoragePage removal warnings, existing InventoryUtils deprecation note and Gradle 10 deprecation remain. Native asset-parser warnings about pre-existing stealth filter fields remain; archive comparison proves every non-manifest asset byte-identical to approved public 1.4.1. Only the two intended existing classes and two new classes change behavior. Nested PickpocketPlugin record classes have line-table drift but identical javap instructions/signatures.

Native tests use the actual loaded plugin/classes in disposable server saves, with reflection restricted to the development proof plugin. They are not connected-client tests. NativeAccess2 and NativeWardrobeFinal cover real service dispatch: successful witnessed native transfer keeps item, exactly one caught message, closed server window/CloseWindow packet/close callback, second queued request rejected, two unseen withdrawals remain open/silent, already-open pursuit cannot withdraw. NativeWardrobeFinal additionally covers safe native deposits/rearrangement and a two-item smart withdrawal with one notification/closure. NativeChestAlone2 repeats the closure, pursuit and smart withdrawal checks using a native chest with Wardrobes absent. Native pursuit was observed after actual queued beacon consumption, Attack.Default and physical movement; target clearing/reset safety was also tested. The first smart test used native EquipOrMergeStack (merge/deposit semantics), so its withdrawal assertion was invalid; the harness was corrected to native PutInHotbarOrWindow, with no gameplay fix. Failed proof logs are retained.

Full existing native pickpocket matrix: 185 configured entries, 174 concrete spawnable roles, nine retired native roles, two abstract definitions; interaction trees and actual role identity passed.

## Installed test pair and rollback

HyThief candidate: `DevTesting/Container_Access_Refinement/Candidate/HyThief-1.4.2-access.1.jar`.
SHA-256: `16baa1097893ec371ce8f4c7aef67407a17f26736f8e9360ee0c69217c2f4880`.
Installed at `C:/Users/askin/AppData/Roaming/Hytale/UserData/Mods/HyThief-1.4.2-access.1.jar`, exactly one HyThief identity.
Wardrobes: unchanged public WonderousWardrobes_v1.3.0.zip (SHA-256 c1385fbdf253b90252a8de22beb46329fdbdd5dec3d64bec7e5c4ffd5b17e2d1), exactly one identity.
Prior installed HyThief 1.4.0, Wardrobes phase2b.1 ZIP and prior wardrobe reset pin are under `DevTesting/Container_Access_Refinement/Rollback`. Source rollback is `source-rollback-1.4.1.zip` and branch base commit 77e84a4d669165898f03bffe8b2b5de8f7360538. Public releases and archived v1.2 remain untouched.

Only `WonderousWardrobes/DevTesting/HyThief_Wardrobe_Phase2A/current-test-candidate.json` was updated outside HyThief: test artifact paths/hashes for the reset's existing safety guard. Wardrobes source, public package and fixture master were not changed. All 31 unrelated installed mod/control files were hash-verified unchanged.

Both actual existing desktop shortcuts were launched successfully: **Reset HyThief Container Test World** and **HyThief Wardrobe Test Reset**. Their scripts, shortcut targets and working directories are unchanged. Reset preserves dirty-save archives, refuses running clients/servers and validates installed version/revision. Container active equals unchanged 44-file master; wardrobe active equals unchanged 46-file master. No fixtures were redesigned.

## Firebeard runtime test — stop checkpoint

Close Hytale before either desktop reset. Both test worlds are now ready with no commands/setup.

1. Container world: RED unowned control — browse/deposit/rearrange/withdraw safely. BLUE or GREEN: crouch out of current six-block witness range or manually block LOS, withdraw twice; no caught message, no forced close. Reset.
2. BLUE/GREEN standing in clear sight: withdraw one item. Keep it; get exactly one existing caught message; container interface closes; native responder engages. Rapidly try a second withdrawal/reopen and another owned container: access blocked, no extra theft message/item. RED unowned storage must remain usable during pursuit.
3. Escape naturally, allow native pursuit/search to finish, return: owned storage opens with no timer. Reset separately and attack a responder directly, then try owned storage: same lockout independent of theft. Also smoke-check caught pickpocket followed by an owned-container opening attempt.
4. Wardrobe world: desktop **HyThief Wardrobe Test Reset**. Stand on green pad (0.5,64.1,1), open wardrobe, test browse/deposit/rearrange first, then withdraw. Keep item, one caught message, forced close, native Razorleaf pursuit and reopen lockout. Reset and repeat crouched (outside six-block sight): multiple withdrawals remain silent/open. Test upper and lower wardrobe clicks and multi-item smart transfer. Native proof covers player-placed wardrobe safety; confirm a player-placed storage block stays usable if testing that encounter manually.

Client-only gates: actual visible interface dismissal, real packet arrival timing under rapid clicks, upper-half wardrobe opening lockout, damage-triggered pursuit, caught pickpocket lockout and natural escape/restoration. Do not call these passed before Firebeard's report. No publication or follow-up gameplay work until that result.

Evidence and repeatable helper scripts: `DevTesting/Container_Access_Refinement`; final native runs and artifact/reset/deployment JSON records are preserved there.
