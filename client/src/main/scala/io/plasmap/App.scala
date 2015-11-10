package io.plasmap

import io.plasmap.components.{PredictionComponent, Tabelle}
import japgolly.scalajs.react.React
import org.scalajs.dom.ext.Ajax

import scala.scalajs.js
import scala.util.{Failure, Success}
import scalaz.effect.IO
import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global
import upickle.default._

/**
 * The main App.
 */
object App extends js.JSApp {
  def main():Unit = {
    val containerNode = dom.document.getElementById("react-container")
/*    Ajax.get("/predict").onComplete{
      case Success(s) =>
        React.render(Tabelle.component(read[List[(String, String)]](s.responseText)), containerNode)
      case Failure(f) => println("Tja")
    }
*/
    Ajax.get("/").onComplete{
      case Success(s) => {
        React.render(PredictionComponent.component(), containerNode)
      }
      case Failure(f) => println("Tja")
    }
  }
}
