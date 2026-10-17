# Pursuit denial notification — access.2

Development checkpoint 2026-10-17, Hytale =0.7.2 / revision d769dc3d57881261a62858e1394f0e4de9a21eca. Baseline development/container-access at 00eeb5e, clean before work.

`OwnedContainerAccess.handle` now sends `PlayerRef.sendMessage(Message.raw(...))`, matching the existing caught-message presentation, after the existing pursuit/ownership gate denies native UseBlockEvent.Pre:

> You cannot access this container while being pursued.

The event is canceled before sending. Its existing isCancelled early return prevents duplicate messages when the same canceled event is dispatched again. A fresh denied event sends a fresh message; no timer, player cache or throttle. Native UseBlock cancellation fails that interaction before executing its container-open branch. The notification is only in the open-denial gate, not in withdrawal observation or guard/crime delivery.

Production changes: `src-java/com/local/pickpocket/OwnedContainerAccess.java` (message only), `build.gradle` and `manifest.json` (development iteration 1.4.2-access.2). No other production implementation changed. In particular, NativePursuit, ContainerTheftService and ContainerCaughtNotification bytes are identical to access.1. The exact caught text remains `You have been caught stealing!`.

Clean Java 25 / Gradle 9.1 build passed. Existing warnings unchanged. Strict comparison: only OwnedContainerAccess.class and manifest.json differ from access.1; no added/removed entries, every other class/asset byte-identical. Focused native-server checks passed for owned wardrobe with both mods and native chest with HyThief alone. Both exercise exact denial text, first attempt=1 message, canceled event redispatch=0 new messages, new attempt=1 new message, unowned/player-placed silence, and restored-access silence. Existing real-service direct/smart withdrawal, one caught message, item retention, window closure, stale queued-request rejection, unseen withdrawal, safe deposit/rearrangement and native guard movement checks also passed. These are disposable native tests, not connected-client rendering tests. No need to repeat unchanged full NPC matrix; it passed in access.1 and all role assets and other classes remain byte-identical.

Artifact: `DevTesting/Container_Access_Refinement/Notification/Candidate/HyThief-1.4.2-access.2.jar`.
SHA-256: `3c17823c58cde486394fcccfc7750b4e9e22ac1c6f7001f784e932bacc194de4`.
Installed: `C:/Users/askin/AppData/Roaming/Hytale/UserData/Mods/HyThief-1.4.2-access.2.jar`, exactly one HyThief identity. Prior installed access.1 backed up under Notification/Rollback. All 32 unrelated installed/control files unchanged, including public Wardrobes 1.3.0.

Explicit user approval allowed updating only `WonderousWardrobes/DevTesting/HyThief_Wardrobe_Phase2A/current-test-candidate.json`: HyThief path/hash/version. Its prior copy is in Notification/Rollback. Wardrobes source, production assets, reset scripts and master remain unchanged. Both existing actual desktop shortcuts were run: Reset HyThief Container Test World (44 master files) and HyThief Wardrobe Test Reset (46). Both active worlds exactly match unchanged masters, and dirty saves were archived by their existing reset scripts. Script/shortcut/public artifact hashes were verified unchanged. No publication, public-main changes or editor development.

## Firebeard test

Close Hytale before resetting. Both worlds are already reset and ready.

1. Chest world, BLUE or GREEN: stand visibly in front of a witness, withdraw one item. Keep item, get one caught message and forced closure. While the responder pursues, press Interact on that owned chest: exactly one `You cannot access this container while being pursued.` message, no opening. Release and press Interact again: one additional denial message. RED control stays usable without the denial message.
2. Escape naturally and let native pursuit finish. Owned storage should reopen without the denial message. Check native attack-triggered pursuit likewise if not yet client-verified from access.1.
3. Desktop HyThief Wardrobe Test Reset, then wardrobe world: stand on green pad, withdraw standing, then attempt reopening during Razorleaf pursuit. Same one-per-attempt message. Reset and withdraw crouched outside witness range: no caught/denial message and no forced closure. Check upper/lower clicks.

Remaining limitations are unchanged from access.1: loaded native LockedTarget plus verified offensive states/pending danger signals; custom target slots/state names need a verified adapter. Human visible text, actual input repetition and natural escape remain client gates. Stop for Firebeard results.

Local evidence and reusable proof copies: `DevTesting/Container_Access_Refinement/Notification` (clean-build.log, artifact-comparison.json, NoticeWardrobe-result.json, NoticeChest-result.json, deployment.json, desktop reset logs, final-verification.json).
