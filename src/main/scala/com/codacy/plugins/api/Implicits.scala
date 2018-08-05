package com.codacy.plugins.api

import com.codacy.plugins.api.docker.v2.{MetricsResult, Problem}
import com.codacy.plugins.api.languages.{Language, Languages}
import com.codacy.plugins.api.metrics.MetricsTool
import play.api.libs.json.{JsResult, _}

import scala.concurrent.duration.{FiniteDuration, TimeUnit}
import scala.language.implicitConversions
import scala.util.Try

object Implicits {

  implicit def optionsValueToJsValue(configValue: Options.Value): JsValue = {
    configValue match {
      case OptionsValue(v) => v
      case _               => JsNull
    }
  }

  @SuppressWarnings(Array("UnusedMethodParameter"))
  implicit class OptionsExtensions(config: Options.type) {
    def Value(jsValue: JsValue): Options.Value = OptionsValue(jsValue)

    def Value(raw: String): Options.Value = Value(Try(Json.parse(raw)).getOrElse(JsString(raw)))
  }

  def enumWrites[E <: Enumeration#Value]: Writes[E] = Writes((e: E) => Json.toJson(e.toString))

  def enumReads[E <: Enumeration](e: E): Reads[e.Value] = {
    Reads.StringReads.flatMap { value =>
      Reads((_: JsValue) =>
        e.values.collectFirst {
          case enumValue if enumValue.toString == value =>
            JsSuccess(enumValue)
        }.getOrElse(JsError(s"Invalid enumeration value $value"): JsResult[e.Value]))
    }
  }

  // Docker Output API

  final private case class ApiFiniteDuration(length: Long, unit: TimeUnit)

  implicit val durationFmt: Format[FiniteDuration] = {
    def finiteDurationFrom(mine: ApiFiniteDuration): FiniteDuration = {
      new FiniteDuration(mine.length, mine.unit)
    }

    def serializableFromFiniteDuration(finiteDuration: FiniteDuration): ApiFiniteDuration = {
      ApiFiniteDuration(finiteDuration.length, finiteDuration.unit)
    }

    implicit val timeUnit: Format[TimeUnit] = Format(Reads[TimeUnit] { stringUnit: JsValue =>
      stringUnit.validate[String].map(x => java.util.concurrent.TimeUnit.valueOf(x))
    }, Writes[TimeUnit] { timeUnit: TimeUnit =>
      JsString(timeUnit.toString)
    })

    Format[FiniteDuration](Json.reads[ApiFiniteDuration].map(finiteDurationFrom),
      Writes[FiniteDuration](fd =>
        Json.writes[ApiFiniteDuration].writes(serializableFromFiniteDuration(fd))))
  }

  implicit lazy val patternLanguageFormat: Format[Language] =
    Format(
      {
        Reads.StringReads.reads(_).flatMap { string =>
          Languages
            .fromName(string)
            .fold[JsResult[Language]](JsError(s"Could not find language for name $string"))(JsSuccess(_))
        }
      },
      Writes((v: Language) => Json.toJson(v.name)))
  implicit lazy val errorMessageFormat: Writes[ErrorMessage] = Writes((v: ErrorMessage) => Json.toJson(v.value))
  implicit lazy val resultLineFormat: Writes[Source.Line] = Writes((v: Source.Line) => Json.toJson(v.value))
  implicit lazy val sourceFileFormat: Format[Source.File] =
    Format(Reads.StringReads.map(Source.File), Writes((v: Source.File) => Json.toJson(v.path)))

  implicit val analysisProblemReason: OWrites[Problem.Reason] = {
    implicit val parameterProblemFmt: OWrites[Problem.Reason.ParameterProblem] =
      Json.writes[Problem.Reason.ParameterProblem]
    implicit val optionProblemFmt: OWrites[Problem.Reason.OptionProblem] = Json.writes[Problem.Reason.OptionProblem]
    val missingConfigurationFmt: OWrites[Problem.Reason.MissingConfiguration] =
      Json.writes[Problem.Reason.MissingConfiguration]
    val invalidConfigurationFmt: OWrites[Problem.Reason.InvalidConfiguration] =
      Json.writes[Problem.Reason.InvalidConfiguration]
    val missingOptionsFmt: OWrites[Problem.Reason.MissingOptions] = Json.writes[Problem.Reason.MissingOptions]
    val invalidOptionsFmt: OWrites[Problem.Reason.InvalidOptions] = Json.writes[Problem.Reason.InvalidOptions]
    val timeoutFmt: OWrites[Problem.Reason.TimedOut] = Json.writes[Problem.Reason.TimedOut]
    val missingArtifactsFmt: OWrites[Problem.Reason.MissingArtifacts] =
      Json.writes[Problem.Reason.MissingArtifacts]
    val invalidArtifactsFmt: OWrites[Problem.Reason.InvalidArtifacts] =
      Json.writes[Problem.Reason.InvalidArtifacts]
    val otherReasonFmt: OWrites[Problem.Reason.OtherReason] = Json.writes[Problem.Reason.OtherReason]

    OWrites {
      case v: Problem.Reason.MissingConfiguration =>
        addType[Problem.Reason.MissingConfiguration](missingConfigurationFmt.writes(v))
      case v: Problem.Reason.InvalidConfiguration =>
        addType[Problem.Reason.InvalidConfiguration](invalidConfigurationFmt.writes(v))
      case v: Problem.Reason.MissingOptions   => addType[Problem.Reason.MissingOptions](missingOptionsFmt.writes(v))
      case v: Problem.Reason.InvalidOptions   => addType[Problem.Reason.InvalidOptions](invalidOptionsFmt.writes(v))
      case v: Problem.Reason.TimedOut         => addType[Problem.Reason.TimedOut](timeoutFmt.writes(v))
      case v: Problem.Reason.MissingArtifacts => addType[Problem.Reason.MissingArtifacts](missingArtifactsFmt.writes(v))
      case v: Problem.Reason.InvalidArtifacts => addType[Problem.Reason.InvalidArtifacts](invalidArtifactsFmt.writes(v))
      case v: Problem.Reason.OtherReason      => addType[Problem.Reason.OtherReason](otherReasonFmt.writes(v))
    }
  }

  implicit lazy val metricsLineComplexityWrites: Writes[MetricsResult.LineComplexity] =
    Json.writes[MetricsResult.LineComplexity]

  implicit val metricsResultFmt: OWrites[MetricsResult] = {
    val metricsResultFileMetricsWrites: OWrites[MetricsResult.FileMetrics] = Json.writes[MetricsResult.FileMetrics]
    val metricsResultProblemWrites: OWrites[MetricsResult.Problem] = Json.writes[MetricsResult.Problem]

    OWrites[MetricsResult] {
      case v: MetricsResult.FileMetrics   => addType[MetricsResult.FileMetrics](metricsResultFileMetricsWrites.writes(v))
      case v: MetricsResult.Problem => addType[MetricsResult.Problem](metricsResultProblemWrites.writes(v))
    }
  }

  implicit lazy val configurationOptionsKeyFormat: Format[Options.Key] = Json.format[Options.Key]
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
      Writes(m => JsObject(m.collect { case (k, v: OptionsValue) => k.value -> v.value })))

  implicit lazy val metricsToolCodacyConfigurationFormat: Reads[MetricsTool.CodacyConfiguration] =
    Json.reads[MetricsTool.CodacyConfiguration]

  private def addType[T](jso: JsObject)(implicit evidence: reflect.ClassTag[T]): JsObject = {
    jso ++ JsObject(Seq(("$type", JsString(evidence.runtimeClass.getName))))
  }

}
