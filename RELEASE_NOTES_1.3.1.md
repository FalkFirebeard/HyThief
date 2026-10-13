# HyThief 1.3.1 — Hytale Update 7 compatibility

Compatibility patch for stable Hytale **0.7.0**. Requires exact server target `=0.7.0` and the built-in Hytale NPC plugin.

- Updated the pinned build dependency and manifest to stable 0.7.0.
- Adapted the existing Pickpocket/stealth composition to the redesigned native Goblin Lobber, Miner, Scrapper and Thief roles.
- Prevented obsolete packaged overrides from recreating nine Goblin IDs removed by Hytale. Their historical resolver/loot configuration remains available; no loot pools or probabilities were redesigned.
- Preserves the existing pickpocket menu, loot/risk/cooldowns, crouch gate, merchant trade and native Kweebec caught response.

This patch contains the existing public 1.3.0 feature set. Experimental owned-container theft is not included.

Native validation passed 174 concrete supported roles and two abstract definitions. Nine historical configured IDs are no longer native roles. Runtime UI/input, stealing/caught and fresh crouched Goblin acquisition require the final client smoke test before publication.
