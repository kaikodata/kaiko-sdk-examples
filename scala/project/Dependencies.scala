import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.3" % Test
  lazy val grpcTesting =  "io.grpc" % "grpc-testing" % "1.59.0" % Test
  lazy val scalaMock = "org.scalamock" %% "scalamock" % "4.4.0" % Test

  lazy val grpcio = "com.google.protobuf" % "protobuf-java" % "3.21.12"
  lazy val netty = "io.grpc" % "grpc-netty" % "1.59.0"
  lazy val sdk = "com.kaiko" %% "scala-sdk" % "1.19.0"
  lazy val retry = "com.evanlennick" % "retry4j" % "0.15.0" // optional dependency only to demonstrate resubscribe
}
