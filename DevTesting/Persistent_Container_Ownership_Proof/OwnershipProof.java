package com.local.hythief.devownership;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.*;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.*;
import com.hypixel.hytale.server.core.modules.block.*;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.codec.ExtraInfo;
import java.nio.file.*;
import java.util.concurrent.*;

/** Headless lifecycle proof in a disposable save only; never shipped in HyThief. */
public final class OwnershipProof extends JavaPlugin {
    static final int X=-5, Y=64, Z=7;
    static final String CHEST="Furniture_Kweebec_Chest_Large";
    private ComponentType<ChunkStore, OwnershipProofData> ownershipType;
    public OwnershipProof(JavaPluginInit init) { super(init); }
    @Override protected void setup() {
        ownershipType = getChunkStoreRegistry().registerComponent(
            OwnershipProofData.class, "HyThiefOwnershipProof", OwnershipProofData.CODEC);
    }
    @Override protected void start() {
        Universe.get().getUniverseReady().thenRun(() -> {
            World world=Universe.get().getDefaultWorld();
            load(world, () -> {
                try {
                    if (!Files.exists(Path.of("OWNERSHIP_PROOF_FIXTURE")))
                        throw new IllegalStateException("Refusing non-fixture working directory");
                    if (Boolean.getBoolean("ownershipproof.seed")) {
                        if (Files.exists(Path.of("seed-completed.json")))
                            throw new IllegalStateException("Seed already ran; never repair missing ownership by reattaching");
                        if (!world.getBlockType(X,Y,Z).getId().equals("Empty"))
                            throw new IllegalStateException("Proof station must start empty");
                        world.setBlock(X,Y,Z,CHEST);
                        Ref<ChunkStore> ref=chest(world);
                        if (world.getChunkStore().getStore().getComponent(ref,ownershipType)!=null)
                            throw new IllegalStateException("Unexpected existing ownership");
                        world.getChunkStore().getStore().addComponent(ref,ownershipType,new OwnershipProofData(true,"NEARBY"));
                        mark(world);
                        container(world).setItemStackForSlot((short)0,new ItemStack("Ingredient_Bar_Copper",16));
                        log("ATTACHED owned=true scope=NEARBY");
                    } else {
                        require(OwnershipProofData.DECODED.get()>0,"No native deserialization observed");
                        check(world,"INITIAL_LOAD_RESTORED");
                    }
                    lifecycle(world);
                    int decodesBeforeUnload=OwnershipProofData.DECODED.get();
                    Files.writeString(Path.of("seed-completed.json"),"{\"owned\":true,\"scope\":\"NEARBY\"}\n");
                    // Leave/save/reload the same world, off its owning thread to avoid a join deadlock.
                    CompletableFuture.runAsync(() -> {
                        try {
                            String name=world.getName();
                            require(Universe.get().removeWorld(name),"World removal cancelled");
                            log("WORLD_SAVED_AND_UNLOADED encoded="+OwnershipProofData.ENCODED.get());
                            Universe.get().loadWorld(name).thenAccept(reloaded -> load(reloaded, () -> {
                                try {
                                    require(reloaded!=world,"World was not recreated");
                                    check(reloaded,"SAVE_LEAVE_RELOAD_RESTORED");
                                    require(OwnershipProofData.DECODED.get()>decodesBeforeUnload,"Reload did not deserialize ownership again");
                                    require(OwnershipProofData.ENCODED.get()>0,"No codec encode calls observed");
                                    require(OwnershipProofData.DECODED.get()>0,"No codec decode calls observed");
                                    Files.writeString(Path.of("result-"+System.getProperty("ownershipproof.phase","unknown")+".json"),
                                        "{\"passed\":true,\"owned\":true,\"scope\":\"NEARBY\",\"encodes\":"+OwnershipProofData.ENCODED.get()+",\"decodes\":"+OwnershipProofData.DECODED.get()+"}\n");
                                    log("PASS phase="+System.getProperty("ownershipproof.phase")+" attachmentAllowed="+Boolean.getBoolean("ownershipproof.seed"));
                                    stop();
                                } catch(Throwable error) { fail(error); }
                            })).exceptionally(error -> { fail(error); return null; });
                        } catch(Throwable error) { fail(error); }
                    });
                } catch(Throwable error) { fail(error); }
            });
        }).exceptionally(error -> { fail(error); return null; });
    }
    private void load(World world,Runnable action) {
        world.getChunkStore().getChunkSectionReferenceAtBlockAsync(X,Y,Z)
            .thenAccept(section -> world.execute(action))
            .exceptionally(error -> { fail(error); return null; });
    }
    private Ref<ChunkStore> chest(World world) {
        var cs=world.getChunkStore(); var store=cs.getStore();
        var section=cs.getChunkSectionReferenceAtBlock(X,Y,Z);
        var components=store.ensureAndGetComponent(section,BlockComponentSection.getComponentType());
        var blocks=store.getComponent(section,BlockSection.getComponentType());
        var type=world.getBlockType(X,Y,Z);
        require(type.getId().equals(CHEST)||type.getId().equals(BlockType.getAssetMap().getAsset(CHEST).getBlockForState("OpenWindow").getId())||type.getId().equals(BlockType.getAssetMap().getAsset(CHEST).getBlockForState("CloseWindow").getId()),"Unexpected native chest block "+type.getId());
        return BlockEntity.ensureBlockEntity(store,section,components,X,Y,Z,type,blocks.getRotationIndex(X,Y,Z));
    }
    private SimpleItemContainer container(World world) {
        var component=world.getChunkStore().getStore().getComponent(chest(world),ItemContainerBlock.getComponentType());
        require(component!=null&&component.getItemContainer()!=null,"Native inventory unavailable");
        return component.getItemContainer();
    }
    private void check(World world,String boundary) {
        var data=world.getChunkStore().getStore().getComponent(chest(world),ownershipType);
        require(data!=null&&data.owned()&&"NEARBY".equals(data.scope()),"Ownership lost at "+boundary);
        log(boundary+" owned="+data.owned()+" scope="+data.scope());
    }
    private void mark(World world) {
        var cs=world.getChunkStore();var section=cs.getChunkSectionReferenceAtBlock(X,Y,Z);
        var components=cs.getStore().getComponent(section,BlockComponentSection.getComponentType());
        components.markBlockNeedsSaving(ChunkUtil.indexBlock(X,Y,Z));
        components.markNeedsSaving();
    }
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
    static void require(boolean result,String message) { if(!result)throw new IllegalStateException(message); }
    static void log(String message) { System.out.println("[HyThief OwnershipTest] "+message); }
    static void stop() { CompletableFuture.runAsync(() -> HytaleServer.get().shutdownServer()); }
    static void fail(Throwable error) { log("FAILED "+error);error.printStackTrace();stop(); }
}
