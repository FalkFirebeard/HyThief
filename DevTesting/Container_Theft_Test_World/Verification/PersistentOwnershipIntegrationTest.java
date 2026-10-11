package com.local.pickpocket;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import java.util.*;

/** Native proof; real packet/client-window acceptance remains the Firebeard checkpoint. */
final class PersistentOwnershipIntegrationTest {
 static void run(World world,Ref<EntityStore> actor) {
  var store=world.getEntityStore().getStore();
  store.getComponent(actor,TransformComponent.getComponentType()).setPosition(new org.joml.Vector3d(.5,64,15.5));
  var inventory=ContainerWorldBuilder.container(world,ContainerWorldBuilder.CHESTS[0]);
  var window=new ContainerBlockWindow(0,64,16,0,world.getBlockType(0,64,16),inventory);
  var source=PersistentContainerOwnership.resolve(world,window,inventory);
  require(source!=null && source.ownedNearby(),"Native source ownership missing");
  require(PersistentContainerOwnership.resolve(world,window,new SimpleItemContainer((short)4))==null,"Wrong attributed inventory accepted");
  var chunks=world.getChunkStore().getStore();var original=source.read().clone();
  // A contradictory legacy registry must not influence attached-component decisions.
  var legacy=new OwnedContainerRegistry();legacy.replaceAutomatic(world,List.of(
    new OwnedContainerRegistry.Entry(source.key(),inventory),
    new OwnedContainerRegistry.Entry(new OwnedContainerRegistry.Key(world.getWorldConfig().getUuid(),0,64,4),ContainerWorldBuilder.container(world,ContainerWorldBuilder.CHESTS[1]))));
  try {
   require(transfer(source,actor,true)==1,"Owned standing withdrawal did not notify once");
   chunks.replaceComponent(source.entity(),OwnershipProofData.getComponentType(),new OwnershipProofData(false,"NEARBY"));
   require(transfer(source,actor,true)==0,"Owned=false notified despite component authority");
   chunks.removeComponent(source.entity(),OwnershipProofData.getComponentType());
   require(transfer(source,actor,true)==0,"Missing component fell back to registry");
   chunks.addComponent(source.entity(),OwnershipProofData.getComponentType(),original);
   var controlInventory=ContainerWorldBuilder.container(world,ContainerWorldBuilder.CHESTS[2]);
   var controlWindow=new ContainerBlockWindow(-3,64,16,0,world.getBlockType(-3,64,16),controlInventory);
   var control=PersistentContainerOwnership.resolve(world,controlWindow,controlInventory);
   require(control!=null && control.read()==null && transfer(control,actor,true)==0,"Unowned control notified");
   require(transfer(source,actor,false)==0,"Insertion/rearrangement/failed transaction notified");
   var stance=store.getComponent(actor,com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent.getComponentType());
   stance.getMovementStates().crouching=true;
   require(transfer(source,actor,true)==1,"Five-block crouched component theft did not notify");
   stance.getMovementStates().crouching=false;
   System.out.println("PERSISTENT_PIPELINE_PASS Source=real-window-block-entity OwnedTrue=1 OwnedFalse=0 Missing=0 ConflictingLegacyRegistryIgnored=true Control=0 InsertionInternalFailed=0 FiveBlockCrouched=1");
  } finally {
   chunks.putComponent(source.entity(),OwnershipProofData.getComponentType(),original);
   var section=world.getChunkStore().getChunkSectionReferenceAtBlock(0,64,16);
   var components=chunks.getComponent(section,com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection.getComponentType());
   components.markBlockNeedsSaving(com.hypixel.hytale.math.util.ChunkUtil.indexBlock(0,64,16));components.markNeedsSaving();
   legacy.close();
  }
 }
 static int transfer(PersistentContainerOwnership.Source source,Ref<EntityStore> actor,boolean withdrawal) {
  var inventory=source.container();var recipient=new SimpleItemContainer((short)8);
  recipient.setItemStackForSlot((short)1,new com.hypixel.hytale.server.core.inventory.ItemStack("Ingredient_Bar_Iron",3));
  var snapshots=new IdentityHashMap<ContainerRemovalProbe.Removal,ContainerVisualWitness.Evidence>();
  var confirmed=new ArrayList<ContainerVisualWitness.Evidence>();
  var probe=new ContainerRemovalProbe(inventory,removal->{
    var data=source.read();if(PersistentContainerOwnership.eligible(data)) {
      var visual=snapshots.get(removal);if(visual!=null)confirmed.add(visual);
    }
  },removal->{if(source.ownedNearby())snapshots.put(removal,ContainerVisualWitness.capture(actor,actor.getStore()));});
  var a=inventory.registerChangeEvent(e->probe.source(e.transaction()));
  var b=recipient.registerChangeEvent(e->probe.receipt(e.transaction()));
  try {
   if(withdrawal) { inventory.moveItemStackFromSlotToSlot((short)0,4,recipient,(short)0); require(recipient.getItemStack((short)0).getQuantity()==4,"Native withdrawal failed"); }
   else {
    recipient.moveItemStackFromSlotToSlot((short)1,3,inventory,(short)4);
    inventory.moveItemStackFromSlotToSlot((short)0,4,inventory,(short)5);
    inventory.moveItemStackFromSlotToSlot((short)35,4,recipient,(short)0);
   }
   int paired=probe.finish(); require(paired==(withdrawal?1:0),"Attribution changed");
   var messages=new ArrayList<com.hypixel.hytale.server.core.Message>();
   ContainerCaughtNotification.notifyWitnessed(confirmed,messages::add);
   if(!messages.isEmpty())require("You have been caught stealing!".equals(messages.getFirst().getRawText()),"Caught text changed");
   return messages.size();
  } finally {a.unregister();b.unregister();ContainerWorldBuilder.fill(inventory);}
 }
 static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
}
