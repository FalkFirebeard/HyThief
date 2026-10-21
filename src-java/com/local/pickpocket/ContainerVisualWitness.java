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

/** Fresh native visual evidence captured at successful source removal. */
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
  static List<Evidence> capture(Ref<EntityStore> player, Store<EntityStore> store,
      OwnershipProofData ownership, java.util.Set<String> enabledRoles, java.util.function.Consumer<String> log) {
    return capture(player, store, ownership, ref -> enabledRoles.contains(store.getComponent(ref, NPCEntity.getComponentType()).getRoleName()), log);
  }
  static List<Evidence> capture(Ref<EntityStore> player, Store<EntityStore> store,
      OwnershipProofData ownership, java.util.function.Predicate<Ref<EntityStore>> eligible, java.util.function.Consumer<String> log) {
    var result=new ArrayList<Evidence>();
    if(player==null || !player.isValid())return result;
    var t=store.getComponent(player,TransformComponent.getComponentType());
    if(t==null)return result;
    var movement=store.getComponent(player,MovementStatesComponent.getComponentType());
    boolean crouched=movement!=null && movement.getMovementStates().crouching;
    var position=new Vector3d(t.getPosition());
    var candidates=new ArrayList<Ref<EntityStore>>();
    store.forEachChunk(NPCEntity.getComponentType(),(chunk,buffer)->{
      for(int n=0;n<chunk.size();n++) {
        var ref=chunk.getReferenceTo(n);var npc=store.getComponent(ref,NPCEntity.getComponentType());
        var transform=store.getComponent(ref,TransformComponent.getComponentType());
        if(npc.getRole()!=null && NpcParticipation.allowed(NpcOverrides.Capability.WITNESS,ref,store,eligible.test(ref)) && transform!=null
            && transform.getPosition().distance(position)<=RANGE
            && store.getComponent(ref,DeathComponent.getComponentType())==null)candidates.add(ref);
      }
    });
    candidates.sort(java.util.Comparator.comparing(ref->store.getComponent(ref,NPCEntity.getComponentType()).getRoleName()));
    for(var candidate:candidates) {
      var npc=store.getComponent(candidate,NPCEntity.getComponentType());
      String faction=ContainerOwnershipPolicy.faction(candidate,store);
      String rejected=ContainerOwnershipPolicy.rejection(ownership,candidate,store);
      log.accept("Action=OWNERSHIP_CANDIDATE Role="+npc.getRoleName()+" Faction="+faction
          +" Eligible="+(rejected==null)+" Reason="+(rejected==null?"scope-match":rejected));
      if(rejected!=null)continue;
      var evidence=evaluate(candidate,player,position,crouched,store);
      log.accept("Action=VISUAL_CANDIDATE Faction="+faction+" "+evidence.diagnostic());
      result.add(evidence);
    }
    return result;
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
