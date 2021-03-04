package com.ivoice;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import com.ivoice.Bot.Think;

public class App {

  public static void main(String[] args) {
    ActorSystem.create(App.create(), "bot-application");
  }

  private static Behavior<NotUsed> create() {
    return Behaviors.setup(context -> new App(context).behavior());
  }

  private final ActorContext<NotUsed> context;

  private App(ActorContext<NotUsed> context) {
    this.context = context;
  }

  private Behavior<NotUsed> behavior() {
    context.spawn(Bot.create(), "bot").tell(Think.INSTANCE);
    return Behaviors.empty();
  }
}
