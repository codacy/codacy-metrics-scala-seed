package com.codacy.docker.api.metrics

import codacy.docker.api.metrics.{FileMetrics, MetricsTool}
import codacy.docker.api.{MetricsConfiguration, Source}
import com.codacy.api.dtos.Language
import com.codacy.docker.api.metrics.DockerMetricsEnvironment._
import com.codacy.docker.api.utils.Delayed

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
abstract class DockerMetrics(metricsTool: MetricsTool) extends Delayed {

  private lazy val timeout: FiniteDuration =
    Option(System.getProperty("timeout"))
      .flatMap(rawDuration =>
        Try(Duration(rawDuration)).toOption.collect {
          case d: FiniteDuration => d
      })
      .getOrElse(10.minutes)

  private lazy val isDebug: Boolean = Option(System.getProperty("debug"))
    .flatMap(rawDebug => Try(rawDebug.toBoolean).toOption)
    .getOrElse(false)

  private lazy val resultsPrinter = new MetricsResultsPrinter(Console.out)

  def main(args: Array[String]): Unit = {
    initTimeout(timeout)
    (for {
      config <- getConfiguration(configFilePath, sourcePath)
      res <- withNativeTry[List[FileMetrics]](
        Source.Directory(sourcePath.pathAsString),
        config.flatMap(_.language),
        config.flatMap(_.files),
        config.flatMap(_.options).getOrElse(Map.empty))(
        metricsTool.apply
      )
    } yield res) match {
      case Success(results) =>
        log("receiving metrics results")
        resultsPrinter.printResults(results)
        log("metrics runner finished")
        Runtime.getRuntime.halt(0)
      case Failure(error) =>
        error.printStackTrace(Console.err)
        Runtime.getRuntime.halt(1)
    }
  }

  private def initTimeout(duration: FiniteDuration): Future[Unit] = {
    delay(duration) {
      Runtime.getRuntime.halt(2)
    }
  }

  private def log(message: String): Unit = if (isDebug) {
    Console.err.println(s"[DockerMetrics] $message")
  }

  private def withNativeTry[T](
      source: Source.Directory,
      language: Option[Language],
      files: Option[Set[Source.File]],
      options: Map[MetricsConfiguration.Key, MetricsConfiguration.Value])(
      block: (
          Source.Directory,
          Option[Language],
          Option[Set[Source.File]],
          Map[MetricsConfiguration.Key, MetricsConfiguration.Value]) => Try[T])
    : Try[T] = {
    try {
      block(source, language, files, options)
    } catch {
      case t: Throwable =>
        Failure[T](t)
    }
  }

}
