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

package v1.controllers.validators

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.ResolveParsedNumber
import api.models.errors.{MtdError, RuleGiftAidNonUkAmountWithoutNamesError, RuleGiftsNonUkAmountWithoutNamesError, StringFormatError}
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.{toFoldableOps, toTraverseOps}
import v1.models.request.createAndAmendCharitableGivingTaxRelief.{
  CreateAndAmendCharitableGivingTaxReliefRequestData,
  GiftAidPayments,
  Gifts,
  NonUkCharities
}

object CreateAndAmendCharitableGivingReliefRulesValidator extends RulesValidator[CreateAndAmendCharitableGivingTaxReliefRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  private val charityNamesRegex = "^[A-Za-z0-9 &'()*,\\-./@£]{1,75}$".r

  def validateBusinessRules(
      parsed: CreateAndAmendCharitableGivingTaxReliefRequestData): Validated[Seq[MtdError], CreateAndAmendCharitableGivingTaxReliefRequestData] = {
    import parsed.body._

    combine(giftAidPayments.traverse_(validate), gifts.traverse_(validate)).onSuccess(parsed)
  }

  def validate(giftAidPayments: GiftAidPayments): Validated[Seq[MtdError], Unit] = {
    import giftAidPayments._

    val validatedNumericFields = List(
      (nonUkCharities.map(_.totalAmount), "/giftAidPayments/nonUkCharities/totalAmount"),
      (totalAmount, "/giftAidPayments/totalAmount"),
      (oneOffAmount, "/giftAidPayments/oneOffAmount"),
      (amountTreatedAsPreviousTaxYear, "/giftAidPayments/amountTreatedAsPreviousTaxYear"),
      (amountTreatedAsSpecifiedTaxYear, "/giftAidPayments/amountTreatedAsSpecifiedTaxYear")
    ).traverse_ { case (value, path) =>
      resolveParsedNumber(value, path = Some(path))
    }

    val validatedCharityNames =
      nonUkCharities.traverse_(validate(_, "/giftAidPayments", RuleGiftAidNonUkAmountWithoutNamesError))

    combine(validatedNumericFields, validatedCharityNames)
  }

  def validate(gifts: Gifts): Validated[Seq[MtdError], Unit] = {
    import gifts._

    val validatedNumericFields = List(
      (nonUkCharities.map(_.totalAmount), "/gifts/nonUkCharities/totalAmount"),
      (landAndBuildings, "/gifts/landAndBuildings"),
      (sharesOrSecurities, "/gifts/sharesOrSecurities")
    ).traverse_ { case (value, path) => resolveParsedNumber(value, path = Some(path)) }

    val validatedCharityNames = nonUkCharities.traverse_(validate(_, "/gifts", RuleGiftsNonUkAmountWithoutNamesError))

    combine(validatedNumericFields, validatedCharityNames)
  }

  def validate(nonUkCharities: NonUkCharities, path: String, missingCharityNamesError: MtdError): Validated[Seq[MtdError], Unit] = {
    import nonUkCharities._

    val validatedMissingCharityNames = nonUkCharities match {
      case NonUkCharities(_, totalAmount) if totalAmount <= 0 => valid
      case NonUkCharities(None, _)                            => Invalid(List(missingCharityNamesError))
      case NonUkCharities(Some(charityNames), _)              => if (charityNames.isEmpty) Invalid(List(missingCharityNamesError)) else valid
    }

    val validateCharityNamesFormat = (name: String, index: Int) =>
      if (charityNamesRegex.matches(name)) valid
      else Invalid(List(StringFormatError.withPath(s"$path/nonUkCharities/charityNames/$index")))

    val validatedCharityNamesFormat = charityNames.traverse_(_.zipWithIndex.traverse(validateCharityNamesFormat.tupled))

    combine(validatedCharityNamesFormat, validatedMissingCharityNames)
  }

}
