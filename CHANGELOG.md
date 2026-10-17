## 1.4.2 — Container Access & Pursuit

Witnessed theft closes the container after completing the transfer. Recognized native pursuit blocks owned-container opening with one denial message per attempt; access returns when pursuit ends. Unowned storage stays accessible. Optional Wonderous Wardrobes compatibility continues; custom pursuit-state limitations remain.

## Development compatibility — Hytale 0.7.0

Container.11 is preserved and updated for 0.7.0. Public compatibility candidate 1.3.1 is isolated from v1.3.0; see Docs/UPDATE_0_7_0_COMPATIBILITY_AUDIT.md. No public container release is approved.

# HyThief 1.3.0

Falk Interactive Studios

HyThief 1.3.0 restores compatibility with Hytale 0.6.8 and brings crouch-aware pickpocketing to the full supported NPC role matrix.

- Restored current interaction, inventory, UI and NPC API compatibility.
- Supports 185 configured NPC role IDs: 183 concrete roles and two abstract Bramblekin definitions.
- Crouching reduces fresh Idle-state acquisition across supported sight, scent and proximity paths. Native hearing, special states and environmental stimuli remain active.
- Crouching after detection does not clear an NPC's awareness or retained target.
- Preserves role-specific loot, escalating theft risk, cooldowns, merchant trade and caught-menu closure. Kweebec merchants can signal a nearby eligible Razorleaf guard through native danger messaging. Other consequences remain role-dependent.
- Removed temporary diagnostic logging and test observation tasks from the shipped plugin.

Requires Hytale **0.6.8**. The plugin declares `ServerVersion: =0.6.8` and uses the built-in Hytale NPC plugin; no separate third-party dependency is required.

Install `HyThief-1.3.0.jar` in your Hytale Mods folder, remove older active HyThief JARs, and enable the mod for your world or server. Back up your world before upgrading.

Approach a supported NPC from behind while crouched, open Pickpocket, and choose how much risk to take. Some NPC states can still detect you through native behavior.

Representative gameplay testing and the revamp.11 matrix validation support this release. We have not manually tested every NPC encounter. Please report issues through the CurseForge project comments with the NPC role, Hytale version, other installed mods, steps to reproduce and relevant log excerpt.
