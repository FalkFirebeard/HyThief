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
 static final int[] XS={-6,-2,0,6};
 static final String[] LABELS={"1 CONTROL - UNOWNED","2 FACTION - KWEEBEC","3 OWNED - NEARBY - STAND ON CENTER SEAM X=0","4 FACTION - FERAN"};
 static final String K="Kweebec_Sapling_HyThiefWitness", F="Feran_Civilian_HyThiefWitness";
 static final Set<String> WITNESSES=Set.of(K,F);
 static OwnershipProofData data(int i){return new OwnershipProofData(i!=0,i==2?"NEARBY":i==0?"":"FACTION",i==1?"Kweebec":i==3?"Feran":"");}
 static double padX(int i){return i==2?0.0:XS[i]+.5;}
 static boolean containsX(int x){for(int v:XS)if(v==x)return true;return false;}
 static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
 protected void start(){Universe.get().getUniverseReady().thenRun(()->{
  var w=Universe.get().getDefaultWorld();var loads=new ArrayList<CompletableFuture<?>>();
  for(int x=-1;x<=0;x++)for(int z=-1;z<=1;z++)loads.add(w.getChunkAsync(com.hypixel.hytale.math.util.ChunkUtil.indexChunk(x,z)));
  CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).thenRun(()->w.execute(()->{
   try{if(Boolean.getBoolean("factionfield.author"))author(w);
    if(Boolean.getBoolean("factionfield.symmetry"))symmetry(w);
    w.scheduleAfter(()->w.execute(()->{try{validate(w);}catch(Throwable e){fail(e);}}),5,TimeUnit.SECONDS);
   }catch(Throwable e){fail(e);}
  }));
 });}
 static ItemContainer chest(World w,int i){return chestAt(w,XS[i]);}
 static ItemContainer chestAt(World w,int x){var cs=w.getChunkStore();var section=cs.getChunkSectionReferenceAtBlock(x,64,12);
  var block=cs.getStore().getComponent(section,BlockComponentSection.getComponentType());
  BlockEntity.ensureBlockEntity(cs.getStore(),section,block,x,64,12,w.getBlockType(x,64,12),0);
  var inventory=BlockModule.getComponent(ItemContainerBlock.getComponentType(),w,x,64,12);
  require(inventory!=null,"native chest component missing");
  if(inventory.getItemContainer()==null){require(Boolean.getBoolean("factionfield.author")||Boolean.getBoolean("factionfield.symmetry"),"inventory lost on restart");inventory.setItemContainer(new SimpleItemContainer(inventory.getCapacity()));}
  return inventory.getItemContainer();
 }
 static void mark(World w,int i){var cs=w.getChunkStore();var section=cs.getChunkSectionReferenceAtBlock(XS[i],64,12);
  var block=cs.getStore().getComponent(section,BlockComponentSection.getComponentType());block.markBlockNeedsSaving(com.hypixel.hytale.math.util.ChunkUtil.indexBlock(XS[i],64,12));block.markNeedsSaving();}
 static Ref<EntityStore> spawn(World w,String role,double x,double z,String label){var store=w.getEntityStore().getStore();
  var pair=NPCPlugin.get().spawnNPC(store,role,null,new Vector3d(x,64,z),new Rotation3f(0,role.endsWith("HyThiefWitness")?(float)Math.PI:0,0));require(pair!=null,"spawn failed "+role);
  var ref=pair.first();store.putComponent(ref,Nameplate.getComponentType(),new Nameplate(label));return ref;
 }
 static void symmetry(World w){
  var old=chestAt(w,2);var items=new ItemStack[old.getCapacity()];for(short i=0;i<items.length;i++)items[i]=old.getItemStack(i);
  var source=BlockModule.getBlockEntity(w,2,64,12);var ownership=w.getChunkStore().getStore().getComponent(source,OwnershipProofData.getComponentType());
  require(ownership!=null&&ownership.owned()&&ownership.scope().equals("NEARBY"),"old NEARBY ownership missing");
  var preserved=new OwnershipProofData(ownership.owned(),ownership.scope(),ownership.faction());
  w.setBlock(2,64,12,"Empty");w.setBlock(1,64,12,"Empty");w.setBlock(2,63,11,"Rock_Stone_Cobble");
  w.setBlock(0,64,12,"Furniture_Kweebec_Chest_Large");var next=chest(w,2);for(short i=0;i<items.length;i++)next.setItemStackForSlot(i,items[i]);
  w.getChunkStore().getStore().addComponent(BlockModule.getBlockEntity(w,0,64,12),OwnershipProofData.getComponentType(),preserved);mark(w,2);
  w.setBlock(-1,63,11,"Rock_Stone_Brick");w.setBlock(0,63,11,"Rock_Stone_Brick");
  var store=w.getEntityStore().getStore();var labels=new ArrayList<Ref<EntityStore>>();var moved=new int[]{0};store.forEachChunk(Nameplate.getComponentType(),(chunk,b)->{for(int i=0;i<chunk.size();i++){
   var ref=chunk.getReferenceTo(i);var n=store.getComponent(ref,Nameplate.getComponentType());if(n.getText().startsWith("3 OWNED - NEARBY")){
    labels.add(ref);moved[0]++;
   }
  }});for(var ref:labels){store.getComponent(ref,TransformComponent.getComponentType()).setPosition(new Vector3d(0,66.5,12.5));store.putComponent(ref,Nameplate.getComponentType(),new Nameplate(LABELS[2]));}require(moved[0]==1,"NEARBY label count "+moved[0]);System.out.println("SYMMETRY_MOVE_PASS OldOrigin=2,64,12 NewOrigin=0,64,12 PlayerPad=0,64,11.5");
 }
 static void author(World w){
  for(int x=-26;x<=26;x++)for(int z=-16;z<=26;z++){
   w.setBlock(x,63,z,"Rock_Stone_Cobble");for(int y=64;y<=70;y++)w.setBlock(x,y,z,"Empty");
  }
  var k=spawn(w,K,-4.5,.5,"KWEEBEC WITNESS - stationary / faces stations");spawn(w,F,4.5,.5,"FERAN WITNESS - stationary / faces stations");
  spawn(w,"Kweebec_Razorleaf",-19.5,-8.5,"KWEEBEC RESPONDER - RAZORLEAF");spawn(w,"Feran_Sharptooth",19.5,-8.5,"FERAN RESPONDER - SHARPTOOTH");
  var store=w.getEntityStore().getStore();
  for(int i=0;i<XS.length;i++){
   w.setBlock(XS[i],64,12,"Furniture_Kweebec_Chest_Large");var c=chest(w,i);c.setItemStackForSlot((short)0,new ItemStack("Ingredient_Bar_Copper",16));c.setItemStackForSlot((short)1,new ItemStack("Ingredient_Bar_Iron",8));
   var ref=BlockModule.getBlockEntity(w,XS[i],64,12);w.getChunkStore().getStore().addComponent(ref,OwnershipProofData.getComponentType(),data(i));mark(w,i);
   w.setBlock(XS[i],63,11,"Rock_Stone_Brick");if(i==2)w.setBlock(-1,63,11,"Rock_Stone_Brick");label(store,LABELS[i],padX(i),66.5,12.5);
  }
  label(store,"FOUR CHESTS ONLY - USE YOUR BLOCKS FOR MANUAL LOS",.5,67,7.5);
  label(store,"1 CONTROL | 2 KWEEBEC | 3 NEARBY | 4 FERAN - RESET AFTER CAUGHT",.5,66.2,7.5);
  System.out.println("FACTION_FIELD_AUTHOR_PASS FourNativeChests=true SerializedOwnership=true");
 }
 static void label(Store<EntityStore> store,String text,double x,double y,double z){
  var holder=EntityStore.REGISTRY.newHolder();holder.addComponent(TransformComponent.getComponentType(),new TransformComponent(new Vector3d(x,y,z),new Rotation3f()));
  holder.addComponent(Nameplate.getComponentType(),new Nameplate(text));
  holder.addComponent(ModelComponent.getComponentType(),new ModelComponent(com.hypixel.hytale.server.core.asset.type.model.config.Model.createStaticScaledModel(com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset.getAssetMap().getAsset("Arrow_Iron"),.01f)));
  holder.addComponent(Frozen.getComponentType(),Frozen.get());holder.addComponent(com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType(),com.hypixel.hytale.server.core.entity.UUIDComponent.randomUUID());store.addEntity(holder,AddReason.SPAWN);
 }
 static void validate(World w)throws Exception{
  for(int x=-26;x<=26;x++)for(int z=-16;z<=26;z++)for(int y=64;y<=70;y++){
   boolean isChest=y==64&&z==12&&(containsX(x)||containsX(x+1)||containsX(x-1));
   require(isChest||"Empty".equals(w.getBlockType(x,y,z).getId()),"Unexpected above-ground obstacle "+x+","+y+","+z);
  }
  int chestCount=0;for(int x=-32;x<32;x++)for(int z=-32;z<64;z++)for(int y=0;y<=80;y++)if(w.getBlockType(x,y,z).getId().contains("Chest")&&w.getChunk(com.hypixel.hytale.math.util.ChunkUtil.indexChunkFromBlock(x,z)).getFiller(x,y,z)==0){
   require(y==64&&z==12&&containsX(x),"unexpected chest "+x+","+y+","+z);chestCount++;
  }
  require(chestCount==4,"native chest count="+chestCount);
  var players=Universe.get().getPlayerStorage().getPlayers();require(players.size()==1,"expected one starting player");var saved=Universe.get().getPlayerStorage().load(players.iterator().next()).join();
  require(saved!=null,"Firebeard starting player missing");var hotbar=saved.getComponent(InventoryComponent.Hotbar.getComponentType());
  require(hotbar!=null,"starting hotbar missing");var blocks=hotbar.getInventory().getItemStack((short)0);
  require(blocks!=null&&blocks.getItemId().equals("Rock_Stone_Cobble")&&blocks.getQuantity()==64,"starting building blocks missing");
  System.out.println("FOUR_CHEST_LAYOUT_PASS ChestCount=4 StartingBlocks=Rock_Stone_Cobble Quantity=64");
  System.out.println("FACTION_OPEN_FIELD_PASS NoPermanentWalls=true ClearResponderPaths=true");
  require(w.getBlockType(-1,63,11).getId().equals("Rock_Stone_Brick")&&w.getBlockType(0,63,11).getId().equals("Rock_Stone_Brick"),"center seam pad missing");var labelCount=new int[]{0};w.getEntityStore().getStore().forEachChunk(Nameplate.getComponentType(),(chunk,b)->{for(int i=0;i<chunk.size();i++){var ref=chunk.getReferenceTo(i);var store=w.getEntityStore().getStore();if(store.getComponent(ref,Nameplate.getComponentType()).getText().equals(LABELS[2])){require(store.getComponent(ref,TransformComponent.getComponentType()).getPosition().x==0,"center label position");labelCount[0]++;}}});require(labelCount[0]==1,"center label lost on restart");
  var store=w.getEntityStore().getStore();var roles=new HashMap<String,Ref<EntityStore>>();var npcCount=new int[]{0};
  store.forEachChunk(NPCEntity.getComponentType(),(chunk,b)->{for(int i=0;i<chunk.size();i++){npcCount[0]++;var ref=chunk.getReferenceTo(i);roles.put(store.getComponent(ref,NPCEntity.getComponentType()).getRoleName(),ref);}});
  require(npcCount[0]==4&&roles.size()==4,"expected exactly four NPC roles");
  var k=roles.get(K);var f=roles.get(F);require(k!=null&&f!=null,"witness roles missing");
  for(var name:List.of("Kweebec_Razorleaf","Feran_Sharptooth")){var ref=roles.get(name);require(ref!=null&&store.getComponent(ref,Frozen.getComponentType())==null,"responder missing or frozen");System.out.println("FIELD_RESPONDER Role="+name+" Position="+(ref==null?"missing":store.getComponent(ref,TransformComponent.getComponentType()).getPosition())+" State="+(ref==null?"missing":store.getComponent(ref,StateSupport.getComponentType()).getStateName()));}
  for(var ref:List.of(k,f)) {require(store.getComponent(ref,Frozen.getComponentType())==null,"witness Frozen disables role processing");var t=store.getComponent(ref,TransformComponent.getComponentType());var h=store.getComponent(ref,HeadRotation.getComponentType());require(Math.abs(Math.abs(h.getRotation().yaw())-Math.PI)<.001,"witness head rotated");require(Math.abs(t.getPosition().z-.5)<.01,"witness wandered");}
  require(ContainerOwnershipPolicy.member(k,"Kweebec",store)&&!ContainerOwnershipPolicy.member(k,"Feran",store),"native Kweebec group");
  require(ContainerOwnershipPolicy.member(f,"Feran",store)&&!ContainerOwnershipPolicy.member(f,"Kweebec",store),"native Feran group");
  var holder=EntityStore.REGISTRY.newHolder();holder.addComponent(TransformComponent.getComponentType(),new TransformComponent(new Vector3d(.5,64,11.5),new Rotation3f()));
  holder.addComponent(com.hypixel.hytale.server.core.modules.physics.component.Velocity.getComponentType(),new com.hypixel.hytale.server.core.modules.physics.component.Velocity());
  holder.addComponent(ModelComponent.getComponentType(),new ModelComponent(store.getComponent(k,ModelComponent.getComponentType()).getModel()));
  var box=store.getComponent(k,BoundingBox.getComponentType());if(box!=null)holder.addComponent(BoundingBox.getComponentType(),(BoundingBox)box.clone());
  var movement=new com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent();movement.setMovementStates(new com.hypixel.hytale.protocol.MovementStates());holder.addComponent(com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent.getComponentType(),movement);
  var actor=store.addEntity(holder,AddReason.SPAWN);
  for(int i=0;i<XS.length;i++){
   var c=chest(w,i);var source=PersistentContainerOwnership.resolve(w,new com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow(XS[i],64,12,0,w.getBlockType(XS[i],64,12),c),c);
   require(source!=null,"source identity");var d=source.read();var expected=data(i);require(d!=null&&d.owned()==expected.owned()&&d.scope().equals(expected.scope())&&d.faction().equals(expected.faction()),"ownership/faction lost on save/restart station "+(i+1));
   var base=w.getBlockType(XS[i],64,12);require("Open_Container".equals(base.getInteractions().get(com.hypixel.hytale.protocol.InteractionType.Use)),"native chest Use changed");
   var section=w.getChunkStore().getChunkSectionReferenceAtBlock(XS[i],64,12);
   for(String state:new String[]{"OpenWindow","CloseWindow"}){
    com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations.setBlockInteractionState(w.getChunkStore(),section,XS[i],64,12,base,state,false);
    require(chest(w,i)==c&&source.read().faction().equals(expected.faction()),"native open/close lost inventory/ownership");
   }
   store.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(padX(i),64,11.5));
   var rawK=ContainerVisualWitness.evaluate(k,actor,store.getComponent(actor,TransformComponent.getComponentType()).getPosition(),false,store);
   var rawF=ContainerVisualWitness.evaluate(f,actor,store.getComponent(actor,TransformComponent.getComponentType()).getPosition(),false,store);
   require(rawK.sector()&&rawF.sector(),"outside forward FOV station "+(i+1));
   require(rawK.los()&&rawF.los(),"wrong occlusion station "+(i+1)+" K="+rawK+" F="+rawF);
   var visual=ContainerVisualWitness.capture(actor,store,d,WITNESSES,System.out::println);var messages=new ArrayList<com.hypixel.hytale.server.core.Message>();
   var selected=ContainerCaughtNotification.notifyWitnessed(visual,messages::add);
   require(messages.size()==(i!=0?1:0),"notification station "+(i+1));
   if(i==2){require(visual.size()==2&&visual.stream().allMatch(ContainerVisualWitness.Evidence::seen),"NEARBY filtered faction");
    var kp=store.getComponent(k,TransformComponent.getComponentType()).getPosition();var fp=store.getComponent(f,TransformComponent.getComponentType()).getPosition();var point=new Vector3d(padX(i),64,11.5);
    double kd=kp.distance(point),fd=fp.distance(point);require(Math.abs(kd-fd)<1e-8&&kd<20&&fd<20,"unequal/out-of-range distances");
    require(Math.abs(kp.y-fp.y)<1e-8&&Math.abs((kp.x+fp.x)/2-point.x)<1e-8,"midpoint/elevation mismatch");
    System.out.println("NEARBY_SYMMETRY_PASS Kweebec="+kp+" Feran="+fp+" Pad="+point+" KDistance="+kd+" FDistance="+fd+" FacingAngleDegrees="+Math.toDegrees(Math.atan2(Math.abs(kp.x-point.x),point.z-kp.z))+" KLOS="+rawK.los()+" FLOS="+rawF.los()+" KFOV="+rawK.sector()+" FFOV="+rawF.sector());
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
  String test=System.getProperty("factionfield.response","");
  if(!test.isEmpty()){
   boolean local=test.startsWith("Local");boolean feran=test.equals("Feran")||test.equals("LocalFeran");
   store.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(local?padX(2):feran?6.5:-1.5,64,11.5));
   if(local){int blockX=feran?-3:2;for(int y=64;y<=67;y++)w.setBlock(blockX,y,6,"Rock_Stone_Cobble");}
   var ownership=local?data(2):feran?data(3):data(1);
   var visuals=ContainerVisualWitness.capture(actor,store,ownership,WITNESSES,System.out::println);
   var notifications=new ArrayList<com.hypixel.hytale.server.core.Message>();
   var selected=ContainerCaughtNotification.notifyWitnessed(visuals,notifications::add).orElseThrow();
   require(notifications.size()==1&&selected.role().equals(feran?F:K),"witness/notification mismatch "+test);
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
 static void finish(){try{Files.writeString(Path.of("faction-verification.json"),"{\"fourStationsPassed\":true}");}catch(Exception e){throw new RuntimeException(e);}System.out.println("FACTION_FIELD_VERIFIED");CompletableFuture.runAsync(()->com.hypixel.hytale.server.core.HytaleServer.get().shutdownServer());}
 static void fail(Throwable t){t.printStackTrace();System.err.println("FACTION_FIELD_FAILED "+t);CompletableFuture.runAsync(()->com.hypixel.hytale.server.core.HytaleServer.get().shutdownServer());}
}
