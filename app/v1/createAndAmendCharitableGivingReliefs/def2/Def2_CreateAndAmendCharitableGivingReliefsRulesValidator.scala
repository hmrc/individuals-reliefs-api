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

package v1.createAndAmendCharitableGivingReliefs.def2

import cats.data.Validated
import cats.implicits.toFoldableOps
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.ResolveParsedNumber
import shared.models.errors.MtdError
import v1.createAndAmendCharitableGivingReliefs.def2.model.request
import v1.createAndAmendCharitableGivingReliefs.def2.model.request.{Def2_GiftAidPayments, Def2_Gifts}
import v1.createAndAmendCharitableGivingReliefs.model.request.Def2_CreateAndAmendCharitableGivingTaxReliefsRequestData

class Def2_CreateAndAmendCharitableGivingReliefsRulesValidator extends RulesValidator[Def2_CreateAndAmendCharitableGivingTaxReliefsRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  def validateBusinessRules(parsed: Def2_CreateAndAmendCharitableGivingTaxReliefsRequestData)
      : Validated[Seq[MtdError], Def2_CreateAndAmendCharitableGivingTaxReliefsRequestData] = {

    import parsed.body.*
    combine(giftAidPayments.traverse_(validateGiftAid), gifts.traverse_(validate)).onSuccess(parsed)
  }

  def validateGiftAid(giftAidPayments: request.Def2_GiftAidPayments): Validated[Seq[MtdError], Unit] = {
    import giftAidPayments.*

    val validatedNumericFields = List(
      (totalAmount, "/giftAidPayments/totalAmount"),
      (oneOffAmount, "/giftAidPayments/oneOffAmount"),
      (amountTreatedAsPreviousTaxYear, "/giftAidPayments/amountTreatedAsPreviousTaxYear"),
      (amountTreatedAsSpecifiedTaxYear, "/giftAidPayments/amountTreatedAsSpecifiedTaxYear")
    ).traverse_ { case (value, path) =>
      resolveParsedNumber(value, path)
    }

    combine(validatedNumericFields)
  }

  def validate(gifts: request.Def2_Gifts): Validated[Seq[MtdError], Unit] = {
    import gifts.*

    val validatedNumericFields = List(
      (landAndBuildings, "/gifts/landAndBuildings"),
      (sharesOrSecurities, "/gifts/sharesOrSecurities")
    ).traverse_ { case (value, path) => resolveParsedNumber(value, path) }

    combine(validatedNumericFields)
  }

}
