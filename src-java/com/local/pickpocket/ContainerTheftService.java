package com.local.pickpocket;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.packets.inventory.*;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.event.events.player.*;
import com.hypixel.hytale.server.core.inventory.*;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.io.handlers.game.*;
import com.hypixel.hytale.server.core.modules.entity.component.PreventInventoryAccess;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.*;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Observes native successful window-to-player transfers; ownership comes only from live block data. */
final class ContainerTheftService implements AutoCloseable {
  private final PickpocketPlugin plugin;
  private final Map<GamePacketHandler, ContainerPacketBridge> bridges = new ConcurrentHashMap<>();
  private final com.hypixel.hytale.event.EventRegistration<?, ?> ready;
  private final com.hypixel.hytale.event.EventRegistration<?, ?> disconnect;
  private volatile boolean closed;

  ContainerTheftService(PickpocketPlugin plugin) {
    this.plugin = plugin;
    ready = plugin.getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
      var ref = event.getPlayerRef();
      if (ref != null && ref.isValid()) {
        var player = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
        if (player != null) bind(player);
      }
    });
    disconnect = plugin.getEventRegistry().register(PlayerDisconnectEvent.class, event -> unbind(event.getPlayerRef()));
  }

  void start() { Universe.get().getPlayers().forEach(this::bind); }

  private void bind(PlayerRef player) {
    if (closed || !(player.getPacketHandler() instanceof GamePacketHandler handler)) return;
    handler.getChannel(NetworkChannel.Default).execute(() -> {
      if (closed || bridges.containsKey(handler)) return;
      var inventory = handler.getSubPacketHandler(InventoryPacketHandler.class);
      if (inventory == null) {
        plugin.getLogger().atWarning().log("HyThief could not attach to native inventory handler");
        return;
      }
      bridges.put(handler, new ContainerPacketBridge(handler, packet -> intercept(player, packet), inventory::handle, inventory::handle));
    });
  }

  private void unbind(PlayerRef player) {
    if (!(player.getPacketHandler() instanceof GamePacketHandler handler)) return;
    handler.getChannel(NetworkChannel.Default).execute(() -> {
      var bridge = bridges.remove(handler);
      if (bridge != null) bridge.close();
    });
  }

  @Override public void close() {
    closed = true;
    ready.unregister();
    disconnect.unregister();
    bridges.forEach((handler, bridge) -> handler.getChannel(NetworkChannel.Default).execute(bridge::close));
    bridges.clear();
  }

  private boolean intercept(PlayerRef player, Packet packet) {
    int from = packet instanceof MoveItemStack m ? m.fromSectionId : ((SmartMoveItemStack) packet).fromSectionId;
    // Negative sections are native player inventories; nonnegative sections are windows.
    if (closed || from < 0 || (packet instanceof MoveItemStack m && m.toSectionId >= 0)) return false;
    var ref = player.getReference();
    if (ref == null || !ref.isValid()) return false;
    var world = ref.getStore().getExternalData().getWorld();
    // Native handlers also queue exactly one task on this world. Keep request ordering intact.
    world.execute(() -> dispatch(player, packet, world));
    return true;
  }

  private void dispatch(PlayerRef player, Packet packet, World world) {
    var ref = player.getReference();
    if (ref == null || !ref.isValid() || ref.getStore().getExternalData().getWorld() != world) return;
    var store = ref.getStore();
    if (store.getArchetype(ref).contains(PreventInventoryAccess.getComponentType())) return;
    boolean executed = false;
    var registrations = new ArrayList<com.hypixel.hytale.event.EventRegistration<?, ?>>();
    try {
      int from = packet instanceof MoveItemStack m ? m.fromSectionId : ((SmartMoveItemStack) packet).fromSectionId;
      var source = InventoryUtils.getSectionById(ref, from, store);
      // A previous witnessed request may already have closed this window. Never replay it.
      if (source == null) return;
      PersistentContainerOwnership.Source owned = null;
      var entity = store.getComponent(ref, Player.getComponentType());
      if (entity != null && source != null) for (var open : entity.getWindowManager().getWindows()) {
        if (open instanceof ContainerBlockWindow window && window.getItemContainer() == source) {
          owned = PersistentContainerOwnership.resolve(world, window, source);
          break;
        }
      }
      final var attached = owned;
      if (attached == null || !PersistentContainerOwnership.eligible(attached.read())) {
        executed = true;
        executeNative(player, packet, ref, store);
        return;
      }
      if (NativePursuit.blocks(ref, store)) {
        closeSource(ref, store, source);
        return;
      }
      var ownership = attached.read().clone();
      var snapshots = new IdentityHashMap<ContainerRemovalProbe.Removal, List<ContainerVisualWitness.Evidence>>();
      var confirmed = new ArrayList<ContainerVisualWitness.Evidence>();
      var probe = new ContainerRemovalProbe(source,
          removal -> confirmed.addAll(snapshots.getOrDefault(removal, List.of())),
          removal -> {
            try {
              snapshots.put(removal, ContainerVisualWitness.capture(ref, store, ownership,
                  candidate -> ContainerWitnessProfiles.eligible(candidate, store), message -> {}));
            } catch (RuntimeException error) {
              // A failed observation must never interrupt an in-progress native inventory transaction.
              plugin.getLogger().atWarning().withCause(error).log("HyThief visual witness observation failed");
            }
          });
      registrations.add(source.registerChangeEvent(event -> probe.source(event.transaction())));
      for (var type : InventoryComponent.EVERYTHING) {
        var inventory = store.getComponent(ref, type);
        if (inventory != null) registrations.add(inventory.getInventory().registerChangeEvent(event -> probe.receipt(event.transaction())));
      }
      executed = true;
      executeNative(player, packet, ref, store);
      probe.finish();
      // Finish all inventory callbacks before notifying or entering native NPC behavior.
      for (var registration : registrations) registration.unregister();
      registrations.clear();
      ContainerCaughtNotification.notifyWitnessed(confirmed, player::sendMessage).ifPresent(witness -> {
        // The successful transfer and all callbacks have finished. Remove the server window
        // before another queued request can use it; native close listeners restore block state.
        closeSource(ref, store, source);
        HyThiefPersonalWitnessAlert.deliver(ownership, ref, confirmed, store, message -> {});
        HyThiefContainerAlert.deliver(plugin, ownership, ref, witness.witness(), store, null);
      });
    } catch (RuntimeException error) {
      plugin.getLogger().atWarning().withCause(error).log("HyThief container withdrawal observation failed; native operation started=%s", executed);
      // Never replay an operation that may already have transferred items.
      if (!executed) executeNative(player, packet, ref, store);
    } finally {
      for (var registration : registrations) registration.unregister();
    }
  }

  static void closeSource(Ref<EntityStore> ref, Store<EntityStore> store, ItemContainer source) {
    var player = store.getComponent(ref, Player.getComponentType());
    if (player == null) return;
    for (var window : List.copyOf(player.getWindowManager().getWindows())) {
      if (window instanceof ContainerBlockWindow container && container.getItemContainer() == source) {
        window.close(ref, store);
      }
    }
  }

  /** Current 0.7.2 native transfer and utility active-slot side effects. No inventory replacement. */
  private static void executeNative(PlayerRef player, Packet packet, Ref<EntityStore> ref, Store<EntityStore> store) {
    if (packet instanceof SmartMoveItemStack m) {
      var settings = store.getComponent(ref, PlayerSettings.getComponentType());
      InventoryUtils.smartMoveItem(ref, m.fromSectionId, m.fromSlotId, m.quantity, m.moveType,
          settings == null ? PlayerSettings.defaults() : settings, store);
      return;
    }
    var m = (MoveItemStack) packet;
    InventoryUtils.moveItem(ref, m.fromSectionId, m.fromSlotId, m.quantity, m.toSectionId, m.toSlotId, store);
    if (m.toSectionId != m.fromSectionId && m.toSectionId == InventoryComponent.UTILITY_SECTION_ID) {
      byte next = (byte) m.toSlotId;
      byte previous = InventoryUtils.getActiveSlot(ref, m.toSectionId, store);
      if (next == previous) return;
      var event = new com.hypixel.hytale.server.core.event.events.ecs.InventoryActiveSlotRequestEvent(m.toSectionId, previous, next, true);
      store.invoke(ref, event);
      if (event.isCancelled() || event.getNewSlot() == previous) return;
      next = event.getNewSlot();
      InventoryUtils.setActiveSlot(ref, m.toSectionId, next, store);
      player.getPacketHandler().writeNoCache(new SetActiveSlot(m.toSectionId, next));
    }
  }
}
