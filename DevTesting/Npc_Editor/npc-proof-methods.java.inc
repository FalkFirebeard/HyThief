
  com.local.pickpocket.NpcOverrides cfg(String p,String w,String r){return new com.local.pickpocket.NpcOverrides(com.local.pickpocket.NpcOverrides.Mode.valueOf(p),com.local.pickpocket.NpcOverrides.Mode.valueOf(w),com.local.pickpocket.NpcOverrides.Mode.valueOf(r));}
  void npcApply(Ref<EntityStore> npc,String scope,Ref<EntityStore> actor,PlayerRef pref,com.local.pickpocket.NpcOverrides next){require(com.local.pickpocket.NpcEditor.apply((com.local.pickpocket.PickpocketPlugin)hy,com.local.pickpocket.NpcEditor.select(npc,s),com.local.pickpocket.NpcEditor.Scope.valueOf(scope),actor,s,pref,next),"expected configuration change");}
  boolean allowed(Ref<EntityStore> npc,String cap){return com.local.pickpocket.NpcParticipation.allowed(com.local.pickpocket.NpcOverrides.Capability.valueOf(cap),npc,s,true);}
  void near(Ref<EntityStore> actor,Ref<EntityStore> npc){var at=s.getComponent(npc,TransformComponent.getComponentType()).getPosition();s.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(at).add(.2,0,3));aimAt(actor,new Vector3d(at).add(0,1,0));}
  void npcTest()throws Exception {
    findNPCs();require(merchant!=null&&guard!=null,"fixture NPCs absent");
    var ct=com.local.pickpocket.NpcOverrides.getComponentType();var rt=com.local.pickpocket.NpcWorldOverrides.getResourceType();
    if(mode.equals("npc-metadata")) {npcMetadata();return;}
    if(mode.equals("npc-prefab")) {npcPrefab();return;}
    if(mode.equals("npc-restart")) {
      var ids=org.bson.BsonDocument.parse(Files.readString(Path.of("npc-ids.json")));
      var a=s.getExternalData().getRefFromUUID(UUID.fromString(ids.getString("a").getValue()));var b=s.getExternalData().getRefFromUUID(UUID.fromString(ids.getString("b").getValue()));
      require(a!=null&&b!=null,"NPCs not restored from native save");
      require(s.getComponent(a,ct).equals(cfg("DISABLED","ENABLED","DEFAULT")),"individual component lost restart");
      require(!allowed(a,"PICKPOCKET")&&allowed(a,"WITNESS"),"individual precedence restart");
      require(allowed(b,"PICKPOCKET")&&!allowed(b,"WITNESS"),"world role/faction resource lost restart");
      require(s.getResource(rt).role("Kweebec_Merchant").equals(cfg("ENABLED","DEFAULT","DEFAULT")),"role map lost");
      require(s.getResource(rt).faction("Kweebec").equals(cfg("DISABLED","DISABLED","DISABLED")),"faction map lost");
      log("NPC_RESTART_PASS Individual=true Role=true Faction=true ExactSavedEntities=true NativeDisk=true");finish();return;
    }
    var second=spawn("Kweebec_Merchant",6,-8,"DISPOSABLE NPC B");
    var actor=actor();var sink=new Sink();var uuid=UUID.randomUUID();
    var pref=new PlayerRef(EntityStore.REGISTRY.newHolder(),uuid,"NpcEditorProof","en-US",sink,null){public Ref<EntityStore> getReference(){return actor;}};
    var player=new com.hypixel.hytale.server.core.entity.entities.Player();player.init(uuid,pref);
    s.addComponent(actor,com.hypixel.hytale.server.core.entity.entities.Player.getComponentType(),player);s.addComponent(actor,PlayerRef.getComponentType(),pref);
    s.addComponent(actor,com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId.getComponentType(),new com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId(s.getExternalData().takeNextNetworkId()));
    var bar=new InventoryComponent.Hotbar(new SimpleItemContainer((short)9),(byte)0);bar.getInventory().setItemStackForSlot((short)0,new ItemStack("HyThief_NPC_Editor_Tool",1));s.addComponent(actor,InventoryComponent.Hotbar.getComponentType(),bar);
    var manager=new com.hypixel.hytale.server.core.entity.InteractionManager(null,null);
    var root=com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction.getAssetMap().getAsset("HyThief_NPC_Editor");require(root!=null&&root.toPacket().requireNewClick,"root/click gate absent");
    var plugin=(com.local.pickpocket.PickpocketPlugin)hy;var perm=com.hypixel.hytale.server.core.permissions.PermissionsModule.get();
    try {
      near(actor,merchant);log("NPC_TARGET "+com.local.pickpocket.NpcEditor.target(actor,s)+" expected "+merchant);
      require(merchant.equals(com.local.pickpocket.NpcEditor.target(actor,s)),"native ray target mismatch");
      var headBefore=s.getComponent(merchant,HeadRotation.getComponentType()).getRotation().clone();var stateBefore=s.getComponent(merchant,StateSupport.getComponentType()).getStateName();
      activate(manager,actor,root);require(player.getPageManager().getCustomPage()==null,"unauthorized UI opened");
      perm.addUserPermission(uuid,Set.of("hythief.npc.edit"));
      activate(manager,actor,root);require(player.getPageManager().getCustomPage() instanceof com.local.pickpocket.NpcEditorPage,"native NPC tool failed");
      var page=player.getPageManager().getCustomPage();int opens=countOpens(sink);activate(manager,actor,root);require(player.getPageManager().getCustomPage()==page&&countOpens(sink)==opens,"duplicate UI replaced");
      require(s.getComponent(merchant,ct)==null&&stateBefore.equals(s.getComponent(merchant,StateSupport.getComponentType()).getStateName())&&headBefore.equals(s.getComponent(merchant,HeadRotation.getComponentType()).getRotation()),"opening changed native NPC");
      page.handleDataEvent(actor,s,"{\"Action\":\"edit\",\"@Pickpocket\":\"DISABLED\"}");page.handleDataEvent(actor,s,"{\"Action\":\"cancel\"}");require(s.getComponent(merchant,ct)==null,"Cancel wrote");
      activate(manager,actor,root);page=player.getPageManager().getCustomPage();perm.removeUserPermission(uuid,Set.of("hythief.npc.edit"));
      page.handleDataEvent(actor,s,"{\"Action\":\"apply\",\"@Pickpocket\":\"DISABLED\",\"@Witness\":\"DEFAULT\",\"@Response\":\"DEFAULT\"}");require(s.getComponent(merchant,ct)==null,"revoked Apply succeeded");page.handleDataEvent(actor,s,"{\"Action\":\"cancel\"}");perm.addUserPermission(uuid,Set.of("hythief.npc.edit"));
      activate(manager,actor,root);page=player.getPageManager().getCustomPage();page.handleDataEvent(actor,s,"{\"Action\":\"apply\",\"@Pickpocket\":\"DISABLED\",\"@Witness\":\"DEFAULT\",\"@Response\":\"DEFAULT\"}");
      require(!plugin.isPickpocketable(merchant,s)&&plugin.isPickpocketable(second,s),"pickpocket individual independence");require(allowed(merchant,"WITNESS")&&allowed(merchant,"RESPONSE"),"settings not independent");
      int packets=sink.sent.size();page.handleDataEvent(actor,s,"{\"Action\":\"apply\",\"@Pickpocket\":\"DEFAULT\"}");require(sink.sent.size()==packets&&!plugin.isPickpocketable(merchant,s),"duplicate Apply processed");
      var stale=com.local.pickpocket.NpcEditor.select(merchant,s);npcApply(merchant,"INDIVIDUAL",actor,pref,cfg("ENABLED","DISABLED","DEFAULT"));
      try {com.local.pickpocket.NpcEditor.apply(plugin,stale,com.local.pickpocket.NpcEditor.Scope.INDIVIDUAL,actor,s,pref,cfg("DEFAULT","DEFAULT","DEFAULT"));throw new IllegalStateException("stale apply accepted");}catch(IllegalArgumentException expected){}
      var seen=(List<?>)call("ContainerVisualWitness","capture",actor,s,new com.local.pickpocket.OwnershipProofData(true,"NEARBY",""),Set.of("Kweebec_Merchant"),(Consumer<String>)this::log);
      for(Object evidence:seen)require(!merchant.equals(read(evidence,"witness")),"disabled witness captured");
      npcApply(merchant,"INDIVIDUAL",actor,pref,cfg("DEFAULT","DEFAULT","DEFAULT"));require(s.getComponent(merchant,ct)==null&&plugin.isPickpocketable(merchant,s),"Default did not restore");
      // Actual native interaction eligibility follows the edited individual setting.
      s.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(0,64,-11));s.getComponent(actor,MovementStatesComponent.getComponentType()).getMovementStates().crouching=true;
      var eligibility=call("HyThiefNativeProbe","evaluate",plugin,actor,merchant,s);require(Boolean.TRUE.equals(read(eligibility,"eligible")),"default rear pickpocket gate failed");
      near(actor,merchant);npcApply(merchant,"INDIVIDUAL",actor,pref,cfg("DISABLED","DEFAULT","DEFAULT"));s.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(0,64,-11));
      eligibility=call("HyThiefNativeProbe","evaluate",plugin,actor,merchant,s);require(!Boolean.TRUE.equals(read(eligibility,"eligible")),"disabled prompt eligibility remained");s.getComponent(actor,MovementStatesComponent.getComponentType()).getMovementStates().crouching=false;
      near(actor,merchant);npcApply(merchant,"INDIVIDUAL",actor,pref,cfg("DEFAULT","DEFAULT","DEFAULT"));
      // Exact-scope precedence, independently per field; native supported paths remain the ceiling.
      npcApply(merchant,"FACTION",actor,pref,cfg("DISABLED","DISABLED","DISABLED"));require(!allowed(merchant,"PICKPOCKET")&&!allowed(second,"WITNESS"),"faction scope failed");
      npcApply(merchant,"ROLE",actor,pref,cfg("ENABLED","DEFAULT","DEFAULT"));require(allowed(second,"PICKPOCKET")&&!allowed(second,"WITNESS"),"role per-field precedence failed");
      npcApply(merchant,"INDIVIDUAL",actor,pref,cfg("DISABLED","ENABLED","DEFAULT"));require(!allowed(merchant,"PICKPOCKET")&&allowed(merchant,"WITNESS")&&!allowed(merchant,"RESPONSE"),"individual per-field precedence failed");
      near(actor,guard);npcApply(guard,"ROLE",actor,pref,cfg("DEFAULT","DEFAULT","ENABLED"));require(allowed(guard,"RESPONSE"),"response Enabled failed");npcApply(guard,"INDIVIDUAL",actor,pref,cfg("DEFAULT","DEFAULT","DISABLED"));require(!allowed(guard,"RESPONSE"),"response Disabled failed");
      var delivered=call("HyThiefGuardAlert","deliver",hy,actor,merchant,s,(Consumer<String>)this::log);require(!Boolean.TRUE.equals(read(delivered,"sent")),"response-disabled guard received crime");
      var fox=spawn("Fox",12,-8,"DISPOSABLE UNSUPPORTED CONTROL");near(actor,fox);require(!com.local.pickpocket.NpcEditor.supported(plugin,com.local.pickpocket.NpcOverrides.Capability.PICKPOCKET,fox,s),"wildlife gained support");
      try{com.local.pickpocket.NpcEditor.apply(plugin,com.local.pickpocket.NpcEditor.select(fox,s),com.local.pickpocket.NpcEditor.Scope.INDIVIDUAL,actor,s,pref,cfg("ENABLED","DEFAULT","DEFAULT"));throw new IllegalStateException("unsupported enabled accepted");}catch(IllegalArgumentException expected){}
      require(!com.local.pickpocket.NpcParticipation.allowed(com.local.pickpocket.NpcOverrides.Capability.PICKPOCKET,fox,s,false),"unsupported default leaked");
      near(actor,merchant);var before=com.local.pickpocket.NpcEditor.select(merchant,s);var data=EntityStore.REGISTRY.serialize(s.copySerializableEntity(merchant));
      require(data.toJson().contains("HyThiefNpcParticipation"),"serialized entity omitted component");var restored=EntityStore.REGISTRY.deserialize(data);require(restored.getComponent(ct).equals(cfg("DISABLED","ENABLED","DEFAULT")),"holder round trip failed");
      s.removeEntity(merchant,RemoveReason.UNLOAD);merchant=s.addEntity(restored,AddReason.LOAD);require(s.getComponent(merchant,ct).equals(cfg("DISABLED","ENABLED","DEFAULT")),"native unload/readd lost override");
      try{com.local.pickpocket.NpcEditor.apply(plugin,before,com.local.pickpocket.NpcEditor.Scope.INDIVIDUAL,actor,s,pref,cfg("DEFAULT","DEFAULT","DEFAULT"));throw new IllegalStateException("stale ref accepted");}catch(IllegalArgumentException expected){}
      var born=spawn("Kweebec_Merchant",10,-8,"DISPOSABLE FRESH SPAWN");require(s.getComponent(born,ct)==null&&allowed(born,"PICKPOCKET")&&!allowed(born,"WITNESS"),"fresh spawn inheritance wrong");
      Files.writeString(Path.of("npc-ids.json"),new org.bson.BsonDocument().append("a",new org.bson.BsonString(s.getComponent(merchant,com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType()).getUuid().toString())).append("b",new org.bson.BsonString(s.getComponent(second,com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType()).getUuid().toString())).toJson());
      // Invalid target and range: no page, no damage, no unnecessary messages.
      s.getComponent(actor,TransformComponent.getComponentType()).setPosition(new Vector3d(0,64,10));aimAt(actor,new Vector3d(0,65,-8));activate(manager,actor,root);require(player.getPageManager().getCustomPage()==null,"out of reach UI");aimAt(actor,new Vector3d(0,90,10));activate(manager,actor,root);require(player.getPageManager().getCustomPage()==null,"air UI");
      require(s.getComponent(guard,com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport.getComponentType()).getMarkedEntityRef("LockedTarget")==null,"editing set hostile target");
      log("NPC_EDITOR_PASS NativeChain=true ExactRay=true Permission=true RevokeApply=true ApplyCancel=true Duplicate=true IndividualIndependent=true ThreeModes=true Precedence=true WitnessFilter=true ResponseFilter=true StaleRef=true HolderRoundTrip=true UnloadReload=true FreshSpawn=true UnsupportedSafe=true NoNativeReaction=true");
    }finally{s.removeComponent(actor,PlayerRef.getComponentType());s.removeComponent(actor,com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());}
    finish();
  }

  void npcPrefab()throws Exception {
    var ct=com.local.pickpocket.NpcOverrides.getComponentType();var value=cfg("DISABLED","ENABLED","DEFAULT");
    s.putComponent(merchant,ct,value);var original=s.getComponent(merchant,com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType()).getUuid();
    var selection=new com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection();
    selection.setPosition(0,64,-8);selection.setAnchor(0,0,0);
    selection.setSelectionArea(new org.joml.Vector3i(0,64,-8),new org.joml.Vector3i(1,67,-7));
    selection.addEntityFromWorld(s.copySerializableEntity(merchant));
    var json=com.hypixel.hytale.server.core.prefab.config.SelectionPrefabSerializer.serialize(selection);
    require(json.toJson().contains("HyThiefNpcParticipation"),"prefab JSON stripped override");Files.writeString(Path.of("npc-prefab.json"),json.toJson());
    var loaded=com.hypixel.hytale.server.core.prefab.config.SelectionPrefabSerializer.deserialize(org.bson.BsonDocument.parse(Files.readString(Path.of("npc-prefab.json"))));
    int[] holders={0};loaded.forEachEntity(h->{require(value.equals(h.getComponent(ct)),"prefab JSON decode stripped override");holders[0]++;});require(holders[0]==1,"prefab entity count");
    var copies=new ArrayList<Ref<EntityStore>>();
    loaded.place(com.hypixel.hytale.server.core.console.ConsoleSender.INSTANCE,w,new org.joml.Vector3i(12,64,-8),null,copies::add);
    require(copies.size()==1,"native prefab paste did not produce reference");var jsonCopy=copies.getFirst();
    var path=Path.of("npc-prefab.prefab.lpf");
    var buffer=com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferSelectionConverter.toPrefabBuffer(path,loaded);
    com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil.writeToFileAsync(buffer,path).join();
    var binary=com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil.readFromFile(path);
    var binarySelection=com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferSelectionConverter.toBlockSelection(binary);
    holders[0]=0;binarySelection.forEachEntity(h->{require(value.equals(h.getComponent(ct)),"binary prefab stripped override");holders[0]++;});require(holders[0]==1,"binary prefab entity count");
    copies.clear();binarySelection.place(com.hypixel.hytale.server.core.console.ConsoleSender.INSTANCE,w,new org.joml.Vector3i(18,64,-8),null,copies::add);
    require(copies.size()==1,"binary native paste did not produce reference");var binaryCopy=copies.getFirst();
    w.scheduleAfter(()->w.execute(()->{try {
      for(var copy:List.of(jsonCopy,binaryCopy)) {
        require(copy.isValid()&&value.equals(s.getComponent(copy,ct)),"native deferred prefab paste stripped override");
        require(!original.equals(s.getComponent(copy,com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType()).getUuid()),"prefab reused original NPC UUID");
      }
      require(!s.getComponent(jsonCopy,com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType()).getUuid().equals(s.getComponent(binaryCopy,com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType()).getUuid()),"two copies share UUID");
      log("NPC_PREFAB_PASS JsonExportImport=true BinaryExportImport=true NativePaste=true IndependentUUID=true IndividualOnly=true WorldRulesNotExported=true");finish();
    }catch(Throwable error){fail(error);}}),1,TimeUnit.SECONDS);
  }

  void npcMetadata()throws Exception {
    var groups=com.hypixel.hytale.builtin.tagset.config.NPCGroup.getAssetMap();var ids=new TreeSet<String>(groups.getAssetMap().keySet());
    var bm=NPCPlugin.get().getBuilderManager();var roles=new TreeSet<String>();var spawnable=new TreeSet<String>();
    for(var info:bm.getAllBuilders().values())if(!info.isRemoved()&&info.getBuilder()!=null&&info.getBuilder().category()==com.hypixel.hytale.server.npc.role.Role.class){roles.add(info.getKeyName());if(info.getBuilder().isSpawnable())spawnable.add(info.getKeyName());}
    require(ids.contains("HyThiefResearch_Lizardfolk")&&roles.contains("HyThiefResearch_Lizardfolk"),"third-party registration missing from native registry");
    var test=spawn("HyThiefResearch_Lizardfolk",12,-8,"DISPOSABLE REGISTRY RESEARCH");var matched=new ArrayList<String>();
    for(var id:ids)if(com.local.pickpocket.ContainerOwnershipPolicy.member(test,id,s))matched.add(id);
    require(matched.contains("HyThiefResearch_Lizardfolk"),"native group membership absent");
    require(!((com.local.pickpocket.PickpocketPlugin)hy).isPickpocketable(test,s),"discovery incorrectly granted pickpocket loot");
    var document=new org.bson.BsonDocument().append("GroupIDs",new org.bson.BsonArray(ids.stream().map(org.bson.BsonString::new).map(v->(org.bson.BsonValue)v).toList())).append("RoleIDs",new org.bson.BsonArray(roles.stream().map(org.bson.BsonString::new).map(v->(org.bson.BsonValue)v).toList())).append("SpawnableRoleIDs",new org.bson.BsonArray(spawnable.stream().map(org.bson.BsonString::new).map(v->(org.bson.BsonValue)v).toList())).append("ResearchNpcGroups",new org.bson.BsonArray(matched.stream().map(org.bson.BsonString::new).map(v->(org.bson.BsonValue)v).toList()));
    Files.writeString(Path.of("native-metadata.json"),document.toJson());
    log("NPC_METADATA_PASS RegisteredGroups="+ids.size()+" RoleBuilders="+roles.size()+" Spawnable="+spawnable.size()+" ThirdPartyDiscovered=true NativeMembership=true Groups="+matched+" NoInventedPickpocket=true");finish();
  }
