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

package v2.createAndAmendCharitableGivingReliefs.def1

import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.{toFoldableOps, toTraverseOps}
import common.{RuleGiftAidNonUkAmountWithoutNamesError, RuleGiftsNonUkAmountWithoutNamesError}
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.ResolveParsedNumber
import shared.models.errors.{MtdError, StringFormatError}
import v2.createAndAmendCharitableGivingReliefs.def1.model.request.{Def1_GiftAidPayments, Def1_Gifts, Def1_NonUkCharities}
import v2.createAndAmendCharitableGivingReliefs.model.request.Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData

class Def1_CreateAndAmendCharitableGivingReliefsRulesValidator extends RulesValidator[Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  private val charityNamesRegex = "^[A-Za-z0-9 &'()*,\\-./@£]{1,75}$".r

  def validateBusinessRules(parsed: Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData)
      : Validated[Seq[MtdError], Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData] = {

    import parsed.body._
    combine(giftAidPayments.traverse_(validateGiftAid), gifts.traverse_(validate)).onSuccess(parsed)
  }

  def validateGiftAid(giftAidPayments: Def1_GiftAidPayments): Validated[Seq[MtdError], Unit] = {
    import giftAidPayments._

    val validatedNumericFields = List(
      (nonUkCharities.map(_.totalAmount), "/giftAidPayments/nonUkCharities/totalAmount"),
      (totalAmount, "/giftAidPayments/totalAmount"),
      (oneOffAmount, "/giftAidPayments/oneOffAmount"),
      (amountTreatedAsPreviousTaxYear, "/giftAidPayments/amountTreatedAsPreviousTaxYear"),
      (amountTreatedAsSpecifiedTaxYear, "/giftAidPayments/amountTreatedAsSpecifiedTaxYear")
    ).traverse_ { case (value, path) =>
      resolveParsedNumber(value, path)
    }

    val validatedCharityNames =
      nonUkCharities.traverse_(validate(_, "/giftAidPayments", RuleGiftAidNonUkAmountWithoutNamesError))

    combine(validatedNumericFields, validatedCharityNames)
  }

  def validate(gifts: Def1_Gifts): Validated[Seq[MtdError], Unit] = {
    import gifts._

    val validatedNumericFields = List(
      (nonUkCharities.map(_.totalAmount), "/gifts/nonUkCharities/totalAmount"),
      (landAndBuildings, "/gifts/landAndBuildings"),
      (sharesOrSecurities, "/gifts/sharesOrSecurities")
    ).traverse_ { case (value, path) => resolveParsedNumber(value, path) }

    val validatedCharityNames = nonUkCharities.traverse_(validate(_, "/gifts", RuleGiftsNonUkAmountWithoutNamesError))

    combine(validatedNumericFields, validatedCharityNames)
  }

  def validate(nonUkCharities: Def1_NonUkCharities, path: String, missingCharityNamesError: MtdError): Validated[Seq[MtdError], Unit] = {
    import nonUkCharities._

    val validatedMissingCharityNames = nonUkCharities match {
      case Def1_NonUkCharities(_, totalAmount) if totalAmount <= 0 => valid
      case Def1_NonUkCharities(None, _)                            => Invalid(List(missingCharityNamesError))
      case Def1_NonUkCharities(Some(charityNames), _)              => if (charityNames.isEmpty) Invalid(List(missingCharityNamesError)) else valid
    }

    val validateCharityNamesFormat = (name: String, index: Int) =>
      if (charityNamesRegex.matches(name)) {
        valid
      } else { Invalid(List(StringFormatError.withPath(s"$path/nonUkCharities/charityNames/$index"))) }

    val validatedCharityNamesFormat = charityNames.traverse_(_.zipWithIndex.traverse(validateCharityNamesFormat.tupled))

    combine(validatedCharityNamesFormat, validatedMissingCharityNames)
  }

}
