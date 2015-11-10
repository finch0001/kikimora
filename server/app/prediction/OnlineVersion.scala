package prediction

import scala.collection.immutable.Seq
import scala.xml._

/**
 * Created by erna on 11/6/15.
 */
object OnlineVersion extends App{

  def getXML(id: String): List[(String, String)] = {

    val elements: NodeSeq = XML.load("http://www.openstreetmap.org/api/0.6/node/"+id) \\ "tag"
    val asSeq = for(el <- elements) yield (el\@("k"),el \@("v"))
    asSeq.toList
  }

}
