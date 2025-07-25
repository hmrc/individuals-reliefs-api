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

package v3.otherReliefs.amend.def1

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.toFoldableOps
import common._
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveIsoDate, ResolveParsedNumber}
import shared.models.errors._
import v3.otherReliefs.amend.def1.model.request._

import java.time.LocalDate

object Def1_AmendOtherReliefsRulesValidator extends RulesValidator[Def1_AmendOtherReliefsRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  private val stringRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  private val minYear = 1900
  private val maxYear = 2100

  def validateBusinessRules(parsed: Def1_AmendOtherReliefsRequestData): Validated[Seq[MtdError], Def1_AmendOtherReliefsRequestData] = {
    import parsed.body._

    combine(
      nonDeductibleLoanInterest.traverse_(validateNonDeductibleLoanInterest),
      payrollGiving.traverse_(validatePayrollGiving),
      qualifyingDistributionRedemptionOfSharesAndSecurities.traverse_(validateQualifyingDistributionRedemptionOfSharesAndSecurities),
      zipAndValidate(postCessationTradeReliefAndCertainOtherLosses, validatePostCessationTradeReliefAndCertainOtherLosses),
      zipAndValidate(maintenancePayments, validateMaintenancePayments),
      annualPaymentsMade.traverse_(validateAnnualPaymentsMade),
      zipAndValidate(qualifyingLoanInterestPayments, validateQualifyingLoanInterestPayments)
    ).onSuccess(parsed)
  }

  private def zipAndValidate[VALUE](maybeFields: Option[Seq[VALUE]],
                                    validate: (VALUE, Int) => Validated[Seq[MtdError], Unit]): Validated[Seq[MtdError], Unit] =
    maybeFields.traverse_(_.zipWithIndex.traverse_(validate.tupled))

  private def validateCustomerRef(ref: String, path: String, error: MtdError = CustomerReferenceFormatError): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(ref)) valid
    else Invalid(List(error.withPath(path)))

  private def validateFieldLength(field: String, path: String, error: MtdError): Validated[Seq[MtdError], Unit] =
    if (field.nonEmpty && field.length <= 105) valid
    else Invalid(List(error.withPath(path)))

  private def validateNonDeductibleLoanInterest(nonDeductibleLoanInterest: NonDeductibleLoanInterest): Validated[Seq[MtdError], Unit] = {
    import nonDeductibleLoanInterest._

    combine(
      customerReference.traverse_(validateCustomerRef(_, "/nonDeductibleLoanInterest/customerReference")),
      resolveParsedNumber(reliefClaimed, "/nonDeductibleLoanInterest/reliefClaimed")
    )
  }

  private def validatePayrollGiving(payrollGiving: PayrollGiving): Validated[Seq[MtdError], Unit] = {
    import payrollGiving._

    combine(
      customerReference.traverse_(validateCustomerRef(_, "/payrollGiving/customerReference")),
      resolveParsedNumber(reliefClaimed, "/payrollGiving/reliefClaimed")
    )
  }

  private def validateQualifyingDistributionRedemptionOfSharesAndSecurities(
      qualifyingDistributionRedemptionOfSharesAndSecurities: QualifyingDistributionRedemptionOfSharesAndSecurities)
      : Validated[Seq[MtdError], Unit] = {
    import qualifyingDistributionRedemptionOfSharesAndSecurities._

    combine(
      customerReference.traverse_(validateCustomerRef(_, "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference")),
      resolveParsedNumber(amount, "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount")
    )
  }

  private def validatePostCessationTradeReliefAndCertainOtherLosses(
      postCessationTradeReliefAndCertainOtherLosses: PostCessationTradeReliefAndCertainOtherLosses,
      index: Int): Validated[Seq[MtdError], Unit] = {
    import postCessationTradeReliefAndCertainOtherLosses._
    val dateBusinessCeasedPath = s"/postCessationTradeReliefAndCertainOtherLosses/$index/dateBusinessCeased"
    combine(
      customerReference.traverse_(validateCustomerRef(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/customerReference")),
      businessName.traverse_(validateFieldLength(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/businessName", BusinessNameFormatError)),
      dateBusinessCeased.traverse_(
        ResolveIsoDate(_, DateFormatError.withPath(dateBusinessCeasedPath)).andThen(isDateInRange(_, dateBusinessCeasedPath))
      ),
      natureOfTrade.traverse_(
        validateCustomerRef(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/natureOfTrade", NatureOfTradeFormatError)),
      incomeSource.traverse_(validateFieldLength(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/incomeSource", IncomeSourceFormatError)),
      resolveParsedNumber(amount, s"/postCessationTradeReliefAndCertainOtherLosses/$index/amount")
    )
  }

  private def validateMaintenancePayments(maintenancePayments: MaintenancePayments, index: Int): Validated[Seq[MtdError], Unit] = {
    import maintenancePayments._

    val exSpouseDateOfBirthPath = s"/maintenancePayments/$index/exSpouseDateOfBirth"
    combine(
      customerReference.traverse_(validateCustomerRef(_, s"/maintenancePayments/$index/customerReference")),
      exSpouseName.traverse_(validateFieldLength(_, s"/maintenancePayments/$index/exSpouseName", ExSpouseNameFormatError)),
      exSpouseDateOfBirth.traverse_(
        ResolveIsoDate(_, DateFormatError.withPath(exSpouseDateOfBirthPath)).andThen(isDateInRange(_, exSpouseDateOfBirthPath))
      ),
      resolveParsedNumber(amount, s"/maintenancePayments/$index/amount")
    )
  }

  private def validateAnnualPaymentsMade(annualPaymentsMade: AnnualPaymentsMade): Validated[Seq[MtdError], Unit] = {
    import annualPaymentsMade._

    combine(
      customerReference.traverse_(validateCustomerRef(_, "/annualPaymentsMade/customerReference")),
      resolveParsedNumber(reliefClaimed, "/annualPaymentsMade/reliefClaimed")
    )
  }

  private def validateQualifyingLoanInterestPayments(qualifyingLoanInterestPayments: QualifyingLoanInterestPayments,
                                                     index: Int): Validated[Seq[MtdError], Unit] = {
    import qualifyingLoanInterestPayments._

    combine(
      customerReference.traverse_(validateCustomerRef(_, s"/qualifyingLoanInterestPayments/$index/customerReference")),
      lenderName.traverse_(validateFieldLength(_, s"/qualifyingLoanInterestPayments/$index/lenderName", LenderNameFormatError)),
      resolveParsedNumber(reliefClaimed, s"/qualifyingLoanInterestPayments/$index/reliefClaimed")
    )
  }

  private def isDateInRange(date: LocalDate, path: String): Validated[Seq[MtdError], Unit] = {
    if (date.getYear >= minYear && date.getYear < maxYear) Valid(()) else Invalid(List(DateFormatError.withPath(path)))
  }

}
