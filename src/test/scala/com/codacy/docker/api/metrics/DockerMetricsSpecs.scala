package com.codacy.docker.api.metrics

import java.io.{ByteArrayOutputStream, PrintStream}

import codacy.docker.api.metrics.{FileMetrics, MetricsTool}
import codacy.docker.api.{MetricsConfiguration, Source}
import com.codacy.api.dtos.Language
import com.codacy.docker.api.utils.FileHelper
import org.specs2.mutable.Specification
import play.api.libs.json.Json

import scala.concurrent.TimeoutException
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}

class DockerMetricsSpecs extends Specification {

  "DockerMetrics" should {
    "print the file metrics results to the given stream and exit with the code 0" in {
      //given
      val outContent = new ByteArrayOutputStream()
      val printStream = new PrintStream(outContent)
      val dockerMetricsEnvironment = new DockerMetricsEnvironment(Map.empty)
      val fileName = "a.scala"
      val sourcePath = dockerMetricsEnvironment.sourcePath.pathAsString
      val fileMetrics = FileMetrics(fileName)
      val metricsTool = new MetricsTool {
        override def apply(
          source: Source.Directory,
          language: Option[Language],
          files: Option[Set[Source.File]],
          options: Map[MetricsConfiguration.Key, MetricsConfiguration.Value]): Try[List[FileMetrics]] = {
          Success(List(fileMetrics))
        }
      }
      val dockerMetrics =
        new DockerMetrics(metricsTool = metricsTool,
                          resultsPrinter = new MetricsResultsPrinter(resultsStream = printStream)) {
          override def halt(status: Int): Unit = {
            status must beEqualTo(0)
          }
        }

      //when
      dockerMetrics.main(Array.empty)

      //then
      Json.parse(outContent.toString) mustEqual Json.toJson(
        fileMetrics.copy(filename = FileHelper.stripPath(fileName, sourcePath)))
    }

    "fail if the apply method fails, print the stacktrace to the given stream and exit with the code 1" in {
      //given
      val outContent = new ByteArrayOutputStream()
      val printStream = new PrintStream(outContent)
      val failedMsg = s"Failed: ${Random.nextInt()}"
      val metricsTool = new MetricsTool {
        override def apply(
          source: Source.Directory,
          language: Option[Language],
          files: Option[Set[Source.File]],
          options: Map[MetricsConfiguration.Key, MetricsConfiguration.Value]): Try[List[FileMetrics]] = {
          Failure(new Throwable(failedMsg))
        }
      }
      val dockerMetrics =
        new DockerMetrics(metricsTool = metricsTool,
                          resultsPrinter = new MetricsResultsPrinter(logStream = printStream)) {
          override def halt(status: Int): Unit = {
            status must beEqualTo(1)
          }
        }

      //when
      dockerMetrics.main(Array.empty)

      //then
      outContent.toString must contain(failedMsg)
    }

    "fail if the configured timeout on the system environment is too low" in {
      //given
      val outContent = new ByteArrayOutputStream()
      val printStream = new PrintStream(outContent)
      val timeOutValue = "2 seconds"
      val timeOutException = new TimeoutException(s"Metrics tool timed out after: $timeOutValue")
      val metricsTool = new MetricsTool {
        override def apply(
          source: Source.Directory,
          language: Option[Language],
          files: Option[Set[Source.File]],
          options: Map[MetricsConfiguration.Key, MetricsConfiguration.Value]): Try[List[FileMetrics]] = {
          Thread.sleep(3.seconds.toMillis)
          Success(List.empty)
        }
      }
      val dockerMetrics =
        new DockerMetrics(metricsTool = metricsTool,
                          resultsPrinter = new MetricsResultsPrinter(logStream = printStream),
                          environment = new DockerMetricsEnvironment(Map("METRICS_TIMEOUT" -> timeOutValue))) {
          override def halt(status: Int): Unit = {
            throw timeOutException
          }
        }

      //when and then
      dockerMetrics.main(Array.empty) must throwA(timeOutException)
    }
  }
}
