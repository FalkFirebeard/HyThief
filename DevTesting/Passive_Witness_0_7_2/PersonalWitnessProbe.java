package com.local.pickpocket;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.plugin.*;
import com.hypixel.hytale.server.core.universe.*;
import com.hypixel.hytale.server.core.universe.world.*;
import com.hypixel.hytale.server.core.universe.world.storage.*;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entitystats.*;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.block.*;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.inventory.*;
import com.hypixel.hytale.server.core.inventory.container.*;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.npc.*;
import com.hypixel.hytale.server.npc.entities.*;
import com.hypixel.hytale.server.npc.role.support.*;
import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3d;
import java.util.*;import java.util.concurrent.*;import java.nio.file.*;

/** Disposable integrated proof. NPC surrogate, not a connected client or packet replay. */
public final class PersonalWitnessProbe extends JavaPlugin {
 public PersonalWitnessProbe(JavaPluginInit init){super(init);}
 World w;Store<EntityStore>s;Ref<EntityStore>actor,witness,peer;ItemContainer chest;
 String role=System.getProperty("personal.role","Goblin_Scrapper");int ticks;boolean combat,secondary;float healthBefore;
 List<String> lines=new ArrayList<>();Map<String,String>last=new HashMap<>();
 void out(String x){lines.add(x);System.out.println("PERSONAL "+x);}
 static void require(boolean ok,String why){if(!ok)throw new IllegalStateException(why);}
 protected void start(){Universe.get().getUniverseReady().thenRun(()->{w=Universe.get().getDefaultWorld();var loads=new ArrayList<CompletableFuture<?>>();for(int x=-1;x<=0;x++)for(int z=-1;z<=1;z++)for(int y=1;y<=2;y++)loads.add(w.getChunkStore().getChunkSectionReferenceAsync(x,y,z,GetChunkFlags.SET_TICKING));CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).thenRun(()->w.execute(()->{try{if(Boolean.getBoolean("personal.author"))author();else if(Boolean.getBoolean("personal.fixtureverify"))verifyFixture();else init();}catch(Throwable e){fail(e);}}));});}
 Ref<EntityStore>spawn(String id,double x,double z){var pair=NPCPlugin.get().spawnNPC(s,id,null,new Vector3d(x,64,z),Rotation3f.ZERO);require(pair!=null,"spawn "+id);return pair.first();}
 void pose(Ref<EntityStore>ref,double x,double z,float yaw){s.getComponent(ref,TransformComponent.getComponentType()).setPosition(new Vector3d(x,64,z));s.getComponent(ref,HeadRotation.getComponentType()).getRotation().setYaw(yaw);}
 void init(){s=w.getEntityStore().getStore();var old=new ArrayList<Ref<EntityStore>>();s.forEachChunk(NPCEntity.getComponentType(),(c,b)->{for(int i=0;i<c.size();i++)old.add(c.getReferenceTo(i));});for(var r:old)s.removeEntity(r,RemoveReason.REMOVE);
  actor=spawn("Test_Dummy_Stationary",0,1.5);witness=spawn(role,0,9.5);peer=spawn(role,12,9.5);
  var movement=new MovementStatesComponent();movement.setMovementStates(new com.hypixel.hytale.protocol.MovementStates());s.putComponent(actor,MovementStatesComponent.getComponentType(),movement);
  for(var r:List.of(witness,peer)){var ws=s.getComponent(r,WorldSupport.getComponentType());require(ws.getDefaultPlayerAttitude()==Attitude.FRIENDLY,"native Friendly config not applied "+role);ws.overrideAttitude(actor,Attitude.FRIENDLY,600);require(s.getComponent(r,CombatSupport.getComponentType())!=null,"combat removed");}
  chest=BlockModule.getComponent(ItemContainerBlock.getComponentType(),w,0,64,2).getItemContainer();require(chest!=null,"native owned chest missing");
  out("PASSIVE_CONFIG Role="+role+" DefaultPlayer=FRIENDLY NormalAI=true CombatSupport=true");schedule();
 }
 void author()throws Exception{
  s=w.getEntityStore().getStore();var old=new HashSet<Ref<EntityStore>>();s.forEachChunk(NPCEntity.getComponentType(),(c,b)->{for(int i=0;i<c.size();i++)old.add(c.getReferenceTo(i));});s.forEachChunk(com.hypixel.hytale.server.core.entity.nameplate.Nameplate.getComponentType(),(c,b)->{for(int i=0;i<c.size();i++)old.add(c.getReferenceTo(i));});for(var r:old)s.removeEntity(r,RemoveReason.REMOVE);
  for(int x=-24;x<=24;x++)for(int z=-16;z<=24;z++){NativeFieldBlocks.set(w,x,63,z,"Rock_Stone_Cobble");for(int y=64;y<=70;y++)NativeFieldBlocks.set(w,x,y,z,"Empty");}
  witness=spawn(role,0,9.5);peer=spawn(role,24,9.5);pose(peer,24,9.5,(float)Math.PI);
  for(var r:List.of(witness,peer)){require(s.getComponent(r,WorldSupport.getComponentType()).getDefaultPlayerAttitude()==Attitude.FRIENDLY,"author passive config");s.putComponent(r,com.hypixel.hytale.server.core.entity.nameplate.Nameplate.getComponentType(),new com.hypixel.hytale.server.core.entity.nameplate.Nameplate((r.equals(witness)?"PERSONAL WITNESS - ":"NATIVE SECONDARY ONLY - ")+role));}
  for(int x:new int[]{-6,0}){
   NativeFieldBlocks.set(w,x,64,2,"Furniture_Kweebec_Chest_Large");var cs=w.getChunkStore();var section=cs.getChunkSectionReferenceAtBlock(x,64,2);var b=cs.getStore().getComponent(section,com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection.getComponentType());var block=BlockEntity.ensureBlockEntity(cs.getStore(),section,b,x,64,2,NativeFieldBlocks.type(w,x,64,2),0);var inv=cs.getStore().getComponent(block,ItemContainerBlock.getComponentType());if(inv.getItemContainer()==null)inv.setItemContainer(new SimpleItemContainer(inv.getCapacity()));inv.getItemContainer().setItemStackForSlot((short)0,new ItemStack("Ingredient_Bar_Copper",16));inv.getItemContainer().setItemStackForSlot((short)1,new ItemStack("Ingredient_Bar_Iron",8));cs.getStore().putComponent(block,OwnershipProofData.getComponentType(),x==0?owned():new OwnershipProofData(false,"",""));b.markNeedsSaving();b.markBlockNeedsSaving(com.hypixel.hytale.math.util.ChunkUtil.indexBlock(x,64,2));
   for(int dx=-2;dx<=1;dx++)for(int dz=0;dz<=1;dz++)NativeFieldBlocks.set(w,x+dx,63,dz,"Cloth_Block_Wool_"+(x==0?"Green":"Red"));label((x==0?"GREEN - OWNED / STANDING WITHDRAWAL = CAUGHT":"RED - UNOWNED CONTROL / SAFE"),x,66.5,2.5);
  }
  label("PASSIVE PERSONAL WITNESS PROOF - "+role,0,68,5);label("OPEN / INSERT = SAFE - STAND ON GREEN PAD TO WITHDRAW",0,67,5);
  for(var id:Universe.get().getPlayerStorage().getPlayers())Universe.get().getPlayerStorage().update(id,holder->holder.putComponent(TransformComponent.getComponentType(),new TransformComponent(new Vector3d(0,64.1,-1.5),new Rotation3f(0,(float)Math.PI,0)))).join();
  out("AUTHOR_PASS Role="+role+" Chests=2 NPCs=2 Passive=FRIENDLY Owned=NEARBY NativeChest=true Walls=0 StartBlocks=64");Files.write(Path.of("personal-evidence.txt"),lines);done();
 }
 void label(String text,double x,double y,double z){var h=EntityStore.REGISTRY.newHolder();h.addComponent(TransformComponent.getComponentType(),new TransformComponent(new Vector3d(x,y,z),new Rotation3f()));h.addComponent(com.hypixel.hytale.server.core.entity.nameplate.Nameplate.getComponentType(),new com.hypixel.hytale.server.core.entity.nameplate.Nameplate(text));h.addComponent(ModelComponent.getComponentType(),new ModelComponent(com.hypixel.hytale.server.core.asset.type.model.config.Model.createStaticScaledModel(com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset.getAssetMap().getAsset("Arrow_Iron"),.01f)));h.addComponent(com.hypixel.hytale.server.core.entity.Frozen.getComponentType(),com.hypixel.hytale.server.core.entity.Frozen.get());h.addComponent(com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType(),com.hypixel.hytale.server.core.entity.UUIDComponent.randomUUID());s.addEntity(h,AddReason.SPAWN);}
 void verifyFixture()throws Exception{
  s=w.getEntityStore().getStore();var npcs=new ArrayList<Ref<EntityStore>>();s.forEachChunk(NPCEntity.getComponentType(),(c,b)->{for(int i=0;i<c.size();i++)npcs.add(c.getReferenceTo(i));});require(npcs.size()==2,"fixture NPC count "+npcs.size());
  for(var r:npcs){require(role.equals(s.getComponent(r,NPCEntity.getComponentType()).getRoleName()),"wrong fixture role");require(s.getComponent(r,WorldSupport.getComponentType()).getDefaultPlayerAttitude()==Attitude.FRIENDLY,"fixture not friendly");require(s.getComponent(r,com.hypixel.hytale.server.core.entity.Frozen.getComponentType())==null,"fixture NPC frozen");}
  var positions=npcs.stream().map(r->s.getComponent(r,TransformComponent.getComponentType()).getPosition()).sorted(Comparator.comparingDouble(v->v.x)).toList();var pad=new Vector3d(0,64,1.5);require(positions.get(0).distance(pad)<20&&positions.get(1).distance(pad)>20&&positions.get(0).distance(positions.get(1))<30,"ambiguous fixture witness/secondary geometry");out("FIXTURE_GEOMETRY_PASS WitnessDistance="+positions.get(0).distance(pad)+" PeerDistance="+positions.get(1).distance(pad)+" NativeRallySeparation="+positions.get(0).distance(positions.get(1)));
  int count=0;for(int x=-26;x<=26;x++)for(int z=-16;z<=26;z++)for(int y=64;y<=70;y++){var type=NativeFieldBlocks.type(w,x,y,z);if(type.getId().contains("Chest")&&NativeFieldBlocks.filler(w,x,y,z)==0)count++;}
  require(count==2,"fixture chest count "+count);
  for(int x:new int[]{-6,0}){var block=BlockModule.getBlockEntity(w,x,64,2);var d=w.getChunkStore().getStore().getComponent(block,OwnershipProofData.getComponentType());require(d!=null&&d.owned()==(x==0)&&(!d.owned()||d.scope().equals("NEARBY")),"fixture ownership lost");var c=BlockModule.getComponent(ItemContainerBlock.getComponentType(),w,x,64,2).getItemContainer();require(c.getItemStack((short)0).getQuantity()==16,"fixture loot lost");}
  for(var id:Universe.get().getPlayerStorage().getPlayers()){var holder=Universe.get().getPlayerStorage().load(id).join();require(holder.getComponent(TransformComponent.getComponentType()).getPosition().distance(new Vector3d(0,64.1,-1.5))<.001,"fixture saved spawn");var blocks=holder.getComponent(InventoryComponent.Hotbar.getComponentType()).getInventory().getItemStack((short)0);require(blocks.getQuantity()==64&&blocks.getItemId().equals("Rock_Stone_Cobble"),"fixture starting blocks");}
  out("FIXTURE_RELOAD_PASS Role="+role+" NPCs=2 Chests=2 SerializedOwnership=true StartingBlocks=64 NativeFriendly=true");Files.write(Path.of("personal-evidence.txt"),lines);done();
 }
 void schedule(){w.scheduleAfter(()->w.execute(()->{try{tick();}catch(Throwable e){fail(e);}}),250,TimeUnit.MILLISECONDS);}
 void stance(boolean crouched){var states=new com.hypixel.hytale.protocol.MovementStates();states.crouching=crouched;s.getComponent(actor,MovementStatesComponent.getComponentType()).setMovementStates(states);}
 OwnershipProofData owned(){return new OwnershipProofData(true,"NEARBY","");}
 int withdrawal(String label,OwnershipProofData data,boolean expected){
  chest.setItemStackForSlot((short)0,new ItemStack("Ingredient_Bar_Copper",16));var receipts=new SimpleItemContainer((short)4);
  var confirmed=new ArrayList<ContainerVisualWitness.Evidence>();var snaps=new IdentityHashMap<ContainerRemovalProbe.Removal,List<ContainerVisualWitness.Evidence>>();
  var probe=new ContainerRemovalProbe(chest,r->confirmed.addAll(snaps.getOrDefault(r,List.of())),r->{if(PersistentContainerOwnership.eligible(data))snaps.put(r,ContainerVisualWitness.capture(actor,s,data,Set.of(role),this::out));});
  var a=chest.registerChangeEvent(e->probe.source(e.transaction()));var b=receipts.registerChangeEvent(e->probe.receipt(e.transaction()));
  try{chest.moveItemStackFromSlotToSlot((short)0,1,receipts,(short)0);require(probe.finish()==1,"withdraw pairing");}finally{a.unregister();b.unregister();}
  var messages=new ArrayList<com.hypixel.hytale.server.core.Message>();ContainerCaughtNotification.notifyWitnessed(confirmed,messages::add);
  require(messages.size()==(expected?1:0),label+" notifications "+messages.size());
  // The exact live adapter; duplicate evidence must not cause two deliveries.
  confirmed.addAll(new ArrayList<>(confirmed));int delivered=HyThiefPersonalWitnessAlert.deliver(data,actor,confirmed,s,this::out);
  require(delivered==(expected?1:0),label+" personal deliveries="+delivered);
  out("TRANSACTION_PASS Case="+label+" Notifications="+messages.size()+" PersonalDeliveries="+delivered);return delivered;
 }
 void negativeTransactions(){
  var incoming=new SimpleItemContainer((short)4);incoming.setItemStackForSlot((short)0,new ItemStack("Ingredient_Fibre",3));var p=new ContainerRemovalProbe(chest,r->{throw new IllegalStateException("false theft");},r->{});
  var a=chest.registerChangeEvent(e->p.source(e.transaction()));var b=incoming.registerChangeEvent(e->p.receipt(e.transaction()));
  try{incoming.moveItemStackFromSlotToSlot((short)0,3,chest,(short)5);chest.moveItemStackFromSlotToSlot((short)5,3,chest,(short)6);chest.moveItemStackFromSlotToSlot((short)19,1,incoming,(short)2);require(p.finish()==0,"noncrime transfer");}finally{a.unregister();b.unregister();}
  require("Open_Container".equals(NativeFieldBlocks.type(w,0,64,2).getInteractions().get(com.hypixel.hytale.protocol.InteractionType.Use)),"chest interaction changed");
  out("NONCRIME_PASS NativeOpenContainer=true BrowsingHasNoRemoval=true Deposit=true Rearrange=true FailedTransfer=true Notifications=0");
 }
 float health(){var map=s.getComponent(actor,EntityStatMap.getComponentType());var value=map==null?null:map.get(DefaultEntityStatTypes.getHealth());return value==null?Float.NaN:value.get();}
 void tick()throws Exception{ticks++;
  for(var r:List.of(witness,peer)){require(r.isValid(),"NPC invalid");var st=s.getComponent(r,StateSupport.getComponentType());var target=s.getComponent(r,MarkedEntitySupport.getComponentType());boolean same=actor.equals(target.getMarkedEntityRef("LockedTarget"));String state=st.getStateName();String value=(r.equals(witness)?"Witness":"Peer")+" State="+state+" TargetOffender="+same+" Position="+s.getComponent(r,TransformComponent.getComponentType()).getPosition();if(!value.equals(last.put(r.toString(),value))&&(ticks%4==0||ticks==17))out("T="+ticks*.25+" "+value);
   if(ticks<=16)require(!state.startsWith("Combat")&&!state.startsWith("Chase")&&s.getComponent(r,WorldSupport.getComponentType()).getAttitude(r,actor,s)!=Attitude.HOSTILE,"not passive "+value);
   if(ticks>16&&same&&(state.startsWith("Combat")||state.startsWith("Chase"))){if(r.equals(witness))combat=true;else secondary=true;}
  }
  if(ticks==16){out("PASSIVE_APPROACH_PASS Seconds=4 ActorHealth="+health());pose(witness,0,9.5,0);pose(peer,12,9.5,(float)Math.PI);pose(actor,0,1.5,0);stance(false);
   String faction=role.startsWith("Goblin")?"Goblin":role.startsWith("Trork")?"Trork":role.startsWith("Outlander")?"Outlander":role.startsWith("Skeleton")?"Skeleton":"Zombie";
   require(ContainerOwnershipPolicy.rejection(new OwnershipProofData(true,"FACTION",faction),witness,s)==null,"native faction match "+faction);out("FACTION_MEMBERSHIP_PASS Group="+faction);
   negativeTransactions();withdrawal("UNOWNED",new OwnershipProofData(false,"",""),false);
   withdrawal("FACTION_MISMATCH",new OwnershipProofData(true,"FACTION","Kweebec"),false);
   pose(witness,0,9.5,(float)Math.PI);withdrawal("REAR",owned(),false);pose(witness,0,9.5,0);
   for(int x=-1;x<=1;x++)for(int y=64;y<=68;y++)NativeFieldBlocks.set(w,x,y,5,"Rock_Stone_Cobble");withdrawal("BLOCKED_LOS",owned(),false);for(int x=-1;x<=1;x++)for(int y=64;y<=68;y++)NativeFieldBlocks.set(w,x,y,5,"Empty");
   stance(true);withdrawal("CROUCHED_8",owned(),false);stance(false);pose(actor,0,-12.5,0);withdrawal("STANDING_22",owned(),false);pose(actor,0,1.5,0);
   healthBefore=health();withdrawal("VISIBLE_8",owned(),true);
  }
  if(ticks>=112){require(combat,"native combat never acquired offender");out("RESULT Role="+role+" PersonalCombat="+combat+" NativeSecondary="+secondary+" ActorHealthBefore="+healthBefore+" ActorHealthAfter="+health()+" CustomBroadcast=false ConnectedClient=false");Files.write(Path.of("personal-evidence.txt"),lines);done();return;}schedule();
 }
 void fail(Throwable e){out("FAILED "+e);e.printStackTrace();try{Files.write(Path.of("personal-evidence.txt"),lines);}catch(Exception ignored){}done();}
 void done(){CompletableFuture.runAsync(()->com.hypixel.hytale.server.core.HytaleServer.get().shutdownServer());}
}
