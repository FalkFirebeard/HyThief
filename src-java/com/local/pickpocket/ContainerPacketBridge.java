package com.local.pickpocket;

import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.inventory.MoveItemStack;
import com.hypixel.hytale.protocol.packets.inventory.SmartMoveItemStack;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Transport-independent registration around the two native inventory transfer handlers. */
final class ContainerPacketBridge implements AutoCloseable {
  private final IPacketHandler handler;
  private final Consumer<MoveItemStack> move;
  private final Consumer<SmartMoveItemStack> smart;
  ContainerPacketBridge(IPacketHandler handler, Predicate<Packet> intercept,
      Consumer<MoveItemStack> move, Consumer<SmartMoveItemStack> smart) {
    this.handler = handler; this.move = move; this.smart = smart;
    handler.registerHandler(MoveItemStack.PACKET_ID, packet -> {
      var request = (MoveItemStack) packet;
      if (!intercept.test(request)) move.accept(request);
    });
    handler.registerHandler(SmartMoveItemStack.PACKET_ID, packet -> {
      var request = (SmartMoveItemStack) packet;
      if (!intercept.test(request)) smart.accept(request);
    });
  }
  @Override public void close() {
    handler.registerHandler(MoveItemStack.PACKET_ID, packet -> move.accept((MoveItemStack) packet));
    handler.registerHandler(SmartMoveItemStack.PACKET_ID, packet -> smart.accept((SmartMoveItemStack) packet));
  }
}
