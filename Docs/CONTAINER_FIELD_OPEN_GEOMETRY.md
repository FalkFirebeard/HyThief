# Container ownership field: open geometry correction

2026-10-10. Supersedes the prebuilt occlusion/shelter layout described in PRODUCTION_OWNERSHIP_CHECKPOINT_2.md.

Removed the two responder shelters (X +/-17..22, Z -10..-5, Y 64..67) and the two witness LOS walls (X -8/7, Z 5..6, Y 64..67). Existing witnesses, responders, native containers and serialized ownership were retained rather than respawned/reseeded. All six stations have clear witness LOS by default; stations 5 and 6 now require Firebeard's manually placed blocks for exclusion/occlusion testing. Labels and test instructions reflect this.

The field has an unobstructed flat floor and no above-ground obstacles other than native chest cells. Both responders remain non-Frozen and use unchanged native combat paths. Only the disposable author/validation helper and world geometry/labels/documentation changed; production Java, role assets, ownership policies, notification, ranges and installed JAR remain unchanged.

Installed JAR: HyThief-1.3.1-container.10.jar, SHA256 66381750f9491adea42f3ffd8a4af533bca5c3ce7653e686340a0697205f1642.

Native validation checks all six clear LOS/FOV stations, source metadata after reload, paired withdrawals/notifications, native chest open/close inventory identity, stationary witnesses and non-Frozen responders. Real-client pursuit remains the runtime confirmation checkpoint.

Geometry validation run:
E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\Container_Theft_Test_World\FactionVerificationRuns\20261010-192926-d3d488e733974b4e8d319bcb1871a7ce

Rollback: Rollback-open-field-20261010-192730 retains the previous full master and tooling. Reset uses unchanged original scripts and checksum verification.

Native response validation also passed: Feran Attack.Ranged / 11.93 blocks moved, Kweebec Attack.Default / 10.89 blocks moved, exact offender target and no wrong-faction target in both disposable runs. Combat-mutated saves were not promoted to Master.
