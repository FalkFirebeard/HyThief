package com.firebeard.hythief.dev;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import org.joml.Vector3i;
public final class WorldVerifier extends JavaPlugin {
 public WorldVerifier(JavaPluginInit init){super(init);}
 @Override protected void setup(){getCommandRegistry().registerCommand(new Verify());}
 static final class Verify extends AbstractWorldCommand {
  Verify(){super("hythiefworld-verify","Isolated development world seed/reload verification");}
  @Override protected void execute(CommandContext context, World world, Store<EntityStore> store){
   try{
    var chunks=new ArrayList<CompletableFuture<?>>();
    for(int x=-2;x<=10;x++)for(int z=-2;z<=11;z++)for(int y=0;y<=3;y++)chunks.add(world.getChunkStore().getChunkSectionReferenceAsync(x,y,z,com.hypixel.hytale.server.core.universe.world.storage.GetChunkFlags.SET_TICKING));
    CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).thenRunAsync(()->{
     try{
      if(Boolean.getBoolean("arena.create")){
       var prefab=PrefabStore.get().getPrefab(PrefabStore.get().findAssetPrefabPath("HyThief_Test_Arena.prefab.json"));
       if(prefab.getEntityCount()!=17)throw new IllegalStateException("Wrong prefab entity count");
       prefab.placeNoReturn(world,new Vector3i(0,100,0),store);
       world.execute(()->inspect(world,store));
      }else CompletableFuture.delayedExecutor(1,TimeUnit.SECONDS).execute(()->world.execute(()->inspect(world,store)));
     }catch(Throwable e){fail(e);}
    },world).exceptionally(e->{fail(e);return null;});
   }catch(Throwable e){fail(e);}
  }
  static void inspect(World world,Store<EntityStore> store){try{
   var lines=new TreeSet<String>();var uuids=new HashSet<UUID>();int[] count={0};
   store.forEachChunk(NPCEntity.getComponentType(),(chunk,buffer)->{
    for(int i=0;i<chunk.size();i++){
     var ref=chunk.getReferenceTo(i);var npc=store.getComponent(ref,NPCEntity.getComponentType());
     var uuid=store.getComponent(ref,UUIDComponent.getComponentType());
     var transform=store.getComponent(ref,TransformComponent.getComponentType());
     if(npc.getRole()==null||uuid==null||transform==null)throw new IllegalStateException("NPC not initialized");
     uuids.add(uuid.getUuid());count[0]++;lines.add(npc.getRoleName()+" "+uuid.getUuid()); System.out.println("CELL_NPC role="+npc.getRoleName()+" uuid="+uuid.getUuid()+" position="+transform.getPosition()+" interactionTree="+(npc.getRole().getInteractionInstruction()!=null)); if(!java.util.Set.of("Fox","Bear_Grizzly","Spectre_Void").contains(npc.getRoleName()) && npc.getRole().getInteractionInstruction()==null)throw new IllegalStateException("Missing supported interaction tree");
     if(npc.getRoleName().equals("Kweebec_Merchant"))System.out.println("MERCHANT_POSITION "+transform.getPosition());
    }
   });
   if(count[0]!=16||uuids.size()!=16)throw new IllegalStateException("Expected 16 unique active NPCs, found "+count[0]);
   var spawn=world.getWorldConfig().getSpawnProvider().getSpawnPoints()[0].getPosition();
   if(block(world,0,99,0)==0 || block(world,0,100,0)!=0 || block(world,0,101,0)!=0)throw new IllegalStateException("Spawn floor/headroom invalid");
   if(block(world,0,99,50)==0)throw new IllegalStateException("Merchant bay floor absent");
   if(block(world,18,101,50)==0)throw new IllegalStateException("Saved arena wall absent above flat terrain");
   System.out.println("WORLD_CONFIG mode="+world.getWorldConfig().getGameMode()+" spawn="+spawn+" uuid="+world.getWorldConfig().getUuid()+" floor/headroom=valid");
      for(int row=0;row<4;row++)for(int col=0;col<4;col++){
    int x=col*96,z=row*96+50;
    if(block(world,x,99,z)==0||block(world,x,100,z)!=0||block(world,x,101,z)!=0||block(world,x+18,101,z)==0)throw new IllegalStateException("Broken bay "+(row*4+col+1));
   }
   Files.write(Path.of("verified-npcs.txt"),lines);
   System.out.println("CELL_GEOMETRY_PASS bays=16 floorHeadroomSideWall=true");
   System.out.println("WORLD_VERIFIED count=16 uniqueUUIDs=16 create="+Boolean.getBoolean("arena.create"));
   world.setTicking(false);
   CompletableFuture.runAsync(()->HytaleServer.get().shutdownServer());
  }catch(Throwable e){fail(e);}}
  static int block(World world,int x,int y,int z){
   var ref=world.getChunkStore().getChunkSectionReference(x>>5,y>>5,z>>5);
   if(ref==null)throw new IllegalStateException("Section absent at "+x+","+y+","+z);
   var section=world.getChunkStore().getStore().getComponent(ref,com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection.getComponentType());
   if(section==null)throw new IllegalStateException("Block section absent");
   return section.get(x,y,z);
  }
  static void fail(Throwable e){System.out.println("WORLD_FAILED "+e);e.printStackTrace();CompletableFuture.runAsync(()->HytaleServer.get().shutdownServer());}
 }
}
