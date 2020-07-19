name := "AkkaCourse"

version := "0.1"
val akkaVersion = "2.6.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.2.0"
)



scalaVersion := "2.13.3"
