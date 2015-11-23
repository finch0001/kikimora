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
object Foundation {
  def basic(children:ReactTag*) = {
    <.div( ^.cls := "row")(
      <.div( ^.cls := "small-12 large-4 large-offset-4 columns")(
       children
      )
    )
  }

  def editorView(left: ReactTag, right: ReactTag) = {
    <.div(^.cls := "row fullWidth")(
      <.div(^.cls := "small-12 large-3 columns")(
        left
      ),
      <.div(^.cls := "small-12 large-9 columns")(
        right
      )
    )
  }
}
