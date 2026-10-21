package com.local.pickpocket;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MerchantStoragePage {
  private static final Pattern ACTION_PATTERN = Pattern.compile("\"Action\":\"([^\"]+)\"");
  private static final Pattern SLOT_INDEX_PATTERN =
      Pattern.compile("\"SlotIndex\":(?:\"(\\d+)\"|(\\d+))");
  private static final Pattern PRESSED_BUTTON_PATTERN =
      Pattern.compile("\"PressedMouseButton\":(\\d+)");

  private final PickpocketPlugin plugin;
  private final UUID npcUuid;
  private final String roleName;
  private final PlayerRef playerRef;
  private final StealthSnapshot snapshot;
  private final Ref<EntityStore> targetRef;
  private final Store<EntityStore> targetStore;
  private final ItemContainer storageContainer;
  private int successfulStealCount;
  private int successfulStolenQuantity;
  private boolean closing;
  private boolean kickedOut;
  private boolean pageClosed;

  public MerchantStoragePage(
      PickpocketPlugin plugin,
      UUID npcUuid,
      String roleName,
      PlayerRef playerRef,
      StealthSnapshot snapshot,
      Ref<EntityStore> targetRef,
      Store<EntityStore> targetStore) {
    this.plugin = plugin;
    this.npcUuid = npcUuid;
    this.roleName = roleName;
    this.playerRef = playerRef;
    this.snapshot = snapshot;
    this.targetRef = targetRef;
    this.targetStore = targetStore;
    this.storageContainer = new SimpleItemContainer((short) plugin.getStorageSize());
    PickpocketPlugin.SessionCarryover carryover =
        plugin.getPickpocketCarryover(playerRef.getUuid(), npcUuid);
    this.successfulStealCount = Math.max(0, carryover.stealCount());
    this.successfulStolenQuantity = Math.max(0, carryover.stolenQuantity());
    loadItems();
  }

  public void open(
      PlayerRef playerRef, Ref<EntityStore> playerEntityRef, Store<EntityStore> playerStore) {
    if (targetRef != null && targetRef.isValid() && !plugin.isPickpocketable(targetRef,targetStore)) return;
    Player player = playerStore.getComponent(playerEntityRef, Player.getComponentType());
    if (player == null) {
      return;
    }

    plugin.setPickpocketPageOpen(playerRef.getUuid(), true);
    HyThiefPickpocketPage page = new HyThiefPickpocketPage(this, playerRef);
    plugin.registerPickpocketPage(playerRef.getUuid(), playerRef, this, page);
    player.getPageManager().openCustomPage(playerEntityRef, playerStore, page);
  }

  public void onPageClosed() {
    if (pageClosed) {
      return;
    }
    pageClosed = true;
    closing = true;
    plugin.savePickpocketCarryover(
        playerRef.getUuid(),
        npcUuid,
        roleName,
        successfulStealCount,
        successfulStolenQuantity);
    plugin.setPickpocketPageOpen(playerRef.getUuid(), false);
    plugin.unregisterPickpocketPage(playerRef.getUuid());
  }

  public List<String> getVisibleEntries() {
    List<String> visibleEntries = new ArrayList<>();
    int capacity = storageContainer.getCapacity();
    for (short slot = 0; slot < capacity; slot++) {
      String entry = toEntry(storageContainer.getItemStack(slot));
      ParsedEntry parsed = parseEntry(entry);
      if (!parsed.itemId.isBlank() && parsed.quantity > 0) {
        visibleEntries.add(entry);
      }
    }
    return visibleEntries;
  }

  public String resolveItemIcon(String itemId) {
    if (itemId == null || itemId.isBlank()) {
      return "";
    }

    Item item = resolveItem(itemId);
    return item == null ? "" : item.getIcon();
  }

  public Item resolveItem(String itemId) {
    if (itemId == null || itemId.isBlank()) {
      return null;
    }

    ItemStack stack = new ItemStack(itemId, 1);
    return stack.getItem();
  }

  public ClickResult handleCustomSlotClick(
      Ref<EntityStore> playerEntityRef, Store<EntityStore> playerStore, String data) {
    if (closing || kickedOut) {
      return ClickResult.IGNORED;
    }

    if (forceCloseIfUnsafe(true)) {
      return ClickResult.CLOSED;
    }

    int visibleSlotIndex = parseIntField(data, SLOT_INDEX_PATTERN, -1);
    if (visibleSlotIndex < 0) {
      return ClickResult.IGNORED;
    }

    int actualSlotIndex = resolveActualSlotIndex(visibleSlotIndex);
    if (actualSlotIndex < 0 || actualSlotIndex >= storageContainer.getCapacity()) {
      return ClickResult.IGNORED;
    }

    Player player = playerStore.getComponent(playerEntityRef, Player.getComponentType());
    if (player == null) {
      return ClickResult.IGNORED;
    }

    ItemStack beforeStack = storageContainer.getItemStack((short) actualSlotIndex);
    String beforeEntry = toEntry(beforeStack);
    ParsedEntry before = parseEntry(beforeEntry);
    if (beforeStack == null || beforeStack.isEmpty() || before.itemId.isBlank() || before.quantity <= 0) {
      return ClickResult.IGNORED;
    }

    String action = parseStringField(data, ACTION_PATTERN, "");
    int pressedMouseButton = parseIntField(data, PRESSED_BUTTON_PATTERN, 1);
    int quantityToSteal = "stealOne".equals(action) || pressedMouseButton == 3 ? 1 : before.quantity;
    quantityToSteal = Math.max(1, Math.min(before.quantity, quantityToSteal));

    ItemStack stolenStack = new ItemStack(before.itemId, quantityToSteal);
    Inventory inventory = player.getInventory();
    if (inventory == null) {
      return ClickResult.IGNORED;
    }

    ItemContainer destination = inventory.getCombinedBackpackStorageHotbarFirst();
    if (destination == null || !destination.canAddItemStack(stolenStack)) {
      playerRef.sendMessage(Message.raw("You don't have space to steal that right now."));
      return ClickResult.IGNORED;
    }

    ItemStackTransaction addTransaction = destination.addItemStack(stolenStack);
    if (addTransaction == null || !addTransaction.succeeded()) {
      playerRef.sendMessage(Message.raw("You don't have space to steal that right now."));
      return ClickResult.IGNORED;
    }

    int remainingQuantity = Math.max(0, before.quantity - quantityToSteal);
    ItemStack afterStack = remainingQuantity <= 0 ? null : new ItemStack(before.itemId, remainingQuantity);
    storageContainer.setItemStackForSlot((short) actualSlotIndex, afterStack);
    String afterEntry = toEntry(afterStack);
    saveItems();

    boolean detected =
        plugin.handleSingleSteal(
            playerRef,
            npcUuid,
            roleName,
            beforeEntry,
            afterEntry,
            snapshot,
            successfulStealCount,
            successfulStolenQuantity,
            targetRef,
            targetStore);
    if (detected) {
      kickedOut = true;
      closing = true;
      return ClickResult.DETECTED;
    }

    successfulStealCount++;
    successfulStolenQuantity += quantityToSteal;
    return ClickResult.STOLEN;
  }

  public void saveItems() {
    List<String> entries = new ArrayList<>();
    int capacity = storageContainer.getCapacity();
    for (short slot = 0; slot < capacity; slot++) {
      entries.add(toEntry(storageContainer.getItemStack(slot)));
    }
    plugin.savePickpocketInventory(npcUuid, entries);
  }

  private int resolveActualSlotIndex(int visibleSlotIndex) {
    int seen = 0;
    int capacity = storageContainer.getCapacity();
    for (short slot = 0; slot < capacity; slot++) {
      ParsedEntry parsed = parseEntry(toEntry(storageContainer.getItemStack(slot)));
      if (parsed.itemId.isBlank() || parsed.quantity <= 0) {
        continue;
      }
      if (seen == visibleSlotIndex) {
        return slot;
      }
      seen++;
    }
    return -1;
  }

  private int parseIntField(String data, Pattern pattern, int fallback) {
    if (data == null || data.isBlank()) {
      return fallback;
    }

    Matcher matcher = pattern.matcher(data);
    if (!matcher.find()) {
      return fallback;
    }

    try {
      for (int group = 1; group <= matcher.groupCount(); group++) {
        String value = matcher.group(group);
        if (value != null && !value.isBlank()) {
          return Integer.parseInt(value);
        }
      }
      return fallback;
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private String parseStringField(String data, Pattern pattern, String fallback) {
    if (data == null || data.isBlank()) {
      return fallback;
    }

    Matcher matcher = pattern.matcher(data);
    return matcher.find() ? matcher.group(1) : fallback;
  }

  private void loadItems() {
    List<String> entries = plugin.getInventoryFor(npcUuid, roleName);
    int capacity = storageContainer.getCapacity();
    for (short slot = 0; slot < capacity && slot < entries.size(); slot++) {
      ParsedEntry parsed = parseEntry(entries.get(slot));
      if (parsed.itemId.isBlank() || parsed.quantity <= 0) {
        continue;
      }
      try {
        storageContainer.setItemStackForSlot(slot, new ItemStack(parsed.itemId, parsed.quantity));
      } catch (Exception ignored) {
      }
    }
  }

  private String toEntry(ItemStack stack) {
    if (stack == null || stack.isEmpty()) {
      return "";
    }
    return stack.getItemId() + ":" + stack.getQuantity();
  }

  private ParsedEntry parseEntry(String entry) {
    if (entry == null || entry.isBlank()) {
      return new ParsedEntry("", 0);
    }
    String[] parts = entry.split(":", 2);
    if (parts.length != 2) {
      return new ParsedEntry("", 0);
    }
    try {
      return new ParsedEntry(parts[0], Integer.parseInt(parts[1]));
    } catch (NumberFormatException ignored) {
      return new ParsedEntry("", 0);
    }
  }

  public record StealthSnapshot(double distance, double facingDot, boolean crouching) {
    static StealthSnapshot defaultSnapshot() {
      return new StealthSnapshot(2.0, 0.0, true);
    }
  }

    public enum ClickResult {
    IGNORED,
    STOLEN,
    DETECTED,
    CLOSED
  }

  private record ParsedEntry(String itemId, int quantity) {}

  public boolean forceCloseIfUnsafe(boolean notifyPlayer) {
    if (closing) {
      return true;
    }

    if (targetRef != null && targetRef.isValid() && !plugin.isPickpocketable(targetRef,targetStore)) {
      closing=true; kickedOut=true;
      if (notifyPlayer) playerRef.sendMessage(Message.raw("Pickpocketing is disabled for this NPC."));
      return true;
    }
    if (!plugin.isPickpocketTargetUnsafe(targetRef, targetStore)) {
      return false;
    }

    closing = true;
    kickedOut = true;
    if (notifyPlayer) {
      playerRef.sendMessage(Message.raw("You lose the opening as the target turns on you."));
    }
    return true;
  }
}
