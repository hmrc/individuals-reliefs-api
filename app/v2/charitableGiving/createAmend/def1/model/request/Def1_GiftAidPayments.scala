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

package v2.charitableGiving.createAmend.def1.model.request

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Def1_GiftAidPayments(nonUkCharities: Option[Def1_NonUkCharities],
                                totalAmount: Option[BigDecimal],
                                oneOffAmount: Option[BigDecimal],
                                amountTreatedAsPreviousTaxYear: Option[BigDecimal],
                                amountTreatedAsSpecifiedTaxYear: Option[BigDecimal])

object Def1_GiftAidPayments {

  implicit val reads: Reads[Def1_GiftAidPayments] = Json.reads[Def1_GiftAidPayments]

  implicit val writes: OWrites[Def1_GiftAidPayments] = (
    (JsPath \ "nonUkCharitiesCharityNames").writeNullable[Seq[String]] and
      (JsPath \ "nonUkCharities").writeNullable[BigDecimal] and
      (JsPath \ "currentYear").writeNullable[BigDecimal] and
      (JsPath \ "oneOffCurrentYear").writeNullable[BigDecimal] and
      (JsPath \ "currentYearTreatedAsPreviousYear").writeNullable[BigDecimal] and
      (JsPath \ "nextYearTreatedAsCurrentYear").writeNullable[BigDecimal]
  )(giftAidPayments =>
    (
      giftAidPayments.nonUkCharities.flatMap(_.charityNames),
      giftAidPayments.nonUkCharities.map(_.totalAmount),
      giftAidPayments.totalAmount,
      giftAidPayments.oneOffAmount,
      giftAidPayments.amountTreatedAsPreviousTaxYear,
      giftAidPayments.amountTreatedAsSpecifiedTaxYear
    ))

}
