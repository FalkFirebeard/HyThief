# Persistent container ownership proof — stable Hytale 0.6.8

Date: 2026-10-10. Development proof only. No production ownership resolution, creator tooling, Nearby/Faction/Prefab policy, public release, or active deployment.

## Outcome and scope

**Native-server persistence proof passed.** An ordinary `Furniture_Kweebec_Chest_Large` carries a codec-backed `HyThiefOwnershipProof` component beside its native `ItemContainerBlock`. `Owned=true` and `Scope=NEARBY` survive native state changes, successful native inventory operations, actual world save/unload/reload, and two additional fresh Java server processes. No world-position ownership registry is consulted. The component is never reattached during restart verification.

This proves the native attachment/save foundation for this ordinary chest. It is not a new physical client UI test: no player connected, no window packet was sent, and no new client gameplay regression result is claimed. The headless harness invokes the exact native state-change method used by `OpenContainerInteraction` on opening and its close callback. Existing real-client-proven one-click chest mechanics and all active gameplay code remain unchanged. A full Hytale client restart was not needed to test serialization: separate Java server processes already discarded all server memory and decoded the saved component.

All proof code is isolated under `DevTesting/Persistent_Container_Ownership_Proof`. The helper JAR is used only in a disposable native-server fixture. It was **not** installed in UserData/Mods and does not ship in the public HyThief JAR. Active HyThief remains `1.3.1-container.8`; exactly one HyThief gameplay JAR is active. No Firebeard manual marking, coordinates, setup commands, or resets were needed.

## Fixture and safety

- `Fixture` is an isolated copy of `DevTesting/Container_Theft_Test_World/Master`; the original master, reset scripts and playable saves were not edited.
- A new ordinary chest at `(-5,64,7)` is the sole ownership-proof subject in that copy. Its diagnostic identifier is `HyThief OwnershipTest`; no special chest asset/interaction is created.
- `ownershipproof.seed=true` authorizes one initial attachment. A completed-seed marker prevents repeating setup to hide a loss. This marker is a harness guard only, not an ownership store.
- On later process starts and same-process world reloads, the component is read from the live block entity; missing ownership fails immediately. No auto-registration or coordinate registry restores it.
- The helper uses loopback port 15546 and offline auth; the base-game installation is read-only.
- Protected content hashes matched for gameplay source, assets, build support, config, archived releases, installed mods, all existing playable saves and reusable masters/packs/prefabs. Evidence: `protected-before.json` and `protected-verification.json` in the proof folder.

The isolated save already contains owned metadata after authoring. Do not copy its helper JAR or save into live gameplay without a separate testing decision. The fixture retains existing unrelated stations only as copied background; they were not redesigned.

## Results for each requested lifecycle boundary

| Test | Observed result | Evidence/limit |
|---|---|---|
| Initial attachment/load | PASS, true/NEARBY | Attached once on seed; every later initial load restored from disk |
| Native open | PASS | Native `OpenWindow` state applied twice in each process; state asset ID asserted |
| Native close | PASS | Native `CloseWindow` state applied twice in each process; state asset ID asserted |
| Withdrawal | PASS | Copper 16→12 in chest, recipient received 4; metadata retained |
| Insertion | PASS | Four copper returned, chest restored to 16; metadata retained |
| Internal rearrangement | PASS | Four copper moved to slot 5 and back; counts and metadata asserted |
| Save → leave → reload | PASS | `Universe.removeWorld` completed save/shutdown; `loadWorld` produced a new World and decoded ownership again |
| Full server/world restart | PASS twice | New Java PIDs; seed=false; metadata present before lifecycle operations |
| Full Hytale client restart | Not necessary for server serialization proof | No physical client window/input test repeated |
| Native open/closed block state transition | PASS | Same ECS Ref and same ItemContainer object, ownership retained |
| Whole-holder serialization | PASS | Registered ChunkStore holder encoded ownership and decoded true/NEARBY |
| Existing mechanics/worlds protected | PASS by hash comparison | No gameplay code/build/deployment change; no new behavioral regression test claimed |

Three processes completed with `PASS`: seed PID 20756, restart-1 PID 22496, restart-2 PID 17280. Final phase results report 3 encode observations per phase; decode observations were 3 for seed and 4 for each restart. Getter counters observe codec usage; counters alone are not disk-save proof. Actual restoration after world unload and fresh process launch is the disk-save proof.

Local raw logs: `seed.log`, `restart-1.log`, `restart-2.log`; compact summary: `results.json`. Native startup includes existing HyThief stealth-asset unknown-attribute warnings for BaseRange/Channel/Projected; these are outside this checkpoint and were not changed. There were no proof compilation errors or lifecycle failures.

Serialized holder excerpt from the final process:

```json
{
  "Components": {
    "ItemContainerBlock": {
      "ItemContainer": { "Capacity": 36, "Items": { "0": { "Id": "Ingredient_Bar_Copper", "Quantity": 16 } } },
      "Capacity": 36
    },
    "HyThiefOwnershipProof": { "Owned": true, "Scope": "NEARBY" }
  }
}
```

The excerpt omits ordinary ItemStack durability/quality fields for readability. The full native encoding is retained in `Fixture/serialized-holder-restart-2.json`.

## How the Java fits together

Previously, test ownership was only a transient entry in `OwnedContainerRegistry` keyed by world/position and live inventory identity. That code is unchanged. The proof adds a separate serializable component on the **same native block entity**, without modifying inventory behavior or using that registry. Positions locate the test chest; they are not fields in its ownership component.

The complete small data class is below. `Component<ChunkStore>` makes this block-attached data; `BuilderCodec` names the persisted fields; `clone()` gives save snapshots their own copy. Empty/false constructor defaults deliberately ensure a missing field cannot masquerade as true/NEARBY. The counters are development instrumentation only and should not become production gameplay fields.

```java
package com.local.hythief.devownership;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.concurrent.atomic.AtomicInteger;

/** Development-only schema. No faction/prefab policy or gameplay ownership resolution. */
public final class OwnershipProofData implements Component<ChunkStore> {
    public static final AtomicInteger ENCODED = new AtomicInteger();
    public static final AtomicInteger DECODED = new AtomicInteger();
    public static final BuilderCodec<OwnershipProofData> CODEC =
        BuilderCodec.builder(OwnershipProofData.class, OwnershipProofData::new)
            .append(new KeyedCodec<>("Owned", Codec.BOOLEAN),
                (data, value) -> data.owned = value, data -> {
                    ENCODED.incrementAndGet(); return data.owned;
                }).add()
            .append(new KeyedCodec<>("Scope", Codec.STRING),
                (data, value) -> data.scope = value, data -> data.scope).add()
            .afterDecode(data -> DECODED.incrementAndGet())
            .build();

    // Deliberately NOT true/NEARBY defaults: missing saved fields must fail the proof.
    private boolean owned;
    private String scope = "";
    public OwnershipProofData() {}
    public OwnershipProofData(boolean owned, String scope) {
        this.owned = owned; this.scope = scope;
    }
    public boolean owned() { return owned; }
    public String scope() { return scope; }
    @Override public OwnershipProofData clone() {
        return new OwnershipProofData(owned, scope);
    }
}
```

Registration happens during plugin setup, before world/chunk loading. Supplying the codec is what makes this component serializable; registering only a runtime supplier would not be enough. `HyThiefOwnershipProof` is a development codec ID, not an approved final production schema/name.

```java
public final class OwnershipProof extends JavaPlugin {
    private ComponentType<ChunkStore, OwnershipProofData> ownershipType;
    public OwnershipProof(JavaPluginInit init) { super(init); }
    @Override protected void setup() {
        ownershipType = getChunkStoreRegistry().registerComponent(
            OwnershipProofData.class, "HyThiefOwnershipProof", OwnershipProofData.CODEC);
    }
}
```

The initial seed runs on the world thread, activates the normal chest's native block entity, and calls:

```java
world.setBlock(X, Y, Z, CHEST);
Ref<ChunkStore> ref = chest(world);
world.getChunkStore().getStore().addComponent(
    ref, ownershipType, new OwnershipProofData(true, "NEARBY"));
mark(world);
```

`chest(world)` resolves the current section and uses current `BlockEntity.ensureBlockEntity` with native block type/rotation. It creates no custom container. Ownership reads use `store.getComponent(ref, ownershipType)`. Every assertion checks the actual live component, not the marker JSON.

The dirty-marking helper names the changed block in `BlockComponentSection` and marks the section for saving. This is deliberate: do not rely on mutating a Java object alone to cause a save.

```java
    private void mark(World world) {
        var cs=world.getChunkStore();var section=cs.getChunkSectionReferenceAtBlock(X,Y,Z);
        var components=cs.getStore().getComponent(section,BlockComponentSection.getComponentType());
        components.markBlockNeedsSaving(ChunkUtil.indexBlock(X,Y,Z));
        components.markNeedsSaving();
    }
```

The lifecycle method supplies the surrounding test context. It verifies that each operation actually changed its native state/items, then checks ownership. The final holder round trip is additional codec validation; it does not replace real world restart testing.

```java
    private void lifecycle(World world) throws Exception {
        check(world,"INITIAL_ATTACHED_OR_LOADED");
        var cs=world.getChunkStore(); var section=cs.getChunkSectionReferenceAtBlock(X,Y,Z);
        var original=chest(world);var inventory=container(world);
        var base=BlockType.getAssetMap().getAsset(CHEST);
        for(String state:new String[]{"OpenWindow","CloseWindow","OpenWindow","CloseWindow"}) {
            BlockOperations.setBlockInteractionState(cs,section,X,Y,Z,base,state,false);
            var changed=world.getBlockType(X,Y,Z);
            require(changed.getId().equals(base.getBlockForState(state).getId()),"State change did not execute "+state);
            require(chest(world).equals(original),"Normal state change replaced underlying entity");
            require(container(world)==inventory,"Normal state change replaced inventory");
            check(world,"STATE_CHANGE state="+state+" sameEntity=true sameInventory=true retained=true");
        }
        // Native successful transfers. No fabricated transaction or ownership registry.
        var recipient=new SimpleItemContainer((short)4);
        require(inventory.getItemStack((short)0).getQuantity()==16,"Saved test loot missing");
        inventory.moveItemStackFromSlotToSlot((short)0,4,recipient,(short)0);
        require(recipient.getItemStack((short)0).getQuantity()==4&&inventory.getItemStack((short)0).getQuantity()==12,"Withdrawal failed");
        check(world,"WITHDRAWAL");
        recipient.moveItemStackFromSlotToSlot((short)0,4,inventory,(short)0);
        require(inventory.getItemStack((short)0).getQuantity()==16,"Insertion failed");
        check(world,"INSERTION");
        inventory.moveItemStackFromSlotToSlot((short)0,4,inventory,(short)5);
        require(inventory.getItemStack((short)5).getQuantity()==4&&inventory.getItemStack((short)0).getQuantity()==12,"Internal rearrangement failed");
        check(world,"INTERNAL_REARRANGEMENT");
        inventory.moveItemStackFromSlotToSlot((short)5,4,inventory,(short)0);
        require(inventory.getItemStack((short)0).getQuantity()==16,"Inventory restoration failed");
        var holder=cs.getStore().copySerializableEntity(chest(world));
        var encoded=ChunkStore.REGISTRY.serialize(holder);
        Files.writeString(Path.of("serialized-holder-"+System.getProperty("ownershipproof.phase")+".json"),encoded.toJson());
        var roundTrip=ChunkStore.REGISTRY.deserialize(encoded);
        var restored=roundTrip.getComponent(ownershipType);
        require(restored!=null&&restored.owned()&&"NEARBY".equals(restored.scope()),"Registry codec round trip failed");
        log("SERIALIZED_HOLDER_ROUND_TRIP owned=true scope=NEARBY");
        mark(world);
    }
```

The world-reload portion calls `removeWorld` off the world thread so its shutdown join cannot deadlock the thread it is trying to stop. It then calls `loadWorld`, loads the chest section, verifies a new World object and a new decode observation, and reads ownership without adding it. `run.py` launches two further fresh Java processes with the seed property absent and rejects any ATTACHED diagnostic during those runs. `seed-completed.json` is never read to reconstruct ownership.

## Why normal native state changes preserve it

Installed/copied stable server JAR SHA256: `DCD2956CC65B950084650EADD56B889CF51F7610C127C3DAAC1F74DC471FEC97`.

`OpenContainerInteraction` calls `BlockOperations.setBlockInteractionState(..., "OpenWindow", false)`; its close callback calls the same method with `CloseWindow`. That method calls `setBlock` with flags 198, including bit 2. `setBlock` skips block-entity replacement when bit 2 is present. The block's state asset changes, but its native entity/inventory remains. The runtime Ref/identity assertions confirm this for the test chest.

Therefore **no ownership migration/copy handler is required for these normal open/closed transitions**, and none was added. Generic `setBlock` or `BlockEntity.setBlockEntity` without preservation may still replace a holder; this checkpoint does not claim all replacement operations preserve custom data. Copying ownership blindly into any new block at the same XYZ would be incorrect.

Registration and section saves serialize `HyThiefOwnershipProof` beside `ItemContainerBlock` through the ChunkStore registry. The subsequent world/process loads restore it via the registered codec. No external position-based ownership file participates.

Reference: [official 0.6.8 block holder documentation](https://docs.hytale.com/assets/item/block/blocks/) and [Update 6 notes](https://hytale.com/news/2026/8/update-6-patch-notes). Exact local API/bytecode evidence: `OpenContainerInteraction.txt`, `BlockOperations.txt`, and the previous `Production_Ownership_Reference_0_6_8` snapshots of BlockEntity, BlockComponentSection and RocksDbChunkStorageProvider$Saver.

## Limits and next checkpoint

The attached component is a sound foundation for this ordinary native container. This is not yet a guarantee for every custom chest, carried block, editor/worldgen/prefab path, whole-holder replacement, plugin-absent save, or entity-backed inventory. Break/new placement, copy remapping and custom container lifecycle tests belong to their later checkpoints. NEARBY here is a persisted label only: no witness membership or scope behavior is implemented.

Recommended next single checkpoint: connect one development fixture's **already attributed successful withdrawal** to reading attached ownership, then invoke the unchanged witness/caught/guard pipeline for true/NEARBY. Prove absent/false ownership safe and true+unseen safe. Do not implement Faction/Prefab or broad creator UI yet. Adopt a separately reviewed versioned production codec before saving ownership to real worlds.

There is no active test JAR swap or reset required for this headless proof. If physical client UI confirmation is requested later, stage a dedicated labeled owned chest with read-only diagnostics; do not reuse a seed-on-load routine that could conceal metadata loss.

## Files and reproducibility

Proof sources/scripts: `DevTesting/Persistent_Container_Ownership_Proof/{OwnershipProofData.java,OwnershipProof.java,manifest.json,prepare.py,run.py,Run-Proof.ps1}`. `Run-Proof.ps1` creates a fresh timestamped directory under `Runs`, copies the protected master into that disposable directory, builds the isolated helper and runs all three phases. It never clears previous evidence or playable worlds. It uses workspace JDK 25 and the pinned 0.6.8 server. Run only this proof runner, not the helper JAR in normal gameplay. The original root fixture/results are retained.

Close-out is restricted to this note and authored proof source/scripts/results. Native third-party bytecode, binary worlds/JARs, copied assets and large protected snapshots remain local. Stop for Firebeard review before implementing ownership behavior.
`Run-Proof.ps1` was also executed successfully end-to-end in fresh reproduction `Runs/20261010-232655-1593510`: all three phases passed and protected hashes matched again.
