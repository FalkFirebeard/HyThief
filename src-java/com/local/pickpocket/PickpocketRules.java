package com.local.pickpocket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class PickpocketRules {
  private final RuleSet defaults;
  private final Map<String, RuleOverride> classProfiles;

  private PickpocketRules(RuleSet defaults, Map<String, RuleOverride> classProfiles) {
    this.defaults = defaults;
    this.classProfiles = classProfiles;
  }

  static PickpocketRules fromJson(JsonObject root) {
    RuleSet defaults = RuleSet.fromJson(getObject(root, "defaults"));
    Map<String, RuleOverride> classProfiles = new LinkedHashMap<>();
    JsonObject classProfileRoot = getObject(root, "classProfiles");
    for (Map.Entry<String, JsonElement> entry : classProfileRoot.entrySet()) {
      classProfiles.put(entry.getKey(), RuleOverride.fromJson(entry.getValue().getAsJsonObject()));
    }
    return new PickpocketRules(defaults, classProfiles);
  }

  static PickpocketRules fallback() {
    return new PickpocketRules(
        new RuleSet(
            0.50, 0.05, 0.05, 0.02, 0.12, 6.0, -0.15, 10.0, 0.2, 0.08, 0.02, 0.08, 0.01, 0.06,
            0.004, 0.08, 0.07, 0.05, 0.03, 0.04, 45),
        Collections.emptyMap());
  }

  RuleSet resolve(String classProfile) {
    RuleOverride override = classProfiles.get(classProfile);
    return override == null ? defaults : override.apply(defaults);
  }

  private static JsonObject getObject(JsonObject root, String memberName) {
    JsonElement element = root.get(memberName);
    if (element == null || !element.isJsonObject()) {
      return new JsonObject();
    }
    return element.getAsJsonObject();
  }

  private static Double getOptionalDouble(JsonObject root, String memberName) {
    JsonElement element = root.get(memberName);
    return element == null || element.isJsonNull() ? null : element.getAsDouble();
  }

  private static Integer getOptionalInt(JsonObject root, String memberName) {
    JsonElement element = root.get(memberName);
    return element == null || element.isJsonNull() ? null : element.getAsInt();
  }

  record RuleSet(
      double baseDetectionChance,
      double crouchDetectionPenalty,
      double behindTargetDetectionPenalty,
      double sideTargetDetectionPenalty,
      double frontTargetDetectionBonus,
      double frontBlockDistance,
      double frontBlockFacingDot,
      double coneBlockDistance,
      double coneBlockFacingDot,
      double closeRangeDetectionBonus,
      double distanceReductionPerBlock,
      double stolenSlotDetectionBonus,
      double stolenQuantityDetectionBonus,
      double sessionStealDetectionBonus,
      double sessionQuantityDetectionBonus,
      double weaponDetectionBonus,
      double armorDetectionBonus,
      double toolDetectionBonus,
      double materialDetectionBonus,
      double recipeDetectionBonus,
      int cooldownSeconds) {
    static RuleSet fromJson(JsonObject root) {
      return new RuleSet(
          root.get("baseDetectionChance").getAsDouble(),
          root.get("crouchDetectionPenalty").getAsDouble(),
          root.get("behindTargetDetectionPenalty").getAsDouble(),
          root.get("sideTargetDetectionPenalty").getAsDouble(),
          root.get("frontTargetDetectionBonus").getAsDouble(),
          root.get("frontBlockDistance").getAsDouble(),
          root.get("frontBlockFacingDot").getAsDouble(),
          root.get("coneBlockDistance").getAsDouble(),
          root.get("coneBlockFacingDot").getAsDouble(),
          root.get("closeRangeDetectionBonus").getAsDouble(),
          root.get("distanceReductionPerBlock").getAsDouble(),
          root.get("stolenSlotDetectionBonus").getAsDouble(),
          root.get("stolenQuantityDetectionBonus").getAsDouble(),
          root.get("sessionStealDetectionBonus").getAsDouble(),
          root.get("sessionQuantityDetectionBonus").getAsDouble(),
          root.get("weaponDetectionBonus").getAsDouble(),
          root.get("armorDetectionBonus").getAsDouble(),
          root.get("toolDetectionBonus").getAsDouble(),
          root.get("materialDetectionBonus").getAsDouble(),
          root.get("recipeDetectionBonus").getAsDouble(),
          root.get("cooldownSeconds").getAsInt());
    }
  }

  record RuleOverride(
      Double baseDetectionChance,
      Double frontBlockDistance,
      Double frontBlockFacingDot,
      Double coneBlockDistance,
      Double coneBlockFacingDot,
      Integer cooldownSeconds) {
    static RuleOverride fromJson(JsonObject root) {
      return new RuleOverride(
          getOptionalDouble(root, "baseDetectionChance"),
          getOptionalDouble(root, "frontBlockDistance"),
          getOptionalDouble(root, "frontBlockFacingDot"),
          getOptionalDouble(root, "coneBlockDistance"),
          getOptionalDouble(root, "coneBlockFacingDot"),
          getOptionalInt(root, "cooldownSeconds"));
    }

    RuleSet apply(RuleSet defaults) {
      return new RuleSet(
          baseDetectionChance == null ? defaults.baseDetectionChance() : baseDetectionChance,
          defaults.crouchDetectionPenalty(),
          defaults.behindTargetDetectionPenalty(),
          defaults.sideTargetDetectionPenalty(),
          defaults.frontTargetDetectionBonus(),
          frontBlockDistance == null ? defaults.frontBlockDistance() : frontBlockDistance,
          frontBlockFacingDot == null ? defaults.frontBlockFacingDot() : frontBlockFacingDot,
          coneBlockDistance == null ? defaults.coneBlockDistance() : coneBlockDistance,
          coneBlockFacingDot == null ? defaults.coneBlockFacingDot() : coneBlockFacingDot,
          defaults.closeRangeDetectionBonus(),
          defaults.distanceReductionPerBlock(),
          defaults.stolenSlotDetectionBonus(),
          defaults.stolenQuantityDetectionBonus(),
          defaults.sessionStealDetectionBonus(),
          defaults.sessionQuantityDetectionBonus(),
          defaults.weaponDetectionBonus(),
          defaults.armorDetectionBonus(),
          defaults.toolDetectionBonus(),
          defaults.materialDetectionBonus(),
          defaults.recipeDetectionBonus(),
          cooldownSeconds == null ? defaults.cooldownSeconds() : cooldownSeconds);
    }
  }
}
