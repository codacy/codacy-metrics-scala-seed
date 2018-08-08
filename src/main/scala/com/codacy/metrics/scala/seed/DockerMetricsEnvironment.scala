package com.codacy.metrics.scala.seed

import java.nio.file.{Files, Path, Paths}

import better.files._
import com.codacy.plugins.api.Implicits._
import com.codacy.plugins.api.metrics.MetricsTool
import play.api.libs.json.{JsError, JsPath, Json, JsonValidationError}

import scala.concurrent.duration.{Duration, FiniteDuration, _}
import scala.util.{Failure, Success, Try}

class DockerMetricsEnvironment(variables: Map[String, String] = sys.env) {

  lazy val configFilePath: Path = File.root.path.resolve(".codacyrc")
  lazy val sourcePath: Path = Paths.get("/src")

  def getConfiguration(configPath: Path = configFilePath,
                       sourceDirPath: Path = sourcePath): Try[MetricsTool.CodacyConfiguration] = {

    val rawConfig: Try[Array[Byte]] = getConfigurationFromFile(configPath, sourceDirPath)
    rawConfig.transform(
      raw => Try(Json.parse(raw)).flatMap(_.validate[MetricsTool.CodacyConfiguration].fold(asFailure, conf => Success(conf))),
      _ => Try(MetricsTool.CodacyConfiguration(None, None, None)))
  }
  private def getConfigurationFromFile(configPath: Path, alternateConfigPath: Path): Try[Array[Byte]] = {
    Try(Files.readAllBytes(configPath)).recoverWith {
      case _ =>
      Try(Files.readAllBytes(alternateConfigPath))
    }
  }

  lazy val timeout: FiniteDuration =
    variables
      .get("TIMEOUT")
      .flatMap(rawDuration =>
        Try(Duration(rawDuration)).toOption.collect {
          case d: FiniteDuration => d
      })
      .getOrElse(15.minutes)

  lazy val isDebug: Boolean =
    variables.get("DEBUG").flatMap(rawDebug => Try(rawDebug.toBoolean).toOption).getOrElse(false)

  private def asFailure[T](error: Seq[(JsPath, Seq[JsonValidationError])]): Try[T] =
    Failure[T](new Throwable(Json.stringify(JsError.toJson(error.toList))))

}
