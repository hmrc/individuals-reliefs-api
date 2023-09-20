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

import api.controllers.validators.Validator
import api.controllers.validators.resolvers._
import api.models.errors.{
  BusinessNameFormatError,
  CustomerReferenceFormatError,
  DateFormatError,
  ExSpouseNameFormatError,
  IncomeSourceFormatError,
  LenderNameFormatError,
  MtdError,
  NatureOfTradeFormatError
}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import config.AppConfig
import play.api.libs.json.JsValue
import v1.models.request.amendOtherReliefs._

import javax.inject.{Inject, Singleton}
import scala.annotation.nowarn

@Singleton
class AmendOtherReliefsValidatorFactory @Inject() (appConfig: AppConfig) {

  private lazy val minTaxYear = 2021

  @nowarn("cat=lint-byname-implicit")
  private val resolveJson = new ResolveNonEmptyJsonObject[AmendOtherReliefsRequestBody]()

  private val resolveParsedNumber = ResolveParsedNumber()

  private val valid       = Valid(())
  private val stringRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  def validateCustomerRef(ref: String, path: String, error: MtdError = CustomerReferenceFormatError): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(ref)) valid
    else Invalid(List(error.withPath(path)))

  def validateFieldLength(field: String, path: String, error: MtdError): Validated[Seq[MtdError], Unit] =
    if (field.nonEmpty && field.length <= 105) valid
    else Invalid(List(error.withPath(path)))

  def validator(nino: String, taxYear: String, body: JsValue): Validator[AmendOtherReliefsRequestData] =
    new Validator[AmendOtherReliefsRequestData] {

      def validate: Validated[Seq[MtdError], AmendOtherReliefsRequestData] =
        (
          ResolveNino(nino),
          ResolveTaxYear(minTaxYear, taxYear, None, None),
          resolveJson(body)
        ).mapN(AmendOtherReliefsRequestData) andThen validateBusinessRules

      private def validateBusinessRules(parsed: AmendOtherReliefsRequestData): Validated[Seq[MtdError], AmendOtherReliefsRequestData] = {
        import parsed.body._

        val validatedNonDeductibleLoanInterest = nonDeductibleLoanInterest.map(validateNonDeductibleLoanInterest).getOrElse(valid)
        val validatedPayrollGiving             = payrollGiving.map(validatePayrollGiving).getOrElse(valid)
        val validatedQualifyingDistributionRedemptionOfSharesAndSecurities =
          qualifyingDistributionRedemptionOfSharesAndSecurities.map(validateQualifyingDistributionRedemptionOfSharesAndSecurities).getOrElse(valid)

        def zipAndValidate[A](fields: Option[Seq[A]], validate: (A, Int) => Validated[Seq[MtdError], Unit]) =
          fields
            .map(
              _.zipWithIndex
                .map { case (entry, index) => validate(entry, index) }
                .sequence
                .andThen(_ => valid))
            .getOrElse(valid)

        val validatedPostCessationTradeReliefAndCertainOtherLosses =
          zipAndValidate(postCessationTradeReliefAndCertainOtherLosses, validatePostCessationTradeReliefAndCertainOtherLosses)

        val validatedMaintenancePayments =
          zipAndValidate(maintenancePayments, validateMaintenancePayments)

        val validatedAnnualPaymentsMade = annualPaymentsMade.map(validateAnnualPaymentsMade).getOrElse(valid)
        val validatedQualifyingLoanInterestPayments =
          zipAndValidate(qualifyingLoanInterestPayments, validateQualifyingLoanInterestPayments)

        List(
          validatedNonDeductibleLoanInterest,
          validatedPayrollGiving,
          validatedQualifyingDistributionRedemptionOfSharesAndSecurities,
          validatedPostCessationTradeReliefAndCertainOtherLosses,
          validatedMaintenancePayments,
          validatedAnnualPaymentsMade,
          validatedQualifyingLoanInterestPayments
        ).sequence.map(_ => parsed)
      }

    }

  private def validateNonDeductibleLoanInterest(nonDeductibleLoanInterest: NonDeductibleLoanInterest): Validated[Seq[MtdError], Unit] = {
    import nonDeductibleLoanInterest._

    val validatedCustomerReference =
      customerReference.map(ref => validateCustomerRef(ref, "/nonDeductibleLoanInterest/customerReference")).getOrElse(valid)

    val validatedReliefClaimed = resolveParsedNumber(reliefClaimed, None, Some("/nonDeductibleLoanInterest/reliefClaimed"))

    List(validatedCustomerReference, validatedReliefClaimed).sequence.andThen(_ => valid)
  }

  private def validatePayrollGiving(payrollGiving: PayrollGiving): Validated[Seq[MtdError], Unit] = {
    import payrollGiving._

    val validatedCustomerReference =
      customerReference.map(ref => validateCustomerRef(ref, "/payrollGiving/customerReference")).getOrElse(valid)

    val validatedReliefClaimed = resolveParsedNumber(reliefClaimed, None, Some("/payrollGiving/reliefClaimed"))

    List(validatedCustomerReference, validatedReliefClaimed).sequence.andThen(_ => valid)
  }

  private def validateQualifyingDistributionRedemptionOfSharesAndSecurities(
      qualifyingDistributionRedemptionOfSharesAndSecurities: QualifyingDistributionRedemptionOfSharesAndSecurities)
      : Validated[Seq[MtdError], Unit] = {
    import qualifyingDistributionRedemptionOfSharesAndSecurities._

    val validatedCustomerReference =
      customerReference
        .map(ref => validateCustomerRef(ref, "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference"))
        .getOrElse(valid)

    val validatedAmount = resolveParsedNumber(amount, None, Some("/qualifyingDistributionRedemptionOfSharesAndSecurities/amount"))

    List(validatedCustomerReference, validatedAmount).sequence.andThen(_ => valid)
  }

  private def validatePostCessationTradeReliefAndCertainOtherLosses(
      postCessationTradeReliefAndCertainOtherLosses: PostCessationTradeReliefAndCertainOtherLosses,
      index: Int): Validated[Seq[MtdError], Unit] = {
    import postCessationTradeReliefAndCertainOtherLosses._

    val validatedCustomerReference =
      customerReference
        .map(ref => validateCustomerRef(ref, s"/postCessationTradeReliefAndCertainOtherLosses/$index/customerReference"))
        .getOrElse(valid)

    val validatedBusinessName =
      businessName
        .map(name => validateFieldLength(name, s"/postCessationTradeReliefAndCertainOtherLosses/$index/businessName", BusinessNameFormatError))
        .getOrElse(valid)

    val validatedDateBusinessCeased =
      dateBusinessCeased
        .map(date => ResolveIsoDate(date, Some(DateFormatError), Some(s"/postCessationTradeReliefAndCertainOtherLosses/$index/dateBusinessCeased")))
        .getOrElse(valid)

    val validatedNatureOfTrade =
      natureOfTrade
        .map(nature => validateCustomerRef(nature, s"/postCessationTradeReliefAndCertainOtherLosses/$index/natureOfTrade", NatureOfTradeFormatError))
        .getOrElse(valid)

    val validatedIncomeSource =
      incomeSource
        .map(source => validateFieldLength(source, s"/postCessationTradeReliefAndCertainOtherLosses/$index/incomeSource", IncomeSourceFormatError))
        .getOrElse(valid)

    val validatedAmount = resolveParsedNumber(amount, None, Some(s"/postCessationTradeReliefAndCertainOtherLosses/$index/amount"))

    List(
      validatedCustomerReference,
      validatedBusinessName,
      validatedDateBusinessCeased,
      validatedNatureOfTrade,
      validatedIncomeSource,
      validatedAmount).sequence.andThen(_ => valid)
  }

  private def validateMaintenancePayments(maintenancePayments: MaintenancePayments, index: Int): Validated[Seq[MtdError], Unit] = {
    import maintenancePayments._

    val validatedCustomerReference =
      customerReference.map(ref => validateCustomerRef(ref, s"/maintenancePayments/$index/customerReference")).getOrElse(valid)

    val validatedExSpouseName =
      exSpouseName.map(name => validateFieldLength(name, s"/maintenancePayments/$index/exSpouseName", ExSpouseNameFormatError)).getOrElse(valid)

    val validatedExSpouseDOB =
      exSpouseDateOfBirth
        .map(date => ResolveIsoDate(date, Some(DateFormatError), Some(s"/maintenancePayments/$index/exSpouseDateOfBirth")))
        .getOrElse(valid)

    val validatedAmount = resolveParsedNumber(amount, None, Some(s"/maintenancePayments/$index/amount"))

    List(validatedCustomerReference, validatedExSpouseName, validatedExSpouseDOB, validatedAmount).sequence.andThen(_ => valid)
  }

  private def validateAnnualPaymentsMade(annualPaymentsMade: AnnualPaymentsMade): Validated[Seq[MtdError], Unit] = {
    import annualPaymentsMade._

    val validatedCustomerReference =
      customerReference.map(ref => validateCustomerRef(ref, "/annualPaymentsMade/customerReference")).getOrElse(valid)

    val validatedReliefClaimed = resolveParsedNumber(reliefClaimed, None, Some("/annualPaymentsMade/reliefClaimed"))

    List(validatedCustomerReference, validatedReliefClaimed).sequence.andThen(_ => valid)
  }

  private def validateQualifyingLoanInterestPayments(qualifyingLoanInterestPayments: QualifyingLoanInterestPayments,
                                                     index: Int): Validated[Seq[MtdError], Unit] = {
    import qualifyingLoanInterestPayments._

    val validatedCustomerReference =
      customerReference.map(ref => validateCustomerRef(ref, s"/qualifyingLoanInterestPayments/$index/customerReference")).getOrElse(valid)

    val validatedFieldLength =
      lenderName.map(name => validateFieldLength(name, s"/qualifyingLoanInterestPayments/$index/lenderName", LenderNameFormatError)).getOrElse(valid)

    val validatedReliefClaimed = resolveParsedNumber(reliefClaimed, None, Some(s"/qualifyingLoanInterestPayments/$index/reliefClaimed"))

    List(validatedCustomerReference, validatedFieldLength, validatedReliefClaimed).sequence.andThen(_ => valid)
  }

}
