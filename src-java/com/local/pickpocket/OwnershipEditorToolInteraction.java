package com.local.pickpocket;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;

/** Native held-tool entry only. No block action, inventory mutation, or client key hook. */
public final class OwnershipEditorToolInteraction extends SimpleInstantInteraction {
  public static final String TYPE = "HyThiefOwnershipEditor";
  public static final String ITEM = "HyThief_Ownership_Editor_Tool";
  public static final BuilderCodec<OwnershipEditorToolInteraction> CODEC = BuilderCodec.builder(
      OwnershipEditorToolInteraction.class, OwnershipEditorToolInteraction::new,
      SimpleInstantInteraction.CODEC).build();

  @Override protected void firstRun(InteractionType type, InteractionContext context, CooldownHandler cooldown) {
    var actor = context.getEntity();
    var commands = context.getCommandBuffer();
    var held = context.getHeldItem();
    if (type != InteractionType.Primary || commands == null || actor == null || !actor.isValid()
        || held == null || !ITEM.equals(held.getItemId())) {
      context.getState().state = InteractionState.Failed;
      return;
    }
    // Native command-buffer callback runs after ECS processing on the world thread.
    commands.run(store -> {
      if (!actor.isValid() || actor.getStore() != store) return;
      var player = store.getComponent(actor, PlayerRef.getComponentType());
      if (player == null) return;
      try { OwnershipEditor.open(actor, store, player); }
      catch (IllegalArgumentException ignored) { /* Tool misses and unauthorized uses are silent. */ }
    });
    context.getState().state = InteractionState.Finished;
  }

  @Override protected void simulateFirstRun(InteractionType type, InteractionContext context, CooldownHandler cooldown) {
    // Only authoritative firstRun opens a page, never the prediction pass.
  }
}
