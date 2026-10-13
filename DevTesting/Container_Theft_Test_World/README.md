# HyThief Container Theft Test World - mirrored color field

Gameplay remains **HyThief-1.3.1-container.11.jar**, target =0.7.0. The fixture changed; no gameplay build or rules changed.

Use the colored floor pad immediately in front of each identical native chest:

- **RED: CONTROL / UNOWNED** - safe, no theft notification/response.
- **BLUE: FACTION / KWEEBEC** - matching Kweebec witness / Razorleaf response.
- **GREEN: NEARBY** - either eligible visible civilian witness / LOCAL response from both ready responders.
- **YELLOW: FACTION / FERAN** - matching Feran witness / Sharptooth response.

Four chests lie along the center line X=0 at Z=-6,-2,2,6. Each color pad spans two floor blocks; stand on its center seam X=0 at Z=-6.5,-2.5,1.5,5.5 respectively. Labels use colors and ownership names, not station numbers. GREEN is near the useful field center. Walk alongside the line to see each marker, then center yourself on that colored pad. No commands needed.

Spawn (0,64.1,10.5) faces the chest line. Both civilian witnesses are stationary at mirrored (-7.5,64,0.5)/(7.5,64,0.5), facing inward. Mobile responders sit farther behind them at (-21.5,64,0.5)/(21.5,64,0.5). Every pad is equally distant from both witnesses; both LOS/FOV checks pass. All witness-to-responder alert distances are 14 or 29 blocks, within unchanged LOCAL range 30. Both responders are farther than 20 blocks from every pad. No shelters/permanent walls.

Start with 64 Rock_Stone_Cobble blocks in hotbar slot 1, matching the field floor. Manually block civilian LOS as required; keep responder routes open. Use standing for alert-scope comparisons. Each chest holds 16 copper / 8 iron bars; withdrawing one copper bar is enough. Opening/browsing alone is safe.

For GREEN, test both visible, then each witness blocked separately, resetting between caught trials. Each confirmed theft should produce one caught message and both native responders. RED remains safe; BLUE/YELLOW retain faction isolation. Close Hytale and use the existing Reset Container Theft launcher; it archives the active save and restores this exact clean field, loot, spawn, colors and starting blocks.

Container visual range 20/6 and normal perception 15/4 are unchanged. Exactly four chests/four NPCs remain. The previous master and active save are preserved in Rollback-color-field-20261012-163359. See Docs/COLOR_FIELD_AND_NATIVE_PREFAB_REVIEW.md for measured geometry, native validation evidence and research-only PREFAB conclusions.
