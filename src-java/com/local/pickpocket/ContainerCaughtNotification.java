package com.local.pickpocket;

import com.hypixel.hytale.server.core.Message;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** One native caught message per request, after successful withdrawal pairing. */
final class ContainerCaughtNotification {
  private ContainerCaughtNotification() {}
  static Optional<ContainerVisualWitness.Evidence> notifyWitnessed(
      List<ContainerVisualWitness.Evidence> confirmed, Consumer<Message> send) {
    var witnessed = confirmed.stream().filter(ContainerVisualWitness.Evidence::seen).findFirst();
    witnessed.ifPresent(visual -> send.accept(Message.raw("You have been caught stealing!")));
    return witnessed;
  }
}
