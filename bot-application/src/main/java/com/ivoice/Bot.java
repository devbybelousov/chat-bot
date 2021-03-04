package com.ivoice;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpEntity.Strict;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.Materializer;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Bot {

  private final ActorContext<Command> ctx;
  private String question;

  private Bot(ActorContext<Command> ctx) {
    this.ctx = ctx;
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(ctx -> new Bot(ctx).waiting());
  }

  private Behavior<Command> waiting() {
    return Behaviors.receive(Command.class)
        .onMessage(Think.class, msg -> startRead())
        .build();
  }

  private Behavior<Command> startRead() {
    ctx.getSelf().tell(Read.INSTANCE);
    return read();
  }

  private Behavior<Command> read() {
    return Behaviors.receive(Command.class)
        .onMessageEquals(Read.INSTANCE, () -> {
          System.out.println("Please, ask your question: ");

          Scanner scanner = new Scanner(System.in);
          question = scanner.nextLine();

          if (question.toLowerCase().equals("n") || question.toLowerCase().equals("no questions")) {
            ctx.getSystem().terminate();
          }

          System.out.println("Question accepted, searching  knowledge base...");
          return startSend();
        })
        .build();
  }

  private Behavior<Command> startSend() {
    ctx.getSelf().tell(Send.INSTANCE);
    return send();
  }

  private Behavior<Command> send() {
    return Behaviors.receive(Command.class)
        .onMessageEquals(Send.INSTANCE, () -> {
          String url = "http://localhost:8080/ask?question=" + question.replace(" ", "%20");

          final CompletionStage<HttpResponse> responseFuture =
              Http.get(ctx.getSystem()).singleRequest(HttpRequest.create(url));

          try {
            HttpResponse httpResponse = responseFuture.toCompletableFuture().get();
            Materializer materializer = Materializer.createMaterializer(ctx.getSystem());
            Strict strict = httpResponse.entity().toStrict(10, materializer).toCompletableFuture()
                .get();
            String body = strict.getData().utf8String();
            System.out.println(body);
          } catch (InterruptedException | ExecutionException e) {
            return Behaviors.stopped();
          }
          return startRead();
        })
        .build();
  }

  enum Read implements Command {
    INSTANCE
  }

  enum Think implements Command {
    INSTANCE
  }

  enum Send implements Command {
    INSTANCE
  }

  interface Command {

  }
}
