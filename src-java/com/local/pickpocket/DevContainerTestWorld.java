package com.local.pickpocket;

import com.google.gson.*;
import com.hypixel.hytale.event.*;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.*;
import com.hypixel.hytale.server.core.universe.world.chunk.section.*;
import com.hypixel.hytale.math.util.ChunkUtil;
import java.nio.file.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Container diagnostic builds only. Coordinates live in the DevTesting save marker, not production policy. */
final class DevContainerTestWorld implements AutoCloseable {
  static final String NAME = "HyThief Container Theft Test World";
  record Station(int x, int y, int z, String block) {}
  record Fixture(World world, List<Station> stations, Station control, Station persistent) {}
  private final OwnedContainerRegistry registry;
  private final Consumer<String> log;
  private final Map<UUID, Fixture> fixtures = new ConcurrentHashMap<>();
  private final Map<UUID, String> errors = new ConcurrentHashMap<>();
  private final List<EventRegistration<?, ?>> events = new ArrayList<>();
  private volatile boolean closed;
  DevContainerTestWorld(PickpocketPlugin plugin, OwnedContainerRegistry registry, Consumer<String> log) {
    this.registry = registry; this.log = log;
    events.add(plugin.getEventRegistry().registerGlobal(StartWorldEvent.class, e -> load(e.getWorld())));
    events.add(plugin.getEventRegistry().registerGlobal(EventPriority.LAST, RemoveWorldEvent.class, e -> {
      if (!e.isCancelled()) remove(e.getWorld());
    }));
  }
  void start() {
    Universe.get().getUniverseReady().thenRun(() -> {
      if (!closed) Universe.get().getWorlds().values().forEach(this::load);
    });
  }
  boolean enabled(World world) {
    var f = fixtures.get(world.getWorldConfig().getUuid());
    return !closed && f != null && f.world() == world;
  }
  private void load(World world) {
    if (closed || !NAME.equals(world.getWorldConfig().getDisplayName()) || enabled(world)) return;
    try {
      Path save = world.getSavePath().toAbsolutePath().normalize();
      Path marker = save.resolve("../../../container-theft-test-world.json").normalize();
      var root = JsonParser.parseString(Files.readString(marker)).getAsJsonObject();
      if (!NAME.equals(root.get("Name").getAsString()) || !"Firebeard.HyThief.ContainerDevWorld".equals(root.get("Kind").getAsString())) return;
      var bytes = Base64.getDecoder().decode(root.getAsJsonObject("WorldUUID").get("$binary").getAsString());
      if (bytes.length != 16) throw new IllegalStateException("Invalid test world UUID");
      var buffer = ByteBuffer.wrap(bytes);var uuid = new UUID(buffer.getLong(), buffer.getLong());
      if (!uuid.equals(world.getWorldConfig().getUuid())) throw new IllegalStateException("Test marker UUID does not match loaded world");
      var config = root.getAsJsonObject("AutoContainerDiagnostics");
      if (config == null) return; // Old manually configured fixture remains a valid rollback.
      if (config.get("Version").getAsInt() != 1 || !"Kweebec".equals(config.get("Owner").getAsString())) throw new IllegalStateException("Invalid diagnostic fixture schema");
      var stations = new ArrayList<Station>();
      for (var item : config.getAsJsonArray("Owned")) stations.add(station(item.getAsJsonObject()));
      var control = station(config.getAsJsonObject("Control"));
      var persistent = config.has("PersistentOwned") ? station(config.getAsJsonObject("PersistentOwned")) : null;
      if(persistent!=null && (samePosition(persistent,control) || !stations.removeIf(s->samePosition(s,persistent))))
        throw new IllegalStateException("Persistent station must be an existing owned fixture, separate from control");
      if (stations.size() < 2 || stations.size() > 5 || stations.stream().map(s -> s.x()+","+s.y()+","+s.z()).distinct().count() != stations.size() || stations.stream().anyMatch(s -> samePosition(s, control))) throw new IllegalStateException("Expected two to five distinct owned stations and separate unowned control");
      var fixture = new Fixture(world, List.copyOf(stations), control, persistent);
      fixtures.put(uuid, fixture);registry.removeAutomatic(world);
      var loads = new ArrayList<CompletableFuture<?>>();
      var loadedStations=new ArrayList<>(stations); if(persistent!=null)loadedStations.add(persistent);
      for (var station : loadedStations)
        loads.add(world.getChunkStore().getChunkSectionReferenceAsync(ChunkUtil.chunkCoordinate(station.x()),ChunkUtil.chunkCoordinate(station.y()),ChunkUtil.chunkCoordinate(station.z())));
      CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).thenRun(() -> world.execute(() -> {
        if (!closed && fixtures.get(uuid) == fixture) {
          refresh(world);
          if(persistent!=null) {
            var block=BlockModule.getBlockEntity(world.getChunkStore().getStore(),world.getChunkStore().getChunkSectionReferenceAtBlock(persistent.x(),persistent.y(),persistent.z()),persistent.x(),persistent.y(),persistent.z());
            var section=world.getChunkStore().getChunkSectionReferenceAtBlock(persistent.x(),persistent.y(),persistent.z());
            var components=world.getChunkStore().getStore().getComponent(section,BlockComponentSection.getComponentType());
            var holder=block==null && components!=null ? components.getBlockHolder(ChunkUtil.indexBlock(persistent.x(),persistent.y(),persistent.z())) : null;
            var data=block!=null ? world.getChunkStore().getStore().getComponent(block,OwnershipProofData.getComponentType()) : holder==null?null:holder.getComponent(OwnershipProofData.getComponentType());
            log.accept("Action=PERSISTENT_LOAD Entity="+block+" ComponentFound="+(data!=null)+" Owned="+(data==null?false:data.owned())+" Scope="+(data==null?"MISSING":data.scope())+" Authority=serialized-component NoReattach=true");
          }
        }
      })).exceptionally(error -> { error(world, error.toString()); return null; });
    } catch (Exception error) { error(world, error.toString()); }
  }
  private void error(World world, String reason) {
    var previous = errors.put(world.getWorldConfig().getUuid(), reason);
    if (!reason.equals(previous)) log.accept("Action=AUTO_REGISTER_ERROR World="+world.getWorldConfig().getUuid()+" Reason="+reason);
  }
  private static boolean samePosition(Station a, Station b) { return a.x()==b.x() && a.y()==b.y() && a.z()==b.z(); }
  private static Station station(JsonObject object) {
    var p=object.getAsJsonArray("Position");
    if (p.size()!=3) throw new IllegalStateException("Invalid station coordinate");
    var s=new Station(p.get(0).getAsInt(),p.get(1).getAsInt(),p.get(2).getAsInt(),object.get("BlockId").getAsString());
    if(Math.abs((long)s.x())>1024 || Math.abs((long)s.z())>1024 || s.y()<0 || s.y()>255)throw new IllegalStateException("Fixture coordinate outside bounded test region");
    return s;
  }
  private static boolean nativeChestState(com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType type, String base) {
    var root = com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType.getAssetMap().getAsset(base);
    return root != null && (type.getId().equals(base)
        || sameState(type, root.getBlockForState("OpenWindow"))
        || sameState(type, root.getBlockForState("CloseWindow")));
  }
  private static boolean sameState(com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType type, com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType state) {
    return state != null && type.getId().equals(state.getId());
  }
  // Called only on load and inventory requests, on the owning world thread. No tick polling/listeners on arbitrary inventories.
  void refresh(World world) {
    if (!enabled(world)) return;
    var fixture=fixtures.get(world.getWorldConfig().getUuid());
    var current=registry.automatic(world);
    if(current.size()==fixture.stations().size() && current.stream().allMatch(e->registry.valid(world,e)))return;
    registry.removeAutomatic(world);
    try {
      var entries=new ArrayList<OwnedContainerRegistry.Entry>();
      for(var station:fixture.stations()) {
        var chunks=world.getChunkStore();var section=chunks.getChunkSectionReferenceAtBlock(station.x(),station.y(),station.z());
        if(section==null || !section.isValid())return; // Never force a packet to wait for unloaded terrain.
        var type=world.getBlockType(station.x(),station.y(),station.z());
        if(!nativeChestState(type, station.block()) || !BlockEntity.declaresComponent(type,ItemContainerBlock.getComponentType()))throw new IllegalStateException("Unexpected live fixture block at "+station);
        var store=chunks.getStore();var components=store.ensureAndGetComponent(section,BlockComponentSection.getComponentType());
        var blocks=store.getComponent(section,BlockSection.getComponentType());
        var block=BlockEntity.ensureBlockEntity(store,section,components,station.x(),station.y(),station.z(),type,blocks.getRotationIndex(station.x(),station.y(),station.z()));
        var container=store.getComponent(block,ItemContainerBlock.getComponentType());
        entries.add(new OwnedContainerRegistry.Entry(new OwnedContainerRegistry.Key(world.getWorldConfig().getUuid(),station.x(),station.y(),station.z()),container.getItemContainer()));
      }
      registry.replaceAutomatic(world,entries);
      errors.remove(world.getWorldConfig().getUuid());
      log.accept("Action=AUTO_REGISTER Owner=Kweebec World="+world.getWorldConfig().getUuid()+" Owned="+entries.stream().map(e->e.key().toString()).toList()+" Control=UNOWNED");
    } catch (RuntimeException error) { error(world, error.toString()); }
  }
  boolean persistentPosition(World world,int x,int y,int z) {
    var f=fixtures.get(world.getWorldConfig().getUuid());var s=f==null?null:f.persistent();
    return s!=null && f.world()==world && s.x()==x && s.y()==y && s.z()==z;
  }
  boolean controlPosition(World world,int x,int y,int z) {
    var f=fixtures.get(world.getWorldConfig().getUuid());var s=f==null?null:f.control();
    return s!=null && f.world()==world && s.x()==x && s.y()==y && s.z()==z;
  }
  boolean isControl(World world, com.hypixel.hytale.server.core.inventory.container.ItemContainer source) {
    var fixture = fixtures.get(world.getWorldConfig().getUuid());
    if (fixture == null || fixture.world() != world) return false;
    var c = fixture.control();
    var live = BlockModule.getComponent(ItemContainerBlock.getComponentType(), world, c.x(), c.y(), c.z());
    return live != null && live.getItemContainer() == source;
  }
  void remove(World world) {
    fixtures.computeIfPresent(world.getWorldConfig().getUuid(),(id,f)->f.world()==world?null:f);
    registry.removeAutomatic(world);
    errors.remove(world.getWorldConfig().getUuid());
  }
  public void close() {
    closed=true;for(var e:events)e.unregister();fixtures.values().forEach(f->registry.removeAutomatic(f.world()));fixtures.clear();errors.clear();
  }
}
