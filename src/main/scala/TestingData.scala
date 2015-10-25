import java.io.{FileWriter, BufferedWriter, File}
import io.plasmap.parser.OsmParser
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import org.jsoup.nodes.{Document, Element}
import scala.util.Try
import java.net.URL

/**
 * Created by erna on 10/15/15.
 */
object TestingData extends App{
  // create a parser from a file
  val parser = OsmParser("duesseldorf-regbez-latest.osm.bz2")

  // pull openstreetmap data
  for (elem <- parser) {
    elem.fold(){
      osmObj =>
        val tags:Map[String, String] = (for(tag <- osmObj.tags) yield tag.key -> tag.value).toMap

        //println(osmObj.id.value)

        val result = check(tags,"restaurant", "cuisine", "website").map(x => scrape(x, tags.get("cuisine").get, osmObj.id.value))
    }
  }

  def check(tags: Map[String, String], amenity: String, valueML: String, contain: String): Option[String] = {

    if(tags.toList.contains("amenity" -> amenity) && tags.get(valueML).isDefined) {
      tags.get(contain)
    } else None
  }

  def scrape(website: String, v: String, id: Long): Unit = {

    val link: String = url(website)
    val browser = new Browser
    val docOpt: Option[Document] = Try(browser.get(link)).toOption
    docOpt.fold(())( doc => {
      //texte auslesen
      val text: String = readText(doc)
      val links: List[String] = getLinks(doc)
      val path: String = "test/"+splitCuisine(v)+"/"+id
      val bwOpt: Option[BufferedWriter] = Try(new BufferedWriter(new FileWriter(new File(path)))).toOption
      bwOpt.fold(())( bw => {
        bw.write(text)
        for(l <- links.distinct) {
          scrapeURL(l,link,bw)
        }
        bw.newLine()
        bw.close()
      })
    }
    )
  }

  def url(website: String): String = {
    val website2 = website.trim()
    val browser = new Browser
    if(!website2.startsWith("http")) {
      "http://" + website2
    }
    else {
      website2
    }
  }

  def count(body: Element): Unit = {
    println(body)
  }

  def readText(doc: Document) = {
    val html: Option[Element] = doc >?> element("html")
    html.map(x => {
      recursiveGet(x)
    }).getOrElse("")
  }

  def getLinks(doc: Document): List[String] = {
    val linksOpt = doc >?> elementList("a") >> attr("href")("a")
    linksOpt.getOrElse(Nil)
  }

  def recursiveGet(elem: Element): String = {
    val otherText = for(child <- elem.children)
      yield recursiveGet(child)

    elem.text + " " + otherText.mkString(" ")
  }

  def splitCuisine(cuisine: String): String = {
    if (cuisine.contains(';')) {
      val a: Array[String] = cuisine.split(";")
      a.apply(0)
    }
    else cuisine
  }

  def getBaseUrl(link:String):Option[String] = Try{
    val url = new URL(link)
    url.getProtocol + "://" + url.getHost
  }.toOption



  def scrapeURL(link: String, baselink: String, bw: BufferedWriter) = {
    def writeToFile(uri:String) = {
      val browser = new Browser
      val docOpt: Option[Document] = Try(browser.get(uri)).toOption
      docOpt.fold(())( doc => {
        bw.write(readText(doc))
      })
    }
    link match {
      case _ if link.startsWith("http") || link.startsWith("www") =>
        val l=url(link)
        val baseUrl = getBaseUrl(l)
        baseUrl.foreach(burl =>
          if(baselink == burl)
            writeToFile(l)
        )
      case _ if link.startsWith("/") =>
        writeToFile(baselink + link)
      case _ =>
        writeToFile(baselink + "/" + link)
    }
  }
}
