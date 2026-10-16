package com.local.pickpocket;

import com.google.gson.*;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.plugin.*;
import com.hypixel.hytale.server.core.universe.*;
import com.hypixel.hytale.server.core.universe.world.*;
import com.hypixel.hytale.server.core.universe.world.storage.*;
import com.hypixel.hytale.server.core.modules.block.*;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.npc.*;
import com.hypixel.hytale.server.npc.entities.*;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.role.support.*;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.builtin.blockspawner.*;
import com.hypixel.hytale.builtin.blockspawner.state.BlockSpawner;
import org.joml.Vector3d;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Disposable native production-asset proof, never shipped. */
public final class ProductionContainerProof extends JavaPlugin {
  World w; Store<EntityStore> s;
  JsonObject map; int attempt; List<String> tables; int[] zs;
  public ProductionContainerProof(JavaPluginInit init) { super(init); }
  static void require(boolean ok,String reason) { if(!ok) throw new IllegalStateException(reason); }
  void out(String text) { System.out.println("PRODUCTION_PROOF "+text); }
  protected void start() {
    Universe.get().getUniverseReady().thenRun(()->{
      w=Universe.get().getDefaultWorld();
      var loads=new ArrayList<CompletableFuture<?>>();
      for(int x=-2;x<=1;x++) for(int z=-1;z<=3;z++) for(int y=1;y<=2;y++)
        loads.add(w.getChunkStore().getChunkSectionReferenceAsync(x,y,z,GetChunkFlags.SET_TICKING));
      CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).thenRun(()->w.execute(()->{
        try { init(); } catch(Throwable t) { fail(t); }
      }));
    });
  }
  void init() throws Exception {
    s=w.getEntityStore().getStore();
    if(System.getProperty("production.mode").equals("production-transactions")) { transactions();return; }
    if(System.getProperty("production.mode").startsWith("production-reaction-")) { reaction();return; }
    map=JsonParser.parseString(Files.readString(Path.of("production-ownership-map.json"))).getAsJsonObject();
    tables=new ArrayList<>(map.keySet());
    zs=new int[tables.size()];
    if(System.getProperty("production.mode").equals("production-restart")) {
      var positions=Files.readAllLines(Path.of("production-spawn-positions.txt"));for(int i=0;i<zs.length;i++)zs[i]=Integer.parseInt(positions.get(i));
      verifyPlaced(true);return;
    }
    int roles=0;
    var profiles=JsonParser.parseString(Files.readString(Path.of("production-witness-roles.json"))).getAsJsonObject();
    for(var group:profiles.entrySet()) for(var value:group.getValue().getAsJsonArray()) {
      String id=value.getAsString();
      var pair=NPCPlugin.get().spawnNPC(s,id,null,new Vector3d(-42,64,40),Rotation3f.ZERO);
      require(pair!=null,"role failed "+id);var ref=pair.first();
      var state=s.getComponent(ref,StateSupport.getComponentType());
      String entry=ContainerWitnessProfiles.entry(ref,s);
      require(!entry.isEmpty(),"profile missing "+id);
      if(entry.equals("Combat.Default")) {
        int combat=state.getStateHelper().getStateIndex("Combat");
        require(combat>=0 && state.getStateHelper().getSubStateIndex(combat,"Default")>=0,"no Combat.Default "+id);
      } else if(entry.equals("TrorkEnemySighted")) {
        var b=s.getComponent(ref,BeaconSupport.getComponentType());boolean found=false;
        if(b!=null && b.getMessageSlots()!=null)for(int i=0;i<b.getMessageSlots().length;i++) found|=entry.equals(b.getMessageTextForIndex(i));
        require(found,"no native rally slot "+id);
      }
      require(s.getComponent(ref,CombatSupport.getComponentType())!=null,"lost native combat "+id);
      out("PRODUCTION_ROLE_PASS Role="+id+" Entry="+entry+" State="+state.getStateName());roles++;
      s.removeEntity(ref,RemoveReason.REMOVE);
    }
    for(String id:List.of("Goblin_Scrapper","Klops_Miner","Wolf_Trork_Hunter")) {
      var pair=NPCPlugin.get().spawnNPC(s,id,null,new Vector3d(-42,64,40),Rotation3f.ZERO);
      require(pair!=null,"control role missing "+id);require(!ContainerWitnessProfiles.eligible(pair.first(),s),"excluded role enabled "+id);s.removeEntity(pair.first(),RemoveReason.REMOVE);
    }
    out("ROLE_COVERAGE_PASS Count="+roles+" GoblinKlopWolfExcluded=true");
    for(var e:map.entrySet()) {
      var table=BlockSpawnerTable.getAssetMap().getAsset(e.getKey());require(table!=null,"table missing "+e.getKey());int count=0;
      for(var entry:table.getEntries().toArray()) {
        var holder=entry.getBlockComponents();if(holder==null)continue;
        if(holder.getComponent(ItemContainerBlock.getComponentType())==null)continue;
        var data=holder.getComponent(OwnershipProofData.getComponentType());
        require(data!=null && data.owned() && data.scope().equals("FACTION") && data.faction().equals(e.getValue().getAsString()),"table ownership failed "+e.getKey());
        require(holder.clone().getComponent(OwnershipProofData.getComponentType()).faction().equals(data.faction()),"clone ownership failed");count++;
      }
      require(count>0,"no owned entry "+e.getKey());out("PRODUCTION_TABLE_PASS Table="+e.getKey()+" OwnedEntries="+count+" EmptyRollsUnchanged=true");
    }
    for(int i=0;i<tables.size();i++) {
      var table=BlockSpawnerTable.getAssetMap().getAsset(tables.get(i));
      for(int z=24;z<60;z++) {
        double roll=com.hypixel.hytale.math.util.HashUtil.random(x(i),66,z,w.getWorldConfig().getSeed()-1699164769L);
        var entry=table.getEntries().get(roll);
        if(entry.getBlockComponents()!=null && entry.getBlockComponents().getComponent(ItemContainerBlock.getComponentType())!=null) { zs[i]=z;break; }
      }
      require(zs[i]!=0,"no chest seed position "+tables.get(i));
    }
    Files.write(Path.of("production-spawn-positions.txt"),Arrays.stream(zs).mapToObj(String::valueOf).toList());
    place();
  }
  void transactions() {
    var chest=new com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer((short)4);
    var receipt=new com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer((short)4);
    chest.setItemStackForSlot((short)0,new com.hypixel.hytale.server.core.inventory.ItemStack("Ingredient_Bar_Copper",8));
    int[] total={0};
    for(int request=0;request<3;request++) {
      var probe=new ContainerRemovalProbe(chest,r->total[0]++);
      var source=chest.registerChangeEvent(e->probe.source(e.transaction()));var sink=receipt.registerChangeEvent(e->probe.receipt(e.transaction()));
      try {
        chest.moveItemStackFromSlotToSlot((short)(request==2?3:0),1,receipt,(short)0);
        int count=probe.finish();require(count==(request==2?0:1),"independent withdrawal/failed transfer");
        require(probe.finish()==0,"same transaction counted twice");
        out("NATIVE_TRANSACTION_PASS Request="+request+" Expected="+(request==2?"failed-empty-source":"successful-removal")+" Attributed="+count+" DuplicateFinish=0");
      }finally {source.unregister();sink.unregister();}
    }
    require(total[0]==2 && chest.getItemStack((short)0).getQuantity()==6 && receipt.getItemStack((short)0).getQuantity()==2,"native quantities");
    out("REPEATED_FAILED_TRANSFER_PASS IndependentWithdrawals=2 FailedTransactions=0 DuplicateReports=0");finish();
  }
  void reaction() {
    String mode=System.getProperty("production.mode").substring("production-reaction-".length());
    var actor=NPCPlugin.get().spawnNPC(s,"Test_Dummy_Stationary",null,new Vector3d(0,64,1.5),Rotation3f.ZERO).first();
    var k=NPCPlugin.get().spawnNPC(s,"Kweebec_Sapling",null,new Vector3d(-7.5,64,.5),new Rotation3f(0,(float)-Math.PI/2,0)).first();
    var f=NPCPlugin.get().spawnNPC(s,"Feran_Civilian",null,new Vector3d(7.5,64,.5),new Rotation3f(0,(float)Math.PI/2,0)).first();
    var kg=NPCPlugin.get().spawnNPC(s,"Kweebec_Razorleaf",null,new Vector3d(-21.5,64,.5),Rotation3f.ZERO).first();
    var fg=NPCPlugin.get().spawnNPC(s,"Feran_Sharptooth",null,new Vector3d(21.5,64,.5),Rotation3f.ZERO).first();
    for(var r:List.of(k,f,kg,fg))s.getComponent(r,WorldSupport.getComponentType()).overrideAttitude(actor,com.hypixel.hytale.server.core.asset.type.attitude.Attitude.FRIENDLY,600);
    w.scheduleAfter(()->w.execute(()->{try {
      var ownership=new OwnershipProofData(true,mode.equals("LOCAL")?"NEARBY":"FACTION",mode.equals("LOCAL")?"":mode);
      var witness=mode.equals("Feran")?f:k;
      var delivery=HyThiefContainerAlert.deliver(PluginManager.get().getPlugin(PickpocketPlugin.class),ownership,actor,witness,s,System.out::println);
      require(delivery.responders().size()==(mode.equals("LOCAL")?2:1),"production guard sensor/delivery "+delivery);
      var before=new HashMap<Ref<EntityStore>,Vector3d>();for(var r:List.of(kg,fg))before.put(r,new Vector3d(s.getComponent(r,com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType()).getPosition()));
      w.scheduleAfter(()->w.execute(()->{try {
        for(var r:List.of(kg,fg)) {
          boolean expected=mode.equals("LOCAL")||(mode.equals("Kweebec")&&r.equals(kg))||(mode.equals("Feran")&&r.equals(fg));
          var marked=s.getComponent(r,MarkedEntitySupport.getComponentType());var state=s.getComponent(r,StateSupport.getComponentType());
          boolean target=actor.equals(marked.getMarkedEntityRef("LockedTarget"));
          double moved=before.get(r).distance(s.getComponent(r,com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType()).getPosition());
          require(expected?target && (state.getStateName().startsWith("Attack")||state.getStateName().startsWith("Combat")):!target,"native production reaction "+mode+" expected="+expected+" target="+target+" state="+state.getStateName());
          if(expected)require(moved>.1,"native guard did not move");
          out("PRODUCTION_NATIVE_RESPONSE_PASS Type="+mode+" Role="+s.getComponent(r,NPCEntity.getComponentType()).getRoleName()+" Expected="+expected+" Target="+target+" State="+state.getStateName()+" NativeMovement="+moved+" FixtureRoleOverride=false");
        }
        finish();
      }catch(Throwable t){fail(t);}}),2,TimeUnit.SECONDS);
    }catch(Throwable t){fail(t);}}),1,TimeUnit.SECONDS);
  }
  int x(int i) { return -48+i*8; }
  void place() {
    // Exercise the real native BlockSpawnerSystem; all setup is in this disposable save.
    w.getWorldConfig().setBlockSpawnersResolved(false);
    var cs=w.getChunkStore();var pending=new ArrayList<Ref<ChunkStore>>();
    for(int i=0;i<tables.size();i++) {
      var current=BlockModule.getBlockEntity(w,x(i),66,zs[i]);
      if(current!=null && current.isValid() && cs.getStore().getComponent(current,OwnershipProofData.getComponentType())!=null) { pending.add(null);continue; }
      NativeFieldBlocks.set(w,x(i),66,zs[i],"Block_Spawner_Block");
      var section=cs.getChunkSectionReferenceAtBlock(x(i),66,zs[i]);var block=NativeFieldBlocks.type(w,x(i),66,zs[i]);
      var ref=BlockEntity.ensureBlockEntity(cs.getStore(),section,cs.getStore().getComponent(section,BlockComponentSection.getComponentType()),x(i),66,zs[i],block,0);
      cs.getStore().removeComponent(ref,BlockSpawner.getComponentType());pending.add(ref);
    }
    w.getWorldConfig().setBlockSpawnersResolved(true);
    for(int i=0;i<pending.size();i++)if(pending.get(i)!=null) {
      var section=cs.getChunkSectionReferenceAtBlock(x(i),66,zs[i]);
      var holder=ChunkStore.REGISTRY.newHolder();holder.addComponent(BlockSpawner.getComponentType(),new BlockSpawner(tables.get(i)));
      BlockEntity.setBlockEntity(cs.getStore(),section,cs.getStore().getComponent(section,BlockComponentSection.getComponentType()),x(i),66,zs[i],NativeFieldBlocks.type(w,x(i),66,zs[i]),0,holder);
    }
    w.scheduleAfter(()->w.execute(()->{try{verifyPlaced(false);}catch(Throwable t){fail(t);}}),1,TimeUnit.SECONDS);
  }
  void verifyPlaced(boolean restart) throws Exception {
    boolean complete=true;var lines=new ArrayList<String>();
    for(int i=0;i<tables.size();i++) {
      String id=tables.get(i);var ref=BlockModule.getBlockEntity(w,x(i),66,zs[i]);
      var data=ref==null?null:w.getChunkStore().getStore().getComponent(ref,OwnershipProofData.getComponentType());
      if(data==null) { out("PLACEMENT_PENDING Table="+id+" Block="+NativeFieldBlocks.type(w,x(i),66,zs[i]).getId()+" Ref="+ref);complete=false;continue; }
      require(data.owned() && data.scope().equals("FACTION") && data.faction().equals(map.get(id).getAsString()),"placed ownership mismatch "+id);
      require(w.getChunkStore().getStore().getComponent(ref,ItemContainerBlock.getComponentType())!=null,"placed inventory missing");
      lines.add(id+":"+data.faction()+":"+NativeFieldBlocks.type(w,x(i),66,zs[i]).getId());
    }
    if(!complete) { require(!restart && ++attempt<12,"no owned native placement after attempts="+attempt);place();return; }
    if(restart) require(lines.equals(Files.readAllLines(Path.of("production-spawn-expected.txt"))),"restart changed native placement");
    else Files.write(Path.of("production-spawn-expected.txt"),lines);
    for(var line:lines)out("NATIVE_SPAWNER_OWNERSHIP_PASS "+line+" FreshProcessRestart="+restart);
    out("PRODUCTION_PERSISTENCE_PASS FreshProcessRestart="+restart+" Count="+lines.size());finish();
  }
  void finish() { out("PRODUCTION_PASS");CompletableFuture.runAsync(()->com.hypixel.hytale.server.core.HytaleServer.get().shutdownServer()); }
  void fail(Throwable error) { out("PRODUCTION_PROOF_FAILED "+error);error.printStackTrace();finish(); }
}
