package com.local.pickpocket;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import java.util.*;

/** Small policy seam: eligible witnesses supplied by native NPC-group data; native NPC groups define faction membership. */
public final class ContainerOwnershipPolicy {
  private ContainerOwnershipPolicy() {}
  public static boolean member(Ref<EntityStore> ref,String group,Store<EntityStore> store) {
    var npc=store.getComponent(ref,NPCEntity.getComponentType());
    int index=NPCGroup.getAssetMap().getIndex(group);
    return npc!=null && index>=0 && WorldSupport.isGroupMember(npc.getRoleIndex(),ref,index,store);
  }
  public static String faction(Ref<EntityStore> ref,Store<EntityStore> store) {
    for(String id:List.of("Kweebec","Feran","Trork","Outlander","Skeleton","Zombie"))if(member(ref,id,store))return id;
    return "UNRESOLVED";
  }
  public static String rejection(OwnershipProofData data,Ref<EntityStore> ref,Store<EntityStore> store) {
    if(!PersistentContainerOwnership.eligible(data))return "UNOWNED_OR_INVALID_SCOPE";
    if("NEARBY".equals(data.scope()))return null; // No faction filter.
    return member(ref,data.faction(),store)?null:"FACTION_MISMATCH";
  }
}
