package com.firebeard.diagnostics;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.plugin.*;
import com.hypixel.hytale.server.core.universe.*;
import com.hypixel.hytale.server.core.universe.world.*;
import com.hypixel.hytale.server.core.universe.world.storage.*;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.npc.*;
import com.hypixel.hytale.server.npc.entities.*;
import com.hypixel.hytale.server.npc.components.messaging.*;
import com.hypixel.hytale.server.npc.role.support.*;
import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3d;
import java.util.*;import java.util.concurrent.*;import java.nio.file.*;
/** Isolated native social test. Never loaded into a user world or public/development mod. */
public final class SocialAlertProbe extends JavaPlugin {
 public SocialAlertProbe(JavaPluginInit init){super(init);}
 World w;Store<EntityStore> s;Ref<EntityStore> actor;List<Ref<EntityStore>> members=new ArrayList<>();Map<String,String> last=new HashMap<>();List<String> lines=new ArrayList<>();String role=System.getProperty("social.role","Goblin_Scrapper");String mode=System.getProperty("social.mode","acquire");int ticks;boolean fired;
 protected void start(){Universe.get().getUniverseReady().thenRun(()->{w=Universe.get().getDefaultWorld();var loads=new ArrayList<CompletableFuture<?>>();for(int x=-1;x<=2;x++)for(int z=-1;z<=0;z++)for(int y=1;y<=2;y++)loads.add(w.getChunkStore().getChunkSectionReferenceAsync(x,y,z,GetChunkFlags.SET_TICKING));CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).thenRun(()->w.execute(()->{try{init();}catch(Throwable e){fail(e);}}));});}
 void out(String v){String t="SOCIAL "+v;lines.add(t);System.out.println(t);}
 Ref<EntityStore> spawn(String name,double x,double z){var p=NPCPlugin.get().spawnNPC(s,name,null,new Vector3d(x,64,z),Rotation3f.ZERO);if(p==null)throw new IllegalStateException("spawn failed "+name);return p.first();}
 void init(){s=w.getEntityStore().getStore();var old=new ArrayList<Ref<EntityStore>>();s.forEachChunk(NPCEntity.getComponentType(),(c,b)->{for(int i=0;i<c.size();i++)old.add(c.getReferenceTo(i));});for(var r:old)s.removeEntity(r,RemoveReason.REMOVE);
  if(Boolean.getBoolean("social.configcheck")){for(String roleName:List.of("Goblin_Scrapper","Trork_Warrior","Outlander_Brute","Skeleton_Fighter","Zombie")){var r=spawn(roleName,0,8);var ws=s.getComponent(r,WorldSupport.getComponentType());if(ws.getDefaultPlayerAttitude()!=Attitude.FRIENDLY)throw new IllegalStateException("friendly setting not applied "+roleName);out("CONFIG_PASS Role="+roleName+" DefaultPlayer="+ws.getDefaultPlayerAttitude()+" RootInstruction="+(s.getComponent(r,NPCEntity.getComponentType()).getRole().getRootInstruction()!=null)+" CombatSupport="+(s.getComponent(r,CombatSupport.getComponentType())!=null));s.removeEntity(r,RemoveReason.REMOVE);}out("COMPLETE CONFIG");try{Files.write(Path.of("social-evidence.txt"),lines);}catch(Exception e){throw new RuntimeException(e);}done();return;}
  actor=spawn("Test_Dummy_Stationary",0,0);
  for(double x:new double[]{0,10,18,52}){var r=spawn(role,x,8);members.add(r);s.getComponent(r,DebugSupport.getComponentType()).setDebugFlags(EnumSet.of(com.hypixel.hytale.server.npc.role.RoleDebugFlags.BeaconMessages));var ws=s.getComponent(r,WorldSupport.getComponentType());ws.overrideAttitude(actor,Attitude.FRIENDLY,600);out("SPAWN Role="+role+" X="+x+" DefaultPlayer="+ws.getDefaultPlayerAttitude()+" AttitudeGroupIndex="+ws.getAttitudeGroup()+" DefaultNPC="+ws.getDefaultNPCAttitude()+" ActorAttitude="+ws.getAttitude(r,actor,s));}
  if(mode.startsWith("flock")){var flock=com.hypixel.hytale.server.flock.FlockPlugin.createFlock(s,s.getComponent(members.get(0),NPCEntity.getComponentType()).getRole());for(int i=0;i<3;i++){boolean ok=com.hypixel.hytale.server.flock.FlockMembershipSystems.canJoinFlock(members.get(i),flock,s);out("FLOCK_SETUP Member="+i+" CanJoin="+ok);if(ok)com.hypixel.hytale.server.flock.FlockMembershipSystems.join(members.get(i),flock,s);}}
  w.scheduleAfter(()->w.execute(()->tick()),250,TimeUnit.MILLISECONDS);
 }
 void tick(){try{ticks++;for(int i=0;i<members.size();i++){var r=members.get(i);if(!r.isValid()){out("INVALID Member="+i);continue;}var ws=s.getComponent(r,WorldSupport.getComponentType());var st=s.getComponent(r,StateSupport.getComponentType());var m=s.getComponent(r,MarkedEntitySupport.getComponentType());var b=s.getComponent(r,BeaconSupport.getComponentType());String q="";if(b!=null&&b.getMessageSlots()!=null)for(int k=0;k<b.getMessageSlots().length;k++)if(b.isMessageQueued(k))q+=b.getMessageTextForIndex(k)+":"+actor.equals(b.peekMessage(k))+",";
  String v="Role="+role+" Member="+i+" State="+st.getStateName()+" TargetActor="+Arrays.asList(m.getEntityTargets()).contains(actor)+" Attitude="+ws.getAttitude(r,actor,s)+" Queued="+q; if(!v.equals(last.put("m"+i,v)))out("T="+(ticks*.25)+" "+v);}
 if(ticks==16){out("PASSIVE_WINDOW_COMPLETE Seconds=4");var source=members.get(0);if(mode.equals("beacon")){String signal=role.startsWith("Goblin")?"Goblin_Attack":role.startsWith("Skeleton")?"Flock_Attack":role.equals("Zombie")?"Flock_Attack":"TrorkEnemySighted";BeaconBroadcast.post(source,signal,actor,1,s);out("TRIGGER SourceOnly=true Kind=NativeBeaconPost Signal="+signal+" ForcedState=false ForcedTarget=false");}else{s.getComponent(source,WorldSupport.getComponentType()).overrideAttitude(actor,Attitude.HOSTILE,600);out("TRIGGER SourceOnly=true Kind=NativePerEntityHostileOverride ForcedState=false ForcedTarget=false");}fired=true;}
 if(ticks>=112){for(int i=0;i<members.size();i++){var r=members.get(i);if(r.isValid())out("FINAL Member="+i+" State="+s.getComponent(r,StateSupport.getComponentType()).getStateName()+" TargetActor="+Arrays.asList(s.getComponent(r,MarkedEntitySupport.getComponentType()).getEntityTargets()).contains(actor)+" Position="+s.getComponent(r,TransformComponent.getComponentType()).getPosition());}out("COMPLETE Role="+role);Files.write(Path.of("social-evidence.txt"),lines);done();return;}
  w.scheduleAfter(()->w.execute(()->tick()),250,TimeUnit.MILLISECONDS);
 }catch(Throwable e){fail(e);}}
 void fail(Throwable e){out("FAILED "+e);e.printStackTrace();done();}
 void done(){CompletableFuture.runAsync(()->com.hypixel.hytale.server.core.HytaleServer.get().shutdownServer());}
}
