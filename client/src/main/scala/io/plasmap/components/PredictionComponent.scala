package io.plasmap.components

import io.plasmap.components.FormComponent.FormProps
import io.plasmap.components.TableComponent.TableProps
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.ScalazReact._

/**
 * Created by erna on 11/3/15.
 */
object PredictionComponent {

  case class State(
                    formId: Int,
                    id: String,
                    tags: Map[String, String]
                    )
  case class PredictionProps(
                            oauthToken: String,
                            oauthVerifier: String
                              )
  val component = ReactComponentB[PredictionProps]("Prediction Component")
    .initialState(State(1, "", Map.empty))
    .renderPS((scope, props, state) ⇒ {

    def updateTags(tags: (Map[String,String],String)): Callback = scope.modState(_.copy(tags=tags._1, id=tags._2, formId=2))
    <.div(
      if (state.formId == 1) FormComponent.component(FormProps(updateTags))

      else TableComponent.component(TableProps(state.tags, state.id))
    )

  }).build

}
