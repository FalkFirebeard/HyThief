# Native metadata compatibility review â€” Creator Update

2026-10-20. Installed Hytale 0.7.2 / d769dc3d57881261a62858e1394f0e4de9a21eca. Research only; no broad production refactor or HyStash modification.

## Conclusion

Dynamic native role and NPC-group discovery is feasible, including independent registrations. A universal single semantic faction and universal crime-response contract were NOT found. Do not replace discovery with growing hardcoded lists; do not treat every NPCGroup as a faction or presume registration supplies gameplay support.

The NPC editor candidate covers its documented bounded scopes, not universal metadata support. Its fixed recognized faction resolver remains a limitation, following Falk's instruction to finish the editor before broader refactoring. This is not claimed to satisfy universal discovery.

## Native evidence

- NPCPlugin.getBuilderManager().getAllBuilders(): BuilderInfo.getKeyName gives stable IDs; builder.category()==Role.class, isRemoved/isValid and builder.isSpawnable distinguish role definitions/templates from other components. Persist string IDs, not transient indexes.
- NPCGroup.getAssetMap().getAssetMap(): enumerate all loaded groups, including mod packs; AssetMap.getAssetPack/getPath expose provenance. WorldSupport.isGroupMember(roleIndex,ref,groupIndex,accessor) evaluates native membership.
- NPCGroup carries included/excluded role patterns and group tags, with no `isFaction` or unique primary-faction field. AttitudeGroup maps attitudes to groups; it does not provide one semantic faction per NPC.
- Disposable independent MetadataResearch asset pack registered a Variant role and group named HyThiefResearch_Lizardfolk, without production HyThief edits. Native enumeration and membership passed: 119 groups, 1167 role builders, 892 spawnable builders. Counts include HyThief, fixture and research assets, not just base game.
- Research NPC matched `[HyThiefResearch_Lizardfolk, Self]`. Query groups can overlap. Picking the first enumerated group is not reliable faction identity. Conflicting matching broad rules need an explicit policy before generic group scope replaces the prototype resolver.
- Research NPC remained non-pickpocketable: no resolver loot or native reaction was invented. This proves discovery separation, not compatibility with a real third-party Lizardfolk mod.

Proof: DevTesting/Npc_Editor/Metadata1-result.json and Run-Metadata1/native-metadata.json/native.log. Research assets are only in the disposable server, excluded from the candidate JAR.

## Current restrictions

| System | Current mechanism |
|---|---|
| NPC role identity | Native NPCEntity.getRoleName; exact role kept throughout |
| NPC editor faction | Seven recognized native groups, first match; unresolved has no faction scope; not universal discovery |
| Ownership editor faction | Accepted six-group native-checked menu; unchanged |
| Ownership membership | Native NPCGroup/WorldSupport; persisted FACTION ID may be registered even when editor menu/response support is narrower |
| Pickpocket | Existing explicit role resolver/loot and generated shared interaction assets; discovery cannot create missing behavior |
| Witness | Existing native HyThief group/profile contracts, fresh FOV/LOS/range; participation gates these paths |
| Crime response | Kweebec/Feran faction danger routes, supported personal combat/rally adapters, LOCAL native listeners; no generic faction-to-alarm API |
| Storage editor | Native block origin/entity plus ItemContainerBlock and live ItemContainer; no chest/wardrobe ID allowlist |
| Live container theft | ContainerBlockWindow, exact attributed ItemContainer, live block entity, native transaction pairing and persistent ownership |
| Pursuit | Recognized native target/state/pending-signal contracts; custom AI can be unsupported |

## Storage capability boundary

Native third-party block storage can potentially work without duplicating its implementation. HyStash is the verified optional example; HyThief-alone regression also passed. Editor keeps original inventory and ownership remains per placed container under existing HyThiefOwnershipProof (Owned, Scope, Faction).

Custom GUIs, virtual/remote inventories, non-ContainerBlockWindow windows, wrapper identity changes, non-native transactions, unsupported multiblock origins or missing persistent entities may be outside attribution/interception. Fail without inventing theft from ambiguous transfers. Persistence and transaction round trips need proof for each unfamiliar architecture. No proprietary implementation was copied or modified.

## Recommended follow-up, subject to review

1. Shared read-only native role/group catalog with stable IDs, membership and provenance; distinguish known semantic labels from general groups. No NPC identity edits and no per-mod discovery patch as the primary approach.
2. Approve overlapping-group precedence/conflict handling before dynamic group rule scopes. Do not silently sort/select a winner. Existing approved component/resource IDs and string-key format can remain.
3. Separate default participation from verified capabilities; missing loot or unknown native response contracts stay unavailable. Prefer proven native listeners/state/targets over name-based adapters where safe.
4. Preserve capability-based storage resolution; document window/transaction/persistence requirements and add concrete compatibility proofs when content is available.

Recommendation: manually test this bounded editor now, review the narrow dynamic identity/group-rule follow-up before public 1.5.0 claims, and defer universal crime-AI adapters or unfamiliar virtual storage. No broad refactor was authorized or performed.

## Sources and limits

Installed API/bytecode and native data are authoritative: NPCPlugin, BuilderManager/BuilderInfo/Builder, NPCGroup, AssetMap, WorldSupport, AttitudeGroup, BlockModule, ItemContainerBlock, ContainerBlockWindow, ItemContainer transactions and native store codecs.

Production source: ContainerOwnershipPolicy, OwnershipEditor, PersistentContainerOwnership, ContainerRemovalProbe, ContainerTheftService, ContainerWitnessProfiles, HyThiefContainerAlert, HyThiefPersonalWitnessAlert, NativePursuit. Existing containers/ownership schema are unchanged.

Official context: [Update 7 notes](https://hytale.com/news/2026/10/2026-10-12-modder-facing-patch-notes-update-7) and [NPC technical rundown](https://hytale.com/news/2026/2/npc-technical-rundown). [NPC group documentation](https://docs.hytale.com/assets/npc/groups/) was discoverable but direct retrieval failed this session; conclusions rely on installed APIs and the real registry proof. No newer/pre-release subsystem was adopted.

## Final 1.5.0 release disposition

Falk explicitly deferred broad metadata refactoring from 1.5.0; only an actual defect in existing promised functionality should block release. Reported manual tests passed for bounded editor functionality. Recommend a separately approved later metadata update; universal compatibility is not advertised.
