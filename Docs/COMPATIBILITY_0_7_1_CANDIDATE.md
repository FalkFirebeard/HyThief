# HyThief 1.3.2 compatibility candidate

Not published; client smoke test pending. Exact target `=0.7.1`, revision `69c03ffa58d7e2f46bce8b0356ed206cfc7f10e6`.

Updated exact compatibility target to Hytale 0.7.1. Existing gameplay unchanged; compatibility validated against the current update. Built from public v1.3.1, excluding ownership/container-theft development and experimental fixtures.

Clean build passed. Final JAR SHA-256: `38c3d5e464c26c1bbca01a4d57424d349b5bb45dca3b502b1937ac015bd16938`. Only manifest.json differs from the released archive; all gameplay classes/assets are byte-identical. Full matrix passed: 174 concrete + 2 abstract + 9 historical retired entries. Caught offender delivered via native Kweebec_All_Danger; Razorleaf transitioned to Attack.Default. General 16-NPC test world passed.

Existing deprecation warnings remain. Released artifacts are preserved. No public release tag/publication. Client smoke: trade, crouched rear pickpocket, steal, caught notification/UI closure/native guard reaction.
