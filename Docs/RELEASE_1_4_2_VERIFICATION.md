# HyThief 1.4.2 release verification

Hytale =0.7.2, revision d769dc3d57881261a62858e1394f0e4de9a21eca. Falk explicitly confirmed successful client testing of 1.4.2-access.2 mechanics and notification and authorized 1.4.2 publication on 2026-10-17. That confirmed development checkpoint 027d61f is preserved in development/container-access history. The 1.4.1 prompt-text hotfix remains included; older releases are preserved.

Final clean Java 25 / Gradle 9.1.0 build passed against pinned server SHA-256 23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff. Only manifest Version differs from the exact client-verified access.2 artifact: every production class and asset is byte-identical. No test helpers, test saves, source files or desktop launchers are packaged. Public manifest has no third-party dependency; native Hytale:NPC dependency remains.

Final production JAR native validation passed:

- Native owned chest with HyThief alone and owned wardrobe with unchanged public Wardrobes 1.3.0.
- Exact denial notification; once per event; same-event redispatch silent; fresh denied event notifies again.
- Unowned/player-placed and restored access silent. Native target clearing restores access with no timer.
- Actual loaded theft-service direct/smart transfers, successful item retention, one caught notification, native CloseWindow/listener cleanup, stale queued requests rejected.
- Two unseen withdrawals remain open/silent; deposits/rearrangement remain safe.
- Pending native danger delivery and subsequent native guard Attack.Default movement/pursuit remain intact.
- Full NPC matrix: 185 configured, 174 concrete spawned, nine retired, two abstract definitions; actual role identity and interaction trees preserved.

Existing compiler/deprecation and native stealth-filter parser warnings remain; no new API/asset repair. NPC AI, witness policy, ownership, faction rules and sight ranges were not changed by release finalization. Custom state names/target slots or position-only tracking remain outside the bounded native pursuit adapter; no heat timer or universal pursuit claim.

Candidate: DevTesting/Release_1_4_2/Candidate/HyThief-1.4.2.jar
SHA-256: a90198328c2cd16be4c119f5d489233524cbeea709ad9f7ec9a0158da7c08642

Final native evidence and download/deployment/reset verification records are retained in DevTesting/Release_1_4_2. Public and development branch synchronization and distribution IDs are recorded in RELEASE_1_4_2_PUBLICATION.md after publication. Wardrobes source and public artifact remain unchanged; only the approved HyThief test artifact pin may update. Both master worlds and reset scripts/shortcuts are preserved.
