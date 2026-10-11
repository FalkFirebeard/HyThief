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
 static final int[] XS={-6,-2,2,6,-12,11};
 static final String[] LABELS={"1 CONTROL - UNOWNED","2 OWNED - NEARBY","3 FACTION - KWEEBEC","4 FACTION - FERAN","5 KWEEBEC OWNED - KWEEBEC BLOCKED","6 FERAN OWNED - FERAN BLOCKED"};
 static final String K="Kweebec_Sapling_HyThiefWitness", F="Feran_Civilian_HyThiefWitness";
 static final Set<String> WITNESSES=Set.of(K,F);
 static OwnershipProofData data(int i){return new OwnershipProofData(i!=0,i==1?"NEARBY":i==0?"":"FACTION",i==2||i==4?"Kweebec":i==3||i==5?"Feran":"");}
 static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
 protected void start(){Universe.get().getUniverseReady().thenRun(()->{
  var w=Universe.get().getDefaultWorld();var loads=new ArrayList<CompletableFuture<?>>();
  for(int x=-1;x<=0;x++)for(int z=-1;z<=1;z++)loads.add(w.getChunkAsync(com.hypixel.hytale.math.util.ChunkUtil.indexChunk(x,z)));
  CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).thenRun(()->w.execute(()->{
   try{if(Boolean.getBoolean("factionfield.author"))author(w);
    w.scheduleAfter(()->w.execute(()->{try{validate(w);}catch(Throwable e){fail(e);}}),5,TimeUnit.SECONDS);
   }catch(Throwable e){fail(e);}
  }));
 });}
 static ItemContainer chest(World w,int i){int x=XS[i];var cs=w.getChunkStore();var section=cs.getChunkSectionReferenceAtBlock(x,64,12);
  var block=cs.getStore().getComponent(section,BlockComponentSection.getComponentType());
  BlockEntity.ensureBlockEntity(cs.getStore(),section,block,x,64,12,w.getBlockType(x,64,12),0);
  var inventory=BlockModule.getComponent(ItemContainerBlock.getComponentType(),w,x,64,12);
  require(inventory!=null,"native chest component missing");
  if(inventory.getItemContainer()==null){require(Boolean.getBoolean("factionfield.author"),"inventory lost on restart");inventory.setItemContainer(new SimpleItemContainer(inventory.getCapacity()));}
  return inventory.getItemContainer();
 }
 static void mark(World w,int i){var cs=w.getChunkStore();var section=cs.getChunkSectionReferenceAtBlock(XS[i],64,12);
  var block=cs.getStore().getComponent(section,BlockComponentSection.getComponentType());block.markBlockNeedsSaving(com.hypixel.hytale.math.util.ChunkUtil.indexBlock(XS[i],64,12));block.markNeedsSaving();}
 static Ref<EntityStore> spawn(World w,String role,double x,double z,String label){var store=w.getEntityStore().getStore();
  var pair=NPCPlugin.get().spawnNPC(store,role,null,new Vector3d(x,64,z),new Rotation3f(0,role.endsWith("HyThiefWitness")?(float)Math.PI:0,0));require(pair!=null,"spawn failed "+role);
  var ref=pair.first();store.putComponent(ref,Nameplate.getComponentType(),new Nameplate(label));return ref;
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
   w.setBlock(XS[i],63,11,"Rock_Stone_Brick");label(store,LABELS[i],XS[i]+.5,66.5,12.5);
  }
  for(int x:new int[]{-8,7})for(int y=64;y<=67;y++)for(int z=5;z<=6;z++)w.setBlock(x,y,z,"Rock_Stone_Brick");
  // Native responders stay mobile; shield direct view and provide an open rear exit.
  for(int side:new int[]{-1,1})for(int y=64;y<=67;y++){
   for(int x=17;x<=22;x++)w.setBlock(side*x,y,-5,"Rock_Stone_Brick");
   for(int z=-10;z<=-5;z++){w.setBlock(side*17,y,z,"Rock_Stone_Brick");w.setBlock(side*22,y,z,"Rock_Stone_Brick");}
  }
  label(store,"SAFE FIRST: 1 CONTROL, 5 KWEEBEC BLOCKED, 6 FERAN BLOCKED",.5,67,7.5);
  label(store,"CAUGHT TESTS: 3 KWEEBEC, RESET, 4 FERAN, RESET, 2 NEARBY",.5,66.2,7.5);
  System.out.println("FACTION_FIELD_AUTHOR_PASS SixNativeChests=true SerializedOwnership=true");
 }
 static void label(Store<EntityStore> store,String text,double x,double y,double z){
  var holder=EntityStore.REGISTRY.newHolder();holder.addComponent(TransformComponent.getComponentType(),new TransformComponent(new Vector3d(x,y,z),new Rotation3f()));
  holder.addComponent(Nameplate.getComponentType(),new Nameplate(text));
  holder.addComponent(ModelComponent.getComponentType(),new ModelComponent(com.hypixel.hytale.server.core.asset.type.model.config.Model.createStaticScaledModel(com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset.getAssetMap().getAsset("Arrow_Iron"),.01f)));
  holder.addComponent(Frozen.getComponentType(),Frozen.get());holder.addComponent(com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType(),com.hypixel.hytale.server.core.entity.UUIDComponent.randomUUID());store.addEntity(holder,AddReason.SPAWN);
 }
 static void validate(World w)throws Exception{
  var store=w.getEntityStore().getStore();var roles=new HashMap<String,Ref<EntityStore>>();
  store.forEachChunk(NPCEntity.getComponentType(),(chunk,b)->{for(int i=0;i<chunk.size();i++){var ref=chunk.getReferenceTo(i);roles.put(store.getComponent(ref,NPCEntity.getComponentType()).getRoleName(),ref);}});
  var k=roles.get(K);var f=roles.get(F);require(k!=null&&f!=null,"witness roles missing");
  for(var name:List.of("Kweebec_Razorleaf","Feran_Sharptooth")){var ref=roles.get(name);System.out.println("FIELD_RESPONDER Role="+name+" Position="+(ref==null?"missing":store.getComponent(ref,TransformComponent.getComponentType()).getPosition())+" State="+(ref==null?"missing":store.getComponent(ref,StateSupport.getComponentType()).getStateName()));}
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
   store.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(XS[i]+.5,64,11.5));
   var rawK=ContainerVisualWitness.evaluate(k,actor,store.getComponent(actor,TransformComponent.getComponentType()).getPosition(),false,store);
   var rawF=ContainerVisualWitness.evaluate(f,actor,store.getComponent(actor,TransformComponent.getComponentType()).getPosition(),false,store);
   require(rawK.sector()&&rawF.sector(),"outside forward FOV station "+(i+1));
   require(rawK.los()==(i!=4)&&rawF.los()==(i!=5),"wrong occlusion station "+(i+1)+" K="+rawK+" F="+rawF);
   var visual=ContainerVisualWitness.capture(actor,store,d,WITNESSES,System.out::println);var messages=new ArrayList<com.hypixel.hytale.server.core.Message>();
   var selected=ContainerCaughtNotification.notifyWitnessed(visual,messages::add);
   require(messages.size()==((i==1||i==2||i==3)?1:0),"notification station "+(i+1));
   if(i==1)require(visual.size()==2&&visual.stream().allMatch(ContainerVisualWitness.Evidence::seen),"NEARBY filtered faction");
   if(i==2)require(selected.orElseThrow().role().equals(K),"Kweebec faction wrong witness");
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
   var victim=test.equals("Feran")?f:k;var target=roles.get(test.equals("Feran")?"Feran_Sharptooth":"Kweebec_Razorleaf");require(target!=null,"responder missing");
   store.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(test.equals("Feran")?6.5:2.5,64,11.5));
   var plugin=PluginManager.get().getPlugin(PickpocketPlugin.class);var before=new Vector3d(store.getComponent(target,TransformComponent.getComponentType()).getPosition());
   var delivery=HyThiefGuardAlert.deliverContainer(plugin,actor,victim,store,System.out::println);require(delivery.sent()&&delivery.guard().equals(target),"faction responder not selected "+delivery);
   w.scheduleAfter(()->w.execute(()->{try{require(target.isValid(),"chosen guard disappeared during native response");var state=store.getComponent(target,StateSupport.getComponentType());var marked=store.getComponent(target,MarkedEntitySupport.getComponentType());double moved=before.distance(store.getComponent(target,TransformComponent.getComponentType()).getPosition());require(Arrays.asList(marked.getEntityTargets()).contains(actor),"offender not marked");require(state.getStateName().startsWith("Attack")&&moved>.2,"no native pursuit "+state.getStateName()+" moved="+moved);var other=roles.get(test.equals("Feran")?"Kweebec_Razorleaf":"Feran_Sharptooth");require(other.isValid(),"unchosen responder disappeared");var otherMarked=store.getComponent(other,MarkedEntitySupport.getComponentType());require(otherMarked==null||!Arrays.asList(otherMarked.getEntityTargets()).contains(actor),"wrong faction responder targeted offender");System.out.println("FACTION_NATIVE_RESPONSE_PASS Faction="+test+" State="+state.getStateName()+" ExactOffender=true WrongFactionTargeted=false Movement="+moved);finish();}catch(Throwable e){fail(e);}}),3,TimeUnit.SECONDS);
  }else{store.removeEntity(actor,RemoveReason.REMOVE);finish();}
 }
 static void finish(){try{Files.writeString(Path.of("faction-verification.json"),"{\"sixStationsPassed\":true}");}catch(Exception e){throw new RuntimeException(e);}System.out.println("FACTION_FIELD_VERIFIED");CompletableFuture.runAsync(()->com.hypixel.hytale.server.core.HytaleServer.get().shutdownServer());}
 static void fail(Throwable t){t.printStackTrace();System.err.println("FACTION_FIELD_FAILED "+t);CompletableFuture.runAsync(()->com.hypixel.hytale.server.core.HytaleServer.get().shutdownServer());}
}
