package com.local.pickpocket;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

/** Container development builds only; same serialized fields/ID as the persistence proof. */
public final class OwnershipProofData implements Component<ChunkStore> {
  public static final BuilderCodec<OwnershipProofData> CODEC =
      BuilderCodec.builder(OwnershipProofData.class, OwnershipProofData::new)
          .append(new KeyedCodec<>("Owned", Codec.BOOLEAN),
              (data, value) -> data.owned = value, data -> data.owned).add()
          .append(new KeyedCodec<>("Scope", Codec.STRING),
              (data, value) -> data.scope = value, data -> data.scope).add().build();
  private static ComponentType<ChunkStore, OwnershipProofData> type;
  private boolean owned;
  private String scope = "";
  public OwnershipProofData() {}
  public OwnershipProofData(boolean owned, String scope) { this.owned=owned; this.scope=scope; }
  static void register(PickpocketPlugin plugin) {
    type=plugin.getChunkStoreRegistry().registerComponent(
        OwnershipProofData.class, "HyThiefOwnershipProof", CODEC);
  }
  public static ComponentType<ChunkStore, OwnershipProofData> getComponentType() { return type; }
  public boolean owned() { return owned; }
  public String scope() { return scope; }
  @Override public OwnershipProofData clone() { return new OwnershipProofData(owned,scope); }
}
