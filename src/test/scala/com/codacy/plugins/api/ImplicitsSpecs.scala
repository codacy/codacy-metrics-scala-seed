package com.codacy.plugins.api


import com.codacy.plugins.api.docker.v2.{MetricsResult, Problem}
import com.codacy.plugins.api.Implicits._
import org.specs2.mutable.Specification
import play.api.libs.json.Json

import scala.concurrent.duration._

class ImplicitsSpecs extends Specification {

  "Implicit conversions" should {

    val missingConfigJsonString =
      s"""{"message":"this is a message","reason":{"supportedFilename":["batato"],"$$type":"com.codacy.plugins.api.docker.v2.Problem$$Reason$$MissingConfiguration"},"$$type":"com.codacy.plugins.api.docker.v2.MetricsResult$$Problem"}"""
    val missingConfigMetricsProblem: MetricsResult =
      MetricsResult.Problem(ErrorMessage("this is a message"), None, Problem.Reason.MissingConfiguration(Set("batato")))

    val timeOutJsonString =
      s"""{"message":"this is a message","reason":{"timeout":{"length":10,"unit":"SECONDS"},"$$type":"com.codacy.plugins.api.docker.v2.Problem$$Reason$$TimedOut"},"$$type":"com.codacy.plugins.api.docker.v2.MetricsResult$$Problem"}"""
    val timeOutMetricsProblem =
      MetricsResult.Problem(ErrorMessage("this is a message"), None, Problem.Reason.TimedOut(10.seconds))

//    "deserialize MetricsProblem with MissingConfiguration reason" in {
//      Json.parse(missingConfigJsonString).asOpt[MetricsResult] should beSome(missingConfigMetricsProblem)
//    }

    "serialize MetricsProblem with MissingConfiguration reason" in {
      Json.stringify(Json.toJson(missingConfigMetricsProblem)) shouldEqual missingConfigJsonString
    }

//    "deserialize MetricsProblem with TimeOut reason" in {
//      Json.parse(timeOutJsonString).asOpt[MetricsResult] should beSome(timeOutMetricsProblem)
//    }

    "serialize MetricsProblem with TimeOut reason" in {
      Json.stringify(Json.toJson(timeOutMetricsProblem)) shouldEqual timeOutJsonString
    }
  }

}
