"""Render the reviewed 0.7.1 structural inventory as a HyThief audit note."""
import collections,csv,json,pathlib,sys
p=pathlib.Path(sys.argv[1]); rows=list(csv.DictReader((p/'NPC_ROSTER_0_7_1.csv').open(encoding='utf-8-sig')))
s=json.loads((p/'NPC_ROSTER_0_7_1_SUMMARY.json').read_text(encoding='utf8'))
e=json.loads((p/'NPC_ROSTER_0_7_1_EVIDENCE.json').read_text(encoding='utf8'))
families=s['families']
def table(head,body):
    def esc(x):return str(x).replace('|','/').replace('\n',' ')
    return '\n'.join(['| '+' | '.join(head)+' |','| '+' | '.join(['---']*len(head))+' |']+['| '+' | '.join(esc(v) for v in row)+' |' for row in body])+'\n'
txt=f'''# Full native 0.7.1 humanoid/sapient NPC audit

Recorded 2026-10-14. Investigation only. PREFAB is paused; no roles, loot, capabilities, gameplay, JARs, releases, masters or active worlds were changed.

## Result and counting boundary

**14 relevant families**, including abstract-only Bramblekin/Slothian and scripted Human roles. **12 families have concrete relevant roles. 254 concrete role IDs** are within this broad inventory: **174 currently pickpocket-configured**, **42 suitable unsupported candidates**, and **38 special/youth/Temple/encounter roles requiring separate review**. These are role IDs, not unique character models or NPC instances. The 254 includes scripted variants and summons, and does not mean 254 additions are recommended. Combat hostility alone excludes nothing.

The installed resolver catalog is **185**, comprising **174 existing concrete + 2 abstract + 9 absent historical IDs**. This agrees with the existing current native matrix. Four retained deprecated Duke phase roles are among the 174; they are not recommended hub actors. No `Draugr` role ID exists in the complete current role scan. Armed humanoid `Wraith` is present; its lantern is a separate excluded familiar.

**1,148 role files / 1,147 distinct IDs** were inspected across the entire installed role tree. Types: 545 Variant, 349 Generic, 89 Abstract, 164 Component. Two developer-only files share `Test_Patrol_Path`; both paths are recorded in the evidence. This collision does not affect any production family or configured role. The CSV contains all 1,147 distinct IDs, including excluded creatures and reusable/developer scaffolding, so the audit is not limited to HyThief's existing whitelist.

## Sources, method and confidence

- Read-only installed `C:\\Users\\askin\\AppData\\Roaming\\Hytale\\install\\release\\package\\game\\latest\\Assets.zip`: SHA-256 `{s['assetsSha256']}`.
- Live server manifest: version `0.7.1`, revision `69c03ffa58d7e2f46bce8b0356ed206cfc7f10e6`. Current pinned server hash `a60fa0b31f1418691f8d8f7cc1159693e3ec504d4f0567fb91493a842d71f8ca`.
- Installed `HyThief-1.3.1-container.11.jar`: SHA-256 `{s['installedJarSha256']}`. Embedded role-map/resolver/loot JSON equals active source defaults; resolved catalog exactly equals `BuildSupport/supported-matrix.json`. Resolver checks exact IDs, explicit role/group blocks, byRole, longest byPrefix, direct assignment, then an existing table. It does **not** inherit pickpocket eligibility merely from a native parent/model.
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

'''
statuses={'Kweebec':'Partial: Elder is a candidate; youth/Temple/prisoner need review','Feran':'All five ordinary adult roles configured; Cub/Temple separate','Klops':'Six ordinary roles configured; Dungeon trader candidate; Temple separate','Goblin':'Partial: four current ordinary roles + four deprecated Duke roles; many Update 7 additions','Trork':'All 12 ordinary sapient roles configured; two wolf aliases excluded','Outlander':'All nine ordinary sapient roles configured; two wolf aliases excluded','Skeleton':'Partial: 118 concrete configured; plain Skeleton/turret candidates; Elite/summons separate','Zombie':'All seven configured; undead animals separate','Tuluk':'Single native Fisherman configured; no combat guard counterpart','Hedera':'Single concrete role configured; no native Hedera group established','Bramblekin':'Both native abstracts configured only; no concrete counterpart','Wraith':'New audit candidate: armed humanoid, unsupported; no named native faction group','Human':'Scripted/cinematic roles only; no general settlement human guard family proven','Slothian':'Abstract display role only; no concrete settlement actor'}
txt+=table(['Family','Concrete A','Concrete B','Concrete C','Summary'],[(f,s['familyCounts'][f].get('A',0),s['familyCounts'][f].get('B',0),s['familyCounts'][f].get('C',0),statuses[f]) for f in families])
txt+='''
## Native faction and response paths

These are verified declarations, not authorization to treat every beacon as a crime alarm. Exact per-role sensor/action strings are in evidence. A message must deliver an offender target into a compatible native state rather than merely animate or summon an NPC.

'''
native=[
('Kweebec','NPCGroup Kweebec; Kweebec_Civilian/Pets and specialized Temple groups','Kweebec_All_Danger -> Razorleaf LockedTarget; civilians broadcast/flee. Kweebec_All_Axe is a different trigger.','Kweebec_Razorleaf / _Patrol; Merchant, Sapling/Rootling as witnesses','Already proven FACTION. LOCAL proven private fixture, not global listener coverage.'),
('Feran','NPCGroup Feran/Feran_Civilian; attitude Feran','Feran_Warriors_Attack -> guardian LockedTarget; Feran_All_Attack and Feran_Warriors_Converge have other gates. Civilian broadcasts Warriors_Attack.','Sharptooth/Longtooth authority; Civilian/Burrower/Windwalker witnesses','Already proven FACTION. Guardian threat/scent/memory persist; do not weaken detected behavior.'),
('Goblin','NPCGroup Goblin: *Goblin_* minus Voidtaken*; several specialized groups; attitudes Goblin/Goblin_Brave','Goblin_Attack is a real acquisition/beacon path; brawl/scold/juggle messages are social protocols, not generic theft alarms.','Guardian/Scrapper/Burner/Lobber combat; Miner/Glutton/Thief/Spectator/social variants have distinct logic','Native candidate; must prove exact guard target/state gate. LOCAL opt-in needed. Avoid bosses/dungeon scripting initially.'),
('Trork','NPCGroup Trork, Trork_Warrior/Spar/Chieftain/Underlings; attitude Trork','TrorkEnemySighted and TrorkChieftainCall; spar/meat beacons are distinct social paths.','Guard, Warrior, Mauler, Sentry, Hunter; no dedicated peaceful civilian/merchant role','Native candidate; prove target-carrying enemy-sighted path. LOCAL receiver needed; hostile-by-default is not exclusion.'),
('Outlander','NPCGroup Outlander; attitude Outlander','Some Template_Trork-derived roles retain TrorkEnemySighted/chieftain/spar mechanics; other caster roots use shared flock/investigate paths.','Brute/Berserker/Marauder/Peon; Priest/Sorcerer/Cultist/Hunter/Stalker','Do not assume a uniform Outlander alarm. Prefer explicit LOCAL opt-in before faction routing; test native group filtering.'),
('Skeleton','NPCGroup Skeleton: *Skeleton*; many Template_Intelligent roles use attitude Empty','Shared Under_Attack -> investigation and Flock_Attack -> combat are conditional/group/flock paths, not a universal Skeleton theft message.','Fighter/Knight/Archer/Mage and biome/class variants; no native civilian role','LOCAL receiver + native combat is safer first proof than inventing Skeleton_All_Danger. Keep undead animals out despite wildcard.'),
('Zombie','NPCGroup Zombie; Predator-template attitude Predators','Message_Attack/Flock_Attack declarations; verify exact target/state and relationship before any reuse.','Zombie and Burnt/Frost/Sand/Aberrant variants','LOCAL/native combat proof needed; hostility is configurable, not grounds for exclusion.'),
('Klops','No named Klops NPCGroup found; merchant attitude absent, several combat roots use Empty','No uniform Klops faction danger message established; shared intelligent combat in Gentleman/Miner is not a property alarm.','Merchant/_Patrol/_Wandering and Dungeon_Klops_Scrap_Trader; Gentleman/Miner self-defense is not a dedicated guard','Explicit small faction/group data + LOCAL receiver proof needed. Do not classify Dungeon trader as Goblin based on folder.'),
('Hedera','No named Hedera NPCGroup; attitude Empty; model parent Trork','Shared Under_Attack/Flock_Attack intelligent paths','Hedera only, native melee','LOCAL proof first; visual parent Trork does not make it a Trork faction witness.'),
('Tuluk','No named Tuluk NPCGroup established','No native combat alarm demonstrated for Fisherman','Tuluk_Fisherman witness-only; no guard counterpart','Witness-only station unless creator supplies appropriate responder; no fabricated combat AI.'),
('Wraith','No named Wraith NPCGroup; attitude Empty; Player model parent','Shared intelligent Under_Attack/Flock_Attack','Wraith armed scythe; Wraith_Lantern excluded familiar','Candidate future witness/target/combat proof; add neither support nor fictitious faction tonight.'),
('Bramblekin','Two abstracts; no native Bramblekin NPCGroup established','No concrete responder proof','Bramblekin / Bramblekin_Shaman abstracts only','Defer hub until a concrete native/creator role is identified.'),
('Human / Slothian','Player model inheritance is not faction identity','Scripted/static/cinematic behavior','Temple_Mithril_Guard; Quest_Master/Slothian abstracts; Cinematic_Shrouded_Figure','Special review only; do not select guard from name alone.')]
txt+=table(['Family','Native identity','Native signals/behavior','Role choices','Integration assessment'],native)
txt+='''
## Exact current per-role matrix

All native concrete relevant IDs and the family abstracts/components follow. Core shared templates are in the complete CSV and each role's evidence chain. D exclusions for wolf aliases and the detached Skeleton head remain visible under their families. `G/V/A/Cmp` mean Generic/Variant/Abstract/Component. `Y` means recommended witness; `R` means manual review; `-` means no/not applicable. Responder `Combat?` requires listener/state/attitude proof; `No` does not claim a role is incapable of self-defense. Pool/profile stay role-specific.

'''
def cap(v):return 'Y' if v.startswith('Yes') else 'R' if v.startswith('Review') else '-'
for fam in families:
    txt+='### '+fam+'\n\n'
    rr=[r for r in rows if r['Family']==fam]
    txt+=table(['Role ID','Type / class','Pickpocket (pool / profile)','Witness','FACTION','LOCAL','Responder','Native group patterns / attitude','Notes'],[(r['Role ID'],{'Variant':'V','Generic':'G','Abstract':'A','Component':'Cmp'}.get(r['Native Type'],r['Native Type'])+' / '+r['Class'],(('Configured only' if r['Native Type']=='Abstract' else 'Yes')+' / '+r['Loot pool']+' / '+r['Profile']) if r['Loot pool'] else r['Pickpocket'],cap(r['Witness']),cap(r['Faction Witness']),cap(r['Local Witness']),'Combat?' if r['Responder'].startswith('Candidate') else 'R' if r['Responder'].startswith('Review') else '-',r['Native group patterns']+' / '+r['Attitude group'],r['Notes'] or ('Native '+r['Parent'] if r['Parent'] else 'Generic native role')) for r in rr])+'\n'
txt+='''## Unsupported suitable exact IDs (B)

These are candidates only; require role-specific pool/profile decisions, native interaction/stealth composition and runtime validation. No inherited race-wide loot flattening. Special C roles appear in the matrix/review appendix rather than silently being added.

'''
for fam in families:
    ids=[r['Role ID'] for r in rows if r['Family']==fam and r['Class']=='B']
    if ids:txt+='- **'+fam+' ('+str(len(ids))+')**: '+', '.join('`'+r+'`' for r in ids)+'.\n'
txt+='''
## Ambiguous and special review decisions

'''
reviews=[
('Ghoul','Template_Predator; hunched undead model, eye 1.2/hitbox 1.8; no social/faction role established','Review, not included in 14-family/254 headline. Biped alone does not prove social sapience.'),
('Werewolf','Template_Predator; Player-derived biped model, beast attacks; no settlement behavior established','Review rather than automatic wildlife inclusion. Existing beast policy remains unchanged.'),
('Shadow_Knight','Template_Predator; large undead model, Hound_Bleached bite animation; no civilized/armed guard path established','Explicit existing block retained. Name is insufficient to make this a knight responder.'),
('Void_Spawn; Void_Spawn_Entombed; Void_Spawn_Entombed_Anomaly; Void_Spawn_Surge','Void spawn template/model and hostile acquisition; humanoid appearance possible, no property/social evidence','Review separately; not default social theft participants. Other void crystals/crawlers/eyes/larvae/tentacles/spectres excluded.'),
('Wraith','Template_Intelligent, Player-derived armed scythe model; differentiated from Wraith_Lantern','B suitable humanoid candidate, with social/faction use still a creator decision. Lantern remains D.'),
('Skeleton','CURRENT Type Variant, Reference Template_Skeleton','B unsupported concrete candidate; no loot pool. Do not repeat old abstract-only claim; normal generated spawn usage still unproven.'),
('Skeleton_Elite; Skeleton_Elite_Phase_2','Elite/phase-specific combat composition','C encounter review. No automatic policy or loot change.'),
('Risen_Gunner; Risen_Knight','Template_Summoned_Ally, Burnt Gunner/Knight appearances','C special summons; current exact blocks remain. No new blanket summoned-undead exclusion.'),
('Feran_Cub; Kweebec_Seedling; Kweebec_Sproutling; Kweebec_Sproutling_Patrol','Native youth roles, current resolver youth block','C youth policy review for witnessing; pickpocket exclusions unchanged.'),
('Kweebec_Prisoner','Native Abstract','C special/template; not a normal concrete settlement actor.'),
('Temple Kweebec/Feran/Klops/Goblin/Mithril variants','Scripted, stationary, guard/patrol, greet or special animation trees; names/models may share races while native role groups do not','C per exact ID, not automatic family inheritance. Some have explicit Temple groups; validate runtime membership and script constraints.'),
('Bramblekin; Bramblekin_Shaman; Slothian; Quest_Master','Native Abstract; latter two are Player-derived displays/scripts','No generic new concrete actors invented. Bramblekin two remain configured-only A.'),
('Dungeon_Feastmaster_Boss; Dungeon_Feastmaster_Feeding; Goblin_Pilot','Combat/feeding/path-only actor phases; Pilot invulnerable/scripted','C Goblin special roles. Gobliterator machinery/barrel are D objects, not sapient guards.'),
('Goblin_Duke and its three phases','Native assets retained in _Deprecated; exact existing resolver pools still present','A support retained but not suitable primary hub representatives. Removing them is a separate policy decision.'),
('Voidtaken_Goblin_Surge','Native Goblin-shaped role uses Template_Goblin_Scrapper but explicitly excluded by Goblin NPCGroup wildcard exclusion','B humanoid candidate; faction identity requires separate review. Do not route as Goblin by model.'),
('Cinematic_Shrouded_Figure; Temple_Mithril_Guard','Concrete human appearance but cinematic/static instructions','C scripts, not proof of general Human civilian or combat guard roles.')]
txt+=table(['Exact role(s)','Evidence','Recommendation'],reviews)
txt+='''
## Excluded inventory

Full exact D IDs (and abstract/component C scaffold IDs) remain in the CSV. Default exclusions: Aquatic fish; Avian birds; Creature critters/hunt/livestock/tamed/mammal/reptile/vermin and non-social mythic beasts; Elemental golems/spirits; Scarak/insectoids including dungeon roles; Temple wildlife; undead animals; non-social Void creatures/crystals/tentacles/spectres; mechanical encounter parts/barrels. Core developer tests are not production candidates even when they use a humanoid model. Do not use a broad NPCGroup wildcard as a sapience policy.

Four historical companion aliases are specifically D: `Wolf_Trork_Hunter`, `Wolf_Trork_Shaman`, `Wolf_Outlander_Priest`, `Wolf_Outlander_Sorcerer`. Their placement under intelligent faction folders/groups does not change their wolf appearance/template behavior. These were removed from configured support previously; no new animal policy is introduced here. Other examples: `Edible_Goblin_Scrapper` uses edible-critter behavior, `Edible_Rat` is a rat, `Skeleton_Incandescent_Head` is a detached flying head, `Wraith_Lantern` is a lantern familiar, `Horse_Skeleton`/`Horse_Skeleton_Armored` remain animals despite the Skeleton group pattern. A floating `Void_Spectre` is not the armed humanoid Wraith.

## Update 7 / 0.7.1 delta

The preserved 0.7.0 reference ZIP (SHA-256 `'''+s['deltaFrom070']['previousAssetsSha256']+'''`) was compared by role ID and parsed JSON. **No production NPC role was added, removed or semantically changed from that 0.7.0 snapshot to installed 0.7.1.** The only additions/changes were `Test_Scarak_Broodmother_Young_Stationary` (added) and `Test_Dragon_Stationary` (changed), both developer tests. Folder formatting is not counted as a semantic change. This scope is NPC role JSON, not a claim that all game assets/API were unchanged.

The new/reworked Goblin roster is the **0.7.0 Update 7 transition**, already present in 0.7.1. Official [Chapter I release notes](https://hytale.com/news/2026/10/chapter-i-out-of-the-breach) describe redesigned Goblins, noncombat social behavior, Void fleeing and encounter bosses. Official [modder-facing notes](https://hytale.com/news/2026/10/modder-facing-patch-notes-update-7) note the `Goblin_Miner` attitude-role rename to `Goblin_Brave`, new targeting/perception composition and pathfinding changes. That attitude rename does **not** remove the concrete `Goblin_Miner` NPC role. Official [Update 7 pre-release notes](https://hytale.com/news/2026/9/pre-release-patch-notes-update-7) document orphan Hermit/Scavenger removals. The [official news index](https://hytale.com/news) dates hotfix 0.7.1 to October 13; its joining/connection summary is not evidence of a new humanoid roster.

Historical 0.6.8 HyThief provenance/matrix plus `Docs/UPDATE_0_7_0_COMPATIBILITY_AUDIT.md` establish the nine retired configured IDs and the four migrated surviving Goblin roots. No full version-certified 0.6.8 roster delta is invented from the older unversioned March asset backup. Exact current unsupported additions are listed above and exact current C scripts below/above; their first appearance dates should not be guessed merely because HyThief omitted them historically.

Retired configured IDs (inactive, not new exclusions):

'''
txt+='\n'.join('- `'+r+'`' for r in s['configuredMissing'])+'\n'
txt+='''
Existing surviving `Goblin_Lobber`, `Goblin_Miner`, `Goblin_Scrapper`, `Goblin_Thief` were re-composed around current native templates during the public compatibility cycle. The four deprecated Duke phases remain. New current Goblin templates include Burner/Glutton/Guardian/Turret, Coffer variants, dungeon variants/carriers/spectators, Lab/S cientist Temple roles and special encounter actors. These are different role IDs and behaviors; they do not receive the four existing roles' loot automatically. Native panic/brawl/flee/combat acquisition and attitude gates must survive any future receiver composition.

## Recommended first multi-faction hub (not built)

Use **eight primary families: Kweebec, Feran, Goblin, Trork, Outlander, Klops, Skeleton, Zombie**. They cover native faction alarms, shared combat templates, social/worker roles and undead with existing configured representative pickpocket targets. This is a future design set, not a claim that eight FACTION branches work today.

'''
hub=[('Kweebec','Kweebec_Sapling x2','Kweebec_Razorleaf','Existing proven native alarm'),('Feran','Feran_Civilian x2','Feran_Sharptooth','Existing proven native alarm; preserve memory/attitude'),('Goblin','Goblin_Miner x2','Goblin_Scrapper','Already configured current templates; prove Goblin_Attack or opt-in LOCAL'),('Trork','Trork_Warrior x2','Trork_Guard','Custom friendly relationship; native combat and enemy-sighted proof'),('Outlander','Outlander_Peon x2','Outlander_Brute','Custom relationship; do not reuse Trork messages across factions blindly'),('Klops','Klops_Merchant x2','Klops_Miner (conditional self-defense actor)','No dedicated native guard/group. Include as witness-only initially; responder must be explicitly approved/proven'),('Skeleton','Skeleton_Fighter x2','Skeleton_Knight','Custom friendly relationship; native group plus explicit LOCAL receiver proof'),('Zombie','Zombie x2','Zombie_Aberrant','Custom friendly relationship; receiver/target/state proof before swarm')]
txt+=table(['Family','Two proposed witnesses','Proposed responder','Prerequisite'],hub)
txt+='''
Keep witness capability independent from responder capability. Combat-capable witnesses need a stationary/facing test composition for a fair future hub; no permanent LOS isolation or hostile-AI workaround is authorized now. Require mirrored distance, elevation, FOV/LOS, trigger/interaction point and pathing before handoff. Prove each family's FACTION filtering and LOCAL receipt separately before a cross-faction swarm. Klops should not block that plan by inventing guard AI: start witness-only or review its native Miner self-defense before designating it a responder.

Second-wave candidates: Tuluk (witness-only; no guard), Hedera (singleton combat, identity proof), Wraith (support/policy/group proof). Defer abstract Bramblekin/Slothian, scripted Human/Temple roles, youth, summons, bosses and ambiguous monsters. No new role addition, hub construction or PREFAB work until Firebeard reviews this audit.

## Verification / preservation

Parser assertions: unique production IDs; source versus installed resolver defaults; exact resolver catalog equals matrix; existing loot table required; counts reconcile; 185 = 174 + 2 + 9; 254 = 174 + 42 + 38. Complete file inventory records the one core test ID collision instead of hiding it. Manual checks covered Wolf aliases, youth/Temple variants, Wraith/lantern, plain Skeleton, dungeon Klops trader, Gobliterator object/phase actors, Goblin group Voidtaken exception and action-versus-sensor beacons.

No gameplay/API repair or runtime hub validation was performed. No JAR built/installed, world reset/edited, ownership/alert policy changed, prefab ID created, or public artifact altered. Only this note, audit evidence and reproducible inspection tools are committed. Stop for Firebeard review.
'''
txt=txt.replace('Lab/S cientist','Lab/Scientist')
(p/'HUMANOID_NPC_AUDIT_0_7_1.md').write_text(txt,encoding='utf8')
assert s['configuredSupported']==185 and s['ordinarySupportedConcrete']==174
assert len([r for r in rows if r['Class']=='B'])==42
assert s['concreteRelevant']==sum(s[k] for k in ['ordinarySupportedConcrete','suitableUnsupported','specialConcreteReview'])
assert e['Skeleton']['inheritance'][:2]==['Skeleton','Template_Skeleton']
assert all(next(r for r in rows if r['Role ID']==x)['Class']=='D' for x in ['Wolf_Trork_Hunter','Wolf_Trork_Shaman','Wolf_Outlander_Priest','Wolf_Outlander_Sorcerer','Wraith_Lantern','Skeleton_Incandescent_Head'])
assert next(r for r in rows if r['Role ID']=='Dungeon_Klops_Scrap_Trader')['Family']=='Klops'
assert 'ActionBeacon:Feran_Warriors_Attack' in e['Feran_Civilian']['nativeBeaconDeclarations']
assert 'SensorBeacon:Feran_Warriors_Attack' in e['Feran_Sharptooth']['nativeBeaconDeclarations']
print('Report rendered; catalog/count/classification/selected native path checks PASS')
