package com.local.pickpocket;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Native pre-use cancellation: owned storage only, before native OpenContainer executes. */
final class OwnedContainerAccess extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {
  OwnedContainerAccess() { super(UseBlockEvent.Pre.class); }
  @Override public Query<EntityStore> getQuery() { return PlayerRef.getComponentType(); }
  @Override public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
      CommandBuffer<EntityStore> buffer, UseBlockEvent.Pre event) {
    if (event.isCancelled()) return;
    var position = event.getTargetBlock();
    if (position == null) return;
    var chunks = store.getExternalData().getWorld().getChunkStore();
    var section = chunks.getChunkSectionReferenceAtBlock(position.x, position.y, position.z);
    if (section == null || !section.isValid()) return;
    var block = BlockModule.getBlockEntity(chunks.getStore(), section, position.x, position.y, position.z);
    if (block == null || !block.isValid()
        || chunks.getStore().getComponent(block, ItemContainerBlock.getComponentType()) == null) return;
    if (PersistentContainerOwnership.eligible(chunks.getStore().getComponent(block, OwnershipProofData.getComponentType()))
        && NativePursuit.blocks(chunk.getReferenceTo(index), store)) {
      // Cancellation is the per-attempt guard: redispatch of this event returns above.
      event.setCancelled(true);
      buffer.getComponent(chunk.getReferenceTo(index), PlayerRef.getComponentType()).sendMessage(
          com.hypixel.hytale.server.core.Message.raw("You cannot access this container while being pursued."));
    }
  }
}
