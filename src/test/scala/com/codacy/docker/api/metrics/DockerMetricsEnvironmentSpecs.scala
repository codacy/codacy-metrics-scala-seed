package com.codacy.docker.api.metrics

import better.files.File
import com.codacy.plugins.api.Source
import com.codacy.plugins.api.languages.Languages.Scala
import com.codacy.plugins.api.metrics.MetricsTool
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class DockerMetricsEnvironmentSpecs extends Specification {

  "DockerMetricsEnvironment" should {

    val dockerMetricsEnvironment = new DockerMetricsEnvironment(Map.empty)

    "get the metrics configuration for the tool, given a valid json file" in {
      //given
      (for {
        tempFile <- File.temporaryFile()
      } yield {
        val metricsConfiguration =
          MetricsTool.CodacyConfiguration(Some(Set(Source.File(s"${tempFile.parent.pathAsString}/a.scala"))),
                                          Some(Scala),
                                          Some(Map.empty))
        tempFile.write(Json.stringify(Json.toJson(metricsConfiguration)))

        //when
        val metricsConfig =
          dockerMetricsEnvironment.configurations(rootFile = tempFile.parent, configFile = tempFile)

        //then
        // scalafix:off NoInfer.any
        metricsConfig must beSuccessfulTry[Option[MetricsTool.CodacyConfiguration]](Some(metricsConfiguration))
        // scalafix:on NoInfer.any
      }).get()
    }

    "fail getting the configuration, if the json is not valid" in {
      //given
      (for {
        tempFile <- File.temporaryFile()
      } yield {
        tempFile.write("{{invalid json}")

        //when
        val metricsConfig =
          dockerMetricsEnvironment.configurations(rootFile = tempFile.parent, configFile = tempFile)

        //then
        metricsConfig must beFailedTry
      }).get()
    }

    "not return any configuration if the configuration file doesn't exist" in {
      //given
      val nonExistentFile = File("notExistentFile.xpto")
      val srcFolder = File.currentWorkingDirectory

      //when
      val metricsConfig =
        dockerMetricsEnvironment.configurations(rootFile = srcFolder, configFile = nonExistentFile)

      //then
      // scalafix:off NoInfer.any
      metricsConfig must beSuccessfulTry[Option[MetricsTool.CodacyConfiguration]](
        Option.empty[MetricsTool.CodacyConfiguration])
      // scalafix:on NoInfer.any
    }
  }
}
