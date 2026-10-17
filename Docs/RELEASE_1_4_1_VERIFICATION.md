# HyThief 1.4.1 UI hotfix verification

Clean build passed on Java 25 / Gradle 9.1.0 against Hytale 0.7.2, revision d769dc3d57881261a62858e1394f0e4de9a21eca. Manifest target remains `=0.7.2`.

The existing shared language-entry correction was reused. All 176 active supported role definitions (174 concrete and two abstract definitions) resolve through the same key, `server.hythief.interactionHints.pickpocketTest`, now displayed exactly as `Pickpocket`. Nine retired historical IDs remain excluded. No interaction condition or role membership was edited.

Strict archive comparison with the exact public 1.4.0 JAR passed. Entry names are identical. Only `manifest.json` (Version 1.4.0 -> 1.4.1) and `Server/Languages/en-US/server.lang` (development suffix removed) differ. All 48 Java classes and every other asset are byte-identical. No gameplay source, ownership, witnesses, crime, NPC AI, container theft or Wardrobes integration changed. This provides hotfix regression evidence without rerunning or modifying test worlds. No new connected-client test is claimed.

Existing warnings remain: two MerchantStoragePage inventory removal warnings, ContainerTheftService deprecated API note and Gradle 10 compatibility notice. No compiler errors or new warnings were introduced.

Exact staged artifact: `.build/public-release/1.4.1/HyThief-1.4.1.jar`.

SHA-256: `d91de629dc0d607b4172e28031c5deeb196b6dfa55f6f258d886fc721efb8395`

The 1.4.0 public artifact, tag, development history, rollbacks and worlds are preserved. Publication identifiers and remote artifact verification will be recorded in `RELEASE_1_4_1_PUBLICATION.md`.
