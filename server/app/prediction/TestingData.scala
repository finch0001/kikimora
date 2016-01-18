package prediction

import java.io.{BufferedWriter, File, FileWriter}
import java.net.URL

import io.plasmap.parser.OsmParser
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.jsoup.nodes.{Document, Element}

import scala.io.Source
import scala.util.Try
import scalaz.\/

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
      val lang: String = doc >> attr("lang")("html")
      println(lang)
      val text: String = {if((lang.toLowerCase).contains("de")) {
        //texte auslesen
        readText(doc)
      }
      else ""}
      val disallowList = getLinksFromRobots(getBaseUrl(link).getOrElse(""))
      println(disallowList)
      //val links: List[String] = getLinks(doc)
      val path: String = "test/"+splitCuisine(v)+"/"+id
      val links: List[String] = getLinks(doc).distinct.filterNot(disallowList.toSet)
      val bwOpt: Option[BufferedWriter] = Try(new BufferedWriter(new FileWriter(new File(path)))).toOption
      bwOpt.fold(())( bw => {
        bw.write(text)
        bw.write(links.foldLeft(text)((texts, l) => texts + scrapeURL(l, link).mkString))
        /*for(l <- links.distinct) {
          scrapeURL(l,link,bw)
        }*/
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

  def getLinksFromRobots(link: String): List[String] = {
    def trimAll(s: String, bad: String): String = {
      @scala.annotation.tailrec def start(n: Int): String =
        if (n == s.length) ""
        else if (bad.indexOf(s.charAt(n)) < 0) end(n, s.length)
        else start(1 + n)

      @scala.annotation.tailrec def end(a: Int, n: Int): String =
        if (n <= a) s.substring(a, n)
        else if (bad.indexOf(s.charAt(n - 1)) < 0) s.substring(a, n)
        else end(a, n - 1)

      start(0)
    }
    def getDisallowed(line:String): String = {
      if(line.startsWith("Disallow:")&&line.length>10) {
        getBaseUrl(link).getOrElse("")+trimAll(line.substring(10)," \t\n\r")
      }
      else ""
    }
    val rob = link+"/robots.txt"
    \/.fromTryCatchNonFatal{
      (for (line <- Source.fromURL(rob).getLines()) yield getDisallowed(line)).toList
    }.getOrElse(Nil).distinct
  }

  def scrapeURL(link: String, baselink: String): String = {
    def readFromFile(uri: String): String = {
      val browser = new Browser
      val docOpt: Option[Document] = Try(browser.get(uri)).toOption
      docOpt.map(readText).getOrElse("")
    }
    link match {
      case _ if link.startsWith("http") || link.startsWith("www") =>
        val l = url(link)
        val baseUrl = getBaseUrl(l)
        if (baselink == baseUrl) readFromFile(l)
        else ""
      case _ if link.startsWith("/") =>
        readFromFile(baselink + link)
      case _ =>
        readFromFile(baselink + "/" + link)
    }
  }
}
