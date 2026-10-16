# HyThief 1.4.0 production container candidate

Target `=0.7.2`; server revision `d769dc3d57881261a62858e1394f0e4de9a21eca`. Candidate SHA-256: `21b302c9e2b44fa519d8428db564d8aff6ec7c674596eb0f515fdc9f7fefc933`. Not published.

## Architecture

`PlayerReadyEvent` installs the proven two-packet native registration bridge for every player, with lifecycle cleanup. Player-only inventory changes and window-internal moves go directly to the original handler. Window-to-player moves run one world task, matching native scheduling/order, `PreventInventoryAccess`, `InventoryUtils` and utility active-slot event side effects. Smart moves retain native settings/defaults. No chest Use/UI override is added.

The live `ContainerBlockWindow` inventory must match its native block entity inventory. Only the persistent ownership component authorizes theft; there is no test registry or heuristic fallback. The established serialized component name remains `HyThiefOwnershipProof` to preserve existing saves. `ContainerKey` records world/block identity only.

`ContainerRemovalProbe` pairs actual successful source removal with player receipt. Fresh visual evidence is captured at removal; notification and reaction occur after callbacks unwind. Missing/invalid ownership, failed transfers, deposits and rearrangement do not qualify. Observation errors are logged and do not replay an already-started operation. One request produces at most one caught message even with multiple visible witnesses.

Native NPC groups bound production witnesses. `ContainerWitnessProfiles` selects the existing family handoff; `HyThiefPersonalWitnessAlert` posts one native rally to each direct Trork/Outlander witness or enters the proven native Combat.Default path for Skeleton Fighter/Zombies. No movement/attack loop or artificial social propagation is introduced. Kweebec/Feran retain the native guard adapter. NEARBY selects the existing 30-block LOCAL branch for compatible listeners, not a universal faction broadcast. Four private guard parent assets add LOCAL as an alternate sensor beside their original native danger sensor; all original actions, LastSeen, pursuit and combat remain intact.

Normal NPC/pickpocket perception remains 15/4. Container sight remains 20/6, native opacity LOS and existing 180-degree view sector. Existing awareness/combat memory is not weakened. Klop and Goblin behavior and unrelated projects are untouched.

## Packaging and rollback

Excluded from production compilation: `HyThiefContainerDiagnostics`, `DevContainerTestWorld`, `OwnedContainerRegistry`. Their development source remains available. No fixture markers, stationary aliases, admin diagnostics, test worlds or test helper plugins are packaged. Two native witness groups in the test-world asset pack opt in its stationary Kweebec/Feran aliases; those IDs never enter the gameplay JAR.

Public 1.3.3 comparison: all unchanged Java classes match except plugin lifecycle and the existing container-compatible guard adapter. Four guard JSON differences reduce exactly to alternate LOCAL sensors. Native spawner content reduces exactly to native JSON after removing the added component. **Flagged packaging-only drift:** 1,532 development LF resources differ from public 1.3.3 CRLF bytes; normalized bytes are identical. These existing development inputs were not reformatted by this task. Full lists: `DevTesting/Release_1_4_0/artifact-comparison.json`. No unexpected content differences were found.

Rollback: `DevTesting/Release_1_4_0/Rollback` preserves source snapshot of `11bffb10965b54dfb73124355a5526473015d2cb`, container.12 JAR, Master/Active worlds and reset inputs. Public 1.3.3 artifact remains separately preserved and hash-checked.

## Validation and remaining client gate

Native proof logs and repeatable helpers are in `DevTesting/Release_1_4_0`. Fresh-process native spawner proof verifies all 11 mappings and saved ownership. Native role capability proof covers all 33 configured witnesses and Goblin/Klop/wolf exclusions. Fixture checks verify 9 chests, 14 NPCs, exact original-area fingerprint, idle poses, primary/hidden-peer LOS, safe native open/close/deposit/rearrangement and persistent scope/faction data.

Final suite results, deployment/reset results and commit are recorded in `CANDIDATE_STATUS.json` alongside the candidate. These are offline native proofs with an NPC surrogate, not human client results. Connected-client packet ingress, notifications/UI and final gameplay feel must be confirmed by Firebeard before publication.

Client smoke test: use the existing desktop reset between caught scenarios. Check RED safe; BLUE Kweebec-only; GREEN both responders; YELLOW Feran-only. Check the Purple Trork, Cyan Outlander, White Skeleton and Black Zombie stations. Orange Goblin remains deferred and must not emit HyThief container caught responses. Open/browse/deposit/rearrange first, then stand on the pad and withdraw. Also check crouched unseen theft, a manually blocked witness and repeated withdrawals. Finish with front merchant trade, crouched rear pickpocket, one steal and deliberate caught/UI closure. Do not publish until these pass.

Build notes: the two pre-existing MerchantStoragePage inventory removal warnings remain. The production service also mirrors the native 0.7.2 handler’s deprecated `InventoryUtils.setActiveSlot` utility side effect; no behavior replacement was guessed. Gradle 9.1 retains its existing Gradle 10 deprecation notice. Explicit LF attributes now stabilize production text resources while saved-world files retain their -text protection. The legacy test-only merchant spawn marker is excluded from the final artifact; all other runtime bytes match the successful native suite.
