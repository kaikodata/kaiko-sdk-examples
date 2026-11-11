import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.20" % Test
  lazy val grpcTesting = "io.grpc" % "grpc-testing" % "1.85.1" % Test
  lazy val grpcInprocess = "io.grpc" % "grpc-inprocess" % "1.85.1" % Test

  lazy val grpcio = "com.google.protobuf" % "protobuf-java" % "3.25.9"
  lazy val netty = "io.grpc" % "grpc-netty" % "1.85.1"
  lazy val sdk = "com.kaiko" %% "scala-sdk" % "1.41.0"
  lazy val scalapbjson4s = "com.thesamet.scalapb" %% "scalapb-json4s" % "0.12.3"
  lazy val retry =
    "com.evanlennick" % "retry4j" % "0.15.0" // optional dependency only to demonstrate resubscribe
}
