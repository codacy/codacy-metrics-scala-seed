package com.codacy.docker.api

import com.codacy.plugins.api.languages.{Language, Languages}
import com.codacy.plugins.api.metrics.{FileMetrics, LineComplexity, MetricsTool}
import com.codacy.plugins.api.{Options, Source}
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

  implicit lazy val sourceFileFormat: Format[Source.File] =
    Format(Reads.StringReads.map(Source.File), Writes((v: Source.File) => Json.toJson(v.path)))

  implicit def configurationValueToJsValue(configValue: Options.Value): JsValue = {
    configValue match {
      case OptionsValue(v) => v
      case _               => JsNull
    }
  }

  @SuppressWarnings(Array("UnusedMethodParameter"))
  implicit class ConfigurationExtensions(config: Options.type) {
    def Value(jsValue: JsValue): Options.Value =
      OptionsValue(jsValue)

    def Value(raw: String): Options.Value =
      Value(Try(Json.parse(raw)).getOrElse(JsString(raw)))
  }

  implicit lazy val configurationValueFormat: Format[Options.Value] =
    Format(implicitly[Reads[JsValue]].map(Options.Value), Writes(configurationValueToJsValue))

  implicit lazy val configurationOptionsKeyFormat: OFormat[Options.Key] =
    Json.format[Options.Key]

  implicit lazy val configurationOptionsFormat: Format[Map[Options.Key, Options.Value]] =
    Format[Map[Options.Key, Options.Value]](
      Reads { json: JsValue =>
        JsSuccess(json.asOpt[Map[String, JsValue]].fold(Map.empty[Options.Key, Options.Value]) {
          _.map {
            case (k, v) =>
              Options.Key(k) -> Options.Value(v)
          }
        })
      },
      Writes(m =>
        JsObject(m.flatMap {
          case (k, v: OptionsValue) => Option(k.value -> v.value)
          case _                    => Option.empty[(String, JsValue)]
        })))

  implicit val lineComplexityFormat: OFormat[LineComplexity] =
    Json.format[LineComplexity]
  implicit val fileMetricsFormat: OFormat[FileMetrics] =
    Json.format[FileMetrics]
  implicit val metricsConfigurationFormat: OFormat[MetricsTool.CodacyConfiguration] =
    Json.format[MetricsTool.CodacyConfiguration]
}

private[this] final case class OptionsValue(value: JsValue) extends AnyVal with Options.Value
