package com.local.pickpocket;
import com.hypixel.hytale.server.core.plugin.*;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.util.PrefabUtil;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.component.*;
import org.joml.Vector3i;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import com.google.gson.*;

/** Disposable native builder/verifier. Never installed globally or in the pristine playable save. */
public final class ContainerWorldBuilder extends JavaPlugin {
 public ContainerWorldBuilder(JavaPluginInit init){super(init);}
 static final int[][] CHESTS={{0,64,16},{0,64,4},{-3,64,16},{0,64,19},{0,64,29},{0,64,33}};
 private org.joml.Vector3d guardStart;
 static final String OWNED="Furniture_Kweebec_Chest_Large";
 static final String HINT="server.interactionHints.openDoor";
 static final String[] ITEMS={"Ingredient_Bar_Copper","Ingredient_Bar_Iron","Ingredient_Fibre"};
 static final int[] AMOUNTS={16,8,32};
 protected void start(){Universe.get().getUniverseReady().thenRun(()->{
  try{
   World w=Universe.get().getDefaultWorld();
   var buffer=PrefabBufferUtil.getCached(Path.of("prefabs/ContainerTheft.prefab.json"));
   var loads=new ArrayList<CompletableFuture<?>>();
   for(int x=-1;x<=0;x++)for(int z=-1;z<=1;z++)loads.add(w.getChunkAsync(com.hypixel.hytale.math.util.ChunkUtil.indexChunk(x,z)));
   CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).thenCompose(v->PrefabUtil.loadPasteRegionAsync(buffer,w,new Vector3i(),PrefabRotation.ROTATION_0,PrefabUtil.Flags.NONE)).thenAccept(region->w.execute(()->{
    try{
     if(!Boolean.getBoolean("containerworld.reload")){
      PrefabUtil.paste(buffer,w,new Vector3i(),Rotation.None,new Random(106081),PrefabUtil.Flags.FORCE,0,region,PrefabUtil.NOOP_BLOCK_ENTITY_CONSUMER,PrefabUtil.NOOP_ENTITY_CONSUMER,w.getEntityStore().getStore());
     }
     w.getEntityStore().getStore().forEachChunk(NPCEntity.getComponentType(),(chunk,b)->{for(int i=0;i<chunk.size();i++){var ref=chunk.getReferenceTo(i);var es=w.getEntityStore().getStore();if(es.getComponent(ref,NPCEntity.getComponentType()).getRoleName().equals("Kweebec_Razorleaf"))guardStart=new org.joml.Vector3d(es.getComponent(ref,TransformComponent.getComponentType()).getPosition());}});
     w.scheduleAfter(()->w.execute(()->{
      try{
       if(!Boolean.getBoolean("containerworld.reload"))for(int[] p:CHESTS)fill(container(w,p));
       if(Boolean.getBoolean("containerworld.layout"))applyLayout(w);
       if(Boolean.getBoolean("containerworld.ownershipseed")) {
        var p=CHESTS[0];container(w,p);
        var ref=BlockModule.getBlockEntity(w,p[0],p[1],p[2]);
        if(w.getChunkStore().getStore().getComponent(ref,OwnershipProofData.getComponentType())!=null)throw new IllegalStateException("Refusing ownership reseed");
        w.getChunkStore().getStore().addComponent(ref,OwnershipProofData.getComponentType(),new OwnershipProofData(true,"NEARBY"));
        var section=w.getChunkStore().getChunkSectionReferenceAtBlock(p[0],p[1],p[2]);
        var components=w.getChunkStore().getStore().getComponent(section,BlockComponentSection.getComponentType());
        components.markBlockNeedsSaving(com.hypixel.hytale.math.util.ChunkUtil.indexBlock(p[0],p[1],p[2]));components.markNeedsSaving();
        w.getEntityStore().getStore().forEachChunk(Nameplate.getComponentType(),(chunk,b)->{for(int i=0;i<chunk.size();i++){
         var label=chunk.getComponent(i,Nameplate.getComponentType());if(label.getText().startsWith("3 OWNED - 5 BLOCKS"))label.setText("3 PERSISTENT OWNED - NEARBY / 5 BLOCKS");
        }});
        System.out.println("PERSISTENT_AUTHOR_PASS Owned=true Scope=NEARBY NativeChest=true");
       }
       if(Boolean.getBoolean("containerworld.ownershiprestartonly")) {
        var c=container(w,CHESTS[0]);
        var source=PersistentContainerOwnership.resolve(w,new com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow(0,64,16,0,w.getBlockType(0,64,16),c),c);
        if(source==null || !source.ownedNearby())throw new IllegalStateException("Ownership lost after pipeline/save/restart");
        if(BlockModule.getComponent(OwnershipProofData.getComponentType(),w,-3,64,16)!=null)throw new IllegalStateException("Control became owned");
        Files.writeString(Path.of("reload-verification.json"),"{\"persistentRestartAfterPipeline\":true}");
        System.out.println("PERSISTENT_RESTART_AFTER_PIPELINE_PASS Owned=true Scope=NEARBY NoReattach=true");
        CompletableFuture.runAsync(()->HytaleServer.get().shutdownServer());return;
       }
       inspect(w,guardStart);
      }catch(Throwable t){fail(t);}
     }),Boolean.getBoolean("containerworld.stationaryguard")?60:6,TimeUnit.SECONDS);
    }catch(Throwable t){fail(t);}
   })).exceptionally(t->{fail(t);return null;});
  }catch(Throwable t){fail(t);}
 });}
 static com.hypixel.hytale.server.core.inventory.container.ItemContainer container(World w,int[] p){
  var cs=w.getChunkStore();var section=cs.getChunkSectionReferenceAtBlock(p[0],p[1],p[2]);
  var bc=cs.getStore().getComponent(section,BlockComponentSection.getComponentType());
  // Native offline authoring: activate a saved holder just as opening a chest does.
  BlockEntity.ensureBlockEntity(cs.getStore(),section,bc,p[0],p[1],p[2],w.getBlockType(p[0],p[1],p[2]),0);
  var c=BlockModule.getComponent(ItemContainerBlock.getComponentType(),w,p[0],p[1],p[2]);
  if(c!=null && c.getItemContainer()==null && (!Boolean.getBoolean("containerworld.reload")||Boolean.getBoolean("containerworld.layout")))c.setItemContainer(new SimpleItemContainer(c.getCapacity()));
  if(c==null||c.getItemContainer()==null)throw new IllegalStateException("Missing native chest "+Arrays.toString(p)+" block="+w.getBlockType(p[0],p[1],p[2]).getId()+" blockBelow="+w.getBlockType(p[0],p[1]-1,p[2]).getId());
  return c.getItemContainer();
 }
 static void fill(com.hypixel.hytale.server.core.inventory.container.ItemContainer c){
  c.clear();for(short s=0;s<ITEMS.length;s++)c.setItemStackForSlot(s,new ItemStack(ITEMS[s],AMOUNTS[s]));
 }
 static void applyLayout(World w){
  // Only the copied diagnostic save is authored. All chest definitions stay native.
  for(int z:new int[]{14,20})w.setBlock(0,64,z,"Empty");
  for(int z:new int[]{13,19})w.setBlock(0,63,z,"Rock_Stone_Brick");
  for(int i:new int[]{0,1,3,4,5}){w.setBlock(CHESTS[i][0],64,CHESTS[i][2],OWNED);fill(container(w,CHESTS[i]));}
  for(int z:new int[]{15,18,28,32})w.setBlock(0,63,z,"Rock_Stone_Cobble");
  var store=w.getEntityStore().getStore();var labels=new ArrayList<Ref<EntityStore>>();
  store.forEachChunk(Nameplate.getComponentType(),(chunk,b)->{for(int i=0;i<chunk.size();i++)labels.add(chunk.getReferenceTo(i));});
  Ref<EntityStore> template=null;
  for(var ref:labels){var label=store.getComponent(ref,Nameplate.getComponentType());String text=label.getText();
   if(text.startsWith("3 OWNED - MID")){template=ref;label.setText("3 OWNED - 5 BLOCKS / caught in both stances");store.getComponent(ref,TransformComponent.getComponentType()).setPosition(new org.joml.Vector3d(-1.5,66.4,16.5));}
   else if(text.startsWith("4 OWNED - CLOSE")||text.startsWith("MID PAD:")||text.startsWith("CLOSE PAD:"))store.removeEntity(ref,RemoveReason.REMOVE);
   else if(text.contains("CHECKPOINT 3"))label.setText("SIGHT TUNING - container 20 standing / 6 crouched; normal NPC sight unchanged");
  }
  if(template==null)throw new IllegalStateException("Missing prior MID label");
  addLabel(store,template,"4 OWNED - 8 BLOCKS / standing caught, crouched unseen",-1.5,66.4,19.5);
  addLabel(store,template,"5 OWNED - 18 BLOCKS / standing caught, crouched unseen",-1.5,66.4,29.5);
  addLabel(store,template,"6 OWNED - 22 BLOCKS / unseen in both stances",-1.5,66.4,33.5);
  addLabel(store,template,"5 BLOCKS - CROUCH SHOULD SEE / stand on this pad",.5,66.2,15.5);
  addLabel(store,template,"8 BLOCKS - CROUCH SHOULD NOT SEE / stand on this pad",.5,66.2,18.5);
  addLabel(store,template,"18 BLOCKS - STANDING SHOULD SEE / stand on this pad",.5,66.2,28.5);
  addLabel(store,template,"22 BLOCKS - STANDING SHOULD NOT SEE / stand on this pad",.5,66.2,32.5);
  // Stage placement only: same guard and native AI, in the same shielded alcove.
  store.forEachChunk(NPCEntity.getComponentType(),(chunk,b)->{for(int i=0;i<chunk.size();i++){
   var ref=chunk.getReferenceTo(i);if(store.getComponent(ref,NPCEntity.getComponentType()).getRoleName().equals("Kweebec_Razorleaf"))store.getComponent(ref,TransformComponent.getComponentType()).teleportPosition(new org.joml.Vector3d(8.999,64,16.5));
  }});
 }

 static void addLabel(Store<EntityStore> store,Ref<EntityStore> source,String text,double x,double y,double z){
  var holder=EntityStore.REGISTRY.newHolder();var t=store.getComponent(source,TransformComponent.getComponentType()).clone();t.setPosition(new org.joml.Vector3d(x,y,z));
  holder.addComponent(TransformComponent.getComponentType(),t);holder.addComponent(Nameplate.getComponentType(),new Nameplate(text));
  holder.addComponent(ModelComponent.getComponentType(),(ModelComponent)store.getComponent(source,ModelComponent.getComponentType()).clone());
  holder.addComponent(Frozen.getComponentType(),Frozen.get());
  holder.addComponent(com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType(),com.hypixel.hytale.server.core.entity.UUIDComponent.randomUUID());
  store.addEntity(holder,AddReason.SPAWN);
 }

 static void oneRun(World w,OwnedContainerRegistry registry){
  var player=new SimpleItemContainer((short)36);int[] order={2,1,0};int ownedReports=0;
  for(int i:order){
   var chest=container(w,CHESTS[i]);var owned=registry.findAutomatic(w,chest,null);
   if(i==2){if(owned!=null)throw new IllegalStateException("Control owned in one-run test");chest.moveItemStackFromSlotToSlot((short)0,4,player,(short)0);}
   else{
    if(owned==null)throw new IllegalStateException("Owned chest missing in one-run test");
    var results=new ArrayList<ContainerRemovalProbe.Removal>();var probe=new ContainerRemovalProbe(chest,results::add);
    var a=chest.registerChangeEvent(e->probe.source(e.transaction()));var b=player.registerChangeEvent(e->probe.receipt(e.transaction()));
    try{chest.moveItemStackFromSlotToSlot((short)0,4,player,(short)0);probe.finish();if(results.size()!=1||results.getFirst().amount()!=4)throw new IllegalStateException("One-run attribution mismatch");ownedReports+=results.size();}finally{a.unregister();b.unregister();}
   }
  }
  if(player.getItemStack((short)0).getQuantity()!=12||ownedReports!=2)throw new IllegalStateException("One-run sequence failed");
  // Restore only after the entire sequence; there was no reset between its three transfers.
  for(var p:CHESTS)fill(container(w,p));
  System.out.println("ONE_RUN_PASS Order=CONTROL,REAR,FRONT ControlReports=0 OwnedReports=2 Received=12");
 }
 static void nativeStates(World w,OwnedContainerRegistry registry){
  var chunks=w.getChunkStore();
  for(int i:new int[]{0,1,3,4,5}){
   var p=CHESTS[i];var original=registry.findAutomatic(w,container(w,p),null);
   var base=w.getBlockType(p[0],p[1],p[2]);var section=chunks.getChunkSectionReferenceAtBlock(p[0],p[1],p[2]);
   for(String state:new String[]{"OpenWindow","CloseWindow"}){
    com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations.setBlockInteractionState(chunks,section,p[0],p[1],p[2],base,state,false);
    if(!registry.valid(w,original)||container(w,p)!=original.container())throw new IllegalStateException("Ownership/container lost during native "+state);
   }


  }
  System.out.println("NATIVE_CHEST_STATE_PASS OpenCloseOwnership=PRESERVED SameContainer=true");
 }
 static void inspect(World w,org.joml.Vector3d guardStart)throws Exception{
  var store=w.getEntityStore().getStore();var npcs=new HashMap<String,Ref<EntityStore>>();
  store.forEachChunk(NPCEntity.getComponentType(),(chunk,b)->{for(int i=0;i<chunk.size();i++){
   var ref=chunk.getReferenceTo(i);var npc=store.getComponent(ref,NPCEntity.getComponentType());
   if(npc.getRole()==null||npcs.put(npc.getRoleName(),ref)!=null)throw new IllegalStateException("Duplicate/uninitialized NPC");
  }});
  if(npcs.size()!=2||!npcs.containsKey("Kweebec_Sapling")||!npcs.containsKey("Kweebec_Razorleaf"))throw new IllegalStateException("Expected exact two native roles: "+npcs.keySet());
  var civilian=npcs.get("Kweebec_Sapling");var guard=npcs.get("Kweebec_Razorleaf");
  var t=store.getComponent(civilian,TransformComponent.getComponentType());var h=store.getComponent(civilian,HeadRotation.getComponentType());
  if(store.getComponent(civilian,Frozen.getComponentType())==null||store.getComponent(guard,Frozen.getComponentType())!=null)throw new IllegalStateException("Frozen must apply only to civilian");
  if(t.getPosition().distance(.5,64,10.5)>0.01||Math.abs(t.getRotation().yaw()-(float)Math.PI)>.001||Math.abs(h.getRotation().yaw()-(float)Math.PI)>.001||h.getDirection().z<.99)throw new IllegalStateException("Civilian moved/turned: "+t.getPosition()+" body="+t.getRotation()+" head="+h.getRotation());
  var nativeWitness=JsonParser.parseString(Files.readString(Path.of("mods/ContainerTheftFixture/Server/NPC/Roles/HyThiefContainerFixture_Native_Sapling.json"))).getAsJsonObject().getAsJsonObject("Parameters");
  if(nativeWitness.getAsJsonObject("ViewRange").get("Value").getAsDouble()!=15.0||nativeWitness.getAsJsonObject("ViewSector").get("Value").getAsDouble()!=180)throw new IllegalStateException("Witness/native template perception mismatch");
  var report=new JsonObject();report.addProperty("civilianRole","Kweebec_Sapling");report.addProperty("civilianFrozen",true);report.addProperty("civilianBodyYaw",t.getRotation().yaw());report.addProperty("civilianHeadYaw",h.getRotation().yaw());report.addProperty("civilianDirection",h.getDirection().toString());report.addProperty("guardRole","Kweebec_Razorleaf");report.addProperty("guardFrozen",false);
  var gp=store.getComponent(guard,TransformComponent.getComponentType()).getPosition();report.addProperty("guardPosition",gp.toString());
  if(Boolean.getBoolean("containerworld.stationaryguard")){if(guardStart==null||gp.distance(guardStart)>.25)throw new IllegalStateException("Guard idle movement remained active "+guardStart+" -> "+gp);report.addProperty("guardIdleObservedSeconds",60);report.addProperty("guardIdleDisplacement",gp.distance(guardStart));}
  if(gp.x<7||gp.x>=12||gp.z<0||gp.z>=22)throw new IllegalStateException("Guard left shielded alcove "+gp);
  // Validate opaque intersections along the entire eye-to-chest/player segment, not a crime/witness check.
  for(double[] point:new double[][]{{.5,4.5},{.5,5.5},{.5,15.5},{.5,16.5},{.5,18.5},{.5,19.5},{.5,28.5},{.5,29.5},{.5,32.5},{.5,33.5},{-2.5,15.5},{-2.5,16.5}}){
   double fraction=(gp.x-6.5)/(gp.x-point[0]), wallZ=gp.z+fraction*(point[1]-gp.z);
   for(int y=64;y<=68;y++)if(!w.getBlockType(6,y,(int)Math.floor(wallZ)).getId().equals("Rock_Stone_Cobble"))throw new IllegalStateException("Shield wall gap");
  }
  report.addProperty("guardChestAndApproachSightlinesOpaque",true);
  Set<String> reachable=new HashSet<>();ArrayDeque<int[]> q=new ArrayDeque<>();q.add(new int[]{0,-4});reachable.add("0,-4");
  while(!q.isEmpty()){var p=q.remove();for(var d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int x=p[0]+d[0],z=p[1]+d[1];String k=x+","+z;
   if(x< -8||x>13||z< -7||z>44||reachable.contains(k))continue;
   if(w.getBlockType(x,64,z).getId().equals("Empty")&&w.getBlockType(x,65,z).getId().equals("Empty")){reachable.add(k);q.add(new int[]{x,z});}
  }}
  if(!reachable.contains("9,10")||!reachable.contains("0,15")||!reachable.contains("0,18")||!reachable.contains("0,28")||!reachable.contains("0,32")||!reachable.contains("0,5")||!reachable.contains("-3,15"))throw new IllegalStateException("Missing guard/control/owned walk access");
  report.addProperty("hubChestGuardTwoPlayerWalkAccess",true);report.addProperty("guardRoute","native path around unchanged shield wall");
  var spawn=w.getWorldConfig().getSpawnProvider().getSpawnPoint(w,new UUID(0,0)).getPosition();
  if(spawn.distance(.5,64.1,7.5)>.01||w.getBlock(0,63,7)==0||w.getBlock(0,64,7)!=0||w.getBlock(0,65,7)!=0)throw new IllegalStateException("Invalid hub spawn");
  report.addProperty("spawn",spawn.toString());
  int[] labelCount={0};store.forEachChunk(Nameplate.getComponentType(),(chunk,b)->{labelCount[0]+=chunk.size();});
  if(labelCount[0]<15)throw new IllegalStateException("Missing coordinate/range labels: "+labelCount[0]);report.addProperty("labelEntities",labelCount[0]);
  var registry=new OwnedContainerRegistry();var chestReport=new JsonArray();
  int[] ownedLabels={0};int[] controls={0};
  store.forEachChunk(Nameplate.getComponentType(),(chunk,b)->{for(int i=0;i<chunk.size();i++){
    var ref=chunk.getReferenceTo(i);var label=store.getComponent(ref,Nameplate.getComponentType());String text=label.getText();
    if(text.contains("/hythiefcontainer"))throw new IllegalStateException("Manual-command label persisted");
    if(text.startsWith("3 PERSISTENT OWNED - NEARBY")||text.startsWith("3 OWNED - 5 BLOCKS")||text.startsWith("4 OWNED - 8 BLOCKS")||text.startsWith("5 OWNED - 18 BLOCKS")||text.startsWith("6 OWNED - 22 BLOCKS")||text.equals("2 OWNED - REAR / UNSEEN"))ownedLabels[0]++;
    if(text.equals("1 CONTROL - UNOWNED")){
     controls[0]++;if(store.getComponent(ref,TransformComponent.getComponentType()).getPosition().distance(-4.5,66.4,16.5)>.01)throw new IllegalStateException("Control label not moved");
    }
  }});
  if(ownedLabels[0]!=5||controls[0]!=1)throw new IllegalStateException("Expected numbered owned/control labels");
  report.addProperty("ownedLabelsWithoutCommands",true);
  if(!w.getBlockType(-4,64,-2).getId().equals("Empty"))throw new IllegalStateException("Old hidden control remains");
  var nativeChest=com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType.getAssetMap().getAsset("Furniture_Kweebec_Chest_Large");
  for(int i=0;i<CHESTS.length;i++){
   var p=CHESTS[i];var type=w.getBlockType(p[0],p[1],p[2]);
   if(!type.getId().equals(OWNED) && !type.getId().equals(nativeChest.getBlockForState("OpenWindow").getId()) && !type.getId().equals(nativeChest.getBlockForState("CloseWindow").getId()))throw new IllegalStateException("Wrong native chest family");
   if(!type.getInteractions().get(com.hypixel.hytale.protocol.InteractionType.Use).equals("Open_Container"))throw new IllegalStateException("Native open changed");
   if(!type.getCustomModel().equals(nativeChest.getCustomModel())||type.getCustomModelScale()!=nativeChest.getCustomModelScale())throw new IllegalStateException("Chest appearance changed");
   if(!type.getInteractionHint().equals(i==2?nativeChest.getInteractionHint():HINT))throw new IllegalStateException("Incorrect ownership hint");
  }
  if(Files.exists(Path.of("mods/ContainerTheftFixture/Server/Item/Items/Container/HyThief_Test_Owned_Kweebec_Chest.json")))throw new IllegalStateException("Custom chest definition remains");
  for(double x:new double[]{-2.5,.5}){
   double dx=x-.5,dz=5;double distance=Math.hypot(dx,dz),angle=Math.toDegrees(Math.atan2(Math.abs(dx),dz));
   if(distance>15||angle>=90)throw new IllegalStateException("Front approach outside native forward sector/range");
   for(int step=1;step<=20;step++){double f=step/20.0;if(!w.getBlockType((int)Math.floor(.5+dx*f),65,(int)Math.floor(10.5+dz*f)).getId().equals("Empty"))throw new IllegalStateException("Front/control approach obstructed");}
  }
  report.addProperty("controlPlayerDistance",Math.hypot(3,5));report.addProperty("controlPlayerForwardAngleDegrees",Math.toDegrees(Math.atan2(3,5)));report.addProperty("frontControlSightlinesClear",true);
  report.addProperty("ownedHint","native");report.addProperty("controlNativeHint",nativeChest.getInteractionHint());report.addProperty("nativeOpenContainerPreserved",true);
  for(int[] p:CHESTS){var c=container(w,p);if(c.getCapacity()!=36)throw new IllegalStateException("Wrong chest capacity");
   for(short s=0;s<3;s++){var item=c.getItemStack(s);if(item==null||!item.getItemId().equals(ITEMS[s])||item.getQuantity()!=AMOUNTS[s])throw new IllegalStateException("Test stack missing");}
   var r=new JsonObject();r.addProperty("coordinate",Arrays.toString(p));r.addProperty("capacity",c.getCapacity());r.addProperty("matchingStacks",true);chestReport.add(r);
  }
  var entries=new ArrayList<OwnedContainerRegistry.Entry>();
  for(int i:new int[]{0,1,3,4,5}){var p=CHESTS[i];entries.add(new OwnedContainerRegistry.Entry(new OwnedContainerRegistry.Key(w.getWorldConfig().getUuid(),p[0],p[1],p[2]),container(w,p)));}
  registry.replaceAutomatic(w,entries);
  for(var entry:entries)if(registry.findAutomatic(w,entry.container(),null)!=entry)throw new IllegalStateException("Automatic lookup mismatch");
  if(registry.findAutomatic(w,container(w,CHESTS[2]),null)!=null)throw new IllegalStateException("Control owned");
  registry.removeAutomatic(w);if(!registry.automatic(w).isEmpty())throw new IllegalStateException("Stale world ownership");registry.replaceAutomatic(w,entries);
  nativeStates(w,registry);
  var proof=BlockModule.getComponent(OwnershipProofData.getComponentType(),w,0,64,16);
  if(!PersistentContainerOwnership.eligible(proof))throw new IllegalStateException("Serialized ownership missing on candidate load");
  if(BlockModule.getComponent(OwnershipProofData.getComponentType(),w,-3,64,16)!=null)throw new IllegalStateException("Control has ownership metadata");
  System.out.println("PERSISTENT_RESTORED_PASS Owned=true Scope=NEARBY NoReattach="+!Boolean.getBoolean("containerworld.ownershipseed"));
  if(!Boolean.getBoolean("containerworld.layout") && !Boolean.getBoolean("containerworld.ownershipauthoronly"))ContainerWitnessTest.run(w,civilian,guard,report);
  oneRun(w,registry);report.addProperty("oneRunControlRearFrontWithoutReset",true);
  report.addProperty("automaticRegistryFiveOwnedOneControl",true);report.addProperty("registryRemovalRebuild",true);
  // Use the SAME packaged probe/registry as the installed diagnostic plugin, on this world's actual containers.
  realProbe(container(w,CHESTS[3]),"close-partial",4,(c,p)->c.moveItemStackFromSlotToSlot((short)0,4,p,(short)0));
  realProbe(container(w,CHESTS[0]),"front-partial",4,(c,p)->c.moveItemStackFromSlotToSlot((short)0,4,p,(short)0));
  realProbe(container(w,CHESTS[0]),"insertion",0,(c,p)->p.moveItemStackFromSlotToSlot((short)1,3,c,(short)4));
  realProbe(container(w,CHESTS[0]),"internal",0,(c,p)->c.moveItemStackFromSlotToSlot((short)0,4,c,(short)5));
  realProbe(container(w,CHESTS[1]),"rear-partial",4,(c,p)->c.moveItemStackFromSlotToSlot((short)0,4,p,(short)0));
  realProbe(container(w,CHESTS[0]),"front-full",16,(c,p)->c.moveItemStackFromSlotToSlot((short)0,16,p,(short)0));
  realProbe(container(w,CHESTS[1]),"rear-quick",16,(c,p)->c.moveItemStackFromSlot((short)0,16,p));
  var control=container(w,CHESTS[2]);var recipient=new SimpleItemContainer((short)4);control.moveItemStackFromSlotToSlot((short)0,4,recipient,(short)0);if(recipient.getItemStack((short)0).getQuantity()!=4||registry.findAutomatic(w,control,recipient)!=null)throw new IllegalStateException("Control withdrawal ownership");fill(control);System.out.println("REAL_CONTROL_TRANSFER_PASS Ownership=UNOWNED Removed=4");
  ContainerProbeTest.matrix();report.addProperty("packagedRegistryCoordinateValidation",true);report.addProperty("controlNotMarked",true);report.addProperty("realChestProbePositiveAndNegatives",true);report.addProperty("nativeMatrixPassed",true);report.add("chests",chestReport);
  Files.writeString(Path.of(Boolean.getBoolean("containerworld.reload")?"reload-verification.json":"build-verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
  for(int x=-1;x<=0;x++)for(int z=-1;z<=1;z++){var saved=w.getChunkIfLoaded(com.hypixel.hytale.math.util.ChunkUtil.indexChunk(x,z));if(saved!=null)saved.markNeedsSaving();}System.out.println("CONTAINER_WORLD_VERIFIED "+report);w.scheduleAfter(()->CompletableFuture.runAsync(()->HytaleServer.get().shutdownServer()),2,TimeUnit.SECONDS);
 }
 interface Move {void run(com.hypixel.hytale.server.core.inventory.container.ItemContainer c,SimpleItemContainer p);}
 static void realProbe(com.hypixel.hytale.server.core.inventory.container.ItemContainer c,String name,int expected,Move action){
  var player=new SimpleItemContainer((short)10);player.setItemStackForSlot((short)1,new ItemStack("Ingredient_Bar_Iron",3));var removed=new ArrayList<ContainerRemovalProbe.Removal>();var probe=new ContainerRemovalProbe(c,removed::add);
  var a=c.registerChangeEvent(e->probe.source(e.transaction()));var b=player.registerChangeEvent(e->probe.receipt(e.transaction()));
  try{action.run(c,player);probe.finish();if(removed.stream().mapToInt(ContainerRemovalProbe.Removal::amount).sum()!=expected || (expected>0 && removed.size()!=1))throw new IllegalStateException("Real chest probe "+name+" "+removed);System.out.println("REAL_CHEST_PROBE_PASS "+name+" "+removed);}
  finally{a.unregister();b.unregister();fill(c);}
 }
 static void fail(Throwable t){System.out.println("CONTAINER_WORLD_FAILED "+t);t.printStackTrace();CompletableFuture.runAsync(()->HytaleServer.get().shutdownServer());}
}
