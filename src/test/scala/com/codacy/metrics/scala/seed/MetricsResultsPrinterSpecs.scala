package com.codacy.metrics.scala.seed

import java.io.{ByteArrayOutputStream, PrintStream}

import com.codacy.metrics.scala.seed.utils.FileHelper
import com.codacy.plugins.api.docker.v2.MetricsResult
import com.codacy.plugins.api.Implicits._
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
      val sourcePath = dockerMetricsEnvironment.sourcePath
      val fileMetrics =
        MetricsResult.FileMetrics(s"$sourcePath/$fileName", Some(1), Some(399), Some(23), Some(3), Some(2), Set(MetricsResult.LineComplexity(1, 2)))

      //when
      printer.printResults(List(fileMetrics), sourcePath.toString)

      //then
      Json.parse(outContent.toString) must beEqualTo(
        Json.toJson(fileMetrics.copy(filename = FileHelper.stripPath(fileName, sourcePath.toString))))
    }
  }
}
