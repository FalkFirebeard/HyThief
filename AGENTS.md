# HyThief project rules

Continue following `E:\Hytale adjustments\AGENTS.md`.

## Standing comparative test-world geometry rule

Before handing off any comparative multi-faction/NPC fixture, measure or verify equal/comparable distance, LOS, facing, elevation, access/pathing, and trigger conditions. Check the player interaction point, not merely block origins. Avoid accidental faction geometry advantages. When perfect symmetry is impossible, document the difference before runtime testing. Preserve reusable test assets and use reversible resets.

## Standing test-infrastructure check after every Hytale update

Before handing any test world to Firebeard after any update/hotfix/patch:

1. Read the actual installed Hytale version and revision.
2. Check the relevant mod build against that installation.
3. Check active-world and master metadata/assets for stale active targets.
4. Check reset scripts and desktop shortcuts point to current fixed masters/paths.
5. Verify removed/renamed NPC roles, blocks, prefabs and assets do not break the fixture.
6. Run the intended isolated reset, preserving rollback backups.
7. Verify every active file matches the intended master after reset.
8. Run appropriate native/load validation in disposable copies where practical, paired with the correct mod build.
9. Only then hand the world to Firebeard.

Do not blindly rewrite fixtures after patches. If nothing relevant changed, record validation and preserve the fixture. Never load a container-development save with public HyThief, and never invoke historical combined/cross-project reset scripts from active shortcuts.

## Standing public/development patch workflow

Whenever Hytale updates: read actual version/revision; update and test the public release line; publish only when approved; immediately align active development compatibility; migrate or validate active native save formats, masters and fixtures; check every embedded ZIP/JAR/folder manifest; verify reset scripts and actual desktop shortcuts; verify visible colors/labels and starting inventory; run reset, native validation and client-ready handoff before new feature work. Do not leave world migration for later. Native codec versions need not equal the game version: compare fresh native saves and current codecs rather than inventing version numbers. A checksum or custom fixture target does not prove client compatibility. Require client visual confirmation when only the client can establish warning removal.
