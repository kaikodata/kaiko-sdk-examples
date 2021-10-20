import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.3" % Test
  lazy val grpcTesting =  "io.grpc" % "grpc-testing" % "1.35.0" % Test
  lazy val scalaMock = "org.scalamock" %% "scalamock" % "4.4.0" % Test

  lazy val grpcio = "com.google.protobuf" % "protobuf-java" % "3.14.0"
  lazy val netty = "io.grpc" % "grpc-netty" % "1.35.0"
  lazy val sdk = "com.kaiko" %% "scala-sdk" % "1.0.3"
}
