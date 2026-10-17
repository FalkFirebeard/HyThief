package com.local.pickpocket;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import java.util.Set;

/** Reads loaded native NPC engagement on demand. No heat, memory cache, radius or AI writes. */
final class NativePursuit {
  private static final Set<String> ENGAGED = Set.of("Attack", "Combat", "Chase", "Search", "Converge");
  private static final Set<String> DANGER = Set.of("Kweebec_All_Danger", "Feran_Warriors_Attack",
      "TrorkEnemySighted", HyThiefContainerAlert.LOCAL);
  private NativePursuit() {}

  static boolean engagedState(String state) {
    if (state == null) return false;
    return ENGAGED.contains(state.split("\\.", 2)[0])
        || state.equals("Threaten.Angry") || state.equals("Threaten.Converge");
  }

  static boolean blocks(Ref<EntityStore> player, Store<EntityStore> store) {
    if (player == null || !player.isValid() || player.getStore() != store) return false;
    boolean[] blocked = {false};
    store.forEachChunk(NPCEntity.getComponentType(), (chunk, buffer) -> {
      for (int i = 0; i < chunk.size() && !blocked[0]; i++) {
        var npc = chunk.getReferenceTo(i);
        if (store.getComponent(npc, DeathComponent.getComponentType()) != null) continue;
        var state = store.getComponent(npc, StateSupport.getComponentType());
        var marked = store.getComponent(npc, MarkedEntitySupport.getComponentType());
        if (state != null && marked != null && engagedState(state.getStateName())
            && player.equals(marked.getMarkedEntityRef("LockedTarget"))) {
          blocked[0] = true;
          continue;
        }
        // Cover the native handoff before its next AI evaluation, using only a live queued
        // danger message for this exact offender. Peek does not consume or extend its lifetime.
        var beacon = store.getComponent(npc, BeaconSupport.getComponentType());
        if (beacon == null || beacon.getMessageSlots() == null) continue;
        for (int slot = 0; slot < beacon.getMessageSlots().length; slot++) {
          if (DANGER.contains(beacon.getMessageTextForIndex(slot)) && beacon.isMessageEnabled(slot)
              && beacon.isMessageQueued(slot) && player.equals(beacon.peekMessage(slot))) {
            blocked[0] = true;
            break;
          }
        }
      }
    });
    return blocked[0];
  }
}
