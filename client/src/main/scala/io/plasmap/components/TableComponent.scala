package io.plasmap.components

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.CompScope.DuringCallbackU
import japgolly.scalajs.react.ScalazReact.ReactS
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.ScalazReact._
import org.scalajs.dom.ext.Ajax
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import org.scalajs.dom

/**
  * Created by mark on 10.11.15.
  */
object TableComponent {

  case class TableState(cuisine:String)

  def predictButtonClicked(website:Option[String], mod: (String) => Callback):Callback = {
    website.fold(Callback.empty)(
      FromServer.predict(_, "cuisine", result => mod(result))
    )
  }

  val component = ReactComponentB[Map[String,String]]("Table Component")
    .initialState(TableState(""))
    .render((scope ) ⇒ {
      val props = scope.props
      val state = scope.state
      <.table(
        if(!props.contains("cuisine"))
        {
          <.div(
            <.tr(
              <.td("cuisine"),
              <.td(
                if(state.cuisine == "") {
                  <.button(^.onClick -->
                    predictButtonClicked(
                      props.get("website"),
                      (result) => scope.modState( _.copy(cuisine = result) )
                    )
                  )("predict")
                }
                else {
                  <.select(^.selected := true)(
                    for(cuisine <- Cuisines.cuisines) yield {
                      if (cuisine == state.cuisine) {
                        <.option(^.value := cuisine, ^.selected := true)(cuisine)
                      }
                      else {
                        <.option(^.value := cuisine)(cuisine)
                      }
                    }
                  )
                }
              )
            ),
            for ((key, value) <- props) yield {
              <.tr(
                <.td(key),
                <.td(value)
              )
            },
            <.tr(
              <.td(),
              <.td(
                <.button(^.onClick --> Callback.log("PAM"))("send")
              )
            )
          )
        }
        else {
          <.div(
            for ((key, value) <- props) yield {
              <.tr(
                <.td(key),
                <.td(value)
              )
            },
            <.tr(
              <.td(),
              <.td(
                <.button(^.onClick --> Callback.log("PAM"))("send")
              )
            )
          )
        }
      )
    }).build

}
