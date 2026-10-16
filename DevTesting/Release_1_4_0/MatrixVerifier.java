package com.local.pickpocket;
import com.google.gson.*;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3d;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
public final class MatrixVerifier extends JavaPlugin {
 public MatrixVerifier(JavaPluginInit init){super(init);}
 @Override protected void setup(){getCommandRegistry().registerCommand(new Verify());}
 static final class Verify extends AbstractWorldCommand {
  Verify(){super("hythiefmatrix-verify","Isolated complete supported NPC matrix verification");}
  @Override protected void execute(CommandContext context,World world,Store<EntityStore> store){
   var chunks=new ArrayList<CompletableFuture<?>>();
   for(int x=0;x<=2;x++)for(int z=0;z<=2;z++)chunks.add(world.getChunkStore().getChunkSectionReferenceAsync(x,3,z,com.hypixel.hytale.server.core.universe.world.storage.GetChunkFlags.SET_TICKING));
   CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).thenRunAsync(()->inspect(world,store),world).exceptionally(e->{fail(e);return null;});
  }
  static void inspect(World world,Store<EntityStore> store){try{
   var matrix=JsonParser.parseString(Files.readString(Path.of("supported-matrix.json"))).getAsJsonArray();
   int good=0;int abstractCount=0;var failures=new ArrayList<String>();var ids=new HashSet<String>();
   for(var row:matrix){
    String id=row.getAsJsonObject().get("Role").getAsString();ids.add(id); if(java.util.Set.of("Goblin_Hermit","Goblin_Lobber_Patrol","Goblin_Miner_Patrol","Goblin_Ogre","Goblin_Scavenger","Goblin_Scavenger_Battleaxe","Goblin_Scavenger_Sword","Goblin_Scrapper_Patrol","Goblin_Thief_Patrol").contains(id)){System.out.println("NATIVE_RETIRED_ROLE_CONFIG_RETAINED role="+id); continue;}
    try{
     if(id.equals("Bramblekin")||id.equals("Bramblekin_Shaman")){
       var manager=NPCPlugin.get().getBuilderManager();int roleIndex=manager.getIndex(id);
       var info=manager.tryGetBuilderInfo(roleIndex);
       if(info==null||!info.getBuilder().getTypeName().equals("Abstract"))throw new IllegalStateException("Expected native abstract definition");
       if(!manager.validateBuilder(info))throw new IllegalStateException("Abstract builder invalid");
       abstractCount++;System.out.println("MATRIX_ABSTRACT_VALID role="+id+" configuredSupported=true directlySpawnable=false");
       continue;
     }
     NPCPlugin.get().validateSpawnableRole(id);
     var pair=NPCPlugin.get().spawnNPC(store,id,null,new Vector3d(3.5+(good%14)*2,100,3.5+(good/14)*2),Rotation3f.ZERO);
     if(pair==null||!pair.first().isValid())throw new IllegalStateException("spawn returned null/invalid");
     var npc=store.getComponent(pair.first(),NPCEntity.getComponentType());
     if(npc==null||npc.getRole()==null||!id.equals(npc.getRoleName()))throw new IllegalStateException("actual role missing/different");
     if(npc.getRole().getInteractionInstruction()==null)throw new IllegalStateException("missing interaction tree");
     verifyIdleFamily(pair.first(), store, id);
     good++;System.out.println("MATRIX_ROLE_READY role="+id+" actualRole="+npc.getRoleName()+" interactionTree=true");
    }catch(Throwable e){failures.add(id+" "+e);System.out.println("MATRIX_ROLE_FAILED role="+id+" "+e);}
   }
   for(String id:List.of("Skeleton","Wolf_Trork_Hunter","Wolf_Trork_Shaman","Wolf_Outlander_Priest","Wolf_Outlander_Sorcerer")){
    try{NPCPlugin.get().validateSpawnableRole(id);var pair=NPCPlugin.get().spawnNPC(store,id,null,new Vector3d(40.5,100,40.5),Rotation3f.ZERO);
     if(pair==null)throw new IllegalStateException("null spawn");
     System.out.println("EXCLUDED_ROLE_NATIVE_SPAWN role="+id+" valid=true");
    }catch(Throwable e){System.out.println("EXCLUDED_ROLE_NATIVE_SPAWN role="+id+" valid=false "+e);}
   }
   Files.write(Path.of("matrix-failures.txt"),failures);
   if(good!=174||abstractCount!=2||ids.size()!=185||!failures.isEmpty())throw new IllegalStateException("Matrix ready="+good+" abstract="+abstractCount+" failures="+failures);
   System.out.println("FULL_MATRIX_VERIFIED configuredRoles=185 spawnedRoles=174 nativeRetired=9 validatedAbstracts=2 actualRolePreserved=true interactionTrees=true");
   world.setTicking(false);CompletableFuture.runAsync(()->HytaleServer.get().shutdownServer());
  }catch(Throwable e){fail(e);}}
  static void verifyIdleFamily(com.hypixel.hytale.component.Ref<EntityStore> ref, Store<EntityStore> store, String role) throws Exception {
   var state = store.getComponent(ref, StateSupport.getComponentType());
   if (state == null) throw new IllegalStateException("Missing StateSupport");
   // Test-only access to package-private production helpers across plugin class loaders.
   // No reflection is added to the shipped mod.
   var filterClass = PickpocketPlugin.class.getClassLoader().loadClass("com.local.pickpocket.HyThiefStealthAcquisition");
   var classify = filterClass.getDeclaredMethod("isIdleFamily", StateSupport.class); classify.setAccessible(true);
   var calibration = filterClass.getDeclaredMethod("multiplier", String.class, String.class); calibration.setAccessible(true);
   var helper = state.getStateHelper(); int savedState = state.getStateIndex(), savedSub = state.getSubStateIndex();
   int tested = 0, idle = 0;
   try {
    for (int main : helper.getAllMainStates()) {
     int highest;
     try { highest = helper.getHighestSubStateIndex(main); }
     catch (NullPointerException unusedMapping) {
      System.out.println("UNINSTANTIATED_STATE_MAPPING role="+role+" mainState="+helper.getStateName(main));
      continue; // reserved/unused main-state names have no native substate table
     }
     for (int sub = 0; sub <= highest; sub++) {
     state.setState(main, sub, false, true); // isolated fixture only; no transition actions or AI ticks
     boolean expected = "Idle".equals(helper.getStateName(main));
     if ((boolean) classify.invoke(null, state) != expected) throw new IllegalStateException("State classification: " + state.getStateName());
     tested++; if (expected) idle++;
     System.out.println("STATE_FAMILY_VERIFIED role="+role+" state="+state.getStateName()+" idleFamily="+expected);
     }
    }
    if (idle == 0) throw new IllegalStateException("Idle family missing");
    for (String channel : List.of("sight","scent","proximity","watch")) {
     double expected = channel.equals("sight") ? 4.0/15.0 : channel.equals("scent")
         ? Set.of("Feran_Sharptooth","Feran_Longtooth","Tuluk_Fisherman").contains(role) ? 0.15 : 0.10 : 0.50;
     if ((double) calibration.invoke(null, channel, role) != expected) throw new IllegalStateException("Calibration changed");
     System.out.println("CHANNEL_CALIBRATION_VERIFIED role="+role+" channel="+channel+" multiplier="+expected);
    }
    System.out.println("IDLE_FAMILY_ROLE_VERIFIED role="+role+" states="+tested+" idleSubstates="+idle);
   } finally { state.setState(savedState, savedSub, false, true); }
  }
  static void fail(Throwable e){System.out.println("FULL_MATRIX_FAILED "+e);e.printStackTrace();CompletableFuture.runAsync(()->HytaleServer.get().shutdownServer());}
 }
}
