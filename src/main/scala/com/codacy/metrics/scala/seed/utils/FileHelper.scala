package com.codacy.metrics.scala.seed.utils

import java.nio.charset.StandardCharsets
import java.nio.file.{Path, StandardOpenOption}

import better.files.File

object FileHelper {

  def createTmpFile(content: String, prefix: String = "config", suffix: String = ".conf"): Path = {
    val tmpFile = File.newTemporaryFile(prefix, suffix)
    tmpFile.write(content)(Seq(StandardOpenOption.CREATE), StandardCharsets.UTF_8)
    tmpFile.path
  }

  def stripPath(filename: Path, prefix: Path): String = {
    stripPath(filename.toString, prefix.toString)
  }

  def stripPath(filename: String, prefix: String): String = {
    filename.stripPrefix(prefix).stripPrefix("/")
  }

  def findConfigurationFile(candidates: Set[String], path: Path): Option[Path] = {
    candidates.flatMap { nativeConfigFileName =>
      File(path).listRecursively.filter(_.name == nativeConfigFileName).map(_.path)
    }.to[List].sortBy(_.toString.length).headOption
  }

}
