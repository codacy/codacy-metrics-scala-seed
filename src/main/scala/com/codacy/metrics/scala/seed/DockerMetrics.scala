package com.codacy.metrics.scala.seed

import com.codacy.metrics.scala.seed.traits.{Haltable, Timeoutable}
import com.codacy.plugins.api.Source
import com.codacy.plugins.api.docker.v2.MetricsResult
import com.codacy.plugins.api.metrics.MetricsTool

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
abstract class DockerMetrics(metricsTool: MetricsTool[MetricsResult],
                             environment: DockerMetricsEnvironment = new DockerMetricsEnvironment(),
                             resultsPrinter: MetricsResultsPrinter = new MetricsResultsPrinter())
    extends Timeoutable
    with Haltable {

  def main(args: Array[String]): Unit = {
    initTimeout
    lazy val sourcePathStr = environment.sourcePath.toString
    (for {
      config <- environment.getConfiguration()
      res <- withNativeTry[List[MetricsResult]](
        metricsTool.apply(Source.Directory(sourcePathStr),
                          config.language,
                          config.files,
                          config.options.getOrElse(Map.empty)))
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
    timeout(environment.timeout) {
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
