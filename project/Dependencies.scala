import sbt._

object Dependencies {

  val playJson = "com.typesafe.play" %% "play-json" % "2.6.9"
  val codacyPluginsApi = "com.codacy" %% "codacy-plugins-api" % "2.1.3" withSources ()
  val betterFiles = "com.github.pathikrit" %% "better-files" % "3.4.0"

  val specs2Version = "4.2.0"
  lazy val specs2 = Seq("org.specs2" %% "specs2-core" % specs2Version)

}
