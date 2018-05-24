package com.codacy.docker.api

import codacy.docker.api.metrics.{FileMetrics, LineComplexity}
import codacy.docker.api.{MetricsConfiguration, Source}
import com.codacy.api.dtos.{Language, Languages}
import play.api.libs.json._

import scala.language.implicitConversions
import scala.util.Try

package object metrics {
  implicit val languageFormat: Format[Language] = Format[Language](
    Reads { json: JsValue =>
      json.validate[String].flatMap { langStr =>
        Languages
          .fromName(langStr)
          .fold[JsResult[Language]](JsError(s"Could not find language for name $langStr"))(JsSuccess(_))
      }
    },
    Writes((v: Language) => JsString(v.name)))

  implicit val fileFormat: OFormat[Source.File] =
    Json.format[codacy.docker.api.Source.File]

  implicit def configurationValueToJsValue(configValue: MetricsConfiguration.Value): JsValue = {
    configValue match {
      case MetricsConfigurationValue(v) => v
      case _                            => JsNull
    }
  }

  implicit class ConfigurationExtensions(config: MetricsConfiguration.type) {
    def Value(jsValue: JsValue): MetricsConfiguration.Value =
      MetricsConfigurationValue(jsValue)

    def Value(raw: String): MetricsConfiguration.Value =
      Value(Try(Json.parse(raw)).getOrElse(JsString(raw)))
  }

  implicit lazy val configurationValueFormat: Format[MetricsConfiguration.Value] =
    Format(implicitly[Reads[JsValue]].map(MetricsConfiguration.Value), Writes(configurationValueToJsValue))

  implicit lazy val configurationOptionsKeyFormat: OFormat[MetricsConfiguration.Key] =
    Json.format[MetricsConfiguration.Key]
  implicit lazy val configurationOptionsFormat: Format[Map[MetricsConfiguration.Key, MetricsConfiguration.Value]] =
    Format[Map[MetricsConfiguration.Key, MetricsConfiguration.Value]](
      Reads { json: JsValue =>
        JsSuccess(
          json.asOpt[Map[String, JsValue]].fold(Map.empty[MetricsConfiguration.Key, MetricsConfiguration.Value]) {
            _.map {
              case (k, v) =>
                MetricsConfiguration.Key(k) -> MetricsConfiguration.Value(v)
            }
          })
      },
      Writes(m =>
        JsObject(m.map {
          case (k, v: MetricsConfigurationValue) => k.value -> v.value
        })))

  implicit val lineComplexityFormat: OFormat[LineComplexity] =
    Json.format[LineComplexity]
  implicit val fileMetricsFormat: OFormat[FileMetrics] =
    Json.format[FileMetrics]
  implicit val metricsConfigurationFormat: OFormat[MetricsConfiguration] =
    Json.format[MetricsConfiguration]
}

private[this] case class MetricsConfigurationValue(value: JsValue) extends AnyVal with MetricsConfiguration.Value
