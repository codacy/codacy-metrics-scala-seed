package com.codacy.docker.api.metrics

import java.nio.file.Path

import com.codacy.docker.api.utils.{Delayed, Halted}
import com.codacy.plugins.api.Source
import com.codacy.plugins.api.metrics.{FileMetrics, MetricsTool}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

abstract class DockerMetrics(metricsTool: MetricsTool,
                             environment: DockerMetricsEnvironment = new DockerMetricsEnvironment())(
  rootFile: Path = environment.defaultRootFile,
  configFile: Path = environment.defaultConfigFile,
  timeout: FiniteDuration = environment.defaultTimeout,
  printer: MetricsResultsPrinter = new MetricsResultsPrinter())
    extends Delayed
    with Halted {

  def main(args: Array[String]): Unit = {
    initTimeout
    (for {
      config <- environment.configurations(rootFile = rootFile, configFile = configFile)
      res <- withNativeTry[List[FileMetrics]](
        metricsTool.apply(Source.Directory(rootFile.toString),
                          config.flatMap(_.language),
                          config.flatMap(_.files),
                          config.flatMap(_.options).getOrElse(Map.empty)))
    } yield res) match {
      case Success(results) =>
        log("receiving metrics results")
        printer.printResults(results, rootFile.toString)
        log("metrics tool finished")
        halt(0)
      case Failure(error) =>
        printer.logStackTrace(error)
        halt(1)
    }
  }

  private def initTimeout: Future[Unit] = {
    delay(timeout) {
      log(s"timed out after $timeout ")
      halt(2)
    }
  }

  private def log(message: String): Unit = if (environment.debug) {
    printer.log(s"[DockerMetrics] $message")
  }

  @SuppressWarnings(Array("CatchThrowable"))
  private def withNativeTry[T](block: => Try[T]): Try[T] = {
    try {
      block
    } catch {
      case t: Throwable =>
        Failure[T](t)
    }
  }

}
