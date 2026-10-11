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
