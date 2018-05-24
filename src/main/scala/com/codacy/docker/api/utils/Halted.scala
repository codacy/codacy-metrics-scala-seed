package com.codacy.docker.api.utils

trait Halted {

  def halt(status: Int): Unit = {
    Runtime.getRuntime.halt(status)
  }

}
