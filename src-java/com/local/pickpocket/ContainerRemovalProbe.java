package com.local.pickpocket;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

/** Synchronous successful transfer evidence. Does not change inventories. */
final class ContainerRemovalProbe {
  record Removal(short slot, String item, int amount, Instant time, long nanoTime) {}
  private final ItemContainer chest;
  private final IdentityHashMap<SlotTransaction, Removal> source = new IdentityHashMap<>();
  private final Set<SlotTransaction> receipt = Collections.newSetFromMap(new IdentityHashMap<>());
  private final Set<SlotTransaction> reported = Collections.newSetFromMap(new IdentityHashMap<>());
  private final Consumer<Removal> sink;
  private final Consumer<Removal> capture;
  ContainerRemovalProbe(ItemContainer chest, Consumer<Removal> sink) {
    this(chest, sink, removal -> {});
  }
  ContainerRemovalProbe(ItemContainer chest, Consumer<Removal> sink, Consumer<Removal> capture) {
    this.chest = chest; this.sink = sink; this.capture = capture;
  }
  void source(Transaction transaction) {
    visit(transaction, move -> {
      if (!move.succeeded()) return;
      SlotTransaction slot = move.getMoveType() == MoveType.MOVE_FROM_SELF
          ? move.getRemoveTransaction()
          : move.getAddTransaction() instanceof SlotTransaction s ? s : null;
      if (slot == null || !slot.succeeded()) return;
      ItemStack before = slot.getSlotBefore(), after = slot.getSlotAfter();
      int amount = removed(before, after);
      if (amount > 0) {
        var removal = new Removal(slot.getSlot(), before.getItemId(), amount, Instant.now(), System.nanoTime());
        source.put(move.getRemoveTransaction(), removal);
        capture.accept(removal);
      }
    });
  }
  void receipt(Transaction transaction) {
    visit(transaction, move -> {
      if (move.succeeded() && (move.getOtherContainer() == chest
          || move.getOtherContainer().containsContainer(chest))) receipt.add(move.getRemoveTransaction());
    });
  }
  int finish() {
    int count = 0;
    for (var entry : source.entrySet()) {
      if (receipt.contains(entry.getKey()) && reported.add(entry.getKey())) {
        sink.accept(entry.getValue()); count++;
      }
    }
    return count;
  }
  static int removed(ItemStack before, ItemStack after) {
    if (ItemStack.isEmpty(before)) return 0;
    return Math.max(0, before.getQuantity() - (!ItemStack.isEmpty(after)
        && ItemStack.isStackableWith(before, after) ? after.getQuantity() : 0));
  }
  private static void visit(Transaction transaction, Consumer<MoveTransaction<?>> visitor) {
    if (transaction instanceof MoveTransaction<?> move) visitor.accept(move);
    else if (transaction instanceof ListTransaction<?> list)
      for (var child : list.getList()) visit(child, visitor);
  }
}
