# Persistent ownership to live theft pipeline — container.9

Date: 2026-10-10. Development checkpoint only; real-client acceptance is pending.

## Result and scope

Clean build succeeded against pinned stable Hytale 0.6.8, Java 25 and Gradle 9.1. Development version is `1.3.1-container.9`, manifest target `=0.6.8`. The two existing inventory deprecation warnings and Gradle deprecation notice remain.

One existing native chest now takes ownership exclusively from its serialized `HyThiefOwnershipProof` component: `Owned=true`, `Scope=NEARBY`. The unowned control takes the same component route and has no ownership component. Four other distance/rear stations retain their previous registry-based diagnostic behavior. This is a targeted integration proof, not a production rollout.

Container sight remains standing 20 / crouched 6. Normal NPC/pickpocket perception remains 15 / 4. Native transfers, chest behavior, visual FOV/LOS, caught text, guard LastSeen delivery and pickpocket logic are unchanged. Packaged Server/Common/config assets exactly match container.8.

## Java walkthrough: old authority

`HyThiefContainerDiagnostics.dispatch` runs on the world thread after the registered inventory packet bridge receives MoveItemStack/SmartMoveItemStack. Native InventoryUtils resolves the source/destination sections. Previously the automatic fixture matched those inventory objects to a temporary coordinate/world registry entry. That entry decided which chest removal would enter witness processing.

The following is the actual container.8 method opening preserved in the rollback:

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
    if (automatic) entry = registry.findAutomatic(world, source, destination);
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
```

## Java walkthrough: serialized data

`Component<ChunkStore>` means this data belongs to the world's chunk/block-entity component store, alongside the chest's native ItemContainerBlock component. It is not a player component or a separate coordinate map. The codec writes Owned and Scope into the native saved block entity; registration teaches Hytale how to load it again. Missing data defaults to unowned/unspecified, never to owned.

The proof uses the existing codec ID and field names from the preceding persistence experiment. The authoring helper attaches the data once to the pristine master, then saves the section. Runtime load and the reset workflow do not attach it again.

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
              (data, value) -> data.scope = value, data -> data.scope).add().build();
  private static ComponentType<ChunkStore, OwnershipProofData> type;
  private boolean owned;
  private String scope = "";
  public OwnershipProofData() {}
  public OwnershipProofData(boolean owned, String scope) { this.owned=owned; this.scope=scope; }
  static void register(PickpocketPlugin plugin) {
    type=plugin.getChunkStoreRegistry().registerComponent(
        OwnershipProofData.class, "HyThiefOwnershipProof", CODEC);
  }
  public static ComponentType<ChunkStore, OwnershipProofData> getComponentType() { return type; }
  public boolean owned() { return owned; }
  public String scope() { return scope; }
  @Override public OwnershipProofData clone() { return new OwnershipProofData(owned,scope); }
}
```

## Java walkthrough: source identity and ownership read

The resolver uses the player's actual native ContainerBlockWindow, its block coordinates and loaded section to find the block entity. It checks exact inventory-object identity against both the window and the entity's ItemContainerBlock. Coordinates locate the live entity; they do not declare ownership. The request-local Key is only a descriptor used by existing attribution/logging code. The resolver neither reads nor writes the ownership registry.

The Source record holds the entity reference and the attributed inventory. Each read rechecks that the entity is valid and still owns that inventory. Only an attached component with Owned=true and exact Scope=NEARBY is eligible.

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
    public boolean ownedNearby() { return eligible(read()); }
  }
  private PersistentContainerOwnership() {}
  public static boolean eligible(OwnershipProofData data) {
    return data!=null && data.owned() && "NEARBY".equals(data.scope());
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

## Java walkthrough: withdrawal to witness processing

Below is the complete current dispatch method. Fixture positions select the experimental route for the proof/control chests, but ownership comes solely from the resolved component. The proof chest is excluded from automatic registry registration. If its entity cannot be resolved, the native transfer remains allowed and SOURCE_UNRESOLVED is logged; no registry fallback occurs. A missing component logs a failed proof and stops crime processing.

ContainerRemovalProbe preserves the existing two-sided successful-transfer attribution. Its raw source-removal callback captures visual evidence at removal time only if the component is eligible. Its confirmed-removal callback reads the component again, then accepts the captured evidence. Insertion, rearrangement and failed/unpaired transfers do not produce caught evidence. After native inventory callbacks/locks unwind, ContainerCaughtNotification.notifyWitnessed sends at most one message per request and HyThiefGuardAlert.deliver uses the existing guard adapter.

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
          && (testWorld.persistentPosition(world,window.getX(),window.getY(),window.getZ())
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
    var evidence = new IdentityHashMap<ContainerRemovalProbe.Removal, ContainerVisualWitness.Evidence>();
    var confirmed = new ArrayList<ContainerVisualWitness.Evidence>();
    var probe = new ContainerRemovalProbe(observed.container(), removal -> {
      var key = observed.key();
      if (attachedSource!=null) {
        log("Request="+request+" Action=SOURCE_RESOLVED Entity="+attachedSource.entity()+" Authority=component NoRegistryFallback=true");
        var data=attachedSource.read();
        log("Request="+request+" Action="+(data==null?"COMPONENT_MISSING":"COMPONENT_FOUND")+" Owned="+(data!=null&&data.owned())+" Scope="+(data==null?"MISSING":data.scope()));
        if(!PersistentContainerOwnership.eligible(data)) {
          boolean proof=testWorld.persistentPosition(world,attachedSource.key().x(),attachedSource.key().y(),attachedSource.key().z());
          log("Request="+request+" Action=UNOWNED_IGNORE ProofFailure="+(proof&&data==null)+" NoRegistryFallback=true");return;
        }
      }
      if (automatic) {
        var visual = evidence.get(removal);
        if (visual != null) { confirmed.add(visual); log("Request=" + request + " " + visual.diagnostic()); }
      }
      log("Request=" + request + " Player=" + player.getUsername() + "/" + player.getUuid()
          + " Chest=" + key.world() + ":" + key.x() + "," + key.y() + "," + key.z()
          + " Slot=" + removal.slot() + " Item=" + removal.item() + " Amount=" + removal.amount()
          + " Action=REMOVE Result=SUCCESS Time=" + removal.time() + " Mono=" + removal.nanoTime());
    }, removal -> {
      if (automatic && (attachedSource==null || attachedSource.ownedNearby())) {
        try { log("Request=" + request + " Action=OWNED_REMOVE_CAPTURE Item=" + removal.item() + " Amount=" + removal.amount()); evidence.put(removal, ContainerVisualWitness.capture(ref, store)); }
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
      var delivery = HyThiefGuardAlert.deliver(plugin, ref, visual.witness(), store, message -> log("Request=" + request + " " + message));
      log("Request=" + request + " Action=" + (delivery.sent() ? "DANGER_SENT" : "DANGER_NOT_SENT")
          + " Witness=" + visual.role() + " Guard=" + delivery.guard() + " Reason=" + delivery.reason());
    });
  }
```

This keeps ownership with the container across save/load and native block-state transitions. It avoids a position-only ownership assertion becoming stale or declaring a replacement chest owned. It does not yet implement ownership transfer on chest movement/destruction, creator tools or production-wide participation.

## Native validation and its limits

Fresh native process validation used the saved pristine candidate with authoring disabled:

| Check | Observed result |
| --- | --- |
| Saved component load | Owned=true, Scope=NEARBY, NoReattach=true |
| Real native window → block entity → component | Resolved successfully |
| Owned=true / visible standing removal | One caught notification |
| Owned=false | No notification |
| Component missing, conflicting legacy registry present | No notification; registry ignored |
| Unowned control | No notification |
| Insertion / internal rearrangement / failed transaction | No notification |
| Five-block crouched withdrawal | One caught notification, inside existing six-block range |
| Existing guard adapter validation | Exact offender marked; Attack.Default; pursuit movement observed |
| Fresh restart after pipeline tests | Owned=true, Scope=NEARBY, no reattachment |

The integration helper exercises real native windows/entities/components and the existing transfer/witness helpers. It does not impersonate a real client's packet ingress. Firebeard's test is required to prove the new ownership boundary through the full live-client path.

Evidence lives under DevTesting/Container_Theft_Test_World:

- `container.9-clean-build.log`
- `container.9-authoring-run.txt` → pristine authoring process
- `container.9-pipeline-validation-run.txt` → fresh reload and pipeline validation
- `VerificationRuns/20261010-181732-ff5aa0f0d30549cb88bf8940edb4ebc8/native.log` → post-pipeline restart
- `container.9-deployment.json`, `container.9-live-reset.txt`, `container.9-final-verification.json`

The playable master was authored separately from guard combat tests. Reset copied all 30 verified master files. Existing reset scripts are unchanged. Protected release archives, prior persistence proof, reusable arena and other test worlds match their before-task hashes. Unrelated installed mods match their prior hashes.

## Deployment and rollback

Installed: `C:\Users\askin\AppData\Roaming\Hytale\UserData\Mods\HyThief-1.3.1-container.9.jar`

SHA256: `8c738499fac0d374b9ab4ba4bb6cc0d9caa75060b48819ad5daf1f566c16f098`

Exactly one HyThief gameplay JAR is active. Prior JAR and complete source/fixture rollback are preserved at:

`E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\Container_Theft_Test_World\Rollback-container.8-before-persistent-integration`

The previous installed JAR is `Installed-container.8.jar` within that directory. The reset archived the dirty live world at `Archives/20261010-181906-e18448cbc6c0455fabbae54e831c9a9e`.

## Firebeard's two-step client test

Launch the already reset **HyThief Container Theft Test World**. No commands are required.

1. Withdraw from **1 CONTROL - UNOWNED**. Expected: normal native transfer, no caught notification and no guard response.
2. Stand on the five-block front pad, facing the chest labeled **3 PERSISTENT OWNED - NEARBY / 5 BLOCKS**. Open it normally; opening alone must do nothing. Withdraw one copper bar while standing. Expected: exactly one “You have been caught stealing!” notification and the existing Razorleaf attack/pursuit response.

CONTROL first avoids testing it after guard aggression. Use the existing Container Theft reset shortcut between caught runs.

For proof withdrawal, inspect the same request sequence for INGRESS → SECTION_RESOLVED → SOURCE_RESOLVED (Authority=component, NoRegistryFallback=true) → COMPONENT_FOUND (Owned=true, Scope=NEARBY) → WITNESS=WITNESSED → CAUGHT_NOTIFICATION → DANGER_SENT. SOURCE_UNRESOLVED or COMPONENT_MISSING is a failed proof, never a reason to enable registry fallback. Early legacy labels OWNED_REQUEST/SECTION_RESOLVED Owned refer to a request-local host descriptor; COMPONENT_FOUND is the decisive ownership evidence.

If response fails, retain that session's server log and report the last successful stage. No additional ownership systems will be implemented before the runtime result and code review.

## Recommended next checkpoint, only after acceptance

Add a narrowly scoped creator/admin targeting tool to attach/edit this native ownership component on a selected container. Production NEARBY participation, data-driven theft-aware NPCs, FACTION and property-group/PREFAB scopes remain later checkpoints.

Reference: current installed stable server API/block-entity behavior and [official block documentation](https://docs.hytale.com/assets/item/block/blocks/). Earlier architecture and persistence findings remain in PRODUCTION_CONTAINER_OWNERSHIP_ARCHITECTURE.md and PERSISTENT_CONTAINER_OWNERSHIP_PROOF.md.
