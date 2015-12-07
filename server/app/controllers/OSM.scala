package controllers


import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.oauth.{RequestToken, ConsumerKey, ServiceInfo, OAuth}
import play.api.mvc._
import prediction.{OnlineVersion, Predict, EvaluateClassifier}
import scala.concurrent.{Await, Future}




/**
 * Created by erna on 11/24/15.
 */
object OSM extends Controller {

  val Key = ConsumerKey("xxxx", "xxxx")

  val Osm = OAuth(ServiceInfo(

    "http://master.apis.dev.openstreetmap.org/oauth/request_token",
    "http://master.apis.dev.openstreetmap.org/oauth/access_token",
    "http://master.apis.dev.openstreetmap.org/oauth/authorize", Key),
    true)

  def authenticate = Action { request =>
    request.getQueryString("oauth_verifier").map { verifier =>
      val tokenPair = sessionTokenPair(request).get
      // We got the verifier; now get the access token, store it and back to index
      Osm.retrieveAccessToken(tokenPair, verifier) match {
        case Right(t) => {
          // We received the authorized tokens in the OAuth object - store it before we proceed
          Redirect(routes.Application.index).withSession("token" -> t.token, "secret" -> t.secret)
        }
        case Left(e) => throw e
      }
    }.getOrElse{
        val token = Osm.retrieveRequestToken("http://kikimora.plasmap.io:9000/")
        token match {
          case Right(t) => {
            // We received the unauthorized tokens in the OAuth object - store it before we proceed
            Redirect(Osm.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
          }
          case Left(e) => throw e
        }}
  }

  def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }
}