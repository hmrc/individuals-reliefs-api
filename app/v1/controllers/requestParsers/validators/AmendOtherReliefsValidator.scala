/*
 * Copyright 2020 HM Revenue & Customs
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

import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import v1.models.request.amendOtherReliefs._

class AmendOtherReliefsValidator extends Validator[AmendOtherReliefsRawData] {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidation, bodyFieldValidation)

  private def parameterFormatValidation: AmendOtherReliefsRawData => List[List[MtdError]] = (data: AmendOtherReliefsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidation: AmendOtherReliefsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendOtherReliefsBody](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyFieldValidation: AmendOtherReliefsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendOtherReliefsBody]

    List(flattenErrors(
      List(
        body.nonDeductableLoanInterest.map(validateNonDeductableLoanInterest).getOrElse(NoValidationErrors),
        body.payrollGiving.map(validatePayrollGiving).getOrElse(NoValidationErrors),
        body.qualifyingDistributionRedemptionOfSharesAndSecurities.map(validateQualifyingDistributionRedemptionOfSharesAndSecurities).getOrElse(NoValidationErrors),
        body.maintenancePayments.map(_.zipWithIndex.flatMap {
          case (item, i) => validateMaintenancePayments(item, i)
        }).getOrElse(NoValidationErrors).toList,
        body.postCessationTradeReliefAndCertainOtherLosses.map(_.zipWithIndex.flatMap {
          case (item, i) => validatePostCessationTradeReliefAndCertainOtherLosses(item, i)
        }).getOrElse(NoValidationErrors).toList,
        body.annualPaymentsMade.map(validateAnnualPayments).getOrElse(NoValidationErrors),
        body.qualifyingLoanInterestPayments.map(_.zipWithIndex.flatMap {
          case (item, i) => validateQualifyingLoanInterestPayments(item, i)
        }).getOrElse(NoValidationErrors).toList)
    ))
  }

  private def validateNonDeductableLoanInterest(nonDeductableLoanInterest: NonDeductableLoanInterest): List[MtdError] = {
    List(
      CustomerReferenceValidation.validateOptional(
        field = nonDeductableLoanInterest.customerReference,
        path = s"/nonDeductableLoanInterest/customerReference"
      ),
      NumberValidation.validateOptional(
        field = Some(nonDeductableLoanInterest.reliefClaimed),
        path = s"/nonDeductableLoanInterest/reliefClaimed"
      )
    ).flatten
  }

  private def validatePayrollGiving(payrollGiving: PayrollGiving): List[MtdError] = {
    List(
      CustomerReferenceValidation.validateOptional(
        field = payrollGiving.customerReference,
        path = s"/payrollGiving/customerReference"
      ),
      NumberValidation.validateOptional(
        field = Some(payrollGiving.reliefClaimed),
        path = s"/payrollGiving/reliefClaimed"
      )
    ).flatten
  }

  private def validateQualifyingDistributionRedemptionOfSharesAndSecurities
  (qualifyingDistributionRedemptionOfSharesAndSecurities: QualifyingDistributionRedemptionOfSharesAndSecurities): List[MtdError] = {
    List(
      CustomerReferenceValidation.validateOptional(
        field = qualifyingDistributionRedemptionOfSharesAndSecurities.customerReference,
        path = s"/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference"
      ),
      NumberValidation.validateOptional(
        field = Some(qualifyingDistributionRedemptionOfSharesAndSecurities.amount),
        path = s"/qualifyingDistributionRedemptionOfSharesAndSecurities/amount"
      )
    ).flatten
  }


  private def validateMaintenancePayments(maintenancePayments: MaintenancePayments, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerReferenceValidation.validateOptional(
        field = Some(maintenancePayments.customerReference),
        path = s"/maintenancePayments/$arrayIndex/customerReference"
      ),
      DateValidation.validateFormatDateOptional(
        date = maintenancePayments.exSpouseDateOfBirth,
        path = s"/maintenancePayments/$arrayIndex/exSpouseDateOfBirth"
      ),
      NumberValidation.validateOptional(
        field = maintenancePayments.amount,
        path = s"/maintenancePayments/$arrayIndex/amount"
      )
    ).flatten
  }

  private def validatePostCessationTradeReliefAndCertainOtherLosses
  (postCessationTradeReliefAndCertainOtherLosses: PostCessationTradeReliefAndCertainOtherLosses, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerReferenceValidation.validateOptional(
        field = Some(postCessationTradeReliefAndCertainOtherLosses.customerReference),
        path = s"/postCessationTradeReliefAndCertainOtherLosses/$arrayIndex/customerReference"
      ),
      DateValidation.validateFormatDateOptional(
        date = postCessationTradeReliefAndCertainOtherLosses.dateBusinessCeased,
        path = s"/postCessationTradeReliefAndCertainOtherLosses/$arrayIndex/dateBusinessCeased"
      ),
      NumberValidation.validateOptional(
        field = postCessationTradeReliefAndCertainOtherLosses.amount,
        path = s"/postCessationTradeReliefAndCertainOtherLosses/$arrayIndex/amount"
      )
    ).flatten
  }


  private def validateAnnualPayments(annualPaymentsMade: AnnualPaymentsMade): List[MtdError] = {
    List(
      CustomerReferenceValidation.validateOptional(
        field = annualPaymentsMade.customerReference,
        path = s"/annualPaymentsMade/customerReference"
      ),
      NumberValidation.validateOptional(
        field = Some(annualPaymentsMade.reliefClaimed),
        path = s"/annualPaymentsMade/reliefClaimed"
      )
    ).flatten
  }

  private def validateQualifyingLoanInterestPayments(qualifyingLoanInterestPayments: QualifyingLoanInterestPayments, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerReferenceValidation.validateOptional(
        field = Some(qualifyingLoanInterestPayments.customerReference),
        path = s"/qualifyingLoanInterestPayments/$arrayIndex/customerReference"
      ),
      NumberValidation.validateOptional(
        field = Some(qualifyingLoanInterestPayments.reliefClaimed),
        path = s"/qualifyingLoanInterestPayments/$arrayIndex/reliefClaimed"
      )
    ).flatten
  }


  override def validate(data: AmendOtherReliefsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
