package com.local.pickpocket;

import com.google.gson.JsonElement;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.event.events.ecs.UseEntityEvent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

/** Shared resolver-driven entry; all eligibility comes from the existing role resolver. */
final class HyThiefNativeProbe extends EntityEventSystem<EntityStore, UseEntityEvent.Pre> {
  static final String SENSOR = "HyThiefTestEligible";
  private final PickpocketPlugin plugin;

  HyThiefNativeProbe(PickpocketPlugin plugin) {
    super(UseEntityEvent.Pre.class);
    this.plugin = plugin;
  }

  private static boolean isTestTarget(PickpocketPlugin plugin, Ref<EntityStore> target,
      ComponentAccessor<EntityStore> accessor) {
    if (target == null || !target.isValid()) return false;
    NPCEntity npc = accessor.getComponent(target, NPCEntity.getComponentType());
    return npc != null && plugin.isPickpocketable(target,target.getStore());
  }

  private static boolean isCrouched(Ref<EntityStore> player, ComponentAccessor<EntityStore> accessor) {
    if (player == null || !player.isValid()) return false;
    MovementStatesComponent movement = accessor.getComponent(player, MovementStatesComponent.getComponentType());
    return movement != null && movement.getMovementStates() != null && movement.getMovementStates().crouching;
  }

  private record Eligibility(boolean supported, boolean crouched, boolean behind,
      double distance, double facingDot, String state) {
    boolean eligible() { return supported && crouched && behind; }
  }

  private static Eligibility evaluate(PickpocketPlugin plugin, Ref<EntityStore> player,
      Ref<EntityStore> target, Store<EntityStore> store) {
    boolean supported = isTestTarget(plugin, target, store);
    boolean crouched = isCrouched(player, store);
    if (!supported || player == null || !player.isValid()
        || store.getComponent(player, TransformComponent.getComponentType()) == null
        || store.getComponent(target, TransformComponent.getComponentType()) == null
        || store.getComponent(target, HeadRotation.getComponentType()) == null) {
      return new Eligibility(supported, crouched, false, Double.NaN, Double.NaN, "unavailable");
    }
    MerchantStoragePage.StealthSnapshot snapshot = plugin.createStealthSnapshot(player, store, target);
    StateSupport state = store.getComponent(target, StateSupport.getComponentType());
    return new Eligibility(supported, crouched, plugin.isBehindForNativeProbe(store.getComponent(target, NPCEntity.getComponentType()).getRoleName(), snapshot),
        snapshot.distance(), snapshot.facingDot(), state == null ? "missing" : state.getStateName());
  }

  @Override
  public Query<EntityStore> getQuery() {
    return PlayerRef.getComponentType();
  }

  @Override
  public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
      CommandBuffer<EntityStore> buffer, UseEntityEvent.Pre event) {
    if (event.getInteractionType() != InteractionType.Use || event.isCancelled()) return;
    Ref<EntityStore> target = event.getTargetEntity();
    if (!isTestTarget(plugin, target, buffer)) return;
    Ref<EntityStore> player = chunk.getReferenceTo(index);
    Eligibility eligibility = evaluate(plugin, player, target, store);
    if (!eligibility.eligible()) {
      return;
    }
    String roleName = buffer.getComponent(target, NPCEntity.getComponentType()).getRoleName();
    // Stop before UseNPC queues HasInteracted, lock-on, the trade state, and Post-use awareness.
    event.setCancelled(true);
    PlayerRef playerRef = buffer.getComponent(player, PlayerRef.getComponentType());
    try {
      UUIDComponent npcId = buffer.getComponent(target, UUIDComponent.getComponentType());
      Player playerComponent = buffer.getComponent(player, Player.getComponentType());
      if (playerRef == null || playerComponent == null || npcId == null) {
        return;
      }
      MerchantStoragePage storagePage = new MerchantStoragePage(plugin, npcId.getUuid(),
          roleName, playerRef, plugin.createStealthSnapshot(player, store, target), target, store);
      storagePage.open(playerRef, player, store);
    } catch (RuntimeException exception) {
      plugin.getLogger().at(java.util.logging.Level.WARNING).withCause(exception).log("HyThief could not open the pickpocket menu");
      if (playerRef != null) {
        playerRef.sendMessage(Message.raw("HyThief could not open the pickpocket menu. Please check the server log."));
      }
    }
  }

  static final class SensorBuilder extends BuilderSensorBase {
    private final PickpocketPlugin plugin;

    SensorBuilder(PickpocketPlugin plugin) { this.plugin = plugin; }

    @Override public String getShortDescription() { return "HyThief role-aware crouched rear interaction selector"; }
    @Override public String getLongDescription() { return getShortDescription(); }
    @Override public BuilderDescriptorState getBuilderDescriptorState() { return BuilderDescriptorState.Experimental; }
    @Override public Builder<Sensor> readConfig(JsonElement config) {
      requireInstructionType(EnumSet.of(InstructionType.Interaction));
      return this;
    }
    @Override public Sensor build(BuilderSupport support) { return new EligibilitySensor(this, plugin); }
  }

  private static final class EligibilitySensor extends SensorBase {
    private final PickpocketPlugin plugin;

    EligibilitySensor(BuilderSensorBase builder, PickpocketPlugin plugin) {
      super(builder);
      this.plugin = plugin;
    }

    @Override
    public boolean matches(Ref<EntityStore> npc, ExecutionSupport support, double dt, Store<EntityStore> store) {
      if (!super.matches(npc, support, dt, store)) return false;
      Ref<EntityStore> player = support.getStateSupport().getInteractionIterationTarget();
      if (player == null || !player.isValid()) return false;
      Eligibility eligibility = evaluate(plugin, player, npc, store);
      return eligibility.eligible();
    }

    @Override public InfoProvider getSensorInfo() { return null; }
  }
}
