# Production Ownership Checkpoint 2 — persistent NEARBY + FACTION

2026-10-10. Development build `1.3.1-container.10`; target `=0.6.8`. Installed and reset; real-client acceptance pending Firebeard.

## Scope and architecture

The native chest still executes ordinary inventory transfers. A paired successful source removal/player receipt selects its actual native ContainerBlockWindow inventory, resolves the owning block entity and reads the serialized ownership component. All six field chests use this authority, with no coordinate registry fallback. Positions are authoring geometry only, not ownership declarations.

`Owned=false` stops crime processing. `Scope=NEARBY` permits any enabled witness without a faction check. `Scope=FACTION` rejects candidates outside the configured native NPC group before native visual evaluation. A qualifying candidate must still pass the unchanged distance, head-facing/FOV and fresh native opaque-block LOS checks.

Participation is a supplied set of role IDs in the development world marker (`WitnessRoles`), cached once on world load. This is the small policy seam for future data-driven/toggleable participation, not the final general role configuration system. Public non-container builds and other worlds are not enabled by the field marker.

## Verified native identities

Installed stable Assets.zip defines `Server/NPC/Groups/Intelligent/Neutral/Kweebec/Kweebec.json` and `Server/NPC/Groups/Intelligent/Neutral/Feran/Feran.json`. These are actual native NPC group IDs `Kweebec` and `Feran`, used by native role attitude/filter/beacon systems. They are social group identity here, not a new reputation system or an invented loot-based faction.

Membership uses `NPCGroup.getAssetMap().getIndex` and current `WorldSupport.isGroupMember(npc.getRoleIndex(), ref, groupIndex, store)`, the same API invoked by native EntityFilterNPCGroup. The group assets include native role patterns; HyThief does not reproduce prefix matching or infer identity from chest models, names or loot.

| Group | Stationary witness | Native source role | Mobile responder |
| --- | --- | --- | --- |
| Kweebec | Kweebec_Sapling_HyThiefWitness | Kweebec_Sapling / Template_Kweebec_Sapling | Kweebec_Razorleaf |
| Feran | Feran_Civilian_HyThiefWitness | Feran_Civilian / Template_Feran_Civilian | Feran_Sharptooth |

Sharptooth is the native combat-capable guardian/warrior choice; there is no role literally named Feran_Guard among the five supported Feran roles. Its native template has melee/ranged attack sequences and Idle/Alerted/Threaten beacon reception for `Feran_Warriors_Attack`, assigning `LockedTarget` and entering Attack.

Stationary witness variants are world-local development assets. Their native models, attitude identity, controllers and interaction definitions are preserved; their staged Idle instruction requests no BodyMotion/HeadMotion. They are not Frozen, and native role/state/interaction processing stays enabled. The existing head-based visual evaluator supplies unchanged perception/LOS for the witness test. They deliberately do not reproduce civilian wandering, sleep or flight in this controlled field.

Both responders remain mobile. Razorleaf keeps its preceding world-local native LastSeen receiver. Sharptooth has two narrow field-only accommodations: its idle camp-leaving/sleep choices are gated off and its idle path motion requests Nothing; its attack-beacon branch uses the same native StorePosition/LastSeen capture as the already-proven Razorleaf receiver. No movement loops, attack timing, forced combat-state polling or custom combat AI were added. Native Attack/Threaten/Search/chase implementations remain intact, and normal HyThief 15/4 perception is unchanged.

## Six-station field

All native chests: Furniture_Kweebec_Chest_Large. Loot: 16 copper bars, 8 iron bars. Feet Y=64; brick standing pads at Z=11.5. Witnesses at (-4.5,64,0.5) and (4.5,64,0.5), facing +Z (native yaw pi). Opaque walls at X=-8 and X=7, Z=5..6, Y=64..67 create the two separate blocked sight lines. Every opposing witness stays inside the unchanged standing range/FOV.

| Station | Chest XYZ | Ownership | Kweebec LOS | Feran LOS | Expected standing withdrawal |
| --- | --- | --- | --- | --- | --- |
| 1 CONTROL | -6,64,12 | Owned=false | clear | clear | no caught/response |
| 2 NEARBY | -2,64,12 | Owned=true, NEARBY | clear | clear | one caught + one selected response |
| 3 KWEEBEC FACTION | 2,64,12 | Owned=true, FACTION, Kweebec | clear | clear | Kweebec caught/response only |
| 4 FERAN FACTION | 6,64,12 | Owned=true, FACTION, Feran | clear | clear | Feran caught/response only |
| 5 KWEEBEC BLOCKED | -12,64,12 | Owned=true, FACTION, Kweebec | blocked | clear | no caught/response |
| 6 FERAN BLOCKED | 11,64,12 | Owned=true, FACTION, Feran | clear | blocked | no caught/response |

The central hub spawn remains (0.5,64.1,7.5). Native responders begin in separate rear alcoves: Razorleaf (-19.5,64,-8.5), Sharptooth (19.5,64,-8.5). Their distinct models and nameplates identify the reacting side. Open rear exits allow native pursuit; native validation confirmed movement around the shields.

NEARBY keeps the existing single downstream notification/handoff. Both eligible visual results are retained and logged. Role-name ordering makes selection deterministic: Feran is selected first when both see the same transfer. This checkpoint does not broadcast to both groups or claim a general multi-witness crime system.

## Java walkthrough: persistent schema

The existing `HyThiefOwnershipProof` codec ID and Owned/Scope keys remain. An additive `Faction` string defaults to empty, so old NEARBY data remains valid. FACTION data without a valid native group is ineligible. The component is stored in ChunkStore with the native container entity/inventory; it survives native block-state changes and save/restart.

```java
package com.local.pickpocket;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

/** Container development builds only; same serialized fields/ID as the persistence proof. */
public final class OwnershipProofData implements Component<ChunkStore> {
  public static final BuilderCodec<OwnershipProofData> CODEC =
      BuilderCodec.builder(OwnershipProofData.class, OwnershipProofData::new)
          .append(new KeyedCodec<>("Owned", Codec.BOOLEAN),
              (data, value) -> data.owned = value, data -> data.owned).add()
          .append(new KeyedCodec<>("Scope", Codec.STRING),
              (data, value) -> data.scope = value, data -> data.scope).add()
          .append(new KeyedCodec<>("Faction", Codec.STRING),
              (data, value) -> data.faction = value, data -> data.faction).add().build();
  private static ComponentType<ChunkStore, OwnershipProofData> type;
  private boolean owned;
  private String scope = "";
  private String faction = "";
  public OwnershipProofData() {}
  public OwnershipProofData(boolean owned, String scope) { this.owned=owned; this.scope=scope; }
  public OwnershipProofData(boolean owned,String scope,String faction) { this(owned,scope);this.faction=faction; }
  public String faction() { return faction; }
  static void register(PickpocketPlugin plugin) {
    type=plugin.getChunkStoreRegistry().registerComponent(
        OwnershipProofData.class, "HyThiefOwnershipProof", CODEC);
  }
  public static ComponentType<ChunkStore, OwnershipProofData> getComponentType() { return type; }
  public boolean owned() { return owned; }
  public String scope() { return scope; }
  @Override public OwnershipProofData clone() { return new OwnershipProofData(owned,scope,faction); }
}
```

## Java walkthrough: source resolution

This class verifies exact inventory-object identity between the attributed transfer, actual native window and ItemContainerBlock. A Key names the request host for diagnostics; it never consults the ownership registry. `eligible` validates the ownership scope before any visual crime processing.

```java
package com.local.pickpocket;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

/** Reads the live window's native block entity. No ownership registry, writes or fallback. */
public final class PersistentContainerOwnership {
  public record Source(OwnedContainerRegistry.Key key, Ref<ChunkStore> entity, ItemContainer container) {
    public OwnershipProofData read() {
      if(entity==null || !entity.isValid())throw new IllegalStateException("Source entity invalid");
      var store=entity.getStore();
      var inventory=store.getComponent(entity,ItemContainerBlock.getComponentType());
      if(inventory==null || inventory.getItemContainer()!=container)
        throw new IllegalStateException("Source inventory/entity no longer match");
      return store.getComponent(entity,OwnershipProofData.getComponentType());
    }
    public boolean ownedNearby() { var data=read(); return eligible(data) && "NEARBY".equals(data.scope()); }
  }
  private PersistentContainerOwnership() {}
  public static boolean eligible(OwnershipProofData data) {
    return data!=null && data.owned() && ("NEARBY".equals(data.scope())
        || ("FACTION".equals(data.scope()) && com.hypixel.hytale.builtin.tagset.config.NPCGroup.getAssetMap().getIndex(data.faction())>=0));
  }
  public static Source resolve(World world,ContainerBlockWindow window,ItemContainer attributed) {
    if(window==null || attributed==null || window.getItemContainer()!=attributed)return null;
    var cs=world.getChunkStore();var section=cs.getChunkSectionReferenceAtBlock(window.getX(),window.getY(),window.getZ());
    if(section==null || !section.isValid())return null;
    var entity=BlockModule.getBlockEntity(cs.getStore(),section,window.getX(),window.getY(),window.getZ());
    if(entity==null || !entity.isValid())return null;
    var inventory=cs.getStore().getComponent(entity,ItemContainerBlock.getComponentType());
    if(inventory==null || inventory.getItemContainer()!=attributed)return null;
    return new Source(new OwnedContainerRegistry.Key(world.getWorldConfig().getUuid(),window.getX(),window.getY(),window.getZ()),entity,attributed);
  }
}
```

## Java walkthrough: NEARBY vs FACTION

Before: an Owned=true/NEARBY component allowed the one controlled Kweebec visual check. After: a reusable policy checks the supplied candidate's native group only for FACTION. Returning null means socially eligible; it does not mean the candidate saw anything. Faction() identifies the two explicitly supported response families for diagnostics/adapter routing. This limited response catalog is distinct from the general membership API.

```java
package com.local.pickpocket;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import java.util.*;

/** Small policy seam: fixture participation supplied by data; native NPC groups define faction membership. */
public final class ContainerOwnershipPolicy {
  private ContainerOwnershipPolicy() {}
  public static boolean member(Ref<EntityStore> ref,String group,Store<EntityStore> store) {
    var npc=store.getComponent(ref,NPCEntity.getComponentType());
    int index=NPCGroup.getAssetMap().getIndex(group);
    return npc!=null && index>=0 && WorldSupport.isGroupMember(npc.getRoleIndex(),ref,index,store);
  }
  public static String faction(Ref<EntityStore> ref,Store<EntityStore> store) {
    for(String id:List.of("Kweebec","Feran"))if(member(ref,id,store))return id;
    return "UNRESOLVED";
  }
  public static String rejection(OwnershipProofData data,Ref<EntityStore> ref,Store<EntityStore> store) {
    if(!PersistentContainerOwnership.eligible(data))return "UNOWNED_OR_INVALID_SCOPE";
    if("NEARBY".equals(data.scope()))return null; // No faction filter.
    return member(ref,data.faction(),store)?null:"FACTION_MISMATCH";
  }
}
```

## Java walkthrough: ownership filtering ends, sight evaluation begins

The new capture overload gathers alive, enabled roles within standing range, sorts candidates and rejects faction mismatches before calling evaluate(). The evaluate method immediately below is unchanged: crouch chooses 6 vs 20, native head yaw determines the 180-degree sector, and a private fresh PositionCache checks native opaque-block LOS without clearing or modifying NPC AI perception.

```java
  static List<Evidence> capture(Ref<EntityStore> player, Store<EntityStore> store,
      OwnershipProofData ownership, java.util.Set<String> enabledRoles, java.util.function.Consumer<String> log) {
    var result=new ArrayList<Evidence>();
    if(player==null || !player.isValid())return result;
    var t=store.getComponent(player,TransformComponent.getComponentType());
    if(t==null)return result;
    var movement=store.getComponent(player,MovementStatesComponent.getComponentType());
    boolean crouched=movement!=null && movement.getMovementStates().crouching;
    var position=new Vector3d(t.getPosition());
    var candidates=new ArrayList<Ref<EntityStore>>();
    store.forEachChunk(NPCEntity.getComponentType(),(chunk,buffer)->{
      for(int n=0;n<chunk.size();n++) {
        var ref=chunk.getReferenceTo(n);var npc=store.getComponent(ref,NPCEntity.getComponentType());
        var transform=store.getComponent(ref,TransformComponent.getComponentType());
        if(npc.getRole()!=null && enabledRoles.contains(npc.getRoleName()) && transform!=null
            && transform.getPosition().distance(position)<=RANGE
            && store.getComponent(ref,DeathComponent.getComponentType())==null)candidates.add(ref);
      }
    });
    candidates.sort(java.util.Comparator.comparing(ref->store.getComponent(ref,NPCEntity.getComponentType()).getRoleName()));
    for(var candidate:candidates) {
      var npc=store.getComponent(candidate,NPCEntity.getComponentType());
      String faction=ContainerOwnershipPolicy.faction(candidate,store);
      String rejected=ContainerOwnershipPolicy.rejection(ownership,candidate,store);
      log.accept("Action=OWNERSHIP_CANDIDATE Role="+npc.getRoleName()+" Faction="+faction
          +" Eligible="+(rejected==null)+" Reason="+(rejected==null?"scope-match":rejected));
      if(rejected!=null)continue;
      var evidence=evaluate(candidate,player,position,crouched,store);
      log.accept("Action=VISUAL_CANDIDATE Faction="+faction+" "+evidence.diagnostic());
      result.add(evidence);
    }
    return result;
  }
  static Evidence evaluate(Ref<EntityStore> witness, Ref<EntityStore> player,
      Vector3d position, boolean crouched, Store<EntityStore> store) {
    var t = store.getComponent(witness, TransformComponent.getComponentType());
    var head = store.getComponent(witness, HeadRotation.getComponentType());
    var npc = store.getComponent(witness, NPCEntity.getComponentType());
    if (t == null || head == null || npc == null) return missing("missing-witness-components");
    double distance = t.getPosition().distance(position);
    boolean sector = NPCPhysicsMath.inViewSector(t.getPosition().x, t.getPosition().z,
        head.getRotation().yaw(), SECTOR, position.x, position.z);
    // A private native cache for this observation avoids stale LOS from a frozen NPC,
    // and does not clear or mutate that NPC's AI cache/target/perception state.
    var sight = new PositionCache();
    sight.setOpaqueBlockSet(-1); // native block opacity, no custom opaque-set policy
    double range = visualRange(crouched, npc.getRoleName());
    boolean los = distance <= range && sector && sight.hasLineOfSight(witness, player, store);
    String reason = distance > range ? "range" : !sector ? "sector" : !los ? "occluded" : "seen";
    return new Evidence(witness, npc.getRoleName(), distance, sector, los, crouched,
        new Vector3d(position), reason, new Vector3d(t.getPosition()), head.getRotation().yaw());
  }
```

## Java walkthrough: complete withdrawal method context

The packet bridge and InventoryUtils transfer calls are unchanged. Raw source removal captures ownership-filtered visual evidence; paired successful removal reads attached ownership again and accepts that evidence. Existing insertion/rearrangement/failed-transfer attribution is unchanged. Only after native transfer callbacks/locks unwind does the existing notifier emit at most one caught message, followed by one selected faction response.

```java
  private void dispatch(PlayerRef player, Packet packet, World world) {
    var ref = player.getReference();
    if (ref == null || !ref.isValid() || ref.getStore().getExternalData().getWorld() != world) return;
    var store = ref.getStore();
    boolean automatic = testWorld.enabled(world);
    if (automatic) testWorld.refresh(world);
    var entry = registry.current();
    if (!automatic && (!participants.contains(player.getUuid()) || !registry.valid(world, entry))) {
      fallback(player, packet); return;
    }
    int from = packet instanceof MoveItemStack m ? m.fromSectionId : ((SmartMoveItemStack) packet).fromSectionId;
    int to = packet instanceof MoveItemStack m ? m.toSectionId : Integer.MIN_VALUE;
    // Preserve native utility active-slot request/event side effects; not attributed in this proof.
    if (to == InventoryComponent.UTILITY_SECTION_ID) { log("Action=PASSTHROUGH Reason=utility-section"); fallback(player, packet); return; }
    ItemContainer source = InventoryUtils.getSectionById(ref, from, store);
    ItemContainer destination = packet instanceof MoveItemStack ? InventoryUtils.getSectionById(ref, to, store) : null;
    PersistentContainerOwnership.Source persistent = null;
    boolean componentRoute = false;
    if(automatic) {
      var entity=store.getComponent(ref,Player.getComponentType());
      for(var open:entity.getWindowManager().getWindows())if(open instanceof ContainerBlockWindow window
          && (window.getItemContainer()==source || window.getItemContainer()==destination)
          && (testWorld.persistentField(world) || testWorld.persistentPosition(world,window.getX(),window.getY(),window.getZ())
              || testWorld.controlPosition(world,window.getX(),window.getY(),window.getZ()))) {
        componentRoute=true;
        persistent=PersistentContainerOwnership.resolve(world,window,window.getItemContainer());
        break;
      }
      if(componentRoute) {
        if(persistent==null) { log("Action=SOURCE_UNRESOLVED Authority=component NoRegistryFallback=true"); fallback(player,packet); return; }
        // Entry is only a request-local host descriptor; never registered as ownership.
        entry=new OwnedContainerRegistry.Entry(persistent.key(),persistent.container());
      } else entry = registry.findAutomatic(world, source, destination);
    }
    final var attachedSource=persistent;
    log("Action=SECTION_RESOLVED Player=" + player.getUuid() + " From=" + from + " To=" + to
        + " Source=" + identity(source) + " Destination=" + identity(destination)
        + " Owned=" + (entry == null ? "none" : entry.key())
        + " Registered=" + registry.automatic(world).stream().map(e -> e.key() + "/" + identity(e.container())).toList());
    if (entry == null || (source != entry.container() && destination != entry.container())) {
      if (automatic && source != null && testWorld.isControl(world, source))
        log("Player=" + player.getUuid() + " Action=CONTROL_UNOWNED_IGNORE");
      log("Action=PASSTHROUGH Reason=no-owned-container-match");
      fallback(player, packet); return;
    }
    if (store.getArchetype(ref).contains(PreventInventoryAccess.getComponentType())) {
      log("Player=" + player.getUuid() + " Action=IGNORE Reason=inventory-access-blocked"); return;
    }
    long request = sequence.incrementAndGet();
    var observed = entry;
    log("Request=" + request + " Action=OWNED_REQUEST Offender=" + ref + " Player=" + player.getUuid() + " Chest=" + observed.key());
    var evidence = new IdentityHashMap<ContainerRemovalProbe.Removal, java.util.List<ContainerVisualWitness.Evidence>>();
    var confirmed = new ArrayList<ContainerVisualWitness.Evidence>();
    var probe = new ContainerRemovalProbe(observed.container(), removal -> {
      var key = observed.key();
      if (attachedSource!=null) {
        log("Request="+request+" Action=SOURCE_RESOLVED Entity="+attachedSource.entity()+" Authority=component NoRegistryFallback=true");
        var data=attachedSource.read();
        log("Request="+request+" Action="+(data==null?"COMPONENT_MISSING":"COMPONENT_FOUND")+" Owned="+(data!=null&&data.owned())+" Scope="+(data==null?"MISSING":data.scope())+" Faction="+(data==null?"MISSING":data.faction()));
        if(!PersistentContainerOwnership.eligible(data)) {
          boolean proof=testWorld.persistentPosition(world,attachedSource.key().x(),attachedSource.key().y(),attachedSource.key().z());
          log("Request="+request+" Action=UNOWNED_IGNORE ProofFailure="+(proof&&data==null)+" NoRegistryFallback=true");return;
        }
      }
      if (automatic) {
        var visual = evidence.get(removal);
        if (visual != null) { confirmed.addAll(visual); for(var seen:visual)log("Request=" + request + " " + seen.diagnostic()); }
      }
      log("Request=" + request + " Player=" + player.getUsername() + "/" + player.getUuid()
          + " Chest=" + key.world() + ":" + key.x() + "," + key.y() + "," + key.z()
          + " Slot=" + removal.slot() + " Item=" + removal.item() + " Amount=" + removal.amount()
          + " Action=REMOVE Result=SUCCESS Time=" + removal.time() + " Mono=" + removal.nanoTime());
    }, removal -> {
      if (automatic && (attachedSource==null || PersistentContainerOwnership.eligible(attachedSource.read()))) {
        try { log("Request=" + request + " Action=OWNED_REMOVE_CAPTURE Item=" + removal.item() + " Amount=" + removal.amount()); evidence.put(removal, attachedSource==null ? java.util.List.of(ContainerVisualWitness.capture(ref,store))
            : ContainerVisualWitness.capture(ref,store,attachedSource.read(),testWorld.witnessRoles(world),message->log("Request="+request+" "+message))); }
        catch (RuntimeException error) { log("Request=" + request + " WITNESS=UNSEEN Reason=visual-error:" + error.getClass().getSimpleName()); plugin.getLogger().atWarning().withCause(error).log("HyThief container visual capture failed"); }
      }
    });
    var registrations = new ArrayList<com.hypixel.hytale.event.EventRegistration<?, ?>>();
    boolean executed = false;
    try {
      registrations.add(entry.container().registerChangeEvent(e -> probe.source(e.transaction())));
      for (var type : InventoryComponent.EVERYTHING) {
        var component = store.getComponent(ref, type);
        if (component != null) registrations.add(component.getInventory().registerChangeEvent(e -> probe.receipt(e.transaction())));
      }
      executed = true;
      if (packet instanceof MoveItemStack m) {
        InventoryUtils.moveItem(ref, m.fromSectionId, m.fromSlotId, m.quantity, m.toSectionId, m.toSlotId, store);
      } else {
        var m = (SmartMoveItemStack) packet;
        var settings = store.getComponent(ref, PlayerSettings.getComponentType());
        InventoryUtils.smartMoveItem(ref, m.fromSectionId, m.fromSlotId, m.quantity, m.moveType, settings, store);
      }
      if (probe.finish() == 0) log("Request=" + request + " Player=" + player.getUuid()
          + " Action=IGNORE Reason=no-paired-chest-removal");
    } catch (RuntimeException error) {
      log("Request=" + request + " Action=ERROR Reason=" + error.getClass().getSimpleName()
          + " NativeExecutionStarted=" + executed);
      // Never replay a possibly applied native operation.
      if (!executed) fallback(player, packet);
    } finally {
      for (var event : registrations) event.unregister();
    }
    // All inventory locks/callbacks have unwound. One downstream notification per request.
    ContainerCaughtNotification.notifyWitnessed(confirmed, player::sendMessage).ifPresent(visual -> {
      log("Request=" + request + " Action=CAUGHT_NOTIFICATION Player=" + player.getUuid() + " Witness=" + visual.role());
      var delivery = HyThiefGuardAlert.deliverContainer(plugin, ref, visual.witness(), store, message -> log("Request=" + request + " " + message));
      log("Request=" + request + " Action=" + (delivery.sent() ? "DANGER_SENT" : "DANGER_NOT_SENT")
          + " Witness=" + visual.role() + " Guard=" + delivery.guard() + " Reason=" + delivery.reason());
    });
  }
```

## Java walkthrough: native responder handoff

Before: the shared adapter looked for a ready Razorleaf and posted Kweebec_All_Danger. That original overload remains the pickpocket entry. The new container overload selects a response family from the confirmed witness's native membership and passes the native message/role set into the same nearest-ready beacon delivery routine. Radius, readiness rules, one-second message lifetime, exact offender reference, notification cleanup and native combat ownership are preserved.

```java
  static Delivery deliverContainer(PickpocketPlugin plugin,Ref<EntityStore> offender,
      Ref<EntityStore> victim,Store<EntityStore> store,java.util.function.Consumer<String> diagnostic) {
    String faction=ContainerOwnershipPolicy.faction(victim,store);
    if("Kweebec".equals(faction))return deliver(plugin,offender,victim,store,diagnostic);
    if("Feran".equals(faction))return deliverNative(plugin,offender,victim,store,diagnostic,
        "Feran_Warriors_Attack",java.util.Set.of("Feran_Sharptooth","Feran_Longtooth"),faction);
    return new Delivery(false,"unsupported-witness-faction",null);
  }
```

The existing native delivery still calls `BeaconBroadcast.post(chosen, danger, offender, 1.0, store)`. BeaconSupport verifies the exact queued offender. The responding role's sensor assigns LockedTarget, the native StorePosition action captures LastSeen, and native role states/motions own the subsequent chase/attack.

The new field-only Feran receiver adds that capture at its native beacon boundary:

```json
"Actions": [
  { "Type": "StorePosition", "Slot": "LastSeen" },
  { "Type": "State", "State": "Attack" }
]
```

This extends the existing LastSeen handoff to the newly introduced family; it does not change the established Razorleaf LastSeen behavior or native search/combat rules. Field assets remain world-local.

## Native results and diagnosed staging failures

Clean final build succeeded; the same two inventory deprecation warnings and Gradle deprecation notice remain. All 185 supported pickpocket roles/generated interaction nodes are unchanged. Packaged Server/Common/config asset hashes match container.9.

All six station cases passed actual native window/entity component reads, paired native transfers, faction filtering and visual evaluation. Stations 1/5/6 emitted zero caught notifications; stations 2/3/4 emitted exactly one. Both front witnesses were physically in range/FOV. Native LOS gave the intended asymmetric results for 5 and 6. Native OpenWindow/CloseWindow transitions retained the same inventory and ownership/faction data.

Kweebec response: Attack.Default, exact offender retained, movement 7.890172348192109, wrong faction not targeted. Feran response: Attack.Ranged, exact offender retained, movement 12.268697664122262, wrong faction not targeted. These tests use an inert modeled ECS actor, not a real client packet session.

Three issues were isolated rather than hidden by changing range/visual policy:

- Initial staged yaw looked away from the field; corrected authoring orientation to pi.
- Native Sharptooth idle wandered/slept, so it was not a ready beacon receiver. Field-only idle gates keep the responder ready.
- The native Feran beacon branch omitted LastSeen capture and entered Search immediately. Reusing the proven native StorePosition handoff retained Attack.Ranged. A subsequent harness failure came from missing Velocity on the inert target; adding the target's required native component fixed ranged aiming without changing gameplay.

Fresh server reloads read all six persisted Owned/Scope/Faction values without reattachment. A further fresh restart from the saved post-transfer/response world also passed all six cases. The playable master uses the separately authored pristine save, not a combat-test save.

Evidence under DevTesting/Container_Theft_Test_World:

- container.10-clean-build.log
- container.10-pristine-author-run.txt
- container.10-kweebec-validation-run.txt
- container.10-feran-validation-run.txt
- container.10-restart-validation-run.txt
- container.10-deployment.json / container.10-live-reset.txt / container.10-final-verification.json

Existing startup warnings from stealth sensor JSON attributes remain; they were not introduced or repaired in this checkpoint. Real-client native packet/window routing and actual player combat remain Firebeard's acceptance tests.

## Rollback and deployment

Installed: C:\Users\askin\AppData\Roaming\Hytale\UserData\Mods\HyThief-1.3.1-container.10.jar. Exactly one HyThief JAR is active.

Rollback: E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\Container_Theft_Test_World\Rollback-container.9-before-faction. Contains complete source, previous master/verification/reset architecture and Installed-container.9.jar. Reset archived the dirty live world at Archives/20261010-185744-c1e0b15d4053428085a96701c88a0827.

The reset copied/verified all 44 master files. Existing reset scripts are byte-identical. Protected world/project/release inventories and unrelated installed mods match their before-task hashes. Old range geometry remains in rollback; the active save has only the new flat field.

## Firebeard client test

Launch the already reset HyThief Container Theft Test World; no commands.

1. Stand on the brick pads and withdraw one copper bar from stations 1, 5, 6. Expect no caught message or responder reaction.
2. Station 3: standing withdrawal should emit one caught message and visibly activate the Kweebec Razorleaf only.
3. Close Hytale and use the existing reset shortcut.
4. Station 4: standing withdrawal should emit one caught message and visibly activate Feran Sharptooth only.
5. Close Hytale and reset.
6. Station 2: both witnesses are socially eligible. Expect one message and the current selected Feran-side handoff.

Opening/browsing alone must stay quiet at every station. Use standing for these comparisons: the station pads are beyond crouched range. Report any unsolicited response before withdrawal, wandering/rotation of witnesses, wrong responding faction, missing notification or missing response. Preserve that run's server log.

Relevant log boundaries: SOURCE_RESOLVED (Authority=component, NoRegistryFallback=true), COMPONENT_FOUND (Owned/Scope/Faction), OWNERSHIP_CANDIDATE (Faction/Eligible/FACTION_MISMATCH), VISUAL_CANDIDATE (range, sector, LOS), CAUGHT_NOTIFICATION, GUARD_CANDIDATE (Faction/readiness), NATIVE_DANGER_QUEUED (signal/exact offender), NATIVE_GUARD_OBSERVATION, DANGER_SENT. Early OWNED_REQUEST still describes the request host, not the final ownership verdict.

No PREFAB, creator UI, generalized all-NPC configuration, reputation, stolen flags or other banked systems were implemented. Stop for runtime results and Java review. Recommended next checkpoint only after acceptance: a targeted ownership-editing tool, followed by production data-driven witness participation.

References: current installed 0.6.8 native group/role assets and server API; [official role attitude/group documentation](https://docs.hytale.com/assets/npc/attitude/roles/), [Update 6 patch notes](https://hytale.com/news/2026/8/update-6-patch-notes). Update 7 remains pre-release and was not adopted.

## Open-field correction (2026-10-10)
The responder shelters and both built-in witness occlusion walls were removed after this checkpoint. Stations 5/6 are now manual LOS tests and are witnessed by default. See CONTAINER_FIELD_OPEN_GEOMETRY.md and the current test-world README. No production gameplay changes.
