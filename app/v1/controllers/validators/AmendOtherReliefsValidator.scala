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
import api.controllers.validators.resolvers.{ResolveIsoDate, ResolveParsedNumber}
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import v1.models.request.amendOtherReliefs._

object AmendOtherReliefsValidator extends RulesValidator[AmendOtherReliefsRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  private val stringRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  private def validateCustomerRef(ref: String, path: String, error: MtdError = CustomerReferenceFormatError): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(ref)) valid
    else Invalid(List(error.withPath(path)))

  private def validateFieldLength(field: String, path: String, error: MtdError): Validated[Seq[MtdError], Unit] =
    if (field.nonEmpty && field.length <= 105) valid
    else Invalid(List(error.withPath(path)))

  def validateBusinessRules(parsed: AmendOtherReliefsRequestData): Validated[Seq[MtdError], AmendOtherReliefsRequestData] = {
    import parsed.body._

    val validatedNonDeductibleLoanInterest = nonDeductibleLoanInterest.map(validateNonDeductibleLoanInterest).getOrElse(valid)
    val validatedPayrollGiving             = payrollGiving.map(validatePayrollGiving).getOrElse(valid)
    val validatedQualifyingDistributionRedemptionOfSharesAndSecurities =
      qualifyingDistributionRedemptionOfSharesAndSecurities.map(validateQualifyingDistributionRedemptionOfSharesAndSecurities).getOrElse(valid)

    def zipAndValidate[A](fields: Option[Seq[A]], validate: (A, Int) => Validated[Seq[MtdError], Unit]) =
      fields
        .map(_.zipWithIndex.traverse(validate.tupled))
        .getOrElse(valid)

    val validatedPostCessationTradeReliefAndCertainOtherLosses =
      zipAndValidate(postCessationTradeReliefAndCertainOtherLosses, validatePostCessationTradeReliefAndCertainOtherLosses)

    val validatedMaintenancePayments =
      zipAndValidate(maintenancePayments, validateMaintenancePayments)

    val validatedAnnualPaymentsMade = annualPaymentsMade.map(validateAnnualPaymentsMade).getOrElse(valid)
    val validatedQualifyingLoanInterestPayments =
      zipAndValidate(qualifyingLoanInterestPayments, validateQualifyingLoanInterestPayments)

    combine(
      validatedNonDeductibleLoanInterest,
      validatedPayrollGiving,
      validatedQualifyingDistributionRedemptionOfSharesAndSecurities,
      validatedPostCessationTradeReliefAndCertainOtherLosses,
      validatedMaintenancePayments,
      validatedAnnualPaymentsMade,
      validatedQualifyingLoanInterestPayments
    ).onSuccess(parsed)
  }

  private def validateNonDeductibleLoanInterest(nonDeductibleLoanInterest: NonDeductibleLoanInterest): Validated[Seq[MtdError], Unit] = {
    import nonDeductibleLoanInterest._

    val validatedCustomerReference =
      customerReference.map(validateCustomerRef(_, "/nonDeductibleLoanInterest/customerReference")).getOrElse(valid)

    val validatedReliefClaimed = resolveParsedNumber(reliefClaimed, None, Some("/nonDeductibleLoanInterest/reliefClaimed"))

    combine(validatedCustomerReference, validatedReliefClaimed).andThen(_ => valid)
  }

  private def validatePayrollGiving(payrollGiving: PayrollGiving): Validated[Seq[MtdError], Unit] = {
    import payrollGiving._

    val validatedCustomerReference =
      customerReference.map(validateCustomerRef(_, "/payrollGiving/customerReference")).getOrElse(valid)

    val validatedReliefClaimed = resolveParsedNumber(reliefClaimed, None, Some("/payrollGiving/reliefClaimed"))

    combine(validatedCustomerReference, validatedReliefClaimed).andThen(_ => valid)
  }

  private def validateQualifyingDistributionRedemptionOfSharesAndSecurities(
      qualifyingDistributionRedemptionOfSharesAndSecurities: QualifyingDistributionRedemptionOfSharesAndSecurities)
      : Validated[Seq[MtdError], Unit] = {
    import qualifyingDistributionRedemptionOfSharesAndSecurities._

    val validatedCustomerReference =
      customerReference.map(validateCustomerRef(_, "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference")).getOrElse(valid)

    val validatedAmount = resolveParsedNumber(amount, None, Some("/qualifyingDistributionRedemptionOfSharesAndSecurities/amount"))

    combine(validatedCustomerReference, validatedAmount).andThen(_ => valid)
  }

  private def validatePostCessationTradeReliefAndCertainOtherLosses(
      postCessationTradeReliefAndCertainOtherLosses: PostCessationTradeReliefAndCertainOtherLosses,
      index: Int): Validated[Seq[MtdError], Unit] = {
    import postCessationTradeReliefAndCertainOtherLosses._

    val validatedCustomerReference =
      customerReference
        .map(validateCustomerRef(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/customerReference"))
        .getOrElse(valid)

    val validatedBusinessName =
      businessName
        .map(validateFieldLength(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/businessName", BusinessNameFormatError))
        .getOrElse(valid)

    val validatedDateBusinessCeased =
      dateBusinessCeased
        .map(ResolveIsoDate(_, Some(DateFormatError), Some(s"/postCessationTradeReliefAndCertainOtherLosses/$index/dateBusinessCeased")))
        .getOrElse(valid)

    val validatedNatureOfTrade =
      natureOfTrade
        .map(validateCustomerRef(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/natureOfTrade", NatureOfTradeFormatError))
        .getOrElse(valid)

    val validatedIncomeSource =
      incomeSource
        .map(validateFieldLength(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/incomeSource", IncomeSourceFormatError))
        .getOrElse(valid)

    val validatedAmount = resolveParsedNumber(amount, None, Some(s"/postCessationTradeReliefAndCertainOtherLosses/$index/amount"))

    combine(
      validatedCustomerReference,
      validatedBusinessName,
      validatedDateBusinessCeased,
      validatedNatureOfTrade,
      validatedIncomeSource,
      validatedAmount).andThen(_ => valid)
  }

  private def validateMaintenancePayments(maintenancePayments: MaintenancePayments, index: Int): Validated[Seq[MtdError], Unit] = {
    import maintenancePayments._

    val validatedCustomerReference =
      customerReference.map(validateCustomerRef(_, s"/maintenancePayments/$index/customerReference")).getOrElse(valid)

    val validatedExSpouseName =
      exSpouseName.map(validateFieldLength(_, s"/maintenancePayments/$index/exSpouseName", ExSpouseNameFormatError)).getOrElse(valid)

    val validatedExSpouseDOB =
      exSpouseDateOfBirth
        .map(ResolveIsoDate(_, Some(DateFormatError), Some(s"/maintenancePayments/$index/exSpouseDateOfBirth")))
        .getOrElse(valid)

    val validatedAmount = resolveParsedNumber(amount, None, Some(s"/maintenancePayments/$index/amount"))

    combine(validatedCustomerReference, validatedExSpouseName, validatedExSpouseDOB, validatedAmount).andThen(_ => valid)
  }

  private def validateAnnualPaymentsMade(annualPaymentsMade: AnnualPaymentsMade): Validated[Seq[MtdError], Unit] = {
    import annualPaymentsMade._

    val validatedCustomerReference =
      customerReference.map(validateCustomerRef(_, "/annualPaymentsMade/customerReference")).getOrElse(valid)

    val validatedReliefClaimed = resolveParsedNumber(reliefClaimed, None, Some("/annualPaymentsMade/reliefClaimed"))

    combine(validatedCustomerReference, validatedReliefClaimed).andThen(_ => valid)
  }

  private def validateQualifyingLoanInterestPayments(qualifyingLoanInterestPayments: QualifyingLoanInterestPayments,
                                                     index: Int): Validated[Seq[MtdError], Unit] = {
    import qualifyingLoanInterestPayments._

    val validatedCustomerReference =
      customerReference.map(validateCustomerRef(_, s"/qualifyingLoanInterestPayments/$index/customerReference")).getOrElse(valid)

    val validatedFieldLength =
      lenderName.map(validateFieldLength(_, s"/qualifyingLoanInterestPayments/$index/lenderName", LenderNameFormatError)).getOrElse(valid)

    val validatedReliefClaimed = resolveParsedNumber(reliefClaimed, None, Some(s"/qualifyingLoanInterestPayments/$index/reliefClaimed"))

    combine(validatedCustomerReference, validatedFieldLength, validatedReliefClaimed).andThen(_ => valid)
  }

}
