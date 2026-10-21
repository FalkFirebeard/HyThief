package com.local.pickpocket;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;

/** Participation only; native role, inventory, ownership and AI state remain separate. */
public final class NpcOverrides implements Component<EntityStore> {
  public enum Mode { DEFAULT, ENABLED, DISABLED }
  public enum Capability { PICKPOCKET, WITNESS, RESPONSE }
  private static final EnumCodec<Mode> MODE = new EnumCodec<>(Mode.class);
  public static final BuilderCodec<NpcOverrides> CODEC = BuilderCodec.builder(NpcOverrides.class, NpcOverrides::new)
      .append(new KeyedCodec<>("Pickpocket", MODE), (d,v)->d.pickpocket=v, d->d.pickpocket).add()
      .append(new KeyedCodec<>("Witness", MODE), (d,v)->d.witness=v, d->d.witness).add()
      .append(new KeyedCodec<>("Response", MODE), (d,v)->d.response=v, d->d.response).add().build();
  private static ComponentType<EntityStore,NpcOverrides> type;
  private Mode pickpocket=Mode.DEFAULT, witness=Mode.DEFAULT, response=Mode.DEFAULT;
  public NpcOverrides() {}
  public NpcOverrides(Mode p, Mode w, Mode r) {
    pickpocket=Objects.requireNonNull(p); witness=Objects.requireNonNull(w); response=Objects.requireNonNull(r);
  }
  static void register(PickpocketPlugin plugin) {
    type=plugin.getEntityStoreRegistry().registerComponent(NpcOverrides.class,"HyThiefNpcParticipation",CODEC);
  }
  public static ComponentType<EntityStore,NpcOverrides> getComponentType() { return type; }
  public Mode get(Capability c) { return switch(c) { case PICKPOCKET->pickpocket; case WITNESS->witness; case RESPONSE->response; }; }
  public boolean defaults() { return pickpocket==Mode.DEFAULT && witness==Mode.DEFAULT && response==Mode.DEFAULT; }
  @Override public NpcOverrides clone() { return new NpcOverrides(pickpocket,witness,response); }
  @Override public boolean equals(Object o) { return o instanceof NpcOverrides d && pickpocket==d.pickpocket && witness==d.witness && response==d.response; }
  @Override public int hashCode() { return Objects.hash(pickpocket,witness,response); }
}
