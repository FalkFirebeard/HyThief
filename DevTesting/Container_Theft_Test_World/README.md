# HyThief Container Theft Test World — current client fixture

Use the installed **HyThief-1.3.1-container.12.jar**, target **=0.7.2**. This fixture expansion did not rebuild/change gameplay. Do not pair this ownership save with public HyThief.

The original area is preserved:

- RED: UNOWNED control.
- BLUE: Kweebec FACTION.
- GREEN: NEARBY / LOCAL, both responders.
- YELLOW: Feran FACTION.

Original chest seams remain X=0, Z=-6.5,-2.5,1.5,5.5. Spawn remains (0,64.1,10.5), with 64 cobblestone blocks for temporary LOS walls. Witnesses/guards and original labels/colors are unchanged.

Follow the gray path behind spawn to five new sections:

| Color | NPC | Chest X,Y,Z | Ownership | Initial witness distance |
|---|---|---|---|---|
| ORANGE | Goblin Scrapper | -36,64,48 | FACTION Goblin | 8 |
| PURPLE | Trork Warrior | 0,64,48 | FACTION Trork | 8 |
| CYAN | Outlander Brute | 36,64,48 | FACTION Outlander | 8 |
| WHITE | Skeleton Fighter | -18,64,88 | FACTION Skeleton | 8 |
| BLACK | Zombie | 18,64,88 | FACTION Zombie | 8 |

Each new native chest has 16 copper / 8 iron bars. Stand on its colored center seam, half a block before chest Z. All five use the proven world-local native Friendly setting, retain AI/perception/combat, and have no extra peers. There are nine chests and nine NPCs total. Nearby stations cannot witness each other's theft at reset positions; ownership also filters by native family. The five new witnesses now hold their exact position and facing while passive/idle; native combat movement is allowed after hostility begins.

**Start at PURPLE Trork**, straight along the path. Opening/browsing/deposit/rearrangement should be safe. Standing visible withdrawal should give one caught message and native attack/chase. Reset between caught tests. For a blocked-LOS case, use the starting blocks to build a sufficiently tall temporary wall between the witness and chest; reset removes it.

**Known container.12 Goblin limitation:** IdleEat.Default is rejected by the existing personal adapter even when visual witnessing succeeds. Two native runs reproduced this; no gameplay change or Goblin combat-state repair was made. The idle motion hold does not prevent IdleEat or bypass that gate. Record the Goblin result/state and any weapon/animation problem for the next repair review. The other four new profiles passed native FACTION combat tests. Connected-client confirmation is pending.

Use the existing desktop **Reset HyThief Container Test World** shortcut. It still runs Reset-Container-Theft.cmd/.ps1, archives the dirty active save and restores the current 0.7.2 master with all nine stations, loot, ownership, starting inventory and original spawn. No extra world or shortcut is required. Actual desktop reset plus native-load/geometry validation passed. Container sight 20/6, normal perception 15/4, existing faction/LOCAL/guard behavior remain unchanged.

Full note: Docs/CONTAINER_PASSIVE_CLIENT_FIELD_0_7_2.md. Evidence/tools and rollback: Passive_Client_Expansion_0_7_2/.

Stationary fixture correction: `Docs/CONTAINER_PASSIVE_STATIONARY_FIXTURE_0_7_2.md`. All five held exact starting poses for 60 seconds; Purple Trork still moved and attacked natively after witnessed theft. The actual desktop reset restores these poses. Rollback/evidence: `Stationary_Witness_0_7_2/`.
