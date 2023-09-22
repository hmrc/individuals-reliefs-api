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

import api.controllers.validators.{Validator, ValidatorOps}
import api.controllers.validators.resolvers._
import api.models.domain.TaxYear
import api.models.errors.{MtdError, RuleGiftAidNonUkAmountWithoutNamesError, RuleGiftsNonUkAmountWithoutNamesError, StringFormatError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import play.api.libs.json.JsValue
import v1.models.request.createAndAmendCharitableGivingTaxRelief._

import javax.inject.Singleton
import scala.annotation.nowarn

@Singleton
class CreateAndAmendCharitableGivingReliefValidatorFactory extends ValidatorOps {

  private val resolveParsedNumber = ResolveParsedNumber()

  private val charityNamesRegex = "^[A-Za-z0-9 &'()*,\\-./@£]{1,75}$".r

  @nowarn("cat=lint-byname-implicit")
  private val resolveJson = new ResolveNonEmptyJsonObject[CreateAndAmendCharitableGivingTaxReliefBody]()

  private val valid = Valid(())

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAndAmendCharitableGivingTaxReliefRequestData] =
    new Validator[CreateAndAmendCharitableGivingTaxReliefRequestData] {

      def validate: Validated[Seq[MtdError], CreateAndAmendCharitableGivingTaxReliefRequestData] =
        (
          ResolveNino(nino),
          ResolveTaxYear(TaxYear.charitableGivingMinimumTaxYear.year, taxYear, None, None),
          resolveJson(body)
        ).mapN(CreateAndAmendCharitableGivingTaxReliefRequestData) andThen validateBusinessRules

      private def validateBusinessRules(parsed: CreateAndAmendCharitableGivingTaxReliefRequestData)
          : Validated[Seq[MtdError], CreateAndAmendCharitableGivingTaxReliefRequestData] = {
        import parsed.body._

        val validatedGiftAidPayments = giftAidPayments.mapOrElse(validateGiftAidPayments)
        val validatedGifts           = gifts.mapOrElse(validateGifts)

        List(validatedGiftAidPayments, validatedGifts).traverse(identity).map(_ => parsed)
      }

    }

  def validateGiftAidPayments(giftAidPayments: GiftAidPayments): Validated[Seq[MtdError], Unit] = {
    import giftAidPayments._

    val validatedNumericFields = validateWithPaths(
      (nonUkCharities.map(_.totalAmount), "/giftAidPayments/nonUkCharities/totalAmount"),
      (totalAmount, "/giftAidPayments/totalAmount"),
      (oneOffAmount, "/giftAidPayments/oneOffAmount"),
      (amountTreatedAsPreviousTaxYear, "/giftAidPayments/amountTreatedAsPreviousTaxYear"),
      (amountTreatedAsSpecifiedTaxYear, "/giftAidPayments/amountTreatedAsSpecifiedTaxYear")
    )(resolveParsedNumber(_: BigDecimal, None, _: Option[String]))

    val validatedCharityNames = nonUkCharities.mapOrElse(validateNonUkCharities(_, "/giftAidPayments", RuleGiftAidNonUkAmountWithoutNamesError))

    List(validatedNumericFields, validatedCharityNames).sequence.andThen(_ => valid)

  }

  def validateGifts(gifts: Gifts): Validated[Seq[MtdError], Unit] = {
    import gifts._

    val validatedNumericFields = validateWithPaths(
      (nonUkCharities.map(_.totalAmount), "/gifts/nonUkCharities/totalAmount"),
      (landAndBuildings, "/gifts/landAndBuildings"),
      (sharesOrSecurities, "/gifts/sharesOrSecurities")
    )(resolveParsedNumber(_: BigDecimal, None, _: Option[String]))

    val validatedCharityNames = nonUkCharities.mapOrElse(validateNonUkCharities(_, "/gifts", RuleGiftsNonUkAmountWithoutNamesError))

    List(validatedNumericFields, validatedCharityNames).sequence.andThen(_ => valid)
  }

  def validateNonUkCharities(nonUkCharities: NonUkCharities, path: String, missingCharityNamesError: MtdError): Validated[Seq[MtdError], Unit] = {
    import nonUkCharities._

    val validatedMissingCharityNames = nonUkCharities match {
      case NonUkCharities(_, totalAmount) if totalAmount <= 0 => valid
      case NonUkCharities(None, _)                            => Invalid(List(missingCharityNamesError))
      case NonUkCharities(Some(cns), _)                       => if (cns.isEmpty) Invalid(List(missingCharityNamesError)) else valid
    }

    val validateCharityNamesFormat = (name: String, index: Int) =>
      if (charityNamesRegex.matches(name)) valid
      else Invalid(List(StringFormatError.withPath(s"$path/nonUkCharities/charityNames/$index")))

    val validatedCharityNamesFormat = charityNames.zipAndValidate(validateCharityNamesFormat)

    List(validatedCharityNamesFormat, validatedMissingCharityNames).sequence.andThen(_ => valid)
  }

}
