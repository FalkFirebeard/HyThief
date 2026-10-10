package com.local.pickpocket;

import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/** Ephemeral diagnostics only: one manual marker, plus isolated test-world fixtures. */
final class OwnedContainerRegistry {
  record Key(UUID world, int x, int y, int z) {}
  record Entry(Key key, ItemContainer container) {}
  private volatile Entry entry;
  record Automatic(World instance, List<Entry> entries) {}
  private final ConcurrentHashMap<UUID, Automatic> automatic = new ConcurrentHashMap<>();
  void replaceAutomatic(World world, List<Entry> entries) {
    if (entries.size() < 2 || entries.size() > 3 || entries.stream().map(Entry::key).distinct().count() != entries.size()
        || entries.stream().anyMatch(e -> !valid(world, e)))
      throw new IllegalStateException("Invalid automatic test containers");
    automatic.put(world.getWorldConfig().getUuid(), new Automatic(world, List.copyOf(entries)));
  }
  List<Entry> automatic(World world) {
    var set = automatic.get(world.getWorldConfig().getUuid());
    return set != null && set.instance() == world ? set.entries() : List.of();
  }
  void removeAutomatic(World world) {
    automatic.computeIfPresent(world.getWorldConfig().getUuid(), (id, set) -> set.instance() == world ? null : set);
  }
  void close() { entry = null; automatic.clear(); }
  Entry findAutomatic(World world, ItemContainer source, ItemContainer destination) {
    for (var candidate : automatic(world))
      if ((candidate.container() == source || candidate.container() == destination) && valid(world, candidate)) return candidate;
    return null;
  }
  Entry current() { return entry; }
  void clear() { entry = null; }
  Entry mark(World world, ContainerBlockWindow window) {
    var proposed = new Entry(new Key(world.getWorldConfig().getUuid(), window.getX(),
        window.getY(), window.getZ()), window.getItemContainer());
    if (!valid(world, proposed)) throw new IllegalStateException("Open window is not the live block container");
    if (entry != null && !entry.key().equals(proposed.key()))
      throw new IllegalStateException("One chest only: clear the existing marker first");
    entry = proposed;
    return proposed;
  }
  Entry mark(World world, int x, int y, int z) {
    var chunks = world.getChunkStore();
    var section = chunks.getChunkSectionReferenceAtBlock(x, y, z);
    if (section == null || !section.isValid()) throw new IllegalStateException("Chest chunk is not loaded");
    var block = BlockModule.getBlockEntity(chunks.getStore(), section, x, y, z);
    var component = block == null || !block.isValid() ? null
        : chunks.getStore().getComponent(block, ItemContainerBlock.getComponentType());
    if (component == null) throw new IllegalStateException("No native block container at that position");
    var proposed = new Entry(new Key(world.getWorldConfig().getUuid(), x, y, z), component.getItemContainer());
    if (entry != null && !entry.key().equals(proposed.key()))
      throw new IllegalStateException("One chest only: clear the existing marker first");
    entry = proposed; return proposed;
  }
  boolean valid(World world, Entry expected) {
    if (expected == null || expected.container() == null || !expected.key().world().equals(world.getWorldConfig().getUuid())) return false;
    var key = expected.key(); var chunks = world.getChunkStore();
    var section = chunks.getChunkSectionReferenceAtBlock(key.x(), key.y(), key.z());
    if (section == null || !section.isValid()) return false;
    var block = BlockModule.getBlockEntity(chunks.getStore(), section, key.x(), key.y(), key.z());
    if (block == null || !block.isValid()) return false;
    var container = chunks.getStore().getComponent(block, ItemContainerBlock.getComponentType());
    return container != null && container.getItemContainer() == expected.container();
  }
}
