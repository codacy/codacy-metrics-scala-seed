package com.codacy.docker.api.metrics

import java.nio.file.{Path, Paths}

import better.files._
import com.codacy.plugins.api.metrics.MetricsTool
import play.api.libs.json.{JsError, JsPath, Json, JsonValidationError}

import scala.concurrent.duration.{Duration, FiniteDuration, _}
import scala.util.{Failure, Success, Try}

class DockerMetricsEnvironment(variables: Map[String, String] = sys.env) {

  val defaultRootFile: Path = Paths.get("/src")
  val defaultConfigFile: Path = Paths.get("/.codacyrc")

  def configurations(rootFile: File = defaultRootFile,
                     configFile: File = defaultConfigFile): Try[Option[MetricsTool.CodacyConfiguration]] = {
    if (configFile.exists) {
      for {
        content <- Try(configFile.byteArray)
        json <- Try(Json.parse(content))
        cfg <- json.validate[MetricsTool.CodacyConfiguration].fold(asFailure, Success.apply)
      } yield {
        Option(cfg.copy(files = cfg.files.map(_.map(file => file.copy(path = (rootFile / file.path).toString)))))
      }
    } else {
      Success(Option.empty[MetricsTool.CodacyConfiguration])
    }
  }

  val defaultTimeout: FiniteDuration =
    variables
      .get("TIMEOUT")
      .flatMap(timeoutStrValue =>
        Try(Duration(timeoutStrValue)).toOption.collect {
          case d: FiniteDuration => d
      })
      .getOrElse(15.minutes)

  val debug: Boolean =
    variables.get("DEBUG").flatMap(debugStrValue => Try(debugStrValue.toBoolean).toOption).getOrElse(false)

  private def asFailure[T](error: Seq[(JsPath, Seq[JsonValidationError])]): Try[T] =
    Failure[T](new Throwable(Json.stringify(JsError.toJson(error.toList))))

}
