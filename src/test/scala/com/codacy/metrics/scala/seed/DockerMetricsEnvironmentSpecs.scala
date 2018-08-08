package com.codacy.metrics.scala.seed

import better.files.File
import com.codacy.plugins.api.Source
import com.codacy.plugins.api.languages.Languages.Scala
import com.codacy.plugins.api.metrics.MetricsTool
import org.specs2.mutable.Specification


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
        val metricsConfigurationJsonStr = """{"files":["/var/folders/1c/h1rc38852t1fpkh8c7gc5fdc0000gn/T/a.scala"],"language":"Scala","options":{}}"""
        tempFile.write(metricsConfigurationJsonStr)

        //when
        val metricsConfig =
          dockerMetricsEnvironment.getConfiguration(tempFile.path, tempFile.parent.path)

        //then
        // scalafix:off NoInfer.any
        metricsConfig must beSuccessfulTry[MetricsTool.CodacyConfiguration](metricsConfiguration)
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
          dockerMetricsEnvironment.getConfiguration(tempFile.path, tempFile.parent.path)

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
        dockerMetricsEnvironment.getConfiguration(nonExistentFile.path, srcFolder.path)

      //then
      // scalafix:off NoInfer.any
      metricsConfig must beSuccessfulTry[MetricsTool.CodacyConfiguration](MetricsTool.CodacyConfiguration(None, None, None))
      // scalafix:on NoInfer.any
    }
  }
}
