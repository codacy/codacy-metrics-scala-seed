import sbt.Keys._
import sbt._
name := """codacy-metrics-scala-seed"""

organization := "com.codacy"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.12.6"

scalacOptions := Seq("-deprecation",
                     "-feature",
                     "-unchecked",
                     "-Xlint",
                     "-Ywarn-adapted-args")
resolvers += "Bintray Typesafe Repo" at "http://dl.bintray.com/typesafe/maven-releases/"


libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.9",
  "com.codacy" %% "codacy-plugins-api" % "1.0.0-SNAPSHOT" withSources (),
  "com.github.pathikrit" %% "better-files" % "3.4.0",
  "org.specs2" %% "specs2-core" % "4.2.0" % "test")

organizationName := "Codacy"

organizationHomepage := Some(new URL("https://www.codacy.com"))

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ =>
  false
}

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

startYear := Some(2018)

description := "Library to develop Codacy metrics plugins"

licenses := Seq(
  "The Apache Software License, Version 2.0" -> url(
    "http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("http://www.github.com/codacy/codacy-metrics-scala-seed/"))

pomExtra :=
  <scm>
    <url>http://www.github.com/codacy/codacy-metrics-scala-seed</url>
    <connection>scm:git:git@github.com:codacy/codacy-metrics-scala-seed.git</connection>
    <developerConnection>scm:git:https://github.com/codacy/codacy-metrics-scala-seed.git</developerConnection>
  </scm>
    <developers>
      <developer>
        <id>rtfpessoa</id>
        <name>Rodrigo Fernandes</name>
        <email>rodrigo [at] codacy.com</email>
        <url>https://github.com/rtfpessoa</url>
      </developer>
      <developer>
        <id>bmbferreira</id>
        <name>Bruno Ferreira</name>
        <email>bruno.ferreira [at] codacy.com</email>
        <url>https://github.com/bmbferreira</url>
      </developer>
      <developer>
        <id>xplosunn</id>
        <name>Hugo Sousa</name>
        <email>hugo [at] codacy.com</email>
        <url>https://github.com/xplosunn</url>
      </developer>
      <developer>
        <id>pedrocodacy</id>
        <name>Pedro Amaral</name>
        <email>pamaral [at] codacy.com</email>
        <url>https://github.com/pedrocodacy</url>
      </developer>
    </developers>
