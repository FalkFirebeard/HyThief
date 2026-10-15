# HyThief — NPC Pickpocketing

**HyThief 1.3.3 is compatible with Hytale 0.7.2.**

HyThief adds NPC pickpocketing built around stealth, positioning, and risk versus reward. Approach a supported NPC from behind while crouched, press your normal Interact key, and choose individual items from the Pickpocket menu. Each theft has a chance of detection, and taking more items increases the risk.

- Crouched rear interaction with supported NPCs.
- Individual item theft and role-specific loot.
- Escalating risk, cooldowns, and caught notifications.
- Clean menu closure on Escape or when caught.
- Native, role-dependent NPC reactions; Kweebec merchants can alert a nearby eligible Razorleaf guard.
- Normal merchant trade remains available from the front, standing or crouched.
- Crouch-aware stealth for fresh encounters. Crouching does not erase an NPC's existing awareness.

NPC role support retains the current Goblin compatibility updates. This is a compatibility update: existing public pickpocket gameplay, loot and risk behavior remain unchanged. Native hearing, special states and environmental stimuli may still reveal a player.

HyThief is actively being expanded with new theft and crime-related features planned for future updates.

## Install

Download **HyThief-1.3.3.jar**, place it in your Hytale Mods folder, remove older active HyThief JARs, and enable it for your world/server. Requires **Hytale 0.7.2**, with exact manifest target `=0.7.2`. No separate third-party mod dependency is required. Back up your world before upgrading.

Use **toggle crouch** if you want to remain crouched while using the menu. Holding and then releasing the crouch key makes you stand normally.

## Compatibility and feedback

The unchanged public gameplay passed prior real-client testing; current Hytale 0.7.2 native checks passed pickpocket role/interaction compatibility and the caught-to-Razorleaf handoff. Native validation covered 174 concrete roles and two abstract definitions. Nine historical configured role IDs were removed by Hytale and their obsolete overrides are no longer packaged. Every encounter has not been manually tested.

Report issues through [GitHub issues](https://github.com/FalkFirebeard/HyThief/issues) or [CurseForge comments](https://www.curseforge.com/hytale/mods/hythief/comments), including the NPC role, game version, other mods, reproduction steps, and relevant log excerpt.

[CurseForge](https://www.curseforge.com/hytale/mods/hythief) · [Release downloads](https://github.com/FalkFirebeard/HyThief/releases)

## Build the public release

Check out **v1.3.3** or the **compatibility/1.3.3** branch. Use Java 25 and the pinned Hytale 0.7.2 dependency; run `./build.ps1`. See Docs/BUILD_ENVIRONMENT_RELEASE.md. Main is ongoing development; use the release tag to reproduce the public JAR. Game binaries/assets are not redistributed.

Developed by **Falk Interactive Studios**. Part of the Firebeard Archives.
