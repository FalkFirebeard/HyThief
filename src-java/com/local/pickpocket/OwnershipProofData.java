package com.local.pickpocket;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

/** Persistent container ownership. The established serialized ID is retained for save compatibility. */
public final class OwnershipProofData implements Component<ChunkStore> {
  public static final BuilderCodec<OwnershipProofData> CODEC =
      BuilderCodec.builder(OwnershipProofData.class, OwnershipProofData::new)
          .append(new KeyedCodec<>("Owned", Codec.BOOLEAN),
              (data, value) -> data.owned = value, data -> data.owned).add()
          .append(new KeyedCodec<>("Scope", Codec.STRING),
              (data, value) -> data.scope = value, data -> data.scope).add()
          .append(new KeyedCodec<>("Faction", Codec.STRING),
              (data, value) -> data.faction = value, data -> data.faction).add().build();
  private static ComponentType<ChunkStore, OwnershipProofData> type;
  private boolean owned;
  private String scope = "";
  private String faction = "";
  public OwnershipProofData() {}
  public OwnershipProofData(boolean owned, String scope) { this.owned=owned; this.scope=scope; }
  public OwnershipProofData(boolean owned,String scope,String faction) { this(owned,scope);this.faction=faction; }
  public String faction() { return faction; }
  static void register(PickpocketPlugin plugin) {
    type=plugin.getChunkStoreRegistry().registerComponent(
        OwnershipProofData.class, "HyThiefOwnershipProof", CODEC);
  }
  public static ComponentType<ChunkStore, OwnershipProofData> getComponentType() { return type; }
  public boolean owned() { return owned; }
  public String scope() { return scope; }
  @Override public OwnershipProofData clone() { return new OwnershipProofData(owned,scope,faction); }
}
