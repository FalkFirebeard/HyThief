# HyThief Container Theft Test World — container.10

Production ownership checkpoint 2: persistent NEARBY + FACTION, six native chests in a compact flat field. No commands or creator tools required.

Use the existing Reset Container Theft shortcut with Hytale closed. It restores this master and archives the dirty active save. Other test worlds are protected.

Stand on each brick pad when withdrawing one copper bar. Opening or browsing alone must do nothing. All stations use identical native Kweebec large chests with 16 copper bars and 8 iron bars.

Test order:

1. Station 1 CONTROL — no notification/reaction.
2. Station 5 KWEEBEC OWNED / KWEEBEC BLOCKED — no notification/reaction. Feran sees but is ineligible.
3. Station 6 FERAN OWNED / FERAN BLOCKED — no notification/reaction. Kweebec sees but is ineligible.
4. Station 3 KWEEBEC FACTION — one caught notification; Kweebec Razorleaf responds.
5. Close Hytale and reset.
6. Station 4 FERAN FACTION — one caught notification; Feran Sharptooth responds.
7. Close Hytale and reset.
8. Station 2 NEARBY — both witnesses eligible; exactly one caught notification and one chosen handoff. Current deterministic witness order selects the Feran when both see the same transfer.

Witnesses use scoped development roles derived from Kweebec_Sapling and Feran_Civilian, requesting no movement/turn motion. They are not Frozen. Native head rotation, visual range/FOV/LOS and role/interaction processing remain active. Responders are mobile. Sharptooth's field-only idle setup suppresses camp-leaving/sleep choices, and its attack-beacon receiver captures LastSeen using the same native StorePosition pattern as the existing Razorleaf fixture. Attack/chase/search logic is unchanged.

Container sight remains 20 standing / 6 crouched; normal HyThief perception remains 15 / 4. Use standing for this ownership checkpoint. Reset between caught tests to avoid combat contaminating another station.

Ownership and faction IDs are saved on each native container entity. The field marker controls diagnostic participation only, never container ownership. See Docs/PRODUCTION_OWNERSHIP_CHECKPOINT_2.md for the Java walkthrough and native validation evidence.
