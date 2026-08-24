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

package v3.otherReliefs.amend.def1.model.request

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v3.otherReliefs.amend.model.request.AmendOtherReliefsBody

case class Def1_AmendOtherReliefsRequestBody(
    nonDeductibleLoanInterest: Option[NonDeductibleLoanInterest],
    payrollGiving: Option[PayrollGiving],
    qualifyingDistributionRedemptionOfSharesAndSecurities: Option[QualifyingDistributionRedemptionOfSharesAndSecurities],
    maintenancePayments: Option[Seq[MaintenancePayments]],
    postCessationTradeReliefAndCertainOtherLosses: Option[Seq[PostCessationTradeReliefAndCertainOtherLosses]],
    annualPaymentsMade: Option[AnnualPaymentsMade],
    qualifyingLoanInterestPayments: Option[Seq[QualifyingLoanInterestPayments]])
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
  implicit val reads: Reads[Def1_AmendOtherReliefsRequestBody] = Json.reads[Def1_AmendOtherReliefsRequestBody]

  implicit val writes: OWrites[Def1_AmendOtherReliefsRequestBody] = (
    (JsPath \ "nonDeductableLoanInterest").writeNullable[NonDeductibleLoanInterest] and
      (JsPath \ "payrollGiving").writeNullable[PayrollGiving] and
      (JsPath \ "qualifyingDistributionRedemptionOfSharesAndSecurities").writeNullable[QualifyingDistributionRedemptionOfSharesAndSecurities] and
      (JsPath \ "maintenancePayments").writeNullable[Seq[MaintenancePayments]] and
      (JsPath \ "postCessationTradeReliefAndCertainOtherLosses").writeNullable[Seq[PostCessationTradeReliefAndCertainOtherLosses]] and
      (JsPath \ "annualPaymentsMade").writeNullable[AnnualPaymentsMade] and
      (JsPath \ "qualifyingLoanInterestPayments").writeNullable[Seq[QualifyingLoanInterestPayments]]
  )(o => Tuple.fromProductTyped(o))

}
