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

import api.controllers.validators.{RulesValidator, ValidatorOps}
import api.controllers.validators.resolvers.{ResolveIsoDate, ResolveParsedNumber}
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.Invalid
import v1.models.request.amendOtherReliefs._

object AmendOtherReliefsValidator extends RulesValidator[AmendOtherReliefsRequestData] with ValidatorOps {

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

    combine(
      nonDeductibleLoanInterest.mapOrElse(validateNonDeductibleLoanInterest),
      payrollGiving.mapOrElse(validatePayrollGiving),
      qualifyingDistributionRedemptionOfSharesAndSecurities.mapOrElse(validateQualifyingDistributionRedemptionOfSharesAndSecurities),
      postCessationTradeReliefAndCertainOtherLosses.zipAndValidate(validatePostCessationTradeReliefAndCertainOtherLosses),
      maintenancePayments.zipAndValidate(validateMaintenancePayments),
      annualPaymentsMade.mapOrElse(validateAnnualPaymentsMade),
      qualifyingLoanInterestPayments.zipAndValidate(validateQualifyingLoanInterestPayments)
    ).onSuccess(parsed)
  }

  private def validateNonDeductibleLoanInterest(nonDeductibleLoanInterest: NonDeductibleLoanInterest): Validated[Seq[MtdError], Unit] = {
    import nonDeductibleLoanInterest._

    combine(
      customerReference.mapOrElse(validateCustomerRef(_, "/nonDeductibleLoanInterest/customerReference")),
      resolveParsedNumber(reliefClaimed, None, Some("/nonDeductibleLoanInterest/reliefClaimed"))
    ).andThen(_ => valid)
  }

  private def validatePayrollGiving(payrollGiving: PayrollGiving): Validated[Seq[MtdError], Unit] = {
    import payrollGiving._

    combine(
      customerReference.mapOrElse(validateCustomerRef(_, "/payrollGiving/customerReference")),
      resolveParsedNumber(reliefClaimed, None, Some("/payrollGiving/reliefClaimed"))
    ).andThen(_ => valid)
  }

  private def validateQualifyingDistributionRedemptionOfSharesAndSecurities(
      qualifyingDistributionRedemptionOfSharesAndSecurities: QualifyingDistributionRedemptionOfSharesAndSecurities)
      : Validated[Seq[MtdError], Unit] = {
    import qualifyingDistributionRedemptionOfSharesAndSecurities._

    combine(
      customerReference.mapOrElse(validateCustomerRef(_, "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference")),
      resolveParsedNumber(amount, None, Some("/qualifyingDistributionRedemptionOfSharesAndSecurities/amount"))
    ).andThen(_ => valid)
  }

  private def validatePostCessationTradeReliefAndCertainOtherLosses(
      postCessationTradeReliefAndCertainOtherLosses: PostCessationTradeReliefAndCertainOtherLosses,
      index: Int): Validated[Seq[MtdError], Unit] = {
    import postCessationTradeReliefAndCertainOtherLosses._

    val validatedCustomerReference =
      customerReference
        .mapOrElse(validateCustomerRef(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/customerReference"))

    val validatedBusinessName =
      businessName
        .mapOrElse(validateFieldLength(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/businessName", BusinessNameFormatError))

    val validatedDateBusinessCeased =
      dateBusinessCeased
        .mapOrElse(
          ResolveIsoDate(_, Some(DateFormatError), Some(s"/postCessationTradeReliefAndCertainOtherLosses/$index/dateBusinessCeased")).andThen(_ =>
            valid))

    val validatedNatureOfTrade =
      natureOfTrade
        .mapOrElse(validateCustomerRef(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/natureOfTrade", NatureOfTradeFormatError))

    val validatedIncomeSource =
      incomeSource
        .mapOrElse(validateFieldLength(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/incomeSource", IncomeSourceFormatError))

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

    val validatedExSpouseDOB =
      exSpouseDateOfBirth
        .mapOrElse(ResolveIsoDate(_, Some(DateFormatError), Some(s"/maintenancePayments/$index/exSpouseDateOfBirth")).andThen(_ => valid))

    combine(
      customerReference.mapOrElse(validateCustomerRef(_, s"/maintenancePayments/$index/customerReference")),
      exSpouseName.mapOrElse(validateFieldLength(_, s"/maintenancePayments/$index/exSpouseName", ExSpouseNameFormatError)),
      validatedExSpouseDOB,
      resolveParsedNumber(amount, None, Some(s"/maintenancePayments/$index/amount"))
    ).andThen(_ => valid)
  }

  private def validateAnnualPaymentsMade(annualPaymentsMade: AnnualPaymentsMade): Validated[Seq[MtdError], Unit] = {
    import annualPaymentsMade._

    combine(
      customerReference.mapOrElse(validateCustomerRef(_, "/annualPaymentsMade/customerReference")),
      resolveParsedNumber(reliefClaimed, None, Some("/annualPaymentsMade/reliefClaimed"))
    ).andThen(_ => valid)
  }

  private def validateQualifyingLoanInterestPayments(qualifyingLoanInterestPayments: QualifyingLoanInterestPayments,
                                                     index: Int): Validated[Seq[MtdError], Unit] = {
    import qualifyingLoanInterestPayments._

    combine(
      customerReference.mapOrElse(validateCustomerRef(_, s"/qualifyingLoanInterestPayments/$index/customerReference")),
      lenderName.mapOrElse(validateFieldLength(_, s"/qualifyingLoanInterestPayments/$index/lenderName", LenderNameFormatError)),
      resolveParsedNumber(reliefClaimed, None, Some(s"/qualifyingLoanInterestPayments/$index/reliefClaimed"))
    ).andThen(_ => valid)
  }

}
