package com.codacy.docker.api.metrics

import better.files.File
import codacy.docker.api.{MetricsConfiguration, Source}
import com.codacy.api.dtos.Languages.Scala
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
          MetricsConfiguration(Some(Set(Source.File(s"${tempFile.parent.pathAsString}/a.scala"))),
                               Some(Scala),
                               Some(Map.empty))
        tempFile.write(Json.stringify(Json.toJson(metricsConfiguration)))

        //when
        val metricsConfig =
          dockerMetricsEnvironment.getConfiguration(tempFile, tempFile.parent)

        //then
        metricsConfig must beSuccessfulTry[Option[MetricsConfiguration]](Some(metricsConfiguration))
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
          dockerMetricsEnvironment.getConfiguration(tempFile, tempFile.parent)

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
        dockerMetricsEnvironment.getConfiguration(nonExistentFile, srcFolder)

      //then
      metricsConfig must beSuccessfulTry[Option[MetricsConfiguration]](Option.empty)
    }
  }
}
