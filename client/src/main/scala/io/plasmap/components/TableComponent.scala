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

  case class TableState(cuisine:String, website:String, additionalRows:Int)
  case class TableProps(tags: (Map[String, String], String))

  def predictButtonClicked(website:Option[String], mod: (String) => Callback):Callback = {
    website.fold(Callback.empty)(
      FromServer.predict(_, "cuisine", result => mod(result))
    )
  }

  val component = ReactComponentB[TableProps]("Table Component")
    .initialState(TableState("","",0))
    .render((scope ) ⇒ {
      val props = scope.props
      val state = scope.state
        val table = <.table(^.width := "100%")(
          <.tr(
            <.td("id"),
            <.td(props.tags._2)
          ),
          <.tr(
            <.td("name"),
            <.td(props.tags._1.get("name").map(x => <.div(x)).getOrElse(<.input(^.tpe := "text")))
          ),
          <.tr(
            <.td("addr:city"),
            <.td(props.tags._1.get("addr:city").map(x => <.div(x)).getOrElse(<.input(^.tpe := "text")))
          ),
          <.tr(
            <.td("website"),
            <.td(props.tags._1.get("website").map(x => <.div(<.a(^.href:=x)(x))).getOrElse(<.input(^.tpe := "text", ^.onChange ==> ((e:ReactEventI) => scope.modState(_.copy(website = e.target.value))))))
          ),
          <.tr(
            <.td("cuisine"),
            <.td(props.tags._1.get("cuisine").map(x => <.div(x)).getOrElse(
              if (state.cuisine == "") {
                <.button(^.onClick -->
                  predictButtonClicked(
                    props.tags._1.get("website"),
                    (result) => scope.modState(_.copy(cuisine = result))
                  )
                )("predict")
              }
              else {
                <.select(^.selected := true)(
                  for (cuisine <- Cuisines.cuisines) yield {
                    if (cuisine == state.cuisine) {
                      <.option(^.value := cuisine, ^.selected := true)(cuisine)
                    }
                    else {
                      <.option(^.value := cuisine)(cuisine)
                    }
                  }
                )
              }
            ))
          ),
          for ((key, value) <- props.tags._1.filterKeys(Set("name", "cuisine", "addr:city","website").contains(_)==false)) yield {
            <.tr(
              <.td(key),
              <.td(value)
            )
          },
          for (x <- 0 to state.additionalRows) yield {
            <.tr(
              <.td(<.input(^.tpe := "text")),
              <.td(<.input(^.tpe := "text"))
            )
          },
          <.tr(
            <.td(
              <.button(^.onClick --> scope.modState(_.copy(additionalRows = state.additionalRows+1)))("+")
            ),
            <.td()
          ),
          <.tr(
            <.td(),
            <.td(
              <.button(^.onClick --> Callback{
                Ajax.get("/save").onComplete {
                  case Success(s) => {
                    println(s.responseText)
                  }
                  case Failure(f) => println("Tja")
                }
              })("send")
            )
          )
        )
        val iframe = <.iframe( ^.width := "100%", ^.height := "800px", ^.src := state.website )
      Foundation.editorView(table, iframe)
    })
    .componentWillMount( cwu =>
      if(cwu.props.tags._1.contains("website")) {
        val y = cwu.props.tags._1.get("website").get
        println(y)
        cwu.modState(_.copy(website = y))
      }
      else cwu.modState(identity)
    )
  .build

}
