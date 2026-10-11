package com.local.pickpocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class PickpocketPlugin extends JavaPlugin {
  private static final int STORAGE_SIZE = 9;
  private static final String STORAGE_FILE_NAME = "merchant-storage.txt";
  private static final int MAX_LOOT_ROLL_DEPTH = 4;
  private static final int SESSION_CARRYOVER_SECONDS = 10;

  private final Map<UUID, List<String>> storageByNpc = new ConcurrentHashMap<>();
  private final Map<UUID, Long> npcCooldownUntil = new ConcurrentHashMap<>();
  private final Map<PickpocketSessionKey, SessionCarryoverState> carryoverBySession =
      new ConcurrentHashMap<>();
  private final Gson gson = new Gson();
  private MerchantInteractListener merchantInteractListener;
  private HyThiefContainerDiagnostics containerDiagnostics;
  private PickpocketRoleResolver roleResolver = PickpocketRoleResolver.fallback();
  private PickpocketRules pickpocketRules = PickpocketRules.fallback();

  public PickpocketPlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    roleResolver = loadResolverConfig();
    pickpocketRules = loadRulesConfig();
    loadStorageState();
    com.hypixel.hytale.server.npc.NPCPlugin.get().registerCoreComponentType(
        HyThiefNativeProbe.SENSOR, () -> new HyThiefNativeProbe.SensorBuilder(this));
    com.hypixel.hytale.server.npc.NPCPlugin.get().registerCoreComponentType(
        HyThiefStealthAcquisition.TYPE, HyThiefStealthAcquisition.FilterBuilder::new);
    getEntityStoreRegistry().registerSystem(new HyThiefNativeProbe(this));
    if (getManifest().getVersion().toString().contains("-container.")) {
      OwnershipProofData.register(this);
      containerDiagnostics = new HyThiefContainerDiagnostics(this);
    }
  }

  @Override
  protected void start() { if (containerDiagnostics != null) containerDiagnostics.start(); }

  @Override
  protected void shutdown() {
    if (containerDiagnostics != null) containerDiagnostics.close();
    if (merchantInteractListener != null) {
      merchantInteractListener.shutdown();
    }
    saveStorageState();
  }

  public int getStorageSize() {
    return STORAGE_SIZE;
  }

  public boolean isPickpocketableRole(String roleName) {
    return roleResolver.resolveRole(roleName).isPresent();
  }

  public void setPickpocketPageOpen(UUID playerUuid, boolean open) {
    if (merchantInteractListener == null) {
      return;
    }
    merchantInteractListener.setPickpocketPageOpen(playerUuid, open);
  }

  public void registerPickpocketPage(
      UUID playerUuid,
      PlayerRef playerRef,
      MerchantStoragePage storagePage,
      HyThiefPickpocketPage page) {
    if (merchantInteractListener == null) {
      return;
    }
    merchantInteractListener.registerPickpocketPage(playerUuid, playerRef, storagePage, page);
  }

  public void unregisterPickpocketPage(UUID playerUuid) {
    if (merchantInteractListener == null) {
      return;
    }
    merchantInteractListener.unregisterPickpocketPage(playerUuid);
  }

  public boolean isPickpocketTargetUnsafe(
      Ref<EntityStore> targetRef, Store<EntityStore> targetStore) {
    if (targetRef == null || !targetRef.isValid() || targetStore == null) {
      return true;
    }

    NPCEntity npc = targetStore.getComponent(targetRef, NPCEntity.getComponentType());
    if (npc == null) {
      return true;
    }

    Role role = npc.getRole();
    if (role == null) {
      return false;
    }

    StateSupport stateSupport = targetStore.getComponent(targetRef, StateSupport.getComponentType());
    if (stateSupport == null) {
      return false;
    }

    if (stateSupport.isInBusyState()) {
      return true;
    }

    String stateName = stateSupport.getStateName();
    if (stateName == null || stateName.isBlank()) {
      return false;
    }

    String normalized = stateName.toLowerCase(Locale.ROOT);
    return normalized.contains("alert")
        || normalized.contains("chase")
        || normalized.contains("search")
        || normalized.contains("warn")
        || normalized.contains("converge")
        || normalized.contains("flee")
        || normalized.contains("attack")
        || normalized.contains("combat")
        || normalized.contains("threat");
  }

  public boolean isPlayerDetectedByAnyNearbyNpc(
      Ref<EntityStore> playerEntityRef, Store<EntityStore> playerStore, double range) {
    if (playerEntityRef == null || !playerEntityRef.isValid() || playerStore == null) {
      return false;
    }

    TransformComponent playerTransform =
        playerStore.getComponent(playerEntityRef, TransformComponent.getComponentType());
    if (playerTransform == null) {
      return false;
    }

    EntityStore entityStore = (EntityStore) playerStore.getExternalData();
    if (entityStore == null) {
      return false;
    }

    Store<EntityStore> worldStore = entityStore.getStore();
    if (worldStore == null) {
      return false;
    }

    Vector3d playerPosition = new Vector3d(playerTransform.getPosition());
    boolean[] detected = new boolean[1];
    worldStore.forEachChunk(
        NPCEntity.getComponentType(),
        (chunk, commandBuffer) -> {
          if (detected[0]) {
            return;
          }

          for (int i = 0; i < chunk.size(); i++) {
            Ref<EntityStore> npcRef = chunk.getReferenceTo(i);
            if (npcRef == null || !npcRef.isValid()) {
              continue;
            }

            NPCEntity npc = worldStore.getComponent(npcRef, NPCEntity.getComponentType());
            if (npc == null || !isPickpocketableRole(npc.getRoleName())) {
              continue;
            }

            if (!isPickpocketTargetUnsafe(npcRef, worldStore)) {
              continue;
            }

            TransformComponent npcTransform =
                worldStore.getComponent(npcRef, TransformComponent.getComponentType());
            if (npcTransform == null) {
              continue;
            }

            Vector3d npcPosition = new Vector3d(npcTransform.getPosition());
            if (distanceBetween(playerPosition, npcPosition) <= range) {
              detected[0] = true;
              return;
            }
          }
        });
    return detected[0];
  }

  public boolean canOpenPickpocket(PlayerRef playerRef, UUID npcUuid) {
    long now = System.currentTimeMillis();
    long cooldownUntil = npcCooldownUntil.getOrDefault(npcUuid, 0L);
    if (playerRef != null) {
      pruneExpiredCarryover(playerRef.getUuid(), npcUuid, now);
    }
    if (cooldownUntil <= now) {
      return true;
    }

    long remainingSeconds = Math.max(1L, (cooldownUntil - now + 999L) / 1000L);
    playerRef.sendMessage(
        Message.raw("This target is too alert right now. Try again in " + remainingSeconds + "s."));
    return false;
  }

  public boolean canOpenPickpocket(
      PlayerRef playerRef,
      UUID npcUuid,
      String roleName,
      MerchantStoragePage.StealthSnapshot snapshot,
      boolean npcAware) {
    if (!canOpenPickpocket(playerRef, npcUuid)) {
      return false;
    }

    if (npcAware) {
    playerRef.sendMessage(Message.raw("This target is already alert to you."));
      return false;
    }

    PickpocketRoleResolver.RoleLootProfile profile = roleResolver.resolveRole(roleName).orElse(null);
    String classProfile = profile == null ? "" : profile.classProfile();
    PickpocketRules.RuleSet rules = pickpocketRules.resolve(classProfile);
    if (snapshot.distance() <= rules.frontBlockDistance()
        && snapshot.facingDot() >= rules.frontBlockFacingDot()) {
    playerRef.sendMessage(Message.raw("You're too close and in clear view."));
      return false;
    }

    if (snapshot.distance() <= rules.coneBlockDistance()
        && snapshot.facingDot() >= rules.coneBlockFacingDot()) {
    playerRef.sendMessage(Message.raw("You need a better angle before trying that."));
      return false;
    }

    return true;
  }

  // Test 2 uses the existing configured rear-angle boundary at every native interaction distance.
  // Geometry is still calculated by createStealthSnapshot; no backend opening side effects here.
  boolean isBehindForNativeProbe(String roleName, MerchantStoragePage.StealthSnapshot snapshot) {
    PickpocketRoleResolver.RoleLootProfile profile = roleResolver.resolveRole(roleName).orElse(null);
    PickpocketRules.RuleSet rules = pickpocketRules.resolve(profile == null ? "" : profile.classProfile());
    return Double.isFinite(snapshot.distance()) && Double.isFinite(snapshot.facingDot())
        && snapshot.facingDot() < Math.min(0.0, rules.frontBlockFacingDot());
  }

  public MerchantStoragePage.StealthSnapshot createStealthSnapshot(
      Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> playerEntityRef,
      Store<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> playerStore,
      Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> targetRef) {
    TransformComponent playerTransform =
        playerStore.getComponent(playerEntityRef, TransformComponent.getComponentType());
    if (playerTransform == null) {
      return MerchantStoragePage.StealthSnapshot.defaultSnapshot();
    }

    Store<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> targetStore =
        targetRef == null ? null : targetRef.getStore();
    if (targetStore == null) {
      return MerchantStoragePage.StealthSnapshot.defaultSnapshot();
    }

    TransformComponent targetTransform =
        targetStore.getComponent(targetRef, TransformComponent.getComponentType());
    if (targetTransform == null) {
      return MerchantStoragePage.StealthSnapshot.defaultSnapshot();
    }

    Vector3d playerPosition = new Vector3d(playerTransform.getPosition());
    Vector3d targetPosition = new Vector3d(targetTransform.getPosition());
    double distance = distanceBetween(playerPosition, targetPosition);

    double facingDot = 0.0;
    HeadRotation headRotation = targetStore.getComponent(targetRef, HeadRotation.getComponentType());
    if (headRotation != null) {
      Vector3d facingDirection = new Vector3d(headRotation.getDirection());
      facingDirection.y = 0;
      if (!Vector3dUtil.closeToZero(facingDirection, 0.0001)) {
        normalizeVector(facingDirection);
        Vector3d toPlayer = directionTo(targetPosition, playerPosition);
        toPlayer.y = 0;
        if (!Vector3dUtil.closeToZero(toPlayer, 0.0001)) {
          normalizeVector(toPlayer);
          facingDot = dotProduct(facingDirection, toPlayer);
        }
      }
    }

    return new MerchantStoragePage.StealthSnapshot(distance, facingDot, true);
  }

  // Preserve v1.2's arithmetic order; JOML's reductions use a different order/FMA.
  private static double distanceBetween(Vector3dc from, Vector3dc to) {
    double x = to.x() - from.x();
    double y = to.y() - from.y();
    double z = to.z() - from.z();
    return Math.sqrt(x * x + y * y + z * z);
  }

  private static Vector3d normalizeVector(Vector3d vector) {
    double scale = 1.0 / Math.sqrt(vector.x * vector.x + vector.y * vector.y + vector.z * vector.z);
    vector.x *= scale;
    vector.y *= scale;
    vector.z *= scale;
    return vector;
  }

  private static Vector3d directionTo(Vector3dc from, Vector3dc to) {
    // The old directionTo normalized in 3D before the caller flattened Y.
    return normalizeVector(new Vector3d(to).sub(from));
  }

  private static double dotProduct(Vector3dc left, Vector3dc right) {
    return left.x() * right.x() + left.y() * right.y() + left.z() * right.z();
  }

  public void savePickpocketInventory(UUID npcUuid, List<String> afterEntries) {
    saveInventoryFor(npcUuid, afterEntries);
  }

  public SessionCarryover getPickpocketCarryover(UUID playerUuid, UUID npcUuid) {
    long now = System.currentTimeMillis();
    SessionCarryoverState state = pruneExpiredCarryover(playerUuid, npcUuid, now);
    if (state == null) {
      return SessionCarryover.empty();
    }
    return new SessionCarryover(state.stealCount(), state.stolenQuantity());
  }

  public void savePickpocketCarryover(
      UUID playerUuid,
      UUID npcUuid,
      String roleName,
      int successfulStealCount,
      int successfulStolenQuantity) {
    PickpocketSessionKey key = new PickpocketSessionKey(playerUuid, npcUuid);
    if (successfulStealCount <= 0 && successfulStolenQuantity <= 0) {
      carryoverBySession.remove(key);
      return;
    }

    int carriedStealCount = Math.max(0, successfulStealCount / 2);
    int carriedStolenQuantity = Math.max(0, successfulStolenQuantity / 2);
    if (carriedStealCount <= 0 && carriedStolenQuantity <= 0) {
      carryoverBySession.remove(key);
      return;
    }

    long expiresAt =
        System.currentTimeMillis() + (SESSION_CARRYOVER_SECONDS * 1000L);
    carryoverBySession.put(
        key, new SessionCarryoverState(carriedStealCount, carriedStolenQuantity, expiresAt));
  }

  public boolean handleSingleSteal(
      PlayerRef playerRef,
      UUID npcUuid,
      String roleName,
      String beforeEntry,
      String afterEntry,
      MerchantStoragePage.StealthSnapshot snapshot,
      int priorSuccessfulStealCount,
      int priorSuccessfulStolenQuantity,
      Ref<EntityStore> targetRef,
      Store<EntityStore> targetStore) {
    PickpocketAttemptResult result =
        evaluateAttempt(
            roleName,
            List.of(beforeEntry),
            List.of(afterEntry),
            snapshot,
            priorSuccessfulStealCount,
            priorSuccessfulStolenQuantity);
    if (!result.stolenAnything()) {
      return false;
    }

    if (result.detected()) {
      HyThiefGuardAlert.signal(this, playerRef, roleName, targetRef, targetStore);
      boolean usingStateReaction = triggerCaughtReaction(playerRef, roleName, targetRef, targetStore);
      if (usingStateReaction) {
        npcCooldownUntil.remove(npcUuid);
      } else {
        long cooldownUntil =
            System.currentTimeMillis() + (long) (result.rules().cooldownSeconds() * 1000L);
        npcCooldownUntil.put(npcUuid, cooldownUntil);
      }
      playerRef.sendMessage(
          Message.raw(
              "You were caught while taking that item. Detection chance: "
                  + Math.round(result.detectionChance() * 100)
                  + "%."));
      return true;
    }

    playerRef.sendMessage(Message.raw("You lift the item without being noticed."));
    return false;
  }

  private boolean beginSearchCooldown(
      String roleName, Ref<EntityStore> targetRef, Store<EntityStore> targetStore) {
    if (roleName == null || !roleName.contains("Merchant")) {
      return false;
    }
    if (targetRef == null || !targetRef.isValid() || targetStore == null) {
      return false;
    }

    NPCEntity npc = targetStore.getComponent(targetRef, NPCEntity.getComponentType());
    if (npc == null) {
      return false;
    }

    Role role = npc.getRole();
    if (role == null) {
      return false;
    }

    StateSupport stateSupport = targetStore.getComponent(targetRef, StateSupport.getComponentType());
    if (stateSupport == null) {
      return false;
    }

    try {
      stateSupport.setState(targetRef, "Searching", null, targetStore);
      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  private boolean triggerCaughtReaction(
      PlayerRef playerRef,
      String roleName,
      Ref<EntityStore> targetRef,
      Store<EntityStore> targetStore) {
    if (roleName == null || roleName.isBlank() || targetRef == null || !targetRef.isValid() || targetStore == null) {
      return false;
    }

    NPCEntity npc = targetStore.getComponent(targetRef, NPCEntity.getComponentType());
    if (npc == null) {
      return false;
    }

    Role role = npc.getRole();
    if (role == null) {
      return false;
    }

    StateSupport stateSupport = targetStore.getComponent(targetRef, StateSupport.getComponentType());
    if (stateSupport == null) {
      return false;
    }

    Ref<EntityStore> playerEntityRef = playerRef.getReference();
    if (playerEntityRef != null && playerEntityRef.isValid()) {
      try {
        role.setMarkedTarget(targetRef, targetStore, "LockedTarget", playerEntityRef);
      } catch (Exception ignored) {
      }
    }
    try {
      ExecutionSupport executionSupport = role.acquireExecutionSupport(targetRef, targetStore);
      try {
        // This UI callback is outside NPC traversal; do not invent an active tree step.
        executionSupport.notifySensorMatch();
      } finally {
        executionSupport.clearForReuse();
      }
    } catch (Exception ignored) {
    }

    PickpocketRoleResolver.RoleLootProfile profile = roleResolver.resolveRole(roleName).orElse(null);
    String classProfile = profile == null ? "" : profile.classProfile();
    boolean alertGuards = shouldAttemptGuardAlert(roleName, classProfile);
    for (String stateName : getReactionStates(roleName, classProfile, alertGuards)) {
      if (trySetState(stateSupport, targetRef, targetStore, stateName)) {
        return true;
      }
    }

    return false;
  }

  private boolean shouldAttemptGuardAlert(String roleName, String classProfile) {
    String normalizedClass = classProfile == null ? "" : classProfile.toLowerCase(Locale.ROOT);
    String normalizedRole = roleName == null ? "" : roleName.toLowerCase(Locale.ROOT);

    if (normalizedRole.contains("merchant")) {
      return false;
    }

    if (normalizedClass.contains("guardian")
        || normalizedClass.contains("melee")
        || normalizedClass.contains("ranged")
        || normalizedClass.contains("caster")
        || normalizedClass.contains("skirmisher")
        || normalizedClass.contains("elite")) {
      return false;
    }

    boolean hasVanillaGuardNetwork =
        normalizedRole.startsWith("kweebec_") || normalizedRole.startsWith("feran_");
    if (!hasVanillaGuardNetwork) {
      return false;
    }

    return ThreadLocalRandom.current().nextBoolean();
  }

  private List<String> getReactionStates(String roleName, String classProfile, boolean alertGuards) {
    String normalizedClass = classProfile == null ? "" : classProfile.toLowerCase(Locale.ROOT);
    String normalizedRole = roleName == null ? "" : roleName.toLowerCase(Locale.ROOT);

    if (normalizedRole.contains("merchant")) {
      return List.of("Searching", "Search", "Alerted");
    }
    if (normalizedClass.contains("civilian")
        || normalizedClass.contains("worker")
        || normalizedClass.contains("mystic")) {
      if (normalizedRole.startsWith("kweebec_")) {
        return List.of("Alerted", "Search");
      }
      return List.of("Alerted", "Search", "Panic", "Flee");
    }
    if (normalizedClass.contains("guardian")) {
      return List.of("Chase", "Combat", "Attack", "Alerted", "Search");
    }
    if (normalizedClass.contains("ranged")
        || normalizedClass.contains("caster")
        || normalizedClass.contains("elite")) {
      return List.of("Combat", "Chase", "Attack", "Alerted", "Search");
    }
    if (normalizedClass.contains("melee")
        || normalizedClass.contains("skirmisher")) {
      return List.of("Chase", "Combat", "Attack", "Alerted", "Search");
    }
    return List.of("Alerted", "Search");
  }

  private boolean trySetState(
      StateSupport stateSupport,
      Ref<EntityStore> targetRef,
      Store<EntityStore> targetStore,
      String stateName) {
    if (stateName == null || stateName.isBlank()) {
      return false;
    }

    StateMappingHelper stateHelper = stateSupport.getStateHelper();
    if (stateHelper != null) {
      try {
        stateHelper.getStateIndex(stateName);
      } catch (Exception ignored) {
        return false;
      }
    }

    try {
      stateSupport.setState(targetRef, stateName, null, targetStore);
      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  public List<String> getInventoryFor(UUID npcUuid, String roleName) {
    return new ArrayList<>(
        storageByNpc.computeIfAbsent(npcUuid, ignored -> createDefaultInventoryEntries(roleName)));
  }

  public void saveInventoryFor(UUID npcUuid, List<String> entries) {
    storageByNpc.put(npcUuid, normalizeEntries(entries));
    saveStorageState();
  }

  private List<String> createDefaultInventoryEntries(String roleName) {
    List<String> entries = new ArrayList<>(Collections.nCopies(STORAGE_SIZE, ""));
    PickpocketRoleResolver.RoleLootProfile profile = roleResolver.resolveRole(roleName).orElse(null);
    if (profile == null) {
      return entries;
    }

    PickpocketRoleResolver.LootTable table = roleResolver.getTable(profile.tableName()).orElse(null);
    if (table == null || table.entries().isEmpty()) {
      return entries;
    }

    if (table.nothingFoundChancePercent() > 0
        && ThreadLocalRandom.current().nextInt(100) < table.nothingFoundChancePercent()) {
      return entries;
    }

    int attempts = Math.min(STORAGE_SIZE, Math.max(1, table.maxStealsPerRefresh()));
    for (int slot = 0; slot < attempts; slot++) {
      String rolledEntry = rollLootEntry(table, 0);
      if (!rolledEntry.isBlank()) {
        entries.set(slot, rolledEntry);
      }
    }
    return entries;
  }

  private String rollLootEntry(PickpocketRoleResolver.LootTable table, int depth) {
    if (table == null || depth > MAX_LOOT_ROLL_DEPTH) {
      return "";
    }

    int totalWeight = 0;
    for (PickpocketRoleResolver.LootEntry entry : table.entries()) {
      totalWeight += Math.max(0, entry.weight());
    }
    if (totalWeight <= 0) {
      return "";
    }

    int roll = ThreadLocalRandom.current().nextInt(totalWeight);
    PickpocketRoleResolver.LootEntry selected = null;
    for (PickpocketRoleResolver.LootEntry entry : table.entries()) {
      roll -= Math.max(0, entry.weight());
      if (roll < 0) {
        selected = entry;
        break;
      }
    }

    if (selected == null) {
      return "";
    }

    if (!selected.tableRef().isBlank()) {
      PickpocketRoleResolver.LootTable nestedTable =
          roleResolver.getTable(selected.tableRef()).orElse(null);
      return rollLootEntry(nestedTable, depth + 1);
    }

    if (selected.itemId().isBlank() || "Empty".equals(selected.itemId())) {
      return "";
    }

    int min = Math.max(0, selected.min());
    int max = Math.max(min, selected.max());
    int quantity = min == max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
    if (quantity <= 0) {
      return "";
    }

    return selected.itemId() + ":" + quantity;
  }

  private List<String> normalizeEntries(List<String> entries) {
    List<String> normalized = new ArrayList<>(Collections.nCopies(STORAGE_SIZE, ""));
    int count = Math.min(entries.size(), STORAGE_SIZE);
    for (int i = 0; i < count; i++) {
      String entry = entries.get(i);
      normalized.set(i, entry == null ? "" : entry);
    }
    return normalized;
  }

  private void loadStorageState() {
    Path storageFile = getStorageFile();
    storageByNpc.clear();

    try {
      Files.createDirectories(getDataDirectory(), new FileAttribute<?>[0]);
      if (!Files.exists(storageFile, new LinkOption[0])) {
        return;
      }

      for (String line : Files.readAllLines(storageFile, StandardCharsets.UTF_8)) {
        if (line == null || line.isBlank()) {
          continue;
        }

        String[] parts = line.split("\t", 2);
        if (parts.length != 2) {
          continue;
        }

        UUID npcUuid = UUID.fromString(parts[0]);
        String[] rawEntries = parts[1].split("\\|", -1);
        List<String> values = new ArrayList<>(rawEntries.length);
        Collections.addAll(values, rawEntries);
        storageByNpc.put(npcUuid, normalizeEntries(values));
      }
    } catch (Exception exception) {
      storageByNpc.clear();
      System.err.println("[Pickpocket] Failed to load storage state; starting fresh.");
      exception.printStackTrace(System.err);
    }
  }

  private void saveStorageState() {
    Path storageFile = getStorageFile();

    try {
      Files.createDirectories(getDataDirectory(), new FileAttribute<?>[0]);
      List<String> lines =
          storageByNpc.entrySet().stream()
              .map(
                  entry ->
                      String.valueOf(entry.getKey())
                          + "\t"
                          + String.join("|", normalizeEntries(entry.getValue())))
              .sorted()
              .collect(Collectors.toList());
      Files.write(storageFile, lines, StandardCharsets.UTF_8, new OpenOption[0]);
    } catch (IOException exception) {
      System.err.println("[Pickpocket] Failed to save storage state.");
      exception.printStackTrace(System.err);
    }
  }

  private Path getStorageFile() {
    return getDataDirectory().resolve(STORAGE_FILE_NAME);
  }

  private PickpocketAttemptResult evaluateAttempt(
      String roleName,
      List<String> beforeEntries,
      List<String> afterEntries,
      MerchantStoragePage.StealthSnapshot snapshot,
      int priorSuccessfulStealCount,
      int priorSuccessfulStolenQuantity) {
    PickpocketRules.RuleSet rules = resolveRulesForRole(roleName);
    TheftSummary summary = summarizeTheft(beforeEntries, afterEntries);
    if (!summary.stolenAnything()) {
      return new PickpocketAttemptResult(false, false, 0.0, 0, rules);
    }

    double chance = rules.baseDetectionChance();
    if (snapshot.crouching()) {
      chance -= rules.crouchDetectionPenalty();
    }

    if (snapshot.facingDot() >= 0.35) {
      chance += rules.frontTargetDetectionBonus();
    } else if (snapshot.facingDot() <= -0.35) {
      chance -= rules.behindTargetDetectionPenalty();
    } else {
      chance -= rules.sideTargetDetectionPenalty();
    }

    if (snapshot.distance() <= 1.5) {
      chance += rules.closeRangeDetectionBonus();
    } else {
      chance -= Math.min(0.12, (snapshot.distance() - 1.5) * rules.distanceReductionPerBlock());
    }

    chance += summary.stolenSlotCount * rules.stolenSlotDetectionBonus();
    chance += summary.stolenQuantity * rules.stolenQuantityDetectionBonus();
    chance +=
        Math.max(0, priorSuccessfulStealCount) * rules.sessionStealDetectionBonus();
    chance +=
        Math.max(0, priorSuccessfulStolenQuantity) * rules.sessionQuantityDetectionBonus();
    chance += summary.weaponSlots * rules.weaponDetectionBonus();
    chance += summary.armorSlots * rules.armorDetectionBonus();
    chance += summary.toolSlots * rules.toolDetectionBonus();
    chance += summary.materialSlots * rules.materialDetectionBonus();
    chance += summary.recipeSlots * rules.recipeDetectionBonus();

    chance = Math.max(0.02, Math.min(0.98, chance));
    boolean detected = ThreadLocalRandom.current().nextDouble() < chance;
    return new PickpocketAttemptResult(true, detected, chance, summary.stolenSlotCount, rules);
  }

  private TheftSummary summarizeTheft(List<String> beforeEntries, List<String> afterEntries) {
    int max = Math.max(beforeEntries.size(), afterEntries.size());
    TheftSummary summary = new TheftSummary();
    for (int i = 0; i < max; i++) {
      ItemEntry before = parseEntry(i < beforeEntries.size() ? beforeEntries.get(i) : "");
      ItemEntry after = parseEntry(i < afterEntries.size() ? afterEntries.get(i) : "");
      int stolenQuantity = before.quantity;
      if (!before.itemId.isBlank() && before.itemId.equals(after.itemId)) {
        stolenQuantity = Math.max(0, before.quantity - after.quantity);
      } else if (before.itemId.isBlank()) {
        stolenQuantity = 0;
      }

      if (stolenQuantity <= 0 || before.itemId.isBlank()) {
        continue;
      }

      summary.stolenSlotCount++;
      summary.stolenQuantity += stolenQuantity;
      if (before.itemId.startsWith("Weapon_")) {
        summary.weaponSlots++;
      } else if (before.itemId.startsWith("Armor_")) {
        summary.armorSlots++;
      } else if (before.itemId.startsWith("Tool_")) {
        summary.toolSlots++;
      } else if (before.itemId.startsWith("Ore_") || before.itemId.startsWith("Ingredient_Bar_")) {
        summary.materialSlots++;
      } else if (before.itemId.startsWith("Recipe_")) {
        summary.recipeSlots++;
      }
    }
    return summary;
  }

  private ItemEntry parseEntry(String value) {
    if (value == null || value.isBlank()) {
      return new ItemEntry("", 0);
    }

    String[] parts = value.split(":", 2);
    if (parts.length != 2) {
      return new ItemEntry("", 0);
    }

    try {
      return new ItemEntry(parts[0], Integer.parseInt(parts[1]));
    } catch (NumberFormatException ignored) {
      return new ItemEntry("", 0);
    }
  }

  private PickpocketRoleResolver loadResolverConfig() {
    try (InputStream roleMapStream = getResource("config/pickpocket-role-map.json");
        InputStream resolverStream = getResource("config/pickpocket-loot-resolver.json");
        InputStream lootTableStream = getResource("config/pickpocket-loot-tables.json")) {
      if (roleMapStream == null || resolverStream == null || lootTableStream == null) {
        System.err.println("[Pickpocket] Missing bundled config JSON; using fallback merchant config.");
        return PickpocketRoleResolver.fallback();
      }

      JsonObject roleMapRoot = readJsonObject(roleMapStream);
      JsonObject resolverRoot = readJsonObject(resolverStream);
      JsonObject lootRoot = readJsonObject(lootTableStream);
      return PickpocketRoleResolver.fromJson(roleMapRoot, resolverRoot, lootRoot);
    } catch (Exception exception) {
      System.err.println("[Pickpocket] Failed to load loot resolver config; using fallback merchant config.");
      exception.printStackTrace(System.err);
      return PickpocketRoleResolver.fallback();
    }
  }

  private PickpocketRules loadRulesConfig() {
    try (InputStream rulesStream = getResource("config/pickpocket-rules.json")) {
      if (rulesStream == null) {
        System.err.println("[Pickpocket] Missing pickpocket rules config; using fallback rules.");
        return PickpocketRules.fallback();
      }

      JsonObject rulesRoot = readJsonObject(rulesStream);
      return PickpocketRules.fromJson(rulesRoot);
    } catch (Exception exception) {
      System.err.println("[Pickpocket] Failed to load pickpocket rules; using fallback rules.");
      exception.printStackTrace(System.err);
      return PickpocketRules.fallback();
    }
  }

  private JsonObject readJsonObject(InputStream inputStream) throws IOException {
    try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      return gson.fromJson(reader, JsonObject.class);
    }
  }

  private InputStream getResource(String resourcePath) {
    return PickpocketPlugin.class.getClassLoader().getResourceAsStream(resourcePath);
  }

  private PickpocketRules.RuleSet resolveRulesForRole(String roleName) {
    PickpocketRoleResolver.RoleLootProfile profile = roleResolver.resolveRole(roleName).orElse(null);
    String classProfile = profile == null ? "" : profile.classProfile();
    return pickpocketRules.resolve(classProfile);
  }

  private SessionCarryoverState pruneExpiredCarryover(UUID playerUuid, UUID npcUuid, long now) {
    PickpocketSessionKey key = new PickpocketSessionKey(playerUuid, npcUuid);
    SessionCarryoverState state = carryoverBySession.get(key);
    if (state == null) {
      return null;
    }
    if (state.expiresAtMillis() > now) {
      return state;
    }
    carryoverBySession.remove(key);
    return null;
  }

  private static final class TheftSummary {
    private int stolenSlotCount;
    private int stolenQuantity;
    private int weaponSlots;
    private int armorSlots;
    private int toolSlots;
    private int materialSlots;
    private int recipeSlots;

    private boolean stolenAnything() {
      return stolenSlotCount > 0;
    }
  }

  private record ItemEntry(String itemId, int quantity) {}

  public record SessionCarryover(int stealCount, int stolenQuantity) {
    static SessionCarryover empty() {
      return new SessionCarryover(0, 0);
    }
  }

  private record PickpocketSessionKey(UUID playerUuid, UUID npcUuid) {}

  private record SessionCarryoverState(
      int stealCount, int stolenQuantity, long expiresAtMillis) {}

  private record PickpocketAttemptResult(
      boolean stolenAnything,
      boolean detected,
      double detectionChance,
      int stolenSlotCount,
      PickpocketRules.RuleSet rules) {}
}
