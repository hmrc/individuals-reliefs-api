/*
 * Copyright 2024 HM Revenue & Customs
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

package v1.otherReliefs.retrieve.def1.model.response

import hateoas.HateoasLinks
import play.api.libs.json.{Json, OWrites, Reads}
import shared.models.domain.Timestamp
import v1.otherReliefs.retrieve.model.response.RetrieveOtherReliefsResponse

case class Def1_RetrieveOtherReliefsResponse(
    submittedOn: Timestamp,
    nonDeductibleLoanInterest: Option[NonDeductibleLoanInterest],
    payrollGiving: Option[PayrollGiving],
    qualifyingDistributionRedemptionOfSharesAndSecurities: Option[QualifyingDistributionRedemptionOfSharesAndSecurities],
    maintenancePayments: Option[Seq[MaintenancePayments]],
    postCessationTradeReliefAndCertainOtherLosses: Option[Seq[PostCessationTradeReliefAndCertainOtherLosses]],
    annualPaymentsMade: Option[AnnualPaymentsMade],
    qualifyingLoanInterestPayments: Option[Seq[QualifyingLoanInterestPayments]]
) extends RetrieveOtherReliefsResponse

object Def1_RetrieveOtherReliefsResponse extends HateoasLinks {

  implicit val reads: Reads[Def1_RetrieveOtherReliefsResponse]    = Json.reads[Def1_RetrieveOtherReliefsResponse]
  implicit val writes: OWrites[Def1_RetrieveOtherReliefsResponse] = Json.writes[Def1_RetrieveOtherReliefsResponse]

}
