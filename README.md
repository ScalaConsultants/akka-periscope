# akka-periscope

![CI](https://github.com/ScalaConsultants/akka-periscope/workflows/Scala%20CI/badge.svg)
![Maven Central](https://img.shields.io/maven-central/v/io.scalac/akka-periscope-core_2.13.svg)

Akka diagnostics collector plugin.

Provides akka-related data for [Panopticon monitoring tool](https://github.com/ScalaConsultants/panopticon-tui), but can be used for general purposes as well.

## What is this?

This is a small library for [akka](https://akka.io/) applications, that can collect valuable data about your actor system:

 * the whole tree of actors in the actor system, starting from root guardians;
 * total count of actors in the actor system.

It also provides facilities for this data to be exposed for diagnostic purposes, specifically to work with [Panopticon](https://github.com/ScalaConsultants/panopticon-tui), which is a terminal-based monitoring tool for Scala apps.

## Dependencies?

No, except for (obviously) [akka](https://akka.io/).

## How to use.

Import the library in your `build.sbt`:

```
libraryDependencies += "io.scalac" %% "akka-periscope-core" % "0.4.0"
```

If you want additional integrations, consider adding one/several of the following:

```
libraryDependencies += "io.scalac" %% "akka-periscope-akka-http" % "0.4.0"
```

### Getting raw data

If you want to just get the data to use it in some way, there are following two function available in `akka-periscope-core`.

#### Actor tree

Here's an example of how to build an actor tree for some particular actor system:
```scala
import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.{Future, ExecutionContext}

import io.scalac.periscope.akka.tree.build
import io.scalac.periscope.akka.tree.ActorTree

val system: ActorSystem = ???
implicit val timeout: Timeout = ??? // time limit for the tree to be assembled
implicit val ec: ExecutionContext = system.scheduler // can use another executor if you prefer to
 
val tree: Future[ActorTree] = build(system)
```

⚠️ Please note, that if some actor is overwhelmed and does not make it in time to answer the ping message, it will not be present in the tree.
We have to use this "best-effort" approach, because there's no way to get full list of actor's children from the outside, so we have to discover it first by sending a broadcast message.

#### Actor count

Here's an example of a more simple, but less memory intensive metric, which is actor count:
```scala
import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.{Future, ExecutionContext}

import io.scalac.periscope.akka.counter.count

val system: ActorSystem = ???
implicit val timeout: Timeout = ??? // time limit for the actor hierarchy to be processed
implicit val ec: ExecutionContext = system.scheduler // can use another executor if you prefer to
 
val actorCount: Future[Long] = count(system)
```

⚠️ See the same note about time limits and possibility of not counting in actors that have to much message backlog to answer pings in time.

### Setting up Panopticon endpoints

If your primary goal is to make [Panopticon](https://github.com/ScalaConsultants/panopticon-tui) work with your app, there are some convenient shortcuts.

#### Akka-http endpoints

If you use akka-http, then you can add `akka-periscope-akka-http` to your build. After that you can create akka-http `Route`s for both actor tree and actor system status (including actor count) using these smart-constructors:

```scala
import io.scalac.periscope.akka.http.ActorTreeRoute
import io.scalac.periscope.akka.http.ActorSystemStatusRoute
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import scala.concurrent.ExecutionContext

val system: ActorSystem = ???
implicit val ec: ExecutionContext = system.dispatcher

pathPrefix("actor-tree") {
  ActorTreeRoute(system)
} ~
pathPrefix("actor-system-status") {
  ActorSystemStatusRoute(system)
}
```

For these endpoints the timeout (in milliseconds) is passed as a query parameter, like this:
```bash
curl --request GET \
  --url 'http://localhost:8080/actor-tree?timeout=2000'
```

Response will be in the format, ready to be consumed by [Panopticon](https://github.com/ScalaConsultants/panopticon-tui):
```json
{
  "system": {
    "log1-Slf4jLogger": {},
    "IO-TCP": {
      "selectors": {
        "$a": {
          "0": {},
          "1": {}
        }
      }
    },
    "Materializers": {
      "StreamSupervisor-0": {
        "flow-1-0-detacher": {},
        "flow-0-0-ignoreSink": {}
      },
      "StreamSupervisor-1": {}
    },
    "eventStreamUnsubscriber-1": {},
    "localReceptionist": {},
    "pool-master": {},
    "deadLetterListener": {}
  },
  "user": {
    "actor-tree-builder-1589444625750": {}
  }
}
```

Developed by [Scalac](https://scalac.io/?utm_source=scalac_github&utm_campaign=scalac1&utm_medium=web)
