/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.CreateAndAmendCharitableGivingReliefs.def1.model.request

import play.api.libs.json.{JsObject, Json, Reads, Writes}

case class Def1_GiftAidPayments(nonUkCharities: Option[Def1_NonUkCharities],
                           totalAmount: Option[BigDecimal],
                           oneOffAmount: Option[BigDecimal],
                           amountTreatedAsPreviousTaxYear: Option[BigDecimal],
                           amountTreatedAsSpecifiedTaxYear: Option[BigDecimal])

object Def1_GiftAidPayments {

  implicit val reads: Reads[Def1_GiftAidPayments] = Json.reads[Def1_GiftAidPayments]

  implicit val writes: Writes[Def1_GiftAidPayments] = new Writes[Def1_GiftAidPayments] {

    def writes(o: Def1_GiftAidPayments): JsObject = Json.obj(
      "nonUkCharitiesCharityNames"       -> o.nonUkCharities.map(_.charityNames),
      "nonUkCharities"                   -> o.nonUkCharities.map(_.totalAmount),
      "currentYear"                      -> o.totalAmount,
      "oneOffCurrentYear"                -> o.oneOffAmount,
      "currentYearTreatedAsPreviousYear" -> o.amountTreatedAsPreviousTaxYear,
      "nextYearTreatedAsCurrentYear"     -> o.amountTreatedAsSpecifiedTaxYear
    )

  }

}
