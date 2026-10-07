package com.local.pickpocket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class PickpocketRoleResolver {
  private final Map<String, String> roleToGroup;
  private final Set<String> blockedGroups;
  private final Set<String> blockedRoles;
  private final Map<String, GroupAssignment> assignments;
  private final Map<String, LootTable> tables;

  private PickpocketRoleResolver(
      Map<String, String> roleToGroup,
      Set<String> blockedGroups,
      Set<String> blockedRoles,
      Map<String, GroupAssignment> assignments,
      Map<String, LootTable> tables) {
    this.roleToGroup = roleToGroup;
    this.blockedGroups = blockedGroups;
    this.blockedRoles = blockedRoles;
    this.assignments = assignments;
    this.tables = tables;
  }

  static PickpocketRoleResolver fromJson(
      JsonObject roleMapRoot, JsonObject resolverRoot, JsonObject lootRoot) {
    Map<String, String> roleToGroup = new HashMap<>();
    JsonObject groups = getObject(roleMapRoot, "groups");
    for (Map.Entry<String, JsonElement> groupEntry : groups.entrySet()) {
      String groupName = groupEntry.getKey();
      for (JsonElement roleElement : groupEntry.getValue().getAsJsonArray()) {
        roleToGroup.put(roleElement.getAsString(), groupName);
      }
    }

    Set<String> blockedGroups = new LinkedHashSet<>();
    blockedGroups.addAll(readStringSet(roleMapRoot, "non_pickpocketable_groups"));
    blockedGroups.addAll(readStringSet(resolverRoot, "blockedGroups"));

    Set<String> blockedRoles = new LinkedHashSet<>();
    blockedRoles.addAll(readStringSet(roleMapRoot, "non_pickpocketable_roles"));
    blockedRoles.addAll(readStringSet(resolverRoot, "blockedRoles"));

    Map<String, GroupAssignment> assignments = new LinkedHashMap<>();
    JsonObject assignmentsRoot = getObject(resolverRoot, "groupAssignments");
    for (Map.Entry<String, JsonElement> entry : assignmentsRoot.entrySet()) {
      assignments.put(entry.getKey(), GroupAssignment.fromJson(entry.getValue().getAsJsonObject()));
    }

    Map<String, LootTable> tables = new LinkedHashMap<>();
    JsonObject tablesRoot = getObject(lootRoot, "tables");
    for (Map.Entry<String, JsonElement> entry : tablesRoot.entrySet()) {
      tables.put(entry.getKey(), LootTable.fromJson(entry.getValue().getAsJsonObject()));
    }

    return new PickpocketRoleResolver(roleToGroup, blockedGroups, blockedRoles, assignments, tables);
  }

  static PickpocketRoleResolver fallback() {
    Map<String, String> roleToGroup = new HashMap<>();
    roleToGroup.put("Kweebec_Merchant", "neutral_merchant");

    Map<String, LootTable> tables = new LinkedHashMap<>();
    tables.put(
        "fallback_merchant",
                new LootTable(
            0,
            3,
            List.of(
                new LootEntry("Ingredient_Spices", "", 1, 3, 24),
                new LootEntry("Ingredient_Salt", "", 1, 3, 24),
                new LootEntry("Ingredient_Dough", "", 1, 2, 20),
                new LootEntry("Food_Pie_Apple", "", 1, 1, 12),
                new LootEntry("Weapon_Arrow_Crude", "", 8, 32, 12),
                new LootEntry("Empty", "", 0, 0, 32))));

    Map<String, GroupAssignment> assignments = new HashMap<>();
    assignments.put(
        "neutral_merchant",
        new GroupAssignment(
            new AssignmentChoice("fallback_merchant", "merchant", "Kweebec"),
            Collections.emptyMap(),
            Collections.emptyMap()));

    return new PickpocketRoleResolver(
        roleToGroup,
        Set.of(
            "neutral_youth",
            "neutral_special",
            "passive_special",
            "aggressive_beast",
            "aggressive_edible"),
        Collections.emptySet(),
        assignments,
        tables);
  }

  Optional<RoleLootProfile> resolveRole(String roleName) {
    if (roleName == null || roleName.isBlank()) {
      return Optional.empty();
    }
    if (blockedRoles.contains(roleName)) {
      return Optional.empty();
    }

    String groupName = roleToGroup.get(roleName);
    if (groupName == null || blockedGroups.contains(groupName)) {
      return Optional.empty();
    }

    GroupAssignment assignment = assignments.get(groupName);
    if (assignment == null) {
      return Optional.empty();
    }

    AssignmentChoice choice = assignment.resolve(roleName);
    if (choice == null) {
      return Optional.empty();
    }

    LootTable table = tables.get(choice.table());
    if (table == null) {
      return Optional.empty();
    }

    return Optional.of(
        new RoleLootProfile(roleName, groupName, choice.table(), choice.classProfile(), choice.race()));
  }

  Optional<LootTable> getTable(String tableName) {
    return Optional.ofNullable(tables.get(tableName));
  }

  private static JsonObject getObject(JsonObject root, String memberName) {
    JsonElement element = root.get(memberName);
    if (element == null || !element.isJsonObject()) {
      return new JsonObject();
    }
    return element.getAsJsonObject();
  }

  private static Set<String> readStringSet(JsonObject root, String memberName) {
    JsonElement element = root.get(memberName);
    if (element == null || !element.isJsonArray()) {
      return Collections.emptySet();
    }

    Set<String> values = new LinkedHashSet<>();
    for (JsonElement item : element.getAsJsonArray()) {
      values.add(item.getAsString());
    }
    return values;
  }

  record RoleLootProfile(
      String roleName, String groupName, String tableName, String classProfile, String race) {}

  record AssignmentChoice(String table, String classProfile, String race) {
    static AssignmentChoice fromJson(JsonObject object) {
      return new AssignmentChoice(
          getString(object, "table"),
          getString(object, "classProfile"),
          getString(object, "race"));
    }
  }

  static final class GroupAssignment {
    private final AssignmentChoice direct;
    private final Map<String, AssignmentChoice> byRole;
    private final Map<String, AssignmentChoice> byPrefix;

    GroupAssignment(
        AssignmentChoice direct,
        Map<String, AssignmentChoice> byRole,
        Map<String, AssignmentChoice> byPrefix) {
      this.direct = direct;
      this.byRole = byRole;
      this.byPrefix = byPrefix;
    }

    static GroupAssignment fromJson(JsonObject object) {
      AssignmentChoice direct = object.has("table") ? AssignmentChoice.fromJson(object) : null;
      Map<String, AssignmentChoice> byRole = readChoiceMap(object, "byRole");
      Map<String, AssignmentChoice> byPrefix = readChoiceMap(object, "byPrefix");
      return new GroupAssignment(direct, byRole, byPrefix);
    }

    AssignmentChoice resolve(String roleName) {
      AssignmentChoice directRole = byRole.get(roleName);
      if (directRole != null) {
        return directRole;
      }

      AssignmentChoice bestPrefixChoice = null;
      int bestPrefixLength = -1;
      for (Map.Entry<String, AssignmentChoice> entry : byPrefix.entrySet()) {
        String prefix = entry.getKey();
        if (roleName.startsWith(prefix) && prefix.length() > bestPrefixLength) {
          bestPrefixChoice = entry.getValue();
          bestPrefixLength = prefix.length();
        }
      }
      if (bestPrefixChoice != null) {
        return bestPrefixChoice;
      }

      return direct;
    }

    private static Map<String, AssignmentChoice> readChoiceMap(JsonObject root, String memberName) {
      JsonElement element = root.get(memberName);
      if (element == null || !element.isJsonObject()) {
        return Collections.emptyMap();
      }

      Map<String, AssignmentChoice> values = new LinkedHashMap<>();
      for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
        values.put(entry.getKey(), AssignmentChoice.fromJson(entry.getValue().getAsJsonObject()));
      }
      return values;
    }
  }

  record LootEntry(String itemId, String tableRef, int min, int max, int weight) {
    static LootEntry fromJson(JsonObject object) {
      return new LootEntry(
          getString(object, "itemId"),
          getString(object, "tableRef"),
          getInt(object, "min", 0),
          getInt(object, "max", 0),
          getInt(object, "weight", 0));
    }
  }

  record LootTable(int nothingFoundChancePercent, int maxStealsPerRefresh, List<LootEntry> entries) {
    static LootTable fromJson(JsonObject object) {
      int nothingFoundChancePercent = Math.max(0, Math.min(100, getInt(object, "nothingFoundChancePercent", 0)));
      int maxStealsPerRefresh = getInt(object, "maxStealsPerRefresh", 1);
      List<LootEntry> entries = new ArrayList<>();
      JsonElement entryElement = object.get("entries");
      if (entryElement != null && entryElement.isJsonArray()) {
        JsonArray array = entryElement.getAsJsonArray();
        for (JsonElement element : array) {
          entries.add(LootEntry.fromJson(element.getAsJsonObject()));
        }
      }
      return new LootTable(nothingFoundChancePercent, maxStealsPerRefresh, entries);
    }
  }

  private static String getString(JsonObject object, String memberName) {
    JsonElement element = object.get(memberName);
    return element == null || element.isJsonNull() ? "" : element.getAsString();
  }

  private static int getInt(JsonObject object, String memberName, int defaultValue) {
    JsonElement element = object.get(memberName);
    return element == null || element.isJsonNull() ? defaultValue : element.getAsInt();
  }
}
