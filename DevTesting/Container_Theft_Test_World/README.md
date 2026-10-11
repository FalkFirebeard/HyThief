# HyThief Container Theft Test World - container.10 / open field

This geometry-only correction removes both responder shelters and both prebuilt witness LOS walls. Stationary Kweebec and Feran witnesses remain fixed; Razorleaf and Sharptooth remain mobile. All six stations now have clear witness LOS by default. There are no permanent LOS barriers or responder enclosures.

Use the existing Reset Container Theft shortcut with Hytale closed. It restores this master and archives the dirty active save. Other worlds are untouched. Stand on a brick pad and withdraw one copper bar; opening/browsing alone must do nothing.

1. Station 1 CONTROL: no notification or theft response.
2. Station 3 KWEEBEC FACTION: one caught notification and Razorleaf response. Reset afterward.
3. Station 4 FERAN FACTION: one caught notification and Sharptooth response. Reset afterward.
4. Station 2 NEARBY: both witnesses eligible; one notification and one handoff (deterministic order selects Feran). Reset afterward.
5. Station 5 KWEEBEC MANUAL LOS: place your own opaque blocks between the Kweebec witness and the player pad, keeping Feran LOS clear. Withdrawal should be unseen: the Feran cannot witness Kweebec-faction theft. Without your blocks, expect caught/Razorleaf. Reset afterward.
6. Station 6 FERAN MANUAL LOS: block the Feran witness yourself while keeping Kweebec LOS clear. Withdrawal should be unseen. Without your blocks, expect caught/Sharptooth.

Use standing for this checkpoint. Manually placed walls should block the witness-to-player view without enclosing a responder or obstructing its pursuit route. Reset removes your temporary blocks. Guard behavior can contaminate later tests, so reset between caught tests.

Native chests, persistent ownership, faction filtering, notification and guard delivery are unchanged. Container sight remains 20 standing / 6 crouched; normal HyThief perception remains 15 / 4. Installed HyThief-1.3.1-container.10.jar is unchanged. Native automated validation is useful evidence; Firebeard must confirm pursuit in the real client.
