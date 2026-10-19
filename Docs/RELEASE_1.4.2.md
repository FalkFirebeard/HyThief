# HyThief 1.4.2 — Container Access & Pursuit

For Hytale 0.7.2 (exact target `=0.7.2`).

- Getting caught stealing from an owned container now closes its inventory immediately after the successful transfer. You keep the stolen item; extra queued withdrawals are rejected.
- Owned containers cannot be opened during recognized native NPC pursuit, whether caused by theft, pickpocketing, attacking an NPC or another native trigger.
- Denied opening attempts display **"You cannot access this container while being pursued."** once per attempt.
- Owned-container access returns automatically when native pursuit ends. No heat timer is added.
- Unowned and player-placed storage remains accessible during pursuit.
- Compatible with HyStash (previously known as Wondrous Wardrobes): supported owned wardrobes use the same native storage and ownership rules. Neither mod requires the other.

Existing pickpocket gameplay, native NPC combat, ownership, witness rules, ranges and wardrobe storage are unchanged. Custom NPCs using unsupported pursuit states/target slots or position-only tracking may need a verified adapter. Pre-existing unmarked containers are not migrated.

Falk confirmed the new access mechanics and notification in-game. Native regression checks cover chest and wardrobe transactions, notifications, native guard handoff and the supported pickpocket matrix.

Back up your save, remove the previous active HyThief JAR, and install **HyThief-1.4.2.jar**. HyStash is optional and is not included.

Developed by Falk Interactive Studios.
