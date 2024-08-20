import Dependencies._

ThisBuild / scalaVersion := "2.13.14"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.kaiko"
ThisBuild / organizationName := "Kaiko"

mainClass in (Compile, run) := Some("endpoints.Main")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

val testDependencies = Seq(
  scalaTest,
  grpcTesting,
  scalaMock
)

val libDependencies = Seq(
  scalapbjson4s,
  netty,
  grpcio,
  sdk,
  retry
)

lazy val root = (project in file("."))
  .settings(
    name := "test",
    libraryDependencies ++= Seq(testDependencies, libDependencies).flatten
  )

resolvers += "Kaiko" at "https://s3.us-east-2.wasabisys.com/kaiko-sdk/scala-sdk/releases"
