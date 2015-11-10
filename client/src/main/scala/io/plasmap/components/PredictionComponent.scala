package io.plasmap.components

import japgolly.scalajs.react.ScalazReact.ReactS
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.ScalazReact._
import org.scalajs.dom.ext.Ajax
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import org.scalajs.dom

/**
 * Created by erna on 11/3/15.
 */
object PredictionComponent {

  val cuisines = List(
    "african",
    "capixaba",
    "fried_food",
    "malagasy",
    "savory_pancakes",
    "american",
    "caribbean",
    "friture",
    "mediterranean",
    "seafood",
    "arab",
    "casserole",
    "gaucho",
    "mexican",
    "shandong",
    "argentinian",
    "chicken",
    "german",
    "mineira",
    "sichuan",
    "asian",
    "chinese",
    "greek",
    "noodle",
    "spanish",
    "australian",
    "coffee_shop",
    "gyro",
    "okinawa_ryori",
    "steak_house",
    "bagel",
    "couscous",
    "hunan",
    "pakistani",
    "sub",
    "baiana",
    "crepe",
    "hungarian",
    "pancake",
    "sushi",
    "balkan",
    "croatian",
    "ice_cream",
    "pasta",
    "tapas",
    "barbecue",
    "curry",
    "indian",
    "peruvian",
    "thai",
    "basque",
    "czech",
    "international",
    "pie",
    "turkish",
    "bavarian",
    "danish",
    "iranian",
    "pizza",
    "vegan",
    "belarusian",
    "dessert",
    "italian",
    "polish",
    "vegetarian",
    "bolivian",
    "donut",
    "japanese",
    "portuguese",
    "vietnamese",
    "bougatsa",
    "doughnut",
    "kebab",
    "regional",
    "westphalian",
    "brazilian",
    "empanada",
    "korean",
    "rhenish",
    "wings",
    "burger",
    "fish",
    "kyo_ryouri",
    "russian",
    "cake",
    "fish_and_chips",
    "latin_american",
    "sandwich",
    "cantonese",
    "french",
    "lebanese",
    "sausage"
  )

  case class State(
                    formId: Int,
                    id: String,
                    list: List[(String, String)]
                    )
  val component = ReactComponentB[Unit]("Prediction Component")
    .initialState(State(1, "", Nil))
    .renderPS((scope, props, state) ⇒ {

    def updateList(l: List[(String,String)]): Unit = scope.modState(_.copy(list=l,formId=2))
    <.div(
      if (state.formId == 1) formComponent(FormProps(updateList))

      else tableComponent(state.list)
    )

  }).buildU

  case class FormState(id:String, list: List[(String,String)])
  case class FormProps(update:List[(String,String)] => Unit)

  val formComponent = ReactComponentB[FormProps]("Form Component")
    .initialState(FormState("",Nil))
    .render( scope ⇒ {
    val props = scope.props
    val state = scope.state
    <.div(
      <.input(
        ^.tpe := "text",
        ^.onChange ==> ((e: ReactEventI) => scope.modState(c => c.copy(id = e.target.value)))
      ),
      <.button(
        ^.onClick ==> ((ev: ReactEventI) => Callback{
          val fut = Ajax.post("/getosmdata", upickle.default.write(state.id))
          fut.onComplete {
            case Success(su) =>
              val response = su.responseText
              val elements = upickle.default.read[List[(String, String)]](response)
              props.update(elements)
            case Failure(fa) => println("Tja")
          }
        } )
      )("Click me!")
    )
  }).build

  case class TableState(cuisine:String)
  val tableComponent = ReactComponentB[List[(String,String)]]("Table Component")
    .initialState(TableState(""))
    .render( scope ⇒ {
    val props = scope.props
    val state = scope.state
    <.table(
      if(!props.exists(_._1 == "cuisine"))
        {
          <.div(
            <.tr(
              <.td("cuisine"),
              <.td(
                if(state.cuisine == "") {
                  <.button(^.onClick ==> ((e: ReactEventI) => Callback{
                    props.toMap.get("website").foreach( ws =>{
                      val fut = Ajax.post("/predict", upickle.default.write((ws, "cuisine")))
                      fut.onComplete {
                        case Success(su) =>
                          val response = su.responseText
                          val result = upickle.default.read[String](response)
                          scope.modState(_.copy(cuisine = result)).runNow()
                        case Failure(fa) => println("Tja")
                      }
                    })

                  }))("predict")
                }
                else {
                  <.select(^.selected := true)(
                    for(cuisine <- cuisines) yield {
                      if (cuisine == state.cuisine) {
                        println(cuisine+" " + state.cuisine)
                        <.option(^.value := cuisine, ^.selected := true)(cuisine)
                      }
                      else {
                        println(cuisine)
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
