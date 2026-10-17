# HyThief 1.4.0 — Container Theft

For **Hytale 0.7.2** (exact manifest target `=0.7.2`). Developed by **Falk Interactive Studios**, part of the Firebeard Archives.

- Added theft detection for containers with persistent faction or local ownership.
- Opening, browsing, depositing and rearranging are safe. Each successful withdrawal is evaluated independently.
- Witnesses must face the player and have clear line of sight within 20 blocks standing or 6 blocks crouched.
- Unseen theft is silent. Witnessed theft displays **"You have been caught stealing!"** once per withdrawal.
- Responses use Hytale's native NPC roles, alerts and combat. Unowned containers remain unrestricted by HyThief.
- Existing pickpocket gameplay, role-specific loot, risk, cooldowns and normal merchant trade are preserved.

## Initial production scope

Automatic faction ownership applies conservatively to eleven selected Kweebec, Feran, Trork and Outlander loot-spawner tables when they resolve into new containers. Previously generated unmarked chests are **not retroactively tagged**, and player-placed chest defaults remain unowned.

Container witnesses cover 33 ordinary roles across Kweebec, Feran, selected Trork/Outlander roles, Skeleton Fighter and Zombie variants. Other Skeleton classes and special/scripted roles are deferred. Existing authored NEARBY/LOCAL ownership remains supported; uncertain stock containers are left unchanged.

Goblin container support, Klop integration, prefab-instance ownership, sound-based detection, stolen-item tracking and forced container closure are not included. Existing native property/hostility behavior remains native.

## Install

Place **HyThief-1.4.0.jar** in your Hytale Mods folder, remove the previous active HyThief JAR, and enable HyThief for your world/server. Back up your save before upgrading. No separate third-party mod dependency is required.

Use toggle crouch to remain crouched while using the pickpocket menu. Crouching does not erase an NPC's existing awareness. Normal HyThief NPC perception remains 15 blocks standing / 4 crouched; container witnessing uses the separate 20/6 ranges.
