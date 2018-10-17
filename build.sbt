import sbtrelease.Version

scalaVersion := "2.12.6"

resolvers += Resolver.sonatypeRepo("public")
releaseNextVersion := { ver => Version(ver).map(_.bumpMinor.string).getOrElse("Error") }

name := "zip-streaming"
organization := "com.sipoju"
version := "1.0"


libraryDependencies ++= Seq(
  "com.typesafe.akka"           %% "akka-http"                  % "10.1.1",
  "com.typesafe.akka"           %% "akka-stream"                % "2.5.12",
  "com.typesafe.akka"           %% "akka-actor"                 % "2.5.12",
  "com.typesafe.scala-logging"  %% "scala-logging"              % "3.7.2",
  "ch.qos.logback"              %  "logback-classic"            % "1.2.3",
  "com.amazonaws"               %  "aws-java-sdk-s3"            % "1.11.317",
  "com.amazonaws"               %  "aws-lambda-java-events"     % "2.2.1",
  "com.amazonaws"               %  "aws-lambda-java-core"       % "1.2.0",
  "commons-io"                  %  "commons-io"                 % "2.6",
  "ch.megard"                   %% "akka-http-cors"             % "0.3.0"
)



mainClass in assembly := Some("com.sipoju.ZipStreamingMain")
assemblyJarName in assembly := "zip-streaming.jar"
test in assembly := {}

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings")