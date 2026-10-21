# NPC Creator Editor â€” Phase A architecture checkpoint

2026-10-20 (America/Denver). Hytale 0.7.2 / d769dc3d57881261a62858e1394f0e4de9a21eca. Accepted source aec6304 / development editor.2. Falk confirmed the Ownership Editor Tool client test passed. No NPC implementation or world modification at this checkpoint.

## Verified source/native evidence

- HyThiefNativeProbe: shared native eligibility sensor and UseEntityEvent.Pre interception. Individual pickpocket gating belongs here, with a second check before backend withdrawals. Preserve role-specific resolver/loot and native trade.
- MerchantStoragePage: target Ref/store already retained; an override changed while a page is open can be checked before transferring an item.
- ContainerVisualWitness.capture: central candidate boundary for visual witnesses. Existing ownership/faction checks, 20/6 range, FOV and native LOS stay intact.
- HyThiefGuardAlert, HyThiefContainerAlert and HyThiefPersonalWitnessAlert: direct HyThief crime response boundaries. Response-disabled NPCs must be filtered before target/state/beacon assignment, independently of witness eligibility.
- PickpocketPlugin.handleSingleSteal / triggerCaughtReaction: victim catches and native reactions are distinct decisions. Witness participation and response participation need separate gating if the editor covers pickpocket crimes as well as container crimes.
- TargetUtil.getTargetEntity(ref,8,false,accessor) supplies the native ray/entity query, but does not by itself establish an unobstructed editor target. Explicit reach, NPC type, death, UUID, same-store and native LOS validation are required.
- ComponentRegistryProxy.registerComponent(Class,String,BuilderCodec) supports serialized individual NPC settings in EntityStore. A runtime Ref is not a persistent identity.
- Native EntitySavingSystem dispatches dirty entity saves by UUID using IChunkSaver.Cubic.saveEntity; Dirty.forceMarkDirty is available. Edit operations must use native dirty tracking rather than relying on serialization registration alone.
- ComponentRegistryProxy.registerResource(Class,String,BuilderCodec) plus native disk resource storage supports world-local role/faction maps. Store.saveAllResources is available; no global native role asset rewrite is needed.
- Existing ContainerOwnershipPolicy uses native NPCGroup/WorldSupport membership and resolves a recognized faction among Kweebec, Feran, Trork, Outlander, Skeleton and Zombie. Faction scope must state that boundary, not assume every modded NPC has one of these factions.

Official API references checked against installed bytecode: https://docs.hytale.com/api/com/hypixel/hytale/component/ComponentRegistryProxy ; https://docs.hytale.com/api/com/hypixel/hytale/server/core/entity/Dirty ; https://docs.hytale.com/api/com/hypixel/hytale/server/core/util/TargetUtil . Official update context: https://hytale.com/news/2026/10/2026-10-12-modder-facing-patch-notes-update-7 . Installed stable binary is authoritative for signatures.

## Proposed bounded storage â€” approval required

New, separate data only; do not alter HyThiefOwnershipProof or existing container data.

1. HyThiefNpcParticipation: serialized per-NPC component with independent Pickpocket/Witness/Response tri-state values (Default, Enabled, Disabled).
2. HyThiefNpcRules: serialized world resource with maps keyed by exact role ID and recognized faction ID.
3. Resolve each capability independently: individual non-default, role non-default, faction non-default, existing HyThief support/default.
4. Enabled can override a broader Disabled value but cannot manufacture missing loot/interaction wiring or a native reaction contract.
5. Role/faction changes are explicitly world-wide at their chosen scope; individual Apply never edits a role asset or shared default.

Request includes separate approval before persistent data-format changes. Automatic approval review rejected implementation of this new storage until Falk approves the concrete design. No rejected source changes were applied. Falk subsequently approved this exact design, and implementation proceeded under that approval.

## Identity and behavior limits

- Individual configuration follows a genuinely saved/restored NPC entity. A fresh replacement spawn does not automatically inherit the dead NPC's individual component.
- Applicable role/faction world rules can govern future spawns in that world. They do not automatically travel with an exported prefab.
- Registered serializable holder components are a candidate for individual prefab copying, but no NPC editor export/placement round trip has run. Do not claim support until tested.
- Native source provenance is not needed for this editor; do not reopen property/PREFAB ownership work.
- Unsupported NPCs can be targeted for safe disabling/default configuration, but Enabled must not invent pickpocket loot or native combat behavior. Third-party profiles need verified compatibility.
- Disabling HyThief crime response is not invulnerability or global pacification. Native damage, ambient hostility, active pursuit and native social/AI propagation remain separate. Native beacon listeners can react to other native signals independently of a direct HyThief adapter call; do not claim that a Java dispatch gate suppresses all native behavior.

## Implementation/verification plan after approval

Native inventory tool HyThief_NPC_Editor_Tool with a distinct native editor icon/brush appearance; Primary-only custom interaction, permission hythief.npc.edit, shared world-thread opener, silent invalid targets, duplicate-page protection. Native page with explicit scope, three dropdowns, capability/inheritance explanation, Apply/Cancel.

Apply must recheck permission, actor/world, NPC Ref+UUID+role, range/LOS, selected-scope original values and native persistence. No writes until Apply. Test individual independence, all three settings and precedence, stale UI, permission revocation, native item acquisition/chain, entity unload/reload and restart; prove prefab serialization/placement separately. Run existing ownership editor, NPC matrix, chest/wardrobe theft/pursuit and no-HyStash regressions in disposable copies only.

Preserve 01 HyThief and all master/active ownership and inventory edits. Any new JAR deployment or version-pin write outside the project requires a separate approval after candidate verification. No publication authorized.
