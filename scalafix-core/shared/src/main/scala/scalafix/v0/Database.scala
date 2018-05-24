package scalafix.v0

import scala.compat.Platform.EOL
import scala.meta.inputs._
import scala.meta.io._
import scala.meta.internal.io.PathIO
import scala.meta.internal.semanticdb._
import scala.meta.internal.semanticdb.{vfs => v}
import scala.meta.internal.{semanticdb3 => s}

final case class Database(documents: Seq[Document]) {
  lazy val names: Seq[ResolvedName] = documents.flatMap(_.names)
  lazy val messages: Seq[Message] = documents.flatMap(_.messages)
  lazy val symbols: Seq[ResolvedSymbol] = documents.flatMap(_.symbols)
  lazy val synthetics: Seq[Synthetic] = documents.flatMap(_.synthetics)

  def syntax: String = {
    val s_entries = documents.map { attrs =>
      val s_input = PathIO.toUnix(attrs.input.syntax)
      val separator = EOL + "-" * s_input.toString.length + EOL
      s_input + separator + attrs.syntax
    }
    s_entries.sorted.mkString(EOL + EOL)
  }

  def structure: String = {
    val s_entries = documents.map(_.structure).mkString(",")
    s"Database(List($s_entries))"
  }

  override def toString: String = syntax
}

object Database {
  def load(classpath: Classpath, sourcepath: Sourcepath): Database = {
    v.Database.load(classpath).toSchema.toDb(Some(sourcepath))
  }

  def load(classpath: Classpath): Database = {
    v.Database.load(classpath).toSchema.toDb(None)
  }

  def load(bytes: Array[Byte]): Database = {
    val sdocs = s.TextDocuments.parseFrom(bytes)
    val mdb = sdocs.mergeDiagnosticOnlyDocuments.toDb(None)
    mdb
  }
}
