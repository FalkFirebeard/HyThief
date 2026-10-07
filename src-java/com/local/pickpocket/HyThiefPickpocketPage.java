package com.local.pickpocket;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;

public final class HyThiefPickpocketPage extends CustomUIPage {
  private final MerchantStoragePage storagePage;

  public HyThiefPickpocketPage(MerchantStoragePage storagePage, PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction);
    this.storagePage = storagePage;
  }

  @Override
  public void build(
      Ref<EntityStore> playerEntityRef,
      UICommandBuilder commands,
      UIEventBuilder events,
      Store<EntityStore> playerStore) {
    commands.append("Pages/HyThiefPickpocketPage.ui");
    commands.set("#Hint.Text", "Steal one slot at a time.");
    commands.set("#SubHint.Text", "Left click steals a stack. Right click steals one.");
    commands.clear("#ElementList");

    List<String> entries = storagePage.getVisibleEntries();
    if (entries.isEmpty()) {
      commands.appendInline(
          "#ElementList",
          "Label { Text: \"Nothing worth taking right now.\"; Style: (Alignment: Center, TextColor: #96a9be); Padding: (Top: 40); }");
      return;
    }

    for (int visibleIndex = 0; visibleIndex < entries.size(); visibleIndex++) {
      appendEntry(
          commands,
          events,
          "#ElementList[" + visibleIndex + "]",
          visibleIndex,
          entries.get(visibleIndex));
    }
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> playerEntityRef, Store<EntityStore> playerStore, String data) {
    MerchantStoragePage.ClickResult result =
        storagePage.handleCustomSlotClick(playerEntityRef, playerStore, data);
    if (result == MerchantStoragePage.ClickResult.DETECTED
        || result == MerchantStoragePage.ClickResult.CLOSED) {
      cleanupAndClose();
      return;
    }
    if (result == MerchantStoragePage.ClickResult.STOLEN) {
      rebuild();
    }
  }

  @Override
  public void onDismiss(Ref<EntityStore> playerEntityRef, Store<EntityStore> playerStore) {
    storagePage.onPageClosed();
    storagePage.saveItems();
  }

  public void forceCloseExternally() {
    cleanupAndClose();
  }

  private void cleanupAndClose() {
    storagePage.onPageClosed();
    storagePage.saveItems();
    close();
  }

  private void appendEntry(
      UICommandBuilder commands,
      UIEventBuilder events,
      String selector,
      int visibleIndex,
      String entry) {
    ParsedEntry parsed = parseEntry(entry);
    if (parsed.itemId.isBlank() || parsed.quantity <= 0) {
      return;
    }

    commands.append("#ElementList", "Pages/HyThiefPickpocketRow.ui");
    commands.set(selector + " #Icon.ItemId", parsed.itemId);
    commands.set(selector + " #Name.TextSpans", resolveItemName(parsed.itemId));
    commands.set(selector + " #Quantity.Text", Integer.toString(parsed.quantity));

    events.addEventBinding(
        CustomUIEventBindingType.Activating,
        selector,
        EventData.of("SlotIndex", Integer.toString(visibleIndex)).append("Action", "stealStack"),
        false);
    events.addEventBinding(
        CustomUIEventBindingType.RightClicking,
        selector,
        EventData.of("SlotIndex", Integer.toString(visibleIndex)).append("Action", "stealOne"),
        false);
  }

  private Message resolveItemName(String itemId) {
    Item item = storagePage.resolveItem(itemId);
    if (item == null) {
      return Message.raw(itemId);
    }
    return Message.translation(item.getTranslationKey());
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

  private record ParsedEntry(String itemId, int quantity) {}
}
