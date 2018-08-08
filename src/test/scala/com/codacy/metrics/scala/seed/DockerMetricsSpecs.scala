package com.codacy.metrics.scala.seed

import java.io.{ByteArrayOutputStream, PrintStream}

import com.codacy.metrics.scala.seed.utils.FileHelper
import com.codacy.plugins.api.docker.v2.MetricsResult
import com.codacy.plugins.api.Implicits._
import com.codacy.plugins.api.{Options, Source}
import com.codacy.plugins.api.languages.Language
import com.codacy.plugins.api.metrics.MetricsTool
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
      val sourcePath = dockerMetricsEnvironment.sourcePath
      val fileMetrics = MetricsResult.FileMetrics(fileName)
      val metricsTool = new MetricsTool[MetricsResult] {
        override def apply(
          source: Source.Directory,
          language: Option[Language],
          files: Option[Set[Source.File]],
          options: Map[Options.Key, Options.Value]): Try[List[MetricsResult]] = {
          Success(List(fileMetrics))
        }
      }
      val dockerMetrics =
        new DockerMetrics(metricsTool = metricsTool,
                          resultsPrinter = new MetricsResultsPrinter(resultsStream = printStream)) {
          override def halt(status: Int): Unit = {
            status must beEqualTo(0)
            ()
          }
        }

      //when
      dockerMetrics.main(Array.empty)

      //then
      Json.parse(outContent.toString) must beEqualTo(
        Json.toJson(fileMetrics.copy(filename = FileHelper.stripPath(fileName, sourcePath.toString))))
    }

    "fail if the apply method fails, print the stacktrace to the given stream and exit with the code 1" in {
      //given
      val outContent = new ByteArrayOutputStream()
      val printStream = new PrintStream(outContent)
      val failedMsg = s"Failed: ${Random.nextInt()}"
      val metricsTool = new MetricsTool[MetricsResult] {
        override def apply(
          source: Source.Directory,
          language: Option[Language],
          files: Option[Set[Source.File]],
          options: Map[Options.Key, Options.Value]): Try[List[MetricsResult]] = {
          Failure(new Throwable(failedMsg))
        }
      }
      val dockerMetrics =
        new DockerMetrics(metricsTool = metricsTool,
                          resultsPrinter = new MetricsResultsPrinter(logStream = printStream)) {
          override def halt(status: Int): Unit = {
            status must beEqualTo(1)
            ()
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
      val metricsTool = new MetricsTool[MetricsResult] {
        override def apply(
          source: Source.Directory,
          language: Option[Language],
          files: Option[Set[Source.File]],
          options: Map[Options.Key, Options.Value]): Try[List[MetricsResult]] = {
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
