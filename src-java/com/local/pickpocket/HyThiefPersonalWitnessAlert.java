package com.local.pickpocket;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.messaging.BeaconBroadcast;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.*;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

/** Personal crime entry only. Native role instructions retain all movement/combat/social behavior. */
final class HyThiefPersonalWitnessAlert {
  private HyThiefPersonalWitnessAlert() {}

  static int deliver(OwnershipProofData ownership, Ref<EntityStore> offender,
      List<ContainerVisualWitness.Evidence> confirmed, Store<EntityStore> store, Consumer<String> trace) {
    if (store == null || offender == null || !offender.isValid() || offender.getStore() != store
        || !PersistentContainerOwnership.eligible(ownership)) return 0;
    int delivered = 0;
    var visited = new HashSet<Ref<EntityStore>>();
    for (var evidence : confirmed) {
      var witness = evidence.witness();
      if (!evidence.seen() || witness == null || !witness.isValid() || witness.equals(offender)
          || witness.getStore() != store || !visited.add(witness)) continue;
      if (ContainerOwnershipPolicy.rejection(ownership, witness, store) != null
          || store.getComponent(witness, DeathComponent.getComponentType()) != null) continue;
      var npc = store.getComponent(witness, NPCEntity.getComponentType());
      if (npc == null || npc.getRole() == null) continue;
      String path = ContainerWitnessProfiles.entry(witness, store);
      if (path.isEmpty() || path.equals("Guard")) continue;
      var state = store.getComponent(witness, StateSupport.getComponentType());
      var marked = store.getComponent(witness, MarkedEntitySupport.getComponentType());
      var world = store.getComponent(witness, WorldSupport.getComponentType());
      if (state == null || marked == null || world == null) continue;
      String before = state.getStateName();
      // Do not replace an ongoing native fight, flee, death, or scripted interaction.
      if (!(before.equals("Idle") || before.startsWith("Idle.")
          || before.equals("Alerted") || before.startsWith("Alerted.")
          || (path.equals("TrorkEnemySighted") && (before.equals("NeutralPlayer")
              || before.startsWith("NeutralPlayer."))))) {
        trace.accept("Action=PERSONAL_WITNESS_SKIP Role=" + npc.getRoleName() + " State=" + before);
        continue;
      }
      if (path.equals("TrorkEnemySighted")) {
        var beacon = store.getComponent(witness, BeaconSupport.getComponentType());
        int slot = -1;
        if (beacon != null && beacon.getMessageSlots() != null)
          for (int i = 0; i < beacon.getMessageSlots().length; i++)
            if (path.equals(beacon.getMessageTextForIndex(i))) { slot = i; break; }
        if (slot < 0 || !beacon.isMessageEnabled(slot) || beacon.isMessageQueued(slot)) continue;
        BeaconBroadcast.post(witness, path, offender, 1.0, store); // ONE receiver; native AI may rally later.
        if (!beacon.isMessageQueued(slot) || !offender.equals(beacon.peekMessage(slot))) continue;
      } else {
        // These existing native Combat.Default branches do not depend on a probabilistic rally
        // acceptance or a flock. Assign only the offender, then let the native tree take over.
        int combat = state.getStateHelper().getStateIndex("Combat");
        if (combat < 0 || state.getStateHelper().getSubStateIndex(combat, "Default") < 0) continue;
        world.overrideAttitude(offender, Attitude.HOSTILE, 60.0); // native damage-hostility duration
        npc.getRole().setMarkedTarget(witness, store, "LockedTarget", offender);
        state.setState(witness, "Combat", "Default", store);
      }
      delivered++;
      trace.accept("Action=PERSONAL_WITNESS_DELIVERED Role=" + npc.getRoleName()
          + " Entry=" + path + " Before=" + before + " After=" + state.getStateName()
          + " ExactOffender=" + offender.equals(marked.getMarkedEntityRef("LockedTarget"))
          + " CustomBroadcast=false");
    }
    return delivered;
  }
}
