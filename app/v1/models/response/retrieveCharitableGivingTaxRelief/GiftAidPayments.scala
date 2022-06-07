/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.response.retrieveCharitableGivingTaxRelief

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

case class GiftAidPayments(nonUkCharities: Option[NonUkCharities],
                           totalAmount: Option[BigDecimal],
                           oneOffAmount: Option[BigDecimal],
                           amountTreatedAsPreviousTaxYear: Option[BigDecimal],
                           amountTreatedAsSpecifiedTaxYear: Option[BigDecimal])

object GiftAidPayments {

  implicit val reads: Reads[GiftAidPayments] = {
    val nonUkCharitiesOptionReads =
      ((JsPath \ "nonUkCharitiesCharityNames").readNullable[Seq[String]] and
        (JsPath \ "nonUkCharities").readNullable[BigDecimal])(NonUkCharities.from _)

    (nonUkCharitiesOptionReads and
      (JsPath \ "currentYear").readNullable[BigDecimal] and
      (JsPath \ "oneOffCurrentYear").readNullable[BigDecimal] and
      (JsPath \ "currentYearTreatedAsPreviousYear").readNullable[BigDecimal] and
      (JsPath \ "nextYearTreatedAsCurrentYear").readNullable[BigDecimal])(GiftAidPayments.apply _)
  }

  implicit val writes: Writes[GiftAidPayments] = Json.writes
}
