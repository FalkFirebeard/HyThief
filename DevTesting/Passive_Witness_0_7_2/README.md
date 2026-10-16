# Passive humanoid personal witness proofs

Development container.12, Hytale 0.7.2 only. No public release.

Open the new **HyThief Passive Witness Test** save; Trork Warrior is ready first. RED is unowned, GREEN owned NEARBY. Open/deposit/rearrange must stay safe. Standing visible withdrawal at GREEN must give one caught message and native witness combat. Close Hytale and use `Reset-Passive-Witness.cmd` to reset/select Goblin, Trork, Outlander, Skeleton, or Zombie. Repeat with primary LOS blocked. NPCs remain mobile; actual positions/FOV matter. Starting inventory includes 64 cobblestone.

All five exact representatives passed integrated native-server proofs; this is NOT connected-client confirmation. See `Docs/PASSIVE_HUMANOID_WITNESS_0_7_2.md` and `results.json` for scope, native entries, secondary observations and known warnings. No new group broadcasting. Existing worlds and public release are unchanged.

`Candidate/` holds the exact installed JAR, `Rollback-container.11/` source/JAR rollback, and `InstalledRollback/` the prior installed JAR. `Masters/` are generated local saves, not redistributed game assets. The reset launcher checks candidate/server hashes, backs up only the proof save, and refuses a changed Hytale patch.

Reproduction: run `Run-Personal-Proof.py setup` with the workspace Python/JDK, then one of the five role IDs and a new unique run label. It refuses to overwrite evidence. Add fourth argument `author` only to author a disposable fixture copy. `Prepare-Client-Fixtures.py` records the specific author runs used here and refuses master overwrite; preserve prior masters before any deliberate replacement. `Verify-Passive-Masters.py ROLE UNIQUE_LABEL` reloads a copy. Existing container regressions use `Verify-Container.ps1`; no public JAR is used.
