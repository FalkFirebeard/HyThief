package com.local.pickpocket;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import static com.local.pickpocket.NpcOverrides.*;

/** Per-capability precedence; Enabled cannot manufacture unsupported native capabilities. */
public final class NpcParticipation {
  private NpcParticipation() {}
  // NPC editor scopes only; does not expand existing container ownership faction policy.
  public static String faction(Ref<EntityStore> ref,Store<EntityStore> store) {
    for(String id:java.util.List.of("Kweebec","Feran","Goblin","Trork","Outlander","Skeleton","Zombie"))
      if(ContainerOwnershipPolicy.member(ref,id,store))return id;
    return "UNRESOLVED";
  }
  public static boolean allowed(Capability capability,Ref<EntityStore> ref,Store<EntityStore> store,boolean supported) {
    if(!supported)return false;
    if(ref==null || !ref.isValid() || store==null || ref.getStore()!=store)return supported;
    var npc=store.getComponent(ref,NPCEntity.getComponentType());if(npc==null)return false;
    var local=store.getComponent(ref,NpcOverrides.getComponentType());
    var mode=local==null?Mode.DEFAULT:local.get(capability);
    if(mode==Mode.DEFAULT) {
      var broad=store.getResource(NpcWorldOverrides.getResourceType());mode=broad.role(npc.getRoleName()).get(capability);
      if(mode==Mode.DEFAULT)mode=broad.faction(faction(ref,store)).get(capability);
    }
    return mode!=Mode.DISABLED;
  }
}
