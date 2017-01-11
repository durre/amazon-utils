name := """amazon-utils"""
organization := "com.github.durre"
version := "1.0.1"


scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.41",
  "org.specs2" %% "specs2-core" % "2.4.15" % "test",
  "org.specs2" %% "specs2-mock" % "2.4.15" % "test"
)
