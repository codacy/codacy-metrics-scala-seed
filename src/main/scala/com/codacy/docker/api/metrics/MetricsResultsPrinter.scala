package com.codacy.docker.api.metrics

import java.io.PrintStream

import codacy.docker.api.metrics.FileMetrics
import com.codacy.docker.api.utils.FileHelper
import play.api.libs.json.Json

class MetricsResultsPrinter(resultsStream: PrintStream = Console.out, logStream: PrintStream = Console.err) {

  def log(message: String): Unit = {
    logStream.println(message)
  }

  def logStackTrace(error: Throwable): Unit = {
    error.printStackTrace(logStream)
  }

  def printResults(results: List[FileMetrics], sourcePath: String): Unit = {
    results.foreach { fileMetrics =>
      val fileMetricsJsonStr = (stripSourcePath _).tupled.andThen(stringifyFileMetricsJson)((fileMetrics, sourcePath))
      resultsStream.println(fileMetricsJsonStr)
    }
  }

  private def stringifyFileMetricsJson(metrics: FileMetrics): String = {
    Json.stringify(Json.toJson(metrics))
  }

  private def stripSourcePath(fileMetrics: FileMetrics, sourcePath: String): FileMetrics = {
    fileMetrics.copy(filename = FileHelper.stripPath(fileMetrics.filename, sourcePath))
  }
}
