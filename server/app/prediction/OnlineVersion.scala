package prediction

import io.plasmap.parser.impl.OsmXmlParser
import scala.collection.immutable
import scala.collection.immutable.{Iterable, Seq}
import scala.io.Source
import scala.xml.{NodeSeq, Node}
import scala.xml.parsing.XhtmlParser
import shapeless._
import syntax.std.tuple._
import poly._


/**
 * Created by erna on 11/6/15.
 */
object OnlineVersion extends App{
  def sequenceTuple[A,B](x:(Option[A],Option[B])):Option[(A,B)] = {
    x match {
      case (Some(a), Some(b)) => Some((a,b))
      case _ => None
    }
  }

  def getXML(id: String): List[(String, String)] = {
    val url = s"http://www.openstreetmap.org/api/0.6/node/$id"
    val source = Source.fromURL(url)
    val parser = XhtmlParser(source)
    val elements: NodeSeq = parser \\ "tag"
    val asSeq = elements.flatMap((elem: Node) => {
      def transform(n:Node, attribute:String):Option[String] = {
        for{
          attrs    <- n.attribute(attribute)
          first    <- attrs.headOption
        } yield first.text
      }
      sequenceTuple(
        (
          transform(elem, "k"),
          transform(elem, "v")
          )
      )
    })
    asSeq.toList

  }

  def createChangeset(comment: String, createdBy: String): String = {
      s"""
         |<?xml version="1.0" encoding="UTF-8"?>
         |<osm version="0.6" generator="Kikimora">
         |  <changeset>
         |    <tag k='comment' v='$comment' />
         |    <tag k='created_by' v='$createdBy' />
         |    <tag k='bot' v='yes' />
         |  </changeset>
         |</osm>
      """.stripMargin.filterNot(_ == '\n').mkString
  }

  def closeChangeset(id: String) {
    //Close: PUT /api/0.6/changeset/
    //return nothing, code 200 or error code
  }
/*
  def createNode(id: String, changesetID: String,  newTags: Map[String, String]): String = {
    val node = "" +
    "<osm>"+
      "<node>"+
        tagsSet(newTags) +
      "</node>"+
    "/osm"
  }

  def tagsSet(tags: Map[String, String]): String = {
    val result = for ((k,v) <- tags) yield {"<tag k=\"" + k + "\" v=\"" + v + "\" />"}

  }
*/
}
