package com.local.pickpocket;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;

public final class OwnershipEditorPage extends InteractiveCustomUIPage<OwnershipEditorPage.Data> {
  public static final class Data {
    public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
        .append(new KeyedCodec<>("Action", Codec.STRING), (d,v)->d.action=v, d->d.action).add()
        .append(new KeyedCodec<>("@Owned", Codec.BOOLEAN), (d,v)->d.owned=v, d->d.owned).add()
        .append(new KeyedCodec<>("@Scope", Codec.STRING), (d,v)->d.scope=v, d->d.scope).add()
        .append(new KeyedCodec<>("@Faction", Codec.STRING), (d,v)->d.faction=v, d->d.faction).add().build();
    String action; Boolean owned; String scope; String faction;
  }
  private final PlayerRef creator;
  private final OwnershipEditor.Selection target;
  private boolean owned;
  private String scope, faction;
  private boolean finished;
  public OwnershipEditorPage(PlayerRef player, OwnershipEditor.Selection target) {
    super(player, CustomPageLifetime.CanDismiss, Data.CODEC);
    this.creator = player; this.target = target;
    var value = target.original();
    owned = value != null && value.owned();
    scope = value != null && "FACTION".equals(value.scope()) ? "FACTION" : "NEARBY";
    faction = value == null ? "" : value.faction();
    if (faction == null) faction = "";
  }
  private static DropdownEntryInfo option(String label, String value) {
    return new DropdownEntryInfo(LocalizableString.fromString(label), value);
  }
  @Override public void build(Ref<EntityStore> actor, UICommandBuilder c, UIEventBuilder e, Store<EntityStore> store) {
    c.append("Pages/HyThiefOwnershipEditor.ui");
    c.set("#Selected.Text", target.description());
    c.set("#Scope.Entries", List.of(option("Nearby", "NEARBY"), option("Faction", "FACTION")));
    c.set("#Faction.Entries", OwnershipEditor.factions().stream().map(g->option(g,g)).toList());
    c.set("#Owned #CheckBox.Value", owned); c.set("#Scope.Value", scope);
    if (OwnershipEditor.factions().contains(faction)) c.set("#Faction.Value", faction);
    else if (owned && "FACTION".equals(scope)) c.set("#Status.Text", "Choose a supported faction. Existing value: " + faction);
    controls(c);
    e.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Owned #CheckBox", EventData.of("Action", "edit").append("@Owned", "#Owned #CheckBox.Value"), false);
    e.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Scope", EventData.of("Action", "edit").append("@Scope", "#Scope.Value"), false);
    e.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Faction", EventData.of("Action", "edit").append("@Faction", "#Faction.Value"), false);
    e.addEventBinding(CustomUIEventBindingType.Activating, "#Apply", EventData.of("Action", "apply").append("@Owned", "#Owned #CheckBox.Value").append("@Scope", "#Scope.Value").append("@Faction", "#Faction.Value"));
    e.addEventBinding(CustomUIEventBindingType.Activating, "#Cancel", EventData.of("Action", "cancel"));
  }
  private void controls(UICommandBuilder c) {
    c.set("#Scope.Disabled", !owned); c.set("#Faction.Disabled", !owned || !"FACTION".equals(scope));
  }
  @Override public void handleDataEvent(Ref<EntityStore> actor, Store<EntityStore> store, Data data) {
    if (finished) return;
    if ("cancel".equals(data.action)) { finished=true; close(); return; }
    if (!"edit".equals(data.action) && !"apply".equals(data.action)) return;
    if (data.owned != null) owned=data.owned;
    if (data.scope != null) scope=data.scope;
    if (data.faction != null) faction=data.faction;
    var update=new UICommandBuilder();
    if ("apply".equals(data.action)) {
      try {
        boolean changed=OwnershipEditor.apply(target, actor, store, creator, owned, scope, faction);
        finished=true;
        creator.sendMessage(Message.raw(changed ? "Container ownership updated." : "Container ownership unchanged."));
        close(); return;
      } catch (IllegalArgumentException ex) { update.set("#Status.Text", ex.getMessage()); }
    }
    controls(update); sendUpdate(update, false);
  }
  @Override public void onDismiss(Ref<EntityStore> actor, Store<EntityStore> store) { finished=true; }
}
