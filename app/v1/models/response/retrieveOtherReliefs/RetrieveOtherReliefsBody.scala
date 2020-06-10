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

package v1.models.response.retrieveOtherReliefs

import config.AppConfig
import play.api.libs.json.{Json, OFormat}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveOtherReliefsBody(nonDeductableLoanInterest: Option[NonDeductableLoanInterest],
                                 payrollGiving: Option[PayrollGiving],
                                 qualifyingDistributionRedemptionOfSharesAndSecurities: Option[QualifyingDistributionRedemptionOfSharesAndSecurities],
                                 maintenancePayments: Option[Seq[MaintenancePayments]],
                                 postCessationTradeReliefAndCertainOtherLosses: Option[Seq[PostCessationTradeReliefAndCertainOtherLosses]],
                                 annualPaymentsMade: Option[AnnualPaymentsMade],
                                 qualifyingLoanInterestPayments: Option[Seq[QualifyingLoanInterestPayments]])

object RetrieveOtherReliefsBody extends HateoasLinks {
  implicit val format: OFormat[RetrieveOtherReliefsBody] = Json.format[RetrieveOtherReliefsBody]

  implicit object RetrieveOtherOrderLinksFactory extends HateoasLinksFactory[RetrieveOtherReliefsBody, RetrieveOtherReliefsHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveOtherReliefsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveOtherReliefs(appConfig, nino, taxYear),
        amendOtherReliefs(appConfig, nino, taxYear),
        deleteOtherReliefs(appConfig, nino, taxYear)
      )
    }
  }
}

case class RetrieveOtherReliefsHateoasData(nino: String, taxYear: String) extends HateoasData