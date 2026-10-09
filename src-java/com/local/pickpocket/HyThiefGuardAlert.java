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
    try {
      if (store == null || offender == null || !offender.isValid()
          || victim == null || !victim.isValid() || offender.getStore() != store
          || victim.getStore() != store) {
        return new Delivery(false, "invalid-source-or-offender", null);
      }
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
        if (npc == null || !("Kweebec_Razorleaf".equals(npc.getRoleName())
            || "Kweebec_Razorleaf_Patrol".equals(npc.getRoleName()))
            || !plugin.isPickpocketableRole(npc.getRoleName())) continue;
        StateSupport state = store.getComponent(candidate, StateSupport.getComponentType());
        BeaconSupport beacon = store.getComponent(candidate, BeaconSupport.getComponentType());
        TransformComponent position = store.getComponent(candidate, TransformComponent.getComponentType());
        int slot = dangerSlot(beacon);
        String stateName = state == null ? "missing" : state.getStateName();
        boolean ready = npc.getRole() != null && acceptsDanger(stateName) && slot >= 0
            && beacon.isMessageEnabled(slot) && !beacon.isMessageQueued(slot)
            && store.getComponent(candidate, DeathComponent.getComponentType()) == null;
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
      BeaconBroadcast.post(chosen, DANGER, offender, 1.0, store);
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

  private static int dangerSlot(BeaconSupport beacon) {
    if (beacon == null || beacon.getMessageSlots() == null) return -1;
    for (int i = 0; i < beacon.getMessageSlots().length; i++) {
      if (DANGER.equals(beacon.getMessageTextForIndex(i))) return i;
    }
    return -1;
  }

}
