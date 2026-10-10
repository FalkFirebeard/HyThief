# Container Theft Checkpoint 2 — container.3 client failure and container.4 ingress repair

Checkpoint remains pending Firebeard's real-client FRONT standing acceptance test.

## Actual failed run

Server: `UserData/Saves/HyThief Container Theft Test World/logs/2026-10-09_19-42-32_server.log`.
Client: `UserData/Logs/2026-10-09_19-42-17_client.log`.
Full failed save/log rollback: `DevTesting/Container_Theft_Test_World/Rollback-container.3-before-ingress/Failed-client-save`.

- Client identifies `HyThief-1.3.1-container.3.jar`; server enables `Local:HyThief` at 01:42:44 UTC.
- Last successful logged container stage: `AUTO_REGISTER` at 01:42:45 UTC, both owned coordinates `(0,64,16)` FRONT and `(0,64,4)` REAR, CONTROL unowned.
- No REMOVE, WITNESS, DANGER_SENT, DANGER_NOT_SENT, CONTROL_UNOWNED_IGNORE, or paired-removal failure records follow. Therefore live witness distance, facing, LOS, offender delivery and guard readiness cannot be reconstructed from this old log. Do not fabricate these values from the offline fixture test.
- Persisted Firebeard hotbar contains copper/fibre/iron; actual item receipt occurred, despite no diagnostic transfer records. Native chest opening is confirmed correct by Firebeard.
- Run uses `QuicheConnection`. Stable installed server and pinned build server have identical SHA256 `DCD2956CC65B950084650EADD56B889CF51F7610C127C3DAAC1F74DC471FEC97`.

## Confirmed first broken boundary

The native withdrawal packet reaches the game's registered handler without reaching HyThief's old inbound adapter. Verified against the installed stable server bytecode:

`QuicheChannel.read` → `ConnectionHandler.handle(ToServerPacket)` → `PacketHandler.handle` → `GenericPacketHandler.accept` → registered inventory consumer.

`PacketHandler.handle` only calls `accept`. The Quiche path never calls `PacketAdapters.__handleInbound`. The call exists in legacy `PlayerChannelHandler.channelRead`, whose Netty path does not explain this Quiche client session.

HyThief container.3 depended entirely on `PacketAdapters.registerInbound(PlayerPacketFilter)` for withdrawal attribution. Consequently its owned-removal probe, visual witness evaluator and danger adapter were never entered through this transport. This explains why offline witness/transaction tests passed: they directly invoked inventory/probe code and did not exercise transport ingress.

Official API confirms the adapter registration surface, but does not promise transport coverage: https://docs.hytale.com/api/com/hypixel/hytale/server/core/io/adapter/PacketAdapters . Installed stable dispatch behavior is the decisive evidence. No live installation is patched.

## Small correction

Diagnostic container builds bind exactly `MoveItemStack` and `SmartMoveItemStack` via native `IPacketHandler.registerHandler` after PlayerReady in the marked fixture (or manual opt-in). Binding runs on the connection executor, is idempotent for repeated ready packets, and delegates ineligible requests to the existing native InventoryPacketHandler. Shutdown restores its two native consumers. No Netty/Quiche patches, reflection, bytecode changes, or movement/combat loops.

The old transfer execution/probe, ownership gate, paired receipt, visual checks, one-ready-guard selection and native beacon handoff remain unchanged. Other packet types, chest assets/opening, UI, crouch multipliers, perception, catch logic and production version are unchanged. This is a development-only two-handler compatibility bridge; any future production event-based container system should revisit the integration surface rather than assume inbound adapters cover Quiche.

## Development trace boundaries

1. `INGRESS_READY`, `INGRESS_BOUND` (or explicit bind failure).
2. `INGRESS`: packet, player UUID, world UUID, fixture enabled.
3. `SECTION_RESOLVED`: native source/destination identities, ownership key and registered container identities; explicit passthrough reason.
4. `OWNED_REQUEST`: offender Ref plus UUID and chest key.
5. `OWNED_REMOVE_CAPTURE`: successful native source-removal capture; `REMOVE Result=SUCCESS` only after paired player receipt.
6. `WITNESS`: exact fixture Ref, role, position, head yaw, player position/crouch, distance, 15-block baseline range, 180-degree sector result, native LOS result and rejection reason. LOS is gated by range/sector; `LOS=false` on those rejection paths is not an independently traced raycast. Exceptions include a stack trace.
7. `DANGER_ADAPTER`, each appropriate `GUARD_CANDIDATE`: role, state, slot, enabled/queued/readiness, position.
8. `NATIVE_DANGER_QUEUED`: signal, guard, queue status and exact offender comparison, followed by DANGER_SENT/NOT_SENT.
9. Two `NATIVE_GUARD_OBSERVATION` snapshots at 250/1250ms: state, queued flag, offender marked in ECS. These are passive evidence, not forced state/target changes. Queue disappearance alone does not prove a sensor match (expiry is possible); combat state plus offender target is stronger evidence. Physical native response still requires the client test.

## Validation and limits

- Clean build: `1.3.1-container.4`, manifest target `=0.6.8`, Java25/Gradle9.1.0. Two preexisting MerchantStoragePage inventory-removal warnings and Gradle deprecations remain.
- New registered-dispatch test proves both transfer types enter the bridge directly, native fallback executes once, shutdown restores native dispatch, and only two registrations are touched. It does not simulate an authenticated client or PlayerReady session.
- Fresh disposable native-server validation passes the original paired-removal matrix, all native chest state/ownership checks, visual front/rear/range/opaque-wall checks, exact removal-time snapshot, and one native danger queue carrying the exact modeled actor.
- Native verification run: `VerificationRuns/20261009-200532-190f96a902384a53a3c665056261fe85`.
- The visual test still uses an inert modeled actor rather than Firebeard's Player ECS. New live logs will expose any further component, window identity, geometry or native guard consumption differences. No such downstream failure was confirmed in container.3, so none was guessed or bypassed.
- Master/fixture pack and native chest assets are byte-identical to container.3. Previous source/JAR and failed live save preserved; reset archives the dirty save again before restoring only this test world. Other worlds, archived releases, prefabs and unrelated installed mods remain protected.

## Firebeard retest

Launch the reset Container Theft Test World. Remain standing. No setup commands.

1. CONTROL: open once and transfer an item into inventory; no reaction.
2. REAR: open once and transfer an item into inventory; no reaction.
3. FRONT last: stand on the civilian side of the FRONT chest, visibly in front of the +Z-facing Sapling; transfer an item into inventory. Expect the native Razorleaf to become hostile and pursue around the south end of its shield wall.

Do not count merely holding an item on the cursor as the acceptance result; complete the transfer into your inventory. Watch briefly for guard pursuit. If no response, quit to flush the log and report which chest/item you moved and whether it reached inventory. The server log in this save's `logs` directory now identifies the last successful boundary. No crouching, sound, random catch roll or range widening is added.
