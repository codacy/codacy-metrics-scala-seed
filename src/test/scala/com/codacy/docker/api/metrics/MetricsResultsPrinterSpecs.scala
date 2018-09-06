package com.codacy.docker.api.metrics

import java.io.{ByteArrayOutputStream, PrintStream}

import com.codacy.docker.api.utils.FileHelper
import com.codacy.plugins.api.metrics.{FileMetrics, LineComplexity}
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class MetricsResultsPrinterSpecs extends Specification {

  "MetricsResultsPrinter" should {
    "print the file metrics converted to json to the given print stream" in {
      //given
      val outContent = new ByteArrayOutputStream()
      val printStream = new PrintStream(outContent)
      val printer = new MetricsResultsPrinter(printStream)
      val dockerMetricsEnvironment = new DockerMetricsEnvironment(Map.empty)
      val fileName = "a.scala"
      val sourcePath = dockerMetricsEnvironment.defaultRootFile.toString
      val fileMetrics =
        FileMetrics(s"$sourcePath/$fileName", Some(1), Some(399), Some(23), Some(3), Some(2), Set(LineComplexity(1, 2)))

      //when
      printer.printResults(List(fileMetrics), sourcePath)

      //then
      Json.parse(outContent.toString) must beEqualTo(
        Json.toJson(fileMetrics.copy(filename = FileHelper.stripPath(fileName, sourcePath))))
    }
  }
}
