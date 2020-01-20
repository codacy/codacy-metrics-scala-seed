package com.codacy.docker.api.metrics

import java.nio.file.{Path, Paths}
import java.util.concurrent.TimeUnit

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
        cfg <- json
          .validate[MetricsTool.CodacyConfiguration]
          .fold(error => Failure(new Throwable(Json.stringify(JsError.toJson(error.toList)))), Success.apply)
      } yield {
        Option(cfg.copy(files = cfg.files.map(_.map(file => file.copy(path = (rootFile / file.path).toString)))))
      }
    } else {
      Success(Option.empty[MetricsTool.CodacyConfiguration])
    }
  }

  val defaultTimeout: FiniteDuration =
    variables
      .get("TIMEOUT_SECONDS")
      .flatMap(timeoutStrValue => Try(FiniteDuration(timeoutStrValue.toLong, TimeUnit.SECONDS)).toOption)
      .getOrElse(15.minutes)

  val debug: Boolean =
    variables.get("DEBUG").flatMap(debugStrValue => Try(debugStrValue.toBoolean).toOption).getOrElse(false)
}
