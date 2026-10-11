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
  ContainerPacketBridgeTest.run();
  var store=world.getEntityStore().getStore();var holder=EntityStore.REGISTRY.newHolder();
  var transform=store.getComponent(civilian,TransformComponent.getComponentType()).clone();
  transform.setPosition(new Vector3d(.5,64,15.5));holder.addComponent(TransformComponent.getComponentType(),transform);
  holder.addComponent(ModelComponent.getComponentType(),new ModelComponent(store.getComponent(civilian,ModelComponent.getComponentType()).getModel()));
  var bbox=store.getComponent(civilian,BoundingBox.getComponentType());
  if(bbox!=null)holder.addComponent(BoundingBox.getComponentType(),(BoundingBox)bbox.clone());
  var stance=new com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent();
  stance.setMovementStates(new com.hypixel.hytale.protocol.MovementStates());
  holder.addComponent(com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent.getComponentType(),stance);
  var actor=store.addEntity(holder,AddReason.SPAWN);
  try{
   transform=store.getComponent(actor,TransformComponent.getComponentType());
   var movement=store.getComponent(actor,com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent.getComponentType());
   require(ContainerVisualWitness.visualRange(true,"Kweebec_Sapling")==6.0&&ContainerVisualWitness.visualRange(false,"Kweebec_Sapling")==20.0,"container-only experimental range");
   require(15.0*HyThiefStealthAcquisition.multiplier("sight","Kweebec_Sapling")==4.0,"normal crouched sight changed");
   for(int distance:new int[]{5,8,18,22}){
    transform.setPosition(new Vector3d(.5,64,10.5+distance));
    var rawSight=new PositionCache();rawSight.setOpaqueBlockSet(-1);
    require(rawSight.hasLineOfSight(civilian,actor,store),"test station LOS obstruction at "+distance);
    for(boolean crouched:new boolean[]{false,true}){
     movement.getMovementStates().crouching=crouched;var visual=ContainerVisualWitness.capture(actor,store);
     boolean expected=distance<=(crouched?6:20);
     require(visual.distance()==distance&&visual.sector()&&visual.crouched()==crouched&&visual.seen()==expected,"stance range failure "+visual);
     var messagesForCase=new ArrayList<com.hypixel.hytale.server.core.Message>();ContainerCaughtNotification.notifyWitnessed(List.of(visual),messagesForCase::add);
     require(messagesForCase.size()==(expected?1:0),"caught message mismatch "+distance+" crouched="+crouched);
     System.out.println("SIGHT_TUNING_CASE Distance="+distance+" Crouched="+crouched+" EffectiveRange="+ContainerVisualWitness.visualRange(crouched,visual.role())+" Sector=true RawLOS=true Witnessed="+visual.seen()+" Notifications="+messagesForCase.size());
    }
   }
   movement.getMovementStates().crouching=false;
   report.addProperty("containerStandingRange",20);report.addProperty("containerCrouchedRange",6);report.addProperty("normalSightMultiplierUnchanged",true);report.addProperty("allFourStancePairsPassed",true);
   System.out.println("SIGHT_TUNING_PASS ContainerStanding=20 ContainerCrouched=6 Pads=5,8,18,22 RawLOS=CLEAR Sector=true NormalStanding=15 NormalCrouched=4");
   transform.setPosition(new Vector3d(.5,64,15.5));
   PersistentOwnershipIntegrationTest.run(world,actor);
   var front=ContainerVisualWitness.capture(actor,store);
   require(front.seen()&&front.los()&&front.sector(),"front must be witnessed "+front);
   transform=store.getComponent(actor,TransformComponent.getComponentType());
   transform.setPosition(new Vector3d(.5,64,5.5));var rear=ContainerVisualWitness.capture(actor,store);
   require(!rear.seen()&&!rear.sector()&&rear.reason().equals("sector"),"rear must be unseen");
   transform.setPosition(new Vector3d(-2.5,64,15.5));var visibleControl=ContainerVisualWitness.capture(actor,store);
   require(visibleControl.seen(),"control position must be visually visible");
   var messages=new ArrayList<com.hypixel.hytale.server.core.Message>();
   require(ContainerCaughtNotification.notifyWitnessed(List.of(),messages::add).isEmpty(),"unowned/control or no paired withdrawal notified");
   require(ContainerCaughtNotification.notifyWitnessed(List.of(rear),messages::add).isEmpty(),"unseen rear notified");
   require(messages.isEmpty(),"control/rear notification count");
   require(ContainerCaughtNotification.notifyWitnessed(List.of(rear,front,front),messages::add).orElseThrow()==front,"wrong witness selected");
   require(messages.size()==1&&"You have been caught stealing!".equals(messages.getFirst().getRawText()),"front must notify exactly once with exact text");
   System.out.println("CAUGHT_NOTIFICATION_PASS Control=0 Rear=0 Front=1 ExactText=true MultipleEvidenceDeduplicated=true");
   transform.setPosition(new Vector3d(.5,64,32.5));var outside=ContainerVisualWitness.capture(actor,store);
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
   transform.setPosition(new Vector3d(.5,64,28.5));
   require(!guardSight.hasLineOfSight(guard,actor,store),"guard sees 18-block pad directly");
   var plugin=PluginManager.get().getPlugin(PickpocketPlugin.class);
   require(plugin!=null,"actual HyThief plugin unavailable");
   var delivery=HyThiefGuardAlert.deliver(plugin,actor,civilian,store);
   require(delivery.sent()&&delivery.guard().equals(guard),"one native guard delivery failed "+delivery);
   var beacon=store.getComponent(guard,BeaconSupport.getComponentType());int slot=-1;
   for(int i=0;i<beacon.getMessageSlots().length;i++)if("Kweebec_All_Danger".equals(beacon.getMessageTextForIndex(i)))slot=i;
   require(slot>=0&&beacon.isMessageQueued(slot)&&actor.equals(beacon.peekMessage(slot)),"native danger offender mismatch");
   beacon.pollMessage(slot);
   HyThiefGuardAlert.deliver(plugin,actor,civilian,store,System.out::println);
   var start=new Vector3d(store.getComponent(guard,TransformComponent.getComponentType()).getPosition());
   world.scheduleAfter(()->world.execute(()->{
    try{
     var state=store.getComponent(guard,com.hypixel.hytale.server.npc.role.support.StateSupport.getComponentType());
     var targets=store.getComponent(guard,com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport.getComponentType());
     boolean marked=java.util.Arrays.asList(targets.getEntityTargets()).contains(actor);
     double moved=start.distance(store.getComponent(guard,TransformComponent.getComponentType()).getPosition());
     require(state.getStateName().startsWith("Attack") && marked && moved>.1,"native pursuit lost offender: "+state.getStateName()+" marked="+marked+" moved="+moved);
     System.out.println("NATIVE_GUARD_PURSUIT_PASS WitnessDistance=18 State="+state.getStateName()+" ExactOffenderMarked="+marked+" DistanceMoved="+moved);
    }catch(Throwable error){ContainerWorldBuilder.fail(error);}
   }),1250,java.util.concurrent.TimeUnit.MILLISECONDS);
   report.addProperty("nativeVisualFrontWitnessed",true);report.addProperty("nativeVisualRearUnseen",true);
   report.addProperty("nativeControlApproachVisibleButUnowned",true);report.addProperty("nativeRangeAndOpaqueWallRejected",true);
   report.addProperty("exactRemovalSnapshotSurvivesLaterMovement",true);report.addProperty("guardHasNoNativeDirectLOS",true);
   report.addProperty("actualPluginAdapterPostsOneDangerWithExactOffender",true);
   report.addProperty("realClientCombatAcceptancePending",true);
   System.out.println("WITNESS_NATIVE_PASS Control=VISIBLE_UNOWNED Rear=UNSEEN Front=WITNESSED Wall=OCCLUDED Range=REJECTED RemovalSnapshot=EXACT Danger=QUEUED_ONE Offender=EXACT GuardLOS=false");
  }finally{} // Actor stays only in the disposable verifier until server shutdown.
 }
 static void require(boolean b,String message){if(!b)throw new IllegalStateException(message);}
}
