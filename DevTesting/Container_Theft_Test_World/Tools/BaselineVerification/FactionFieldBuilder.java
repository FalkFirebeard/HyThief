package com.local.pickpocket;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.plugin.*;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.modules.block.*;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.inventory.*;
import com.hypixel.hytale.server.core.inventory.container.*;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.role.support.*;
import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3d;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;

/** Disposable author/validator for the ownership field; never installed in the live Mods folder. */
public final class FactionFieldBuilder extends JavaPlugin {
 public FactionFieldBuilder(JavaPluginInit init){super(init);}
 static final int[] ZS={-6,-2,2,6};
 static final String[] COLORS={"Red","Blue","Green","Yellow"};
 static final String[] LABELS={"RED - CONTROL / UNOWNED","BLUE - FACTION / KWEEBEC","GREEN - NEARBY","YELLOW - FACTION / FERAN"};
 static final String K="Kweebec_Sapling_HyThiefWitness", F="Feran_Civilian_HyThiefWitness";
 static final Set<String> WITNESSES=Set.of(K,F);
 static OwnershipProofData data(int i){return new OwnershipProofData(i!=0,i==2?"NEARBY":i==0?"":"FACTION",i==1?"Kweebec":i==3?"Feran":"");}
 static double padZ(int i){return ZS[i]-.5;}
 static boolean containsZ(int z){for(int v:ZS)if(v==z)return true;return false;}
 static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
 protected void start(){Universe.get().getUniverseReady().thenRun(()->{
  var w=Universe.get().getDefaultWorld();var loads=new ArrayList<CompletableFuture<?>>();
  for(int x=-1;x<=0;x++)for(int z=-1;z<=1;z++)for(int y=0;y<=2;y++)loads.add(w.getChunkStore().getChunkSectionReferenceAsync(x,y,z,com.hypixel.hytale.server.core.universe.world.storage.GetChunkFlags.SET_TICKING));
  CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).thenRun(()->w.execute(()->{
   try{if(Boolean.getBoolean("factionfield.author"))author(w);
    w.scheduleAfter(()->w.execute(()->{try{validate(w);}catch(Throwable e){fail(e);}}),5,TimeUnit.SECONDS);
   }catch(Throwable e){fail(e);}
  }));
 });}
 static ItemContainer chest(World w,int i){return chestAt(w,ZS[i]);}
 static ItemContainer chestAt(World w,int z){int x=0;var cs=w.getChunkStore();var section=cs.getChunkSectionReferenceAtBlock(x,64,z);
  var block=cs.getStore().getComponent(section,BlockComponentSection.getComponentType());
  BlockEntity.ensureBlockEntity(cs.getStore(),section,block,x,64,z,NativeFieldBlocks.type(w,x,64,z),0);
  var inventory=BlockModule.getComponent(ItemContainerBlock.getComponentType(),w,x,64,z);
  require(inventory!=null,"native chest component missing");
  if(inventory.getItemContainer()==null){require(Boolean.getBoolean("factionfield.author"),"inventory lost on restart");inventory.setItemContainer(new SimpleItemContainer(inventory.getCapacity()));}
  return inventory.getItemContainer();
 }
 static void mark(World w,int i){var cs=w.getChunkStore();var section=cs.getChunkSectionReferenceAtBlock(0,64,ZS[i]);
  var block=cs.getStore().getComponent(section,BlockComponentSection.getComponentType());block.markBlockNeedsSaving(com.hypixel.hytale.math.util.ChunkUtil.indexBlock(0,64,ZS[i]));block.markNeedsSaving();}
 static Ref<EntityStore> spawn(World w,String role,double x,double z,String label){var store=w.getEntityStore().getStore();
  var pair=NPCPlugin.get().spawnNPC(store,role,null,new Vector3d(x,64,z),new Rotation3f(0,role.equals(K)?-(float)Math.PI/2:role.equals(F)?(float)Math.PI/2:0,0));require(pair!=null,"spawn failed "+role);
  var ref=pair.first();store.putComponent(ref,Nameplate.getComponentType(),new Nameplate(label));return ref;
 }
 static void author(World w){
  for(int x=-26;x<=26;x++)for(int z=-16;z<=26;z++){
   NativeFieldBlocks.set(w,x,63,z,"Rock_Stone_Cobble");for(int y=64;y<=70;y++)NativeFieldBlocks.set(w,x,y,z,"Empty");
  }
  var k=spawn(w,K,-7.5,.5,"KWEEBEC WITNESS - stationary / faces stations");spawn(w,F,7.5,.5,"FERAN WITNESS - stationary / faces stations");
  spawn(w,"Kweebec_Razorleaf",-21.5,.5,"KWEEBEC RESPONDER - RAZORLEAF");spawn(w,"Feran_Sharptooth",21.5,.5,"FERAN RESPONDER - SHARPTOOTH");
  var store=w.getEntityStore().getStore();
  for(int i=0;i<ZS.length;i++){
   NativeFieldBlocks.set(w,0,64,ZS[i],"Furniture_Kweebec_Chest_Large");var c=chest(w,i);c.setItemStackForSlot((short)0,new ItemStack("Ingredient_Bar_Copper",16));c.setItemStackForSlot((short)1,new ItemStack("Ingredient_Bar_Iron",8));
   var ref=BlockModule.getBlockEntity(w,0,64,ZS[i]);w.getChunkStore().getStore().addComponent(ref,OwnershipProofData.getComponentType(),data(i));mark(w,i);
   for(int x=-1;x<=0;x++)NativeFieldBlocks.set(w,x,63,ZS[i]-1,"Cloth_Block_Wool_"+COLORS[i]);label(store,LABELS[i]+" - CENTER SEAM",0,66.5,ZS[i]+.5);
  }
  label(store,"RED SAFE / BLUE KWEEBEC / GREEN LOCAL / YELLOW FERAN",0,67,10.5);
  label(store,"STAND ON COLORED CENTER SEAM - RESET AFTER CAUGHT",0,66.2,10.5);
  System.out.println("FACTION_FIELD_AUTHOR_PASS FourNativeChests=true SerializedOwnership=true");
 }
 static void label(Store<EntityStore> store,String text,double x,double y,double z){
  var holder=EntityStore.REGISTRY.newHolder();holder.addComponent(TransformComponent.getComponentType(),new TransformComponent(new Vector3d(x,y,z),new Rotation3f()));
  holder.addComponent(Nameplate.getComponentType(),new Nameplate(text));
  holder.addComponent(ModelComponent.getComponentType(),new ModelComponent(com.hypixel.hytale.server.core.asset.type.model.config.Model.createStaticScaledModel(com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset.getAssetMap().getAsset("Arrow_Iron"),.01f)));
  holder.addComponent(Frozen.getComponentType(),Frozen.get());holder.addComponent(com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType(),com.hypixel.hytale.server.core.entity.UUIDComponent.randomUUID());store.addEntity(holder,AddReason.SPAWN);
 }
 static void validate(World w)throws Exception{
  if(Boolean.getBoolean("factionfield.expectPersistence"))verifyPersistence(w,"fullServerRestart");
  for(int x=-26;x<=26;x++)for(int z=-16;z<=26;z++)for(int y=64;y<=70;y++){
   boolean isChest=y==64&&containsZ(z)&&(x==-1||x==0);
   require(isChest||"Empty".equals(NativeFieldBlocks.type(w,x,y,z).getId()),"Unexpected above-ground obstacle "+x+","+y+","+z);
  }
  int chestCount=0;for(int x=-32;x<32;x++)for(int z=-32;z<64;z++)for(int y=0;y<=80;y++)if(NativeFieldBlocks.type(w,x,y,z).getId().contains("Chest")&&NativeFieldBlocks.filler(w,x,y,z)==0){
   require(y==64&&containsZ(z)&&x==0,"unexpected chest "+x+","+y+","+z);chestCount++;
  }
  require(chestCount==4,"native chest count="+chestCount);
  var players=Universe.get().getPlayerStorage().getPlayers();require(players.size()==1,"expected one starting player");var saved=Universe.get().getPlayerStorage().load(players.iterator().next()).join();
  require(saved!=null,"Firebeard starting player missing");var spawn=saved.getComponent(TransformComponent.getComponentType());require(spawn!=null&&spawn.getPosition().distance(new Vector3d(0,64.1,10.5))<.001&&Math.abs(spawn.getRotation().yaw())<.001,"spawn not facing color row");var hotbar=saved.getComponent(InventoryComponent.Hotbar.getComponentType());
  require(hotbar!=null,"starting hotbar missing");var blocks=hotbar.getInventory().getItemStack((short)0);
  require(blocks!=null&&blocks.getItemId().equals("Rock_Stone_Cobble")&&blocks.getQuantity()==64,"starting building blocks missing");
  System.out.println("FOUR_CHEST_LAYOUT_PASS ChestCount=4 StartingBlocks=Rock_Stone_Cobble Quantity=64");
  System.out.println("FACTION_OPEN_FIELD_PASS NoPermanentWalls=true ClearResponderPaths=true");
  for(int i=0;i<ZS.length;i++){
   for(int x=-1;x<=0;x++)require(NativeFieldBlocks.type(w,x,63,ZS[i]-1).getId().equals("Cloth_Block_Wool_"+COLORS[i]),"color pad missing "+COLORS[i]);
   final int station=i;var count=new int[]{0};w.getEntityStore().getStore().forEachChunk(Nameplate.getComponentType(),(chunk,b)->{for(int n=0;n<chunk.size();n++){var ref=chunk.getReferenceTo(n);var store=w.getEntityStore().getStore();if(store.getComponent(ref,Nameplate.getComponentType()).getText().equals(LABELS[station]+" - CENTER SEAM")){var t=store.getComponent(ref,TransformComponent.getComponentType());require(t.getPosition().x==0&&t.getPosition().z==ZS[station]+.5,"color label position");count[0]++;}}});require(count[0]==1,"color label lost "+COLORS[i]);
  }
  var store=w.getEntityStore().getStore();var roles=new HashMap<String,Ref<EntityStore>>();var npcCount=new int[]{0};
  store.forEachChunk(NPCEntity.getComponentType(),(chunk,b)->{for(int i=0;i<chunk.size();i++){npcCount[0]++;var ref=chunk.getReferenceTo(i);roles.put(store.getComponent(ref,NPCEntity.getComponentType()).getRoleName(),ref);}});
  require(npcCount[0]==4&&roles.size()==4,"expected exactly four NPC roles");
  var k=roles.get(K);var f=roles.get(F);require(k!=null&&f!=null,"witness roles missing");
  for(var name:List.of("Kweebec_Razorleaf","Feran_Sharptooth")){var ref=roles.get(name);require(ref!=null&&store.getComponent(ref,Frozen.getComponentType())==null,"responder missing or frozen");System.out.println("FIELD_RESPONDER Role="+name+" Position="+(ref==null?"missing":store.getComponent(ref,TransformComponent.getComponentType()).getPosition())+" State="+(ref==null?"missing":store.getComponent(ref,StateSupport.getComponentType()).getStateName()));}
  for(var ref:List.of(k,f)) {require(store.getComponent(ref,Frozen.getComponentType())==null,"witness Frozen disables role processing");var t=store.getComponent(ref,TransformComponent.getComponentType());var h=store.getComponent(ref,HeadRotation.getComponentType());require(Math.abs(Math.abs(h.getRotation().yaw())-Math.PI/2)<.001,"witness head rotated");require(Math.abs(t.getPosition().z-.5)<.01,"witness wandered");}
  require(ContainerOwnershipPolicy.member(k,"Kweebec",store)&&!ContainerOwnershipPolicy.member(k,"Feran",store),"native Kweebec group");
  require(ContainerOwnershipPolicy.member(f,"Feran",store)&&!ContainerOwnershipPolicy.member(f,"Kweebec",store),"native Feran group");
  var holder=EntityStore.REGISTRY.newHolder();holder.addComponent(TransformComponent.getComponentType(),new TransformComponent(new Vector3d(0,64,padZ(2)),new Rotation3f()));
  holder.addComponent(com.hypixel.hytale.server.core.modules.physics.component.Velocity.getComponentType(),new com.hypixel.hytale.server.core.modules.physics.component.Velocity());
  holder.addComponent(ModelComponent.getComponentType(),new ModelComponent(store.getComponent(k,ModelComponent.getComponentType()).getModel()));
  var box=store.getComponent(k,BoundingBox.getComponentType());if(box!=null)holder.addComponent(BoundingBox.getComponentType(),(BoundingBox)box.clone());
  var movement=new com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent();movement.setMovementStates(new com.hypixel.hytale.protocol.MovementStates());holder.addComponent(com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent.getComponentType(),movement);
  var actor=store.addEntity(holder,AddReason.SPAWN);
  for(int i=0;i<ZS.length;i++){
   var c=chest(w,i);var source=PersistentContainerOwnership.resolve(w,new com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow(0,64,ZS[i],0,NativeFieldBlocks.type(w,0,64,ZS[i]),c),c);
   require(source!=null,"source identity");var d=source.read();var expected=data(i);require(d!=null&&d.owned()==expected.owned()&&d.scope().equals(expected.scope())&&d.faction().equals(expected.faction()),"ownership/faction lost on save/restart station "+(i+1));
   var base=NativeFieldBlocks.type(w,0,64,ZS[i]);require("Open_Container".equals(base.getInteractions().get(com.hypixel.hytale.protocol.InteractionType.Use)),"native chest Use changed");
   var section=w.getChunkStore().getChunkSectionReferenceAtBlock(0,64,ZS[i]);
   for(String state:new String[]{"OpenWindow","CloseWindow"}){
    com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations.setBlockInteractionState(w.getChunkStore(),section,0,64,ZS[i],base,state,false);
    require(chest(w,i)==c&&source.read().faction().equals(expected.faction()),"native open/close lost inventory/ownership");
   }
   store.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(0,64,padZ(i)));
   var rawK=ContainerVisualWitness.evaluate(k,actor,store.getComponent(actor,TransformComponent.getComponentType()).getPosition(),false,store);
   var rawF=ContainerVisualWitness.evaluate(f,actor,store.getComponent(actor,TransformComponent.getComponentType()).getPosition(),false,store);
   require(rawK.sector()&&rawF.sector(),"outside forward FOV station "+(i+1));
   require(rawK.los()&&rawF.los(),"wrong occlusion station "+(i+1)+" K="+rawK+" F="+rawF);
   var visual=ContainerVisualWitness.capture(actor,store,d,WITNESSES,System.out::println);var messages=new ArrayList<com.hypixel.hytale.server.core.Message>();
   var selected=ContainerCaughtNotification.notifyWitnessed(visual,messages::add);
   require(messages.size()==(i!=0?1:0),"notification station "+(i+1));
   {if(i==2)require(visual.size()==2&&visual.stream().allMatch(ContainerVisualWitness.Evidence::seen),"NEARBY filtered faction");
    var kp=store.getComponent(k,TransformComponent.getComponentType()).getPosition();var fp=store.getComponent(f,TransformComponent.getComponentType()).getPosition();var point=new Vector3d(0,64,padZ(i));
    double kd=kp.distance(point),fd=fp.distance(point);require(Math.abs(kd-fd)<1e-8&&kd<20&&fd<20,"unequal/out-of-range distances");
    require(Math.abs(kp.y-fp.y)<1e-8&&Math.abs((kp.x+fp.x)/2-point.x)<1e-8,"midpoint/elevation mismatch");
    System.out.println("COLOR_SYMMETRY_PASS Color="+COLORS[i]+" Kweebec="+kp+" Feran="+fp+" Pad="+point+" KDistance="+kd+" FDistance="+fd+" FacingAngleDegrees="+Math.toDegrees(Math.atan2(Math.abs(point.z-kp.z),Math.abs(kp.x-point.x)))+" KLOS="+rawK.los()+" FLOS="+rawF.los()+" KFOV="+rawK.sector()+" FFOV="+rawF.sector());
   }
   if(i==1)require(selected.orElseThrow().role().equals(K),"Kweebec faction wrong witness");
   if(i==3)require(selected.orElseThrow().role().equals(F),"Feran faction wrong witness");
   // Confirm the source component gates actual paired native inventory withdrawal.
   var receipts=new SimpleItemContainer((short)4);var confirmed=new ArrayList<ContainerVisualWitness.Evidence>();var snapshots=new IdentityHashMap<ContainerRemovalProbe.Removal,List<ContainerVisualWitness.Evidence>>();
   var probe=new ContainerRemovalProbe(c,r->{if(PersistentContainerOwnership.eligible(source.read()))confirmed.addAll(snapshots.getOrDefault(r,List.of()));},r->{if(PersistentContainerOwnership.eligible(source.read()))snapshots.put(r,ContainerVisualWitness.capture(actor,store,source.read(),WITNESSES,s->{}));});
   var a=c.registerChangeEvent(e->probe.source(e.transaction()));var b=receipts.registerChangeEvent(e->probe.receipt(e.transaction()));
   try{c.moveItemStackFromSlotToSlot((short)0,1,receipts,(short)0);require(probe.finish()==1,"paired withdrawal attribution");}finally{a.unregister();b.unregister();c.setItemStackForSlot((short)0,new ItemStack("Ingredient_Bar_Copper",16));mark(w,i);}
   var transferMessages=new ArrayList<com.hypixel.hytale.server.core.Message>();ContainerCaughtNotification.notifyWitnessed(confirmed,transferMessages::add);require(transferMessages.size()==messages.size(),"paired pipeline differs");
   System.out.println("FACTION_STATION_PASS Station="+(i+1)+" Owned="+d.owned()+" Scope="+d.scope()+" Faction="+d.faction()+" KweebecLOS="+rawK.los()+" FeranLOS="+rawF.los()+" Notifications="+messages.size()+" NoReattach="+!Boolean.getBoolean("factionfield.author"));
  }
  require(ContainerVisualWitness.RANGE==20&&ContainerVisualWitness.CROUCHED_RANGE==6,"container ranges changed");
  require(15.0*HyThiefStealthAcquisition.multiplier("sight","Feran_Civilian")==4.0,"normal perception changed");
  for(var witness:List.of(k,f))for(var name:List.of("Kweebec_Razorleaf","Feran_Sharptooth")){
   var wp=store.getComponent(witness,TransformComponent.getComponentType()).getPosition();var rp=store.getComponent(roles.get(name),TransformComponent.getComponentType()).getPosition();double distance=wp.distance(rp);require(distance<HyThiefContainerAlert.LOCAL_RADIUS,"outside LOCAL delivery");
   for(int i=0;i<ZS.length;i++)require(rp.distance(new Vector3d(0,64,padZ(i)))>20,"responder inside direct sight range");
   System.out.println("COLOR_ALERT_GEOMETRY_PASS Witness="+store.getComponent(witness,NPCEntity.getComponentType()).getRoleName()+" Responder="+name+" Distance="+distance+" OutsideAllStandingPads=true");
  }
  testVisualBoundaries(w,k,actor,store);
  if(Boolean.getBoolean("factionfield.persistence")){store.removeEntity(actor,RemoveReason.REMOVE);testPersistence(w);return;}
  String test=System.getProperty("factionfield.response","");
  if(!test.isEmpty()){
   boolean local=test.startsWith("Local");boolean feran=test.equals("Feran")||test.equals("LocalFeran");
   store.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(0,64,padZ(local?2:feran?3:1)));
   if(local&&!test.equals("LocalBoth")){int blockX=feran?-4:3;for(int y=64;y<=67;y++)NativeFieldBlocks.set(w,blockX,y,1,"Rock_Stone_Cobble");}
   var ownership=local?data(2):feran?data(3):data(1);
   var visuals=ContainerVisualWitness.capture(actor,store,ownership,WITNESSES,System.out::println);
   var notifications=new ArrayList<com.hypixel.hytale.server.core.Message>();
   var selected=ContainerCaughtNotification.notifyWitnessed(visuals,notifications::add).orElseThrow();
   require(notifications.size()==1&&(test.equals("LocalBoth")||selected.role().equals(feran?F:K)),"witness/notification mismatch "+test);
   var plugin=PluginManager.get().getPlugin(PickpocketPlugin.class);
   var before=new HashMap<Ref<EntityStore>,Vector3d>();for(var name:List.of("Kweebec_Razorleaf","Feran_Sharptooth")){var ref=roles.get(name);before.put(ref,new Vector3d(store.getComponent(ref,TransformComponent.getComponentType()).getPosition()));}
   var delivery=HyThiefContainerAlert.deliver(plugin,ownership,actor,selected.witness(),store,System.out::println);
   require(delivery.sent()&&delivery.responders().size()==(local?2:1),"wrong responder count "+delivery);
   if(local)require(delivery.type().equals("LOCAL")&&delivery.signal().equals(HyThiefContainerAlert.LOCAL),"wrong local alert");
   else require(delivery.responders().contains(roles.get(feran?"Feran_Sharptooth":"Kweebec_Razorleaf")),"wrong faction delivery");
   w.scheduleAfter(()->w.execute(()->{try{
    for(var name:List.of("Kweebec_Razorleaf","Feran_Sharptooth")){
     var ref=roles.get(name);require(ref.isValid(),"responder disappeared");var state=store.getComponent(ref,StateSupport.getComponentType());var marked=store.getComponent(ref,MarkedEntitySupport.getComponentType());
     boolean targeted=marked!=null&&Arrays.asList(marked.getEntityTargets()).contains(actor);double moved=before.get(ref).distance(store.getComponent(ref,TransformComponent.getComponentType()).getPosition());
     boolean expected=local||(feran?name.equals("Feran_Sharptooth"):name.equals("Kweebec_Razorleaf"));
     require(targeted==expected,"target faction mismatch "+name);
     if(expected)require(state.getStateName().startsWith("Attack")&&moved>.2,"no native pursuit "+name+" state="+state.getStateName()+" moved="+moved);
     System.out.println("THEFT_NATIVE_RESPONDER Role="+name+" Expected="+expected+" Targeted="+targeted+" State="+state.getStateName()+" Movement="+moved);
    }
    System.out.println("FACTION_NATIVE_RESPONSE_PASS Test="+test+" Type="+delivery.type()+" Signal="+delivery.signal()+" Responders="+delivery.responders().size()+" Notifications="+notifications.size()+" Witness="+selected.role());finish();
   }catch(Throwable error){fail(error);}}),3,TimeUnit.SECONDS);

  }else{store.removeEntity(actor,RemoveReason.REMOVE);finish();}
 }


 static void testVisualBoundaries(World w,Ref<EntityStore> witness,Ref<EntityStore> actor,Store<EntityStore> store){
  var origin=store.getComponent(witness,TransformComponent.getComponentType()).getPosition();var t=store.getComponent(actor,TransformComponent.getComponentType());
  var movement=store.getComponent(actor,com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent.getComponentType());
  double[] distances={5,8,8,18,22};boolean[] crouch={true,true,false,false,false};boolean[] expected={true,false,true,true,false};
  for(int i=0;i<distances.length;i++){
   var states=new com.hypixel.hytale.protocol.MovementStates();states.crouching=crouch[i];movement.setMovementStates(states);t.setPosition(new Vector3d(origin.x+distances[i],origin.y,origin.z));
   var e=ContainerVisualWitness.evaluate(witness,actor,t.getPosition(),movement.getMovementStates().crouching,store);require(e.seen()==expected[i],"container visual range boundary "+e.diagnostic());
   System.out.println("CONTAINER_RANGE_PASS Distance="+distances[i]+" Crouched="+crouch[i]+" Seen="+e.seen()+" Reason="+e.reason());
  }
  movement.setMovementStates(new com.hypixel.hytale.protocol.MovementStates());t.setPosition(new Vector3d(origin.x-3,origin.y,origin.z));var rear=ContainerVisualWitness.evaluate(witness,actor,t.getPosition(),false,store);require(!rear.seen()&&!rear.sector(),"rear FOV accepted");
  t.setPosition(new Vector3d(origin.x+5,origin.y,origin.z));for(int y=64;y<=67;y++)NativeFieldBlocks.set(w,-4,y,0,"Rock_Stone_Cobble");
  var blocked=ContainerVisualWitness.evaluate(witness,actor,t.getPosition(),false,store);require(!blocked.seen()&&blocked.sector()&&!blocked.los(),"opaque LOS block ignored");for(int y=64;y<=67;y++)NativeFieldBlocks.set(w,-4,y,0,"Empty");
  require(ContainerVisualWitness.evaluate(witness,actor,t.getPosition(),false,store).seen(),"LOS did not recover after wall removal");
  System.out.println("CONTAINER_FOV_LOS_PASS RearRejected=true OpaqueWallRejected=true ClearedLOSRestored=true NormalPerceptionStanding=15 NormalPerceptionCrouched=4");
 }

 static List<String> inventorySnapshot(ItemContainer c){
  var result=new ArrayList<String>();for(short slot=0;slot<c.getCapacity();slot++){var stack=c.getItemStack(slot);result.add(stack==null||stack.isEmpty()?"EMPTY":stack.getItemId()+":"+stack.getQuantity());}return result;
 }
 static OwnershipProofData ownership(World w,int i){var c=chest(w,i);var source=PersistentContainerOwnership.resolve(w,new com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow(0,64,ZS[i],0,NativeFieldBlocks.type(w,0,64,ZS[i]),c),c);require(source!=null,"ownership source absent");return source.read();}
 static void checkOwnership(World w,int i){var a=ownership(w,i);var b=data(i);require(a!=null&&a.owned()==b.owned()&&a.scope().equals(b.scope())&&a.faction().equals(b.faction()),"ownership changed station "+(i+1));}
 static void verifyPersistence(World w,String boundary)throws Exception{
  var expected=Files.readAllLines(Path.of("persistence-expected.txt"));
  for(int i=0;i<ZS.length;i++){checkOwnership(w,i);require(inventorySnapshot(chest(w,i)).toString().equals(expected.get(i)),"persisted inventory changed at "+boundary+" station "+(i+1));}
  System.out.println("OWNERSHIP_PERSISTENCE_PASS Boundary="+boundary+" Stations=4 OwnershipUnchanged=true InventoryUnchanged=true NoReattach=true");
 }
 static void testPersistence(World w)throws Exception{
  var expected=new ArrayList<String>();
  for(int i=0;i<ZS.length;i++){
   var c=chest(w,i);short last=(short)(c.getCapacity()-1),other=(short)(last-1);require(c.getItemStack(last)==null||c.getItemStack(last).isEmpty(),"sentinel slot not empty");
   var incoming=new SimpleItemContainer((short)2);incoming.setItemStackForSlot((short)0,new ItemStack("Ingredient_Fibre",i+3));
   var probe=new ContainerRemovalProbe(c,r->{throw new IllegalStateException("Insertion/rearrangement misclassified as withdrawal");},r->{});
   var events=c.registerChangeEvent(e->probe.source(e.transaction()));var receipts=incoming.registerChangeEvent(e->probe.receipt(e.transaction()));
   try{
    incoming.moveItemStackFromSlotToSlot((short)0,i+3,c,last);checkOwnership(w,i);
    c.moveItemStackFromSlotToSlot(last,i+3,c,other);checkOwnership(w,i);
    c.moveItemStackFromSlotToSlot(other,i+3,c,last);checkOwnership(w,i);
    require(probe.finish()==0,"insertion/internal rearrangement counted as outbound theft");
   }finally{events.unregister();receipts.unregister();}
   require(c.getItemStack(last)!=null&&c.getItemStack(last).getQuantity()==i+3,"native insertion/rearrangement failed");
   mark(w,i);expected.add(inventorySnapshot(c).toString());
   System.out.println("OWNERSHIP_TRANSACTION_PASS Station="+(i+1)+" OpenClose=true Withdrawal=true Insertion=true Rearrangement=true FalseWithdrawal=0");
  }
  Files.write(Path.of("persistence-expected.txt"),expected);
  var cs=w.getChunkStore();var keys=new HashSet<Long>();for(int z:ZS)keys.add(com.hypixel.hytale.math.util.ChunkUtil.indexChunkFromBlock(0,z));
  var saves=new ArrayList<CompletableFuture<Void>>();for(long key:keys){var ref=cs.getChunkReference(key);require(ref!=null,"chunk save ref missing");saves.add(cs.getSaver().saveChunkColumn(com.hypixel.hytale.math.util.ChunkUtil.xOfChunkIndex(key),com.hypixel.hytale.math.util.ChunkUtil.zOfChunkIndex(key),cs.getStore(),ref,w,()->{}));}
  CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new)).thenRun(()->w.execute(()->{try{
   cs.getSaver().flush();for(long key:keys){var ref=cs.getChunkReference(key);cs.remove(ref,RemoveReason.UNLOAD);}
   var loads=new ArrayList<CompletableFuture<?>>();for(int z:ZS)loads.add(cs.getChunkSectionReferenceAtBlockAsync(0,64,z,com.hypixel.hytale.server.core.universe.world.storage.GetChunkFlags.SET_TICKING));
   CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).thenRun(()->w.execute(()->{try{verifyPersistence(w,"nativeChunkUnloadReload");Files.writeString(Path.of("persistence-verification.json"),"{\"transactions\":true,\"nativeChunkUnloadReload\":true,\"ownershipUnchanged\":true,\"inventoryUnchanged\":true}");finish();}catch(Throwable t){fail(t);}})).exceptionally(t->{fail(t);return null;});
  }catch(Throwable t){fail(t);}})).exceptionally(t->{fail(t);return null;});
 }

 static void finish(){try{Files.writeString(Path.of("faction-verification.json"),"{\"fourStationsPassed\":true}");}catch(Exception e){throw new RuntimeException(e);}System.out.println("FACTION_FIELD_VERIFIED");CompletableFuture.runAsync(()->com.hypixel.hytale.server.core.HytaleServer.get().shutdownServer());}
 static void fail(Throwable t){t.printStackTrace();System.err.println("FACTION_FIELD_FAILED "+t);CompletableFuture.runAsync(()->com.hypixel.hytale.server.core.HytaleServer.get().shutdownServer());}
}
