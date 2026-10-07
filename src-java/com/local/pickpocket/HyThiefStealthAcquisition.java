package com.local.pickpocket;

import com.google.gson.JsonElement;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import java.util.HashMap;
import java.util.Map;

/** Vetted initial-acquisition sites; stance never clears an acquired target. */
final class HyThiefStealthAcquisition extends EntityFilterBase {
  static final String TYPE = "HyThiefStealthAcquisition";
  private static final java.util.Set<String> SUPPORTED_ROLES = java.util.Set.of(
      "Bramblekin",
      "Bramblekin_Shaman",
      "Dungeon_Skeleton_Sand_Archer",
      "Dungeon_Skeleton_Sand_Assassin",
      "Dungeon_Skeleton_Sand_Mage",
      "Dungeon_Skeleton_Sand_Soldier",
      "Feran_Burrower",
      "Feran_Civilian",
      "Feran_Longtooth",
      "Feran_Sharptooth",
      "Feran_Windwalker",
      "Goblin_Duke",
      "Goblin_Duke_Phase_2",
      "Goblin_Duke_Phase_3_Fast",
      "Goblin_Duke_Phase_3_Slow",
      "Goblin_Hermit",
      "Goblin_Lobber",
      "Goblin_Lobber_Patrol",
      "Goblin_Miner",
      "Goblin_Miner_Patrol",
      "Goblin_Ogre",
      "Goblin_Scavenger",
      "Goblin_Scavenger_Battleaxe",
      "Goblin_Scavenger_Sword",
      "Goblin_Scrapper",
      "Goblin_Scrapper_Patrol",
      "Goblin_Thief",
      "Goblin_Thief_Patrol",
      "Hedera",
      "Klops_Gentleman",
      "Klops_Merchant",
      "Klops_Merchant_Patrol",
      "Klops_Merchant_Wandering",
      "Klops_Miner",
      "Klops_Miner_Patrol",
      "Kweebec_Merchant",
      "Kweebec_Razorleaf",
      "Kweebec_Razorleaf_Patrol",
      "Kweebec_Rootling",
      "Kweebec_Sapling",
      "Kweebec_Sapling_Orange",
      "Kweebec_Sapling_Pink",
      "Outlander_Berserker",
      "Outlander_Brute",
      "Outlander_Cultist",
      "Outlander_Hunter",
      "Outlander_Marauder",
      "Outlander_Peon",
      "Outlander_Priest",
      "Outlander_Sorcerer",
      "Outlander_Stalker",
      "Skeleton_Archer",
      "Skeleton_Archer_Patrol",
      "Skeleton_Archer_Wander",
      "Skeleton_Archmage",
      "Skeleton_Archmage_Patrol",
      "Skeleton_Archmage_Wander",
      "Skeleton_Burnt_Alchemist",
      "Skeleton_Burnt_Alchemist_Patrol",
      "Skeleton_Burnt_Alchemist_Wander",
      "Skeleton_Burnt_Archer",
      "Skeleton_Burnt_Archer_Patrol",
      "Skeleton_Burnt_Archer_Wander",
      "Skeleton_Burnt_Gunner",
      "Skeleton_Burnt_Gunner_Patrol",
      "Skeleton_Burnt_Gunner_Wander",
      "Skeleton_Burnt_Knight",
      "Skeleton_Burnt_Knight_Patrol",
      "Skeleton_Burnt_Knight_Wander",
      "Skeleton_Burnt_Lancer",
      "Skeleton_Burnt_Lancer_Patrol",
      "Skeleton_Burnt_Lancer_Wander",
      "Skeleton_Burnt_Praetorian",
      "Skeleton_Burnt_Praetorian_Patrol",
      "Skeleton_Burnt_Praetorian_Wander",
      "Skeleton_Burnt_Soldier",
      "Skeleton_Burnt_Soldier_Patrol",
      "Skeleton_Burnt_Soldier_Wander",
      "Skeleton_Burnt_Wizard",
      "Skeleton_Burnt_Wizard_Patrol",
      "Skeleton_Burnt_Wizard_Wander",
      "Skeleton_Fighter",
      "Skeleton_Fighter_Patrol",
      "Skeleton_Fighter_Wander",
      "Skeleton_Frost_Archer",
      "Skeleton_Frost_Archer_Patrol",
      "Skeleton_Frost_Archer_Wander",
      "Skeleton_Frost_Archmage",
      "Skeleton_Frost_Archmage_Patrol",
      "Skeleton_Frost_Archmage_Wander",
      "Skeleton_Frost_Fighter",
      "Skeleton_Frost_Fighter_Patrol",
      "Skeleton_Frost_Fighter_Wander",
      "Skeleton_Frost_Knight",
      "Skeleton_Frost_Knight_Patrol",
      "Skeleton_Frost_Knight_Wander",
      "Skeleton_Frost_Mage",
      "Skeleton_Frost_Mage_Patrol",
      "Skeleton_Frost_Mage_Wander",
      "Skeleton_Frost_Ranger",
      "Skeleton_Frost_Ranger_Patrol",
      "Skeleton_Frost_Ranger_Wander",
      "Skeleton_Frost_Scout",
      "Skeleton_Frost_Scout_Patrol",
      "Skeleton_Frost_Scout_Wander",
      "Skeleton_Frost_Soldier",
      "Skeleton_Frost_Soldier_Patrol",
      "Skeleton_Frost_Soldier_Wander",
      "Skeleton_Incandescent_Fighter",
      "Skeleton_Incandescent_Fighter_Patrol",
      "Skeleton_Incandescent_Fighter_Wander",
      "Skeleton_Incandescent_Footman",
      "Skeleton_Incandescent_Footman_Patrol",
      "Skeleton_Incandescent_Footman_Wander",
      "Skeleton_Incandescent_Mage",
      "Skeleton_Incandescent_Mage_Patrol",
      "Skeleton_Incandescent_Mage_Wander",
      "Skeleton_Knight",
      "Skeleton_Knight_Patrol",
      "Skeleton_Knight_Wander",
      "Skeleton_Mage",
      "Skeleton_Mage_Patrol",
      "Skeleton_Mage_Wander",
      "Skeleton_Pirate_Captain",
      "Skeleton_Pirate_Captain_Patrol",
      "Skeleton_Pirate_Captain_Wander",
      "Skeleton_Pirate_Gunner",
      "Skeleton_Pirate_Gunner_Patrol",
      "Skeleton_Pirate_Gunner_Wander",
      "Skeleton_Pirate_Striker",
      "Skeleton_Pirate_Striker_Patrol",
      "Skeleton_Pirate_Striker_Wander",
      "Skeleton_Ranger",
      "Skeleton_Ranger_Patrol",
      "Skeleton_Ranger_Wander",
      "Skeleton_Sand_Archer",
      "Skeleton_Sand_Archer_Patrol",
      "Skeleton_Sand_Archer_Wander",
      "Skeleton_Sand_Archmage",
      "Skeleton_Sand_Archmage_Patrol",
      "Skeleton_Sand_Archmage_Wander",
      "Skeleton_Sand_Assassin",
      "Skeleton_Sand_Assassin_Patrol",
      "Skeleton_Sand_Assassin_Wander",
      "Skeleton_Sand_Guard",
      "Skeleton_Sand_Guard_Patrol",
      "Skeleton_Sand_Guard_Wander",
      "Skeleton_Sand_Mage",
      "Skeleton_Sand_Mage_Patrol",
      "Skeleton_Sand_Mage_Wander",
      "Skeleton_Sand_Ranger",
      "Skeleton_Sand_Ranger_Patrol",
      "Skeleton_Sand_Ranger_Wander",
      "Skeleton_Sand_Scout",
      "Skeleton_Sand_Scout_Patrol",
      "Skeleton_Sand_Scout_Wander",
      "Skeleton_Sand_Soldier",
      "Skeleton_Sand_Soldier_Patrol",
      "Skeleton_Sand_Soldier_Wander",
      "Skeleton_Scout",
      "Skeleton_Scout_Patrol",
      "Skeleton_Scout_Wander",
      "Skeleton_Soldier",
      "Skeleton_Soldier_Patrol",
      "Skeleton_Soldier_Wander",
      "Trork_Brawler",
      "Trork_Chieftain",
      "Trork_Doctor_Witch",
      "Trork_Guard",
      "Trork_Hunter",
      "Trork_Mauler",
      "Trork_Sentry",
      "Trork_Sentry_Patrol",
      "Trork_Shaman",
      "Trork_Unarmed",
      "Trork_Warrior",
      "Trork_Warrior_Patrol",
      "Tuluk_Fisherman",
      "Zombie",
      "Zombie_Aberrant",
      "Zombie_Aberrant_Big",
      "Zombie_Aberrant_Small",
      "Zombie_Burnt",
      "Zombie_Frost",
      "Zombie_Sand");
  private final double baseRange;
  private final boolean projected;
  private final String channel;

  HyThiefStealthAcquisition(double baseRange, boolean projected, String channel) {
    this.baseRange = baseRange; this.projected = projected; this.channel = channel;
  }

  static double multiplier(String channel, String role) {
    // Historical 4/15 sight and 6/20 scent guide calibration, not global AI parameters.
    // Compose future modifiers here; never mutate an acquired target to apply stealth.
    if (channel.equals("scent")) return role.equals("Feran_Sharptooth")
        || role.equals("Feran_Longtooth") || role.equals("Tuluk_Fisherman") ? 0.15 : 0.10;
    if (channel.equals("proximity") || channel.equals("watch")) return 0.5;
    return 4.0 / 15.0;
  }

  static boolean isIdleFamily(StateSupport state) {
    // getStateName() is State.SubState; query the native main-state mapping instead.
    return state != null && state.getStateHelper() != null
        && "Idle".equals(state.getStateHelper().getStateName(state.getStateIndex()));
  }

  @Override public boolean matchesEntity(Ref<EntityStore> npc, Ref<EntityStore> candidate,
      ExecutionSupport support, Store<EntityStore> store) {
    if (candidate == null || !candidate.isValid() || store.getComponent(candidate, Player.getComponentType()) == null) return true;
    var state = store.getComponent(npc, StateSupport.getComponentType());
    var marked = store.getComponent(npc, MarkedEntitySupport.getComponentType());
    var world = store.getComponent(npc, WorldSupport.getComponentType());
    var entity = store.getComponent(npc, NPCEntity.getComponentType());
    if (entity == null || !SUPPORTED_ROLES.contains(entity.getRoleName())) return true;
    if (state == null || marked == null || world == null || !isIdleFamily(state)
        || state.isInBusyState() || world.getOverriddenAttitude(candidate) != null) return true;
    var targets = marked.getEntityTargets();
    if (targets == null) return true;
    for (var target : targets) if (candidate.equals(target)) return true;
    var movement = store.getComponent(candidate, MovementStatesComponent.getComponentType());
    if (movement == null || movement.getMovementStates() == null
        || !movement.getMovementStates().crouching) return true;
    var npcTransform = store.getComponent(npc, TransformComponent.getComponentType());
    var playerTransform = store.getComponent(candidate, TransformComponent.getComponentType());
    if (npcTransform == null || playerTransform == null) return true;
    var p = npcTransform.getPosition(); var q = playerTransform.getPosition();
    double dx = p.x() - q.x(), dz = p.z() - q.z(), dy = projected ? 0.0 : p.y() - q.y();
    return Math.sqrt(dx * dx + dy * dy + dz * dz) <= baseRange * multiplier(channel, entity.getRoleName());
  }

  @Override public int cost() { return IEntityFilter.LOW_COST; }

  static final class FilterBuilder extends BuilderEntityFilterBase {
    private double range;
    private boolean projected;
    private String channel;
    @Override public String getShortDescription() { return "HyThief initial player acquisition stance filter"; }
    @Override public String getLongDescription() { return getShortDescription(); }
    @Override public BuilderDescriptorState getBuilderDescriptorState() { return BuilderDescriptorState.Experimental; }
    @Override public Builder<IEntityFilter> readConfig(JsonElement config) {
      var json = config.getAsJsonObject();
      range = json.get("BaseRange").getAsDouble();
      projected = json.has("Projected") && json.get("Projected").getAsBoolean();
      channel = json.get("Channel").getAsString();
      if (!Double.isFinite(range) || range < 0 || !java.util.Set.of("sight", "scent", "proximity", "watch").contains(channel))
        throw new IllegalArgumentException("Invalid HyThief acquisition configuration");
      return this;
    }
    @Override public IEntityFilter build(BuilderSupport support) {
      return new HyThiefStealthAcquisition(range, projected, channel);
    }
  }
}
