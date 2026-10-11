# HyThief Container Theft Test World - four-chest field

Installed gameplay: HyThief-1.3.1-container.11.jar.

Left to right: **1 CONTROL | 2 KWEEBEC | 3 NEARBY | 4 FERAN**. These are the only four chests/stations. NEARBY is near the row center. All are native Kweebec large chests, with 16 copper bars and 8 iron bars. Stand on the corresponding brick pad and withdraw one copper bar. Opening/browsing is safe.

- CONTROL: unowned; no caught notification or theft reaction.
- KWEEBEC: FACTION/Kweebec; matching visual witness alerts Razorleaf, not Sharptooth.
- NEARBY: any eligible visible theft-aware witness; exactly one caught notification and a generic LOCAL theft alert to both ready compatible responders, independent of the witness faction.
- FERAN: FACTION/Feran; matching visual witness alerts Sharptooth, not Razorleaf.

The stationary witnesses face the row. Both responders remain mobile in a flat field without shelters or permanent LOS walls. You start at the spawn pad with **64 Rock_Stone_Cobble blocks in hotbar slot 1**, matching the field floor.

For manual LOS tests, place opaque blocks between your player pad and the chosen witness. A blocked matching witness should not catch faction theft; the other faction must not substitute. For NEARBY, any unblocked eligible witness may still catch you. Keep responder pursuit routes open. Use standing for this ownership checkpoint.

Test CONTROL first, then one caught test per reset. Close Hytale and use the existing Reset Container Theft shortcut. It archives the active save and restores exactly these four chests, four NPCs, clean LOS, loot and your starting block stack. There are no stations 5/6 or legacy range stations.

Container sight stays 20/6, normal HyThief perception stays 15/4. Ownership, faction filtering, native chest interaction, caught notification, native pursuit/combat, LastSeen and pickpocket logic remain unchanged. NEARBY now selects a generic native LOCAL alert; FACTION retains its faction alert. Firebeard's real-client test is still required.
