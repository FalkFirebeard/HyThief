package com.local.pickpocket;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Explicit, asset-driven ordinary-role coverage; group membership uses the native role index. */
final class ContainerWitnessProfiles {
  private ContainerWitnessProfiles() {}
  static String entry(Ref<EntityStore> npc, Store<EntityStore> store) {
    if (ContainerOwnershipPolicy.member(npc, "HyThief_Container_Kweebec", store)
        || ContainerOwnershipPolicy.member(npc, "HyThief_Container_Feran", store)) return "Guard";
    if (ContainerOwnershipPolicy.member(npc, "HyThief_Container_Rally", store)) return "TrorkEnemySighted";
    if (ContainerOwnershipPolicy.member(npc, "HyThief_Container_Combat", store)) return "Combat.Default";
    return "";
  }
  static boolean eligible(Ref<EntityStore> npc, Store<EntityStore> store) {
    return !entry(npc, store).isEmpty();
  }
}
