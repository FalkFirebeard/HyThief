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
  private final Map<com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler, ContainerPacketBridge> bridges = new ConcurrentHashMap<>();
  private final com.hypixel.hytale.event.EventRegistration<?, ?> readyRegistration;
  private volatile boolean closed;
  private final DevContainerTestWorld testWorld;
  HyThiefContainerDiagnostics(PickpocketPlugin plugin) {
    this.plugin = plugin;
    testWorld = new DevContainerTestWorld(plugin, registry, this::log);
    readyRegistration = plugin.getEventRegistry().registerGlobal(
        com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent.class, event -> {
          var ref = event.getPlayerRef();
          if (ref != null && ref.isValid()) {
            var player = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
            if (player != null && testWorld.enabled(ref.getStore().getExternalData().getWorld())) bind(player);
          }
        });
    log("Action=INGRESS_READY Mode=native-handler-registration Version=" + plugin.getManifest().getVersion());
    plugin.getCommandRegistry().registerCommand(new Command());
  }
  void close() { closed = true; readyRegistration.unregister(); testWorld.close(); participants.clear(); registry.close(); bridges.values().forEach(ContainerPacketBridge::close); bridges.clear(); }
  private void bind(PlayerRef player) {
    if (closed || !(player.getPacketHandler() instanceof com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler handler)) return;
    handler.getChannel(com.hypixel.hytale.protocol.NetworkChannel.Default).execute(() -> {
      if (closed || bridges.containsKey(handler)) return;
      var nativeInventory = handler.getSubPacketHandler(InventoryPacketHandler.class);
      if (nativeInventory == null) { log("Action=INGRESS_BIND_FAILED Reason=no-native-inventory-handler"); return; }
      bridges.put(handler, new ContainerPacketBridge(handler, packet -> test(player, packet),
          nativeInventory::handle, nativeInventory::handle));
      log("Action=INGRESS_BOUND Player=" + player.getUuid() + " Handler=" + handler.getClass().getSimpleName());
    });
  }
  void start() { testWorld.start(); }
  @Override public boolean test(PlayerRef player, Packet packet) {
    if (!(player.getPacketHandler() instanceof com.hypixel.hytale.server.core.io.handlers.IPacketHandler)

        || !(packet instanceof MoveItemStack || packet instanceof SmartMoveItemStack)) return false;
    var ref = player.getReference();
    if (ref == null || !ref.isValid()) return false;
    World world = ref.getStore().getExternalData().getWorld();
    if (DevContainerTestWorld.NAME.equals(world.getWorldConfig().getDisplayName()) || participants.contains(player.getUuid()))
      log("Action=INGRESS Player=" + player.getUuid() + " Packet=" + packet.getClass().getSimpleName() + " World=" + world.getWorldConfig().getUuid() + " FixtureEnabled=" + testWorld.enabled(world));
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
    if (to == InventoryComponent.UTILITY_SECTION_ID) { log("Action=PASSTHROUGH Reason=utility-section"); fallback(player, packet); return; }
    ItemContainer source = InventoryUtils.getSectionById(ref, from, store);
    ItemContainer destination = packet instanceof MoveItemStack ? InventoryUtils.getSectionById(ref, to, store) : null;
    PersistentContainerOwnership.Source persistent = null;
    boolean componentRoute = false;
    if(automatic) {
      var entity=store.getComponent(ref,Player.getComponentType());
      for(var open:entity.getWindowManager().getWindows())if(open instanceof ContainerBlockWindow window
          && (window.getItemContainer()==source || window.getItemContainer()==destination)
          && (testWorld.persistentField(world) || testWorld.persistentPosition(world,window.getX(),window.getY(),window.getZ())
              || testWorld.controlPosition(world,window.getX(),window.getY(),window.getZ()))) {
        componentRoute=true;
        persistent=PersistentContainerOwnership.resolve(world,window,window.getItemContainer());
        break;
      }
      if(componentRoute) {
        if(persistent==null) { log("Action=SOURCE_UNRESOLVED Authority=component NoRegistryFallback=true"); fallback(player,packet); return; }
        // Entry is only a request-local host descriptor; never registered as ownership.
        entry=new OwnedContainerRegistry.Entry(persistent.key(),persistent.container());
      } else entry = registry.findAutomatic(world, source, destination);
    }
    final var attachedSource=persistent;
    log("Action=SECTION_RESOLVED Player=" + player.getUuid() + " From=" + from + " To=" + to
        + " Source=" + identity(source) + " Destination=" + identity(destination)
        + " Owned=" + (entry == null ? "none" : entry.key())
        + " Registered=" + registry.automatic(world).stream().map(e -> e.key() + "/" + identity(e.container())).toList());
    if (entry == null || (source != entry.container() && destination != entry.container())) {
      if (automatic && source != null && testWorld.isControl(world, source))
        log("Player=" + player.getUuid() + " Action=CONTROL_UNOWNED_IGNORE");
      log("Action=PASSTHROUGH Reason=no-owned-container-match");
      fallback(player, packet); return;
    }
    if (store.getArchetype(ref).contains(PreventInventoryAccess.getComponentType())) {
      log("Player=" + player.getUuid() + " Action=IGNORE Reason=inventory-access-blocked"); return;
    }
    long request = sequence.incrementAndGet();
    var observed = entry;
    log("Request=" + request + " Action=OWNED_REQUEST Offender=" + ref + " Player=" + player.getUuid() + " Chest=" + observed.key());
    var evidence = new IdentityHashMap<ContainerRemovalProbe.Removal, java.util.List<ContainerVisualWitness.Evidence>>();
    var confirmed = new ArrayList<ContainerVisualWitness.Evidence>();
    var caughtOwnership = new OwnershipProofData[1];
    var probe = new ContainerRemovalProbe(observed.container(), removal -> {
      var key = observed.key();
      if (attachedSource!=null) {
        log("Request="+request+" Action=SOURCE_RESOLVED Entity="+attachedSource.entity()+" Authority=component NoRegistryFallback=true");
        var data=attachedSource.read();
        log("Request="+request+" Action="+(data==null?"COMPONENT_MISSING":"COMPONENT_FOUND")+" Owned="+(data!=null&&data.owned())+" Scope="+(data==null?"MISSING":data.scope())+" Faction="+(data==null?"MISSING":data.faction()));
        if(!PersistentContainerOwnership.eligible(data)) {
          boolean proof=testWorld.persistentPosition(world,attachedSource.key().x(),attachedSource.key().y(),attachedSource.key().z());
          log("Request="+request+" Action=UNOWNED_IGNORE ProofFailure="+(proof&&data==null)+" NoRegistryFallback=true");return;
        }
      }
      if (attachedSource != null) caughtOwnership[0] = attachedSource.read().clone();
      if (automatic) {
        var visual = evidence.get(removal);
        if (visual != null) { confirmed.addAll(visual); for(var seen:visual)log("Request=" + request + " " + seen.diagnostic()); }
      }
      log("Request=" + request + " Player=" + player.getUsername() + "/" + player.getUuid()
          + " Chest=" + key.world() + ":" + key.x() + "," + key.y() + "," + key.z()
          + " Slot=" + removal.slot() + " Item=" + removal.item() + " Amount=" + removal.amount()
          + " Action=REMOVE Result=SUCCESS Time=" + removal.time() + " Mono=" + removal.nanoTime());
    }, removal -> {
      if (automatic && (attachedSource==null || PersistentContainerOwnership.eligible(attachedSource.read()))) {
        try { log("Request=" + request + " Action=OWNED_REMOVE_CAPTURE Item=" + removal.item() + " Amount=" + removal.amount()); evidence.put(removal, attachedSource==null ? java.util.List.of(ContainerVisualWitness.capture(ref,store))
            : ContainerVisualWitness.capture(ref,store,attachedSource.read(),testWorld.witnessRoles(world),message->log("Request="+request+" "+message))); }
        catch (RuntimeException error) { log("Request=" + request + " WITNESS=UNSEEN Reason=visual-error:" + error.getClass().getSimpleName()); plugin.getLogger().atWarning().withCause(error).log("HyThief container visual capture failed"); }
      }
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
    // All inventory locks/callbacks have unwound. One downstream notification per request.
    ContainerCaughtNotification.notifyWitnessed(confirmed, player::sendMessage).ifPresent(visual -> {
      log("Request=" + request + " Action=CAUGHT_NOTIFICATION Player=" + player.getUuid() + " Witness=" + visual.role());
      var delivery = HyThiefContainerAlert.deliver(plugin, caughtOwnership[0], ref, visual.witness(), store, message -> log("Request=" + request + " " + message));
      log("Request=" + request + " Action=" + (delivery.sent() ? "DANGER_SENT" : "DANGER_NOT_SENT")
          + " Witness=" + visual.role() + " AlertType=" + delivery.type() + " Signal=" + delivery.signal() + " Responders=" + delivery.responders() + " Reason=" + delivery.reason());
    });
  }
  private static String identity(Object value) { return value == null ? "null" : value.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(value)); }
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
        participants.add(player.getUuid()); bind(player); context.sendMessage(Message.raw("Your native transfers are now observed for the marked chest.")); return;
      }
      if (words.length == 5 && words[1].equals("mark")) {
        try {
          var marker = registry.mark(world, Integer.parseInt(words[2]), Integer.parseInt(words[3]), Integer.parseInt(words[4]));
          participants.add(player.getUuid()); bind(player); context.sendMessage(Message.raw("Diagnostics armed: " + marker.key()));
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
