import com.sun.deploy.xml.XMLParser
import play.api.libs.json._
import play.libs.XML

import scala.io.Source

"aaaaaaa"
val userInfo = "<osm version=\"0.6\" generator=\"OpenStreetMap server\">\n <user display_name=\"Max Muster\" account_created=\"2006-07-21T19:28:26Z\" id=\"1234\">\n   <contributor-terms agreed=\"true\" pd=\"true\"/>\n   <img href=\"http://www.openstreetmap.org/attachments/users/images/000/000/1234/original/someLongURLOrOther.JPG\"/>\n   <roles></roles>\n   <changesets count=\"4182\"/>\n   <traces count=\"513\"/>\n   <blocks>\n       <received count=\"0\" active=\"0\"/>\n   </blocks>\n   <home lat=\"49.4733718952806\" lon=\"8.89285988577866\" zoom=\"3\"/>\n   <description>The description of your profile</description>\n   <languages>\n     <lang>de-DE</lang>\n     <lang>de</lang>\n     <lang>en-US</lang>\n     <lang>en</lang>\n   </languages>\n   <messages>\n     <received count=\"1\" unread=\"0\"/>\n     <sent count=\"0\"/>\n   </messages>\n </user>\n</osm>"
val xml = XML.fromString(userInfo)
val user = xml.getElementsByTagName("user")
xml.getXmlVersion
println(user.item(0).getAttributes.getNamedItem("id"))