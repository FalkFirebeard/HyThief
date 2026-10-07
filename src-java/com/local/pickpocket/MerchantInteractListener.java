package com.local.pickpocket;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class MerchantInteractListener implements PlayerPacketFilter {
  private static final double GLOBAL_DETECTION_CLOSE_RANGE = 24.0;

  private final PickpocketPlugin plugin;
  private final Map<UUID, Boolean> crouchingByPlayer = new ConcurrentHashMap<>();
  private final Map<UUID, Boolean> pageForcedCrouchByPlayer = new ConcurrentHashMap<>();
  private final Map<UUID, ActivePickpocketSession> activeSessionByPlayer = new ConcurrentHashMap<>();
  private final ScheduledExecutorService pageWatchdog =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> {
            Thread thread = new Thread(runnable, "hythief-page-watchdog");
            thread.setDaemon(true);
            return thread;
          });

  public MerchantInteractListener(PickpocketPlugin plugin) {
    this.plugin = plugin;
    pageWatchdog.scheduleAtFixedRate(this::pollActivePages, 150L, 150L, TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean test(PlayerRef playerRef, Packet packet) {
    if (packet instanceof ClientMovement movementPacket) {
      UUID playerUuid = playerRef.getUuid();
      if (movementPacket.movementStates != null) {
        if (pageForcedCrouchByPlayer.getOrDefault(playerUuid, false)) {
          movementPacket.movementStates.crouching = true;
          crouchingByPlayer.put(playerUuid, true);
        } else {
          crouchingByPlayer.put(playerUuid, movementPacket.movementStates.crouching);
        }
      }
      return false;
    }

    if (!(packet instanceof SyncInteractionChains chains) || chains.updates == null) {
      return false;
    }

    if (!crouchingByPlayer.getOrDefault(playerRef.getUuid(), false)) {
      return false;
    }

    Ref<EntityStore> playerEntityRef = playerRef.getReference();
    if (playerEntityRef == null || !playerEntityRef.isValid()) {
      return false;
    }

    Store<EntityStore> playerStore = playerEntityRef.getStore();
    if (playerStore == null) {
      return false;
    }

    World world = ((EntityStore) playerStore.getExternalData()).getWorld();
    if (world == null) {
      return false;
    }

    for (SyncInteractionChain chain : chains.updates) {
      if (!isUseStart(chain)) {
        continue;
      }

      int targetNetworkId = chain.data.entityId;
      world.execute(() -> openPickpocketStorage(playerRef, targetNetworkId));
      return true;
    }

    return false;
  }

  private boolean isUseStart(SyncInteractionChain chain) {
    if (chain == null || !chain.initial || chain.interactionType != InteractionType.Use) {
      return false;
    }

    return chain.data != null && chain.data.entityId >= 0;
  }

  private String getTargetRoleName(Ref<EntityStore> targetRef) {
    if (targetRef == null || !targetRef.isValid()) {
      return null;
    }

    Store<EntityStore> targetStore = targetRef.getStore();
    if (targetStore == null) {
      return null;
    }

    NPCEntity npc = targetStore.getComponent(targetRef, NPCEntity.getComponentType());
    return npc == null ? null : npc.getRoleName();
  }

  private void openPickpocketStorage(PlayerRef playerRef, int targetNetworkId) {
    Ref<EntityStore> playerEntityRef = playerRef.getReference();
    if (playerEntityRef == null || !playerEntityRef.isValid()) {
      return;
    }

    Store<EntityStore> playerStore = playerEntityRef.getStore();
    if (playerStore == null) {
      return;
    }

    World world = ((EntityStore) playerStore.getExternalData()).getWorld();
    if (world == null) {
      return;
    }

    Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(targetNetworkId);
    String roleName = getTargetRoleName(targetRef);
    if (!plugin.isPickpocketableRole(roleName)) {
      return;
    }

    Store<EntityStore> targetStore = targetRef.getStore();
    if (targetStore == null) {
      return;
    }

    UUIDComponent uuidComponent = targetStore.getComponent(targetRef, UUIDComponent.getComponentType());
    if (uuidComponent == null) {
      return;
    }

    UUID npcUuid = uuidComponent.getUuid();
    MerchantStoragePage.StealthSnapshot snapshot =
        plugin.createStealthSnapshot(playerEntityRef, playerStore, targetRef);
    if (!plugin.canOpenPickpocket(
        playerRef, npcUuid, roleName, snapshot, plugin.isPickpocketTargetUnsafe(targetRef, targetStore))) {
      return;
    }

    new MerchantStoragePage(plugin, npcUuid, roleName, playerRef, snapshot, targetRef, targetStore)
        .open(playerRef, playerEntityRef, playerStore);
  }

  public void setPickpocketPageOpen(UUID playerUuid, boolean open) {
    if (open) {
      pageForcedCrouchByPlayer.put(playerUuid, true);
      crouchingByPlayer.put(playerUuid, true);
      return;
    }

    pageForcedCrouchByPlayer.remove(playerUuid);
    crouchingByPlayer.put(playerUuid, false);
  }

  public void registerPickpocketPage(
      UUID playerUuid,
      PlayerRef playerRef,
      MerchantStoragePage storagePage,
      HyThiefPickpocketPage page) {
    activeSessionByPlayer.put(playerUuid, new ActivePickpocketSession(playerRef, storagePage, page));
  }

  public void unregisterPickpocketPage(UUID playerUuid) {
    activeSessionByPlayer.remove(playerUuid);
  }

  public void shutdown() {
    pageWatchdog.shutdownNow();
  }

  private void pollActivePages() {
    for (ActivePickpocketSession session : activeSessionByPlayer.values()) {
      PlayerRef playerRef = session.playerRef;
      if (playerRef == null) {
        continue;
      }

      Ref<EntityStore> playerEntityRef = playerRef.getReference();
      if (playerEntityRef == null || !playerEntityRef.isValid()) {
        continue;
      }

      Store<EntityStore> playerStore = playerEntityRef.getStore();
      if (playerStore == null) {
        continue;
      }

      World world = ((EntityStore) playerStore.getExternalData()).getWorld();
      if (world == null) {
        continue;
      }

      world.execute(
          () -> {
            boolean targetUnsafe = session.storagePage.forceCloseIfUnsafe(true);
            if (targetUnsafe || plugin.isPlayerDetectedByAnyNearbyNpc(
                    playerEntityRef, playerStore, GLOBAL_DETECTION_CLOSE_RANGE)) {
              closeActivePage(session);
            }
          });
    }
  }

  private void closeActivePage(ActivePickpocketSession session) {
    if (session == null) {
      return;
    }

    PlayerRef playerRef = session.playerRef;
    if (playerRef == null) {
      session.page.forceCloseExternally();
      return;
    }

    Ref<EntityStore> playerEntityRef = playerRef.getReference();
    Store<EntityStore> playerStore = playerEntityRef == null ? null : playerEntityRef.getStore();
    World world =
        playerStore == null
            ? null
            : ((EntityStore) playerStore.getExternalData()).getWorld();
    if (world != null) {
      world.execute(session.page::forceCloseExternally);
    } else {
      session.page.forceCloseExternally();
    }
  }

  private record ActivePickpocketSession(
      PlayerRef playerRef, MerchantStoragePage storagePage, HyThiefPickpocketPage page) {}
}
