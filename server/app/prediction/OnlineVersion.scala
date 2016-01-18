package prediction

import io.plasmap.model.{OsmTag, OsmUser, OsmObject, OsmNode}
import io.plasmap.parser.impl.{OsmGeoJSONParser, OsmXmlParser}
import io.plasmap.serializer.XMLSerialiser
import play.api.libs.json._
import play.libs.XML
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

  def transform(n:Node, attribute:String):Option[String] = {
    for{
      attrs    <- n.attribute(attribute)
      first    <- attrs.headOption
    } yield first.text
  }

  def getXML(id: String): List[(String, String)] = {
    val url = s"http://www.openstreetmap.org/api/0.6/node/$id"
    val source = Source.fromURL(url)
    val parser = XhtmlParser(source)
    val elements: NodeSeq = parser \\ "tag"
    val asSeq = elements.flatMap((elem: Node) => {
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

  def getXMLNode(tags: Map[String, String], changeSet: String, userName: String, userId: String): (String, String) = {
    tags.get("id").map(
    y => {
      val osmTags: Iterable[OsmTag] = for ((k, v) <- tags) yield OsmTag.apply(k, v)
      val a = OsmXmlParser.apply(Source.fromURL(s"http://www.openstreetmap.org/api/0.6/node/$y"))
      a.next().map {
        x => {
          val node = OsmNode(x.id, Some(OsmUser(userName, userId.toLong)), x.version, osmTags.toList.filter(t => t.key!=""&&t.value!=""&&t.key!="id"), x.nodeOption.get.point)
          val version = node.version.versionNumber
          val newVersion = node.version.copy(changeset = changeSet.toInt)
          val newNode = node.copy(version = newVersion)
          (y, XMLSerialiser.toXML(newNode))
        }
      }.getOrElse((y,""))
    }
    ).getOrElse(("",""))
  }

  def getAdresses(id:String): (List[String], List[String], List[String]) = {

    val xmlParser = OsmXmlParser.apply(Source.fromURL(s"http://www.openstreetmap.org/api/0.6/node/$id"))
    val coords = xmlParser.next().get.nodeOption.get.point //POINT IST NUR BEI NODE
    val lon = coords.lon
    val lat = coords.lat
    val mapzenURL = s"""https://search.mapzen.com/v1/reverse?api_key=search-J1iJrKY&point.lat=${lat}&point.lon=${lon}"""
    val parser = Json.parse(Source.fromURL(mapzenURL).mkString)
    ((parser\\"street").map(_.as[String]).toList,(parser\\"housenumber").map(_.as[String]).toList,(parser\\"locality").map(_.as[String]).toList)
  }

  def getUId (userInfo: String): String = {
    val xml = XML.fromString(userInfo)
    val user = xml.getElementsByTagName("user")
    println(xml.getXmlVersion)
    val id = user.item(0).getAttributes.getNamedItem("id").getTextContent
    println(id)
    id
  }
}
