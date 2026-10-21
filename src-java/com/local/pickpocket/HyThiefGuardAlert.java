package com.local.pickpocket;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.components.messaging.BeaconBroadcast;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import java.util.ArrayList;

/** Deliver one native danger signal; the guard's own role owns combat. */
final class HyThiefGuardAlert {
  private static final String DANGER = "Kweebec_All_Danger";
  private static final double ALERT_RADIUS = 20.0; // Existing merchant beacon radius.

  private HyThiefGuardAlert() {}

  static void signal(PickpocketPlugin plugin, PlayerRef player, String victimRole,
      Ref<EntityStore> victim, Store<EntityStore> store) {
    if (!"Kweebec_Merchant".equals(victimRole)) return;
    deliver(plugin, player == null ? null : player.getReference(), victim, store);
  }

  record Delivery(boolean sent, String reason, Ref<EntityStore> guard) {}

  /** Shared downstream handoff; callers establish caught/witness policy separately. */
  static Delivery deliver(PickpocketPlugin plugin, Ref<EntityStore> offender,
      Ref<EntityStore> victim, Store<EntityStore> store) {
    return deliver(plugin, offender, victim, store, null);
  }

  static Delivery deliver(PickpocketPlugin plugin, Ref<EntityStore> offender,
      Ref<EntityStore> victim, Store<EntityStore> store, java.util.function.Consumer<String> diagnostic) {
    return deliverNative(plugin,offender,victim,store,diagnostic,DANGER,java.util.Set.of("Kweebec_Razorleaf","Kweebec_Razorleaf_Patrol"),"Kweebec");
  }
  static Delivery deliverContainer(PickpocketPlugin plugin,Ref<EntityStore> offender,
      Ref<EntityStore> victim,Store<EntityStore> store,java.util.function.Consumer<String> diagnostic) {
    return deliverContainer(plugin,offender,victim,store,ContainerOwnershipPolicy.faction(victim,store),diagnostic);
  }
  static Delivery deliverContainer(PickpocketPlugin plugin,Ref<EntityStore> offender,
      Ref<EntityStore> victim,Store<EntityStore> store,String faction,java.util.function.Consumer<String> diagnostic) {
    if("Kweebec".equals(faction))return deliver(plugin,offender,victim,store,diagnostic);
    if("Feran".equals(faction))return deliverNative(plugin,offender,victim,store,diagnostic,
        "Feran_Warriors_Attack",java.util.Set.of("Feran_Sharptooth","Feran_Longtooth"),faction);
    return new Delivery(false,"unsupported-witness-faction",null);
  }
  private static Delivery deliverNative(PickpocketPlugin plugin,Ref<EntityStore> offender,
      Ref<EntityStore> victim,Store<EntityStore> store,java.util.function.Consumer<String> diagnostic,
      String danger,java.util.Set<String> responderRoles,String faction) {
    boolean tracing = diagnostic != null;
    if (diagnostic == null) diagnostic = message -> {};
    var trace = diagnostic;
    try {
      diagnostic.accept("Action=DANGER_ADAPTER Offender=" + offender + " Witness=" + victim);
      if (store == null || offender == null || !offender.isValid()
          || victim == null || !victim.isValid() || offender.getStore() != store
          || victim.getStore() != store) {
        return new Delivery(false, "invalid-source-or-offender", null);
      }
      if (!NpcParticipation.allowed(NpcOverrides.Capability.WITNESS,victim,store,true))
        return new Delivery(false,"witness-disabled",null);
      TransformComponent origin = store.getComponent(victim, TransformComponent.getComponentType());
      if (origin == null) {
        return new Delivery(false, "missing-source-transform", null);
      }
      var nearby = new ArrayList<Ref<EntityStore>>();
      store.getResource(NPCPlugin.get().getNpcSpatialResource()).getSpatialStructure()
          .collect(origin.getPosition(), ALERT_RADIUS, nearby);
      Ref<EntityStore> chosen = null;
      double nearest = ALERT_RADIUS * ALERT_RADIUS;
      for (Ref<EntityStore> candidate : nearby) {
        if (candidate == null || !candidate.isValid()) continue;
        NPCEntity npc = store.getComponent(candidate, NPCEntity.getComponentType());
        if (npc == null || !responderRoles.contains(npc.getRoleName())
            || !plugin.isPickpocketableRole(npc.getRoleName())
            || !NpcParticipation.allowed(NpcOverrides.Capability.RESPONSE,candidate,store,true)) continue;
        StateSupport state = store.getComponent(candidate, StateSupport.getComponentType());
        BeaconSupport beacon = store.getComponent(candidate, BeaconSupport.getComponentType());
        TransformComponent position = store.getComponent(candidate, TransformComponent.getComponentType());
        int slot = dangerSlot(beacon,danger);
        String stateName = state == null ? "missing" : state.getStateName();
        boolean ready = npc.getRole() != null && acceptsDanger(stateName) && slot >= 0
            && beacon.isMessageEnabled(slot) && !beacon.isMessageQueued(slot)
            && store.getComponent(candidate, DeathComponent.getComponentType()) == null;
        diagnostic.accept("Action=GUARD_CANDIDATE Faction="+faction+" Role=" + npc.getRoleName() + " Ref=" + candidate
            + " State=" + stateName + " Slot=" + slot + " Enabled=" + (slot >= 0 && beacon.isMessageEnabled(slot))
            + " Queued=" + (slot >= 0 && beacon.isMessageQueued(slot)) + " Ready=" + ready
            + " Position=" + (position == null ? "missing" : position.getPosition()));
        if (!ready || position == null) continue;
        double distanceSq = origin.getPosition().distanceSquared(position.getPosition());
        if (distanceSq <= nearest) {
          chosen = candidate;
          nearest = distanceSq;
        }
      }
      if (chosen == null) {
        return new Delivery(false, "no-ready-native-guard", null);
      }
      // Same one-second lifetime as native ActionBeacon. No forced state, attitude or attack.
      BeaconBroadcast.post(chosen, danger, offender, 1.0, store);
      var beacon = store.getComponent(chosen, BeaconSupport.getComponentType());
      int slot = dangerSlot(beacon,danger);
      diagnostic.accept("Action=NATIVE_DANGER_QUEUED Guard=" + chosen + " Slot=" + slot
          + " Signal=" + danger + " Queued=" + (slot >= 0 && beacon.isMessageQueued(slot))
          + " ExactOffender=" + (slot >= 0 && offender.equals(beacon.peekMessage(slot))));
      var guard = chosen;
      if (tracing) for (long delay : new long[]{250, 1250}) java.util.concurrent.CompletableFuture.delayedExecutor(delay, java.util.concurrent.TimeUnit.MILLISECONDS).execute(() ->
          store.getExternalData().getWorld().execute(() -> {
            if (!guard.isValid() || !offender.isValid()) return;
            var state = store.getComponent(guard, StateSupport.getComponentType());
            var messages = store.getComponent(guard, BeaconSupport.getComponentType());
            var marked = store.getComponent(guard, com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport.getComponentType());
            trace.accept("Action=NATIVE_GUARD_OBSERVATION DelayMs=" + delay + " Guard=" + guard
                + " State=" + (state == null ? "missing" : state.getStateName())
                + " DangerQueued=" + (messages != null && slot >= 0 && messages.isMessageQueued(slot))
                + " OffenderMarked=" + (marked != null && java.util.Arrays.asList(marked.getEntityTargets()).contains(offender)));
          }));
      return new Delivery(true, "native-danger-posted", chosen);
    } catch (RuntimeException exception) {
      plugin.getLogger().at(java.util.logging.Level.WARNING).withCause(exception).log("HyThief guard alert failed");
      return new Delivery(false, "native-danger-error", null);
    }
  }

  private static boolean acceptsDanger(String state) {
    return state != null && (state.equals("Idle") || state.startsWith("Idle.")
        || state.equals("Alerted") || state.startsWith("Alerted."));
  }

  private static int dangerSlot(BeaconSupport beacon,String danger) {
    if (beacon == null || beacon.getMessageSlots() == null) return -1;
    for (int i = 0; i < beacon.getMessageSlots().length; i++) {
      if (danger.equals(beacon.getMessageTextForIndex(i))) return i;
    }
    return -1;
  }

}
