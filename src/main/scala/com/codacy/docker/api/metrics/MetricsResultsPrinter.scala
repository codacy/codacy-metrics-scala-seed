package com.codacy.docker.api.metrics

import java.io.PrintStream

import codacy.docker.api.metrics.FileMetrics
import com.codacy.docker.api.metrics.DockerMetricsEnvironment.sourcePath
import com.codacy.docker.api.utils.FileHelper
import play.api.libs.json.Json

class MetricsResultsPrinter(printStream: PrintStream) {

  def printResults(results: List[FileMetrics]): Unit = {
    results.foreach { fileMetrics =>
      val fileMetricsJsonStr = stringifyFileMetricsJson(stripFileMetricsFileNameSourcePath(fileMetrics))
      printStream.println(fileMetricsJsonStr)
    }
  }

  private def stringifyFileMetricsJson(metrics: FileMetrics) = {
    Json.stringify(Json.toJson(metrics))
  }

  private def stripFileMetricsFileNameSourcePath(fileMetrics: FileMetrics) = {
    fileMetrics.copy(filename = FileHelper.stripPath(fileMetrics.filename, sourcePath.pathAsString))
  }
}
