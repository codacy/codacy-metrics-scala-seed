import sbt.Keys._
import sbt._

val scalaBinaryVersionNumber = "2.12"
val scalaVersionNumber = s"$scalaBinaryVersionNumber.4"

lazy val codacyAnalysisCli = project
  .in(file("."))
  .settings(
    inThisBuild(
      List(
        organization := "com.codacy",
        scalaVersion := scalaVersionNumber,
        version := "0.1.0-SNAPSHOT",
        scalacOptions ++= Common.compilerFlags,
        scalacOptions in Test ++= Seq("-Yrangepos"),
        scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")
      )),
    name := "codacy-metrics-scala-seed",
    // App Dependencies
    libraryDependencies ++= Seq(
      Dependencies.playJson,
      Dependencies.codacyPluginsApi,
      Dependencies.betterFiles
    ),
    // Test Dependencies
    libraryDependencies ++= Dependencies.specs2.map(_ % Test)
  )
  .settings(Common.genericSettings: _*)

// Scapegoat
scalaVersion in ThisBuild := scalaVersionNumber
scalaBinaryVersion in ThisBuild := scalaBinaryVersionNumber
scapegoatDisabledInspections in ThisBuild := Seq()
scapegoatVersion in ThisBuild := "1.3.5"

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

licenses := Seq("The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

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
