package com.local.pickpocket;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.entity.Dirty;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.PositionCache;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import java.util.UUID;
import static com.local.pickpocket.NpcOverrides.*;

/** World-thread creator operations; opening and cancelling never write NPC data. */
public final class NpcEditor {
  public static final String PERMISSION="hythief.npc.edit";
  public static final float REACH=8;
  public enum Scope { INDIVIDUAL, ROLE, FACTION }
  private NpcEditor() {}
  public record Selection(Ref<EntityStore> target,UUID uuid,String role,String faction,
      NpcOverrides individual,NpcOverrides roleValue,NpcOverrides factionValue) {
    public NpcOverrides value(Scope scope) { return switch(scope) {case INDIVIDUAL->individual;case ROLE->roleValue;case FACTION->factionValue;}; }
    public String description() { return role+" / "+faction+"\n"+uuid; }
    public String scopeDescription(Scope scope) { return switch(scope) {
      case INDIVIDUAL->"Only this saved NPC. Default inherits role, then faction.";
      case ROLE->"All "+role+" NPCs in THIS world. Individual overrides take priority.";
      case FACTION->"Recognized "+faction+" NPCs in THIS world. Individual and role overrides take priority.";}; }
  }
  public static Ref<EntityStore> target(Ref<EntityStore> actor,Store<EntityStore> store) {
    if(actor==null || !actor.isValid() || store.getComponent(actor,TransformComponent.getComponentType())==null)return null;
    return TargetUtil.getTargetEntity(actor,REACH,false,store);
  }
  public static Selection select(Ref<EntityStore> ref,Store<EntityStore> store) {
    if(ref==null || !ref.isValid() || ref.getStore()!=store)throw new IllegalArgumentException("Look at a live NPC within 8 blocks.");
    var npc=store.getComponent(ref,NPCEntity.getComponentType());var id=store.getComponent(ref,UUIDComponent.getComponentType());
    if(npc==null || npc.getRole()==null || id==null || store.getComponent(ref,DeathComponent.getComponentType())!=null)
      throw new IllegalArgumentException("This target is not a live configurable NPC.");
    var broad=store.getResource(NpcWorldOverrides.getResourceType());var local=store.getComponent(ref,NpcOverrides.getComponentType());
    String faction=NpcParticipation.faction(ref,store);
    return new Selection(ref,id.getUuid(),npc.getRoleName(),faction,local==null?new NpcOverrides():local.clone(),
        broad.role(npc.getRoleName()).clone(),broad.faction(faction).clone());
  }
  private static void validateActor(Ref<EntityStore> actor,Store<EntityStore> store,PlayerRef player) {
    if(!player.hasPermission(PERMISSION))throw new IllegalArgumentException("You do not have permission to edit NPC participation.");
    if(actor==null || !actor.isValid() || actor.getStore()!=store || !actor.equals(player.getReference()))
      throw new IllegalArgumentException("The editor's player or world changed. Reopen it.");
  }
  private static void validateReach(Ref<EntityStore> actor,Ref<EntityStore> target,Store<EntityStore> store) {
    var a=store.getComponent(actor,TransformComponent.getComponentType());var b=store.getComponent(target,TransformComponent.getComponentType());
    var sight=new PositionCache();sight.setOpaqueBlockSet(-1);
    if(a==null || b==null || a.getPosition().distanceSquared(b.getPosition())>REACH*REACH || !sight.hasLineOfSight(actor,target,store))
      throw new IllegalArgumentException("The NPC is too far away or obstructed. Reopen nearby.");
  }
  public static void open(PickpocketPlugin plugin,Ref<EntityStore> actor,Store<EntityStore> store,PlayerRef player) {
    validateActor(actor,store,player);var entity=store.getComponent(actor,Player.getComponentType());if(entity==null)return;
    if(entity.getPageManager().getCustomPage() instanceof NpcEditorPage)return;
    var selection=select(target(actor,store),store);validateReach(actor,selection.target(),store);
    entity.getPageManager().openCustomPage(actor,store,new NpcEditorPage(plugin,player,selection));
  }
  public static boolean supported(PickpocketPlugin plugin,Capability c,Ref<EntityStore> ref,Store<EntityStore> store) {
    var npc=store.getComponent(ref,NPCEntity.getComponentType());if(npc==null || npc.getRole()==null)return false;
    return switch(c) {
      case PICKPOCKET->plugin.isPickpocketableRole(npc.getRoleName());
      case WITNESS->ContainerWitnessProfiles.eligible(ref,store) || plugin.isPickpocketableRole(npc.getRoleName());
      case RESPONSE->{
        boolean path=ContainerWitnessProfiles.eligible(ref,store) || plugin.isPickpocketableRole(npc.getRoleName());
        var state=store.getComponent(ref,com.hypixel.hytale.server.npc.role.support.StateSupport.getComponentType());
        boolean reaction=state!=null && java.util.List.of("Attack","Combat","Alerted","Flee","Searching","Alarm","Threaten")
            .stream().anyMatch(id->state.getStateHelper().getStateIndex(id)>=0);
        yield path && reaction;
      }
    };
  }
  public static boolean apply(PickpocketPlugin plugin,Selection selected,Scope scope,Ref<EntityStore> actor,
      Store<EntityStore> store,PlayerRef player,NpcOverrides next) {
    validateActor(actor,store,player);var live=select(selected.target(),store);validateReach(actor,live.target(),store);
    if(!selected.uuid().equals(live.uuid()) || !selected.role().equals(live.role()) || !selected.faction().equals(live.faction())
        || !selected.value(scope).equals(live.value(scope)))throw new IllegalArgumentException("NPC or configuration changed. Reopen the editor.");
    if(scope==Scope.FACTION && "UNRESOLVED".equals(live.faction()))throw new IllegalArgumentException("No supported faction was resolved.");
    for(var c:Capability.values())if(next.get(c)==Mode.ENABLED && !supported(plugin,c,live.target(),store))
      throw new IllegalArgumentException(c+" has no supported HyThief path for this role. Enabled cannot create native capabilities.");
    if(next.equals(live.value(scope)))return false;
    if(!store.getExternalData().getWorld().getWorldConfig().canSaveChunks())throw new IllegalArgumentException("World saving is disabled.");
    if(scope==Scope.INDIVIDUAL) {
      var dirty=store.getComponent(live.target(),Dirty.getComponentType());
      if(dirty==null)throw new IllegalArgumentException("This NPC does not expose native persistent entity saving.");
      if(next.defaults())store.tryRemoveComponent(live.target(),NpcOverrides.getComponentType());
      else store.putComponent(live.target(),NpcOverrides.getComponentType(),next.clone());
      dirty.forceMarkDirty();
      var sessionType=PrefabEditSession.getResourceType();var session=sessionType==null?null:store.getResource(sessionType);
      if(session!=null) {
        var position=store.getComponent(live.target(),TransformComponent.getComponentType()).getPosition();
        session.markPrefabsDirtyAtPosition(new org.joml.Vector3i((int)Math.floor(position.x),(int)Math.floor(position.y),(int)Math.floor(position.z)));
      }
    } else {
      var type=NpcWorldOverrides.getResourceType();var rules=store.getResource(type);
      store.replaceResource(type,rules.with(scope==Scope.ROLE,scope==Scope.ROLE?live.role():live.faction(),next));
    }
    return true;
  }
}
