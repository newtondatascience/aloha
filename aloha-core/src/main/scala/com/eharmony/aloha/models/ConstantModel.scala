package com.eharmony.aloha.models

import com.eharmony.aloha.id.{ModelId, ModelIdentity}
import com.eharmony.aloha.score.Scores.Score
import com.eharmony.aloha.score.conversions.ScoreConverter
import com.eharmony.aloha.score.basic.ModelOutput
import com.eharmony.aloha.factory.{ModelParser, BasicModelParser, ParserProviderCompanion}
import spray.json.{DeserializationException, JsValue, JsonReader}

case class ConstantModel[+B: ScoreConverter](constant: ModelOutput[B], modelId: ModelIdentity) extends BaseModel[Any, B] {
    private[this] val Seq(w, wo) = Seq(true, false).map(b => scoreTuple(constant)(b, implicitly[ScoreConverter[B]]))
    private[aloha] final def getScore(a: Any)(implicit audit: Boolean): (ModelOutput[B], Option[Score]) = if (audit) w else wo
}

object ConstantModel extends ParserProviderCompanion {
    def apply[B: ScoreConverter](b: B): ConstantModel[B] = ConstantModel(ModelOutput(b), ModelId())

    object Parser extends BasicModelParser {
        val modelType = "Constant"
        private val valueField = "value"

        def modelJsonReader[A, B: JsonReader : ScoreConverter]: JsonReader[ConstantModel[B]] = new JsonReader[ConstantModel[B]] {
            def read(json: JsValue) = {
                val model = for {
                    jsV <- json(valueField)
                    mId <- getModelId(json)
                    v = jsV.convertTo[B]
                    m = new ConstantModel[B](ModelOutput(v), mId)
                } yield m

                model getOrElse { throw new DeserializationException("") }
            }
        }
    }

    def parser: ModelParser = Parser
}
