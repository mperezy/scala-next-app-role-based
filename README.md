# scala-next-app-role-based

## Quick description

- This app intends to replicate the same behavior from repo [next-app-role-based](https://github.com/mperezy/next-app-role-based) build in Nextjs with Typescript but focused on the usage of Scala as main language and Play Framework for the backend.

## Requirements

- Docker and Docker Compose

## Init Play Framework app with Scala using docker

```shell
docker run --rm --interactive --tty --volume $PWD:/root sbtscala/scala-sbt:graalvm-community-22.0.1_1.10.6_3.5.2 sbt "new playframework/play-scala-seed.g8"
copying runtime jar...
[info] [launcher] getting org.scala-sbt sbt 1.10.6  (this may take some time)...
[info] [launcher] getting Scala 2.12.20 (for sbt)...
[info] Updated file /root/project/build.properties: set sbt.version to 1.10.6
[info] welcome to sbt 1.10.6 (GraalVM Community Java 22.0.1)
...
[info] set current project to root (in build file:/root/)
. # <- use . as root project
[info] downloading https://repo1.maven.org/maven2/org/scala-sbt/sbt-giter8-resolver/sbt-giter8-resolver_2.12/0.16.2/sbt-giter8-resolver_2.12-0.16.2.jar ...
...
This template generates a Play Scala project

name [play-scala-seed]: organization [com.example]:  # Enter your organization
play_version [3.0.6]: # Enter key
scala_version [2.13.15]: # Enter key
sbt_giter8_scaffold_version [0.16.2]: # Enter key
Skipping existing file: /root/././project/build.properties

Template applied in /root/./.
```

## How to run

```shell
docker compose up
```

- Server will be running at http://localhost:9000