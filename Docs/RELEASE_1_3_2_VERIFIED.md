# HyThief 1.3.2 — public publication checkpoint

Public version **1.3.2**, exact Hytale target **=0.7.1**. Approved client-tested artifact published without rebuilding.

SHA-256: `38c3d5e464c26c1bbca01a4d57424d349b5bb45dca3b502b1937ac015bd16938`.
Public commit: `f7119cbcc23c5740d553254354ae3a3e67509555`, annotated tag **v1.3.2**, branch **compatibility/1.3.2**.

## GitHub

[Official release](https://github.com/FalkFirebeard/HyThief/releases/tag/v1.3.2) is live, not draft/prerelease, and verified as the **latest** GitHub release. Assets **HyThief-1.3.2.jar** and **SHA256SUMS.txt** downloaded and verified; downloaded JAR and API digest match the approved hash.

## CurseForge

Existing project **1496959**, new file **9169008**, [file page](https://www.curseforge.com/hytale/mods/hythief/files/9169008). Display name **HyThief 1.3.2 — Hytale 0.7.1**; release type **Release**; offered game category **0.7**, exact 0.7.1 explicitly stated in title/manifest/notes/description. Configured to publish automatically once approved.

Current authoritative author status: **Processing**. Approval/live/default cannot yet be claimed. Existing 1.3.1 remains the current CurseForge main/default file until moderation completes. Do not re-upload a processing file; inspect this same ID later. The displayed CurseForge CDN URL already serves the exact JAR; downloaded SHA matches the approved hash. Main public description was saved and verified live with 1.3.2/0.7.1, current pickpocket features and general active-future-development wording.

## Scope and proof

Candidate ZIP integrity and all JSON parsed. Exactly one archive entry differs from public 1.3.1: manifest.json. All gameplay classes and non-manifest assets are byte-identical. Public Java/config/BuildSupport/Server/Common source matches v1.3.1, with metadata/docs updated separately.

Explicit exclusions verified: no container theft/ownership, FACTION/NEARBY container policy, LOCAL theft alerts, persistent ownership, PREFAB work, test-world/prefab/diagnostic helper assets/classes or other unreleased features.

Prior native validation passed clean build, full NPC matrix (174 concrete + 2 abstract, 9 historical IDs inactive), pickpocket/caught flow and native Razorleaf target/state handoff. Firebeard explicitly confirmed the required real-client smoke test on Hytale 0.7.1. No rebuild or new runtime experiment was performed during publication.

Public branch README, build documentation and changelog/release notes reflect 1.3.2/0.7.1. Main README updated only as public-facing documentation; GitHub repository description updated. Historical versioned documentation retains its original context. Public source remains separate from main development.

## Development preservation

Development source/build/assets/config hashes exactly match the pre-publication snapshot at main commit `1f62a3a6d8a62295718085c9444ec899aed7266d`. No development merge/replacement or gameplay change. Preserved development **HyThief-1.3.1-container.11.jar** remains at **DevTesting/Update_0_7_1/Candidates/Development/**, SHA-256 **f2dca782e22c38839640e8886f764b16ca00c36cdf8b4c812d6c099181eae08c**, target =0.7.1. Rollbacks, historical tags and test-world masters remain untouched.

Exactly the approved public JAR remains installed; no installed mod swap or world reset was performed. Wonderous Wardrobes was not modified or published in this task. No PREFAB work started.

Local release evidence: **.build/public-release/1.3.2/** — JAR, sums, notes/description draft, public commit, prepublish proof, downloaded GitHub/CurseForge copies, API/author status and development preservation hashes. This checkpoint intentionally records CurseForge moderation as pending.
