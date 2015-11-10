package io.plasmap.components

import japgolly.scalajs.react.{CallbackTo, Callback}
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.XMLHttpRequest
import scalaz.syntax.id._
import upickle.default.read
import upickle.default.write
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
  * Created by mark on 10.11.15.
  */
object FromServer {

  private def post[I, O](serialise: I => String, deserialise: String => O)
                        (url:String, input:I, callback: O => Callback):Callback = {
    val fut = Ajax
      .post(url, serialise(input))
      .map(_.responseText |> deserialise.andThen(callback) )
    (CallbackTo future fut).void
  }

  def osmData(id:String, callback: (Map[String, String]) => Callback) = {
    post(write[String], read[Map[String, String]])(
      "/getosmdata", id, callback
    )
  }

  def predict(website:String, category:String, callback: (String) => Callback) = {
    post(write[(String, String)], read[String])(
      "/predict", (website, category), callback
    )
  }
}
