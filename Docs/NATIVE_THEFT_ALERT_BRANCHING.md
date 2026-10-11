# Native container theft alert branching - container.11

**REAL-CLIENT PROVEN — Firebeard report, 2026-10-10.** Stable target =0.6.8; development checkpoint, no public release.

Symmetric NEARBY trials passed with both witnesses visible, Kweebec LOS blocked, and Feran LOS blocked. Exactly one caught notification and both Razorleaf/Sharptooth responses occurred in every trial. FACTION and NEARBY ownership/alert branches are proven for the configured participants. See CONTAINER_11_PROOF_AND_PREFAB_PROPERTY.md for the end-of-night record and next-scope investigation.

Before: `deliverContainer(...)` inferred the danger signal from the first confirmed witness's faction. NEARBY therefore only selected that witness's faction guard.

After: the successful withdrawal callback retains a clone of its eligible persistent ownership data. Visual capture/attribution remains unchanged. One `notifyWitnessed` call confirms a seen witness and sends the original caught message; only then ownership scope chooses the alert. FACTION validates that witness against ownership and retains the existing Kweebec_All_Danger or Feran_Warriors_Attack nearest-guard adapter. NEARBY selects HyThief_Local_Theft regardless of witness family.

LOCAL uses a 30-block beacon delivery radius centered on the confirmed witness, matching native role alarm declarations. This is NOT a visual detection increase: witnessing stays 20 standing / 6 crouched, normal HyThief perception stays 15/4. The opposite test responder is approximately 26 blocks from the witness, so the old 20-block faction-delivery radius cannot deliver to both. FACTION retains its existing 20-block handoff.

## Java walkthrough: successful theft and one notification

Source: src-java/com/local/pickpocket/HyThiefContainerDiagnostics.java. `caughtOwnership` is populated only after the existing successful owned-removal eligibility gate. The post-transaction handoff is:

```java
      if (!executed) fallback(player, packet);
    } finally {
      for (var event : registrations) event.unregister();
    }
    // All inventory locks/callbacks have unwound. One downstream notification per request.
    ContainerCaughtNotification.notifyWitnessed(confirmed, player::sendMessage).ifPresent(visual -> {
      log("Request=" + request + " Action=CAUGHT_NOTIFICATION Player=" + player.getUuid() + " Witness=" + visual.role());
      var delivery = HyThiefContainerAlert.deliver(plugin, caughtOwnership[0], ref, visual.witness(), store, message -> log("Request=" + request + " " + message));
      log("Request=" + request + " Action=" + (delivery.sent() ? "DANGER_SENT" : "DANGER_NOT_SENT")
          + " Witness=" + visual.role() + " AlertType=" + delivery.type() + " Signal=" + delivery.signal() + " Responders=" + delivery.responders() + " Reason=" + delivery.reason());
    });
  }
  private static String identity(Object value) { return value == null ? "null" : value.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(value)); }
```

The notification mechanism is unchanged and is called once per native transfer request, outside the responder loop. Multiple listeners do not produce multiple player messages.

## Java walkthrough: ownership classification and native dispatch

Source: src-java/com/local/pickpocket/HyThiefContainerAlert.java:

```java
package com.local.pickpocket;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.components.messaging.BeaconBroadcast;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Ownership selects the crime signal; native Beacon sensors and role actions own the response. */
final class HyThiefContainerAlert {
  static final String LOCAL = "HyThief_Local_Theft";
  // Native Kweebec/Feran role alarm distance. Independent of 20/6 visual witnessing.
  static final double LOCAL_RADIUS = 30.0;
  record Result(String type, String signal, List<Ref<EntityStore>> responders, String reason) {
    boolean sent() { return !responders.isEmpty(); }
  }
  private HyThiefContainerAlert() {}

  static Result deliver(PickpocketPlugin plugin, OwnershipProofData ownership,
      Ref<EntityStore> offender, Ref<EntityStore> witness, Store<EntityStore> store,
      Consumer<String> diagnostic) {
    Consumer<String> trace = diagnostic == null ? message -> {} : diagnostic;
    if (!PersistentContainerOwnership.eligible(ownership))
      return new Result("NONE", "", List.of(), "invalid-ownership");
    if (store == null || offender == null || witness == null || !offender.isValid()
        || !witness.isValid() || offender.getStore() != store || witness.getStore() != store)
      return new Result("NONE", "", List.of(), "invalid-offender-or-witness");
    if ("FACTION".equals(ownership.scope())) {
      if (ContainerOwnershipPolicy.rejection(ownership, witness, store) != null)
        return new Result("FACTION", "", List.of(), "witness-faction-mismatch");
      String signal = switch (ownership.faction()) {
        case "Kweebec" -> "Kweebec_All_Danger";
        case "Feran" -> "Feran_Warriors_Attack";
        default -> "";
      };
      if (signal.isEmpty()) return new Result("FACTION", "", List.of(), "unsupported-faction");
      trace.accept("Action=THEFT_ALERT_CLASSIFIED Type=FACTION Faction=" + ownership.faction() + " Signal=" + signal);
      var delivery = HyThiefGuardAlert.deliverContainer(plugin, offender, witness, store, ownership.faction(), trace);
      return new Result("FACTION", signal, delivery.sent() ? List.of(delivery.guard()) : List.of(), delivery.reason());
    }
    if (!"NEARBY".equals(ownership.scope()))
      return new Result("NONE", "", List.of(), "unsupported-scope");
    trace.accept("Action=THEFT_ALERT_CLASSIFIED Type=LOCAL Signal=" + LOCAL + " Radius=" + LOCAL_RADIUS);
    var origin = store.getComponent(witness, TransformComponent.getComponentType());
    if (origin == null) return new Result("LOCAL", LOCAL, List.of(), "missing-witness-transform");
    var candidates = new ArrayList<Ref<EntityStore>>();
    store.getResource(NPCPlugin.get().getNpcSpatialResource()).getSpatialStructure()
        .collect(origin.getPosition(), LOCAL_RADIUS, candidates);
    var delivered = new ArrayList<Ref<EntityStore>>();
    for (var candidate : candidates) {
      if (candidate == null || !candidate.isValid() || candidate.equals(offender)) continue;
      var npc = store.getComponent(candidate, NPCEntity.getComponentType());
      if (npc == null || npc.getRole() == null || !plugin.isPickpocketableRole(npc.getRoleName())) continue;
      var beacon = store.getComponent(candidate, BeaconSupport.getComponentType());
      int slot = slot(beacon);
      if (slot < 0) continue; // Native role sensor is the data-driven opt-in, not a Java role-ID list.
      var state = store.getComponent(candidate, StateSupport.getComponentType());
      String name = state == null ? "missing" : state.getStateName();
      boolean ready = (name.equals("Idle") || name.startsWith("Idle.") || name.equals("Alerted") || name.startsWith("Alerted."))
          && beacon.isMessageEnabled(slot) && !beacon.isMessageQueued(slot)
          && store.getComponent(candidate, DeathComponent.getComponentType()) == null;
      trace.accept("Action=LOCAL_LISTENER Role=" + npc.getRoleName() + " State=" + name + " Slot=" + slot + " Ready=" + ready);
      if (!ready || delivered.contains(candidate)) continue;
      try {
        BeaconBroadcast.post(candidate, LOCAL, offender, 1.0, store);
        boolean accepted = beacon.isMessageQueued(slot) && offender.equals(beacon.peekMessage(slot));
        trace.accept("Action=LOCAL_DANGER_QUEUED Role=" + npc.getRoleName() + " Signal=" + LOCAL + " ExactOffender=" + accepted);
        if (accepted) delivered.add(candidate);
      } catch (RuntimeException error) {
        plugin.getLogger().at(java.util.logging.Level.WARNING).withCause(error).log("HyThief local theft beacon delivery failed");
        trace.accept("Action=LOCAL_DANGER_ERROR Role=" + npc.getRoleName());
      }
    }
    return new Result("LOCAL", LOCAL, List.copyOf(delivered), delivered.isEmpty() ? "no-ready-local-listener" : "native-local-beacon-posted");
  }
  private static int slot(BeaconSupport beacon) {
    if (beacon == null || beacon.getMessageSlots() == null) return -1;
    for (int i = 0; i < beacon.getMessageSlots().length; i++)
      if (LOCAL.equals(beacon.getMessageTextForIndex(i))) return i;
    return -1;
  }
}

```

The LOCAL recipient loop uses the existing native NPC spatial resource and discovers capability from BeaconSupport's registered HyThief_Local_Theft slot. Current compatible/supported roles must opt in through their role tree, have that sensor enabled, be in existing ready Idle/Alerted states, and be alive. No Java list of LOCAL role IDs, security group registry, forced state, manual target assignment, movement, pathfinding, attack timer or persistent polling was added. Each ready listener receives the same native BeaconBroadcast.post with the offender Ref and the existing one-second message lifetime. Native role sensors consume it.

FACTION still calls the HyThiefGuardAlert adapter with an explicit ownership-faction argument after verifying the selected ownership faction; its native signals, guard policy, radius, LastSeen, and targeting are unchanged. Unsupported ownership branches fail closed; PREFAB is not implemented.

## Native listener data

The new LOCAL listener is added alongside the existing faction beacon sensor in the ACTIVE test-world Razorleaf fixture and Feran field template. These scoped development assets override their main/native roles in this save, so the listener must be added there. The production JAR's Server/Common/config entries remain byte-identical to container.10. No universal responder enrollment is claimed; future compatible role assets can add this same native listener.

```json
{
  "Sensor": {
    "Type": "Or",
    "Sensors": [
      {"Type": "Beacon", "Message": "Kweebec_All_Danger", "TargetSlot": "LockedTarget"},
      {"Type": "Beacon", "Message": "HyThief_Local_Theft", "TargetSlot": "LockedTarget"}
    ]
  },
  "Actions": [
    {"Type": "StorePosition", "Slot": "LastSeen"},
    {"Type": "State", "State": "Attack"}
  ]
}
```

The Feran existing Or tree retains Feran_All_Attack / Feran_Warriors_Attack and gains the same generic Beacon leaf. Its existing StorePosition and Attack actions are untouched. Sensor output sets LockedTarget to the posted offender; existing actions retain LastSeen and enter Attack. Native attack/chase/search/disengagement remain the role's responsibility. No explicit ExecutionSupport notification is needed for queued beacon consumption; the native matching sensor drives execution.

Source verification: installed stable Server/HytaleServer.jar matches pinned SHA256 dcd2956cc65b950084650eadd56b889cf51f7610c127c3daac1f74dc471fec97. Inspected current BeaconBroadcast/BeaconSupport APIs and native/HyThief-composed Beacon sensor instruction trees. Official API web pages were unavailable through the browser; binary API and native role assets are the concrete implementation evidence. Official [NPC technical rundown](https://hytale.com/news/2026/2/npc-technical-rundown) explains sensor/action composition; [Update 6 patch notes](https://hytale.com/news/2026/8/update-6-patch-notes) reviewed for current NPC/beacon changes. No pre-release API adopted.

## Runtime handoff

Four-chest layout unchanged: CONTROL | KWEEBEC | NEARBY | FERAN. Witnesses fixed, responders mobile, no permanent LOS barriers, 64 floor blocks in the starting hotbar. Use standing. Reset with Hytale closed between caught tests.

1. CONTROL: no caught notification or theft response.
2. KWEEBEC: exactly one caught message; Razorleaf only.
3. Reset, FERAN: exactly one caught message; Sharptooth only.
4. Reset, NEARBY: one caught message; both responders.
5. Reset, block Kweebec witness LOS, NEARBY: Feran witness; still both responders.
6. Reset, block Feran witness LOS, NEARBY: Kweebec witness; still both responders.

Do not wall off the responders or their pursuit routes. Native validation is not a substitute for the real-client test. Live logs: OWNED_REMOVE_CAPTURE/REMOVE SUCCESS, VISUAL_CANDIDATE, CAUGHT_NOTIFICATION, THEFT_ALERT_CLASSIFIED, LOCAL_LISTENER, LOCAL_DANGER_QUEUED (ExactOffender), DANGER_SENT with AlertType/Signal/Responders. FACTION retains existing GUARD_CANDIDATE/NATIVE_DANGER_QUEUED/NATIVE_GUARD_OBSERVATION diagnostics.

Full rollback: DevTesting/Container_Theft_Test_World/Rollback-container.10-before-local-20261010-202845 (source-assets ZIP, old sources, world/assets/helpers, installed container.10 JAR). Archived v1.2 hashes verified unchanged.

## Validation and deployment evidence

Clean build passed with the two existing inventory warnings and Gradle deprecation warning. Disposable geometry checker retains its known getFiller deprecation warning. Production Server/Common/config bytes, protected backend source bytes, native guard delivery body, original world geometry/player data, archived releases, and responder actions verified unchanged.

```text
LocalFeran [20261010-203310-372d13141f754eeab42565df2b4c4196]
THEFT_NATIVE_RESPONDER Role=Kweebec_Razorleaf Expected=true Targeted=true State=Attack.Default Movement=10.971857935807078[m
THEFT_NATIVE_RESPONDER Role=Feran_Sharptooth Expected=true Targeted=true State=Attack.Ranged Movement=14.36326680053731[m
FACTION_NATIVE_RESPONSE_PASS Test=LocalFeran Type=LOCAL Signal=HyThief_Local_Theft Responders=2 Notifications=1 Witness=Feran_Civilian_HyThiefWitness[m
LocalKweebec [20261010-203332-c2998dd119a94092b748da83474c91fa]
THEFT_NATIVE_RESPONDER Role=Kweebec_Razorleaf Expected=true Targeted=true State=Attack.Default Movement=10.988855909981446[m
THEFT_NATIVE_RESPONDER Role=Feran_Sharptooth Expected=true Targeted=true State=Attack.Ranged Movement=14.416572832553872[m
FACTION_NATIVE_RESPONSE_PASS Test=LocalKweebec Type=LOCAL Signal=HyThief_Local_Theft Responders=2 Notifications=1 Witness=Kweebec_Sapling_HyThiefWitness[m
Kweebec [20261010-203739-2f5cf97592bb4bfcbfb9b3879e90efde]
THEFT_NATIVE_RESPONDER Role=Kweebec_Razorleaf Expected=true Targeted=true State=Attack.Default Movement=10.840710978865593[m
THEFT_NATIVE_RESPONDER Role=Feran_Sharptooth Expected=false Targeted=false State=Idle.Default Movement=0.0[m
FACTION_NATIVE_RESPONSE_PASS Test=Kweebec Type=FACTION Signal=Kweebec_All_Danger Responders=1 Notifications=1 Witness=Kweebec_Sapling_HyThiefWitness[m
Feran [20261010-203801-146d5948f3e44d6481d52c64272d4934]
THEFT_NATIVE_RESPONDER Role=Kweebec_Razorleaf Expected=false Targeted=false State=Idle.Default Movement=0.0[m
THEFT_NATIVE_RESPONDER Role=Feran_Sharptooth Expected=true Targeted=true State=Attack.Ranged Movement=12.116087265721209[m
FACTION_NATIVE_RESPONSE_PASS Test=Feran Type=FACTION Signal=Feran_Warriors_Attack Responders=1 Notifications=1 Witness=Feran_Civilian_HyThiefWitness[m
```

LOCAL cases passed before the final explicit-ownership faction overload; the LOCAL code and role branches remained unchanged afterward. Both FACTION cases were rerun against the final JAR. Every run also passed four-chest identity, ownership persistence, visual FOV/LOS, paired withdrawal/notification, head/facing, mobility and decoded starting inventory checks. Combat/temporary-block saves were never promoted to Master.

Installed JAR SHA256: 08ddb2033d942c901a52de3ef20429e83f21db7212bf676e5287f05b0123c233. Exactly one active HyThief plugin; previous installed file preserved at Rollback-container.10-before-local-20261010-202845/Installed-Removed-HyThief-container.10.jar. Reset completed and all 33 live files match Master hashes, including four chests and the 64-block hotbar. Unrelated Mods hashes and other-world file metadata match the before snapshots. Dirty save archive: Archives/20261010-203909-28538069596c4f72bc3371f8267b18b0.

Real-client checkpoint completed: all three symmetric LOCAL witness cases passed, each with one notification and both native responders. Only the scoped test responders opt into the generic listener so far; this is not proof for all arbitrary roles. No public release.
