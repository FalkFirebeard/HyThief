package com.local.pickpocket;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;

/** Editing only: never invokes damage, UseNPC, trade or pickpocket actions. */
public final class NpcEditorToolInteraction extends SimpleInstantInteraction {
  public static final String ITEM="HyThief_NPC_Editor_Tool",TYPE="HyThiefNpcEditor";
  private final PickpocketPlugin plugin;
  private NpcEditorToolInteraction(PickpocketPlugin plugin) { this.plugin=plugin; }
  static BuilderCodec<NpcEditorToolInteraction> codec(PickpocketPlugin plugin) {
    return BuilderCodec.builder(NpcEditorToolInteraction.class,()->new NpcEditorToolInteraction(plugin),SimpleInstantInteraction.CODEC).build();
  }
  @Override protected void firstRun(InteractionType type,InteractionContext context,CooldownHandler cooldown) {
    var actor=context.getEntity();var commands=context.getCommandBuffer();var held=context.getHeldItem();
    if(type!=InteractionType.Primary || commands==null || actor==null || !actor.isValid() || held==null || !ITEM.equals(held.getItemId())) {
      context.getState().state=InteractionState.Failed;return;
    }
    commands.run(store->{
      if(!actor.isValid() || actor.getStore()!=store)return;
      var player=store.getComponent(actor,PlayerRef.getComponentType());if(player==null)return;
      try { NpcEditor.open(plugin,actor,store,player); }
      catch(IllegalArgumentException ignored) { /* Invalid target/permission stays silent. */ }
    });
    context.getState().state=InteractionState.Finished;
  }
  @Override protected void simulateFirstRun(InteractionType type,InteractionContext context,CooldownHandler cooldown) { }
}
