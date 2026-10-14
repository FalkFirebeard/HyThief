package com.local.pickpocket;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.plugin.*;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.*;
import com.hypixel.hytale.server.core.universe.world.worldgen.*;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.*;
import com.hypixel.hytale.server.core.universe.world.storage.*;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.components.SpawnMarkerReference;
import com.hypixel.hytale.builtin.hytalegenerator.*;
import com.hypixel.hytale.builtin.hytalegenerator.props.*;
import com.hypixel.hytale.builtin.hytalegenerator.material.*;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.engine.entityfunnel.EntityFunnel;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.math.vector.*;
import org.joml.*;
import org.bson.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.LongPredicate;
import it.unimi.dsi.fastutil.longs.Long2FloatFunction;
public final class NativePrefabIdProof extends JavaPlugin {
 public NativePrefabIdProof(JavaPluginInit i){super(i);}
 static final int[] XS={-12,12}; static final String SOURCE="Server/Prefabs/Npc/Kweebec/Autumn/Shops/Kweebec_Autumn_Shops_001.prefab.json";
 protected void setup(){IWorldGenProvider.CODEC.register("NativePrefabIdProof",Provider.class,Provider.CODEC);}
 public static final class Provider implements IWorldGenProvider {
  public static final BuilderCodec<Provider> CODEC=BuilderCodec.builder(Provider.class,Provider::new).build();
  public IWorldGen getGenerator() throws WorldGenLoadException{return new Generator();}
 }
 static final class Generator implements IWorldGen {
  final IWorldGen flat; com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer retainedBuffer;
  Generator()throws WorldGenLoadException{flat=new FlatWorldGenProvider(new com.hypixel.hytale.protocol.Color((byte)91,(byte)158,(byte)40),new FlatWorldGenProvider.Layer[]{new FlatWorldGenProvider.Layer(0,64,"Env_Default_Flat","Rock_Stone_Cobble")}).getGenerator();}
  public WorldGenTimingsCollector getTimings(){return flat.getTimings();}
  public Transform[] getSpawnPoints(int seed){return new Transform[]{new Transform(new Vector3d(0,64.1,12),new Rotation3f())};}
  public CompletableFuture<GeneratedChunk> generate(int seed,long index,int cx,int cz,LongPredicate skip,Long2FloatFunction f){return flat.generate(seed,index,cx,cz,skip,f).thenApply(chunk->{synchronized(this){
   var cache=new MaterialCache();if(retainedBuffer==null)retainedBuffer=PrefabBufferUtil.getCached(Path.of("native-shop.prefab.json"));var buf=retainedBuffer;
   var space=new VoxelSpace<Material>() {
    final Bounds3i bounds=new Bounds3i(new Vector3i(cx*32,0,cz*32),new Vector3i(cx*32+32,320,cz*32+32));
    public Bounds3i getBounds(){return bounds;}
    public Material get(int x,int y,int z){return cache.EMPTY;}
    public Material get(Vector3i v){return get(v.x,v.y,v.z);}
    public void setAll(Material m){throw new UnsupportedOperationException();}
    public void set(Material m,Vector3i v){set(m,v.x,v.y,v.z);}
    public void set(Material m,int x,int y,int z){if(!bounds.contains(x,y,z))throw new IllegalStateException("write outside chunk");var b=m.solid();if(m.fluid().fluidId!=0)throw new IllegalStateException("unexpected fluid");chunk.getBlockChunk().setBlock(x&31,y,z&31,b.blockId,b.rotation,b.filler);if(b.holder!=null){chunk.setState(x&31,y,z&31,b.holder.clone());System.out.println("PREFAB_ID_BLOCK_INPUT Copy="+(x<0?"A":"B")+" Position="+x+","+y+","+z+" NativeHolder="+ChunkStore.REGISTRY.serialize(b.holder).toJson());}}
   };
   EntityFunnel funnel=new EntityFunnel(){public Bounds3i getBounds(){return space.getBounds();}public void addEntity(EntityPlacementData d){chunk.getEntityChunk().addEntities(d.getOffset(),d.getRotation(),new Holder[]{d.getEntityHolder()},0,d.getPrefabInstanceId());var pos=d.getEntityHolder().getComponent(TransformComponent.getComponentType()).getPosition();System.out.println("PREFAB_ID_WORLDGEN Copy="+(pos.x<0?"A":"B")+" Id="+d.getPrefabInstanceId()+" EntityPosition="+pos+" Source="+SOURCE);}};
   for(int x:XS){var pool=new WeightedMap<List<com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer>>();pool.add(List.of(buf),1);var prop=new PrefabProp(pool,cache,new SeedBox(seed),name->List.of());var context=new Prop.Context(new Vector3i(x,64,0),space,space,funnel,100,new Vector3d(x,64,0),null);prop.generate(context);}
   return chunk;
  }});}
  public void shutdown(){flat.shutdown();}
 }
 String previous="";boolean labelled=false;int count=0;
 protected void start(){Universe.get().getUniverseReady().thenRun(()->{var w=Universe.get().getDefaultWorld();var loads=new ArrayList<CompletableFuture<?>>();for(int x=-1;x<=0;x++)for(int z=-1;z<=0;z++)for(int y=1;y<=2;y++)loads.add(w.getChunkStore().getChunkSectionReferenceAsync(x,y,z,com.hypixel.hytale.server.core.universe.world.storage.GetChunkFlags.SET_TICKING));CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).thenRun(()->w.scheduleAfter(()->w.execute(()->poll(w)),6,TimeUnit.SECONDS));});}
 static String copy(double x){return x<0?"A":"B";}
 static Object id(Store<EntityStore> st,Ref<EntityStore> r){if(r==null||!r.isValid())return "unavailable";var c=st.getComponent(r,FromPrefabInstance.getComponentType());return c==null?"absent":c.getPrefabInstanceId();}
 void poll(World w){try{
  var st=w.getEntityStore().getStore();var markers=new ArrayList<Ref<EntityStore>>();st.forEachChunk(SpawnMarkerEntity.getComponentType(),(c,b)->{for(int n=0;n<c.size();n++)markers.add(c.getReferenceTo(n));});
  if(Boolean.getBoolean("prefabproof.automated"))for(var r:markers){var m=st.getComponent(r,SpawnMarkerEntity.getComponentType());var already=new boolean[]{false};var markerUuid=st.getComponent(r,UUIDComponent.getComponentType()).getUuid();st.forEachChunk(SpawnMarkerReference.getComponentType(),(cc,bb)->{for(int nn=0;nn<cc.size();nn++){var rr=cc.getReferenceTo(nn);if(markerUuid.equals(st.getComponent(rr,SpawnMarkerReference.getComponentType()).getReference().getUuid()))already[0]=true;}});if(!already[0]){boolean ok=m.spawnNPC(r,m.getCachedMarker(),new ArrayList<>(),st);System.out.println("PREFAB_ID_NATIVE_MARKER_SPAWN Copy="+copy(st.getComponent(r,TransformComponent.getComponentType()).getPosition().x)+" Success="+ok);}}
  var rows=new BsonArray();StringBuilder signature=new StringBuilder();
  for(var r:markers){var pos=st.getComponent(r,TransformComponent.getComponentType()).getPosition();var uuid=st.getComponent(r,UUIDComponent.getComponentType()).getUuid();var nativeId=id(st,r);var row=new BsonDocument().append("copy",new BsonString(copy(pos.x))).append("kind",new BsonString("MARKER")).append("uuid",new BsonString(uuid.toString())).append("nativeId",new BsonString(""+nativeId));rows.add(row);signature.append(uuid).append(nativeId);Files.createDirectories(Path.of("prefab-id-diagnostics"));Files.writeString(Path.of("prefab-id-diagnostics/marker-"+copy(pos.x)+".json"),EntityStore.REGISTRY.serialize(st.copySerializableEntity(r)).toJson());}
  st.forEachChunk(NPCEntity.getComponentType(),(c,b)->{for(int n=0;n<c.size();n++){var r=c.getReferenceTo(n);var npc=st.getComponent(r,NPCEntity.getComponentType());var mr=st.getComponent(r,SpawnMarkerReference.getComponentType());var marker=mr==null?null:mr.getReference().getEntity(st);var pos=st.getComponent(r,TransformComponent.getComponentType()).getPosition();var uuid=st.getComponent(r,UUIDComponent.getComponentType()).getUuid();var row=new BsonDocument().append("copy",new BsonString(copy(pos.x))).append("kind",new BsonString("NPC")).append("role",new BsonString(npc.getRoleName())).append("uuid",new BsonString(uuid.toString())).append("directId",new BsonString(""+id(st,r))).append("markerUuid",new BsonString(mr==null?"absent":""+mr.getReference().getUuid())).append("markerId",new BsonString(""+id(st,marker)));rows.add(row);signature.append(uuid).append(id(st,marker));}});
  for(int x:XS){int bx=x+2,by=65,bz=1;var r=BlockModule.getBlockEntity(w,bx,by,bz);var inv=BlockModule.getComponent(ItemContainerBlock.getComponentType(),w,bx,by,bz);String type=NativeFieldBlocks.type(w,bx,by,bz).getId();var row=new BsonDocument().append("copy",new BsonString(copy(x))).append("kind",new BsonString("CHEST_SLOT")).append("position",new BsonString(bx+","+by+","+bz)).append("block",new BsonString(type)).append("hasContainer",new BsonBoolean(inv!=null));if(r!=null){var data=ChunkStore.REGISTRY.serialize(w.getChunkStore().getStore().copySerializableEntity(r));Files.writeString(Path.of("prefab-id-diagnostics/chest-"+copy(x)+".json"),data.toJson());row.append("components",data).append("runtimeArchetype",new BsonString(""+w.getChunkStore().getStore().copyEntity(r).getArchetype())).append("runtimeRef",new BsonString(""+r));}rows.add(row);signature.append(type).append(inv!=null);}
  if(!labelled){st.forEachChunk(Nameplate.getComponentType(),(c,b)->{for(int n=0;n<c.size();n++)if(c.getComponent(n,Nameplate.getComponentType()).getText().equals("PREFAB A"))labelled=true;});}if(!labelled){for(int x:XS)label(st,"PREFAB "+copy(x),x,68,5);labelled=true;}
  if(!previous.equals(signature.toString())){previous=signature.toString();var document=new BsonDocument().append("phase",new BsonString(System.getProperty("prefabproof.phase","client"))).append("source",new BsonString(SOURCE)).append("records",rows);Files.writeString(Path.of("prefab-id-diagnostics/snapshot-"+System.getProperty("prefabproof.phase","client")+"-"+System.currentTimeMillis()+"-"+(++count)+".json"),document.toJson());for(var value:rows){var row=value.asDocument();System.out.println("PREFAB "+row.getString("copy").getValue()+" "+row.getString("kind").getValue()+" "+(row.getString("kind").getValue().equals("CHEST_SLOT")?"position="+row.getString("position").getValue()+" block="+row.getString("block").getValue()+" hasContainer="+row.getBoolean("hasContainer").getValue()+" components="+(row.containsKey("components")?row.getDocument("components").getDocument("Components").keySet():"none"):row.toJson()));}System.out.println("PREFAB_ID_SNAPSHOT_WRITTEN phase="+System.getProperty("prefabproof.phase","client"));}
  if(Boolean.getBoolean("prefabproof.automated")){System.out.println("PREFAB_ID_NATIVE_PROOF_COMPLETE");CompletableFuture.runAsync(()->com.hypixel.hytale.server.core.HytaleServer.get().shutdownServer());}else w.scheduleAfter(()->w.execute(()->poll(w)),10,TimeUnit.SECONDS);
 }catch(Throwable t){t.printStackTrace();System.err.println("PREFAB_ID_PROOF_FAILED");if(Boolean.getBoolean("prefabproof.automated"))CompletableFuture.runAsync(()->com.hypixel.hytale.server.core.HytaleServer.get().shutdownServer());}}
 static void label(Store<EntityStore> st,String text,int x,int y,int z){var h=EntityStore.REGISTRY.newHolder();h.addComponent(TransformComponent.getComponentType(),new TransformComponent(new Vector3d(x,y,z),new Rotation3f()));h.addComponent(Nameplate.getComponentType(),new Nameplate(text));h.addComponent(Frozen.getComponentType(),Frozen.get());h.addComponent(ModelComponent.getComponentType(),new ModelComponent(com.hypixel.hytale.server.core.asset.type.model.config.Model.createStaticScaledModel(com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset.getAssetMap().getAsset("Arrow_Iron"),.01f)));h.addComponent(UUIDComponent.getComponentType(),UUIDComponent.randomUUID());st.addEntity(h,AddReason.SPAWN);}
}
