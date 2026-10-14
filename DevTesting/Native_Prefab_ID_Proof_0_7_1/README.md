# Native Prefab ID Proof — Hytale 0.7.1

Decision from native tests: **PREFAB OWNERSHIP DEFERRED** for an automatic production path. Stable marker IDs exist, but no shared native chest ID or clean stock block-association hook was verified. This fixture is diagnostic only; no ownership components/IDs are added.

Active world: `C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves\HyThief Native Prefab ID Proof`. Installed HyThief remains 1.3.1-container.11; per-world diagnostic helper is NativePrefabIdProof-1.0.0.jar, target =0.7.1. Do not install the helper globally. It overrides the stock Zone1_Kweebec_Tier3 block-spawner choice **inside this proof world only** to select its unchanged chest entry and omit its Empty entry. Stock prefab, merchant, drop table, native chest interactions and native spawn references remain intact. Existing Container Theft Test World and its reset shortcut are unchanged.

1. Load HyThief Native Prefab ID Proof.
2. Walk to PREFAB A, open its native small chest (-10,65,1), and approach/use normal merchant interaction.
3. Repeat at PREFAB B, chest (14,65,1).
4. Exit Hytale completely, restart and reload **the same world without resetting**.
5. Repeat A/B. No commands or manual setup.

Area anchors: A=(-12,64,0), B=(12,64,0); spawn is in front of both shops. Labels persist. Native NPC and marker spawning are enabled; merchants may naturally move/respawn. Native IDs expected: A=274125628; B=274840612. Merchant UUID changes are allowed; its marker reference/ID must remain consistent. Automatic diagnostics print concise `PREFAB A` / `PREFAB B` lines at startup or native membership changes. Full JSON appears under the server working directory's `prefab-id-diagnostics`, normally the save root; server logs also contain the grouped lines. No commands required to start diagnostics. Ordinary open/trade does not need to modify identity to log it.

Our headless proof invoked the native marker spawn method without a connected player; the final client fixture enables normal spawning. The physical client flow is still awaiting Firebeard. Do not treat automated save/restart results as that client confirmation.

Rebuild helper with shared JDK25 against `.build/dependencies/HytaleServer-0.7.1.jar`: compile NativePrefabIdProof.java and NativeFieldBlocks.java, copy diagnostic-manifest.json as classes/manifest.json and FixtureAssets/Server into classes/Server, then package classes into NativePrefabIdProof-1.0.0.jar. Source prefab is copied byte-for-byte from current Assets.zip at Server/Prefabs/Npc/Kweebec/Autumn/Shops/Kweebec_Autumn_Shops_001.prefab.json, named native-shop.prefab.json beside config.json. It is deliberately local-only. Provider codec Type=NativePrefabIdProof; fresh native server/world codecs use Version=4. Only the native Prop calculates IDs.

Native runs: ready-native.log, restart-native.log, client-ready-native.log; snapshots: before-restart.json, after-restart.json, final-native-snapshot.json. Earlier incomplete helper runs remain preserved. Actual save master and active copy contain no automated shutdown flags. Do not overwrite them with failed/partial runs.
