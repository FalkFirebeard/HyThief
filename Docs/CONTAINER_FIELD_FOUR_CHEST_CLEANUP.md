# Final four-chest Container Theft Test World

2026-10-10. Supersedes the six-station field and its manual stations 5/6.

The field was rebuilt from an empty copied universe rather than deleting selected legacy chests. Only four native chests exist: CONTROL (-6,64,12), KWEEBEC (-2,64,12), NEARBY (2,64,12), FERAN (6,64,12). Ownership is false for CONTROL; Kweebec/Feran use persistent FACTION metadata; NEARBY uses persistent NEARBY metadata. Labels show exactly this order. No old labels or ownership/range stations were carried over.

Exactly four native NPCs were recreated with the unchanged test-role assets: fixed Kweebec/Feran witnesses at (-4.5,64,0.5)/(4.5,64,0.5), yaw pi toward the row; mobile Razorleaf/Sharptooth at (-19.5,64,-8.5)/(19.5,64,-8.5). No permanent LOS walls, shelters or blocked stations. All four chests are within forward sector and clear LOS of both witnesses.

Firebeard starts at (0.5,64.1,7.5), with 64 Rock_Stone_Cobble blocks in first hotbar slot, matching the floor. The test-save player data was seeded from Firebeard's previously archived save, with clean test inventory/position. It is local save data, not a production plugin hook. Native PlayerStorage decoding verifies this stack.

Disposable native validation counts chest origins across all six loaded authored chunks (ignoring native multi-block filler cells), checks exactly four NPC roles, forward head rotation/stationary witness state, non-Frozen responders, ownership metadata, native chest open/close inventory identity, paired withdrawal attribution, faction selection and caught notifications. The reset retains original world identity and verifies every master file hash. Real-client visibility, manual placement and combat remain Firebeard's runtime checkpoint.

No gameplay Java or deployed JAR changes. container.10 hash remains 66381750f9491adea42f3ffd8a4af533bca5c3ce7653e686340a0697205f1642. Main role assets and world-local fixture packs are unchanged. Original full rollback: Rollback-four-chests-20261010-193635. Rebuild run:
E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\Container_Theft_Test_World\FactionVerificationRuns\20261010-194515-78115c2047bf41bdae60e9401e4c9307

Fresh reload passed in FactionVerificationRuns/20261010-194759-71f4386123684d14840ff4b227493648: four origins (including native CloseWindow variants), exactly four NPCs, decoded 64-block starting hotbar, all four persistent ownership/withdrawal/notification checks. Active reset verified 33 files against master hashes. Other save metadata, installed container.10 JAR, and both world-local fixture ZIP hashes are unchanged. Reset archive: Archives/20261010-194838-30be11566e4e4eae82bec3dc53a17523.
