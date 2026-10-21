package com.local.pickpocket;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class OwnershipEditorCommand extends AbstractCommandCollection {
  public OwnershipEditorCommand() {
    super("hythief", "HyThief creator tools");
    requirePermission(OwnershipEditor.PERMISSION);
    addSubCommand(new Edit());
  }
  private static final class Edit extends AbstractPlayerCommand {
    Edit() {
      super("ownership", "Edit ownership of the storage container you are looking at");
      requirePermission(OwnershipEditor.PERMISSION);
    }
    @Override protected void execute(CommandContext context, Store<EntityStore> store,
        Ref<EntityStore> actor, PlayerRef player, World world) {
      if (!player.hasPermission(OwnershipEditor.PERMISSION)) {
        context.sendMessage(Message.raw("You do not have permission to edit ownership.")); return;
      }
      try {
        OwnershipEditor.open(actor, store, player);
      } catch (IllegalArgumentException ex) { context.sendMessage(Message.raw(ex.getMessage())); }
    }
  }
}
