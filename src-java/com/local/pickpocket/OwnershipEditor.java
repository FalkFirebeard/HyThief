package com.local.pickpocket;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.Objects;
import org.joml.Vector3i;

/** Creator-only, world-thread ownership edits. Never replaces storage or its block. */
public final class OwnershipEditor {
  public static final String PERMISSION = "hythief.ownership.edit";
  public static final double REACH = 8;
  // Native groups represented by ContainerWitnessProfiles and ContainerOwnershipPolicy.
  private static final List<String> GROUPS = List.of("Kweebec", "Feran", "Trork", "Outlander", "Skeleton", "Zombie");
  private OwnershipEditor() {}
  public static List<String> factions() {
    return GROUPS.stream().filter(g -> NPCGroup.getAssetMap().getIndex(g) >= 0).toList();
  }
  public static Vector3i target(Ref<EntityStore> actor, Store<EntityStore> store) {
    // In stable 0.7.2 this predicate means continue traversal, not accept hit.
    return com.hypixel.hytale.server.core.util.TargetUtil.getTargetBlockOrigin(actor, id -> id == 0, REACH, store);
  }
  /** Shared world-thread entry. Opening is read-only; Apply revalidates independently. */
  public static void open(Ref<EntityStore> actor, Store<EntityStore> store, PlayerRef player) {
    if (!player.hasPermission(PERMISSION))
      throw new IllegalArgumentException("You do not have permission to edit ownership.");
    if (actor == null || !actor.isValid() || actor.getStore() != store
        || !actor.equals(player.getReference()))
      throw new IllegalArgumentException("The editor's player or world changed. Reopen the editor.");
    var entity = store.getComponent(actor, Player.getComponentType());
    if (entity == null) throw new IllegalArgumentException("The editor's player is unavailable.");
    // Queued activations must not replace a page or discard unsaved edits.
    if (entity.getPageManager().getCustomPage() instanceof OwnershipEditorPage) return;
    var selection = select(store.getExternalData().getWorld(), target(actor, store));
    entity.getPageManager().openCustomPage(actor, store, new OwnershipEditorPage(player, selection));
  }
  public record Value(boolean owned, String scope, String faction) {
    static Value read(OwnershipProofData data) {
      return data == null ? null : new Value(data.owned(), data.scope(), data.faction());
    }
  }
  public record Selection(World world, int x, int y, int z, Ref<ChunkStore> entity,
      ItemContainer inventory, int block, int rotation, Value original) {
    public String description() {
      var type = com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType.getAssetMap().getAsset(block);
      return type.getId() + " @ " + x + ", " + y + ", " + z;
    }
  }
  public static Selection select(World world, Vector3i p) {
    if (p == null) throw new IllegalArgumentException("Look at a storage container within 8 blocks.");
    var cs = world.getChunkStore(); var store = cs.getStore();
    var section = cs.getChunkSectionReferenceAtBlock(p.x, p.y, p.z);
    if (section == null || !section.isValid()) throw new IllegalArgumentException("Target section is not loaded.");
    var blocks = store.getComponent(section, BlockSection.getComponentType());
    var entity = BlockModule.getBlockEntity(store, section, p.x, p.y, p.z);
    if (blocks == null || entity == null || !entity.isValid()) throw new IllegalArgumentException("This block is not supported storage.");
    var storage = store.getComponent(entity, ItemContainerBlock.getComponentType());
    if (storage == null || storage.getItemContainer() == null) throw new IllegalArgumentException("This block is not supported storage.");
    return new Selection(world, p.x, p.y, p.z, entity, storage.getItemContainer(), blocks.get(p.x,p.y,p.z),
        blocks.getRotationIndex(p.x,p.y,p.z), Value.read(store.getComponent(entity, OwnershipProofData.getComponentType())));
  }
  /** Runs atomically on the same world thread as native block and inventory operations. */
  public static boolean apply(Selection selected, Ref<EntityStore> actor, Store<EntityStore> actors,
      PlayerRef player, boolean owned, String scope, String faction) {
    if (!player.hasPermission(PERMISSION)) throw new IllegalArgumentException("You do not have permission to edit ownership.");
    if (actor == null || !actor.isValid() || actor.getStore() != actors || player.getReference() == null
        || !actor.equals(player.getReference()) || selected.world().getEntityStore().getStore() != actors)
      throw new IllegalArgumentException("The editor's player or world changed. Reopen the editor.");
    var transform = actors.getComponent(actor, TransformComponent.getComponentType());
    if (transform == null || transform.getPosition().distanceSquared(selected.x()+.5, selected.y()+.5, selected.z()+.5) > REACH*REACH)
      throw new IllegalArgumentException("The container is too far away. Reopen the editor nearby.");
    Value next;
    if (!owned) next = new Value(false, "", "");
    else if ("NEARBY".equals(scope)) next = new Value(true, "NEARBY", "");
    else if ("FACTION".equals(scope) && factions().contains(faction)) next = new Value(true, "FACTION", faction);
    else throw new IllegalArgumentException("Select Nearby or a supported native faction.");
    var live = select(selected.world(), new Vector3i(selected.x(), selected.y(), selected.z()));
    if (!selected.entity().isValid() || !selected.entity().equals(live.entity())
        || live.inventory() != selected.inventory() || live.block() != selected.block() || live.rotation() != selected.rotation()
        || !Objects.equals(live.original(), selected.original()))
      throw new IllegalArgumentException("The container changed while editing. Reopen the editor.");
    if (Objects.equals(next, live.original())) return false;
    var cs = selected.world().getChunkStore(); var store = cs.getStore();
    var section = cs.getChunkSectionReferenceAtBlock(selected.x(), selected.y(), selected.z());
    var components = store.getComponent(section, BlockComponentSection.getComponentType());
    var column = cs.getChunkReference(ChunkUtil.indexChunkFromBlock(selected.x(), selected.z()));
    var chunk = column == null ? null : store.getComponent(column, WorldChunk.getComponentType());
    if (components == null || chunk == null) throw new IllegalArgumentException("The container's save section is unavailable.");
    var prefabType = PrefabEditSession.getResourceType();
    var prefab = prefabType == null ? null : actors.getResource(prefabType);
    store.putComponent(selected.entity(), OwnershipProofData.getComponentType(), new OwnershipProofData(next.owned(), next.scope(), next.faction()));
    components.markBlockNeedsSaving(ChunkUtil.indexBlock(selected.x(), selected.y(), selected.z()));
    chunk.markNeedsSaving();
    if (prefab != null) prefab.markPrefabsDirtyAtPosition(new Vector3i(selected.x(), selected.y(), selected.z()));
    return true;
  }
}
