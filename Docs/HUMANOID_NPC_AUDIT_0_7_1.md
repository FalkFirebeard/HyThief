# Full native 0.7.1 humanoid/sapient NPC audit

Recorded 2026-10-14. Investigation only. PREFAB is paused; no roles, loot, capabilities, gameplay, JARs, releases, masters or active worlds were changed.

## Result and counting boundary

**14 relevant families**, including abstract-only Bramblekin/Slothian and scripted Human roles. **12 families have concrete relevant roles. 254 concrete role IDs** are within this broad inventory: **174 currently pickpocket-configured**, **42 suitable unsupported candidates**, and **38 special/youth/Temple/encounter roles requiring separate review**. These are role IDs, not unique character models or NPC instances. The 254 includes scripted variants and summons, and does not mean 254 additions are recommended. Combat hostility alone excludes nothing.

The installed resolver catalog is **185**, comprising **174 existing concrete + 2 abstract + 9 absent historical IDs**. This agrees with the existing current native matrix. Four retained deprecated Duke phase roles are among the 174; they are not recommended hub actors. No `Draugr` role ID exists in the complete current role scan. Armed humanoid `Wraith` is present; its lantern is a separate excluded familiar.

**1,148 role files / 1,147 distinct IDs** were inspected across the entire installed role tree. Types: 545 Variant, 349 Generic, 89 Abstract, 164 Component. Two developer-only files share `Test_Patrol_Path`; both paths are recorded in the evidence. This collision does not affect any production family or configured role. The CSV contains all 1,147 distinct IDs, including excluded creatures and reusable/developer scaffolding, so the audit is not limited to HyThief's existing whitelist.

## Sources, method and confidence

- Read-only installed `C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip`: SHA-256 `b9164eb07d7c42e697d208ba3d35078a9b4a0125bef6261847a2f91e293e53b4`.
- Live server manifest: version `0.7.1`, revision `69c03ffa58d7e2f46bce8b0356ed206cfc7f10e6`. Current pinned server hash `a60fa0b31f1418691f8d8f7cc1159693e3ec504d4f0567fb91493a842d71f8ca`.
- Installed `HyThief-1.3.1-container.11.jar`: SHA-256 `f2dca782e22c38839640e8886f764b16ca00c36cdf8b4c812d6c099181eae08c`. Embedded role-map/resolver/loot JSON equals active source defaults; resolved catalog exactly equals `BuildSupport/supported-matrix.json`. Resolver checks exact IDs, explicit role/group blocks, byRole, longest byPrefix, direct assignment, then an existing table. It does **not** inherit pickpocket eligibility merely from a native parent/model.
- Native role Type, Reference, Modify/Parameters, appearance/model parent, component references, action/sensor declarations and NPC group assets were cross-checked. JSON evidence records the native asset path, inheritance chain, model parent, parameter declarations, beacon sensor/action distinction and resolver outcome per ID.
- `WorldSupport.isGroupMember` in the current server API uses the entity's role index and native tag-group membership. The table's **native group patterns** are direct IncludeRoles-minus-ExcludeRoles matches, not a promise of evaluated membership. IncludeGroups/ExcludeGroups, runtime custom roles, state gates, expressions and attitude overrides still require native runtime validation before implementation. An attitude group (e.g. `Goblin_Brave`, `Empty`, `Predators`) is not an NPCGroup/property identity.
- Static scanner resolves named parameters and role inheritance; it deliberately leaves compound Compute expressions as `EXPR:`. Beacon declarations are potential paths, not proof that a state enables a receiver. Concrete means native Generic/Variant, **not** proof of normal world-generation spawning. No new native spawn, interaction or combat test is claimed by this documentation task. Existing matrix/runtime proofs remain the baseline.
- The current user's gameplay overrides/third-party mods can change runtime behavior. This is the installed base-game roster and HyThief packaged-default audit, not a re-certification of arbitrary mod combinations.

Files: `NPC_ROSTER_0_7_1.csv` (complete exact-ID inventory); `NPC_ROSTER_0_7_1_EVIDENCE.json` (traceable evidence); `NPC_ROSTER_0_7_1_SUMMARY.json` (counts/hash/delta). Audit tools: `BuildSupport/NpcAudit/npc-roster-audit.py` and `render-npc-audit.py`.

## Classification and capability meanings

- **A**: current resolver accepts the exact ID. Abstract A entries are configured-only, clearly marked.
- **B**: concrete humanoid candidate not currently supported. No loot is invented or enabled by this audit.
- **C**: abstract/component, youth, Temple/scripted, summoned or encounter role requiring distinct review. Existing exclusions stay in force.
- **D**: excluded creature/object family. No gameplay exclusion was added.

Witness/FACTION/LOCAL/Responder columns are **recommendations**, independent of Pickpocket. `Yes` for witness means logical eligibility for future composition using existing visual checks; it does not mean container.11 already discovers that role. Faction witnessing requires actual native group identity (or a later approved small explicit grouping). LOCAL does not require same faction, but responders require an opt-in listener, valid offender, enabled state, attitude-compatible native combat and a separate proof. A role with native self-defense is a possible combat actor, not automatically a town guard. Prefer Razorleaf/guardian/warrior roles over civilians/workers; do not turn all witness candidates into responders.

## What container.11 actually implements

`ContainerVisualWitness` is called with fixture-declared witness roles supplied by `DevContainerTestWorld.witnessRoles`. `ContainerOwnershipPolicy.faction()` currently recognizes only native `Kweebec` and `Feran`. `member(...)` can query an existing native NPCGroup; rejection uses ownership's group. `HyThiefContainerAlert` / `HyThiefGuardAlert` dispatch FACTION only for Kweebec/Feran, with Razorleaf and Sharptooth/Longtooth authority candidates. The proven LOCAL signal `HyThief_Local_Theft` uses radius 30, pickpocket eligibility and an enabled BeaconSupport slot, not a universal humanoid responder list.

**LOCAL listener assets currently belong to the private container test fixture**, including its Razorleaf handoff role and the faction-field pack's Feran template. The global container.11 JAR contains no native role JSON declaring `HyThief_Local_Theft`. Do not mistake the fixture proof for production listener support across all 185 configured IDs. Native base-game roles do not already listen to this HyThief message.

Future expansion seams, after review only: witness participation data; faction/group mapping and dispatcher; small receiver composition for appropriate roles; current offender/LastSeen/Beacon delivery. Persistent ownership, transaction attribution, native chest interaction, 20/6 container sight, 15/4 normal perception, FOV/LOS, notification deduplication, UI and pickpocket loot/backend can stay untouched. No property ID or PREFAB implementation is proposed here.

## Family summary

| Family | Concrete A | Concrete B | Concrete C | Summary |
| --- | --- | --- | --- | --- |
| Bramblekin | 0 | 0 | 0 | Both native abstracts configured only; no concrete counterpart |
| Feran | 5 | 0 | 3 | All five ordinary adult roles configured; Cub/Temple separate |
| Goblin | 8 | 37 | 8 | Partial: four current ordinary roles + four deprecated Duke roles; many Update 7 additions |
| Hedera | 1 | 0 | 0 | Single concrete role configured; no native Hedera group established |
| Human | 0 | 0 | 2 | Scripted/cinematic roles only; no general settlement human guard family proven |
| Klops | 6 | 1 | 2 | Six ordinary roles configured; Dungeon trader candidate; Temple separate |
| Kweebec | 7 | 1 | 19 | Partial: Elder is a candidate; youth/Temple/prisoner need review |
| Outlander | 9 | 0 | 0 | All nine ordinary sapient roles configured; two wolf aliases excluded |
| Skeleton | 118 | 2 | 4 | Partial: 118 concrete configured; plain Skeleton/turret candidates; Elite/summons separate |
| Slothian | 0 | 0 | 0 | Abstract display role only; no concrete settlement actor |
| Trork | 12 | 0 | 0 | All 12 ordinary sapient roles configured; two wolf aliases excluded |
| Tuluk | 1 | 0 | 0 | Single native Fisherman configured; no combat guard counterpart |
| Wraith | 0 | 1 | 0 | New audit candidate: armed humanoid, unsupported; no named native faction group |
| Zombie | 7 | 0 | 0 | All seven configured; undead animals separate |

## Native faction and response paths

These are verified declarations, not authorization to treat every beacon as a crime alarm. Exact per-role sensor/action strings are in evidence. A message must deliver an offender target into a compatible native state rather than merely animate or summon an NPC.

| Family | Native identity | Native signals/behavior | Role choices | Integration assessment |
| --- | --- | --- | --- | --- |
| Kweebec | NPCGroup Kweebec; Kweebec_Civilian/Pets and specialized Temple groups | Kweebec_All_Danger -> Razorleaf LockedTarget; civilians broadcast/flee. Kweebec_All_Axe is a different trigger. | Kweebec_Razorleaf / _Patrol; Merchant, Sapling/Rootling as witnesses | Already proven FACTION. LOCAL proven private fixture, not global listener coverage. |
| Feran | NPCGroup Feran/Feran_Civilian; attitude Feran | Feran_Warriors_Attack -> guardian LockedTarget; Feran_All_Attack and Feran_Warriors_Converge have other gates. Civilian broadcasts Warriors_Attack. | Sharptooth/Longtooth authority; Civilian/Burrower/Windwalker witnesses | Already proven FACTION. Guardian threat/scent/memory persist; do not weaken detected behavior. |
| Goblin | NPCGroup Goblin: *Goblin_* minus Voidtaken*; several specialized groups; attitudes Goblin/Goblin_Brave | Goblin_Attack is a real acquisition/beacon path; brawl/scold/juggle messages are social protocols, not generic theft alarms. | Guardian/Scrapper/Burner/Lobber combat; Miner/Glutton/Thief/Spectator/social variants have distinct logic | Native candidate; must prove exact guard target/state gate. LOCAL opt-in needed. Avoid bosses/dungeon scripting initially. |
| Trork | NPCGroup Trork, Trork_Warrior/Spar/Chieftain/Underlings; attitude Trork | TrorkEnemySighted and TrorkChieftainCall; spar/meat beacons are distinct social paths. | Guard, Warrior, Mauler, Sentry, Hunter; no dedicated peaceful civilian/merchant role | Native candidate; prove target-carrying enemy-sighted path. LOCAL receiver needed; hostile-by-default is not exclusion. |
| Outlander | NPCGroup Outlander; attitude Outlander | Some Template_Trork-derived roles retain TrorkEnemySighted/chieftain/spar mechanics; other caster roots use shared flock/investigate paths. | Brute/Berserker/Marauder/Peon; Priest/Sorcerer/Cultist/Hunter/Stalker | Do not assume a uniform Outlander alarm. Prefer explicit LOCAL opt-in before faction routing; test native group filtering. |
| Skeleton | NPCGroup Skeleton: *Skeleton*; many Template_Intelligent roles use attitude Empty | Shared Under_Attack -> investigation and Flock_Attack -> combat are conditional/group/flock paths, not a universal Skeleton theft message. | Fighter/Knight/Archer/Mage and biome/class variants; no native civilian role | LOCAL receiver + native combat is safer first proof than inventing Skeleton_All_Danger. Keep undead animals out despite wildcard. |
| Zombie | NPCGroup Zombie; Predator-template attitude Predators | Message_Attack/Flock_Attack declarations; verify exact target/state and relationship before any reuse. | Zombie and Burnt/Frost/Sand/Aberrant variants | LOCAL/native combat proof needed; hostility is configurable, not grounds for exclusion. |
| Klops | No named Klops NPCGroup found; merchant attitude absent, several combat roots use Empty | No uniform Klops faction danger message established; shared intelligent combat in Gentleman/Miner is not a property alarm. | Merchant/_Patrol/_Wandering and Dungeon_Klops_Scrap_Trader; Gentleman/Miner self-defense is not a dedicated guard | Explicit small faction/group data + LOCAL receiver proof needed. Do not classify Dungeon trader as Goblin based on folder. |
| Hedera | No named Hedera NPCGroup; attitude Empty; model parent Trork | Shared Under_Attack/Flock_Attack intelligent paths | Hedera only, native melee | LOCAL proof first; visual parent Trork does not make it a Trork faction witness. |
| Tuluk | No named Tuluk NPCGroup established | No native combat alarm demonstrated for Fisherman | Tuluk_Fisherman witness-only; no guard counterpart | Witness-only station unless creator supplies appropriate responder; no fabricated combat AI. |
| Wraith | No named Wraith NPCGroup; attitude Empty; Player model parent | Shared intelligent Under_Attack/Flock_Attack | Wraith armed scythe; Wraith_Lantern excluded familiar | Candidate future witness/target/combat proof; add neither support nor fictitious faction tonight. |
| Bramblekin | Two abstracts; no native Bramblekin NPCGroup established | No concrete responder proof | Bramblekin / Bramblekin_Shaman abstracts only | Defer hub until a concrete native/creator role is identified. |
| Human / Slothian | Player model inheritance is not faction identity | Scripted/static/cinematic behavior | Temple_Mithril_Guard; Quest_Master/Slothian abstracts; Cinematic_Shrouded_Figure | Special review only; do not select guard from name alone. |

## Exact current per-role matrix

All native concrete relevant IDs and the family abstracts/components follow. Core shared templates are in the complete CSV and each role's evidence chain. D exclusions for wolf aliases and the detached Skeleton head remain visible under their families. `G/V/A/Cmp` mean Generic/Variant/Abstract/Component. `Y` means recommended witness; `R` means manual review; `-` means no/not applicable. Responder `Combat?` requires listener/state/attitude proof; `No` does not claim a role is incapable of self-defense. Pool/profile stay role-specific.

### Bramblekin

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Bramblekin | A / A | Configured only / bramblekin_civilian / civilian | - | - | - | - | None direct /  | Native Abstract; not a normal concrete spawn |
| Bramblekin_Shaman | A / A | Configured only / bramblekin_mystic / mystic | - | - | - | - | None direct /  | Native Abstract; not a normal concrete spawn |

### Feran

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Component_Feran_Instruction_Search | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Feran_Instruction_Threaten | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Feran_Sensor_Low_HP | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Feran_Sensor_Scarak | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Feran_Burrower | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Feran_Civilian | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Feran_Cub | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Feran_Longtooth | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Feran_Longtooth_Ranged | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Feran_Sharptooth | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Feran_Sharptooth_Ranged | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Feran_Windwalker | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Sensor_Feran_Day | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Sensor_Feran_Sharptooth_Wakeup | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Feran_Burrower | V / A | Yes / feran_civilian / civilian | Y | Y | Y | Combat? | Feran / Feran | Native Template_Feran_Burrower |
| Feran_Civilian | V / A | Yes / feran_civilian / civilian | Y | Y | Y | Combat? | Feran, Feran_Civilian / Feran | Native Template_Feran_Civilian |
| Feran_Cub | V / C | No: existing exclusion | R | R | R | - | Feran / Feran | HyThief exclusion: neutral_youth; No automatic youth pickpocket addition |
| Feran_Longtooth | V / A | Yes / feran_guardian / guardian | Y | Y | Y | Combat? | Feran / Feran | Native Template_Feran_Sharptooth |
| Feran_Sharptooth | V / A | Yes / feran_guardian / guardian | Y | Y | Y | Combat? | Feran / Feran | Native Template_Feran_Sharptooth |
| Feran_Windwalker | V / A | Yes / feran_mystic / mystic | Y | Y | Y | Combat? | Feran / Feran | Native Template_Feran_Windwalker |
| Template_Feran_Burrower | A / C | N/A | - | - | - | - | None direct / Feran | Native Abstract; not a normal concrete spawn |
| Template_Feran_Civilian | A / C | N/A | - | - | - | - | None direct / Feran | Native Abstract; not a normal concrete spawn |
| Template_Feran_Cub | A / C | N/A | - | - | - | - | None direct / Feran | Native Abstract; not a normal concrete spawn |
| Template_Feran_Sharptooth | A / C | N/A | - | - | - | - | None direct / Feran | Native Abstract; not a normal concrete spawn |
| Template_Feran_Windwalker | A / C | N/A | - | - | - | - | None direct / Feran | Native Abstract; not a normal concrete spawn |
| Temple_Feran | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Feran_Longtooth | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |

### Goblin

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Coffer_Goblin_Burner | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Coffer_Goblin, Goblin, Goblin_Burner / Coffer_Goblin | Native Template_Goblin_Burner |
| Coffer_Goblin_Glutton | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Coffer_Goblin, Goblin / Coffer_Goblin | Native Template_Goblin_Glutton |
| Coffer_Goblin_Guardian | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Coffer_Goblin, Goblin / Coffer_Goblin | Native Template_Goblin_Guardian |
| Coffer_Goblin_Lobber | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Coffer_Goblin, Goblin / Coffer_Goblin | Native Template_Goblin_Lobber |
| Coffer_Goblin_Miner | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Coffer_Goblin, Goblin / Coffer_Goblin | Native Template_Goblin_Miner |
| Coffer_Goblin_Scrapper | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Coffer_Goblin, Goblin / Coffer_Goblin | Native Template_Goblin_Scrapper |
| Coffer_Goblin_Turret | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Coffer_Goblin, Goblin / Coffer_Goblin | Native Template_Goblin_Turret |
| Component_Instruction_Dungeon_Goblin_Scale_Health | Cmp / C | N/A | - | - | - | - | Goblin /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Goblin_Eat | Cmp / C | N/A | - | - | - | - | Goblin /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Goblin_Flee_Void | Cmp / C | N/A | - | - | - | - | Goblin /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Goblin_Idles | Cmp / C | N/A | - | - | - | - | Goblin /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Dungeon_Feastmaster_Boss | V / C | Review: encounter; no loot configured | R | R | R | R | Dungeon_Feastmaster_Group / Goblin | Special encounter/phase role; no automatic pickpocket expansion |
| Dungeon_Feastmaster_Feeding | G / C | Review: encounter; no loot configured | R | R | R | R | Dungeon_Feastmaster_Group / Goblin | Special encounter/phase role; no automatic pickpocket expansion |
| Dungeon_Feastmaster_Scrapper | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Dungeon_Goblin_Pack / Goblin | Native Template_Dungeon_Goblin_Scrapper |
| Dungeon_Goblin_Arena_Spectator | G / B | Candidate; no loot configured | Y | Y | Y | - | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Goblin / Goblin | Generic native role |
| Dungeon_Goblin_Burner | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin, Goblin_Burner / Goblin | Native Template_Dungeon_Goblin_Burner |
| Dungeon_Goblin_Burner_Patrol | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin, Goblin_Burner / Goblin | Native Dungeon_Goblin_Burner |
| Dungeon_Goblin_Food_Carrier | G / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Feastmaster_Carrier_Group, Dungeon_Feastmaster_Food_Carrier_Group, Dungeon_Goblin, Goblin / Goblin | Generic native role |
| Dungeon_Goblin_Glutton | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Goblin / Goblin_Brave | Native Template_Goblin_Glutton |
| Dungeon_Goblin_Guardian | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Template_Dungeon_Goblin_Guardian |
| Dungeon_Goblin_Lobber | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Template_Dungeon_Goblin_Lobber |
| Dungeon_Goblin_Lobber_Patrol | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Dungeon_Goblin_Lobber |
| Dungeon_Goblin_Lobber_Turret | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Template_Dungeon_Goblin_Turret |
| Dungeon_Goblin_Mace | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Template_Dungeon_Goblin_Mace |
| Dungeon_Goblin_Miner | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Template_Dungeon_Goblin_Miner |
| Dungeon_Goblin_Miner_Patrol | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Dungeon_Goblin_Miner |
| Dungeon_Goblin_Scrapper | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Template_Dungeon_Goblin_Scrapper |
| Dungeon_Goblin_Scrapper_Enrage | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Template_Dungeon_Goblin_Scrapper |
| Dungeon_Goblin_Scrapper_Patrol | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Dungeon_Goblin_Scrapper |
| Dungeon_Goblin_Shroom_Carrier | G / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Feastmaster_Carrier_Group, Dungeon_Goblin, Goblin / Goblin | Generic native role |
| Dungeon_Goblin_Spectator | G / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Goblin / Goblin | Generic native role |
| Dungeon_Goblin_Thief | V / B | Candidate; no loot configured | Y | Y | Y | - | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Template_Dungeon_Goblin_Thief |
| Dungeon_Goblin_Thief_Patrol | V / B | Candidate; no loot configured | Y | Y | Y | - | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Dungeon_Goblin_Thief |
| Dungeon_Goblin_Thrower_Spectator | G / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Thrower_Spectator, Goblin / Goblin | Generic native role |
| Dungeon_Goblin_Turret | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Combat_Music_Targets_Deepworks, Dungeon_Goblin, Dungeon_Goblin_Pack, Goblin / Goblin | Native Template_Dungeon_Goblin_Turret |
| Edible_Goblin_Scrapper | V / D | No: existing exclusion | - | - | - | - | Edible_Goblin_Scrapper, Goblin /  | Animal/insectoid/edible creature/detached head; not a social theft participant; HyThief exclusion: aggressive_edible; Native deprecated asset retained; not a normal expansion candidate |
| Edible_Rat | V / D | No: existing exclusion | - | - | - | - | Edible_Rat /  | Animal/insectoid/edible creature/detached head; not a social theft participant; HyThief exclusion: aggressive_edible; Native deprecated asset retained; not a normal expansion candidate |
| Goblin_Burner | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Goblin, Goblin_Burner / Goblin_Brave | Native Template_Goblin_Burner |
| Goblin_Duke | V / A | Yes / goblin_elite / elite | Y | Y | Y | Combat? | Goblin / Empty | Native deprecated asset retained; not a normal expansion candidate |
| Goblin_Duke_Phase_2 | V / A | Yes / goblin_elite / elite | Y | Y | Y | Combat? | Goblin / Goblin | Native deprecated asset retained; not a normal expansion candidate |
| Goblin_Duke_Phase_3_Fast | V / A | Yes / goblin_elite / elite | Y | Y | Y | Combat? | Goblin / Goblin | Native deprecated asset retained; not a normal expansion candidate |
| Goblin_Duke_Phase_3_Slow | V / A | Yes / goblin_elite / elite | Y | Y | Y | Combat? | Goblin / Goblin | Native deprecated asset retained; not a normal expansion candidate |
| Goblin_Glutton | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Goblin, Goblin_Glutton / Goblin_Brave | Native Template_Goblin_Glutton |
| Goblin_Guardian | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Goblin / Goblin | Native Template_Goblin_Guardian |
| Goblin_Lab_Scrapper | V / C | Review: scripted role; no loot configured | R | R | R | - | Goblin, Goblin_Lab_Scrapper / Goblin | Temple/scripted variant; model does not imply faction or normal combat |
| Goblin_Launcher | G / B | Candidate; no loot configured | Y | Y | Y | - | Goblin /  | Generic native role |
| Goblin_Lobber | V / A | Yes / goblin_ranged / ranged | Y | Y | Y | Combat? | Goblin, Goblin_Lobber, Goblin_Thief_Seek / Goblin | Native Template_Goblin_Lobber |
| Goblin_Lobber_Turret | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Goblin / Goblin | Native Template_Goblin_Turret |
| Goblin_Miner | V / A | Yes / goblin_worker / worker | Y | Y | Y | Combat? | Goblin, Goblin_Miner, Goblin_Thief_Seek / Goblin_Brave | Native Template_Goblin_Miner |
| Goblin_Pilot | G / C | Review: encounter; no loot configured | R | R | R | R | Goblin / Goblin | Special encounter/phase role; no automatic pickpocket expansion |
| Goblin_Scientist | V / C | Review: scripted role; no loot configured | R | R | R | - | Goblin, Goblin_Scientist / Goblin | Temple/scripted variant; model does not imply faction or normal combat |
| Goblin_Scrapper | V / A | Yes / goblin_melee / melee | Y | Y | Y | Combat? | Goblin, Goblin_Scrapper, Goblin_Thief_Seek / Goblin | Native Template_Goblin_Scrapper |
| Goblin_Spectator | G / B | Candidate; no loot configured | Y | Y | Y | Combat? | Goblin / Goblin | Generic native role |
| Goblin_Thief | V / A | Yes / goblin_skirmisher / skirmisher | Y | Y | Y | - | Goblin / Goblin | Native Template_Goblin_Thief |
| Goblin_Turret | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Goblin / Goblin | Native Template_Goblin_Turret |
| Template_Dungeon_Goblin_Burner | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Dungeon_Goblin_Guardian | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Dungeon_Goblin_Lobber | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Dungeon_Goblin_Mace | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Dungeon_Goblin_Miner | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Dungeon_Goblin_Scrapper | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Dungeon_Goblin_Thief | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Dungeon_Goblin_Turret | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Goblin_Burner | A / C | N/A | - | - | - | - | Goblin / Goblin_Brave | Native Abstract; not a normal concrete spawn |
| Template_Goblin_Duke_Phase_1 | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn; Native deprecated asset retained; not a normal expansion candidate |
| Template_Goblin_Duke_Phase_2 | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn; Native deprecated asset retained; not a normal expansion candidate |
| Template_Goblin_Duke_Phase_3 | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn; Native deprecated asset retained; not a normal expansion candidate |
| Template_Goblin_Glutton | A / C | N/A | - | - | - | - | Goblin / Goblin_Brave | Native Abstract; not a normal concrete spawn |
| Template_Goblin_Guardian | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Goblin_Lab_Scrapper | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Goblin_Lobber | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Goblin_Miner | A / C | N/A | - | - | - | - | Goblin / Goblin_Brave | Native Abstract; not a normal concrete spawn |
| Template_Goblin_Scientist | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Goblin_Scrapper | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Goblin_Thief | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Goblin_Turret | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Temple_Goblin_Lab_Repair_Static | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Template_Temple_Goblin_Scrapper | A / C | N/A | - | - | - | - | Goblin / Goblin | Native Abstract; not a normal concrete spawn |
| Temple_Goblin_Lab_Repair_Static | V / C | Review: scripted role; no loot configured | R | R | R | - | Goblin, Temple_Goblin_Lab_Repair_Static / Goblin | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Goblin_Scrapper | V / C | Review: scripted role; no loot configured | R | R | R | Combat? | Goblin, Temple_Goblin_Scrapper / Goblin | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Goblin_Scrapper_Sleep_Static | G / C | Review: scripted role; no loot configured | R | R | R | - | Goblin, Temple_Goblin_Scrapper / Goblin | Temple/scripted variant; model does not imply faction or normal combat |
| Voidtaken_Goblin_Surge | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Void / Void | Native Template_Goblin_Scrapper |

### Hedera

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Hedera | V / A | Yes / hedera_melee / melee | Y | R | Y | Combat? | None direct / Empty | Hedera model/role family; model parent Trork is not faction membership |

### Human

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Cinematic_Shrouded_Figure | G / C | Review: no normal configured target | R | R | R | R | Cinematic_Void_Group /  | Generic native role |
| Quest_Master | A / C | Review: no normal configured target | R | R | R | R | None direct /  | Native Abstract; not a normal concrete spawn; HyThief exclusion: passive_special |
| Temple_Mithril_Guard | V / C | Review: no normal configured target | R | R | R | R | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |

### Klops

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Component_Instruction_Klops_Idle | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Dungeon_Klops_Scrap_Trader | G / B | Candidate; no loot configured | Y | R | Y | - | None direct / Goblin | Generic native role |
| Klops_Gentleman | V / A | Yes / klops_civilian / civilian | Y | R | Y | Combat? | None direct / Empty | Native Template_Intelligent |
| Klops_Merchant | G / A | Yes / klops_merchant / merchant | Y | R | Y | - | None direct /  | Generic native role |
| Klops_Merchant_Patrol | V / A | Yes / klops_merchant / merchant | Y | R | Y | - | None direct /  | Native Klops_Merchant |
| Klops_Merchant_Wandering | G / A | Yes / klops_merchant / merchant | Y | R | Y | - | None direct /  | Generic native role |
| Klops_Miner | V / A | Yes / klops_worker / worker | Y | R | Y | Combat? | None direct / Empty | Native Template_Intelligent |
| Klops_Miner_Patrol | V / A | Yes / klops_worker / worker | Y | R | Y | Combat? | None direct / Empty | Native Klops_Miner |
| Temple_Klops | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Klops_Merchant | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |

### Kweebec

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Component_Instruction_Attack_Sequence_Kweebec_Sapling_Razorleaf | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Kweebec_Instruction_Panic | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Kweebec_Instruction_Search | Cmp / C | N/A | - | - | - | - | None direct /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Kweebec_Elder | V / B | No: existing exclusion | Y | Y | Y | - | Kweebec / Kweebec | HyThief exclusion: neutral_special |
| Kweebec_Merchant | G / A | Yes / kweebec_merchant / merchant | Y | Y | Y | - | Kweebec /  | Generic native role |
| Kweebec_Prisoner | A / C | No: existing exclusion | - | - | - | - | Kweebec, Kweebec_Prisoner /  | Native Abstract; not a normal concrete spawn; HyThief exclusion: neutral_special |
| Kweebec_Razorleaf | V / A | Yes / kweebec_guardian / guardian | Y | Y | Y | Combat? | Kweebec / Kweebec | Native Template_Kweebec_Razorleaf |
| Kweebec_Razorleaf_Patrol | V / A | Yes / kweebec_guardian / guardian | Y | Y | Y | Combat? | Kweebec / Kweebec | Native Template_Kweebec_Razorleaf |
| Kweebec_Rootling | V / A | Yes / kweebec_civilian / civilian | Y | Y | Y | - | Kweebec / Kweebec | Native Template_Kweebec_Sapling |
| Kweebec_Sapling | V / A | Yes / kweebec_civilian / civilian | Y | Y | Y | - | Kweebec, Kweebec_Civilian / Kweebec | Native Template_Kweebec_Sapling |
| Kweebec_Sapling_Orange | V / A | Yes / kweebec_civilian / civilian | Y | Y | Y | - | Kweebec, Kweebec_Civilian / Kweebec | Native Template_Kweebec_Sapling |
| Kweebec_Sapling_Pink | V / A | Yes / kweebec_civilian / civilian | Y | Y | Y | - | Kweebec, Kweebec_Civilian / Kweebec | Native Template_Kweebec_Sapling |
| Kweebec_Seedling | V / C | No: existing exclusion | R | R | R | - | Capture_Crate, Kweebec / Kweebec | HyThief exclusion: neutral_youth; No automatic youth pickpocket addition |
| Kweebec_Sproutling | V / C | No: existing exclusion | R | R | R | - | Kweebec / Kweebec | HyThief exclusion: neutral_youth; No automatic youth pickpocket addition |
| Kweebec_Sproutling_Patrol | V / C | No: existing exclusion | R | R | R | - | Kweebec / Kweebec | HyThief exclusion: neutral_youth; No automatic youth pickpocket addition |
| Template_Kweebec_Razorleaf | A / C | N/A | - | - | - | - | Kweebec / Kweebec | Native Abstract; not a normal concrete spawn |
| Template_Kweebec_Sapling | A / C | N/A | - | - | - | - | Kweebec, Kweebec_Civilian / Kweebec | Native Abstract; not a normal concrete spawn |
| Template_Kweebec_Youngling | A / C | N/A | - | - | - | - | Kweebec / Kweebec | Native Abstract; not a normal concrete spawn |
| Template_Temple | A / C | N/A | - | - | - | - | None direct /  | Native Abstract; not a normal concrete spawn |
| Template_Temple_Kweebec_Guard | A / C | N/A | - | - | - | - | None direct / Kweebec | Native Abstract; not a normal concrete spawn |
| Template_Temple_Kweebec_Scared | A / C | N/A | - | - | - | - | None direct / Kweebec | Native Abstract; not a normal concrete spawn |
| Temple_Kweebec | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Elder | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Merchant | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Razorleaf | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Razorleaf_Guard | V / C | Review: scripted role; no loot configured | R | R | R | - | Temple_Kweebec_Guard / Kweebec | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Razorleaf_Patrol | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Razorleaf_Patrol1 | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Razorleaf_Patrol2 | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Razorleaf_Patrol3 | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Razorleaf_Patrol4 | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Razorleaf_Patrol5 | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Rootling_Static | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Scared | V / C | Review: scripted role; no loot configured | R | R | R | - | Temple_Kweebec_Scared / Kweebec | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Seedling | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Seedling_Static | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |
| Temple_Kweebec_Static | V / C | Review: scripted role; no loot configured | R | R | R | - | None direct /  | Temple/scripted variant; model does not imply faction or normal combat |

### Outlander

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Component_Instruction_Attack_Sequence_Outlander_Berserker | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Outlander_Brute | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Outlander_Cultist | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Outlander_Cultist_Ranged | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Outlander_Hunter | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Outlander_Hunter_Ranged | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Outlander_Marauder | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Outlander_Peon | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Outlander_Priest | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Outlander_Priest_Ranged | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Outlander_Sorcerer_Ranged | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Outlander_Stalker | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Outlander_Stalker_Ranged | Cmp / C | N/A | - | - | - | - | Outlander /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Outlander_Berserker | V / A | Yes / outlander_melee / melee | Y | Y | Y | Combat? | Outlander, Outlander_Spar / Outlander | Native Template_Trork_Melee |
| Outlander_Brute | V / A | Yes / outlander_melee / melee | Y | Y | Y | Combat? | Outlander / Outlander | Native Template_Trork_Melee |
| Outlander_Cultist | V / A | Yes / outlander_caster / caster | Y | Y | Y | Combat? | Outlander / Outlander_Ranged | Native Template_Trork_Hybrid |
| Outlander_Hunter | V / A | Yes / outlander_ranged / ranged | Y | Y | Y | Combat? | Outlander / Outlander_Ranged | Native Template_Trork_Hybrid |
| Outlander_Marauder | V / A | Yes / outlander_melee / melee | Y | Y | Y | Combat? | Outlander, Outlander_Marauder / Outlander | Native Template_Trork_Chieftain |
| Outlander_Peon | V / A | Yes / outlander_worker / worker | Y | Y | Y | Combat? | Outlander / Outlander | Native Template_Trork_Melee |
| Outlander_Priest | V / A | Yes / outlander_caster / caster | Y | Y | Y | Combat? | Outlander / Outlander | Native Template_Trork_Mage |
| Outlander_Sorcerer | V / A | Yes / outlander_caster / caster | Y | Y | Y | Combat? | Outlander / Outlander | Native Template_Trork_Mage |
| Outlander_Stalker | V / A | Yes / outlander_skirmisher / skirmisher | Y | Y | Y | Combat? | Outlander / Outlander_Ranged | Native Template_Trork_Hybrid |
| Wolf_Outlander_Priest | V / D | No: existing exclusion | - | - | - | - | Outlander, PredatorsBig / Trork | Animal/insectoid/edible creature/detached head; not a social theft participant; HyThief exclusion: aggressive_caster |
| Wolf_Outlander_Sorcerer | V / D | No: existing exclusion | - | - | - | - | Outlander, PredatorsBig / Trork | Animal/insectoid/edible creature/detached head; not a social theft participant; HyThief exclusion: aggressive_caster |

### Skeleton

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Dungeon_Skeleton_Sand_Archer | V / A | Yes / skeleton_sand_dungeon_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Dungeon_Skeleton_Sand_Assassin | V / A | Yes / skeleton_sand_dungeon_skirmisher / skirmisher | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Dungeon_Skeleton_Sand_Mage | V / A | Yes / skeleton_sand_dungeon_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Dungeon_Skeleton_Sand_Soldier | V / A | Yes / skeleton_sand_dungeon_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Risen_Gunner | V / C | No: existing exclusion | R | R | R | R | None direct /  | Template_Summoned_Ally; retain current policy, no blanket summoned-undead rule; HyThief exclusion: explicit role |
| Risen_Knight | V / C | No: existing exclusion | R | R | R | R | None direct /  | Template_Summoned_Ally; retain current policy, no blanket summoned-undead rule; HyThief exclusion: explicit role |
| Skeleton | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Skeleton / Skeleton | CURRENT native Variant -> Template_Skeleton. No pool configured; ordinary spawn use not established. Earlier template-only inference is insufficient. |
| Skeleton_Archer | V / A | Yes / skeleton_crypt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Archer_Patrol | V / A | Yes / skeleton_crypt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Archer |
| Skeleton_Archer_Turret | V / B | Candidate; no loot configured | Y | Y | Y | Combat? | Skeleton / Skeleton | Native Template_Skeleton_Turret |
| Skeleton_Archer_Wander | V / A | Yes / skeleton_crypt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Archer |
| Skeleton_Archmage | V / A | Yes / skeleton_crypt_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Archmage_Patrol | V / A | Yes / skeleton_crypt_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Archmage |
| Skeleton_Archmage_Wander | V / A | Yes / skeleton_crypt_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Archmage |
| Skeleton_Burnt_Alchemist | V / A | Yes / skeleton_burnt_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Burnt_Alchemist_Patrol | V / A | Yes / skeleton_burnt_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Alchemist |
| Skeleton_Burnt_Alchemist_Wander | V / A | Yes / skeleton_burnt_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Alchemist |
| Skeleton_Burnt_Archer | V / A | Yes / skeleton_burnt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Burnt_Archer_Patrol | V / A | Yes / skeleton_burnt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Archer |
| Skeleton_Burnt_Archer_Wander | V / A | Yes / skeleton_burnt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Archer |
| Skeleton_Burnt_Gunner | V / A | Yes / skeleton_burnt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Burnt_Gunner_Patrol | V / A | Yes / skeleton_burnt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Gunner |
| Skeleton_Burnt_Gunner_Wander | V / A | Yes / skeleton_burnt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Gunner |
| Skeleton_Burnt_Knight | V / A | Yes / skeleton_burnt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Burnt_Knight_Patrol | V / A | Yes / skeleton_burnt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Knight |
| Skeleton_Burnt_Knight_Wander | V / A | Yes / skeleton_burnt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Knight |
| Skeleton_Burnt_Lancer | V / A | Yes / skeleton_burnt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Burnt_Lancer_Patrol | V / A | Yes / skeleton_burnt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Lancer |
| Skeleton_Burnt_Lancer_Wander | V / A | Yes / skeleton_burnt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Lancer |
| Skeleton_Burnt_Praetorian | V / A | Yes / skeleton_burnt_elite / elite | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Burnt_Praetorian_Patrol | V / A | Yes / skeleton_burnt_elite / elite | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Praetorian |
| Skeleton_Burnt_Praetorian_Wander | V / A | Yes / skeleton_burnt_elite / elite | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Praetorian |
| Skeleton_Burnt_Soldier | V / A | Yes / skeleton_burnt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Burnt_Soldier_Patrol | V / A | Yes / skeleton_burnt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Soldier |
| Skeleton_Burnt_Soldier_Wander | V / A | Yes / skeleton_burnt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Soldier |
| Skeleton_Burnt_Wizard | V / A | Yes / skeleton_burnt_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Burnt_Wizard_Patrol | V / A | Yes / skeleton_burnt_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Wizard |
| Skeleton_Burnt_Wizard_Wander | V / A | Yes / skeleton_burnt_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Burnt_Wizard |
| Skeleton_Elite | V / C | Review: encounter; no loot configured | R | R | R | R | Skeleton, Skeleton_Elite_Encounter_Target / Skeleton | Special encounter/phase role; no automatic pickpocket expansion |
| Skeleton_Elite_Phase_2 | V / C | Review: encounter; no loot configured | R | R | R | R | Skeleton, Skeleton_Elite_Encounter_Target / Skeleton | Special encounter/phase role; no automatic pickpocket expansion |
| Skeleton_Fighter | V / A | Yes / skeleton_crypt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Fighter_Patrol | V / A | Yes / skeleton_crypt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Fighter |
| Skeleton_Fighter_Wander | V / A | Yes / skeleton_crypt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Fighter |
| Skeleton_Frost_Archer | V / A | Yes / skeleton_frost_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Frost_Archer_Patrol | V / A | Yes / skeleton_frost_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Archer |
| Skeleton_Frost_Archer_Wander | V / A | Yes / skeleton_frost_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Archer |
| Skeleton_Frost_Archmage | V / A | Yes / skeleton_frost_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Frost_Archmage_Patrol | V / A | Yes / skeleton_frost_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Archmage |
| Skeleton_Frost_Archmage_Wander | V / A | Yes / skeleton_frost_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Archmage |
| Skeleton_Frost_Fighter | V / A | Yes / skeleton_frost_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Frost_Fighter_Patrol | V / A | Yes / skeleton_frost_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Fighter |
| Skeleton_Frost_Fighter_Wander | V / A | Yes / skeleton_frost_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Fighter |
| Skeleton_Frost_Knight | V / A | Yes / skeleton_frost_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Frost_Knight_Patrol | V / A | Yes / skeleton_frost_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Knight |
| Skeleton_Frost_Knight_Wander | V / A | Yes / skeleton_frost_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Knight |
| Skeleton_Frost_Mage | V / A | Yes / skeleton_frost_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Frost_Mage_Patrol | V / A | Yes / skeleton_frost_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Mage |
| Skeleton_Frost_Mage_Wander | V / A | Yes / skeleton_frost_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Mage |
| Skeleton_Frost_Ranger | V / A | Yes / skeleton_frost_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Frost_Ranger_Patrol | V / A | Yes / skeleton_frost_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Ranger |
| Skeleton_Frost_Ranger_Wander | V / A | Yes / skeleton_frost_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Ranger |
| Skeleton_Frost_Scout | V / A | Yes / skeleton_frost_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Frost_Scout_Patrol | V / A | Yes / skeleton_frost_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Scout |
| Skeleton_Frost_Scout_Wander | V / A | Yes / skeleton_frost_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Scout |
| Skeleton_Frost_Soldier | V / A | Yes / skeleton_frost_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Frost_Soldier_Patrol | V / A | Yes / skeleton_frost_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Soldier |
| Skeleton_Frost_Soldier_Wander | V / A | Yes / skeleton_frost_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Frost_Soldier |
| Skeleton_Incandescent_Fighter | V / A | Yes / skeleton_incandescent_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Incandescent_Fighter_Patrol | V / A | Yes / skeleton_incandescent_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Incandescent_Fighter |
| Skeleton_Incandescent_Fighter_Wander | V / A | Yes / skeleton_incandescent_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Incandescent_Fighter |
| Skeleton_Incandescent_Footman | V / A | Yes / skeleton_incandescent_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Incandescent_Footman_Patrol | V / A | Yes / skeleton_incandescent_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Incandescent_Footman |
| Skeleton_Incandescent_Footman_Wander | V / A | Yes / skeleton_incandescent_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Incandescent_Footman |
| Skeleton_Incandescent_Head | V / D | No: existing exclusion | - | - | - | - | Skeleton / Empty | Animal/insectoid/edible creature/detached head; not a social theft participant; HyThief exclusion: explicit role |
| Skeleton_Incandescent_Mage | V / A | Yes / skeleton_incandescent_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Incandescent_Mage_Patrol | V / A | Yes / skeleton_incandescent_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Incandescent_Mage |
| Skeleton_Incandescent_Mage_Wander | V / A | Yes / skeleton_incandescent_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Incandescent_Mage |
| Skeleton_Knight | V / A | Yes / skeleton_crypt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Knight_Patrol | V / A | Yes / skeleton_crypt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Knight |
| Skeleton_Knight_Wander | V / A | Yes / skeleton_crypt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Knight |
| Skeleton_Mage | V / A | Yes / skeleton_crypt_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Mage_Patrol | V / A | Yes / skeleton_crypt_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Mage |
| Skeleton_Mage_Wander | V / A | Yes / skeleton_crypt_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Mage |
| Skeleton_Pirate_Captain | V / A | Yes / skeleton_pirate_elite / elite | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Pirate_Captain_Patrol | V / A | Yes / skeleton_pirate_elite / elite | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Pirate_Captain |
| Skeleton_Pirate_Captain_Wander | V / A | Yes / skeleton_pirate_elite / elite | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Pirate_Captain |
| Skeleton_Pirate_Gunner | V / A | Yes / skeleton_pirate_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Pirate_Gunner_Patrol | V / A | Yes / skeleton_pirate_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Pirate_Gunner |
| Skeleton_Pirate_Gunner_Wander | V / A | Yes / skeleton_pirate_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Pirate_Gunner |
| Skeleton_Pirate_Striker | V / A | Yes / skeleton_pirate_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Pirate_Striker_Patrol | V / A | Yes / skeleton_pirate_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Pirate_Striker |
| Skeleton_Pirate_Striker_Wander | V / A | Yes / skeleton_pirate_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Pirate_Striker |
| Skeleton_Ranger | V / A | Yes / skeleton_crypt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Ranger_Patrol | V / A | Yes / skeleton_crypt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Ranger |
| Skeleton_Ranger_Wander | V / A | Yes / skeleton_crypt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Ranger |
| Skeleton_Sand_Archer | V / A | Yes / skeleton_sand_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Sand_Archer_Patrol | V / A | Yes / skeleton_sand_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Archer |
| Skeleton_Sand_Archer_Wander | V / A | Yes / skeleton_sand_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Archer |
| Skeleton_Sand_Archmage | V / A | Yes / skeleton_sand_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Sand_Archmage_Patrol | V / A | Yes / skeleton_sand_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Archmage |
| Skeleton_Sand_Archmage_Wander | V / A | Yes / skeleton_sand_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Archmage |
| Skeleton_Sand_Assassin | V / A | Yes / skeleton_sand_skirmisher / skirmisher | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Sand_Assassin_Patrol | V / A | Yes / skeleton_sand_skirmisher / skirmisher | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Assassin |
| Skeleton_Sand_Assassin_Wander | V / A | Yes / skeleton_sand_skirmisher / skirmisher | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Assassin |
| Skeleton_Sand_Guard | V / A | Yes / skeleton_sand_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Sand_Guard_Patrol | V / A | Yes / skeleton_sand_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Guard |
| Skeleton_Sand_Guard_Wander | V / A | Yes / skeleton_sand_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Guard |
| Skeleton_Sand_Mage | V / A | Yes / skeleton_sand_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Sand_Mage_Patrol | V / A | Yes / skeleton_sand_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Mage |
| Skeleton_Sand_Mage_Wander | V / A | Yes / skeleton_sand_caster / caster | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Mage |
| Skeleton_Sand_Ranger | V / A | Yes / skeleton_sand_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Sand_Ranger_Patrol | V / A | Yes / skeleton_sand_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Ranger |
| Skeleton_Sand_Ranger_Wander | V / A | Yes / skeleton_sand_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Ranger |
| Skeleton_Sand_Scout | V / A | Yes / skeleton_sand_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Sand_Scout_Patrol | V / A | Yes / skeleton_sand_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Scout |
| Skeleton_Sand_Scout_Wander | V / A | Yes / skeleton_sand_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Scout |
| Skeleton_Sand_Soldier | V / A | Yes / skeleton_sand_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Sand_Soldier_Patrol | V / A | Yes / skeleton_sand_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Soldier |
| Skeleton_Sand_Soldier_Wander | V / A | Yes / skeleton_sand_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Sand_Soldier |
| Skeleton_Scout | V / A | Yes / skeleton_crypt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Scout_Patrol | V / A | Yes / skeleton_crypt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Scout |
| Skeleton_Scout_Wander | V / A | Yes / skeleton_crypt_ranged / ranged | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Scout |
| Skeleton_Soldier | V / A | Yes / skeleton_crypt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Template_Intelligent |
| Skeleton_Soldier_Patrol | V / A | Yes / skeleton_crypt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Soldier |
| Skeleton_Soldier_Wander | V / A | Yes / skeleton_crypt_melee / melee | Y | Y | Y | Combat? | Skeleton / Empty | Native Skeleton_Soldier |
| Template_Skeleton | A / C | N/A | - | - | - | - | Skeleton / Skeleton | Native Abstract; not a normal concrete spawn |
| Template_Skeleton_Elite_Phase_2 | A / C | N/A | - | - | - | - | Skeleton / Skeleton | Native Abstract; not a normal concrete spawn |
| Template_Skeleton_Turret | A / C | N/A | - | - | - | - | Skeleton / Skeleton | Native Abstract; not a normal concrete spawn |

### Slothian

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Slothian | A / C | Review: no normal configured target | R | R | R | R | None direct /  | Native Abstract; not a normal concrete spawn; HyThief exclusion: passive_special |

### Trork

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Component_Instruction_Attack_Sequence_Trork_Chieftain | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Trork_Hunter | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Trork_Hunter_Ranged | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Trork_Sentry | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Trork_Sentry_Ranged | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Attack_Sequence_Trork_Warrior | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Instruction_Flee_Trork_Shaman | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Alerted | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Converge | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Dropped_Meat | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Greeting | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Hybrid_Idle_Extra | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Idle | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Idle_Motion_Follow_Path | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Idle_Motion_Wander | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Offensive_Default | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Panic | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Pre_Checks | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Reputation_Switch | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Return_Home | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Search | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Shoot_Bush | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Sleep | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Spar | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Warn | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Instruction_Warning_Shot | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Ranger_Instruction_Idle_Motion_Wander | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Sensor_Can_Sleep | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Component_Trork_Sensor_Dropped_Meat | Cmp / C | N/A | - | - | - | - | Trork /  | Native Component; not a normal concrete spawn; Reusable instruction/component/template or dev fixture; not a production NPC |
| Template_Trork_Chieftain | A / C | N/A | - | - | - | - | Trork / Trork | Native Abstract; not a normal concrete spawn |
| Template_Trork_Companion | A / C | N/A | - | - | - | - | Trork / Trork | Native Abstract; not a normal concrete spawn |
| Template_Trork_Hybrid | A / C | N/A | - | - | - | - | Trork / Trork_Ranged | Native Abstract; not a normal concrete spawn |
| Template_Trork_Mage | A / C | N/A | - | - | - | - | Trork / Trork | Native Abstract; not a normal concrete spawn |
| Template_Trork_Melee | A / C | N/A | - | - | - | - | Trork / Trork | Native Abstract; not a normal concrete spawn |
| Template_Trork_Ranger | A / C | N/A | - | - | - | - | Trork / Trork_Hunter | Native Abstract; not a normal concrete spawn |
| Trork_Brawler | V / A | Yes / trork_melee / melee | Y | Y | Y | Combat? | Trork / Trork | Native Template_Trork_Melee |
| Trork_Chieftain | V / A | Yes / trork_elite / elite | Y | Y | Y | Combat? | Trork, Trork_Chieftain / Trork | Native Template_Trork_Chieftain |
| Trork_Doctor_Witch | V / A | Yes / trork_caster / caster | Y | Y | Y | Combat? | Trork / Trork | Native Template_Trork_Mage |
| Trork_Guard | V / A | Yes / trork_melee / melee | Y | Y | Y | Combat? | Trork / Trork_Hunter | Native Template_Trork_Ranger |
| Trork_Hunter | V / A | Yes / trork_ranged / ranged | Y | Y | Y | Combat? | Trork / Trork_Hunter | Native Template_Trork_Ranger |
| Trork_Mauler | V / A | Yes / trork_melee / melee | Y | Y | Y | Combat? | Trork / Trork | Native Template_Trork_Melee |
| Trork_Sentry | V / A | Yes / trork_ranged / ranged | Y | Y | Y | Combat? | Trork / Trork_Ranged | Native Template_Trork_Hybrid |
| Trork_Sentry_Patrol | V / A | Yes / trork_ranged / ranged | Y | Y | Y | Combat? | Trork / Trork_Ranged | Native Template_Trork_Hybrid |
| Trork_Shaman | V / A | Yes / trork_caster / caster | Y | Y | Y | Combat? | Trork / Trork | Native Template_Trork_Mage |
| Trork_Unarmed | V / A | Yes / trork_melee / melee | Y | Y | Y | Combat? | Trork / Trork | Native Template_Trork_Melee |
| Trork_Warrior | V / A | Yes / trork_melee / melee | Y | Y | Y | Combat? | Trork, Trork_Spar, Trork_Warrior / Trork | Native Template_Trork_Melee |
| Trork_Warrior_Patrol | V / A | Yes / trork_melee / melee | Y | Y | Y | Combat? | Trork / Trork | Native Template_Trork_Melee |
| Wolf_Trork_Hunter | V / D | No: existing exclusion | - | - | - | - | PredatorsBig, Trork / Trork | Animal/insectoid/edible creature/detached head; not a social theft participant; HyThief exclusion: aggressive_ranged |
| Wolf_Trork_Shaman | V / D | No: existing exclusion | - | - | - | - | PredatorsBig, Trork / Trork | Animal/insectoid/edible creature/detached head; not a social theft participant; HyThief exclusion: aggressive_caster |

### Tuluk

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Tuluk_Fisherman | V / A | Yes / tuluk_worker / worker | Y | R | Y | - | None direct /  | Native Template_Placeholder |

### Wraith

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Wraith | V / B | Candidate; no loot configured | Y | R | Y | Combat? | None direct / Empty | Template_Intelligent; Player-derived model; wielded scythe. Armed humanoid undead candidate; social/faction identity requires creator decision |

### Zombie

| Role ID | Type / class | Pickpocket (pool / profile) | Witness | FACTION | LOCAL | Responder | Native group patterns / attitude | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Zombie | V / A | Yes / zombie_melee / melee | Y | Y | Y | Combat? | Zombie / Predators | Native Template_Predator |
| Zombie_Aberrant | V / A | Yes / zombie_aberrant_elite / elite | Y | Y | Y | Combat? | Zombie / Predators | Native Template_Predator |
| Zombie_Aberrant_Big | V / A | Yes / zombie_aberrant_elite / elite | Y | Y | Y | Combat? | Zombie / Predators | Native Template_Predator |
| Zombie_Aberrant_Small | V / A | Yes / zombie_aberrant_melee / melee | Y | Y | Y | Combat? | Zombie / Predators | Native Template_Predator |
| Zombie_Burnt | V / A | Yes / zombie_burnt_melee / melee | Y | Y | Y | Combat? | Zombie / Predators | Native Template_Predator |
| Zombie_Frost | V / A | Yes / zombie_frost_melee / melee | Y | Y | Y | Combat? | Zombie / Predators | Native Template_Predator |
| Zombie_Sand | V / A | Yes / zombie_sand_melee / melee | Y | Y | Y | Combat? | Zombie / Predators | Native Template_Predator |

## Unsupported suitable exact IDs (B)

These are candidates only; require role-specific pool/profile decisions, native interaction/stealth composition and runtime validation. No inherited race-wide loot flattening. Special C roles appear in the matrix/review appendix rather than silently being added.

- **Goblin (37)**: `Coffer_Goblin_Burner`, `Coffer_Goblin_Glutton`, `Coffer_Goblin_Guardian`, `Coffer_Goblin_Lobber`, `Coffer_Goblin_Miner`, `Coffer_Goblin_Scrapper`, `Coffer_Goblin_Turret`, `Dungeon_Feastmaster_Scrapper`, `Dungeon_Goblin_Arena_Spectator`, `Dungeon_Goblin_Burner`, `Dungeon_Goblin_Burner_Patrol`, `Dungeon_Goblin_Food_Carrier`, `Dungeon_Goblin_Glutton`, `Dungeon_Goblin_Guardian`, `Dungeon_Goblin_Lobber`, `Dungeon_Goblin_Lobber_Patrol`, `Dungeon_Goblin_Lobber_Turret`, `Dungeon_Goblin_Mace`, `Dungeon_Goblin_Miner`, `Dungeon_Goblin_Miner_Patrol`, `Dungeon_Goblin_Scrapper`, `Dungeon_Goblin_Scrapper_Enrage`, `Dungeon_Goblin_Scrapper_Patrol`, `Dungeon_Goblin_Shroom_Carrier`, `Dungeon_Goblin_Spectator`, `Dungeon_Goblin_Thief`, `Dungeon_Goblin_Thief_Patrol`, `Dungeon_Goblin_Thrower_Spectator`, `Dungeon_Goblin_Turret`, `Goblin_Burner`, `Goblin_Glutton`, `Goblin_Guardian`, `Goblin_Launcher`, `Goblin_Lobber_Turret`, `Goblin_Spectator`, `Goblin_Turret`, `Voidtaken_Goblin_Surge`.
- **Klops (1)**: `Dungeon_Klops_Scrap_Trader`.
- **Kweebec (1)**: `Kweebec_Elder`.
- **Skeleton (2)**: `Skeleton`, `Skeleton_Archer_Turret`.
- **Wraith (1)**: `Wraith`.

## Ambiguous and special review decisions

| Exact role(s) | Evidence | Recommendation |
| --- | --- | --- |
| Ghoul | Template_Predator; hunched undead model, eye 1.2/hitbox 1.8; no social/faction role established | Review, not included in 14-family/254 headline. Biped alone does not prove social sapience. |
| Werewolf | Template_Predator; Player-derived biped model, beast attacks; no settlement behavior established | Review rather than automatic wildlife inclusion. Existing beast policy remains unchanged. |
| Shadow_Knight | Template_Predator; large undead model, Hound_Bleached bite animation; no civilized/armed guard path established | Explicit existing block retained. Name is insufficient to make this a knight responder. |
| Void_Spawn; Void_Spawn_Entombed; Void_Spawn_Entombed_Anomaly; Void_Spawn_Surge | Void spawn template/model and hostile acquisition; humanoid appearance possible, no property/social evidence | Review separately; not default social theft participants. Other void crystals/crawlers/eyes/larvae/tentacles/spectres excluded. |
| Wraith | Template_Intelligent, Player-derived armed scythe model; differentiated from Wraith_Lantern | B suitable humanoid candidate, with social/faction use still a creator decision. Lantern remains D. |
| Skeleton | CURRENT Type Variant, Reference Template_Skeleton | B unsupported concrete candidate; no loot pool. Do not repeat old abstract-only claim; normal generated spawn usage still unproven. |
| Skeleton_Elite; Skeleton_Elite_Phase_2 | Elite/phase-specific combat composition | C encounter review. No automatic policy or loot change. |
| Risen_Gunner; Risen_Knight | Template_Summoned_Ally, Burnt Gunner/Knight appearances | C special summons; current exact blocks remain. No new blanket summoned-undead exclusion. |
| Feran_Cub; Kweebec_Seedling; Kweebec_Sproutling; Kweebec_Sproutling_Patrol | Native youth roles, current resolver youth block | C youth policy review for witnessing; pickpocket exclusions unchanged. |
| Kweebec_Prisoner | Native Abstract | C special/template; not a normal concrete settlement actor. |
| Temple Kweebec/Feran/Klops/Goblin/Mithril variants | Scripted, stationary, guard/patrol, greet or special animation trees; names/models may share races while native role groups do not | C per exact ID, not automatic family inheritance. Some have explicit Temple groups; validate runtime membership and script constraints. |
| Bramblekin; Bramblekin_Shaman; Slothian; Quest_Master | Native Abstract; latter two are Player-derived displays/scripts | No generic new concrete actors invented. Bramblekin two remain configured-only A. |
| Dungeon_Feastmaster_Boss; Dungeon_Feastmaster_Feeding; Goblin_Pilot | Combat/feeding/path-only actor phases; Pilot invulnerable/scripted | C Goblin special roles. Gobliterator machinery/barrel are D objects, not sapient guards. |
| Goblin_Duke and its three phases | Native assets retained in _Deprecated; exact existing resolver pools still present | A support retained but not suitable primary hub representatives. Removing them is a separate policy decision. |
| Voidtaken_Goblin_Surge | Native Goblin-shaped role uses Template_Goblin_Scrapper but explicitly excluded by Goblin NPCGroup wildcard exclusion | B humanoid candidate; faction identity requires separate review. Do not route as Goblin by model. |
| Cinematic_Shrouded_Figure; Temple_Mithril_Guard | Concrete human appearance but cinematic/static instructions | C scripts, not proof of general Human civilian or combat guard roles. |

## Excluded inventory

Full exact D IDs (and abstract/component C scaffold IDs) remain in the CSV. Default exclusions: Aquatic fish; Avian birds; Creature critters/hunt/livestock/tamed/mammal/reptile/vermin and non-social mythic beasts; Elemental golems/spirits; Scarak/insectoids including dungeon roles; Temple wildlife; undead animals; non-social Void creatures/crystals/tentacles/spectres; mechanical encounter parts/barrels. Core developer tests are not production candidates even when they use a humanoid model. Do not use a broad NPCGroup wildcard as a sapience policy.

Four historical companion aliases are specifically D: `Wolf_Trork_Hunter`, `Wolf_Trork_Shaman`, `Wolf_Outlander_Priest`, `Wolf_Outlander_Sorcerer`. Their placement under intelligent faction folders/groups does not change their wolf appearance/template behavior. These were removed from configured support previously; no new animal policy is introduced here. Other examples: `Edible_Goblin_Scrapper` uses edible-critter behavior, `Edible_Rat` is a rat, `Skeleton_Incandescent_Head` is a detached flying head, `Wraith_Lantern` is a lantern familiar, `Horse_Skeleton`/`Horse_Skeleton_Armored` remain animals despite the Skeleton group pattern. A floating `Void_Spectre` is not the armed humanoid Wraith.

## Update 7 / 0.7.1 delta

The preserved 0.7.0 reference ZIP (SHA-256 `dd53c25076a8ad9521103a765e7d3c71b18fc9af62e60519f14f757765257fa5`) was compared by role ID and parsed JSON. **No production NPC role was added, removed or semantically changed from that 0.7.0 snapshot to installed 0.7.1.** The only additions/changes were `Test_Scarak_Broodmother_Young_Stationary` (added) and `Test_Dragon_Stationary` (changed), both developer tests. Folder formatting is not counted as a semantic change. This scope is NPC role JSON, not a claim that all game assets/API were unchanged.

The new/reworked Goblin roster is the **0.7.0 Update 7 transition**, already present in 0.7.1. Official [Chapter I release notes](https://hytale.com/news/2026/10/chapter-i-out-of-the-breach) describe redesigned Goblins, noncombat social behavior, Void fleeing and encounter bosses. Official [modder-facing notes](https://hytale.com/news/2026/10/modder-facing-patch-notes-update-7) note the `Goblin_Miner` attitude-role rename to `Goblin_Brave`, new targeting/perception composition and pathfinding changes. That attitude rename does **not** remove the concrete `Goblin_Miner` NPC role. Official [Update 7 pre-release notes](https://hytale.com/news/2026/9/pre-release-patch-notes-update-7) document orphan Hermit/Scavenger removals. The [official news index](https://hytale.com/news) dates hotfix 0.7.1 to October 13; its joining/connection summary is not evidence of a new humanoid roster.

Historical 0.6.8 HyThief provenance/matrix plus `Docs/UPDATE_0_7_0_COMPATIBILITY_AUDIT.md` establish the nine retired configured IDs and the four migrated surviving Goblin roots. No full version-certified 0.6.8 roster delta is invented from the older unversioned March asset backup. Exact current unsupported additions are listed above and exact current C scripts below/above; their first appearance dates should not be guessed merely because HyThief omitted them historically.

Retired configured IDs (inactive, not new exclusions):

- `Goblin_Hermit`
- `Goblin_Lobber_Patrol`
- `Goblin_Miner_Patrol`
- `Goblin_Ogre`
- `Goblin_Scavenger`
- `Goblin_Scavenger_Battleaxe`
- `Goblin_Scavenger_Sword`
- `Goblin_Scrapper_Patrol`
- `Goblin_Thief_Patrol`

Existing surviving `Goblin_Lobber`, `Goblin_Miner`, `Goblin_Scrapper`, `Goblin_Thief` were re-composed around current native templates during the public compatibility cycle. The four deprecated Duke phases remain. New current Goblin templates include Burner/Glutton/Guardian/Turret, Coffer variants, dungeon variants/carriers/spectators, Lab/Scientist Temple roles and special encounter actors. These are different role IDs and behaviors; they do not receive the four existing roles' loot automatically. Native panic/brawl/flee/combat acquisition and attitude gates must survive any future receiver composition.

## Recommended first multi-faction hub (not built)

Use **eight primary families: Kweebec, Feran, Goblin, Trork, Outlander, Klops, Skeleton, Zombie**. They cover native faction alarms, shared combat templates, social/worker roles and undead with existing configured representative pickpocket targets. This is a future design set, not a claim that eight FACTION branches work today.

| Family | Two proposed witnesses | Proposed responder | Prerequisite |
| --- | --- | --- | --- |
| Kweebec | Kweebec_Sapling x2 | Kweebec_Razorleaf | Existing proven native alarm |
| Feran | Feran_Civilian x2 | Feran_Sharptooth | Existing proven native alarm; preserve memory/attitude |
| Goblin | Goblin_Miner x2 | Goblin_Scrapper | Already configured current templates; prove Goblin_Attack or opt-in LOCAL |
| Trork | Trork_Warrior x2 | Trork_Guard | Custom friendly relationship; native combat and enemy-sighted proof |
| Outlander | Outlander_Peon x2 | Outlander_Brute | Custom relationship; do not reuse Trork messages across factions blindly |
| Klops | Klops_Merchant x2 | Klops_Miner (conditional self-defense actor) | No dedicated native guard/group. Include as witness-only initially; responder must be explicitly approved/proven |
| Skeleton | Skeleton_Fighter x2 | Skeleton_Knight | Custom friendly relationship; native group plus explicit LOCAL receiver proof |
| Zombie | Zombie x2 | Zombie_Aberrant | Custom friendly relationship; receiver/target/state proof before swarm |

Keep witness capability independent from responder capability. Combat-capable witnesses need a stationary/facing test composition for a fair future hub; no permanent LOS isolation or hostile-AI workaround is authorized now. Require mirrored distance, elevation, FOV/LOS, trigger/interaction point and pathing before handoff. Prove each family's FACTION filtering and LOCAL receipt separately before a cross-faction swarm. Klops should not block that plan by inventing guard AI: start witness-only or review its native Miner self-defense before designating it a responder.

Second-wave candidates: Tuluk (witness-only; no guard), Hedera (singleton combat, identity proof), Wraith (support/policy/group proof). Defer abstract Bramblekin/Slothian, scripted Human/Temple roles, youth, summons, bosses and ambiguous monsters. No new role addition, hub construction or PREFAB work until Firebeard reviews this audit.

## Verification / preservation

Parser assertions: unique production IDs; source versus installed resolver defaults; exact resolver catalog equals matrix; existing loot table required; counts reconcile; 185 = 174 + 2 + 9; 254 = 174 + 42 + 38. Complete file inventory records the one core test ID collision instead of hiding it. Manual checks covered Wolf aliases, youth/Temple variants, Wraith/lantern, plain Skeleton, dungeon Klops trader, Gobliterator object/phase actors, Goblin group Voidtaken exception and action-versus-sensor beacons.

No gameplay/API repair or runtime hub validation was performed. No JAR built/installed, world reset/edited, ownership/alert policy changed, prefab ID created, or public artifact altered. Only this note, audit evidence and reproducible inspection tools are committed. Stop for Firebeard review.
