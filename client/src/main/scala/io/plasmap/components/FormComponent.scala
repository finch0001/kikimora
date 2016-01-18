package io.plasmap.components

import japgolly.scalajs.react.{CallbackTo, ReactComponentB, Callback}
import org.scalajs.dom.ext.Ajax
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
  * Created by erna on 10.11.15.
  */
object FormComponent {
  case class FormState(id:String, list: List[(String,String)])
  case class FormProps(update:((Map[String,String],String,(List[String],List[String],List[String]))) => Callback)

  val component = ReactComponentB[FormProps]("Form Component")
    .initialState(FormState("3891486472",Nil))
    .render( scope ⇒ {
      val props = scope.props
      val state = scope.state
      
      Foundation.basic(
        <.div( ^.cls := "row collapse")(
          <.div( ^.cls := "small-8 columns")(
            <.input(
              ^.tpe := "text",
              ^.value := state.id,
              ^.onChange ==> ((e: ReactEventI) => scope.modState(c => c.copy(id = e.target.value)))
            )
          ),
          <.div( ^.cls := "small-4 columns")(
            <.button(
              ^.cls := "postfix",
              ^.onClick --> FromServer.osmData(state.id, props.update)
            )("Edit Node")
          )
        )
      )
    }).build


}
