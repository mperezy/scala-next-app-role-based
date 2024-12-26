name := """scala-next-app-role-based"""
organization := "com.mperezy.scala-next-app-role-based"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.15"

val auth0Dependencies = Seq(
//  "com.paulownia" %% "jwt-play" % "0.19.0",
  "com.github.jwt-scala" %% "jwt-play-json" % "10.0.1",
//  "com.paulownia" %% "jwt-core" % "0.19.0",
  "com.github.jwt-scala" %% "jwt-core" % "10.0.1",
  "com.auth0" % "jwks-rsa" % "0.6.1"
)

libraryDependencies ++= Seq(guice, ws, "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
  "org.scalameta" %% "scalafmt-dynamic" % "3.8.3")

libraryDependencies ++= auth0Dependencies

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.mperezy.scala-next-app-role-based.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.mperezy.scala-next-app-role-based.binders._"
