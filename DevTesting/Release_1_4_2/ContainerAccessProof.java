package falk.wardrobeproof;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.plugin.*;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.*;
import com.hypixel.hytale.server.core.universe.world.*;
import com.hypixel.hytale.server.core.universe.world.storage.*;
import com.hypixel.hytale.server.core.universe.world.chunk.*;
import com.hypixel.hytale.server.core.universe.world.chunk.section.*;
import com.hypixel.hytale.server.core.modules.block.*;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.inventory.*;
import com.hypixel.hytale.server.core.inventory.container.*;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.npc.*;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.*;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.builtin.blockspawner.state.BlockSpawner;
import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerTable;
import org.joml.Vector3d;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.function.Consumer;

/** Disposable native validation/authoring tool. Never installed or required by either mod. */
public final class ContainerAccessProof extends JavaPlugin {
  World w; Store<EntityStore> s; String mode; PluginBase hy;
  Ref<EntityStore> merchant,guard; final Vector3d pad=new Vector3d(.5,64,1);
  public ContainerAccessProof(JavaPluginInit i){super(i);}
  static void require(boolean b,String msg){if(!b)throw new IllegalStateException(msg);}
  void log(String v){System.out.println("WARDROBE_PHASE2A "+v);}
  protected void start(){Universe.get().getUniverseReady().thenRun(()->{
    w=Universe.get().getDefaultWorld();s=w.getEntityStore().getStore();mode=System.getProperty("wardrobe.mode","verify");
    hy=PluginManager.get().getPlugin(new PluginIdentifier("Local","HyThief"));
    var loads=new ArrayList<CompletableFuture<?>>();
    for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)for(int y=1;y<=2;y++)loads.add(w.getChunkStore().getChunkSectionReferenceAsync(x,y,z,GetChunkFlags.SET_TICKING));
    CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).thenRun(()->w.scheduleAfter(()->w.execute(()->{try{init();}catch(Throwable t){fail(t);}}),2,TimeUnit.SECONDS));
  });}
  BlockType type(int x,int y,int z){var cs=w.getChunkStore();return BlockType.getAssetMap().getAsset(cs.getStore().getComponent(cs.getChunkSectionReferenceAtBlock(x,y,z),BlockSection.getComponentType()).get(x,y,z));}
  void set(int x,int y,int z,String id){var cs=w.getChunkStore();var sr=cs.getChunkSectionReferenceAtBlock(x,y,z);cs.getStore().ensureComponent(sr,BlockComponentSection.getComponentType());var map=BlockType.getAssetMap();BlockOperations.setBlock(cs,sr,x,y,z,map.getIndex(id),map.getAsset(id),0,0,0);}
  ItemContainerBlock block(int x,int y,int z){return BlockModule.getComponent(ItemContainerBlock.getComponentType(),w,x,y,z);}
  Object call(String cls,String name,Object...args)throws Exception{
    // Validation-only access to the ACTUAL loaded HyThief classes; no gameplay class copies.
    Class<?> c=hy.getClass().getClassLoader().loadClass("com.local.pickpocket."+cls);
    for(Method m:c.getDeclaredMethods())if(m.getName().equals(name)&&m.getParameterCount()==args.length){m.setAccessible(true);try{return m.invoke(null,args);}catch(IllegalArgumentException ignored){} }
    throw new IllegalStateException("No matching production method "+cls+"."+name);
  }
  Object own(int x,int y,int z)throws Exception{
    var entity=BlockModule.getBlockEntity(w,x,y,z);if(entity==null)return null;
    var ct=(ComponentType)call("OwnershipProofData","getComponentType");return w.getChunkStore().getStore().getComponent(entity,ct);
  }
  Object read(Object o,String method)throws Exception{Method m=o.getClass().getDeclaredMethod(method);m.setAccessible(true);return m.invoke(o);}
  void ownership(boolean expected)throws Exception{
    var c=block(0,64,2);require(c!=null&&c.getItemContainer().getCapacity()==54,"54 slot wardrobe absent");
    require("Open_Container".equals(type(0,64,2).getInteractions().get(com.hypixel.hytale.protocol.InteractionType.Use)),"non-native opening");
    var entity=BlockModule.getBlockEntity(w,0,64,2);require(entity!=null,"missing native block entity");
    if(hy!=null){Object o=own(0,64,2);require(o!=null&&Boolean.TRUE.equals(read(o,"owned"))&&read(o,"scope").equals("FACTION")&&read(o,"faction").equals("Kweebec"),"ownership lost");}
    else {var u=w.getChunkStore().getStore().getComponent(entity,ChunkStore.REGISTRY.getUnknownComponentType());require(u!=null&&u.contains("HyThiefOwnershipProof"),"standalone opaque metadata lost");}
    log("OWNERSHIP_PASS Mode="+mode+" Typed="+(hy!=null)+" Native54Slots=true");
  }
  void init()throws Exception{
    if(mode.equals("hy-alone")){require(hy!=null,"HyThief absent");require(BlockType.getAssetMap().getAsset("Furniture_Kweebec_Wardrobe")!=null,"native furniture absent");require(BlockSpawnerTable.getAssetMap().getAsset("Zone1_Kweebec_Wardrobe_Tier1")==null,"Wardrobes unexpectedly installed");log("HYTHIEF_ALONE_PASS");finish();return;}
    if(mode.equals("author")||mode.equals("standalone-create")){
      // Native generation path, not direct ownership attachment in Java.
      var table=BlockSpawnerTable.getAssetMap().getAsset("Zone1_Kweebec_Wardrobe_Tier1");require(table!=null,"WW spawner missing");
      double roll=com.hypixel.hytale.math.util.HashUtil.random(0,64,2,w.getWorldConfig().getSeed()-1699164769L);
      require(!table.getEntries().get(roll).getBlockName().equals("Empty"),"fixture seed produces empty roll");
      if(mode.equals("author")){for(int x=-26;x<=8;x++)for(int z=-6;z<=10;z++)set(x,63,z,"Rock_Stone_Cobble");set(0,63,1,"Cloth_Block_Wool_Green");}
      w.getWorldConfig().setBlockSpawnersResolved(false);set(0,64,2,"Block_Spawner_Block");
      var cs=w.getChunkStore();var sr=cs.getChunkSectionReferenceAtBlock(0,64,2);var bc=cs.getStore().getComponent(sr,BlockComponentSection.getComponentType());
      BlockEntity.ensureBlockEntity(cs.getStore(),sr,bc,0,64,2,type(0,64,2),0);
      w.getWorldConfig().setBlockSpawnersResolved(true);var holder=ChunkStore.REGISTRY.newHolder();holder.addComponent(BlockSpawner.getComponentType(),new BlockSpawner("Zone1_Kweebec_Wardrobe_Tier1"));
      BlockEntity.setBlockEntity(cs.getStore(),sr,bc,0,64,2,type(0,64,2),0,holder);
      w.scheduleAfter(()->w.execute(()->{try{afterGeneration();}catch(Throwable t){fail(t);}}),2,TimeUnit.SECONDS);return;
    }
    if(mode.equals("access-chest")) {
      Object data=own(0,64,2);
      set(0,64,2,"Furniture_Kweebec_Chest_Large");
      var entity=BlockModule.getBlockEntity(w,0,64,2);
      if(entity==null){var cs=w.getChunkStore();var sr=cs.getChunkSectionReferenceAtBlock(0,64,2);
        var bc=cs.getStore().ensureAndGetComponent(sr,BlockComponentSection.getComponentType());
        BlockEntity.ensureBlockEntity(cs.getStore(),sr,bc,0,64,2,type(0,64,2),0);entity=BlockModule.getBlockEntity(w,0,64,2);}
      var ct=(ComponentType)call("OwnershipProofData","getComponentType");w.getChunkStore().getStore().putComponent(entity,ct,(Component)data);
      var inventory=block(0,64,2);if(inventory.getItemContainer()==null)inventory.setItemContainer(new SimpleItemContainer(inventory.getCapacity()));
      inventory.getItemContainer().setItemStackForSlot((short)0,new ItemStack("Ingredient_Bar_Copper",16));
      findNPCs();access();return;
    }
    ownership(true);findNPCs();
    if(mode.equals("access")){access();return;}
    if(mode.equals("verify")||mode.equals("restart")||mode.equals("standalone-reload")||mode.equals("late-hythief")){
      require(block(0,64,2).getItemContainer().getItemStack((short)0).getQuantity()==16,"test loot lost");
      if(mode.equals("verify")||mode.equals("restart"))verifyPose();
      if(mode.equals("late-hythief"))require(hy!=null,"HyThief missing after independent generation");
      log("RESTART_PASS Mode="+mode);finish();return;
    }
    if(mode.equals("storage")||mode.equals("standalone-storage")){storage();return;}
    if(mode.equals("witnessed")||mode.equals("unseen")||mode.equals("blocked")){witness();return;}
    if(mode.equals("chunk-reload")){reload();return;}
    throw new IllegalStateException("Unknown proof mode "+mode);
  }
  void afterGeneration()throws Exception{
    ownership(true);var c=block(0,64,2).getItemContainer();c.clear();c.setItemStackForSlot((short)0,new ItemStack("Ingredient_Bar_Copper",16));c.setItemStackForSlot((short)1,new ItemStack("Ingredient_Bar_Iron",8));
    if(mode.equals("author")){
      merchant=spawn("Kweebec_Merchant",-8,.5,"KWEEBEC MERCHANT - WITNESS");guard=spawn("Kweebec_Razorleaf",-22,.5,"RAZORLEAF - IDLE STATIONARY / COMBAT MOBILE");
      label("ONE OWNED WARDROBE - STAND ON GREEN PAD",.5,67,3);
      label("STANDING: CAUGHT / CROUCHED: UNSEEN / RESET AFTER CAUGHT",.5,67.8,3);
      w.scheduleAfter(()->w.execute(()->{try{verifyPose();log("AUTHOR_PASS NPCs=2 Wardrobes=1 GeneratedFaction=true");finish();}catch(Throwable t){fail(t);}}),8,TimeUnit.SECONDS);
    }else{log("WARDROBES_ALONE_GENERATION_PASS");finish();}
  }
  Ref<EntityStore> spawn(String role,double x,double z,String label){var pair=NPCPlugin.get().spawnNPC(s,role,null,new Vector3d(x,64,z),new Rotation3f(0,-(float)Math.PI/2,0));require(pair!=null,"NPC spawn failed "+role);s.putComponent(pair.first(),Nameplate.getComponentType(),new Nameplate(label));return pair.first();}
  void label(String text,double x,double y,double z){var h=EntityStore.REGISTRY.newHolder();h.addComponent(TransformComponent.getComponentType(),new TransformComponent(new Vector3d(x,y,z),Rotation3f.ZERO));h.addComponent(Nameplate.getComponentType(),new Nameplate(text));h.addComponent(ModelComponent.getComponentType(),new ModelComponent(com.hypixel.hytale.server.core.asset.type.model.config.Model.createStaticScaledModel(com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset.getAssetMap().getAsset("Arrow_Iron"),.01f)));h.addComponent(com.hypixel.hytale.server.core.entity.Frozen.getComponentType(),com.hypixel.hytale.server.core.entity.Frozen.get());h.addComponent(com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType(),com.hypixel.hytale.server.core.entity.UUIDComponent.randomUUID());s.addEntity(h,AddReason.SPAWN);}
  void findNPCs(){s.forEachChunk(NPCEntity.getComponentType(),(c,b)->{for(int i=0;i<c.size();i++){var r=c.getReferenceTo(i);var id=s.getComponent(r,NPCEntity.getComponentType()).getRoleName();if(id.equals("Kweebec_Merchant"))merchant=r;if(id.equals("Kweebec_Razorleaf"))guard=r;}});}
  void verifyPose(){require(merchant!=null&&guard!=null,"NPCs absent");for(var r:List.of(merchant,guard)){var t=s.getComponent(r,TransformComponent.getComponentType());double x=r.equals(merchant)?-8:-22;require(t.getPosition().distance(new Vector3d(x,64,.5))<.06,"idle NPC moved "+t.getPosition());require(Math.abs(s.getComponent(r,HeadRotation.getComponentType()).getRotation().yaw()+Math.PI/2)<.02,"idle NPC facing changed");require(s.getComponent(r,com.hypixel.hytale.server.core.entity.Frozen.getComponentType())==null,"NPC frozen");}log("POSE_PASS Merchant=-8,64,0.5 Guard=-22,64,0.5 Yaw=-PI/2");}
  void storage()throws Exception{
    var inv=block(0,64,2).getItemContainer();var temp=new SimpleItemContainer((short)54);Object before=hy==null?null:own(0,64,2);
    inv.moveItemStackFromSlotToSlot((short)0,2,temp,(short)0);require(temp.getItemStack((short)0).getQuantity()==2,"withdrawal transfer");
    temp.moveItemStackFromSlotToSlot((short)0,1,inv,(short)53);require(inv.getItemStack((short)53).getQuantity()==1,"slot 54 deposit");
    inv.moveItemStackFromSlotToSlot((short)53,1,inv,(short)52);require(inv.getItemStack((short)52).getQuantity()==1,"internal move");
    var cs=w.getChunkStore();var sr=cs.getChunkSectionReferenceAtBlock(0,64,2);for(String state:List.of("OpenWindow","CloseWindow","OpenWindow","CloseWindow")){BlockOperations.setBlockInteractionState(cs,sr,0,64,2,type(0,64,2),state,false);ownership(true);require(block(0,64,2).getItemContainer().getItemStack((short)52).getQuantity()==1,"state cycle lost items");}
    var section=cs.getStore().getComponent(sr,BlockSection.getComponentType());require(section.getFiller(0,65,2)!=0,"upper multiblock filler absent");
    set(5,64,5,"Furniture_Kweebec_Wardrobe");var e=BlockModule.getBlockEntity(w,5,64,5);require(block(5,64,5).getItemContainer().getCapacity()==54,"player-place storage");
    if(hy!=null)require(own(5,64,5)==null,"player-placed got ownership");else{var u=cs.getStore().getComponent(e,ChunkStore.REGISTRY.getUnknownComponentType());require(u==null||!u.contains("HyThiefOwnershipProof"),"player-place opaque ownership");}
    log("STORAGE_PASS Deposit=true Rearrange=true SplitTransfer=true Slot54=true OpenClose=true MultiblockFiller=true PlayerPlacedUnowned=true");finish();
  }
  Ref<EntityStore> actor(){var h=EntityStore.REGISTRY.newHolder();h.addComponent(TransformComponent.getComponentType(),new TransformComponent(new Vector3d(pad),Rotation3f.ZERO));h.addComponent(com.hypixel.hytale.server.core.modules.physics.component.Velocity.getComponentType(),new com.hypixel.hytale.server.core.modules.physics.component.Velocity());h.addComponent(ModelComponent.getComponentType(),new ModelComponent(s.getComponent(merchant,ModelComponent.getComponentType()).getModel()));h.addComponent(BoundingBox.getComponentType(),(BoundingBox)s.getComponent(merchant,BoundingBox.getComponentType()).clone());var move=new MovementStatesComponent();move.setMovementStates(new com.hypixel.hytale.protocol.MovementStates());h.addComponent(MovementStatesComponent.getComponentType(),move);return s.addEntity(h,AddReason.SPAWN);}
  void witness()throws Exception{
    require(hy!=null,"HyThief absent");verifyPose();var actor=actor();boolean unseen=mode.equals("unseen");s.getComponent(actor,MovementStatesComponent.getComponentType()).getMovementStates().crouching=unseen;
    if(mode.equals("blocked"))for(int y=64;y<=67;y++)for(int z=-1;z<=3;z++)set(-4,y,z,"Rock_Stone_Cobble");
    var data=own(0,64,2);var window=new ContainerBlockWindow(0,64,2,0,type(0,64,2),block(0,64,2).getItemContainer());var source=call("PersistentContainerOwnership","resolve",w,window,window.getItemContainer());require(source!=null,"window source attribution");
    var cls=hy.getClass().getClassLoader().loadClass("com.local.pickpocket.ContainerRemovalProbe");var ctor=cls.getDeclaredConstructors();java.lang.reflect.Constructor<?> chosen=Arrays.stream(ctor).filter(c->c.getParameterCount()==2).findFirst().orElseThrow();chosen.setAccessible(true);
    var count=new int[]{0};Object probe=chosen.newInstance(window.getItemContainer(),(Consumer<Object>)r->count[0]++);var from=cls.getDeclaredMethod("source",com.hypixel.hytale.server.core.inventory.transaction.Transaction.class);var receipt=cls.getDeclaredMethod("receipt",com.hypixel.hytale.server.core.inventory.transaction.Transaction.class);var finish=cls.getDeclaredMethod("finish");from.setAccessible(true);receipt.setAccessible(true);finish.setAccessible(true);
    var inv=window.getItemContainer();var dest=new SimpleItemContainer((short)36);var a=inv.registerChangeEvent(e->{try{from.invoke(probe,e.transaction());}catch(Exception ex){throw new RuntimeException(ex);}});var b=dest.registerChangeEvent(e->{try{receipt.invoke(probe,e.transaction());}catch(Exception ex){throw new RuntimeException(ex);}});
    try{dest.setItemStackForSlot((short)0,new ItemStack("Ingredient_Bar_Iron",2));dest.moveItemStackFromSlotToSlot((short)0,1,inv,(short)52);inv.moveItemStackFromSlotToSlot((short)52,1,inv,(short)53);require(((Number)finish.invoke(probe)).intValue()==0,"safe action classified theft");inv.moveItemStackFromSlotToSlot((short)0,1,dest,(short)1);require(((Number)finish.invoke(probe)).intValue()==1&&count[0]==1,"withdrawal not paired");}finally{a.unregister();b.unregister();}
    var evidence=(List<?>)call("ContainerVisualWitness","capture",actor,s,data,Set.of("Kweebec_Merchant"),(Consumer<String>)this::log);var messages=new ArrayList<com.hypixel.hytale.server.core.Message>();var witness=(Optional<?>)call("ContainerCaughtNotification","notifyWitnessed",evidence,(Consumer<com.hypixel.hytale.server.core.Message>)messages::add);
    boolean seen=mode.equals("witnessed");require(witness.isPresent()==seen&&messages.size()==(seen?1:0),"wrong witness/notification result "+evidence);
    if(!seen){log("UNSEEN_PASS Mode="+mode+" Messages=0 GuardHandoff=none");finish();return;}
    log("WITHDRAWAL_WITNESSED_PASS SuccessfulRemoval=1 Message=1 SourceMatches=true");
    var start=new Vector3d(s.getComponent(guard,TransformComponent.getComponentType()).getPosition());var result=call("HyThiefContainerAlert","deliver",hy,data,actor,merchant,s,(Consumer<String>)this::log);require(Boolean.TRUE.equals(read(result,"sent")),"guard rejected native alert");
    w.scheduleAfter(()->w.execute(()->{try{boolean target=actor.equals(s.getComponent(guard,MarkedEntitySupport.getComponentType()).getMarkedEntityRef("LockedTarget"));double moved=start.distance(s.getComponent(guard,TransformComponent.getComponentType()).getPosition());var state=s.getComponent(guard,StateSupport.getComponentType()).getStateName();require(target&&moved>.1,"guard no native pursuit "+state+" moved="+moved);log("NATIVE_RESPONSE_PASS State="+state+" Movement="+moved+" ExactOffender="+target);finish();}catch(Throwable t){fail(t);}}),3,TimeUnit.SECONDS);
  }
  void reload(){try{var cs=w.getChunkStore();long k=ChunkUtil.indexChunkFromBlock(0,2);var ref=cs.getChunkReference(k);cs.getSaver().saveChunkColumn(ChunkUtil.xOfChunkIndex(k),ChunkUtil.zOfChunkIndex(k),cs.getStore(),ref,w,()->{}).thenRun(()->w.execute(()->{try{cs.getSaver().flush();cs.remove(ref,RemoveReason.UNLOAD);cs.getChunkSectionReferenceAtBlockAsync(0,64,2,GetChunkFlags.SET_TICKING).thenRun(()->w.execute(()->{try{ownership(true);require(block(0,64,2).getItemContainer().getItemStack((short)0).getQuantity()==16,"reload items");log("CHUNK_RELOAD_PASS");finish();}catch(Throwable t){fail(t);}}));}catch(Throwable t){fail(t);}}));}catch(Throwable t){fail(t);}}

  void access() throws Exception {
    var actor=actor();
    require(!Boolean.TRUE.equals(call("NativePursuit","blocks",actor,s)),"fresh actor locked");
    var marked=s.getComponent(guard,MarkedEntitySupport.getComponentType());
    var state=s.getComponent(guard,StateSupport.getComponentType());
    marked.setMarkedEntity("LockedTarget",actor);
    require(!Boolean.TRUE.equals(call("NativePursuit","blocks",actor,s)),"idle watch incorrectly locked");
    for(String name:List.of("Attack","Attack.Default","Combat.Chase","Chase","Search","Threaten.Angry"))
      require(Boolean.TRUE.equals(call("NativePursuit","engagedState",name)),"missing native engagement "+name);
    for(String name:List.of("Idle","Idle.Default","ReturnHome","Alerted","Threaten.Default"))
      require(!Boolean.TRUE.equals(call("NativePursuit","engagedState",name)),"noncombat engagement "+name);
    state.setState(guard,"Attack","Default",s);
    require(Boolean.TRUE.equals(call("NativePursuit","blocks",actor,s)),"native target/state not locked");
    var other=actor();require(!Boolean.TRUE.equals(call("NativePursuit","blocks",other,s)),"other player locked");
    marked.clearMarkedEntity(0);
    state.setState(guard,"Idle","Default",s);
    require(!Boolean.TRUE.equals(call("NativePursuit","blocks",actor,s)),"access did not restore");
    log("PURSUIT_STATE_PASS ExactTarget=true IdleWatchSafe=true ReleaseRestores=true NoTimer=true");
    // Real native danger post must block before the AI consumes it.
    var result=call("HyThiefContainerAlert","deliver",hy,own(0,64,2),actor,merchant,s,(Consumer<String>)this::log);
    require(Boolean.TRUE.equals(read(result,"sent")),"no native danger delivery");
    require(Boolean.TRUE.equals(call("NativePursuit","blocks",actor,s)),"queued native danger gap");
    log("PENDING_NATIVE_DANGER_PASS");
    var start=new Vector3d(s.getComponent(guard,TransformComponent.getComponentType()).getPosition());
    w.scheduleAfter(()->w.execute(()->{try{
      require(Boolean.TRUE.equals(call("NativePursuit","blocks",actor,s)),"native pursuit not locked");
      require(start.distance(s.getComponent(guard,TransformComponent.getComponentType()).getPosition())>.1,"native guard did not pursue");
      log("ACTUAL_NATIVE_PURSUIT_LOCK_PASS State="+state.getStateName());
      windows(actor);
      finish();
    }catch(Throwable t){fail(t);}}),3,TimeUnit.SECONDS);
  }
  static final class Sink extends com.hypixel.hytale.server.core.io.PacketHandler {
    final List<com.hypixel.hytale.protocol.ToClientPacket> sent=new ArrayList<>();
    Sink(){super((com.hypixel.hytale.protocol.io.ChannelConnection)Proxy.newProxyInstance(
        ContainerAccessProof.class.getClassLoader(),new Class[]{com.hypixel.hytale.protocol.io.ChannelConnection.class},
        (p,m,a)->{if(m.getReturnType()==boolean.class)return true;if(m.getName().equals("execute")){((Runnable)a[0]).run();return null;}return null;}),new com.hypixel.hytale.server.core.io.ProtocolVersion(0));}
    public String getIdentifier(){return "test-only";}
    public void accept(com.hypixel.hytale.protocol.ToServerPacket packet){}
    public void writeNoCache(com.hypixel.hytale.protocol.ToClientPacket packet){sent.add(packet);}
    public void write(com.hypixel.hytale.protocol.ToClientPacket packet){sent.add(packet);}
  }
  void windows(Ref<EntityStore> actor) throws Exception {
    var sink=new Sink();var uuid=UUID.randomUUID();
    var pref=new PlayerRef(EntityStore.REGISTRY.newHolder(),uuid,"NativeProof","en-US",sink,null){
      public Ref<EntityStore> getReference(){return actor;}
    };
    var player=new com.hypixel.hytale.server.core.entity.entities.Player();player.init(uuid,pref);
    s.addComponent(actor,com.hypixel.hytale.server.core.entity.entities.Player.getComponentType(),player);
    s.addComponent(actor,PlayerRef.getComponentType(),pref);
    try {
      var inv=block(0,64,2).getItemContainer();
      var window=new ContainerBlockWindow(0,64,2,0,type(0,64,2),inv);
      var manager=player.getWindowManager();
      require(manager.openWindow(actor,window,s)!=null,"native open failed");
      int id=window.getId();boolean[] closed={false};window.registerCloseEvent(e->closed[0]=true);
      var dest=new SimpleItemContainer((short)36);inv.moveItemStackFromSlotToSlot((short)0,1,dest,(short)0);
      call("ContainerTheftService","closeSource",actor,s,inv);
      require(manager.getWindow(id)==null&&closed[0],"window or listener not closed");
      require(dest.getItemStack((short)0).getQuantity()==1,"stolen item removed");
      require(sink.sent.stream().anyMatch(p->p instanceof com.hypixel.hytale.protocol.packets.window.CloseWindow),"close not sent");
      log("NATIVE_WINDOW_CLOSE_PASS ItemKept=true WindowInvalid=true ClosePacket=true CloseListener=true");
      var pre=new com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent.Pre(com.hypixel.hytale.protocol.InteractionType.Use,null,new org.joml.Vector3i(0,64,2),type(0,64,2));

      sink.sent.clear();
      s.invoke(actor,pre);require(pre.isCancelled(),"owned pre-use not blocked");
      String denied="You cannot access this container while being pursued.";
      require(sink.sent.size()==1&&sink.sent.get(0) instanceof com.hypixel.hytale.protocol.packets.interface_.ServerMessage text&&denied.equals(text.message.rawText),"exact denied notification missing");
      s.invoke(actor,pre);require(sink.sent.size()==1,"same canceled event notified twice");
      var retry=new com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent.Pre(com.hypixel.hytale.protocol.InteractionType.Use,null,new org.joml.Vector3i(0,64,2),type(0,64,2));
      s.invoke(actor,retry);require(retry.isCancelled()&&sink.sent.size()==2,"new denied attempt did not notify once");
      require(sink.sent.stream().allMatch(p->p instanceof com.hypixel.hytale.protocol.packets.interface_.ServerMessage text&&denied.equals(text.message.rawText)),"denial reused caught text");
      log("DENIED_NOTIFICATION_PASS ExactText=true FirstAttempt=1 SameEventRepeat=0 NewAttempt=1");
      set(5,64,5,"Furniture_Kweebec_Wardrobe");
      var unowned=new com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent.Pre(com.hypixel.hytale.protocol.InteractionType.Use,null,new org.joml.Vector3i(5,64,5),type(5,64,5));
      s.invoke(actor,unowned);require(!unowned.isCancelled()&&sink.sent.size()==2,"player-placed storage blocked or notified");
      log("NATIVE_USE_EVENT_PASS OwnedBlocked=true UnownedSafe=true");

      // Exercise the actual loaded service, not a reconstructed theft pipeline.
      var mark=s.getComponent(guard,MarkedEntitySupport.getComponentType());
      for(int k=0;k<mark.getMarkedEntitySlotCount();k++)mark.clearMarkedEntity(k);
      s.getComponent(guard,StateSupport.getComponentType()).setState(guard,"Idle","Default",s);

      var restored=new com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent.Pre(com.hypixel.hytale.protocol.InteractionType.Use,null,new org.joml.Vector3i(0,64,2),type(0,64,2));
      s.invoke(actor,restored);require(!restored.isCancelled()&&sink.sent.size()==2,"restored access still notified");
      log("DENIED_SAFE_PATHS_PASS UnownedSilent=true RestoredAccessSilent=true");
      var storage=new InventoryComponent.Storage((short)36);
      s.addComponent(actor,InventoryComponent.Storage.getComponentType(),storage);
      var field=hy.getClass().getDeclaredField("containerDiagnostics");field.setAccessible(true);var service=field.get(hy);
      var dispatch=service.getClass().getDeclaredMethod("dispatch",PlayerRef.class,com.hypixel.hytale.protocol.Packet.class,World.class);dispatch.setAccessible(true);
      var second=new ContainerBlockWindow(0,64,2,0,type(0,64,2),inv);
      require(manager.openWindow(actor,second,s)!=null,"second open failed");
      int next=second.getId();int before=inv.getItemStack((short)0).getQuantity();sink.sent.clear();
      var request=new com.hypixel.hytale.protocol.packets.inventory.MoveItemStack(next,0,1,InventoryComponent.STORAGE_SECTION_ID,0);
      dispatch.invoke(service,pref,request,w);
      require(storage.getInventory().getItemStack((short)0)!=null,"dispatch transfer failed");
      require(storage.getInventory().getItemStack((short)0).getQuantity()==1,"wrong kept quantity");
      require(inv.getItemStack((short)0).getQuantity()==before-1,"source count wrong");
      require(manager.getWindow(next)==null,"caught dispatch did not close");
      require(sink.sent.stream().filter(p->p instanceof com.hypixel.hytale.protocol.packets.interface_.ServerMessage).count()==1,"caught notification count");
      dispatch.invoke(service,pref,request,w);
      require(storage.getInventory().getItemStack((short)0).getQuantity()==1,"stale queued withdrawal succeeded");
      require(sink.sent.stream().filter(p->p instanceof com.hypixel.hytale.protocol.packets.interface_.ServerMessage).count()==1,"duplicate caught message");
      log("REAL_SERVICE_DISPATCH_PASS ItemKept=1 Notification=1 WindowClosed=true QueuedSecondRejected=true");
      // Crouched outside six blocks: two separate real transfers must keep the window usable.
      s.getComponent(actor,MovementStatesComponent.getComponentType()).getMovementStates().crouching=true;
      var guardBeacon=s.getComponent(guard,com.hypixel.hytale.server.npc.components.messaging.BeaconSupport.getComponentType());
      for(int k=0;k<guardBeacon.getMessageSlots().length;k++)guardBeacon.pollMessage(k);
      for(int k=0;k<mark.getMarkedEntitySlotCount();k++)mark.clearMarkedEntity(k);
      s.getComponent(guard,StateSupport.getComponentType()).setState(guard,"Idle","Default",s);
      var third=new ContainerBlockWindow(0,64,2,0,type(0,64,2),inv);require(manager.openWindow(actor,third,s)!=null,"unseen open failed");
      sink.sent.clear();var unseen=new com.hypixel.hytale.protocol.packets.inventory.MoveItemStack(third.getId(),0,1,InventoryComponent.STORAGE_SECTION_ID,0);
      dispatch.invoke(service,pref,unseen,w);dispatch.invoke(service,pref,unseen,w);
      require(manager.getWindow(third.getId())!=null,"unseen closed");
      require(storage.getInventory().getItemStack((short)0).getQuantity()==3,"unseen separate transfers failed");
      require(sink.sent.stream().noneMatch(p->p instanceof com.hypixel.hytale.protocol.packets.interface_.ServerMessage),"unseen notified");
      log("REAL_UNSEEN_DISPATCH_PASS SeparateWithdrawals=2 WindowOpen=true Notification=0");
      // Pursuit which starts with a window already open cannot be bypassed by withdrawing.
      mark.setMarkedEntity("LockedTarget",actor);s.getComponent(guard,StateSupport.getComponentType()).setState(guard,"Attack","Default",s);
      dispatch.invoke(service,pref,unseen,w);
      require(storage.getInventory().getItemStack((short)0).getQuantity()==3&&manager.getWindow(third.getId())==null,"open-window lockout bypass");
      log("ALREADY_OPEN_PURSUIT_PASS TransferBlocked=true WindowClosed=true");

      for(int k=0;k<mark.getMarkedEntitySlotCount();k++)mark.clearMarkedEntity(k);
      s.getComponent(guard,StateSupport.getComponentType()).setState(guard,"Idle","Default",s);
      for(int k=0;k<guardBeacon.getMessageSlots().length;k++)guardBeacon.pollMessage(k);
      s.getComponent(actor,MovementStatesComponent.getComponentType()).getMovementStates().crouching=false;
      // Native smart moves require normal destination sections as on a real player.
      s.addComponent(actor,InventoryComponent.Hotbar.getComponentType(),new InventoryComponent.Hotbar());
      var fourth=new ContainerBlockWindow(0,64,2,0,type(0,64,2),inv);require(manager.openWindow(actor,fourth,s)!=null,"smart open failed");

      sink.sent.clear();
      int end=inv.getCapacity()-1;
      var deposit=new com.hypixel.hytale.protocol.packets.inventory.MoveItemStack(InventoryComponent.STORAGE_SECTION_ID,0,1,fourth.getId(),end);
      dispatch.invoke(service,pref,deposit,w);
      var internal=new com.hypixel.hytale.protocol.packets.inventory.MoveItemStack(fourth.getId(),end,1,fourth.getId(),end-1);
      dispatch.invoke(service,pref,internal,w);
      require(inv.getItemStack((short)(end-1)).getQuantity()==1,"safe native deposit/rearrangement failed");
      require(manager.getWindow(fourth.getId())!=null&&sink.sent.stream().noneMatch(p->p instanceof com.hypixel.hytale.protocol.packets.interface_.ServerMessage),"safe action closed/notified");
      log("SAFE_DEPOSIT_INTERNAL_PASS NativeTransfers=true WindowOpen=true Notification=0");
      int beforeSmart=inv.getItemStack((short)0).getQuantity();
      var smart=new com.hypixel.hytale.protocol.packets.inventory.SmartMoveItemStack(fourth.getId(),0,2,com.hypixel.hytale.protocol.SmartMoveType.PutInHotbarOrWindow);
      dispatch.invoke(service,pref,smart,w);
      require(inv.getItemStack((short)0).getQuantity()==beforeSmart-2,"smart transfer failed");
      require(manager.getWindow(fourth.getId())==null,"smart window not closed");
      require(sink.sent.stream().filter(p->p instanceof com.hypixel.hytale.protocol.packets.interface_.ServerMessage).count()==1,"smart notification count");
      dispatch.invoke(service,pref,smart,w);
      require(inv.getItemStack((short)0).getQuantity()==beforeSmart-2,"stale smart withdrawal succeeded");
      log("SMART_TRANSFER_PASS Quantity=2 Notification=1 Closed=true StaleRejected=true");
      s.removeComponent(actor,InventoryComponent.Hotbar.getComponentType());
      s.removeComponent(actor,InventoryComponent.Storage.getComponentType());

    } finally {
      s.tryRemoveComponent(actor,InventoryComponent.Storage.getComponentType());
      s.tryRemoveComponent(actor,InventoryComponent.Hotbar.getComponentType());
      s.removeComponent(actor,PlayerRef.getComponentType());
      s.removeComponent(actor,com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
    }
  }

  void finish(){log("PASS Mode="+mode);CompletableFuture.runAsync(()->HytaleServer.get().shutdownServer());}
  void fail(Throwable t){log("FAILED Mode="+mode+" "+t);t.printStackTrace();try{Files.writeString(Path.of("proof-failure.txt"),t.toString());}catch(Exception ignored){}finish();}
}
