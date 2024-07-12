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

package v1.AmendOtherReliefs.def1.model.request

import play.api.libs.json.{Json, OFormat}
import v1.AmendOtherReliefs.model.request.AmendOtherReliefsBody

case class Def1_AmendOtherReliefsRequestBody(
    nonDeductibleLoanInterest: Option[Def1_NonDeductibleLoanInterest],
    payrollGiving: Option[Def1_PayrollGiving],
    qualifyingDistributionRedemptionOfSharesAndSecurities: Option[Def1_QualifyingDistributionRedemptionOfSharesAndSecurities],
    maintenancePayments: Option[Seq[Def1_MaintenancePayments]],
    postCessationTradeReliefAndCertainOtherLosses: Option[Seq[Def1_PostCessationTradeReliefAndCertainOtherLosses]],
    annualPaymentsMade: Option[Def1_AnnualPaymentsMade],
    qualifyingLoanInterestPayments: Option[Seq[Def1_QualifyingLoanInterestPayments]])
    extends AmendOtherReliefsBody {

  private def isEmpty: Boolean =
    nonDeductibleLoanInterest.isEmpty &&
      payrollGiving.isEmpty &&
      qualifyingDistributionRedemptionOfSharesAndSecurities.isEmpty &&
      maintenancePayments.isEmpty &&
      postCessationTradeReliefAndCertainOtherLosses.isEmpty &&
      annualPaymentsMade.isEmpty &&
      qualifyingLoanInterestPayments.isEmpty

  private def maintenancePaymentsIsEmpty: Boolean =
    maintenancePayments.isDefined && maintenancePayments.get.isEmpty

  private def postCessationTradeReliefAndCertainOtherLossesIsEmpty: Boolean =
    postCessationTradeReliefAndCertainOtherLosses.isDefined && postCessationTradeReliefAndCertainOtherLosses.get.isEmpty

  private def qualifyingLoanInterestPaymentsIsEmpty: Boolean =
    qualifyingLoanInterestPayments.isDefined && qualifyingLoanInterestPayments.get.isEmpty

  def isIncorrectOrEmptyBody: Boolean = isEmpty || {
    maintenancePaymentsIsEmpty ||
    postCessationTradeReliefAndCertainOtherLossesIsEmpty ||
    qualifyingLoanInterestPaymentsIsEmpty
  }

}

object Def1_AmendOtherReliefsRequestBody {
  implicit val format: OFormat[Def1_AmendOtherReliefsRequestBody] = Json.format[Def1_AmendOtherReliefsRequestBody]
}
