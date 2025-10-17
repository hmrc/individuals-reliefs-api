/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v3.charitableGiving.retrieve.model.response

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v3.charitableGiving.retrieve.def1.model.response.{Def1_GiftAidPayments, Def1_Gifts}
import v3.charitableGiving.retrieve.def2.model.response.{Def2_GiftAidPayments, Def2_Gifts}

sealed trait RetrieveCharitableGivingReliefsResponse {
  def retrieveCharitableGivingReliefResponse: RetrieveCharitableGivingReliefsResponse
}

object RetrieveCharitableGivingReliefsResponse {

  implicit val reads: Reads[RetrieveCharitableGivingReliefsResponse] = {

    val def2Reads: Reads[RetrieveCharitableGivingReliefsResponse] =
      Json.reads[Def2_RetrieveCharitableGivingReliefsResponse].map(identity)

    val def1Reads: Reads[RetrieveCharitableGivingReliefsResponse] =
      Def1_RetrieveCharitableGivingReliefsResponse.reads.map(identity)

    def1Reads orElse def2Reads
  }

  implicit val writes: OWrites[RetrieveCharitableGivingReliefsResponse] = {
    case def1: Def1_RetrieveCharitableGivingReliefsResponse => Json.toJsObject(def1)
    case def2: Def2_RetrieveCharitableGivingReliefsResponse => Json.toJsObject(def2)
  }

}

case class Def1_RetrieveCharitableGivingReliefsResponse(giftAidPayments: Option[Def1_GiftAidPayments], gifts: Option[Def1_Gifts])
    extends RetrieveCharitableGivingReliefsResponse {

  implicit val reads: Reads[Def1_RetrieveCharitableGivingReliefsResponse]                  = Json.reads[Def1_RetrieveCharitableGivingReliefsResponse]
  def retrieveCharitableGivingReliefResponse: Def1_RetrieveCharitableGivingReliefsResponse = this
}

object Def1_RetrieveCharitableGivingReliefsResponse {
  implicit val writes: OWrites[Def1_RetrieveCharitableGivingReliefsResponse] = Json.writes

  implicit val reads: Reads[Def1_RetrieveCharitableGivingReliefsResponse] = {
    val defaultReads: Reads[Def1_RetrieveCharitableGivingReliefsResponse] = Json.reads

    val ifsReads = (JsPath \ "charitableGivingAnnual").read(defaultReads)

    ifsReads orElse defaultReads
  }

}

case class Def2_RetrieveCharitableGivingReliefsResponse(
    giftAidPayments: Option[Def2_GiftAidPayments],
    gifts: Option[Def2_Gifts]
) extends RetrieveCharitableGivingReliefsResponse {
  implicit val reads: Reads[Def2_RetrieveCharitableGivingReliefsResponse]                  = Json.reads[Def2_RetrieveCharitableGivingReliefsResponse]
  def retrieveCharitableGivingReliefResponse: Def2_RetrieveCharitableGivingReliefsResponse = this
}

object Def2_RetrieveCharitableGivingReliefsResponse {
  implicit val writes: OWrites[Def2_RetrieveCharitableGivingReliefsResponse] = Json.writes
  implicit val reads: Reads[Def2_RetrieveCharitableGivingReliefsResponse]    = Json.reads
}
