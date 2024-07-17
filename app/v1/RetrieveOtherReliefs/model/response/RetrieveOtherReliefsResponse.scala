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

package v1.RetrieveOtherReliefs.model.response

import api.hateoas.{HateoasData, HateoasLinks, HateoasLinksFactory, Link}
import api.models.domain.Timestamp
import config.AppConfig
import play.api.libs.json.{Json, OWrites, Reads}
import v1.RetrieveOtherReliefs.def1.model.response._
import v1.RetrieveOtherReliefs.model.response.Def1_RetrieveOtherReliefsResponse.Def1_RetrieveOtherReliefsLinksFactory

sealed trait RetrieveOtherReliefsResponse

object RetrieveOtherReliefsResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveOtherReliefsResponse] = OWrites[RetrieveOtherReliefsResponse] { case def1: Def1_RetrieveOtherReliefsResponse =>
    Json.toJsObject(def1)
  }

  implicit object LinksFactory extends HateoasLinksFactory[RetrieveOtherReliefsResponse, RetrieveOtherReliefsHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveOtherReliefsHateoasData): Seq[Link] = {
      data.taxYear match {
        case _ => Def1_RetrieveOtherReliefsLinksFactory.links(appConfig, data)
      }
    }

  }

}

case class RetrieveOtherReliefsHateoasData(nino: String, taxYear: String) extends HateoasData

case class Def1_RetrieveOtherReliefsResponse(
    submittedOn: Timestamp,
    nonDeductibleLoanInterest: Option[Def1_NonDeductibleLoanInterest],
    payrollGiving: Option[Def1_PayrollGiving],
    qualifyingDistributionRedemptionOfSharesAndSecurities: Option[Def1_QualifyingDistributionRedemptionOfSharesAndSecurities],
    maintenancePayments: Option[Seq[Def1_MaintenancePayments]],
    postCessationTradeReliefAndCertainOtherLosses: Option[Seq[Def1_PostCessationTradeReliefAndCertainOtherLosses]],
    annualPaymentsMade: Option[Def1_AnnualPaymentsMade],
    qualifyingLoanInterestPayments: Option[Seq[Def1_QualifyingLoanInterestPayments]]
) extends RetrieveOtherReliefsResponse

object Def1_RetrieveOtherReliefsResponse extends HateoasLinks {

  implicit val reads: Reads[Def1_RetrieveOtherReliefsResponse]    = Json.reads[Def1_RetrieveOtherReliefsResponse]
  implicit val writes: OWrites[Def1_RetrieveOtherReliefsResponse] = Json.writes[Def1_RetrieveOtherReliefsResponse]

  implicit object Def1_RetrieveOtherReliefsLinksFactory
      extends HateoasLinksFactory[Def1_RetrieveOtherReliefsResponse, RetrieveOtherReliefsHateoasData] {

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
