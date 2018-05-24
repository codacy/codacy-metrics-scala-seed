package com.codacy.docker.api.metrics

import better.files._
import codacy.docker.api.MetricsConfiguration
import play.api.libs.json.{JsError, JsPath, Json, JsonValidationError}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

class DockerMetricsEnvironment(variables: Map[String, String] = sys.env) {

  lazy val configFilePath: File = File.root / ".codacyrc"

  lazy val sourcePath: File = File("/src")

  def getConfiguration(configFile: File = configFilePath,
                       sourceDir: File = sourcePath): Try[Option[MetricsConfiguration]] = {
    if (configFile.exists) {
      for {
        content <- Try(configFile.byteArray)
        json <- Try(Json.parse(content))
        cfg <- json.validate[MetricsConfiguration].fold(asFailure, Success.apply)
      } yield {
        Some(cfg.copy(files = cfg.files.map(_.map(file => file.copy(path = (sourceDir / file.path).toString)))))
      }
    } else {
      Success(Option.empty[MetricsConfiguration])
    }
  }

  lazy val timeout: FiniteDuration =
    variables
      .get("METRICS_TIMEOUT")
      .flatMap(rawDuration =>
        Try(Duration(rawDuration)).toOption.collect {
          case d: FiniteDuration => d
      })
      .getOrElse(10.minutes)

  lazy val isDebug: Boolean =
    variables.get("METRICS_DEBUG").flatMap(rawDebug => Try(rawDebug.toBoolean).toOption).getOrElse(false)

  private def asFailure[T](error: Seq[(JsPath, Seq[JsonValidationError])]): Try[T] =
    Failure[T](new Throwable(Json.stringify(JsError.toJson(error.toList))))

}
