package prediction

import nak.core.{FeaturizedClassifier, IndexedClassifier}
import nak.NakContext._
import java.io.File
import java.net.URL

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

import scalaz.\/

//import robots.protocol.exclusion.robotstxt.RobotstxtParser
//import robots.protocol.exclusion.html.Page
import scala.io.Source
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
    val linksOpt:Option[List[String]] = doc >?> elementList("a") >> attr("href")("a")
    linksOpt.getOrElse(Nil)
  }

  def scrape(website: String): String = {

    val link: String = url(website)
    val browser = new Browser
    val docOpt: Option[Document] = Try(browser.get(link)).toOption

    docOpt.map(doc => {
    //texte auslesen
      val text = readText(doc)
      val disallowList = getLinksFromRobots(getBaseUrl(link).getOrElse(""))
      println(disallowList)

      val links: List[String] = getLinks(doc).distinct.filterNot(disallowList.toSet)
      links.foldLeft(text)((texts, l) => texts + scrapeURL(l, link).mkString
        )
    }).getOrElse("")
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
    import scalaz._, Scalaz._
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

  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

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
