package com.codacy.metrics.scala.seed.traits

trait Haltable {

  def halt(status: Int): Unit = {
    Runtime.getRuntime.halt(status)
  }

}
