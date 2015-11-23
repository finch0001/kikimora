import prediction.Predict._
import scala.io.Source
import scalaz.\/
val link = "http://www.heise.de"
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
getLinksFromRobots(link)