package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import prediction.{OnlineVersion, Predict, EvaluateClassifier}
import scala.concurrent.Future
import upickle.default._
import scala.io.Source._

/**
 * Created by mark on 04.08.15.
 */
object Application extends Controller {

  def index() = Action.async {
    request ⇒ Future(Ok(views.html.index()))
  }

  def loadform() = Action.async {
    request ⇒ Future(Ok(views.html.index()))
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
}
