package controllers

import java.io.{File, PrintWriter}

import io.plasmap.model.OsmNode
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.oauth._
import play.api.libs.ws.{WSAuthScheme, WS}
import play.api.mvc._
import play.api.Play.current
import prediction.{OnlineVersion, Predict, EvaluateClassifier}
import scala.concurrent.{Await, Future}
import upickle.default._

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
          println(id)
          Ok(write((tags,id)))
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
    request ⇒ {
      request.body.asText.map(
        (text:String) => {
          val (username, password) = upickle.default.read[(String,String)](text)
          println("HAHAHAHAHAHAHAHAHA\n\n\n\n\n")
          WS.url("http://api06.dev.openstreetmap.org/api/0.6/permissions")
          .withAuth(username, password, WSAuthScheme.BASIC)
          .get
            .map(r => {println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n\n\n\n"+r.body+"\n\n\n\n\n\n"); r})
          .map(result => Ok(write(result.body)).withSession("username" -> username, "password"-> password))
        }).getOrElse(
          Future.successful(Redirect(routes.Application.login()))
        )
    }
  }

  def save() = Action.async {
     request => {
       request.body.asText.map(
         (test:String) => {
       val userData = request.session.data
       val username: String = userData.getOrElse("username","")
       val userpass: String = userData.getOrElse("password","")
           println(username + userpass)
          val writer = new PrintWriter(new File("changeset" ))
          writer.write(OnlineVersion.createChangeset("test", "Kikimora"))
          writer.close()
          println("\n\n\nLALALA")
          WS.url("http://api06.dev.openstreetmap.org/api/0.6/changeset/create")
          .withAuth(username,userpass, WSAuthScheme.BASIC)
          .put(new File("changeset"))
            .map(r=>{
            println("\n\n\nLALALA"+r.body); r})
          .map(result => Ok(write(result.body)))
        }
      ).getOrElse({println("\n\n error1");Future.successful(Redirect(routes.Application.index()))})
    }
  }
}
