/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.foreignReliefs.retrieve.model.response

import play.api.libs.json.{Json, OWrites, Reads}
import shared.models.domain.Timestamp
import v2.foreignReliefs.retrieve.def1.model.response.{Def1_ForeignIncomeTaxCreditRelief, Def1_ForeignTaxCreditRelief, Def1_ForeignTaxForFtcrNotClaimed}

sealed trait RetrieveForeignReliefsResponse

object RetrieveForeignReliefsResponse {

  implicit val writes: OWrites[RetrieveForeignReliefsResponse] = OWrites[RetrieveForeignReliefsResponse] {
    case def1: Def1_RetrieveForeignReliefsResponse => Json.toJsObject(def1)
  }

}

case class Def1_RetrieveForeignReliefsResponse(
    submittedOn: Timestamp,
    foreignTaxCreditRelief: Option[Def1_ForeignTaxCreditRelief],
    foreignIncomeTaxCreditRelief: Option[Seq[Def1_ForeignIncomeTaxCreditRelief]],
    foreignTaxForFtcrNotClaimed: Option[Def1_ForeignTaxForFtcrNotClaimed]
) extends RetrieveForeignReliefsResponse {
  implicit val reads: Reads[Def1_RetrieveForeignReliefsResponse] = Json.reads[Def1_RetrieveForeignReliefsResponse]
}

object Def1_RetrieveForeignReliefsResponse {

  implicit val reads: Reads[Def1_RetrieveForeignReliefsResponse]    = Json.reads[Def1_RetrieveForeignReliefsResponse]
  implicit val writes: OWrites[Def1_RetrieveForeignReliefsResponse] = Json.writes[Def1_RetrieveForeignReliefsResponse]

}
