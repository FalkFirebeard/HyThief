package com.local.pickpocket;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.packets.interface_.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;
import static com.local.pickpocket.NpcOverrides.*;

public final class NpcEditorPage extends InteractiveCustomUIPage<NpcEditorPage.Data> {
  public static final class Data {
    public static final BuilderCodec<Data> CODEC=BuilderCodec.builder(Data.class,Data::new)
        .append(new KeyedCodec<>("Action",Codec.STRING),(d,v)->d.action=v,d->d.action).add()
        .append(new KeyedCodec<>("@Scope",Codec.STRING),(d,v)->d.scope=v,d->d.scope).add()
        .append(new KeyedCodec<>("@Pickpocket",Codec.STRING),(d,v)->d.pickpocket=v,d->d.pickpocket).add()
        .append(new KeyedCodec<>("@Witness",Codec.STRING),(d,v)->d.witness=v,d->d.witness).add()
        .append(new KeyedCodec<>("@Response",Codec.STRING),(d,v)->d.response=v,d->d.response).add().build();
    String action,scope,pickpocket,witness,response;
  }
  private final PickpocketPlugin plugin;
  private final PlayerRef creator;
  private final NpcEditor.Selection selected;
  private NpcEditor.Scope scope=NpcEditor.Scope.INDIVIDUAL;
  private Mode pickpocket,witness,response;
  private boolean finished;
  public NpcEditorPage(PickpocketPlugin plugin,PlayerRef player,NpcEditor.Selection selected) {
    super(player,CustomPageLifetime.CanDismiss,Data.CODEC);this.plugin=plugin;creator=player;this.selected=selected;load();
  }
  private void load() { var value=selected.value(scope);pickpocket=value.get(Capability.PICKPOCKET);witness=value.get(Capability.WITNESS);response=value.get(Capability.RESPONSE); }
  private static DropdownEntryInfo option(String label,String id) { return new DropdownEntryInfo(LocalizableString.fromString(label),id); }
  private static String label(Enum<?> value) { String s=value.name().toLowerCase(Locale.ROOT);return Character.toUpperCase(s.charAt(0))+s.substring(1); }
  private void controls(UICommandBuilder c) {
    c.set("#Scope.Value",scope.name());c.set("#Pickpocket.Value",pickpocket.name());c.set("#Witness.Value",witness.name());c.set("#Response.Value",response.name());
    c.set("#Status.Text",selected.scopeDescription(scope));
  }
  @Override public void build(Ref<EntityStore> actor,UICommandBuilder c,UIEventBuilder e,Store<EntityStore> store) {
    c.append("Pages/HyThiefNpcEditor.ui");c.set("#Selected.Text",selected.description());
    var scopes=new ArrayList<DropdownEntryInfo>();scopes.add(option("Individual NPC","INDIVIDUAL"));scopes.add(option("Role — this world","ROLE"));
    if(!selected.faction().equals("UNRESOLVED"))scopes.add(option("Faction — this world","FACTION"));c.set("#Scope.Entries",scopes);
    var options=Arrays.stream(Mode.values()).map(v->option(label(v),v.name())).toList();
    for(String id:List.of("Pickpocket","Witness","Response"))c.set("#"+id+".Entries",options);
    c.set("#Capabilities.Text","Enabled preserves supported paths only. No new loot, native AI or faction is created. Native hostility is separate.");
    controls(c);
    for(String id:List.of("Scope","Pickpocket","Witness","Response"))e.addEventBinding(CustomUIEventBindingType.ValueChanged,"#"+id,EventData.of("Action","edit").append("@"+id,"#"+id+".Value"),false);
    e.addEventBinding(CustomUIEventBindingType.Activating,"#Apply",EventData.of("Action","apply").append("@Scope","#Scope.Value").append("@Pickpocket","#Pickpocket.Value").append("@Witness","#Witness.Value").append("@Response","#Response.Value"));
    e.addEventBinding(CustomUIEventBindingType.Activating,"#Cancel",EventData.of("Action","cancel"));
  }
  @Override public void handleDataEvent(Ref<EntityStore> actor,Store<EntityStore> store,Data data) {
    if(finished)return;
    if("cancel".equals(data.action)) { finished=true;close();return; }
    if(!"apply".equals(data.action) && !"edit".equals(data.action))return;
    var update=new UICommandBuilder();
    try {
      var nextScope=data.scope==null?scope:NpcEditor.Scope.valueOf(data.scope);
      if(nextScope==NpcEditor.Scope.FACTION && selected.faction().equals("UNRESOLVED"))throw new IllegalArgumentException("No recognized faction.");
      if(nextScope!=scope) {
        if("apply".equals(data.action))throw new IllegalArgumentException("Select the scope before applying changes.");
        scope=nextScope;load();controls(update);sendUpdate(update,false);return;
      }
      if(data.pickpocket!=null)pickpocket=Mode.valueOf(data.pickpocket);
      if(data.witness!=null)witness=Mode.valueOf(data.witness);
      if(data.response!=null)response=Mode.valueOf(data.response);
      if("apply".equals(data.action)) {
        boolean changed=NpcEditor.apply(plugin,selected,scope,actor,store,creator,new NpcOverrides(pickpocket,witness,response));
        finished=true;creator.sendMessage(Message.raw(changed?"NPC participation updated.":"NPC participation unchanged."));close();return;
      }
      controls(update);
    } catch(IllegalArgumentException ex) { update.set("#Status.Text",ex.getMessage()); }
    sendUpdate(update,false);
  }
  @Override public void onDismiss(Ref<EntityStore> actor,Store<EntityStore> store) { finished=true; }
}
