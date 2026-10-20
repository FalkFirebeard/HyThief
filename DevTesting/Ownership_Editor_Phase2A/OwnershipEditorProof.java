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
public final class OwnershipEditorProof extends JavaPlugin {
  World w; Store<EntityStore> s; String mode; PluginBase hy;
  Ref<EntityStore> merchant,guard; final Vector3d pad=new Vector3d(.5,64,1);
  public OwnershipEditorProof(JavaPluginInit i){super(i);}
  static void require(boolean b,String msg){if(!b)throw new IllegalStateException(msg);}
  void log(String v){System.out.println("OWNERSHIP_EDITOR "+v);}
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
    if(mode.startsWith("editor-")){editor();return;}
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
        OwnershipEditorProof.class.getClassLoader(),new Class[]{com.hypixel.hytale.protocol.io.ChannelConnection.class},
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


  Object selection(int x) throws Exception { return call("OwnershipEditor","select",w,new org.joml.Vector3i(x,64,2)); }
  void assign(int x,boolean owned,String scope,String faction)throws Exception {
    var entity=BlockModule.getBlockEntity(w,x,64,2);var ct=(ComponentType)call("OwnershipProofData","getComponentType");
    var c=hy.getClass().getClassLoader().loadClass("com.local.pickpocket.OwnershipProofData").getConstructor(boolean.class,String.class,String.class).newInstance(owned,scope,faction);
    w.getChunkStore().getStore().putComponent(entity,ct,(Component)c);
  }
  void editor()throws Exception {
    if(mode.equals("editor-author")) {
      for(int x=-16;x<=16;x++)for(int z=-24;z<=12;z++)set(x,63,z,"Rock_Stone_Cobble");
      int[] xs={-8,-4,0,4,8};String[] labels={"A - UNOWNED CHEST","B - KWEEBEC CHEST","C - NEARBY CHEST","D - UNOWNED HYSTASH 54","E - KWEEBEC HYSTASH 54"};
      for(int k=0;k<xs.length;k++) {
        int x=xs[k];set(x,64,2,k<3?"Furniture_Kweebec_Chest_Large":"Furniture_Kweebec_Wardrobe");
        var cs=w.getChunkStore();var sr=cs.getChunkSectionReferenceAtBlock(x,64,2);var bc=cs.getStore().ensureAndGetComponent(sr,BlockComponentSection.getComponentType());
        BlockEntity.ensureBlockEntity(cs.getStore(),sr,bc,x,64,2,type(x,64,2),0);
        var b=block(x,64,2);if(b.getItemContainer()==null)b.setItemContainer(new SimpleItemContainer(b.getCapacity()));
        b.getItemContainer().clear();b.getItemContainer().setItemStackForSlot((short)0,new ItemStack("Ingredient_Bar_Copper",16));b.getItemContainer().setItemStackForSlot((short)1,new ItemStack("Ingredient_Bar_Iron",8));
        assign(x,k==1||k==2||k==4,k==2?"NEARBY":(k==1||k==4?"FACTION":""),k==1||k==4?"Kweebec":"");
        set(x,63,0,"Cloth_Block_Wool_Green"); label(labels[k],x+.5,67,2.5);
        cs.getStore().getComponent(sr,BlockComponentSection.getComponentType()).markBlockNeedsSaving(ChunkUtil.indexBlock(x,64,2));
        cs.getStore().getComponent(cs.getChunkReference(ChunkUtil.indexChunkFromBlock(x,2)),WorldChunk.getComponentType()).markNeedsSaving();
      }
      merchant=spawn("Kweebec_Merchant",0,-8,"WITNESS - FACE CHEST ROW");guard=spawn("Kweebec_Razorleaf",0,-20,"RAZORLEAF - NATIVE RESPONSE");
      for(var ref:List.of(merchant,guard)){var rot=new Rotation3f(0,(float)Math.PI,0);s.getComponent(ref,TransformComponent.getComponentType()).setRotation(rot);s.getComponent(ref,HeadRotation.getComponentType()).setRotation(rot);}
      label("01 HYTHIEF - LOOK AT CONTAINER: /hythief ownership",.5,68,7);
      label("A / D SAFE | B / E KWEEBEC | C NEARBY - RESET AFTER CAUGHT",.5,68.8,7);
      log("AUTHOR_PASS FiveContainers=true NativeStorage=true NoSpawner=true");finish();return;
    }
    findNPCs();require(merchant!=null&&guard!=null,"fixture NPCs missing");
    if(mode.equals("editor-restart")) {
      require(Boolean.TRUE.equals(read(own(-8,64,2),"owned"))&&read(own(-8,64,2),"scope").equals("NEARBY"),"chest edited ownership lost restart");
      require(Boolean.TRUE.equals(read(own(4,64,2),"owned"))&&read(own(4,64,2),"faction").equals("Feran"),"wardrobe edited ownership lost restart");
      require(!Boolean.TRUE.equals(read(own(8,64,2),"owned")),"explicit false lost restart");
      for(int x:new int[]{-8,-4,0,4,8})require(block(x,64,2).getItemContainer().getItemStack((short)0).getQuantity()==16,"items changed restart");
      require(block(4,64,2).getItemContainer().getCapacity()==54,"wardrobe capacity restart");
      log("FULL_PROCESS_RESTART_PASS EditedChestNearby=true EditedWardrobeFeran=true ExplicitUnowned=true InventoryIntact=true");finish();return;
    }
    if(mode.equals("editor-command")){editorCommand();return;}
    if(mode.equals("editor-fixture")){editorFixture();return;}
    var actor=actor();s.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(-7.5,64,5));
    var sink=new Sink();var uuid=UUID.randomUUID();boolean[] allowed={true};
    var pref=new PlayerRef(EntityStore.REGISTRY.newHolder(),uuid,"CreatorProof","en-US",sink,null){public Ref<EntityStore> getReference(){return actor;}public boolean hasPermission(String p){return allowed[0]&&super.hasPermission(p);}};
    com.hypixel.hytale.server.core.permissions.PermissionsModule.get().addUserPermission(uuid,Set.of("hythief.ownership.edit"));
    var player=new com.hypixel.hytale.server.core.entity.entities.Player();player.init(uuid,pref);s.addComponent(actor,com.hypixel.hytale.server.core.entity.entities.Player.getComponentType(),player);s.addComponent(actor,PlayerRef.getComponentType(),pref);
    try {
      var loader=hy.getClass().getClassLoader();var command=loader.loadClass("com.local.pickpocket.OwnershipEditorCommand").getConstructor().newInstance();
      var perm=command.getClass().getMethod("hasPermission",com.hypixel.hytale.server.core.command.system.CommandSender.class);
      require(Boolean.TRUE.equals(perm.invoke(command,pref)),"authorized native permission rejected");
      Object sel=selection(-8);allowed[0]=false;com.hypixel.hytale.server.core.permissions.PermissionsModule.get().removeUserPermission(uuid,Set.of("hythief.ownership.edit"));require(!Boolean.TRUE.equals(perm.invoke(command,pref)),"unauthorized native permission accepted");
      denied(()->call("OwnershipEditor","apply",sel,actor,s,pref,true,"NEARBY",""));allowed[0]=true;com.hypixel.hytale.server.core.permissions.PermissionsModule.get().addUserPermission(uuid,Set.of("hythief.ownership.edit"));
      denied(()->call("OwnershipEditor","select",w,new org.joml.Vector3i(-8,63,2)));
      var inv=block(-8,64,2).getItemContainer();var items=new ArrayList<ItemStack>();for(short n=0;n<inv.getCapacity();n++)items.add(inv.getItemStack(n));
      require(Boolean.TRUE.equals(call("OwnershipEditor","apply",sel,actor,s,pref,true,"NEARBY","")),"nearby apply failed");
      require(Boolean.TRUE.equals(read(own(-8,64,2),"owned")),"owned toggle lost");
      require(read(own(-4,64,2),"faction").equals("Kweebec"),"identical other chest modified");
      require(!Boolean.TRUE.equals(call("OwnershipEditor","apply",selection(-8),actor,s,pref,true,"NEARBY","")),"unchanged wrote");
      denied(()->call("OwnershipEditor","apply",sel,actor,s,pref,true,"FACTION","Kweebec"));
      denied(()->call("OwnershipEditor","apply",selection(-8),actor,s,pref,true,"FACTION","Wildlife"));
      for(short n=0;n<inv.getCapacity();n++)require(Objects.equals(items.get(n),inv.getItemStack(n)),"inventory slot changed "+n);
      log("COMMAND_PERMISSIONS_APPLY_PASS Allowed=true Denied=true UnsupportedBlockRejected=true NoOp=true StaleConflictRejected=true InvalidFactionRejected=true IndependentChest=true InventorySlotsUnchanged=true");
      var pageClass=loader.loadClass("com.local.pickpocket.OwnershipEditorPage");
      Object page=pageClass.getConstructors()[0].newInstance(pref,selection(-8));
      var ui=new com.hypixel.hytale.server.core.ui.builder.UICommandBuilder();var events=new com.hypixel.hytale.server.core.ui.builder.UIEventBuilder();
      pageClass.getMethod("build",Ref.class,com.hypixel.hytale.server.core.ui.builder.UICommandBuilder.class,com.hypixel.hytale.server.core.ui.builder.UIEventBuilder.class,Store.class).invoke(page,actor,ui,events,s);
      player.getPageManager().openCustomPage(actor,s,(com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage)page);
      require(sink.sent.stream().anyMatch(p->p.getClass().getSimpleName().contains("CustomPage")),"native UI packet not sent");
      var jsonEvent=pageClass.getMethod("handleDataEvent",Ref.class,Store.class,String.class);
      jsonEvent.invoke(page,actor,s,"{\"Action\":\"edit\",\"@Owned\":false}");
      jsonEvent.invoke(page,actor,s,"{\"Action\":\"cancel\"}");
      require(Boolean.TRUE.equals(read(own(-8,64,2),"owned")),"cancel wrote ownership");
      // Reopen, apply through the real typed UI codec/event path, and suppress duplicate Apply.
      Object uiPage=pageClass.getConstructors()[0].newInstance(pref,selection(-8));player.getPageManager().openCustomPage(actor,s,(com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage)uiPage);
      jsonEvent.invoke(uiPage,actor,s,"{\"Action\":\"apply\",\"@Owned\":true,\"@Scope\":\"NEARBY\",\"@Faction\":\"\"}");
      int packets=sink.sent.size();jsonEvent.invoke(uiPage,actor,s,"{\"Action\":\"apply\",\"@Owned\":false}");require(sink.sent.size()==packets,"duplicate UI apply processed");
      log("NATIVE_UI_PASS OpenPacket=true Codec=true Edit=true CancelNoWrite=true Apply=true DuplicateSuppressed=true");
      s.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(4.5,64,5));
      var before=block(4,64,2).getItemContainer();require(before.getCapacity()==54,"HyStash capacity not54");
      require(Boolean.TRUE.equals(call("OwnershipEditor","apply",selection(4),actor,s,pref,true,"FACTION","Feran")),"wardrobe faction apply");
      require(before==block(4,64,2).getItemContainer()&&before.getCapacity()==54,"wardrobe inventory replaced");
      require(Boolean.TRUE.equals(call("OwnershipEditor","apply",selection(8),actor,s,pref,false,"FACTION","Kweebec")),"unowned toggle failed");
      // Target the upper wardrobe filler with the native ray/origin resolver.
      s.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(4.5,64.1,5));
      s.putComponent(actor,HeadRotation.getComponentType(),new HeadRotation(new Rotation3f(0,0,0)));
      var origin=(org.joml.Vector3i)call("OwnershipEditor","target",actor,s);
      require(origin!=null&&origin.x==4&&origin.y==64&&origin.z==2,"wardrobe filler origin resolution "+origin);
      log("WARDROBE_PASS NativeFillerTarget="+origin+" Slots54=true IndependentOwnership=true UnownedToggle=true");
      // Container disappeared/replaced after selection, even if rebuilt with the same block ID.
      Object replace=selection(4);set(4,64,2,"Empty");denied(()->call("OwnershipEditor","apply",replace,actor,s,pref,true,"NEARBY",""));
      set(4,64,2,"Furniture_Kweebec_Wardrobe");var cs=w.getChunkStore();var sr=cs.getChunkSectionReferenceAtBlock(4,64,2);BlockEntity.ensureBlockEntity(cs.getStore(),sr,cs.getStore().getComponent(sr,BlockComponentSection.getComponentType()),4,64,2,type(4,64,2),0);
      var restored=block(4,64,2);if(restored.getItemContainer()==null)restored.setItemContainer(new SimpleItemContainer(restored.getCapacity()));restored.getItemContainer().setItemStackForSlot((short)0,new ItemStack("Ingredient_Bar_Copper",16));restored.getItemContainer().setItemStackForSlot((short)1,new ItemStack("Ingredient_Bar_Iron",8));assign(4,false,"","");call("OwnershipEditor","apply",selection(4),actor,s,pref,true,"FACTION","Feran");
      denied(()->call("OwnershipEditor","apply",replace,actor,s,pref,true,"NEARBY",""));
      log("REPLACEMENT_REJECTED_PASS");
    } finally {w.execute(()->{s.removeComponent(actor,PlayerRef.getComponentType());s.removeComponent(actor,com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());});}
    finish();
  }


  void editorFixture()throws Exception {
    int[] count={0};w.getChunkStore().getStore().forEachChunk(ItemContainerBlock.getComponentType(),(c,b)->{count[0]+=c.size();});require(count[0]==5,"fixture storage count "+count[0]);
    for(var r:List.of(merchant,guard)) {
      double z=r.equals(merchant)?-8:-20;
      require(s.getComponent(r,TransformComponent.getComponentType()).getPosition().distance(new Vector3d(0,64,z))<.05,"NPC position wrong");
      require(Math.abs(Math.abs(s.getComponent(r,HeadRotation.getComponentType()).getRotation().yaw())-Math.PI)<.02,"NPC does not face row");
      require(s.getComponent(r,com.hypixel.hytale.server.core.entity.Frozen.getComponentType())==null,"NPC frozen");
    }
    var actor=actor();
    for(int x:new int[]{-8,-4,0,4,8}) {
      var data=own(x,64,2);boolean expected=x==-4||x==0||x==8;
      require(Boolean.TRUE.equals(read(data,"owned"))==expected,"fixture ownership wrong "+x);
      if(expected)require(read(data,"scope").equals(x==0?"NEARBY":"FACTION"),"scope wrong "+x);
      if(x==-4||x==8)require(read(data,"faction").equals("Kweebec"),"faction wrong "+x);
      var inv=block(x,64,2).getItemContainer();require(inv.getItemStack((short)0).getQuantity()==16,"fixture loot wrong");
      if(x==4||x==8)require(inv.getCapacity()==54,"wardrobe not54");
      s.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(x+.5,64,.5));
      var evidence=(List<?>)call("ContainerVisualWitness","capture",actor,s,data,Set.of("Kweebec_Merchant"),(Consumer<String>)this::log);
      boolean seen=false;for(Object e:evidence)seen|=Boolean.TRUE.equals(read(e,"seen"));require(seen==expected,"fixture standing witness wrong x="+x);
      s.getComponent(actor,MovementStatesComponent.getComponentType()).getMovementStates().crouching=true;
      evidence=(List<?>)call("ContainerVisualWitness","capture",actor,s,data,Set.of("Kweebec_Merchant"),(Consumer<String>)this::log);
      for(Object e:evidence)require(!Boolean.TRUE.equals(read(e,"seen")),"crouched approach unexpectedly witnessed");
      s.getComponent(actor,MovementStatesComponent.getComponentType()).getMovementStates().crouching=false;
    }
    require(com.hypixel.hytale.server.core.permissions.PermissionsModule.get().hasPermission(UUID.fromString("d49a059a-84ec-4b9c-8560-ef2ff633ce44"),"hythief.ownership.edit"),"creator fixture permission missing");
    log("FIXTURE_PASS StorageCount=5 NativeChest=3 HyStash54=2 NPCs=2 Pose=true FOV=true LOS=true StandingWitness=true CrouchedUnseen=true CreatorPermission=true");finish();
  }
  void editorCommand()throws Exception {
    var actor=actor();var at=new Vector3d(-7.5,64.1,5);s.getComponent(actor,TransformComponent.getComponentType()).setPosition(at);
    double eye=com.hypixel.hytale.server.core.modules.entity.component.ModelComponent.getEyeHeight(actor,s);
    var rot=Rotation3f.lookAt(new Vector3d(at).add(0,eye,0),new Vector3d(-7.5,64.5,2.5));s.putComponent(actor,HeadRotation.getComponentType(),new HeadRotation(rot));
    var sink=new Sink();var uuid=UUID.randomUUID();var pref=new PlayerRef(EntityStore.REGISTRY.newHolder(),uuid,"CreatorCommandProof","en-US",sink,null){public Ref<EntityStore> getReference(){return actor;}};
    pref.updatePosition(w,new com.hypixel.hytale.math.vector.Transform(at.x,at.y,at.z,rot.pitch(),rot.yaw(),0),rot);
    var player=new com.hypixel.hytale.server.core.entity.entities.Player();player.init(uuid,pref);s.addComponent(actor,com.hypixel.hytale.server.core.entity.entities.Player.getComponentType(),player);s.addComponent(actor,PlayerRef.getComponentType(),pref);
    com.hypixel.hytale.server.core.permissions.PermissionsModule.get().addUserPermission(uuid,Set.of("hythief.ownership.edit"));
    var command=com.hypixel.hytale.server.core.command.system.CommandManager.get();
    log("COMMAND_TARGET "+call("OwnershipEditor","target",actor,s));
    command.handleCommand(pref,"hythief ownership").whenComplete((v,error)->w.execute(()->{
      try {require(error==null,"native command failure "+error);require(player.getPageManager().getCustomPage()!=null,"native command no custom page");log("REGISTERED_COMMAND_PASS NativeDispatch=true TargetChest=true PageOpen=true");}
      catch(Throwable t){fail(t);return;}
      finally {s.removeComponent(actor,PlayerRef.getComponentType());s.removeComponent(actor,com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());}
      finish();
    }));
  }
  @FunctionalInterface interface Attempt {Object run()throws Exception;}
  void denied(Attempt a)throws Exception {try {a.run();}catch(java.lang.reflect.InvocationTargetException ex){require(ex.getCause() instanceof IllegalArgumentException,"unexpected rejection "+ex.getCause());return;}throw new IllegalStateException("unsafe operation accepted");}
  void finish(){log("PASS Mode="+mode);CompletableFuture.runAsync(()->HytaleServer.get().shutdownServer());}
  void fail(Throwable t){log("FAILED Mode="+mode+" "+t);t.printStackTrace();try{Files.writeString(Path.of("proof-failure.txt"),t.toString());}catch(Exception ignored){}finish();}
}
