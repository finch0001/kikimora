package io.plasmap.components

import japgolly.scalajs.react.ScalazReact.ReactS
import japgolly.scalajs.react.{ReactEvent, ReactEventI, ReactComponentB, React}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.ScalazReact._

/**
 * Created by erna on 10/30/15.
 */
object Tabelle {

  val component = ReactComponentB[List[(String, String)]]("Component holding a String")
    .stateless
    .render( scope ⇒ {
    val props = scope.props
    val state = scope.state
    <.table()(
    for((cuisine, predicted) <- props) yield {
      <.tr(
        <.td(cuisine),
        <.td(predicted)
      )
    }
    )}
    ).build

}
