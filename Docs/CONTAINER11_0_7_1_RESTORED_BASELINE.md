# Restored container.11 / Hytale 0.7.1 baseline — 2026-10-14

**Superseded for client readiness by CONTAINER_WORLD_NATIVE_0_7_1_CORRECTION.md.** The earlier native mechanics tests passed, but this audit missed the =0.6.8 target inside the embedded ContainerFactionField ZIP. Its readiness claim did not establish disappearance of the client warning. No PREFAB implementation or publication performed.

## Active build and preservation

- Installed `C:\Users\askin\AppData\Roaming\Hytale\UserData\Mods\HyThief-1.3.1-container.11.jar`, version **1.3.1-container.11**, exact target **=0.7.1**; SHA-256 `f2dca782e22c38839640e8886f764b16ca00c36cdf8b4c812d6c099181eae08c`.
- Installed Hytale **0.7.1**, revision **69c03ffa58d7e2f46bce8b0356ed206cfc7f10e6** verified before restore. Exactly one HyThief JAR identity active.
- Previous public 1.3.2 installation backed up under **DevTesting/Container11_Baseline_0_7_1_20261014/InstalledRollback/**; archived official and approved candidate copies remain separate and unchanged (SHA-256 **38c3d5e464c26c1bbca01a4d57424d349b5bb45dca3b502b1937ac015bd16938**).
- No JAR rebuild, gameplay/API/asset/config change. Other installed mods and other worlds/projects were hash-checked and left untouched.

## Exact master/active fixture

Actual desktop **Reset HyThief Container Test World.lnk** was ShellExecuted with its current target **DevTesting/Container_Theft_Test_World/Reset-Container-Theft.cmd** and matching working directory, blank arguments. It calls only the adjacent isolated reset, validates the current =0.7.1 master and preserves the dirty active save in Archives. The pre-reset active copy had **9 changed files**; reset restored all **33 files**. Master/active hashes match after the completed disposable tests. The reset now reads the installed server version/revision and refuses a master with mismatched fixture target or validation version/revision before changing any save. The actual desktop shortcut passed again after this safeguard was added (final-desktop-reset.log). Other saves/projects unchanged.

Four native chests only, X=0 / Y=64 / Z=-6,-2,2,6; no duplicate, old range or legacy stations. Red unowned, blue FACTION Kweebec, green NEARBY, yellow FACTION Feran. Each initially holds 16 copper/8 iron bars. Color pads, unique labels and clear open field passed native checks.

Stationary witnesses: Kweebec_Sapling_HyThiefWitness (-7.5,64,0.5), Feran_Civilian_HyThiefWitness (7.5,64,0.5), inward-facing native head rotations. Responders: unfrozen/mobile Kweebec_Razorleaf (-21.5,64,0.5), Feran_Sharptooth (21.5,64,0.5). No permanent walls; native pursuit moved responding guards. Green witness distance is 7.566372975210778 blocks each. All pad distances/FOV/LOS/elevations mirror; alert delivery distances are 14/29, within LOCAL 30, and responders lie outside direct container sight from pads. Spawn (0,64.1,10.5); saved hotbar starts with 64 Rock_Stone_Cobble, matching the field.

## Native ownership persistence proof

All four serialized ownership components matched owned/scope/faction on native load; no registry fallback or ownership reattachment. Native OpenWindow/CloseWindow preserved the inventory/component relationship. Paired native withdrawal attributed successfully; insertion and same-chest rearrangement yielded **zero outbound theft classifications** while preserving all ownership fields. Test-only sentinel items inserted by actual native transactions were retained in the same slots/quantities.

The private persistence run explicitly saved/flushed chest chunk columns, unloaded and reloaded them through current ChunkStore APIs, and re-resolved fresh block entities. Full ownership and inventory snapshots matched. That saved private world then loaded in a **new native server process**; ownership and complete inventory snapshots matched before further validation. This proves native world persistence/restart, not a new connected-client UI session. Clean-master desktop reset then already verifies the serialized baseline unchanged; all tests were disposable and the actual active save remains clean/master-identical.

Initial test-only receipt wiring incorrectly treated the chest as a player receipt container; corrected to match production InventoryComponent.EVERYTHING. Initial saved-run restart helper also needed Force for inherited private classes and removal of inherited proof files. These were harness corrections, not gameplay repairs. The final native runs passed; failed disposable attempts remain archived as evidence.

## Mechanics results

| Color | Native result |
|---|---|
| RED / UNOWNED | Zero caught notifications, no alert delivery; withdrawal remains native. |
| BLUE / KWEEBEC | One caught message; Kweebec_All_Danger; exactly Razorleaf targeted offender and entered native attack/pursuit; Sharptooth stayed untargeted. |
| GREEN / NEARBY | LOCAL HyThief_Local_Theft, exactly one notification; both Razorleaf/Sharptooth targeted and pursued. Passed both visible, Kweebec-only visible, Feran-only visible. |
| YELLOW / FERAN | One caught message; Feran_Warriors_Attack; exactly Sharptooth targeted offender and pursued; Razorleaf stayed untargeted. |

Container standing/crouched ranges remain **20/6**, normal NPC perception **15/4**, LOCAL delivery **30**. Runtime visual evaluations passed crouched 5=seen, crouched 8=unseen, standing 8/18=seen, standing 22=unseen. Rear approach rejected by native FOV; opaque block wall rejected by native LOS; removing it restored LOS. No range/perception tuning.

Exact native offender refs, Attack state, movement and LastSeen/pursuit paths passed without custom movement/combat loops. Native public-style pickpocket interaction matrix passed **185 configured: 174 concrete, 2 abstract, 9 historical retired**. Nineteen pickpocket UI/resolver/rules/stealth class entries match the client-tested public JAR byte-for-byte. No proven interaction/UI behavior was reworked or claimed as a new physical client test.

## Evidence / reusable tools

Evidence directory: **DevTesting/Container11_Baseline_0_7_1_20261014/**. See restore-reset.json, actual-desktop-reset.log, Persistence-final/restart.log and their RUN native logs/expected snapshots, Mechanics-*.log, Pickpocket-matrix.log, pickpocket-backend-parity.json and final-baseline.json.

Reusable disposable native verifier/source saved under **DevTesting/Container_Theft_Test_World/Tools/BaselineVerification/**. It copies the input world; authoring is always false; generated probe JAR never enters the live Mods folder. Run sequentially with 2 GB heaps. Never publish these helpers or replace the clean master with a sentinel/alert validation run.

No gameplay/test geometry repair was necessary. Only restoration/reset, the reset version/revision safeguard, new test-only proof tooling and this checkpoint documentation changed. Stop for Firebeard review before PREFAB-ID research.
