package com.codacy.metrics.scala.seed

import java.io.PrintStream

import com.codacy.plugins.api.Implicits._
import com.codacy.metrics.scala.seed.utils.FileHelper
import com.codacy.plugins.api.docker.v2.MetricsResult
import play.api.libs.json.Json

class MetricsResultsPrinter(resultsStream: PrintStream = Console.out, logStream: PrintStream = Console.err) {

  def log(message: String): Unit = {
    logStream.println(message)
  }

  def logStackTrace(error: Throwable): Unit = {
    error.printStackTrace(logStream)
  }

  def logStackTrace(stackTrace: String): Unit = {
    logStream.println(stackTrace)
  }

  def printResult(result : MetricsResult): Unit = {
    resultsStream.println(stringifyMetricsResultJson(result))
  }

  def printResults(results: List[MetricsResult], sourcePath: String): Unit = {
    val relativizedResults = results.map {
      case fileMetrics: MetricsResult.FileMetrics =>
        stripSourcePath(fileMetrics, sourcePath)
      case problem => problem
    }

    relativizedResults.foreach(printResult)
  }

  private def stringifyMetricsResultJson(metrics: MetricsResult): String = {
    Json.stringify(Json.toJson(metrics))
  }

  private def stripSourcePath(fileMetrics: MetricsResult.FileMetrics, sourcePath: String): MetricsResult.FileMetrics = {
    fileMetrics.copy(filename = FileHelper.stripPath(fileMetrics.filename, sourcePath))
  }
}
