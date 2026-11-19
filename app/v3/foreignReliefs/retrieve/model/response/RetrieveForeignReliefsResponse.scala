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

package v3.foreignReliefs.retrieve.model.response

import api.models.domain.Timestamp
import play.api.libs.json.*

case class RetrieveForeignReliefsResponse(
    submittedOn: Timestamp,
    foreignTaxCreditRelief: Option[ForeignTaxCreditRelief],
    foreignIncomeTaxCreditRelief: Option[Seq[ForeignIncomeTaxCreditRelief]],
    foreignTaxForFtcrNotClaimed: Option[ForeignTaxForFtcrNotClaimed]
) {
  implicit val reads: Reads[RetrieveForeignReliefsResponse] = Json.reads[RetrieveForeignReliefsResponse]
}

object RetrieveForeignReliefsResponse {

  implicit val reads: Reads[RetrieveForeignReliefsResponse]    = Json.reads[RetrieveForeignReliefsResponse]
  implicit val writes: OWrites[RetrieveForeignReliefsResponse] = Json.writes[RetrieveForeignReliefsResponse]

}
