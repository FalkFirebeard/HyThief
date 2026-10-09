package com.local.pickpocket;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.inventory.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.*;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.io.adapter.*;
import com.hypixel.hytale.server.core.io.handlers.game.InventoryPacketHandler;
import com.hypixel.hytale.server.core.modules.entity.component.PreventInventoryAccess;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Diagnostic builds: automatic fixture participation, manual opt-in elsewhere. */
final class HyThiefContainerDiagnostics implements PlayerPacketFilter {
  private final PickpocketPlugin plugin;
  private final OwnedContainerRegistry registry = new OwnedContainerRegistry();
  private final Set<UUID> participants = ConcurrentHashMap.newKeySet();
  private final AtomicLong sequence = new AtomicLong();
  private final PacketFilter registration;
  private final DevContainerTestWorld testWorld;
  HyThiefContainerDiagnostics(PickpocketPlugin plugin) {
    this.plugin = plugin;
    testWorld = new DevContainerTestWorld(plugin, registry, this::log);
    registration = PacketAdapters.registerInbound((PlayerPacketFilter) this);
    plugin.getCommandRegistry().registerCommand(new Command());
  }
  void close() { testWorld.close(); participants.clear(); registry.close(); PacketAdapters.deregisterInbound(registration); }
  void start() { testWorld.start(); }
  @Override public boolean test(PlayerRef player, Packet packet) {
    if (!(player.getPacketHandler() instanceof com.hypixel.hytale.server.core.io.handlers.IPacketHandler)

        || !(packet instanceof MoveItemStack || packet instanceof SmartMoveItemStack)) return false;
    var ref = player.getReference();
    if (ref == null || !ref.isValid()) return false;
    World world = ref.getStore().getExternalData().getWorld();
    if (!participants.contains(player.getUuid()) && !testWorld.enabled(world)) return false;
    world.execute(() -> dispatch(player, packet, world));
    return true;
  }
  private void fallback(PlayerRef player, Packet packet) {
    var nativeHandler = new InventoryPacketHandler((com.hypixel.hytale.server.core.io.handlers.IPacketHandler) player.getPacketHandler());
    if (packet instanceof MoveItemStack move) nativeHandler.handle(move);
    else nativeHandler.handle((SmartMoveItemStack) packet);
  }
  private void dispatch(PlayerRef player, Packet packet, World world) {
    var ref = player.getReference();
    if (ref == null || !ref.isValid() || ref.getStore().getExternalData().getWorld() != world) return;
    var store = ref.getStore();
    boolean automatic = testWorld.enabled(world);
    if (automatic) testWorld.refresh(world);
    var entry = registry.current();
    if (!automatic && (!participants.contains(player.getUuid()) || !registry.valid(world, entry))) {
      fallback(player, packet); return;
    }
    int from = packet instanceof MoveItemStack m ? m.fromSectionId : ((SmartMoveItemStack) packet).fromSectionId;
    int to = packet instanceof MoveItemStack m ? m.toSectionId : Integer.MIN_VALUE;
    // Preserve native utility active-slot request/event side effects; not attributed in this proof.
    if (to == InventoryComponent.UTILITY_SECTION_ID) { fallback(player, packet); return; }
    ItemContainer source = InventoryUtils.getSectionById(ref, from, store);
    ItemContainer destination = packet instanceof MoveItemStack ? InventoryUtils.getSectionById(ref, to, store) : null;
    if (automatic) entry = registry.findAutomatic(world, source, destination);
    if (entry == null || (source != entry.container() && destination != entry.container())) {
      fallback(player, packet); return;
    }
    if (store.getArchetype(ref).contains(PreventInventoryAccess.getComponentType())) {
      log("Player=" + player.getUuid() + " Action=IGNORE Reason=inventory-access-blocked"); return;
    }
    long request = sequence.incrementAndGet();
    var observed = entry;
    var probe = new ContainerRemovalProbe(observed.container(), removal -> {
      var key = observed.key();
      log("Request=" + request + " Player=" + player.getUsername() + "/" + player.getUuid()
          + " Chest=" + key.world() + ":" + key.x() + "," + key.y() + "," + key.z()
          + " Slot=" + removal.slot() + " Item=" + removal.item() + " Amount=" + removal.amount()
          + " Action=REMOVE Result=SUCCESS Time=" + removal.time() + " Mono=" + removal.nanoTime());
    });
    var registrations = new ArrayList<com.hypixel.hytale.event.EventRegistration<?, ?>>();
    boolean executed = false;
    try {
      registrations.add(entry.container().registerChangeEvent(e -> probe.source(e.transaction())));
      for (var type : InventoryComponent.EVERYTHING) {
        var component = store.getComponent(ref, type);
        if (component != null) registrations.add(component.getInventory().registerChangeEvent(e -> probe.receipt(e.transaction())));
      }
      executed = true;
      if (packet instanceof MoveItemStack m) {
        InventoryUtils.moveItem(ref, m.fromSectionId, m.fromSlotId, m.quantity, m.toSectionId, m.toSlotId, store);
      } else {
        var m = (SmartMoveItemStack) packet;
        var settings = store.getComponent(ref, PlayerSettings.getComponentType());
        InventoryUtils.smartMoveItem(ref, m.fromSectionId, m.fromSlotId, m.quantity, m.moveType, settings, store);
      }
      if (probe.finish() == 0) log("Request=" + request + " Player=" + player.getUuid()
          + " Action=IGNORE Reason=no-paired-chest-removal");
    } catch (RuntimeException error) {
      log("Request=" + request + " Action=ERROR Reason=" + error.getClass().getSimpleName()
          + " NativeExecutionStarted=" + executed);
      // Never replay a possibly applied native operation.
      if (!executed) fallback(player, packet);
    } finally {
      for (var event : registrations) event.unregister();
    }
  }
  private void log(String message) { plugin.getLogger().atInfo().log("[HyThief ContainerTest] " + message); }
  private final class Command extends AbstractPlayerCommand {
    Command() {
      super("hythiefcontainer", "Development-only: mark your open chest, or clear diagnostics");
      requirePermission("hythief.container.diagnostics"); setAllowsExtraArguments(true);
    }
    @Override protected void execute(CommandContext context, Store<EntityStore> store,
        Ref<EntityStore> ref, PlayerRef player, World world) {
      if (testWorld.enabled(world)) {
        context.sendMessage(Message.raw("This test world observes both owned chests automatically. No commands are needed; fallback commands are for other worlds.")); return;
      }
      var words = context.getInputString().trim().split("\\s+");
      if (words.length == 2 && words[1].equals("clear")) {
        participants.clear(); registry.clear(); context.sendMessage(Message.raw("HyThief container diagnostics cleared.")); return;
      }
      if (words.length == 2 && words[1].equals("join")) {
        if (!registry.valid(world, registry.current())) {
          context.sendMessage(Message.raw("No valid marked chest in this world.")); return;
        }
        participants.add(player.getUuid()); context.sendMessage(Message.raw("Your native transfers are now observed for the marked chest.")); return;
      }
      if (words.length == 5 && words[1].equals("mark")) {
        try {
          var marker = registry.mark(world, Integer.parseInt(words[2]), Integer.parseInt(words[3]), Integer.parseInt(words[4]));
          participants.add(player.getUuid()); context.sendMessage(Message.raw("Diagnostics armed: " + marker.key()));
        } catch (IllegalStateException | NumberFormatException error) { context.sendMessage(Message.raw(error.getMessage())); }
        return;
      }
      if (words.length != 1) {
        context.sendMessage(Message.raw("Use /hythiefcontainer mark X Y Z, join, or clear.")); return;
      }
      var entity = store.getComponent(ref, Player.getComponentType());
      var windows = entity.getWindowManager().getWindows().stream()
          .filter(w -> w instanceof ContainerBlockWindow).map(w -> (ContainerBlockWindow) w).toList();
      if (windows.size() != 1) {
        context.sendMessage(Message.raw("Open exactly one ordinary block chest, then run /hythiefcontainer.")); return;
      }
      try {
        var marker = registry.mark(world, windows.getFirst()); participants.add(player.getUuid());
        context.sendMessage(Message.raw("Diagnostics armed for you: " + marker.key() + ". No crime consequences. /hythiefcontainer clear to stop."));
      } catch (IllegalStateException error) { context.sendMessage(Message.raw(error.getMessage())); }
    }
  }
}
