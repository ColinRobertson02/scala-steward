package org.scalasteward.core.mock

import better.files.File
import cats.effect.IO
import org.scalasteward.core.io.FileAlgTest.ioFileAlg
import cats.implicits._

final case class MockState(
    commands: Vector[List[String]],
    logs: Vector[(Option[Throwable], String)],
    files: Map[File, String]
) {
  def add(file: File, content: String): MockState =
    copy(files = files + (file -> content))

  def addFiles(newFiles: Map[File, String]): MockState =
    copy(files = files ++ newFiles)

  def rm(file: File): MockState =
    copy(files = files - file)

  def exec(cmd: List[String], env: (String, String)*): MockState =
    copy(commands = commands :+ (env.map { case (k, v) => s"$k=$v" }.toList ++ cmd))

  def log(maybeThrowable: Option[Throwable], msg: String): MockState =
    copy(logs = logs :+ ((maybeThrowable, msg)))

  def init: IO[MockState] =
    files.toList.traverse_ { case (file, content) => ioFileAlg.writeFile(file, content) }.as(this)
}

object MockState {
  def empty: MockState =
    MockState(commands = Vector.empty, logs = Vector.empty, files = Map.empty)
}
