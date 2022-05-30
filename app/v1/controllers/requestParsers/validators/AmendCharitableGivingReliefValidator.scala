/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v1.controllers.requestParsers.validators

import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.{MtdError, RuleGiftAidNonUkAmountWithoutNamesError, RuleGiftsNonUkInvestmentsAmountWithoutNamesError, StringFormatError}
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
      giftAidPayments.nonUkCharities
        .map(NonUkCharitiesValidation.hasMissingNames)
        .map(missingNames => if (missingNames) List(RuleGiftAidNonUkAmountWithoutNamesError) else Nil)
        .getOrElse(Nil),
      giftAidPayments.nonUkCharities.map(x => validateCharityNames(x.charityNames, "/giftAidPayments/nonUkCharities/charityNames")).getOrElse(Nil)
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
      gifts.nonUkCharities
        .map(NonUkCharitiesValidation.hasMissingNames)
        .map(missingNames => if (missingNames) List(RuleGiftsNonUkInvestmentsAmountWithoutNamesError) else Nil)
        .getOrElse(Nil),
      gifts.nonUkCharities.map(x => validateCharityNames(x.charityNames, "/gifts/nonUkCharities/charityNames")).getOrElse(Nil)
    ).flatten
  }

  private def validateCharityNames(charityNames: Option[Seq[String]], path: String): Seq[MtdError] = {
    charityNames match {
      case None => Nil
      case Some(names) =>
        names.zipWithIndex.collect {
          case (name, i) if !NonUkCharitiesValidation.isNameValid(name) => StringFormatError.copy(paths = Some(Seq(s"$path/$i")))
        }
    }
  }

  override def validate(data: CreateAndAmendCharitableGivingTaxReliefRawData): List[MtdError] =
    run(validationSet, data).distinct

}
