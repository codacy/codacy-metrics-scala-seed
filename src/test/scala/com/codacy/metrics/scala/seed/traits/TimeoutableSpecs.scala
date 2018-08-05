package com.codacy.metrics.scala.seed.traits

import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, TimeoutException}

class TimeoutableSpecs extends Specification with Timeoutable {

  "Timeoutable" should {
    "should throw exception" in {
      val f = timeout(100.seconds) {
        failure("It should fail because a timeoutException wasn't thrown.")
      }

      Await.result(f, 1.second) must throwA[TimeoutException]
    }

    "shouldn't throw exception" in {
      val f = timeout(1.seconds) {
        success("The delay didn't throw a TimeoutException exception")
      }

      // scalafix:off NoInfer.any
      Await.result(f, Duration.Inf) must not(throwA[TimeoutException])
      // scalafix:on NoInfer.any
    }
  }
}
