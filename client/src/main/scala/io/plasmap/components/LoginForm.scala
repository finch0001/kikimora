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
 * Created by mark on 10.11.15.
 */
object LoginForm {
  case class LoginFormState(username: String, password: String)
  case class LoginFormProps(update:(String) => Callback)

  val component = ReactComponentB[LoginFormProps]("Form Component")
    .initialState(LoginFormState("",""))
    .render( scope ⇒ {
    val props = scope.props
    val state = scope.state

    Foundation.basic(
      <.div( ^.cls := "row collapse")(
        <.div( ^.cls := "small-8 columns")(
          <.input(
            ^.tpe := "text",
            ^.onChange ==> ((e: ReactEventI) => scope.modState(c => c.copy(username = e.target.value)))
          )
        ),
        <.div( ^.cls := "small-8 columns")(
          <.input(
            ^.tpe := "password",
            ^.onChange ==> ((e: ReactEventI) => scope.modState(c => c.copy(password = e.target.value)))
          )
        ),
        <.div( ^.cls := "small-4 columns")(
          <.button(
            ^.cls := "postfix",
            ^.onClick --> FromServer.login(state.username, state.password, props.update)
          )("log in")
        )
      )
    )
  }).build
}
