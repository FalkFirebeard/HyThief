package com.local.pickpocket;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.PositionCache;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import org.joml.Vector3d;
import java.util.ArrayList;
import java.util.List;

/** Development fixture only: fresh visual evidence captured in the removal callback. */
final class ContainerVisualWitness {
  // Container theft balance experiment only; normal NPC/pickpocket perception is unchanged.
  static final double RANGE = 20.0;
  static final double CROUCHED_RANGE = 6.0;
  static double visualRange(boolean crouched, String role) {
    return crouched ? CROUCHED_RANGE : RANGE;
  }
  static final float SECTOR = (float) Math.toRadians(180);
  record Evidence(Ref<EntityStore> witness, String role, double distance, boolean sector,
      boolean los, boolean crouched, Vector3d playerPosition, String reason, Vector3d witnessPosition, float headYaw) {
    boolean seen() { return "seen".equals(reason); }
    String diagnostic() { return "WITNESS=" + (seen() ? role : "UNSEEN") + " Reason=" + reason
        + " Distance=" + distance + " Range=" + visualRange(crouched, role) + " StandingRange=" + RANGE + " Sector=" + sector + " LOS=" + los
        + " Witness=" + witness + " WitnessPosition=" + witnessPosition + " HeadYaw=" + headYaw + " ViewSectorDegrees=180"
        + " Crouched=" + crouched + " PlayerPosition=" + playerPosition; }
  }
  private ContainerVisualWitness() {}
  static Evidence capture(Ref<EntityStore> player, Store<EntityStore> store) {
    if (player == null || !player.isValid() || player.getStore() != store)
      return missing("invalid-player");
    var transform = store.getComponent(player, TransformComponent.getComponentType());
    if (transform == null) return missing("missing-player-transform");
    var position = new Vector3d(transform.getPosition());
    var movement = store.getComponent(player, MovementStatesComponent.getComponentType());
    boolean crouched = movement != null && movement.getMovementStates().crouching;
    // Select exactly the controlled Sapling; other nearby NPCs are outside this prototype.
    List<Ref<EntityStore>> candidates = new ArrayList<>();
    store.forEachChunk(NPCEntity.getComponentType(), (chunk, buffer) -> {
      for (int i = 0; i < chunk.size(); i++) {
        var ref = chunk.getReferenceTo(i);
        var npc = store.getComponent(ref, NPCEntity.getComponentType());
        var t = store.getComponent(ref, TransformComponent.getComponentType());
        if ("Kweebec_Sapling".equals(npc.getRoleName()) && npc.getRole() != null && t != null
            && t.getPosition().distance(.5, 64, 10.5) < .75
            && store.getComponent(ref, DeathComponent.getComponentType()) == null) candidates.add(ref);
      }
    });
    if (candidates.size() != 1) return missing("missing-or-ambiguous-fixture-witness");
    return evaluate(candidates.getFirst(), player, position, crouched, store);
  }
  static Evidence evaluate(Ref<EntityStore> witness, Ref<EntityStore> player,
      Vector3d position, boolean crouched, Store<EntityStore> store) {
    var t = store.getComponent(witness, TransformComponent.getComponentType());
    var head = store.getComponent(witness, HeadRotation.getComponentType());
    var npc = store.getComponent(witness, NPCEntity.getComponentType());
    if (t == null || head == null || npc == null) return missing("missing-witness-components");
    double distance = t.getPosition().distance(position);
    boolean sector = NPCPhysicsMath.inViewSector(t.getPosition().x, t.getPosition().z,
        head.getRotation().yaw(), SECTOR, position.x, position.z);
    // A private native cache for this observation avoids stale LOS from a frozen NPC,
    // and does not clear or mutate that NPC's AI cache/target/perception state.
    var sight = new PositionCache();
    sight.setOpaqueBlockSet(-1); // native block opacity, no custom opaque-set policy
    double range = visualRange(crouched, npc.getRoleName());
    boolean los = distance <= range && sector && sight.hasLineOfSight(witness, player, store);
    String reason = distance > range ? "range" : !sector ? "sector" : !los ? "occluded" : "seen";
    return new Evidence(witness, npc.getRoleName(), distance, sector, los, crouched,
        new Vector3d(position), reason, new Vector3d(t.getPosition()), head.getRotation().yaw());
  }
  private static Evidence missing(String reason) {
    return new Evidence(null, "missing", Double.NaN, false, false, false, null, reason, null, Float.NaN);
  }
}
