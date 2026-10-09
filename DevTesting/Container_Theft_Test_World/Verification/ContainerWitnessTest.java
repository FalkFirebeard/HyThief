package com.local.pickpocket;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.support.PositionCache;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import org.joml.Vector3d;
import com.google.gson.JsonObject;
import java.util.*;

/** Native API integration proof with an inert modeled test target, not a client session. */
final class ContainerWitnessTest {
 static void run(World world,Ref<EntityStore> civilian,Ref<EntityStore> guard,JsonObject report){
  var store=world.getEntityStore().getStore();var holder=EntityStore.REGISTRY.newHolder();
  var transform=store.getComponent(civilian,TransformComponent.getComponentType()).clone();
  transform.setPosition(new Vector3d(.5,64,15.5));holder.addComponent(TransformComponent.getComponentType(),transform);
  holder.addComponent(ModelComponent.getComponentType(),new ModelComponent(store.getComponent(civilian,ModelComponent.getComponentType()).getModel()));
  var bbox=store.getComponent(civilian,BoundingBox.getComponentType());
  if(bbox!=null)holder.addComponent(BoundingBox.getComponentType(),(BoundingBox)bbox.clone());
  var actor=store.addEntity(holder,AddReason.SPAWN);
  try{
   var front=ContainerVisualWitness.capture(actor,store);
   require(front.seen()&&front.los()&&front.sector(),"front must be witnessed "+front);
   transform=store.getComponent(actor,TransformComponent.getComponentType());
   transform.setPosition(new Vector3d(.5,64,5.5));var rear=ContainerVisualWitness.capture(actor,store);
   require(!rear.seen()&&!rear.sector()&&rear.reason().equals("sector"),"rear must be unseen");
   transform.setPosition(new Vector3d(-2.5,64,15.5));var visibleControl=ContainerVisualWitness.capture(actor,store);
   require(visibleControl.seen(),"control position must be visually visible");
   transform.setPosition(new Vector3d(.5,64,26.5));var outside=ContainerVisualWitness.capture(actor,store);
   require(!outside.seen()&&outside.reason().equals("range"),"outside visual range");
   transform.setPosition(new Vector3d(9,64,16));var blocked=ContainerVisualWitness.capture(actor,store);
   require(blocked.sector()&&!blocked.los()&&!blocked.seen(),"wall must block visual witness");
   transform.setPosition(new Vector3d(.5,64,15.5));
   var guardSight=new PositionCache();guardSight.setOpaqueBlockSet(-1);
   require(!guardSight.hasLineOfSight(guard,actor,store),"guard has direct front player LOS");
   // Confirm request-time source snapshot survives movement before paired finish.
   var chest=ContainerWorldBuilder.container(world,ContainerWorldBuilder.CHESTS[0]);
   var recipient=new com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer((short)4);
   var snapshots=new IdentityHashMap<ContainerRemovalProbe.Removal,ContainerVisualWitness.Evidence>();
   var captured=new ArrayList<ContainerVisualWitness.Evidence>();
   var probe=new ContainerRemovalProbe(chest,r->captured.add(snapshots.get(r)),r->snapshots.put(r,ContainerVisualWitness.capture(actor,store)));
   var a=chest.registerChangeEvent(e->probe.source(e.transaction()));var b=recipient.registerChangeEvent(e->probe.receipt(e.transaction()));
   try{chest.moveItemStackFromSlotToSlot((short)0,4,recipient,(short)0);transform.setPosition(new Vector3d(.5,64,5.5));probe.finish();}
   finally{a.unregister();b.unregister();ContainerWorldBuilder.fill(chest);}
   require(captured.size()==1&&captured.getFirst().seen()&&captured.getFirst().playerPosition().z==15.5,"removal-time snapshot changed");
   transform.setPosition(new Vector3d(.5,64,15.5));
   var plugin=PluginManager.get().getPlugin(PickpocketPlugin.class);
   require(plugin!=null,"actual HyThief plugin unavailable");
   var delivery=HyThiefGuardAlert.deliver(plugin,actor,civilian,store);
   require(delivery.sent()&&delivery.guard().equals(guard),"one native guard delivery failed "+delivery);
   var beacon=store.getComponent(guard,BeaconSupport.getComponentType());int slot=-1;
   for(int i=0;i<beacon.getMessageSlots().length;i++)if("Kweebec_All_Danger".equals(beacon.getMessageTextForIndex(i)))slot=i;
   require(slot>=0&&beacon.isMessageQueued(slot)&&actor.equals(beacon.peekMessage(slot)),"native danger offender mismatch");
   beacon.pollMessage(slot); // Disposable proof ends before fake target is allowed to drive combat.
   report.addProperty("nativeVisualFrontWitnessed",true);report.addProperty("nativeVisualRearUnseen",true);
   report.addProperty("nativeControlApproachVisibleButUnowned",true);report.addProperty("nativeRangeAndOpaqueWallRejected",true);
   report.addProperty("exactRemovalSnapshotSurvivesLaterMovement",true);report.addProperty("guardHasNoNativeDirectLOS",true);
   report.addProperty("actualPluginAdapterPostsOneDangerWithExactOffender",true);
   report.addProperty("realClientCombatAcceptancePending",true);
   System.out.println("WITNESS_NATIVE_PASS Control=VISIBLE_UNOWNED Rear=UNSEEN Front=WITNESSED Wall=OCCLUDED Range=REJECTED RemovalSnapshot=EXACT Danger=QUEUED_ONE Offender=EXACT GuardLOS=false");
  }finally{store.removeEntity(actor,RemoveReason.REMOVE);}
 }
 static void require(boolean b,String message){if(!b)throw new IllegalStateException(message);}
}
