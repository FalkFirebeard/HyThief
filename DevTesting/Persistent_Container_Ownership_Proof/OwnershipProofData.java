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
