package prediction

import java.io.{File, FileWriter, BufferedWriter}
import java.net.URL

import io.plasmap.model.OsmObject
import io.plasmap.parser.OsmParser
import nak.NakContext._
import nak.core.{FeaturizedClassifier, IndexedClassifier}
import nak.data.Example
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.scraper.ContentExtractors._
import org.jsoup.nodes.{Element, Document}
import nak.NakContext._
import nak.core._
import nak.data.Example
import java.io.{BufferedWriter, File, FileWriter}
import java.net.URL

import io.plasmap.parser.OsmParser
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.jsoup.nodes.{Document, Element}
import scala.reflect.io.Directory
import scala.util.Try

/**
 * Created by erna on 11/2/15.
 */
object Predict {
  def check(tags: Map[String, String], amenity: String, missingKey: String, contain: String): Option[String] = {

    if (tags.toList.contains("amenity" -> amenity) && tags.get(missingKey).isEmpty) {
      tags.get(contain)
    } else None
  }

  def getLinks(doc: Document): List[String] = {
    val linksOpt = doc >?> elementList("a") >> attr("href")("a")
    linksOpt.getOrElse(Nil)
  }

  def scrape(website: String): String = {

    val link: String = url(website)
    val browser = new Browser
    val docOpt: Option[Document] = Try(browser.get(link)).toOption
    var texts = ""
    docOpt.fold(())(doc => {
          //texte auslesen
          readText(doc)


      val links: List[String] = getLinks(doc)
      val text2 = for (l <- links.distinct) yield  (scrapeURL(l, link)).mkString
      texts=text+" "+text2
      })
    texts
  }

  def url(website: String): String = {
    val website2 = website.trim()
    val browser = new Browser
    if (!website2.startsWith("http")) {
      "http://" + website2
    }
    else {
      website2
    }
  }

  def readText(doc: Document): String = {
    val html: Option[Element] = doc >?> element("html")
    html.map(x => {
      recursiveGet(x)
    }).getOrElse("")
  }

  def recursiveGet(elem: Element): String = {
    val otherText = for (child <- elem.children)
      yield recursiveGet(child)

    elem.text + " " + otherText.mkString(" ")
  }

  def getBaseUrl(link: String): Option[String] = Try {
    val url = new URL(link)
    url.getProtocol + "://" + url.getHost
  }.toOption



  def scrapeURL(link: String, baselink: String): String = {
    def readFromFile(uri: String): String = {
      val browser = new Browser
      val docOpt: Option[Document] = Try(browser.get(uri)).toOption
      var text=""
      docOpt.fold(())(doc => {
          //texte auslesen

          text=readText(doc)

      })
      text
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

  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def run(web:String, t:String): String = {

    val missingTag = t match {
      case _ => "cuisine"
    }

    println("scraping...")
    val text = scrape(web)


    println("loading...")
    val classifier = loadClassifier[IndexedClassifier[String] with FeaturizedClassifier[String, String]]("classifier")

    println("predicting...")
    val result = classifier.predict(text.mkString)
    result

  }
}
