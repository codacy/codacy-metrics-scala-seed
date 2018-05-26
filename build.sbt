import sbt._
import sbt.Keys._

val scalaBinaryVersionNumber = "2.12"
val scalaVersionNumber = s"$scalaBinaryVersionNumber.4"

lazy val codacyMetricsScalaSeed = project
  .in(file("."))
  .settings(
    inThisBuild(
      List(organization := "com.codacy",
           scalaVersion := scalaVersionNumber,
           version := "0.1.0-SNAPSHOT",
           scalacOptions ++= Common.compilerFlags,
           scalacOptions in Test ++= Seq("-Yrangepos"),
           scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings"))),
    name := "codacy-metrics-scala-seed",
    // App Dependencies
    libraryDependencies ++= Seq(Dependencies.playJson, Dependencies.codacyPluginsApi, Dependencies.betterFiles),
    // Test Dependencies
    libraryDependencies ++= Dependencies.specs2.map(_ % Test))
  .settings(Common.genericSettings: _*)

// Scapegoat
scalaVersion in ThisBuild := scalaVersionNumber
scalaBinaryVersion in ThisBuild := scalaBinaryVersionNumber
scapegoatDisabledInspections in ThisBuild := Seq()
scapegoatVersion in ThisBuild := "1.3.5"

// Bintray JCenter
bintrayOrganization := Some("Codacy")
bintrayRepository := "maven"
licenses += ("AGPL-3.0", url("https://choosealicense.com/licenses/agpl-3.0/"))
bintrayPackageLabels := Seq("scala", "codacy", "framework", "metrics", "compexity", "static", "analysis")
bintrayPackageAttributes ~=
  ((_: Map[String, Iterable[bintry.Attr[_]]]) ++ Map("maturity" -> Seq(bintry.Attr.String("Development"))))
