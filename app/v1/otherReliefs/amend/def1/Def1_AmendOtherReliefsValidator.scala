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

package v1.otherReliefs.amend.def1

import api.controllers.validators.resolvers._
import api.controllers.validators.{RulesValidator, Validator}
import api.models.domain.TaxYear
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import play.api.libs.json.JsValue
import v1.otherReliefs.amend.def1.Def1_AmendOtherReliefsValidator.validateBusinessRules
import v1.otherReliefs.amend.def1.model.request._
import v1.otherReliefs.amend.model.request.AmendOtherReliefsRequestData

import java.time.LocalDate
import javax.inject.Inject
import scala.annotation.nowarn

class Def1_AmendOtherReliefsValidator @Inject() (nino: String, taxYear: String, body: JsValue) extends Validator[AmendOtherReliefsRequestData] {

  @nowarn("cat=lint-byname-implicit")
  private val resolveJson = new ResolveNonEmptyJsonObject[AmendOtherReliefsRequestBody]()

  def validate: Validated[Seq[MtdError], AmendOtherReliefsRequestData] =
    (
      ResolveNino(nino),
      ResolveTaxYear(TaxYear.minimumTaxYear.year, taxYear, None, None),
      resolveJson(body)
    ).mapN(Def1_AmendOtherReliefsRequestData) andThen validateBusinessRules

}

object Def1_AmendOtherReliefsValidator extends RulesValidator[AmendOtherReliefsRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()

  private val stringRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  private val minYear = 1900
  private val maxYear = 2100

  def validateBusinessRules(parsed: AmendOtherReliefsRequestData): Validated[Seq[MtdError], AmendOtherReliefsRequestData] = {

    val body = parsed.body

    combine(
      body.nonDeductibleLoanInterest.traverse_(validateNonDeductibleLoanInterest),
      body.payrollGiving.traverse_(validatePayrollGiving),
      body.qualifyingDistributionRedemptionOfSharesAndSecurities.traverse_(validateQualifyingDistributionRedemptionOfSharesAndSecurities),
      zipAndValidate(body.postCessationTradeReliefAndCertainOtherLosses, validatePostCessationTradeReliefAndCertainOtherLosses),
      zipAndValidate(body.maintenancePayments, validateMaintenancePayments),
      body.annualPaymentsMade.traverse_(validateAnnualPaymentsMade),
      zipAndValidate(body.qualifyingLoanInterestPayments, validateQualifyingLoanInterestPayments)
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
      resolveParsedNumber(reliefClaimed, None, Some("/nonDeductibleLoanInterest/reliefClaimed"))
    )
  }

  private def validatePayrollGiving(payrollGiving: PayrollGiving): Validated[Seq[MtdError], Unit] = {
    import payrollGiving._

    combine(
      customerReference.traverse_(validateCustomerRef(_, "/payrollGiving/customerReference")),
      resolveParsedNumber(reliefClaimed, path = Some("/payrollGiving/reliefClaimed"))
    )
  }

  private def validateQualifyingDistributionRedemptionOfSharesAndSecurities(
      qualifyingDistributionRedemptionOfSharesAndSecurities: QualifyingDistributionRedemptionOfSharesAndSecurities)
      : Validated[Seq[MtdError], Unit] = {
    import qualifyingDistributionRedemptionOfSharesAndSecurities._

    combine(
      customerReference.traverse_(validateCustomerRef(_, "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference")),
      resolveParsedNumber(amount, path = Some("/qualifyingDistributionRedemptionOfSharesAndSecurities/amount"))
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
        ResolveIsoDate(_, Some(DateFormatError), Some(dateBusinessCeasedPath))
          .andThen(isDateInRange(_, dateBusinessCeasedPath))),
      natureOfTrade.traverse_(
        validateCustomerRef(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/natureOfTrade", NatureOfTradeFormatError)),
      incomeSource.traverse_(validateFieldLength(_, s"/postCessationTradeReliefAndCertainOtherLosses/$index/incomeSource", IncomeSourceFormatError)),
      resolveParsedNumber(amount, None, Some(s"/postCessationTradeReliefAndCertainOtherLosses/$index/amount"))
    )
  }

  private def validateMaintenancePayments(maintenancePayments: MaintenancePayments, index: Int): Validated[Seq[MtdError], Unit] = {
    import maintenancePayments._

    val exSpouseDateOfBirthPath = s"/maintenancePayments/$index/exSpouseDateOfBirth"
    combine(
      customerReference.traverse_(validateCustomerRef(_, s"/maintenancePayments/$index/customerReference")),
      exSpouseName.traverse_(validateFieldLength(_, s"/maintenancePayments/$index/exSpouseName", ExSpouseNameFormatError)),
      exSpouseDateOfBirth.traverse_(
        ResolveIsoDate(_, Some(DateFormatError), Some(exSpouseDateOfBirthPath)).andThen(isDateInRange(_, exSpouseDateOfBirthPath))),
      resolveParsedNumber(amount, path = Some(s"/maintenancePayments/$index/amount"))
    )
  }

  private def validateAnnualPaymentsMade(annualPaymentsMade: AnnualPaymentsMade): Validated[Seq[MtdError], Unit] = {
    import annualPaymentsMade._

    combine(
      customerReference.traverse_(validateCustomerRef(_, "/annualPaymentsMade/customerReference")),
      resolveParsedNumber(reliefClaimed, path = Some("/annualPaymentsMade/reliefClaimed"))
    )
  }

  private def validateQualifyingLoanInterestPayments(qualifyingLoanInterestPayments: QualifyingLoanInterestPayments,
                                                     index: Int): Validated[Seq[MtdError], Unit] = {
    import qualifyingLoanInterestPayments._

    combine(
      customerReference.traverse_(validateCustomerRef(_, s"/qualifyingLoanInterestPayments/$index/customerReference")),
      lenderName.traverse_(validateFieldLength(_, s"/qualifyingLoanInterestPayments/$index/lenderName", LenderNameFormatError)),
      resolveParsedNumber(reliefClaimed, path = Some(s"/qualifyingLoanInterestPayments/$index/reliefClaimed"))
    )
  }

  private def isDateInRange(date: LocalDate, path: String): Validated[Seq[MtdError], Unit] = {
    if (date.getYear >= minYear && date.getYear < maxYear) Valid(()) else Invalid(List(DateFormatError.withPath(path)))
  }

}
