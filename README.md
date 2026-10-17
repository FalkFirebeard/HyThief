# HyThief — Pickpocketing & Container Theft

**HyThief 1.4.1 — Pickpocket UI Hotfix** is compatible with **Hytale 0.7.2**, exact target `=0.7.2`.

HyThief adds stealth-focused pickpocketing and witnessed theft from owned containers. Positioning, visibility and player choice determine whether you get away with it.

## NPC pickpocketing

Crouch behind a supported NPC, press your normal Interact key, and select individual items from the Pickpocket menu. Each theft carries a chance of detection; taking more items increases the risk.

- Role-specific loot, escalating risk and cooldowns.
- Caught notifications and clean menu closure on Escape or when caught.
- Native, role-dependent NPC reactions; Kweebec merchants can alert an eligible nearby Razorleaf.
- Normal front merchant trade works standing or crouched.
- Crouch-aware fresh acquisition; crouching does not erase existing awareness.

## Container theft — new in 1.4.0

Opening, browsing, depositing and rearranging are safe under HyThief. Removing items from an owned container triggers a fresh witness check for each successful withdrawal.

- Persistent **FACTION** and **NEARBY/LOCAL** ownership.
- Witnesses need clear LOS, correct facing and range: **20 blocks standing / 6 crouched**.
- Unseen theft succeeds silently; witnessed theft gives exactly one **"You have been caught stealing!"** notification per withdrawal.
- Hytale's native NPC behavior handles alerts, pursuit and combat. No universal HyThief faction broadcast is added.
- Unowned containers remain unrestricted by HyThief.

Automatic ownership is deliberately conservative: eleven selected Kweebec, Feran, Trork and Outlander loot-spawner tables stamp ownership on newly resolved chests. **Previously generated unmarked chests are not backfilled.** Player-placed chest defaults remain unowned.

Initial container witnessing covers **33 ordinary roles**: 7 Kweebec, 5 Feran, 7 Trork, 6 Outlander, Skeleton Fighter and 7 Zombie variants. [Exact role list and content coverage](Docs/CONTAINER_THEFT_1_4_0_CONTENT_AUDIT.md). Goblin container support, Klop integration, prefab-instance ownership and special/scripted roles are deferred. Existing native property/hostility behavior is preserved. Existing pickpocket support is unchanged.

## Install

Download **HyThief-1.4.1.jar** from [GitHub releases](https://github.com/FalkFirebeard/HyThief/releases/latest) or [CurseForge](https://www.curseforge.com/hytale/mods/hythief). Place it in your Hytale Mods folder, remove older active HyThief JARs, and enable it for your world/server. Requires **Hytale 0.7.2**. No separate third-party mod dependency is required. Back up your world before upgrading.

Use **toggle crouch** to remain crouched while using the menu. Releasing a held crouch key makes you stand normally. Normal NPC perception remains 15/4; container witnessing uses 20/6.

## Compatibility and feedback

Native validation passed the container ownership/transaction/witness/response pipeline, persistence across unload/reload and fresh restart, and the public pickpocket matrix of 174 concrete roles plus two abstract definitions. Nine historical configured IDs are retired by Hytale. Every encounter has not been manually tested.

Report issues through [GitHub issues](https://github.com/FalkFirebeard/HyThief/issues) or [CurseForge comments](https://www.curseforge.com/hytale/mods/hythief/comments), including NPC role, Hytale version, other mods, reproduction steps and relevant logs. HyThief remains in active development.

## Build this release

Check out **v1.4.1**. Use Java 25 and the pinned Hytale 0.7.2 server dependency, then run `./build.ps1`. See [build instructions](Docs/BUILD_ENVIRONMENT_RELEASE.md). Main continues development; use the release tag to reproduce public source. Game binaries and test-world/helper assets are not redistributed in the JAR.

[Release notes](Docs/RELEASE_1.4.1.md) · [CurseForge](https://www.curseforge.com/hytale/mods/hythief) · [GitHub releases](https://github.com/FalkFirebeard/HyThief/releases)

Developed by **Falk Interactive Studios**. Part of the Firebeard Archives.
