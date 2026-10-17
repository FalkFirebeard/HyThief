> Historical candidate draft. Superseded by [public 1.4.0 release notes](RELEASE_1.4.0.md).

# HyThief 1.4.0 — Container Theft (candidate)

Hytale target: `=0.7.2`. Not published; final client approval pending.

- Added witnessed theft detection for containers with persistent faction or local ownership.
- Opening, browsing, depositing and rearranging are safe; each successful withdrawal is evaluated independently.
- Witnesses must have clear line of sight, face the player, and be within 20 blocks standing or 6 crouched.
- Unseen theft is silent. Witnessed theft displays “You have been caught stealing!” once for that withdrawal.
- Responses use native NPC roles and combat. Unowned containers remain unrestricted.
- Existing pickpocket gameplay, loot, risk and normal merchant trade remain unchanged.

Automatic ownership is conservative: selected newly resolved Kweebec, Feran, Trork and Outlander loot-spawner tables. Previously generated unmarked chests are not backfilled. Skeleton/Zombie witnessing applies to explicitly owned containers; no generic dungeon ownership is inferred.

Goblin container support, Klop integration, prefab-instance ownership and other experimental systems are not included.
