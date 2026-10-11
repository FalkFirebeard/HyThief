package com.local.pickpocket;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.components.messaging.BeaconBroadcast;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Ownership selects the crime signal; native Beacon sensors and role actions own the response. */
final class HyThiefContainerAlert {
  static final String LOCAL = "HyThief_Local_Theft";
  // Native Kweebec/Feran role alarm distance. Independent of 20/6 visual witnessing.
  static final double LOCAL_RADIUS = 30.0;
  record Result(String type, String signal, List<Ref<EntityStore>> responders, String reason) {
    boolean sent() { return !responders.isEmpty(); }
  }
  private HyThiefContainerAlert() {}

  static Result deliver(PickpocketPlugin plugin, OwnershipProofData ownership,
      Ref<EntityStore> offender, Ref<EntityStore> witness, Store<EntityStore> store,
      Consumer<String> diagnostic) {
    Consumer<String> trace = diagnostic == null ? message -> {} : diagnostic;
    if (!PersistentContainerOwnership.eligible(ownership))
      return new Result("NONE", "", List.of(), "invalid-ownership");
    if (store == null || offender == null || witness == null || !offender.isValid()
        || !witness.isValid() || offender.getStore() != store || witness.getStore() != store)
      return new Result("NONE", "", List.of(), "invalid-offender-or-witness");
    if ("FACTION".equals(ownership.scope())) {
      if (ContainerOwnershipPolicy.rejection(ownership, witness, store) != null)
        return new Result("FACTION", "", List.of(), "witness-faction-mismatch");
      String signal = switch (ownership.faction()) {
        case "Kweebec" -> "Kweebec_All_Danger";
        case "Feran" -> "Feran_Warriors_Attack";
        default -> "";
      };
      if (signal.isEmpty()) return new Result("FACTION", "", List.of(), "unsupported-faction");
      trace.accept("Action=THEFT_ALERT_CLASSIFIED Type=FACTION Faction=" + ownership.faction() + " Signal=" + signal);
      var delivery = HyThiefGuardAlert.deliverContainer(plugin, offender, witness, store, ownership.faction(), trace);
      return new Result("FACTION", signal, delivery.sent() ? List.of(delivery.guard()) : List.of(), delivery.reason());
    }
    if (!"NEARBY".equals(ownership.scope()))
      return new Result("NONE", "", List.of(), "unsupported-scope");
    trace.accept("Action=THEFT_ALERT_CLASSIFIED Type=LOCAL Signal=" + LOCAL + " Radius=" + LOCAL_RADIUS);
    var origin = store.getComponent(witness, TransformComponent.getComponentType());
    if (origin == null) return new Result("LOCAL", LOCAL, List.of(), "missing-witness-transform");
    var candidates = new ArrayList<Ref<EntityStore>>();
    store.getResource(NPCPlugin.get().getNpcSpatialResource()).getSpatialStructure()
        .collect(origin.getPosition(), LOCAL_RADIUS, candidates);
    var delivered = new ArrayList<Ref<EntityStore>>();
    for (var candidate : candidates) {
      if (candidate == null || !candidate.isValid() || candidate.equals(offender)) continue;
      var npc = store.getComponent(candidate, NPCEntity.getComponentType());
      if (npc == null || npc.getRole() == null || !plugin.isPickpocketableRole(npc.getRoleName())) continue;
      var beacon = store.getComponent(candidate, BeaconSupport.getComponentType());
      int slot = slot(beacon);
      if (slot < 0) continue; // Native role sensor is the data-driven opt-in, not a Java role-ID list.
      var state = store.getComponent(candidate, StateSupport.getComponentType());
      String name = state == null ? "missing" : state.getStateName();
      boolean ready = (name.equals("Idle") || name.startsWith("Idle.") || name.equals("Alerted") || name.startsWith("Alerted."))
          && beacon.isMessageEnabled(slot) && !beacon.isMessageQueued(slot)
          && store.getComponent(candidate, DeathComponent.getComponentType()) == null;
      trace.accept("Action=LOCAL_LISTENER Role=" + npc.getRoleName() + " State=" + name + " Slot=" + slot + " Ready=" + ready);
      if (!ready || delivered.contains(candidate)) continue;
      try {
        BeaconBroadcast.post(candidate, LOCAL, offender, 1.0, store);
        boolean accepted = beacon.isMessageQueued(slot) && offender.equals(beacon.peekMessage(slot));
        trace.accept("Action=LOCAL_DANGER_QUEUED Role=" + npc.getRoleName() + " Signal=" + LOCAL + " ExactOffender=" + accepted);
        if (accepted) delivered.add(candidate);
      } catch (RuntimeException error) {
        plugin.getLogger().at(java.util.logging.Level.WARNING).withCause(error).log("HyThief local theft beacon delivery failed");
        trace.accept("Action=LOCAL_DANGER_ERROR Role=" + npc.getRoleName());
      }
    }
    return new Result("LOCAL", LOCAL, List.copyOf(delivered), delivered.isEmpty() ? "no-ready-local-listener" : "native-local-beacon-posted");
  }
  private static int slot(BeaconSupport beacon) {
    if (beacon == null || beacon.getMessageSlots() == null) return -1;
    for (int i = 0; i < beacon.getMessageSlots().length; i++)
      if (LOCAL.equals(beacon.getMessageTextForIndex(i))) return i;
    return -1;
  }
}
