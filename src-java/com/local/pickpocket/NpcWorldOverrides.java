package com.local.pickpocket;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;

/** World-local broad overrides; native role assets and other worlds remain unchanged. */
public final class NpcWorldOverrides implements Resource<EntityStore> {
  public static final BuilderCodec<NpcWorldOverrides> CODEC = BuilderCodec.builder(NpcWorldOverrides.class,NpcWorldOverrides::new)
      .append(new KeyedCodec<>("Roles",new MapCodec<>(NpcOverrides.CODEC,HashMap::new)),(d,v)->d.roles=v,d->d.roles).add()
      .append(new KeyedCodec<>("Factions",new MapCodec<>(NpcOverrides.CODEC,HashMap::new)),(d,v)->d.factions=v,d->d.factions).add().build();
  private static ResourceType<EntityStore,NpcWorldOverrides> type;
  private Map<String,NpcOverrides> roles=new HashMap<>(), factions=new HashMap<>();
  static void register(PickpocketPlugin plugin) {
    type=plugin.getEntityStoreRegistry().registerResource(NpcWorldOverrides.class,"HyThiefNpcRules",CODEC);
  }
  public static ResourceType<EntityStore,NpcWorldOverrides> getResourceType() { return type; }
  public NpcOverrides role(String id) { return roles.getOrDefault(id,new NpcOverrides()); }
  public NpcOverrides faction(String id) { return factions.getOrDefault(id,new NpcOverrides()); }
  public NpcWorldOverrides with(boolean role,String id,NpcOverrides data) {
    var copy=clone();var map=role?copy.roles:copy.factions;
    if(data.defaults())map.remove(id);else map.put(id,data.clone());return copy;
  }
  @Override public NpcWorldOverrides clone() {
    var copy=new NpcWorldOverrides();roles.forEach((k,v)->copy.roles.put(k,v.clone()));factions.forEach((k,v)->copy.factions.put(k,v.clone()));return copy;
  }
}
