package com.local.pickpocket;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.inventory.*;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.*;
import java.util.function.Consumer;

/** Exercises the registered dispatch path used after Quiche ConnectionHandler.handle. */
final class ContainerPacketBridgeTest {
  static void run() {
    var handlers = new HashMap<Integer, Consumer<ToServerPacket>>();
    IPacketHandler handler = new IPacketHandler() {
      public void registerHandler(int id, Consumer<ToServerPacket> consumer) { handlers.put(id, consumer); }
      public void registerNoOpHandlers(int... ids) {}
      public PlayerRef getPlayerRef() { return null; }
      public String getIdentifier() { return "disposable-dispatch-test"; }
    };
    int[] intercepted = {0}, fallback = {0}; boolean[] eligible = {true};
    var bridge = new ContainerPacketBridge(handler, packet -> { intercepted[0]++; return eligible[0]; },
        packet -> fallback[0]++, packet -> fallback[0]++);
    handlers.get(MoveItemStack.PACKET_ID).accept(new MoveItemStack());
    handlers.get(SmartMoveItemStack.PACKET_ID).accept(new SmartMoveItemStack());
    require(intercepted[0] == 2 && fallback[0] == 0, "registered transfer ingress did not intercept exactly once");
    eligible[0] = false;
    handlers.get(MoveItemStack.PACKET_ID).accept(new MoveItemStack());
    handlers.get(SmartMoveItemStack.PACKET_ID).accept(new SmartMoveItemStack());
    require(intercepted[0] == 4 && fallback[0] == 2, "native passthrough not exactly once");
    bridge.close();
    handlers.get(MoveItemStack.PACKET_ID).accept(new MoveItemStack());
    handlers.get(SmartMoveItemStack.PACKET_ID).accept(new SmartMoveItemStack());
    require(intercepted[0] == 4 && fallback[0] == 4, "native restoration failed");
    require(handlers.size() == 2, "unrelated packet registrations changed");
    System.out.println("CONTAINER_PACKET_BRIDGE_PASS DirectRegisteredDispatch=INTERCEPTED NativeFallback=ONCE Shutdown=RESTORED Scope=TWO_TRANSFER_IDS");
  }
  private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
