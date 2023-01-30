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

package v1.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations._
import api.models.errors.{MtdError, RuleGiftAidNonUkAmountWithoutNamesError, RuleGiftsNonUkAmountWithoutNamesError, StringFormatError}
import v1.models.request.createAndAmendCharitableGivingTaxRelief._

class AmendCharitableGivingReliefValidator extends Validator[CreateAndAmendCharitableGivingTaxReliefRawData] {
  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidation, bodyFieldValidation)

  private def parameterFormatValidation: CreateAndAmendCharitableGivingTaxReliefRawData => List[List[MtdError]] =
    (data: CreateAndAmendCharitableGivingTaxReliefRawData) => {
      List(
        NinoValidation.validate(data.nino),
        TaxYearValidation.validate(data.taxYear)
      )
    }

  private def parameterRuleValidation: CreateAndAmendCharitableGivingTaxReliefRawData => List[List[MtdError]] =
    (data: CreateAndAmendCharitableGivingTaxReliefRawData) => {
      List(
        MtdTaxYearValidation.validate(data.taxYear, charitableGivingMinimumTaxYear)
      )
    }

  private def bodyFormatValidation: CreateAndAmendCharitableGivingTaxReliefRawData => List[List[MtdError]] = { data =>
    JsonFormatValidation.validateAndCheckNonEmpty[CreateAndAmendCharitableGivingTaxReliefBody](data.body) match {
      case Nil          => NoValidationErrors
      case schemaErrors => List(schemaErrors)
    }
  }

  private def bodyFieldValidation: CreateAndAmendCharitableGivingTaxReliefRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[CreateAndAmendCharitableGivingTaxReliefBody]

    List(
      flattenErrors(
        List(body.giftAidPayments.map(validateGiftAid).getOrElse(Nil), body.gifts.map(validateGifts).getOrElse(Nil))
      )
    )
  }

  private def validateGiftAid(giftAidPayments: GiftAidPayments): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = giftAidPayments.nonUkCharities.map(_.totalAmount),
        path = s"/giftAidPayments/nonUkCharities/totalAmount"
      ),
      NumberValidation.validateOptional(
        field = giftAidPayments.totalAmount,
        path = s"/giftAidPayments/totalAmount"
      ),
      NumberValidation.validateOptional(
        field = giftAidPayments.oneOffAmount,
        path = s"/giftAidPayments/oneOffAmount"
      ),
      NumberValidation.validateOptional(
        field = giftAidPayments.amountTreatedAsPreviousTaxYear,
        path = s"/giftAidPayments/amountTreatedAsPreviousTaxYear"
      ),
      NumberValidation.validateOptional(
        field = giftAidPayments.amountTreatedAsSpecifiedTaxYear,
        path = s"/giftAidPayments/amountTreatedAsSpecifiedTaxYear"
      ),
      validateMissingCharityNames(giftAidPayments.nonUkCharities, RuleGiftAidNonUkAmountWithoutNamesError),
      validateFormatCharityNames(giftAidPayments.nonUkCharities, "/giftAidPayments/nonUkCharities/charityNames")
    ).flatten
  }

  private def validateGifts(gifts: Gifts): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = gifts.nonUkCharities.map(_.totalAmount),
        path = s"/gifts/nonUkCharities/totalAmount"
      ),
      NumberValidation.validateOptional(
        field = gifts.landAndBuildings,
        path = s"/gifts/landAndBuildings"
      ),
      NumberValidation.validateOptional(
        field = gifts.sharesOrSecurities,
        path = s"/gifts/sharesOrSecurities"
      ),
      validateMissingCharityNames(gifts.nonUkCharities, RuleGiftsNonUkAmountWithoutNamesError),
      validateFormatCharityNames(gifts.nonUkCharities, "/gifts/nonUkCharities/charityNames")
    ).flatten
  }

  private def validateMissingCharityNames(nonUkCharities: Option[NonUkCharities], error: MtdError): Seq[MtdError] =
    nonUkCharities
      .map(nonUk => if (NonUkCharitiesValidation.hasMissingNames(nonUk)) List(error) else Nil)
      .getOrElse(Nil)

  private def validateFormatCharityNames(nonUkCharities: Option[NonUkCharities], path: String): Seq[MtdError] =
    nonUkCharities
      .map(_.charityNames match {
        case None => Nil
        case Some(names) =>
          names.zipWithIndex.collect {
            case (name, i) if !NonUkCharitiesValidation.isNameValid(name) => StringFormatError.copy(paths = Some(Seq(s"$path/$i")))
          }
      })
      .getOrElse(Nil)

  override def validate(data: CreateAndAmendCharitableGivingTaxReliefRawData): List[MtdError] =
    run(validationSet, data).distinct

}
