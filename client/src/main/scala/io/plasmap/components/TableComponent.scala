package io.plasmap.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.CompScope.DuringCallbackU
import japgolly.scalajs.react.ScalazReact.ReactS
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.ScalazReact._
import monocle.macros.GenLens
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.NodeList
import scala.collection.immutable.IndexedSeq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import org.scalajs.dom

/**
  * Created by mark on 10.11.15.
  */
object TableComponent {

  case class TableState(tags:List[(String, String)], id:String, website: String)
  case class TableProps(tags: (Map[String, String], String),printAnswer:String => Callback)

  val tags = GenLens[TableState](_.tags)
  val changeKey = (index:Int, key:String) => tags.modify(
    l => l.updated(index, (key, l(index)._2))
  )
  val changeValue = (index:Int, value:String) => tags.modify(
    l => l.updated(index, (l(index)._1, value))
  )

  def predictButtonClicked(website:Option[String], mod: (String) => Callback):Callback = {
    website.fold(Callback.empty)(
      FromServer.predict(_, "cuisine", result => mod(result))
    )
  }

  val component = ReactComponentB[TableProps]("Table Component")
    .initialState(TableState(Nil,"",""))
    .render((scope ) ⇒ {
      val props = scope.props
      val state = scope.state
      def inputsForKV(k:String, v:String, index:Int)= {
        val finiteSetKeys = Map(
          "cuisine" -> Cuisines.cuisines
        )
        def finite(current:String, options:List[String]): ReactTag = {
          <.tr(
            <.td(
              <.input(
                ^.tpe := "text",
                ^.value := k,
                ^.onChange ==> ((e:ReactEventI) => scope.modState(changeKey(index, e.target.value)))
              )
            ),
            <.td(
              if(current!="") {
                <.select(
                  ^.onChange ==> ((e:ReactEventI) => scope.modState(changeValue(index, e.target.value)))
                )(
                    for (o <- options) yield {
                      if (o == current) {
                        <.option(^.value := o, ^.selected := true)(o)
                      }
                      else {
                        <.option(^.value := o)(o)
                      }
                    }
                  )
              }
              else {
                <.button(^.onClick -->
                  predictButtonClicked(
                    props.tags._1.get("website"),
                    (result) => scope.modState(changeValue(index,result))
                  )
                )("predict")
              }
            )
          )
        }
        def regular(value:String): ReactTag = {
          <.tr(
            <.td(
              <.input(
              ^.tpe := "text",
              ^.value := k,
              ^.onChange ==> ((e:ReactEventI) => scope.modState(changeKey(index, e.target.value)))
              )
            ),
            <.td(
              <.input(
                ^.tpe := "text",
                ^.value := v,
                ^.onChange ==> ((e:ReactEventI) => scope.modState(changeValue(index, e.target.value)))
              )
            )
          )
        }
        finiteSetKeys.get(k).fold(regular(v))(options => finite(v, options))
      }
    val table = <.table(^.width := "100%")(
        <.tbody(
          <.tr(
            <.td("id"),
            <.td(state.id)
          ),
          for ( ((k,v), index) <- state.tags.zipWithIndex) yield {
            inputsForKV(k,v,index)
          },
          <.tr(
            <.td(
              <.button(^.onClick --> scope.modState(tags.modify(_ :+ ("", ""))))("+")
            ),
            <.td()
          ),
          <.tr(
            <.td(),
            <.td(
              <.button(^.onClick --> {
                val tagsToSend: Map[String, String] = (state.tags:+("id",state.id)).toMap
                FromServer.save(tagsToSend,props.printAnswer)
              })("send")
            )
          )
        )
        )
        val iframe = <.iframe( ^.width := "100%", ^.height := "800px", ^.src := state.tags.toMap.getOrElse("website","") )
      Foundation.editorView(table, iframe)
    })
    .componentWillMount( cwm => {
      def setTags(s:TableState):TableState =

        s.copy(tags =
          if(cwm.props.tags._1.contains("cuisine")) cwm.props.tags._1.-("id").toList
          else cwm.props.tags._1.-("id").toList:+("cuisine","")
          , id=cwm.props.tags._2)

      if(cwm.props.tags._1.contains("website")) {
        val y = cwm.props.tags._1.get("website").get
        println(y)
        cwm.modState(x => setTags(x).copy(website = y))
      }
      else cwm.modState(setTags)
    })
  .build

}
