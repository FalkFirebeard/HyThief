# Isolated native NPC social-alert diagnostics (0.7.2)

Research only. Not a HyThief release, not installed, and not a client test world. Read `../../Docs/NATIVE_NPC_AGGRESSION_SOCIAL_ALERT_0_7_2.md` before using results. `results.json` records nine accepted native runs, actual beacon events, artifact hashes and limitations. The older `Run-Skeleton_Fighter-flock` is invalid evidence; use `flock-once`.

`SocialAlertProbe.java` uses an NPC dummy surrogate, temporary per-target WorldSupport attitudes, native BeaconBroadcast, and optionally one-time FlockPlugin membership. It never sets NPC combat states, marked targets, movement or attack loops. Its 250-ms diagnostic snapshots stop after 28 scheduled seconds and emit only changes.

`Run-Probe.py` is self-contained with the adjacent Java source, current project dependency, shared JDK25 and installed Assets.zip. It verifies both server hashes against pinned 0.7.2 before doing anything. Run with the workspace Python:

- `Run-Probe.py setup` compiles this helper, not HyThief.
- `Run-Probe.py Goblin_Scrapper` (or Trork_Warrior / Outlander_Brute / Skeleton_Fighter / Zombie) uses one-source hostility acquisition.
- `Run-Probe.py Goblin_Scrapper beacon` or `Trork_Warrior beacon` delivers one native alarm without a hostile override.
- `Run-Probe.py Skeleton_Fighter flock-once` establishes one native flock, then triggers only the source.
- `Run-Probe.py Goblin_Scrapper config` temporarily loads four Friendly-default native template overrides and verifies construction of all five representative roles.

The runner aborts if a Run-* destination exists; preserve prior evidence instead of overwriting it. All shipped evidence directories already exist. For another run, supply a new optional fourth command-line argument, for example `Run-Probe.py Skeleton_Fighter flock-once repeat1`; prior evidence is never overwritten. It uses localhost port15578, a fresh flat save made from a **copy of only** the Container Master world config, no installed mod and no fixture pack. Native copied assets, binary helpers, reference dumps, full logs and world files remain ignored; do not commit or publish them. No native template copy is included in committed source.
