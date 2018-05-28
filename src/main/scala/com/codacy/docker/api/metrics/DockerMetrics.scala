package com.codacy.docker.api.metrics

import codacy.docker.api.Source
import codacy.docker.api.metrics.{FileMetrics, MetricsTool}
import com.codacy.docker.api.utils.{Delayed, Halted}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
abstract class DockerMetrics(metricsTool: MetricsTool,
                             environment: DockerMetricsEnvironment = new DockerMetricsEnvironment(),
                             resultsPrinter: MetricsResultsPrinter = new MetricsResultsPrinter())
    extends Delayed
    with Halted {

  def main(args: Array[String]): Unit = {
    initTimeout
    lazy val sourcePathStr = environment.sourcePath.pathAsString
    (for {
      config <- environment.getConfiguration()
      res <- withNativeTry[List[FileMetrics]](
        metricsTool.apply(Source.Directory(sourcePathStr),
                          config.flatMap(_.language),
                          config.flatMap(_.files),
                          config.flatMap(_.options).getOrElse(Map.empty)))
    } yield res) match {
      case Success(results) =>
        log("receiving metrics results")
        resultsPrinter.printResults(results, sourcePathStr)
        log("metrics tool finished")
        halt(0)
      case Failure(error) =>
        resultsPrinter.logStackTrace(error)
        halt(1)
    }
  }

  private def initTimeout: Future[Unit] = {
    delay(environment.timeout) {
      log(s"timed out after ${environment.timeout} ")
      halt(2)
    }
  }

  private def log(message: String): Unit = if (environment.isDebug) {
    resultsPrinter.log(s"[DockerMetrics] $message")
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
