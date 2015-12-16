package io.plasmap.serializer


import io.plasmap.model._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by erna on 12/7/15.
 */
object XMLSerialiser {

    val dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-DD'T'HH:mm:ss'Z'")
  def prependXEquals(toPrepend:String, x:Option[String]) = x.fold("")(y => toPrepend + "=\"" + y + "\"")

    def elToXML(el:OsmObject, latAndLon: String, extraData: String): String = {
      val id:String = el.id.toString
      val userName:String = prependXEquals("user",el.user.map(_.username))
      val userId:String = prependXEquals("uid",el.user.map(_.uid.toString))
      val visible:String = el.version.visible.toString
      val timestamp:String = DateTime.now().toString(dateTimeFormat)
      val tags = for(tag <- el.tags) yield s"""<tag k="${tag.key}" v="${tag.value}" />"""
      val changeset:String = el.version.changeset.toString
      val version = el.version.versionNumber
      val elType: String = {if(el.isNode) "node"
      else if (el.isWay) "way"
      else "relation"}

      s"""
         |<?xml version="1.0" encoding="UTF-8"?>
         |<osm version="0.6" generator="geow">
         |<${elType} id="${id}" ${if (latAndLon == "") "" else latAndLon} ${userName} ${userId} visible="${visible}" version="${version}" changeset="${changeset}" timestamp="${timestamp}">
         |${tags.mkString}
         |${extraData}
         |</${elType}>
         |</osm>
     """.stripMargin.filterNot(_ == '\n').mkString
    }
    def nodeToXML(n:OsmNode):String = {
      val lat:String = n.point.lat.toString
      val lon:String = n.point.lon.toString

      elToXML(n,"lat=\""+lat+"\" "+"lon=\""+lon+"\"","")
    }

  def wayToXML(way: OsmWay) = {

    val nodes = for (nd <- way.nds) yield s"""<nd ref="${nd.toString}" />"""

    elToXML(way,"",nodes.mkString("\n"))
  }

  def relToXML(relation: OsmRelation) = {

    val refs = for(ref <- relation.refs) yield s"""<member type="${ref.typ.toString}" ref="${ref.ref.toString}" role="${ref.role.toString}" />"""

    elToXML(relation, "", refs.mkString("\n"))

  }

    def toXML(osmObject:OsmObject):String = {
      osmObject.fold(
        nodeToXML,
        wayToXML,
        relToXML
      )
    }
}
