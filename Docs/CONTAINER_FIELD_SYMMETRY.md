# Container.11 test-field symmetry correction

2026-10-10. Geometry-only checkpoint; gameplay/source/assets and installed container.11 JAR unchanged. Preserve commit `f90bbcf` as gameplay and previous fixture-helper rollback. No build, deployment swap, push or public release.

## Measured geometry

World coordinates (X,Y,Z). Witnesses remain Kweebec (-4.5,64,0.5), Feran (4.5,64,0.5), both fixed and facing +Z with native head yaw magnitude pi. Center the forward interaction row on their midpoint X=0, retaining Z=12. Do not move the chest to the literal midpoint Z=0.5: that would no longer test both forward-facing witnesses.

| Measurement | Previous | Corrected |
|---|---|---|
| NEARBY native chest origin | (2,64,12) | (0,64,12) |
| NEARBY player interaction pad | (2.5,64,11.5) | (0,64,11.5) |
| Kweebec to player | 13.038405 | 11.884864 |
| Feran to player | 11.180340 | 11.884864 |
| Facing offset | unequal | mirrored 22.249024 degrees |

Both native FOV checks and actual native LOS checks pass at the corrected interaction point. Equal Y=64 and both distances are comfortably inside unchanged standing range 20; both are outside unchanged crouched range 6. Native chest layout remains exactly four origins: CONTROL (-6,64,12), KWEEBEC (-2,64,12), NEARBY (0,64,12), FERAN (6,64,12). Other stations intentionally remain unchanged: comparative LOCAL trials should use the centered NEARBY pad, not their unequal lateral positions.

NEARBY has a two-tile brick pad at X=-1 and X=0, Z=11. **Stand on the center seam X=0**, directly under the centered NEARBY label. Standing at either tile center introduces a half-block bias; this player-position difference is documented rather than ignored. All four chests retain serialized ownership metadata and 16 copper / 8 iron bars. Native opening and closing, unowned notification exclusion, faction witness filtering, one caught notification for witnessed owned removal, four-NPC count, 64 starting floor blocks, no permanent walls and mobile unfrozen responders pass development validation. Responders remain at mirrored positions (-19.5,64,-8.5) and (19.5,64,-8.5); open pathing is unchanged. Different native combat roles are intentional, not a fixture geometry difference.

## Evidence / reset / rollback

- Old-master rollback: `DevTesting/Container_Theft_Test_World/Rollback-symmetry-20261010-204928` (Master, helper sources, stations, README, original checksums, f90bbcf pointer).
- Successful disposable geometry-author run: `FactionVerificationRuns/20261010-205014-da037367c2e84749b97e014042079a8a/native.log`.
- Reload validation: `FactionVerificationRuns/20261010-205104-82868fb042164a389b2cb2ccb018be10/native.log`.
- Final reload including saved pad and label checks: `FactionVerificationRuns/20261010-205138-2662191608f546f889ebdce6a6882c33/native.log`.
- Only native saved chunk files were promoted from the successful author copy; master player inventory, NPC role assets, world configuration and other test worlds were not rewritten. Updated master marker records LayoutVersion=8 and exact NEARBY geometry.
- Existing reset script succeeded, archived prior active save at `Archives/20261010-205202-311f4f51984b4c6f93066b34ce5f301a`, and verified all 33 restored files against refreshed master checksums. Master unchanged by reset. Post-reset independent SHA256 comparison also passes.
- Installed JAR SHA256 remains `08ddb2033d942c901a52de3ef20429e83f21db7212bf676e5287f05b0123c233`. Exactly one HyThief JAR remains active. No gameplay repair or rebalance.
- Development-only verifier retains its existing getFiller deprecation warning. An initial disposable author attempt hit an ECS iteration write restriction; label replacements now execute after collecting references. The failed copy was never promoted.

## Repeatable fixture operation

`Verification/Verify-Faction-Field.ps1 -Symmetry -InputWorld <copy of old f90bbcf master>` applies the old-to-new move in a disposable copy, preserving inventory and ownership. Never reapply to the corrected master. Without -Symmetry the verifier only validates a disposable copy and checks persistence. The author helper now also creates the centered layout from scratch. Optional LOCAL response harness cases use the new center and mirrored temporary LOS-block coordinates; no permanent blockers are stored in Master.

## Standing comparative-fixture rule

Before runtime handoff, verify equal/comparable player-trigger distance, LOS, facing, elevation, access/pathing and trigger conditions. Measure the actual player interaction point, not just block origins. Document unavoidable asymmetry before testing. This standing rule is also recorded in project AGENTS.md.

## Firebeard retest

Launch HyThief Container Theft Test World with existing container.11. Use standing for LOCAL tests. Stand on NEARBY's center seam and withdraw one copper bar. Expect one caught notification and native LOCAL response from both ready responders. Reset between caught trials. To test each witness separately, manually block one witness's LOS and keep responder routes open; repeat after reset blocking the other. CONTROL and both FACTION stations retain their previous behavior. Real-client confirmation remains pending.
