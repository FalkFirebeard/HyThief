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
 static final int[][] CHESTS={{0,64,16},{0,64,4},{-4,64,-2}};
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
     w.scheduleAfter(()->w.execute(()->{
      try{
       if(!Boolean.getBoolean("containerworld.reload"))for(int[] p:CHESTS)fill(container(w,p));
       inspect(w);
      }catch(Throwable t){fail(t);}
     }),6,TimeUnit.SECONDS);
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
  if(c!=null && c.getItemContainer()==null && !Boolean.getBoolean("containerworld.reload"))c.setItemContainer(new SimpleItemContainer(c.getCapacity()));
  if(c==null||c.getItemContainer()==null)throw new IllegalStateException("Missing native chest "+Arrays.toString(p)+" block="+w.getBlockType(p[0],p[1],p[2]).getId()+" blockBelow="+w.getBlockType(p[0],p[1]-1,p[2]).getId());
  return c.getItemContainer();
 }
 static void fill(com.hypixel.hytale.server.core.inventory.container.ItemContainer c){
  c.clear();for(short s=0;s<ITEMS.length;s++)c.setItemStackForSlot(s,new ItemStack(ITEMS[s],AMOUNTS[s]));
 }
 static void inspect(World w)throws Exception{
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
  var report=new JsonObject();report.addProperty("civilianRole","Kweebec_Sapling");report.addProperty("civilianFrozen",true);report.addProperty("civilianBodyYaw",t.getRotation().yaw());report.addProperty("civilianHeadYaw",h.getRotation().yaw());report.addProperty("civilianDirection",h.getDirection().toString());report.addProperty("guardRole","Kweebec_Razorleaf");report.addProperty("guardFrozen",false);
  var gp=store.getComponent(guard,TransformComponent.getComponentType()).getPosition();report.addProperty("guardPosition",gp.toString());
  if(gp.x<7||gp.x>=12||gp.z<0||gp.z>=22)throw new IllegalStateException("Guard left shielded alcove "+gp);
  // Validate opaque intersections along the entire eye-to-chest/player segment, not a crime/witness check.
  for(double zz:new double[]{4.5,5.5,15.5,16.5}){
   double fraction=(gp.x-6.5)/(gp.x-.5), wallZ=gp.z+fraction*(zz-gp.z);
   for(int y=64;y<=68;y++)if(!w.getBlockType(6,y,(int)Math.floor(wallZ)).getId().equals("Rock_Stone_Cobble"))throw new IllegalStateException("Shield wall gap");
  }
  report.addProperty("guardChestAndApproachSightlinesOpaque",true);
  Set<String> reachable=new HashSet<>();ArrayDeque<int[]> q=new ArrayDeque<>();q.add(new int[]{0,-4});reachable.add("0,-4");
  while(!q.isEmpty()){var p=q.remove();for(var d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int x=p[0]+d[0],z=p[1]+d[1];String k=x+","+z;
   if(x< -8||x>13||z< -7||z>44||reachable.contains(k))continue;
   if(w.getBlockType(x,64,z).getId().equals("Empty")&&w.getBlockType(x,65,z).getId().equals("Empty")){reachable.add(k);q.add(new int[]{x,z});}
  }}
  if(!reachable.contains("9,10")||!reachable.contains("0,15")||!reachable.contains("0,5")||!reachable.contains("-1,15"))throw new IllegalStateException("Missing guard/hub/two-player walk access");
  report.addProperty("hubChestGuardTwoPlayerWalkAccess",true);report.addProperty("guardRoute","south around wall x=6,z<0");
  var spawn=w.getWorldConfig().getSpawnProvider().getSpawnPoint(w,new UUID(0,0)).getPosition();
  if(spawn.distance(.5,64.1,7.5)>.01||w.getBlock(0,63,7)==0||w.getBlock(0,64,7)!=0||w.getBlock(0,65,7)!=0)throw new IllegalStateException("Invalid hub spawn");
  report.addProperty("spawn",spawn.toString());
  int[] labelCount={0};store.forEachChunk(Nameplate.getComponentType(),(chunk,b)->{labelCount[0]+=chunk.size();});
  if(labelCount[0]<15)throw new IllegalStateException("Missing coordinate/range labels: "+labelCount[0]);report.addProperty("labelEntities",labelCount[0]);
  var registry=new OwnedContainerRegistry();var chestReport=new JsonArray();
  int[] ownedLabels={0};
  store.forEachChunk(Nameplate.getComponentType(),(chunk,b)->{for(int i=0;i<chunk.size();i++){
    var ref=chunk.getReferenceTo(i);var label=store.getComponent(ref,Nameplate.getComponentType());String text=label.getText(); if(Boolean.getBoolean("containerworld.pristine") && text.contains("/hythiefcontainer"))throw new IllegalStateException("Manual-command label persisted"); if(Boolean.getBoolean("containerworld.pristine") && text.contains("/hythiefcontainer"))throw new IllegalStateException("Manual-command label persisted");
    if(text.startsWith("FRONT")){label.setText("FRONT - Kweebec-owned / open and take items");ownedLabels[0]++;}
    if(text.startsWith("REAR")){label.setText("REAR - Kweebec-owned / open and take items");ownedLabels[0]++;}
    if(text.startsWith("UNOWNED CONTROL"))label.setText("UNOWNED CONTROL - no removal diagnostic");
    if(text.startsWith("HUB"))label.setText("ZERO COMMANDS - automatic attribution / no crime consequences");
  }});
  if(ownedLabels[0]!=2)throw new IllegalStateException("Expected two owned station labels");
  report.addProperty("ownedLabelsWithoutCommands",true);
  for(int[] p:CHESTS){var c=container(w,p);if(c.getCapacity()!=36)throw new IllegalStateException("Wrong chest capacity");
   for(short s=0;s<3;s++){var item=c.getItemStack(s);if(item==null||!item.getItemId().equals(ITEMS[s])||item.getQuantity()!=AMOUNTS[s])throw new IllegalStateException("Test stack missing");}
   var r=new JsonObject();r.addProperty("coordinate",Arrays.toString(p));r.addProperty("capacity",c.getCapacity());r.addProperty("matchingStacks",true);chestReport.add(r);
  }
  var entries=new ArrayList<OwnedContainerRegistry.Entry>();
  for(int i=0;i<2;i++){var p=CHESTS[i];entries.add(new OwnedContainerRegistry.Entry(new OwnedContainerRegistry.Key(w.getWorldConfig().getUuid(),p[0],p[1],p[2]),container(w,p)));}
  registry.replaceAutomatic(w,entries);
  for(var entry:entries)if(registry.findAutomatic(w,entry.container(),null)!=entry)throw new IllegalStateException("Automatic lookup mismatch");
  if(registry.findAutomatic(w,container(w,CHESTS[2]),null)!=null)throw new IllegalStateException("Control owned");
  registry.removeAutomatic(w);if(!registry.automatic(w).isEmpty())throw new IllegalStateException("Stale world ownership");registry.replaceAutomatic(w,entries);
  report.addProperty("automaticRegistryTwoOwnedOneControl",true);report.addProperty("registryRemovalRebuild",true);
  // Use the SAME packaged probe/registry as the installed diagnostic plugin, on this world's actual containers.
  realProbe(container(w,CHESTS[0]),"front-partial",4,(c,p)->c.moveItemStackFromSlotToSlot((short)0,4,p,(short)0));
  realProbe(container(w,CHESTS[0]),"insertion",0,(c,p)->p.moveItemStackFromSlotToSlot((short)1,3,c,(short)4));
  realProbe(container(w,CHESTS[0]),"internal",0,(c,p)->c.moveItemStackFromSlotToSlot((short)0,4,c,(short)5));
  realProbe(container(w,CHESTS[1]),"rear-partial",4,(c,p)->c.moveItemStackFromSlotToSlot((short)0,4,p,(short)0));
  realProbe(container(w,CHESTS[0]),"front-full",16,(c,p)->c.moveItemStackFromSlotToSlot((short)0,16,p,(short)0));
  realProbe(container(w,CHESTS[1]),"rear-quick",16,(c,p)->c.moveItemStackFromSlot((short)0,16,p));
  var control=container(w,CHESTS[2]);var recipient=new SimpleItemContainer((short)4);control.moveItemStackFromSlotToSlot((short)0,4,recipient,(short)0);if(recipient.getItemStack((short)0).getQuantity()!=4||registry.findAutomatic(w,control,recipient)!=null)throw new IllegalStateException("Control withdrawal ownership");fill(control);System.out.println("REAL_CONTROL_TRANSFER_PASS Ownership=UNOWNED Removed=4");
  ContainerProbeTest.matrix();report.addProperty("packagedRegistryCoordinateValidation",true);report.addProperty("controlNotMarked",true);report.addProperty("realChestProbePositiveAndNegatives",true);report.addProperty("nativeMatrixPassed",true);report.add("chests",chestReport);
  Files.writeString(Path.of(Boolean.getBoolean("containerworld.reload")?"reload-verification.json":"build-verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
  for(int x=-1;x<=0;x++)for(int z=-1;z<=1;z++){var saved=w.getChunkIfLoaded(com.hypixel.hytale.math.util.ChunkUtil.indexChunk(x,z));if(saved!=null)saved.markNeedsSaving();}System.out.println("CONTAINER_WORLD_VERIFIED "+report);w.setTicking(false);CompletableFuture.runAsync(()->HytaleServer.get().shutdownServer());
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
