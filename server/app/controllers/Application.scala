package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.oauth._
import play.api.libs.ws.WS
import play.api.mvc._
import play.api.Play.current
import prediction.{OnlineVersion, Predict, EvaluateClassifier}
import scala.concurrent.{Await, Future}
import upickle.default._
import scala.io.Source._
/**
 * Created by mark on 04.08.15.
 */
object Application extends Controller {

  def index() = Action.async {
    request ⇒ Future{
      val loginOpt = for {
        token <- request.session.get("token")
        secret <- request.session.get("secret")
      } yield (token, secret)
      loginOpt.map{
        case (t,s) => Ok(views.html.index(t,s))
        }.getOrElse(Redirect(routes.OSM.authenticate))
    }
  }

  def loadform() = Action.async {
    request ⇒ Future(Ok(views.html.index("","")))
  }

  def getdata() = Action.async {
    request ⇒ Future{
      request.body.asText.map(
        (text:String) => {
          val id = upickle.default.read[String](text)
          val tags: Map[String, String] = OnlineVersion.getXML(id).toMap
          Ok(write(tags))
        }).getOrElse(
          BadRequest("Bad Request")
        )
    }
  }

  def predict() = Action.async {
    request ⇒ Future {
      request.body.asText.map(
        (text:String) => {
          val (web, t) = upickle.default.read[(String,String)](text)
          val result: String = Predict.run(web,t)
          Ok(write(result))
        }).getOrElse(
          BadRequest("Bad Request")
        )
    }
  }

  def login() = Action.async {
    implicit request =>
      OSM.sessionTokenPair match {
        case Some(credentials) => {
          WS.url("http://api.openstreetmap.org/api/0.6/capabilities")
            .sign(OAuthCalculator(OSM.Key, credentials))
            .get
            .map(r => {println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n\n\n\n"+r.body+"\n\n\n\n\n\n"); r})
            .map(result => Ok(result.body))
        }
        case _ => Future.successful(Redirect(routes.OSM.authenticate))
      }
  }

  def save() = Action.async {
    implicit request => {
      OSM.sessionTokenPair.map( credentials =>
        {
          WS.url(/*"http://api.openstreetmap.org/api/0.6/node/#id"*/"http://api.openstreetmap.org/api/0.6/capabilities")
            .sign(OAuthCalculator(OSM.Key, credentials))
            .get
            .map(r => {println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n\n\n\n"+r.body+"\n\n\n\n\n\n"); r})
            .map(result => Ok(result.body))
        }).getOrElse(Future.successful(Redirect(routes.OSM.authenticate)))
    }
  }
}
