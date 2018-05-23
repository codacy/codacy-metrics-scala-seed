package com.codacy.docker.api.metrics

import better.files._
import codacy.docker.api.MetricsConfiguration
import play.api.libs.json.{JsError, JsPath, Json, JsonValidationError}

import scala.util.{Failure, Success, Try}

object DockerMetricsEnvironment {

  lazy val configFilePath: File = File.root / ".codacyrc"

  lazy val sourcePath: File = File("/src")

  def getConfiguration(configFilePath: File,
                       sourcePath: File): Try[Option[MetricsConfiguration]] = {
    if (configFilePath.exists) {
      for {
        content <- Try(configFilePath.byteArray)
        json <- Try(Json.parse(content))
        cfg <- json
          .validate[MetricsConfiguration]
          .fold(asFailure, Success.apply)
      } yield {
        Some(cfg.copy(files = cfg.files.map(_.map(file =>
          file.copy(path = (sourcePath / file.path).toString)))))
      }
    } else {
      Success(Option.empty[MetricsConfiguration])
    }
  }

  private def asFailure[T](
      error: Seq[(JsPath, Seq[JsonValidationError])]): Try[T] =
    Failure[T](new Throwable(Json.stringify(JsError.toJson(error.toList))))

}
