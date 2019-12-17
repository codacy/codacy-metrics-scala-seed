val scala211 = "2.11.12"
val scala212 = "2.12.10"
val scala213 = "2.13.1"

val specs2Version = "4.7.1"

organization := "com.codacy"
scalaVersion := "2.12.10"
crossScalaVersions := Seq(scala211, scala212, scala213)
name := "codacy-metrics-scala-seed"
libraryDependencies ++= Seq(("com.typesafe.play" %% "play-json" % "2.7.4").withSources(),
                            ("com.codacy" %% "codacy-plugins-api" % "3.1.0").withSources(),
                            ("com.github.pathikrit" %% "better-files" % "3.8.0").withSources(),
                            "org.specs2" %% "specs2-core" % specs2Version % Test,
                            "org.specs2" %% "specs2-mock" % specs2Version % Test)
scalacOptions := Seq()

ThisBuild / scapegoatVersion := "1.3.10"

// HACK: This setting is not picked up properly from the plugin
pgpPassphrase := Option(System.getenv("SONATYPE_GPG_PASSPHRASE")).map(_.toCharArray)

description := "Library to develop Codacy metrics plugins"

scmInfo := Some(
  ScmInfo(url("https://github.com/codacy/codacy-metrics-scala-seed"),
          "scm:git:git@github.com:codacy/codacy-metrics-scala-seed.git"))

publicMvnPublish
