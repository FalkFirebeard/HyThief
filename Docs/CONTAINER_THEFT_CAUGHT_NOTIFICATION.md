# Container theft caught notification — container.6

Container.5 real-client acceptance: CONTROL and REAR caused no response; witnessed FRONT theft caused Razorleaf native attack. The native chest and LastSeen guard handoff remain unchanged.

Container.6 adds only the native player message `You have been caught stealing!` after successful owned withdrawal pairing and confirmed visual witness, before the existing danger adapter. It reuses PlayerRef.sendMessage(Message.raw(...)), the same native message API as existing pickpocket caught messages. No new UI.

One message per transfer request even if the request produces multiple confirmed withdrawal records. Separate successful witnessed transfers can each notify. Notification is independent of whether a guard is ready. Opening/browsing, unowned withdrawals, unseen withdrawals, insertion, internal rearrangement and failed transactions do not create confirmed witnessed withdrawals and therefore do not notify.

Validation: clean build against pinned stable 0.6.8; manifest =0.6.8. Native integration checks CONTROL=0, REAR=0, FRONT=1 exact text; multiple evidence records give one message. Existing native transaction matrix now also asserts notification counts for successful/failed/insertion/rearrangement cases. Native guard pursuit still retains the offender and moves using native AI. PickpocketPlugin.java and MerchantStoragePage.java are byte-identical to container.5, including caught messages. Existing inventory deprecation warnings remain.

Reference: official PlayerRef message API https://docs.hytale.com/api/com/hypixel/hytale/server/core/universe/PlayerRef ; installed stable server API checked; stable server SHA256 DCD2956CC65B950084650EADD56B889CF51F7610C127C3DAAC1F74DC471FEC97 unchanged.

Deployment: exactly one active HyThief, version 1.3.1-container.6. Previous installed JAR and source/verifier rollback: DevTesting/Container_Theft_Test_World/Rollback-container.5-before-caught-notification. Test-world master, role pack, native chest assets, sight/FOV/LOS, LastSeen logic, ownership and crouch behavior unchanged. Existing safe reset archives the dirty live save before restoring checksum-verified master. Other worlds/mods are preserved.

Real-client test pending: standing CONTROL -> REAR -> FRONT last. Opening/browsing must not notify. CONTROL withdrawal no message/reaction; REAR withdrawal no message/reaction; one FRONT withdrawal gives exactly one caught message, then native Razorleaf pursuit/attack. Existing pickpocket caught notifications should remain unchanged. Automated tests check server message dispatch/policy; real-client visibility is Firebeard's checkpoint.
