val scala213 = "2.13.11"

val specs2Version = "4.7.1"

organization := "com.codacy"
scalaVersion := scala213
crossScalaVersions := Seq(scala213)
name := "codacy-metrics-scala-seed"
libraryDependencies ++= Seq("com.typesafe.play" %% "play-json" % "2.7.4",
                            "com.codacy" %% "codacy-plugins-api" % "7.2.1",
                            "com.github.pathikrit" %% "better-files" % "3.8.0",
                            "org.specs2" %% "specs2-core" % specs2Version % Test,
                            "org.specs2" %% "specs2-mock" % specs2Version % Test)

// HACK: This setting is not picked up properly from the plugin
pgpPassphrase := Option(System.getenv("SONATYPE_GPG_PASSPHRASE")).map(_.toCharArray)

description := "Library to develop Codacy metrics plugins"

scmInfo := Some(
  ScmInfo(url("https://github.com/codacy/codacy-metrics-scala-seed"),
          "scm:git:git@github.com:codacy/codacy-metrics-scala-seed.git"))

publicMvnPublish
