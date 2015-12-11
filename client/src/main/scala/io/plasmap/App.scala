package io.plasmap

//import io.plasmap.components.PredictionComponent.PredictionProps
import io.plasmap.components.{PredictionComponent, Tabelle}
import japgolly.scalajs.react.{ReactDOM, React}
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
    val byId = dom.document.getElementById _
    val containerNode = byId("react-container")

    Ajax.get("/").onComplete{
      case Success(s) => {
        ReactDOM.render(PredictionComponent.component(), containerNode)
      }
      case Failure(f) => println("Tja")
    }
  }
}
